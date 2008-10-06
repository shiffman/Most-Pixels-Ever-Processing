package bubbles;

import mpe.client.UDPClient;
import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.*;

/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

public class BubblesMain extends PApplet { 

	// The Main function
	static public void main(String args[]) {
		PApplet.main(new String[] {"bubbles.BubblesMain"});
	}

	final int ID = 0;
	// A client object
	UDPClient client;
	boolean started = false;

	// An array of Bubble objects
	Bubble[] bubbles;
	
	PFont f;

	// This is triggered by the client whenever a new frame should be rendered
	public void frameEvent(UDPClient c){
		started = true;
		//System.out.println(System.currentTimeMillis() + " requested a redraw!!! " + client.getFrameCount());
		
		loop();
	}

	public void setup() {

		// Make a new Client with an INI file.  
		// sketchPath() is used so that the INI file is local to the sketch
		client = new UDPClient(sketchPath("local/mpe"+ID+".ini"),this);
		// The size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight(),OPENGL);
		//frame.setUndecorated(true);
		client.start();
		randomSeed(99);

		noLoop();
		// An array of 500 bubbles!
		bubbles = new Bubble[250];

		for (int i = 0; i < bubbles.length; i++) {
			bubbles[i] = new Bubble(this, client);
		}
		smooth();
		
		f = createFont("Times",24,true);
	}

	int now = 0;
	float fr = 0;
	public void draw() {
		background(0);
		frame.setLocation(200+ID*width,200);

		pushMatrix();
		if (started) {
			client.placeScreen();
			// Run all the bubbles
			for (int i = 0; i < bubbles.length; i++) {
				bubbles[i].update();
				bubbles[i].render();
			}

			client.done();
		}
		popMatrix();
		textFont(f);
		fill(255);
		
		int passed = millis() - now;
		fr = (1000.0f / passed)*0.1f + fr*0.9f;
		text((int)fr,10,height/2);
		noLoop();
		now = millis();
	}

}
