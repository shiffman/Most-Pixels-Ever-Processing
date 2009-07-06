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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import mpe.config.FileParser;

public class UDPServer {

	public static int repeatTime = 250;
	public static int disconnectTime = repeatTime*1000;

	SendAllThread sat;
	CheckClientsThread cct;

	Connection[] connections;// = new ArrayList();
	private int port;
	private boolean running = false;
	//public boolean[] connected; // When the clients connect, they switch their slot to true
	//public boolean[] ready; // When the clients are ready for next frame, they switch their slot to true
	public boolean allConnected = false;  // When true, we are off and running
	int frameCount = 0;
	private long before;

	// Server will add a message to the frameEvent
	public boolean newMessage = false;
	public String message = "";

	// For UDP
	DatagramSocket frontDoor;

	public UDPServer(int port_, int listenPort_, int screens, int frameRate, boolean debug_) {
		port = port_;
		//listenPort =listenPort_;
		//parse ini file if it exists
		mpePrefs.setFramerate(frameRate);
		mpePrefs.setScreens(screens);
		port = port_;
		out("framerate = "+mpePrefs.FRAMERATE+",  screens = "+mpePrefs.SCREENS);//, master dimensions = "+ mpePrefs.M_WIDTH+", "+mpePrefs.M_HEIGHT);
		mpePrefs.DEBUG = debug_;

		//connected = new boolean[mpePrefs.SCREENS]; // default to all false
		//ready = new boolean[mpePrefs.SCREENS]; // default to all false
		connections = new Connection[mpePrefs.SCREENS];

		sat = new SendAllThread(this);
		sat.start();

		// start checking
		cct = new CheckClientsThread(this);
		cct.start();
	}

	public void run() {
		running = true;
		//if (listener) startListener();
		before = System.currentTimeMillis(); // Getting the current time
		byte[] data = new byte[65535];

		try {
			frontDoor = new DatagramSocket(port);
			System.out.println("Starting server: " + InetAddress.getLocalHost() + "  " + frontDoor.getLocalPort());

			// Wait for connections (could thread this)
			while (running) {
				DatagramPacket packet = new DatagramPacket(data,data.length);
				frontDoor.receive(packet);
				String msg = new String(data,0,packet.getLength());
				read(packet,msg);
			}
		} catch (IOException e) {
			System.out.println("Zoinks!" + e);
		}

	}

