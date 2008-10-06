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
	boolean sendingStart = true;
	
	// We have our own frameCount and we choose when to update it!
	int frameCount = 0;

	public BroadcastDoneThread(UDPClient p) {
		parent = p;
	}

	public void sendStart() {
		String msg = "S" + parent.id;
		send(msg);
	}
	
	public void sendDone() {
		String msg = "D," + parent.id + "," + parent.frameCount;
		send(msg);
	}
	
	// No need for a heartbeat, just frameCount updating
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
			
			// It's sometimes sending:
			// H,0,1284
			// when it never sent D,0,1284, so it went through frameCount++ but missed sending D,0,1284, not a packet lost, just a missed
			// step, we could account for that here and make sure it can't do that.
			// Is this the problem that happens with openGL??  Ok, i think this is fixable!!!
			
			// Also, sometimes it sends S0, but that packet is lost, has to repeat it over and over
			
			if (sendingStart) {
				sendStart();
			} else if (sendingDone) {
				sendDone();
			} else {
				//sendDone();
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

	public void setFrameCount(int fc) {
		frameCount = fc;
	}
}