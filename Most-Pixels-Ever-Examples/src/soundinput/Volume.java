/**
 * Big Screens Week 4 
 * Reads sound level from microphone and broadcasts volume to all clients
 * @author Shiffman
 */

package soundinput;

import ddf.minim.AudioInput;
import ddf.minim.Minim;
import mpe.client.TCPClient;

import processing.core.PApplet;

public class Volume extends PApplet {

	final int ID = 0;
	TCPClient client;
	float volume;

	Minim minim;
	AudioInput in;

	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "soundinput.Volume"});
	}	


	//--------------------------------------
	public void setup() {
		// make a new Client using an XML file
		client = new TCPClient(this, "mpe" + ID + ".xml");

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		if (client.getID() == 0) {
			minim = new Minim(this);
			in = minim.getLineIn(Minim.STEREO, 512);
		}

		smooth();
		background(255);

		resetEvent(client);
		
		// IMPORTANT, YOU MUST START THE CLIENT!
		client.start();
	}
	
	// --------------------------------------
		// Start over
		public void resetEvent(TCPClient c) {
			volume = 0;			
		}

	//--------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
	}

	//--------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {
		if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			// Convert the first element in the array to a float
			volume = Float.parseFloat(msg[0]);
		}
		background(255);

		rectMode(CENTER);
		fill(0,150);
		noStroke();

		// Rectangles are drawn based on global variable "volume"
		for (int x = 0; x < client.getMWidth(); x += 25) {
			rect(x,client.getMHeight()/2,volume*client.getMHeight(),volume*client.getMHeight());
		}

		// If we are client 0, then broadcast the volume level
		if (client.getID() == 0) {
			float level = (in.left.level() + in.right.level())/2;
			client.broadcast("" + level);
		}
	}

	//--------------------------------------
	public void stop() {
		if (client.getID() == 0) {
			in.close();
			minim.stop();
		}
		super.stop();
	}

}






