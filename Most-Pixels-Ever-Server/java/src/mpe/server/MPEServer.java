/**
 * The "Most Pixels Ever" Wallserver.
 * @author Shiffman and Kairalla
 *
 */

package mpe.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class MPEServer {

	private HashMap<Integer,Connection> connectionlookup = new HashMap<Integer,Connection>();
	private ArrayList<Connection> synchconnections = new ArrayList<Connection>();
	private ArrayList<Connection> asynchconnections = new ArrayList<Connection>();
	private HashMap<Integer,ArrayList<String>> messages = new HashMap<Integer,ArrayList<String>>();

	private int port;
	int frameCount = 0;
	private long before;

	// Server will add a message to the frameEvent
	// public boolean newMessage = false;
	// public String message = "";

	public boolean dataload = false;

	public int numRequiredClients = 1;
	public int frameRate = 30;

	public boolean verbose = false;

	public boolean waitForAll = false;
	private boolean running = false;
	public boolean allConnected = false;  // When true, we are off and running

	public boolean paused;

	// TODO: What's the correct relative path for the default file?
	public static final String defaultSettingsFile = "../data/settings.xml";

	public void setFramerate(int fr){
		if (fr > -1) frameRate = fr;

	}
	public void setRequiredClients(int sc){
		if (sc > -1) {
			numRequiredClients = sc;
			waitForAll = true;
		} 
	}

	public MPEServer(int _screens, int _framerate, int _port, boolean v) {
		setRequiredClients(_screens);
		setFramerate(_framerate);
		port = _port;
		verbose = v;
		out("framerate = " + frameRate + ", screens = " + numRequiredClients + ", waitForAll = " + waitForAll + ", verbose = " + verbose);
	}

	public int totalConnections() {
		return synchconnections.size();
	}

	public void run() {
		running = true;
		before = System.currentTimeMillis(); // Getting the current time
		ServerSocket frontDoor;
		try {
			frontDoor = new ServerSocket(port);

			System.out.println("Starting server: " + InetAddress.getLocalHost() + "  " + frontDoor.getLocalPort());

			// Wait for connections (could thread this)
			while (running) {
				Socket socket = frontDoor.accept();  // BLOCKING!                       
				System.out.println(socket.getRemoteSocketAddress() + " connected.");
				// Make  and start connection object
				Connection conn = new Connection(socket,this);
				conn.start();
			}
		} catch (IOException e) {
			System.out.println("Zoinks!" + e);
		}
	}

	public synchronized void addMessage(String message, Integer fromClientId, ArrayList<Integer> toClientIDs)
	{    
		String formattedMessage = fromClientId + "," + message;
		for (Integer cID : toClientIDs)
		{
			ArrayList<String> clientMessages = messages.get(cID);
			if (clientMessages == null)
			{
				clientMessages = new ArrayList<String>();
				messages.put(cID, clientMessages);
			}
			clientMessages.add(formattedMessage);
		}
	}

	public ArrayList<Integer> receivingClientIDs()
	{
		ArrayList<Integer> clientIDs = new ArrayList<Integer>();
		// Add all client IDs to the message
		for (Connection conn : synchconnections) {
			clientIDs.add(conn.clientID);
		}
		for (Connection conn : asynchconnections) {
			if (conn.asynchReceive) {
				clientIDs.add(conn.clientID);
			}
		}            
		return clientIDs;
	}

	// Synchronize?!!!
	public synchronized void triggerFrame(boolean reset) {
		int desired = (int) ((1.0f / (float) frameRate) * 1000.0f);
		long now = System.currentTimeMillis();
		int diff = (int) (now - before);
		if (diff < desired) {
			// Where do we max out a framerate?  Here?
			try {
				long sleepTime = desired-diff;
				if (sleepTime >= 0){
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 

		if (reset) {
			// set frameCount to 0 and clear any data
			resetFrameCount();
			String send = "R|"+frameCount;
			sendAll(send);
		} else {
			String send = "G|"+frameCount;
			// Adding a data message to the frameEvent
			// substring removes the ":" at the end.
			if (messages.size() > 0)
			{
				ArrayList<Integer> rcvClientIDs = receivingClientIDs();
				for (Integer clientID : rcvClientIDs) {          
					String clientSend = send;
					ArrayList<String> messageBodies = messages.get(clientID);
					if (messageBodies != null) {
						for (String message : messageBodies) {
							clientSend += "|" + message;
						}              
					}
					Connection c = connectionlookup.get(clientID);
					// Make sure the client should receive messages
					if (c != null){
						c.send(clientSend);
					}
				}
				messages.clear();
			}
			else
			{
				sendAll(send);
			}
			/*
			if (newMessage && message.length() > 0) {
        send += "|" + message.substring(0, message.length()-1);
      }
			newMessage = false;
			message = "";
			sendAll(send);
			 */
		}

		// After frame is triggered all connections should be set to "unready"
		for (Connection c : synchconnections) {
			c.ready = false;
		}

		before = System.currentTimeMillis();
	}

	private void print(String string) {
		System.out.println("MPEServer: "+string);

	}

	public synchronized void sendAll(String msg){
		int howmany = 0;
		for (Connection conn : asynchconnections) {
			if (conn.asynchReceive) {
				howmany++;
			}
		}

		if (verbose) {
			System.out.println("Sending to " + synchconnections.size() + " sync clients, " + howmany + " async clients: " + msg);
		}

		ArrayList<Integer> rcvClientIDs = receivingClientIDs();
		for (Integer clientID : rcvClientIDs) {
			Connection c = connectionlookup.get(clientID);
			if (c != null){        
				c.send(msg);
			}
		}    
	}

	public void killConnection(int i){
		Connection c = connectionlookup.get(i);
		connectionlookup.remove(i);
		synchconnections.remove(c);

		// TODO: asynch connections remove also?
	}

	boolean allDisconected(){
		if (synchconnections.size() < 1){
			return true;
		} else return false;
	}

	void resetFrameCount(){
		frameCount = 0;
		//newMessage = false;
		//message = "";
		messages.clear();
		if (verbose) {
			System.out.println("resetting frame count.");
		}
	}

	public void killServer() {
		running = false;
	}

	public static void main(String[] args) {

		boolean help = false;

		/*
      Collecting Server Params:
      1) Add default values to serverParams.
      2) Collect command line arguments.
      3) Check if there's an XML path in the CL args.
      4) Check if there's a default settings.xml file.
      5) If 3 or 4 is true, parse the file and overwrite the default values.
      6) Parse the command line arguments to clobber any existing values.
		 */

		HashMap<String,Integer> serverParams = new HashMap<String,Integer>();
		HashMap<String,String> runArgs = new HashMap<String,String>();

		// Default values
		serverParams.put("screens", -1);
		serverParams.put("framerate", 30);
		serverParams.put("port", 9002);
		serverParams.put("verbose", 0);

		// Collect command line args
		for (int i = 0; i < args.length; i++) {
			if (args[i].contains("-screens")) {
				runArgs.put("screens", args[i].substring(8));
			} 
			else if (args[i].contains("-framerate")) {
				runArgs.put("framerate", args[i].substring(10));
			} 
			else if (args[i].contains("-port")) {
				runArgs.put("port", args[i].substring(5));
			}
			else if (args[i].contains("-verbose")) {
				runArgs.put("verbose", "1");
			}
			else if (args[i].contains("-xml")) {
				String settingsFile = args[i].substring(4);
				// Remove double quotes from the filename
				settingsFile = settingsFile.replaceAll("^\"|\"$", "");
				runArgs.put("xml", settingsFile);
			}
			else {
				help = true;
			}
		}

		// Settings.xml
		// If there's an XML file (passed in or default), parse it.
		File settingsFile = null;
		String filename = defaultSettingsFile;
		if (runArgs.get("xml") != null) {
			filename = runArgs.get("xml");
		}
		settingsFile = new File(filename);
		if (settingsFile.exists()) {
			out("Reading settings from "+filename);
			if (!parseXMLFile(settingsFile, serverParams)) {
				help = true;
			}
		} else {
			out("Cannot find XML Settings file. Using defaults.");
		}

		// Override any of the default or xml files with the command line args
		for (String key : runArgs.keySet()) {
			if (key != "xml") {
				try {
					serverParams.put(key, Integer.parseInt(runArgs.get(key)));
				} catch (Exception e) {
					out("ERROR: I can't parse " + key + ": " + runArgs.get(key) + "\n" + e);
					help = true;          
				}
			}
		}

		if (help) {
			// if anything unrecognized is sent to the command line, help is
			// displayed and the server quits
			System.out.println(" * The \"Most Pixels Ever\" server.\n" +
					" * This server can accept the following parameters from the command line:\n" +
					" * -screens<number of screens> Total # of expected clients. If no value is provided, the server will reset when each sync client joins.\n" +
					" * -framerate<framerate> Desired frame rate.  Defaults to 30\n" +
					" * -port<port number> Defines the port.  Defaults to 9002\n" +
					" * -verbose Turns debugging messages on.\n" +
					" * -xml<path to file with XML settings>  Path to initialization file.  Defaults to \""+defaultSettingsFile+"\".\n");
			System.exit(1);
		}

		MPEServer ws = new MPEServer(serverParams.get("screens"), serverParams.get("framerate"), 
				serverParams.get("port"), serverParams.get("verbose") == 1);
		ws.run();
	}

	private static void out(String s) {
		System.out.println("MPEServer: "+ s);
	}

	public static boolean parseXMLFile(File settingsFile, HashMap<String,Integer> serverParams) {    
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(settingsFile);

			NodeList settingsNodes = doc.getElementsByTagName("settings");
			if (settingsNodes.getLength() == 0) {
				throw new Exception("Could not find <settings> node XML file");
			}

			NodeList nodeList = settingsNodes.item(0).getChildNodes();   

			for (int count = 0; count < nodeList.getLength(); count++) 
			{
				Node element = nodeList.item(count);
				String nodeName = element.getNodeName().toLowerCase();          
				String value = null;

				if (element.getChildNodes().getLength() > 0) {
					value = element.getFirstChild().getNodeValue();
				}

				if (nodeName.equals("port") || nodeName.equals("screens") || 
						nodeName.equals("framerate") || nodeName.equals("verbose")) {                     
					// Convert any booleans to ints
					if (value.toLowerCase().equals("true")) {
						value = "1";
					} else if (value.toLowerCase().equals("false")) {
						value = "0";
					}

					try {
						serverParams.put(nodeName, Integer.parseInt(value));
					} catch (Exception e) {
						out("PARSE ERROR: I can't parse " + nodeName + ": " + value + "\n" + e);
						return false;
					}
				}                    
			} 
		} catch(Exception e) {
			out("ERROR: Cannot parse XML file.\n"+ e );
			return false;
		} 
		return true;
	}

	// synchronize??
	public synchronized void setReady(int clientID) { 
		Connection c = connectionlookup.get(clientID);
		c.ready = true;
		if (!paused && isReady()) {
			frameCount++;
			triggerFrame(false);
		}
	}

	// synchronize?
	public synchronized boolean isReady() {
		boolean allReady = true;
		for (Connection c : synchconnections) {
			if (!c.ready) {
				allReady = false;
			}
		}
		return allReady;
	}

	public void addConnection(Connection c) {
		if (c.isAsynch) {
			asynchconnections.add(c);
		} else {
			synchconnections.add(c);
		}
		connectionlookup.put(c.clientID,c);    
	}
}