package p3d;

import mpe.client.*;
import processing.core.*;

public class BoxDemo extends PApplet {
	//--------------------------------------
	final int ID = 1;

	TCPClient client;

	float angle = 0.0f;

	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "p3d.BoxDemo" });
	}

	//--------------------------------------
	public void setup() {
		// Make a new Client with an XML file.  
		client = new TCPClient(this, "mpe" + ID + ".xml");

		// The size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight(),P3D);
		client.enable3D(true);        

		resetEvent(client);

		// IMPORTANT, YOU MUST START THE CLIENT!
		client.start();
	}

	//--------------------------------------
	//Start over
	public void resetEvent(TCPClient c) {
		angle = 0.0f;
	}


	//--------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
	}

	
	//--------------------------------------
	public void frameEvent(TCPClient c) {
		// clear the screen
		background(255);
		for (int x = 0; x < client.getMWidth(); x += 100) {
			pushMatrix();
			translate(x,client.getMHeight()/2);
			stroke(0);
			noFill();
			rotate(angle);
			box(75);
			popMatrix();
		}
		angle += 0.02f;
	}

}
