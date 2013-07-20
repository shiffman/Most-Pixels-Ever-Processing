/**
 * A regular old Connection to the server (from an MPE client)
 * @author Shiffman and Kairalla
 */

package mpe.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Connection extends Thread {
	Socket socket;

	InputStream in;
	OutputStream os;
	//DataInputStream dis;
	BufferedReader brin;
	DataOutputStream dos;

	//CyclicBarrier barrier;

	int clientID = -1;
	String uniqueName;

	String msg = "";

	boolean running = true;
	MPEServer parent;

	Connection(Socket socket_, MPEServer p) {
		//barrier = new CyclicBarrier(2);
		socket = socket_;
		parent = p;
		uniqueName = "Conn" + socket.getRemoteSocketAddress();
		// Trade the standard byte input stream for a fancier one that allows for more than just bytes (dano)
		try {
			in = socket.getInputStream();
			//dis = new DataInputStream(in);
			brin = new BufferedReader(new InputStreamReader(in));
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
		} catch (IOException e) {
			System.out.println("couldn't get streams" + e);
		}
	}

	void read(String msg) {
		// A little bit of a hack, it seems there are blank messages sometimes??
		char startsWith =  ' ';
		if (msg.length() > 0) startsWith = msg.charAt(0);

		String[] tokens = msg.split("\\|");
		
		switch(startsWith){
		// For Starting Up
		case 'S':
			if (MPEPrefs.VERBOSE) System.out.println(msg);

			int start = 1;
			clientID = Integer.parseInt(tokens[1]);
			System.out.println("Connecting Client " + clientID);
			parent.connected[clientID] = true;
			boolean all = true;
			for (int i = 0; i < parent.connected.length; i++){  //if any are false then wait
				if (parent.connected[i] == false) {
					all = false;
					break;
				}
			}
			parent.allConnected = all;
			if (parent.allConnected) parent.triggerFrame();
			break;
			//is it receiving a "done"?
		case 'D':   
			if (parent.allConnected) {
				// Networking protocol could be optimized to deal with bytes instead of chars in a String?
				clientID = Integer.parseInt(tokens[1]);
				int fc = Integer.parseInt(tokens[2]);
				if (MPEPrefs.VERBOSE) System.out.println("Receive: " + clientID + ": " + fc + "  match: " + parent.frameCount);
				if (fc == parent.frameCount) {
					parent.setReady(clientID);
				}
			}
			break;
		case 'T':   
			if (MPEPrefs.VERBOSE) print ("adding message to next frameEvent: " + msg);
			parent.newMessage = true;
			parent.message += clientID + "," + tokens[1] + "|";
			break;

		}
	}

	public void run() {
		while (running) {
			//String msg = null;
			try {
				String input = brin.readLine();
				if (input != null) {
					read(input);
				} else {
					killMe();
					break;
				}
			} catch (IOException e) {
				System.out.println("Someone left?  " + e);
				killMe();
				break;
			}            
		}

	}

	private void print(String string) {
		System.out.println("Connection: "+string);

	}

	public void killMe(){
		System.out.println("Removing Connection " + clientID);
		parent.killConnection(this);
		parent.drop(clientID);
		running = false;
		if (parent.allDisconected()) parent.resetFrameCount();
	}

	// Trying out no synchronize
	public void send(String msg) {
		if (MPEPrefs.VERBOSE) System.out.println("Sending: " + this + ": " + msg);
		try {
			msg+="\n";
			dos.write(msg.getBytes());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Trying out no synchronize
	public void sendBytes(byte[] b) {
		if (MPEPrefs.VERBOSE) System.out.println("Sending " + b.length + " bytes");
		try {
			dos.writeInt(b.length);
			dos.write(b,0,b.length);
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Trying out no synchronize
	public void sendInts(int[] ints) {
		if (MPEPrefs.VERBOSE) System.out.println("Sending " + ints.length + " ints");
		try {
			dos.writeInt(ints.length);
			for (int i = 0; i < ints.length; i++) {
				dos.writeInt(ints[i]);
			}
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}

