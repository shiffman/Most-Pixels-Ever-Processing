/**
 * Big Screens Week 4 
 * Analyzes motion in video and broadcasts
 * @author Shiffman
 */

package video;

import mpe.client.TCPClient;
import processing.core.PApplet;
import processing.core.PImage;
import processing.video.*; 

public class OverallMotion extends PApplet {

	final int ID = 0;

	//	Variable for capture device 
	Capture video; 
	//	Previous Frame 
	PImage prevFrame; 

	//	How different must a pixel be to be a "motion" pixel 
	float threshold = 50; 

	// An array of objects
	Jiggler[] jigglers = new Jiggler[100];

	//	 A client object
	TCPClient client;

	// Global variables for motion
	float motion = 0;


	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "video.OverallMotion"});
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

		// Only if I am client 0
		if (client.getID() == 0) {
			// Using the default capture device 
			video = new Capture(this, width, height, 15); 
			// Create an empty image the same size as the video 
			prevFrame = createImage(video.width,video.height,RGB); 
		}

		for (int i = 0; i < jigglers.length; i++) {
			jigglers[i] = new Jiggler(this,client,random(client.getMWidth()),random(client.getMHeight()));
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
		
		// Get the message and convert it to a float for motion variables
		if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			motion = Float.parseFloat(msg[0]);
		}

		// Call video analysis function if I am client 0
		if (client.getID() == 0) {
			analyzeVideo();
		}

		for (int i = 0; i < jigglers.length; i++) {
			// Objects move around based on overall motion
			jigglers[i].jiggle(motion / 32f);
			jigglers[i].draw();
		}	
	}


	void analyzeVideo() {
		if (video.available()) { 
			// Save previous frame for motion detection!! 
			prevFrame.copy(video,0,0,video.width,video.height,0,0,video.width,video.height); 
			prevFrame.updatePixels(); 
			video.read(); 
		} 

		video.loadPixels(); 
		prevFrame.loadPixels(); 

		// Begin loop to walk through every pixel 
		// Start with a total of 0 
		float totalMotion = 0; 

		// Sum the brightness of each pixel 
		for (int i = 0; i < video.pixels.length; i++) { 
			int current = video.pixels[i]; // Step 2, what is the current color 
			int previous = prevFrame.pixels[i]; // Step 3, what is the previous color 
			// Step 4, compare colors (previous vs. current) 
			float r1 = red(current); float g1 = green(current); float b1 = blue(current); 
			float r2 = red(previous); float g2 = green(previous); float b2 = blue(previous); 
			float diff = dist(r1,g1,b1,r2,g2,b2); 
			totalMotion += diff; 
		} 

		float avgMotion = totalMotion / video.pixels.length; 

		// We aren't broadcasting the video!
		// We are just analyzing it and broadcasting a single value!
		client.broadcast("" + (int) avgMotion);
	}
}






