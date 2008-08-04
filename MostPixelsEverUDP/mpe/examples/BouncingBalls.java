/**
 * Simple Bouncing Ball Demo
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 */

package mpe.examples;

//Import necessary libraries
import java.util.ArrayList;

import mpe.client.*;
import processing.core.PApplet;

public class BouncingBalls extends PApplet {

	final int ID = 0;
	ArrayList balls;
	// A client object
	UDPClient client;

	static public void main(String args[]) {
		PApplet.main(new String[] {"mpe.examples.BouncingBalls" });
	}

	// This is triggered by the client whenever a new frame should be rendered
	public void frameEvent(UDPClient c){
		if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			String[] xy = msg[0].split(",");
			float x = Integer.parseInt(xy[0]);
			float y = Integer.parseInt(xy[1]);
			balls.add(new Ball(this,x,y));
		}
		started = true;
		
		long now = millis();
		redraw();
		System.out.println("Frame took: " + (millis() - now));
		// Alert the server that you've finished drawing a frame
		client.done();
	}

	boolean started = false;

	public void setup() {
		// Make a new Client with an INI file.  
		// sketchPath() is used so that the INI file is local to the sketch
		client = new UDPClient(sketchPath("local/mpeSc"+ID+".ini"),this);
		// The size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());
		randomSeed(1);
		balls = new  ArrayList();
		for (int i = 0; i < 10; i++) {
			Ball ball= new Ball(this,random(client.getMWidth()),random(client.getMHeight()));
			balls.add(ball);
		}
		background(0);
		noStroke();
		smooth();
		// IMPORTANT, MUST START THE CLIENT!
		client.start();
		// CRUCIAL, MUST STOP THE AUTOMATIC LOOPING OF PROCESSING!
		noLoop();
	}

	public void draw() {
		frame.setLocation(400+ID*client.getLWidth(),300);

		// Before we do anything, the client must place itself within the larger display
		// (This is done with translate, so use push/pop if you want to overlay any info on all screens)
		client.placeScreen();
		// Do whatever it is you would normally do
		background(100);
		for (int i = 0; i < balls.size(); i++) {
			Ball ball = (Ball) balls.get(i);
			ball.calc();
			ball.draw();
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println(frameRat	e);
	}

	public void mousePressed() {
		// How to broadcast a message
		// Do not include a ":" in your message
		int x = mouseX + client.getXoffset();
		int y = mouseY + client.getYoffset();
		client.broadcast(x + "," + y);
		// UDPClient.DEBUG = true;
	}


}
