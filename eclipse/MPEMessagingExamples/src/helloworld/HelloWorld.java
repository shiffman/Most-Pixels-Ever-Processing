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

	final int ID = 1;
	float x;
	TCPClient client;

	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "helloworld.HelloWorld" });
	}

	//--------------------------------------
	public void setup() {
		// make a new Client using an INI file
		// sketchPath() is used so that the INI file is local to the sketch
		client = new TCPClient(sketchPath("mpefiles/mpe"+ID+".ini"), this);

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		// the random seed must be identical for all clients
		randomSeed(1);

		smooth();
		background(255);

		// IMPORTANT, YOU MUST START THE CLIENT!
		client.start();
	}

	//--------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
		frame.setLocation(client.getID()*client.getLWidth(),0);
	}

	//--------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {
		// clear the screen     
		background(255);
		fill(0);
		rect(x,0,40,height);
		x = (x + 5) % client.getMWidth();
	}

}


