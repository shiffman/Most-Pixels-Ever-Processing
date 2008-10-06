/**
 * Simple Demonstration of Broadcasting an array
 * The array is an array of integers from a video image
 * Note there are severe limitations to how much information can be sent
 * A byte array is also supported
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 */

package mpe.examples;

import mpe.client.UDPClient;
import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public class VideoTest extends PApplet {

	final int ID = 0;

	//  All clients have a Capture object, but only one client will broadcast data
	Capture video;

	//  Very low res video!!
	int w = 32;
	int h = 24;

	PImage img;     // The image to be displayed
	UDPClient client;  // The client object
	int x;          // Location where we will see image
	int y;    

	boolean started = false;

	static public void main(String args[]) {
		PApplet.main(new String[] {"mpe.examples.VideoTest"});
	}

	//  Called by library whenever a new frame should be rendered
	public void frameEvent(UDPClient c){
		// This is insane, but I am just testing by passing an array of ints as a comma separate String
		// The library will be updated / improved at some point
		if (c.messageAvailable()) {

		
			String[] ints = c.getDataMessage()[0].split(",");
			int[] pix = new int[ints.length];
			for (int i = 0; i < pix.length; i++) {
				pix[i] = Integer.parseInt(ints[i]);
			}
			img.pixels = pix;
			img.updatePixels();
			
		}
		started = true;
		redraw();
	}

	public void setup() {
		// Make a new Client with an INI file.  
		// sketchPath() is used so that the INI file is local to the sketch
		client = new UDPClient(sketchPath("ini_video/mpeSc"+ID+".ini"),this);
		// The size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		x = 0;
		y = client.getMHeight()/2;

		// Start with an empty image
		img = createImage(w,h,RGB);

		// Only Client ID 0 will capture from the camera!
		if (client.getClientID() == 0) video = new Capture(this,w,h,15);

		// IMPORTANT, MUST START THE CLIENT!
		client.start();
		// CRUCIAL, MUST STOP THE AUTOMATIC LOOPING OF PROCESSING!
		noLoop();

	}

	//  Read from the camera
	public void captureEvent(Capture c) {
		c.read();
	}

	public void draw() {
		smooth();
		background(255);

		if (started) {
			// Before we do anything, the client must place itself within the larger display
			// (This is done with translate, so use push/pop if you want to overlay any info on all screens)
			client.placeScreen();

			// Display the video
			image(img,x,y,img.width*4,img.height*4);
			x = (x+4) % client.getMWidth();

			// Every 30 frames, let's send a new video image
			// We can't necessarily do this every frame, it's too much
			// if (frameCount % 30 == 0 &&
			if (client.getClientID() == 0) {
				sendImage();
			}
			
			// Alert the server that you've finished drawing a frame
			client.done();
		}
		
	}

//	A function to broadcast the video's pixel array
	public void sendImage() {
		String pix = "";
		for (int i = 0; i < video.pixels.length; i++) {
			pix += video.pixels[i] + ",";
		}
		client.broadcast(pix);
	}




}