	// Synchronize?!!!
	public synchronized void triggerFrame() {
		if (frameCount % 10 == 0) {
			//System.out.println("Framecount: " + frameCount);
		}

		int desired = (int) ((1.0f / (float) mpePrefs.FRAMERATE) * 1000.0f);
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
		} else {
			try {
				long sleepTime = 2;
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Reset everything to false
		for (int i = 0; i < connections.length; i++) {
			connections[i].setReady(false);
		}        

		frameCount++;

		String send = "G,"+(frameCount-1);

		// Adding a data message to the frameEvent
		//substring removes the ":" at the end.
		if (newMessage) send += ":" + message.substring(0, message.length()-1);
		newMessage = false;
		message="";

		sat.setMessage(send);

		// We're calling this extra times, but that's ok, just sets boolean to true
		// Fix this at some point
		sat.startSending();

		sat.interrupt();





		before = System.currentTimeMillis();

	}

	private void print(String string) {
		System.out.println("MPEServer: "+string);
	}

	public synchronized void sendAll(String msg){
		//System.out.println("Sending " + msg + " to clients: " + connections.size());
		for (int i = 0; i < connections.length; i++){
			connections[i].send(msg);
		}
	}

	boolean allDisconnected(){
		int connected = 0;
		for (int i = 0; i < connections.length; i++) {
			if (connections[i] != null) connected++;
		}
		if (connected < 1){
			return true;
		} else return false;
	}

	void resetFrameCount(){
		frameCount = 0;
		newMessage = false;
		message = "";
		allConnected = false;
		print ("resetting frame count.");
		sat.stopSending();
		// We just always check anyway (they will be null and skipped)	
		// cct.quit();
	}

	public void killServer() {
		running = false;
	}

	public static void main(String[] args){
		int portInt = 9002;
		int listenPortInt = 9003;

		// DS: Revising to no longer take an INI file, just do everything via command line arguments
		boolean help = true;
		int screens = -1;
		int framerate =  -1;
		boolean debug = false;
		
		// how often is how often server repeats messages, little hack for now
		//int howoften = 1000;

		for (int i = 0; i < args.length; i++){
			if (args[i].contains("-screens")) {
				args[i] = args[i].substring(8);
				help = false;
				try{
					screens = Integer.parseInt(args[i]);
				} catch (Exception e) {
					out("I can't parse the total screens "+args[i]+"\n"+e);
					System.exit(1);
				}
			} else if (args[i].contains("-framerate")){
				help = false;
				args[i] = args[i].substring(10);
				try{
					framerate = Integer.parseInt(args[i]);
				} catch (Exception e) {
					out("I can't parse the frame rate "+args[i]+"\n"+e);
					System.exit(1);
				}//catch
			} else if (args[i].contains("-port")){
				help = false;
				args[i] = args[i].substring(5);
				try{
					portInt = Integer.parseInt(args[i]);
				} catch (Exception e) {
					out("I can't parse the port number "+args[i]+"\n"+e);
					System.exit(1);
				}//catch
			} else if (args[i].contains("-debug")){
				help = false;
				args[i] = args[i].substring(6);
				try{
					debug = Boolean.parseBoolean(args[i]);
				} catch (Exception e) {
					out("I can't parse the port number "+args[i]+"\n"+e);
					System.exit(1);
				}//catch
			} else if (args[i].contains("-n")){
				help = false;
				args[i] = args[i].substring(2);
				try{
					repeatTime = Integer.parseInt(args[i]);
					System.out.println("Repeat time: " + repeatTime + " milliseconds");
				} catch (Exception e) {
					//out("I can't parse the port number "+args[i]+"\n"+e);
					System.exit(1);
				}//catch
			}
		}

		if (help){
			System.out.println(" * The \"Most Pixels Ever\" server.\n"+
					" * This server can accept values from the command line:\n"+
					" * -screens<number of screens> Total # of expected clients.  Defaults to 2\n"+
					" * -framerate<framerate> Desired frame rate.  Defaults to 30\n"+
					" * -port<port number> Defines the port.  Defaults to 9002\n"+
					" * -debug<true or false> Turns on or off debug messages\n"+
			" * Please note the use of an INI file with the server is now deprecated");
			//" * -listen  Turns on an optional port listener so that other apps can send data to the screens.\n"+
			// "It listens to port 9003 by default.\n"+
			//" * -listenPort<port number>  Defines listening port.  Defaults to 9003.\n");
		}
		UDPServer ws = new UDPServer(portInt,listenPortInt, screens, framerate,debug);
		ws.run();
	}
	private static void out(String s){
		System.out.println("WallServer: "+ s);
	}

	public void drop(int i) {
		connections[i] = null;
	}

	// synchronize??
	public void setReady(int clientID) { 
		connections[clientID].setReady(true);
		if (isReady()) triggerFrame();
	}

	// synchronize?
	public boolean isReady() {
		boolean allReady = true;
		for (int i = 0; i < connections.length; i++){  //if any are false then wait
			Connection c = connections[i];
			if (c == null || !c.isReady()) allReady = false;
		}
		return allReady;
	}


	void read(DatagramPacket packet, String msg) {
		if (mpePrefs.DEBUG) System.out.println("Server receive: " + msg);

		// A little bit of a hack, it seems there are blank messages sometimes??
		char startsWith =  ' ';
		if (msg.length() > 0) startsWith = msg.charAt(0);

		switch(startsWith){
		// For Starting Up
		case 'S':

			String from = packet.getAddress().getHostAddress();
			int port = packet.getPort();

			// if (mpePrefs.DEBUG) System.out.println(msg);
			// do this with regex eventually, but for now..
			// (DS: also, probably just use delimiter and split?)
			// this is in serious need of error checking.
			int start = 1;
			int end = msg.indexOf("M");
			int clientID = Integer.parseInt(msg.substring(start));

			// Make  and start connection object, only if not before
			if (connections[clientID] == null) {
				System.out.println("Connecting Client " + clientID);
				Connection conn = new Connection(clientID,from,port,this);
				conn.setReady(true);
				// Add to list of connections
				connections[clientID] = conn; 
			} else {
				connections[clientID].active();
			}

			boolean all = true;
			for (int i = 0; i < connections.length; i++){  //if any are false then wait
				if (connections[i] == null) {
					all = false;
					break;
				}
			}
			allConnected = all;

			// we used to have the server control the dimensions, but no longer
			// send("M"+mpePrefs.M_WIDTH+","+mpePrefs.M_HEIGHT);

			if (allConnected) {
				triggerFrame();
			}
			break;
			//is it receiving a "done"?
		case 'D':   
			if (allConnected) {
				// Networking protocol could be optimized to deal with bytes instead of chars in a String?
				String[] info = msg.split(",");
				clientID = Integer.parseInt(info[1]);
				int fc = Integer.parseInt(info[2]);
				if (connections[clientID] != null) connections[clientID].active();
				if (fc == frameCount) {
					setReady(clientID);
					//if (parent.isReady()) parent.triggerFrame();
				} else {
					// Extra messages in case it didn't come through
					if (mpePrefs.DEBUG) {
						System.out.println("Extra message, already on to next frame: " + packet.getAddress() + ": " + packet.getPort() + " " + fc + "  server's count: " + frameCount);
					}
				}
			}
			break;
			//is it receiving a "daTa"?

		case 'H':
			// Just a heartbeat
			String[] info = msg.split(",");
			clientID = Integer.parseInt(info[1]);
			if (connections[clientID] != null) connections[clientID].active();
			break;
			//is it receiving a "daTa"?
		case 'T':   
			if (allConnected) {
				if (mpePrefs.DEBUG) print ("adding message to next frameEvent: " + msg);
				newMessage = true;
				message += msg.substring(1,msg.length())+":";
				//parent.sendAll(msg);
			}
			break;
		}

	}
}