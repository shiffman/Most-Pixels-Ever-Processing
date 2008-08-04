
/**
 * The MPE "Send All" Thread
 * <http://mostpixelsever.com>
 * @author Shiffman
 */

package mpe.server;

import java.io.IOException;
import java.net.DatagramPacket;

public class SendAllThread extends Thread {

	UDPServer parent;

	boolean running = false;
	boolean sending = false;
	
	String msg = "";

	public SendAllThread(UDPServer p) {
		parent = p;
	}

	public void setMessage(String m) {
		msg = m;
	}
	
    public synchronized void sendAll(){
        //System.out.println("Sending " + msg + " to clients: " + connections.size());
        for (int i = 0; i < parent.connections.length; i++){
        	Connection c = parent.connections[i];
        	if (c != null) c.send(msg);
        }
    }
	
	public void start() {
		running = true;
		super.start();
	}
	
	public void stopSending() {
		sending = false;
	}
	
	public void startSending() {
		sending = true;
	}

	public void run() {

		while (running) {
			//System.out.println("Broadcast done thread running");
			
			if (sending) {
				sendAll();
			}

			// Ok so that we are not overloading the system
			try {
				Thread.sleep(UDPServer.repeatTime);
			} catch (InterruptedException e) {
				//System.out.println("Interrupting!");
				//e.printStackTrace();
			}

		}
	}

	public void quit() {
		System.out.println("Quitting.");
		running = false; // Setting running to false ends the loop in run()
		interrupt(); // In case the thread is waiting. . .
	}
}