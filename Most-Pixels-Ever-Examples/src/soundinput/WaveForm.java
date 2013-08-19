/**
 * Big Screens Week 4 
 * Reads from microphone and broadcasts pitch as an array
 * @author Shiffman
 */

package soundinput;

import java.util.ArrayList;

import ddf.minim.AudioInput;
import ddf.minim.Minim;
import mpe.client.TCPClient;

import processing.core.PApplet;

public class WaveForm extends PApplet {

	final int ID = 0;

	// A client object
	TCPClient client;

	Minim minim;
	AudioInput in;

	// An array of pitch values
	float[] wave;

	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "soundinput.WaveForm"});
	}	

	//--------------------------------------
	public void setup() {
		// make a new Client using an XML file
		client = new TCPClient(this, "mpe" + ID + ".xml");

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		if (client.getID() == 0) {
			minim = new Minim(this);
			in = minim.getLineIn(Minim.MONO, 256);
		}

		smooth();
		resetEvent(client);

		// IMPORTANT, YOU MUST START THE CLIENT!
		client.start();
	}
	
	//--------------------------------------
		// Start over
		public void resetEvent(TCPClient c) {
			//Nothing to do here!

		}

	//--------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
		frame.setLocation(client.getID()*client.getLWidth(),0);
	}

	//--------------------------------------
	// Separate data event
	public void dataEvent(TCPClient c) {
		String[] msg = c.getDataMessage();
		String[] vals = msg[0].split(",");
		wave = new float[vals.length];
		for (int i = 0; i < wave.length; i++) {
			wave[i] = Float.parseFloat(vals[i]);
		}
	}
	
	//--------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {
		background(255);

		// You can get data here as well
		/*if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			String[] vals = msg[0].split(",");
			wave = new float[vals.length];
			for (int i = 0; i < wave.length; i++) {
				wave[i] = Float.parseFloat(vals[i]);
			}
		}*/
		
		rectMode(CENTER);
		stroke(0);

		// As long as the array exists, draw lines based on values
		if (wave != null) {
			float w = (float) client.getMWidth() / wave.length;
			beginShape();
			for (int i = 0; i < wave.length; i++) {
				float val = map(wave[i],-1,1,0,height);
				float x = i*w;
				vertex(x,height-val);
			}
			endShape();
		}

		// One client gets the pitch values and broadcasts them
		if (client.getID() == 0) {
			String message = "";
			for(int i = 0; i < in.bufferSize(); i++) {
				message += in.left.get(i);
				message += ",";
			}
			client.broadcast(message);
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


