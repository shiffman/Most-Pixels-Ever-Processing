/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

package bubbles;

import mpe.client.UDPClient;
import processing.core.PApplet;

// A Bubble object
public class Bubble {

	PApplet parent;
	
	float r,g,b;  // Color
	float x,y;    // Location
	float vx,vy;  // Velocity
	float size;   // Size
	
	float buffer = 20; // how far off the screen (horizontally) do they go?
	UDPClient client;

	public Bubble(PApplet parent_, UDPClient client_) {
		parent = parent_;
		client = client_;
		// Initialize variables (we could do this via the constructor, which might be better)
		r = 255; g = 255; b = 255;
		x = parent.random(client.getMWidth());
		y = parent.random(client.getMHeight());
		vx = 0;
		vy = 0;
		size = 16;
	}

	// Update bubble
	public void update() {
		// Update location
		x += vx;
		y += vy;
		
		// Alter velocity randomly, simulate "wind"
		vx += parent.random(-0.05f,0.1f);
		vy += parent.random(-0.05f,0.05f);
		
		// If bubble leaves the window
		if (x > client.getMWidth() + buffer)  {
			x = -buffer;
			y = parent.random(client.getMHeight());
			vx = 0;
			vy = 0;
		}
	}
	
	// Function to display
	public void render() {
		parent.smooth();
		parent.stroke(r,g,b,150);
		parent.fill(r,g,b,150);
		parent.ellipse(x,y,size,size);
	}
}

