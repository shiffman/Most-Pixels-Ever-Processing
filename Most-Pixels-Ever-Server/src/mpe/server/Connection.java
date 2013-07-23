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
	boolean ready = false;

	InputStream in;
	OutputStream os;
	BufferedReader brin;
	DataOutputStream dos;

	int clientID = -1;
	String uniqueName;
	String msg = "";
	boolean running = true;
	MPEServer parent;


	Connection(Socket socket_, MPEServer p) {
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
			if (parent.verbose) {
				System.out.println("Raw message: " + msg);
			}
			clientID = Integer.parseInt(tokens[1]);

			parent.addConnection(this);

			System.out.println("Connecting Client " + clientID);
			int total = parent.totalConnections();

			if (parent.waitForAll) {
				parent.allConnected = (total == parent.numRequiredClients);
				if (parent.allConnected) parent.triggerFrame(false);
			} else {
				parent.triggerFrame(true);
			}

			break;
			//is it receiving a "done"?
		case 'D':   
			if (!parent.waitForAll || parent.allConnected) {
				clientID = Integer.parseInt(tokens[1]);
				int fc = Integer.parseInt(tokens[2]);
				if (parent.verbose) {
					System.out.println("Client: " + clientID + " says done with: " + fc + "  server count: " + parent.frameCount);
				}
				if (fc == parent.frameCount) {
					parent.setReady(clientID);
				}
			}
			break;
		case 'T':   
			if (parent.verbose) {
				System.out.println("Adding message to next frameEvent: " + msg);
			}
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

	public void killMe(){
		System.out.println("Removing Connection " + clientID);
		parent.killConnection(clientID);
		
		if (parent.allDisconected()) {
			parent.resetFrameCount();
		} else {
			// Need to check if everyone is all set now that someone has disconnected
			// TODO: encapsulate below into method, same as in setReady();
			if (parent.isReady()) {
				parent.frameCount++;
				parent.triggerFrame(false);
			}
		}
		running = false;
	}

	// Trying out no synchronize
	public void send(String msg) {
		try {
			msg+="\n";
			dos.write(msg.getBytes());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

