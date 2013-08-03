/**
 * Big Screens Week 4 
 * Track color XY and broadcast info
 * @author Shiffman
 */

package video;

import mpe.client.TCPClient;
import processing.core.PApplet;
import processing.core.PImage;
import processing.video.*; 

public class ColorTracking extends PApplet {

	final int ID = 1;

	//	Variable for capture device 
	Capture video; 
	//	Previous Frame 
	PImage prevFrame; 

	int trackColor; 

	int x;
	int y;

	//	 A client object
	TCPClient client;

	// Global variables for motion
	float motion = 0;


	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "video.ColorTracking"});
	}	

	//--------------------------------------
	public void setup() {
		// make a new Client using an INI file
		// sketchPath() is used so that the INI file is local to the sketch
		client = new TCPClient(this, sketchPath("mpefiles/mpe"+ID+".ini"));

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		// the random seed must be identical for all clients
		randomSeed(1);

		trackColor = color(255,0,0); // Start off tracking for red 

		// Only if I am client 0
		if (client.getID() == 0) {
			// Using the default capture device 
			video = new Capture(this, width, height, 15); 
		}

		smooth();

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

		// Get the message and convert it to xy coordinate
		if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			String[] xy = msg[0].split(",");
			x = Integer.parseInt(xy[0]);
			y = Integer.parseInt(xy[1]);
		}

		// Call video analysis function if I am client 0
		if (client.getID() == 0) {
			trackColors();
			image(video,0,0); 
		}

		fill(trackColor); 
		strokeWeight(4.0f); 
		stroke(0); 
		ellipse(x + client.getXoffset(), y + client.getYoffset(),16,16);

	}

	void trackColors() {
		// Capture and display the video 
		if (video.available()) { 
			video.read(); 
		} 
		video.loadPixels(); 

		// Closest record, we start with a high number 
		float closestRecord = 500.0f; 
		// XY coordinate of closest color 
		int closestX = 0; 
		int closestY = 0; 
		// Begin loop to walk through every pixel 
		for ( int x = 0; x < video.width; x++) { 
			for ( int y = 0; y < video.height; y++) { 
				int loc = x + y*video.width; 
				// What is current color 
				int currentColor = video.pixels[loc]; 
				float r1 = red(currentColor); 
				float g1 = green(currentColor); 
				float b1 = blue(currentColor); 
				float r2 = red(trackColor);   
				float g2 = green(trackColor);   
				float b2 = blue(trackColor); 
				// Using euclidean distance to compare colors 
				float d = dist(r1,g1,b1,r2,g2,b2); 
				// If current color is more similar to tracked color than 
				// closest color, save current location and current difference 
				if (d < closestRecord) { 
					closestRecord = d; 
					closestX = x; 
					closestY = y; 
				} 
			} 
		} 

		client.broadcast(closestX + "," + closestY);
	}

	public void mousePressed() { 
		// Save color where the mouse is clicked in trackColor variable 
		if (client.getID() == 0) {
			int loc = mouseX + mouseY*video.width; 
			trackColor = video.pixels[loc]; 
		}
	} 
}
