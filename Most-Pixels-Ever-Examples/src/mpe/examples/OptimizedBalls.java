/**
 * Simple Bouncing Ball Demo using automatic mode.
 * <http://code.google.com/p/mostpixelsever/>
 */

package mpe.examples;

import java.util.ArrayList;
import mpe.client.*;
import mpe.examples.BouncingBalls.Ball;
import processing.core.*;

public class OptimizedBalls extends PApplet {
    //--------------------------------------
    final int ID = 0;

	ArrayList<Ball> balls;
	TCPClient client;

	// --------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "mpe.examples.OptimizedBalls" });
	}

	// --------------------------------------
	public void setup() {
		// make a new Client using an XML file
		client = new TCPClient(this, "mpe" + ID + ".xml");

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		smooth();
		background(255);

		// IMPORTANT, YOU MUST START THE CLIENT!
		client.start();
	}

	// --------------------------------------
	// Start over
	public void resetEvent(TCPClient c) {
		// the random seed must be identical for all clients
		randomSeed(1);

		// add a "randomly" placed ball
		balls = new ArrayList<Ball>();
		Ball ball = new Ball(random(client.getMWidth()),
				random(client.getMHeight()));
		balls.add(ball);

	}

	// --------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
	}

	// --------------------------------------
	// Separate data event
	public void dataEvent(TCPClient c) {
		String[] msg = c.getDataMessage();
		String[] xy = msg[0].split(",");
		float x = Integer.parseInt(xy[0]);
		float y = Integer.parseInt(xy[1]);
		balls.add(new Ball(x, y));
	}

	// --------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {
		// clear the screen
		background(255);

		// move and draw all the balls
		for (int i = 0; i < balls.size(); i++) {
			Ball ball = (Ball) balls.get(i);
			ball.calc();
			if (client.isOnScreen(ball.x - ball.d / 2, ball.y - ball.d / 2,
					ball.d, ball.d)) {
				ball.draw();
			}
		}

		// You can also read incoming messages in frameEvent
		/*if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			String[] xy = msg[0].split(",");
			float x = Integer.parseInt(xy[0]);
			float y = Integer.parseInt(xy[1]);
			balls.add(new Ball(x, y));
		}*/
	}

	// --------------------------------------
	// Adds a Ball to the stage at the position of the mouse click.
	public void mousePressed() {
		// never include a ":" when broadcasting your message
		int x = mouseX + client.getXoffset();
		int y = mouseY + client.getYoffset();
		client.broadcast(x + "," + y);
	}

	// --------------------------------------
	// A Ball moves and bounces off walls.
	class Ball {
		// --------------------------------------
		float x = 0; // center x position
		float y = 0; // center y position
		float xDir = 1; // x velocity
		float yDir = 1; // y velocity
		float d = 50; // diameter

		// --------------------------------------
		public Ball(float _x, float _y) {
			x = _x;
			y = _y;
			xDir = random(-5, 5);
			yDir = random(-5, 5);
		}

		// --------------------------------------
		// Moves and changes direction if it hits a wall.
		public void calc() {
			if (x < 0 || x > client.getMWidth())
				xDir *= -1;
			if (y < 0 || y > client.getMHeight())
				yDir *= -1;
			x += xDir;
			y += yDir;
		}

		// --------------------------------------
		public void draw() {
			stroke(0);
			fill(0, 100);
			ellipse(x, y, d, d);
		}
	}
}
