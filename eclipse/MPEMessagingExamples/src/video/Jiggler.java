/**
 * Jiggler class for motion example
 * <http://mostpixelsever.com>
 * @author Shiffman
 */

package video;

import mpe.client.TCPClient;
import processing.core.PApplet;

public class Jiggler {
	
	// Know about both the PApplet and the Client
	PApplet parent;
	TCPClient client;
    
	float x = 0; //ellipse x location
    float y = 0; //ellipse y location
	
    // Initialize in the constructor
    public Jiggler(PApplet _parent, TCPClient _c, float _x, float _y){
		parent = _parent;
		client = _c;
		x = _x;
		y = _y;
	}

    // Move randomly
    public void jiggle(float amount) {
    	x += parent.random(0,amount/2);
    	y += parent.random(-amount,amount);
    	
    	if (x > client.getMWidth() + 100) {
    		x = -100;
    	}
    }
	
	// Display
    public void draw(){
		parent.smooth();
		parent.fill(0,150);
		parent.stroke(0);
		parent.ellipse(x,y,32,32);
	}
	
}
