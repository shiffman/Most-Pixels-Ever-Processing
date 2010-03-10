package mpe.client;

import java.io.*;
import java.net.*;

import mpe.config.*;

public class AsyncClient {
	
	
    /** If DEBUG is true, the client will print lots of messages about what it is doing.
     * Set with debug=1; in your INI file. */
    
	public static boolean DEBUG = true;
    
    protected FileParser fp;
    
    protected boolean running;
    
    // TCP stuff
    String hostName;
    int serverPort = 9003;
    Socket socket;
    InputStream is;
    //DataInputStream dis;
    BufferedReader brin;
    DataOutputStream dos;
    OutputStream os;
    
    /**
     * Builds a default AsyncClient. It connects to 'localhost' on port 9003.
     */
    public AsyncClient() {
        this("localhost", 9003);
    }
    
    /**
     * Builds an AsyncClient.
     * 
     * @param _hostName the server host name
     * @param _serverPort the server port
     */
    public AsyncClient(String _hostName, int _serverPort) {
        connect(_hostName, _serverPort);
    }
    
    /**
     * Builds an AsyncClient using an INI file.
     * 
     * @param _fileString the path to the INI file 
     
     */
    public AsyncClient(String _fileString) {
        loadIniFile(_fileString);
        connect(hostName, serverPort);
    }
    
    /**
     * Loads the settings from the Client INI file.
     * 
     * @param fileString the path to the INI file 
     */
    protected void loadIniFile(String _fileString) {
        fp = new FileParser(_fileString);
        
        if (fp.fileExists()) {
            // parse INI file
            setServer(fp.getStringValue("server"));
            setPort(fp.getIntValue("port"));
            out("Settings: server = " + hostName + ":" + serverPort);
            if (fp.getIntValue("debug") == 1) DEBUG = true;
        }
    }
    
    /**
     * Connects to the server.
     * 
     * @param _hostName the server host name
     * @param _serverPort the server port
     */
    protected void connect(String _hostName, int _serverPort) {
        // create a socket
        try {
        	socket = new Socket(_hostName, _serverPort);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
        } catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // set the server address and port
        setServer(_hostName);
        setPort(_serverPort);
    }
    
    /**
     * Sets the server address.
     * 
     * @param _hostName the server host name
     */
    protected void setServer(String _hostName) {
        if (_hostName != null) {
            hostName = _hostName;
        }
    }
    
    /**
     * Sets the server port.
     * 
     * @param _serverPort the server port
     */
    protected void setPort(int _serverPort) {
        if (_serverPort > -1)
            serverPort = _serverPort;
    }
    
    public int getPort() { return serverPort; }
    
    /**
     * Format a broadcast message and send it.
     * 
     * @param _msg the message to broadcast
     */
    public void broadcast(String _msg) {
        // The AsyncClient doesn't need to prepend
    	// _msg = "T"+ _msg;
        send(_msg);
    }
    
    /**
     * Send a message to the server using TCP.
     * 
     * @param _msg the message to send
     */
    private void send(String _msg) {
        if (DEBUG) out("Sending: " + _msg);
        _msg += "\n";
        try {
            dos.write(_msg.getBytes());
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Outputs a message to the console.
     * 
     * @param _str the message to output.
     */
    protected void out(String _str) {
        print(_str);
    }
    
    /**
     * Outputs a message to the console.
     * 
     * @param _str the message to output.
     */
    protected void print(String _str) {
        System.out.println("Client: " + _str);
    }
    
    /**
     * Outputs an error message to the console.
     * 
     * @param _str the message to output.
     */
    protected void err(String _str) {
        System.err.println("Client: " + _str);
    }
}
