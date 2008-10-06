package pong;

import mpe.client.UDPClient;
import processing.core.PApplet;

/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

// A Ball class
public class Ball {
	
	float x,y;	 // location
	float vx,vy; // velocity
	float size;  // size
	
	PApplet parent;
	UDPClient client;
	
	public Ball(PApplet p, UDPClient c) {
		parent = p;
		client = c;
		// The ball starts on the right hand side
		// with negative velocity
		x = client.getMWidth();
		y = client.getMHeight()/2;
		vx = -2f;
		vy = -0.8f;
		size = 200;
	}
	
	// update location
	public void update() {
		x += vx;
		y += vy;
		
		// Bounce ball of edges
		if (x > client.getMWidth()) {
 			vx *= -1;
 		} else if (y < 0 || y > client.getMHeight()) {
 			vy *= -1;
 		// If it gets past the left hand side, you've lost!
 		} else if (x < 0) {
 			x = client.getMWidth();
 			vx = -2f;
 		}
	}
	
	// Check ball intersection with a Paddle
	public void hit(Paddle p) {
		if (x < p.width && y > p.y && y < p.y + p.height) {
			vx *= -1;
			x = p.width;
 		} 
	}
	
	// Display ball
	public void display() {
		parent.fill(255);
		parent.stroke(255);
		parent.rect(x,y,size,size);
	}

}
