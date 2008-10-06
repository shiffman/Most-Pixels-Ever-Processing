/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

//This one should be really easy!
//Just moves a rectangle across the screen. . .

package helloworld;

import mpe.client.UDPClient;
import processing.core.PApplet;
import processing.opengl.*;

public class HelloWorld extends PApplet {

	static public void main(String args[]) {
		PApplet.main(new String[] { "helloworld.HelloWorld"});
	}	

	int x;
	final int ID = 1;

	// A UDPClient object
	UDPClient client;
	boolean started = false;

	// This is triggered by the UDPClient whenever a new frame should be rendered
	public void frameEvent(UDPClient c){
		started = true;
		redraw();
	}

	public void setup() {
		// Make a new UDPClient with an INI file.  
		// sketchPath() is used so that the INI file is local to the sketch
		client = new UDPClient(sketchPath("mpe"+ID+".ini"),this);
		// The size is determined by the UDPClient's local width and height
		size(client.getLWidth(), client.getLHeight(),OPENGL);
		frame.dispose();
		frame.setUndecorated(true);
		frame.setVisible(true);
		client.start();
		noLoop();
	}

	public void draw() {
		frame.setLocation(0,0);
		background(0);
		if (started) {
			client.placeScreen();
			fill(255);
			rect(x,0,40,height);
			x = (x + 5) % client.getMWidth();
			started = false;
			client.done();
		}
	}
}
