
/**
 * Big Screens Week 4 
 * Reads sound level from microphone and broadcasts sound events to all clients
 * @author Shiffman
 */

package soundinput;

import java.util.ArrayList;

import ddf.minim.AudioInput;
import ddf.minim.Minim;

import mpe.client.TCPClient;

import processing.core.PApplet;

public class Threshold extends PApplet {

	final int ID = 0;
	TCPClient client;

	float clapLevel = 0.05f;  // How loud is a clap 
	float threshold = 0.005f;  // How quiet is silence 
	boolean clapping = false; 

	ArrayList balls;

	Minim minim;
	AudioInput in;

	
	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "soundinput.Threshold"});
	}	


	//--------------------------------------
	public void setup() {
		// make a new Client using an INI file
		// sketchPath() is used so that the INI file is local to the sketch
		client = new TCPClient(sketchPath("mpefiles/mpe"+ID+".ini"), this);

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());
		
		balls = new ArrayList();
		randomSeed(1);

		if (client.getID() == 0) {
			minim = new Minim(this);
			in = minim.getLineIn(Minim.MONO, 256);
		}


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
		background(255);
		if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			// If the message C comes in, then make a new Ball object
			// and add to ArrayList
			if (msg[0].equals("C")) {
				balls.add(new Ball(this,client,0,height/2));
			}
		}

		// Deal with all balls
		for (int i = 0; i < balls.size(); i++) {
			Ball b = (Ball) balls.get(i);
			b.calc();
			b.draw();
		}

		if (client.getID() == 0) {
			// Get the overall volume (between 0 and ??) 
			float vol = in.left.level();
			noStroke(); 
			// If the volume is greater than one and 
			// we are not clapping 
			if (vol > clapLevel && !clapping) { 
				// When we are clapping broadcast a "C" (this is an arbitrary choice)
				client.broadcast("C");
				clapping = true;  // We are now clapping! 
				// If we are finished clapping 
			} else if (clapping && vol < 0.5) {  
				clapping = false; 
			} 
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








