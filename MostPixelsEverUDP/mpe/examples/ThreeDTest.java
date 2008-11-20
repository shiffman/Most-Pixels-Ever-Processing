/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

//This one should be really easy!
//Just moves a rectangle across the screen. . .

package mpe.examples;

import mpe.client.UDPClient;
import processing.core.*;
import processing.opengl.*;

public class ThreeDTest extends PApplet {

	static public void main(String args[]) {
		PApplet.main(new String[] { "mpe.examples.ThreeDTest"});
	}	

	float theta = 0;
	final int ID = 1;
	// A client object
	UDPClient client;

	// This is triggered by the client whenever a new frame should be rendered
	public void frameEvent(UDPClient c){
		started = true;
		redraw();
	}

	boolean started = false;

	public void setup() {
		// Make a new Client with an INI file.  
		// sketchPath() is used so that the INI file is local to the sketch
		client = new UDPClient(sketchPath("ini_threeD/mpe"+ID+".ini"), this);
		// The size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight(),OPENGL);
		client.start();
		noLoop();
	}


	public void draw() {
		background(0);


		if (started) {
		    client.placeScreen();

			// Draw a spinning cube
			translate(320,120);
			rectMode(CENTER);
			noFill();
			stroke(255);
			rotateY(theta);
			rotateX(theta/3.0f);
			rotateZ(theta/5.0f);
			box(100);
			theta += 0.05f;

			client.done();
		}
	}
}
