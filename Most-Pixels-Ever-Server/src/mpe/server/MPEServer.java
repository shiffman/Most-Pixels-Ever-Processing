/**
 * The "Most Pixels Ever" Wallserver.
 * This server can accept two values from the command line:
 * -port<port number> Defines the port.  Defaults to 9002
 * -ini<Init file path>  File path to mpeServer.ini.  Defaults to directory of server.
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

public class MPEServer {

	private HashMap<Integer,Connection> connectionlookup = new HashMap<Integer,Connection>();
	private ArrayList<Connection> synchconnections = new ArrayList<Connection>();
	private ArrayList<Connection> asynchconnections = new ArrayList<Connection>();

	private int port;
	int frameCount = 0;
	private long before;

	// Server will add a message to the frameEvent
	public boolean newMessage = false;
	public String message = "";

	public boolean dataload = false;

	public int numRequiredClients = 1;
	public int frameRate = 30;

	public boolean verbose = false;

	public boolean waitForAll = false;
	private boolean running = false;
	public boolean allConnected = false;  // When true, we are off and running
	
	public boolean paused;


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
		out("framerate = " + frameRate + ",  screens = " + numRequiredClients + ",waitForAll = " + waitForAll + ", verbose = " + verbose);
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
			if (newMessage) send += "|" + message.substring(0, message.length()-1);
			newMessage = false;
			message = "";
			sendAll(send);
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

		for (Connection conn : synchconnections) {
			conn.send(msg);
		}

		for (Connection conn : asynchconnections) {
			if (conn.asynchReceive) {
				conn.send(msg);
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
		newMessage = false;
		message = "";
		print ("resetting frame count.");
	}
	public void killServer() {
		running = false;
	}

	public static void main(String[] args) {
		// set default values
		int screens = -1;
		int framerate = 30;
		int port = 9002;
		
		boolean help = false;
		boolean verbose = false;

		// see if info is given on the command line
		for (int i = 0; i < args.length; i++) {
			if (args[i].contains("-screens")) {
				args[i] = args[i].substring(8);
				try{
					screens = Integer.parseInt(args[i]);
				} catch (Exception e) {
					out("ERROR: I can't parse the # of screens " + args[i] + "\n" + e);
					help = true;
				}
			} 
			else if (args[i].contains("-framerate")) {
				args[i] = args[i].substring(10);
				try{
					framerate = Integer.parseInt(args[i]);
				} catch (Exception e) {
					out("ERROR: I can't parse the frame rate " + args[i] + "\n" + e);
					help = true;
				}
			} 
			else if (args[i].contains("-port")) {
				args[i] = args[i].substring(5);
				try {
					port = Integer.parseInt(args[i]);
				} catch (Exception e) {
					out("ERROR: I can't parse the port number " + args[i] + "\n" + e);
					help = true;
				}
			}
			else if (args[i].contains("-verbose")) {
				verbose = true;
			}
			else {
				help = true;
			}
		}

		if (help) {
			// if anything unrecognized is sent to the command line, help is
			// displayed and the server quits
			System.out.println(" * The \"Most Pixels Ever\" server.\n" +
					" * This server can accept the following parameters from the command line:\n" +
					" * -screens<number of screens> Total # of expected clients.  Defaults to 2\n" +
					" * -framerate<framerate> Desired frame rate.  Defaults to 30\n" +
					" * -port<port number> Defines the port.  Defaults to 9002\n" +
					" * -verbose Turns debugging messages on.\n" +
					" * -xml<path to file with XML settings>  Path to initialization file.  Defaults to \"settings.xml\".\n");
			System.exit(1);
		}
		else {
			MPEServer ws = new MPEServer(screens, framerate, port, verbose);
			ws.run();
		}
	}
	private static void out(String s){
		System.out.println("MPEServer: "+ s);
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
		// TODO Account for asynch connections
		if (c.isAsynch) {
			asynchconnections.add(c);
			connectionlookup.put(c.clientID,c);
		} else {
			synchconnections.add(c);
			connectionlookup.put(c.clientID,c);
		}
	}
}