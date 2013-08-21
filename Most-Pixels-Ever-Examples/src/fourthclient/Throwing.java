/**
 * Big Screens Week 4 
 * Reads sound level from microphone and broadcasts sound events to all clients
 * @author Shiffman
 */

package fourthclient;

import java.util.ArrayList;
import mpe.client.TCPClient;
import processing.core.PApplet;

public class Throwing extends PApplet {

	final int ID = 0;

	TCPClient client;

	ArrayList<Ball> balls;

	// --------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "fourthclient.Throwing" });
	}

	// --------------------------------------
	public void setup() {
		// make a new Client using an XML file
		client = new TCPClient(this, "mpe" + ID + ".xml");

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		smooth();
		background(255);

		// IMPORTANT, YOU MUST START THE CLIENT!
		client.start();
	}

	// --------------------------------------
	// Start over
	public void resetEvent(TCPClient c) {
		// the random seed must be identical for all clients
		randomSeed(1);
		balls = new ArrayList<Ball>();
	}

	// --------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
	}

	// --------------------------------------
	// Separate data event
	public void dataEvent(TCPClient c) {
		String[] msg = c.getDataMessage();
		// If the message C comes in, then make a new Ball object
		// and add to ArrayList
		if (msg[0].equals("C")) {
			balls.add(new Ball(this, client, 0, height / 2));
		}
	}

	// --------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {
		background(255);
		if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			// If the message C comes in, then make a new Ball object
			// and add to ArrayList
			if (msg[0].equals("C")) {
				balls.add(new Ball(this, client, 0, height / 2));
			}
		}

		// Deal with all balls
		for (int i = 0; i < balls.size(); i++) {
			Ball b = (Ball) balls.get(i);
			b.calc();
			b.draw();
		}

	}

}
