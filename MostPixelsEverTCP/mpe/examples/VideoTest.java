/**
 * Simple Demonstration of Broadcasting an array
 * The array is an array of integers from a video image
 * Note there are severe limitations to how much information can be sent
 * A byte array is also supported
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 */

package mpe.examples;

import mpe.client.TCPClient;
import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public class VideoTest extends PApplet {

	// Not Working at the moment!
	/*final int ID = 0;

	//  All clients have a Capture object, but only one client will broadcast data
	Capture video;

	//  Very low res video!!
	int w = 32;
	int h = 24;

	PImage img;     // The image to be displayed
	TCPClient client;  // The client object
	int x;          // Location where we will see image
	int y;    

	static public void main(String args[]) {
		PApplet.main(new String[] {"mpe.examples.VideoTest"});
	}



	public void setup() {
		// Make a new Client with an INI file.  
		// sketchPath() is used so that the INI file is local to the sketch
		client = new TCPClient(sketchPath("mpeSc"+ID+".ini"),this);
		// The size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		x = 0;
		y = client.getMHeight()/2;

		// Start with an empty image
		img = createImage(w,h,RGB);

		// Only Client ID 0 will capture from the camera!
		if (client.getID() == 0) video = new Capture(this,w,h,15);

		// IMPORTANT, MUST START THE CLIENT!
		client.start();

	}

	//  Read from the camera
	public void captureEvent(Capture c) {
		c.read();
	}

	//--------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {}


	//  Called by library whenever a new frame should be rendered
	public void frameEvent(TCPClient c){
		// If there is an integer array go and get it!
		if (c.intsAvailable()) {
			int[] pix = c.getInts();
			img.pixels = pix;
			img.updatePixels();
		}


		smooth();
		background(255);

		// Display the video
		image(img,x,y,img.width*4,img.height*4);
		x = (x+4) % client.getMWidth();

		// Every 30 frames, let's send a new video image
		// We can't necessarily do this every frame, it's too much
		if (frameCount % 30 == 0 && client.getID() == 0) sendImage();

	}

	//  A function to broadcast the video's pixel array
	public void sendImage() {
		client.broadcastIntArray(video.pixels);
	}*/




}
