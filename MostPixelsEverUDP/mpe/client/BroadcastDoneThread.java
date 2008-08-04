/**
 * The MPE "Sending Done" Thread
 * <http://mostpixelsever.com>
 * @author Shiffman
 */

package mpe.client;

import java.io.IOException;
import java.net.DatagramPacket;

public class BroadcastDoneThread extends Thread {

	UDPClient parent;

	boolean running = false;
	boolean sendingDone = false;

	public BroadcastDoneThread(UDPClient p) {
		parent = p;
	}

	public void sendDone() {
		String msg = "D," + parent.id + "," + parent.frameCount;
		send(msg);
	}
	
	public void sendHeartBeat() {
		String msg = "H," + parent.id + "," + parent.frameCount;
		send(msg);
	}
	
	public void start() {
		running = true;
		super.start();
	}

	public void run() {

		while (running) {
			//System.out.println("Broadcast done thread running");
			
			if (sendingDone) {
				sendDone();
			} else {
				sendHeartBeat();
			}

			
			// Ok so that we are not overloading the system
			try {
				Thread.sleep(UDPClient.repeatTime);
			} catch (InterruptedException e) {
				//System.out.println("Interrupting!");
				//e.printStackTrace();
			}

		}
	}

	// synchronize question
	private void send(String msg) {
		if (parent.DEBUG) System.out.println("Sending: " + msg);
		try {
			byte[] data = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(data,data.length,parent.address,parent.serverPort);
			packet.setData(data);
			parent.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void quit() {
		System.out.println("Quitting.");
		running = false; // Setting running to false ends the loop in run()
		interrupt(); // In case the thread is waiting. . .
	}
}