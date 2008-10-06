/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

package pong;

import mpe.client.UDPClient;
import processing.core.PApplet;

public class Pong extends PApplet {

	// Main function to run as application
	static public void main(String args[]) {
		PApplet.main(new String[] { "pong.Pong"});
	}	

	// Ball object and Paddle object
	Ball ball;
	Paddle paddle;

	final int ID = 0;
	// A client object
	UDPClient client;
	boolean started = false;

	// This is triggered by the client whenever a new frame should be rendered
	public void frameEvent(UDPClient c){
		if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			int paddleY = Integer.parseInt(msg[0]);
			paddle.setLocation(paddleY);
		}
		started = true;
		redraw();

	}

	public void setup() {

		// Make a new Client with an INI file.  
		// sketchPath() is used so that the INI file is local to the sketch
		client = new UDPClient(sketchPath("mpe"+ID+".ini"),this);
		// The size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());
		frame.setUndecorated(true);
		client.start();
		noLoop();

		// Make the two objects
		ball = new Ball(this,client);
		paddle = new Paddle(this);
	}

	public void draw() {
		background(0);
		frame.setLocation(0,0);
		
		if (started) {
			client.placeScreen();

			// Run the ball
			ball.update();
			ball.hit(paddle); // check if ball intersects with paddle 
			ball.display();

			paddle.display();
			client.done();

			if (ID == 0) {
				String msg = "" + mouseY;
				client.broadcast(msg);
			}
		}

	}
}
