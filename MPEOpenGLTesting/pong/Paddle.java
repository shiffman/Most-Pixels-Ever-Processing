/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

package pong;

import processing.core.PApplet;

public class Paddle {
	
	// Paddle has y location, width and height
	int y;
	int width;
	int height;
	
	PApplet parent;
	
	public Paddle(PApplet p) {
		parent = p;
		y = 0;
		width = 10;
		height = 60;
	}
	
	// This function will be called to
	// set the paddle according to mouse
	public void setLocation(int mouseY) {
		y = mouseY;
	}
	
	// Display the Paddle
	public void display() {
		parent.fill(255);
		parent.stroke(255);
		parent.rect(0,y,width,height);
	}

}
