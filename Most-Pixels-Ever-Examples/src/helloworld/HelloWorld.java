/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

//This one should be really easy!
//Just moves a rectangle across the screen. . .

package helloworld;

import mpe.client.TCPClient;
import processing.core.PApplet;

public class HelloWorld extends PApplet {

	final int ID = 0;
	float x;
	TCPClient client;

	// --------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "helloworld.HelloWorld" });
	}

	// --------------------------------------
	public void setup() {
		// make a new Client using an XML file
		client = new TCPClient(this, "mpe" + ID + ".xml");

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		smooth();
		background(255);
		
		resetEvent(client);

		// IMPORTANT, YOU MUST START THE CLIENT!
		client.start();
	}

	// --------------------------------------
	// Start over
	public void resetEvent(TCPClient c) {
		// the random seed must be identical for all clients
		randomSeed(1);
		x = 0;
	}

	// --------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
	}

	// --------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {
		// clear the screen
		background(255);
		fill(0);
		rect(x, 0, 40, height);
		x = (x + 5) % client.getMWidth();
	}

}
