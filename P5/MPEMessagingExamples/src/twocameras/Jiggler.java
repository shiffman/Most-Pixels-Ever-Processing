/**
 * Jiggler class for motion example
 * <http://mostpixelsever.com>
 * @author Shiffman
 */

package twocameras;

import mpe.client.TCPClient;
import processing.core.PApplet;
import processing.core.PConstants;

public class Jiggler {
	
	// Know about both the PApplet and the Client
	PApplet parent;
	TCPClient client;
    
	float x = 0; //ellipse x location
    float y = 0; //ellipse y location
    
    float vx = 0;
    float vy = 0;
    
    boolean flying;
    
    float w;
	
    // Initialize in the constructor
    public Jiggler(PApplet _parent, TCPClient _c, float _x, float _y){
		parent = _parent;
		client = _c;
		x = _x;
		y = _y;
		w = parent.random(4,16);
	}

    public void update() {
    	x += vx;
    	y += vy;
    	
    	vx *= 0.9f;
    	vy *= 0.9f;
    }
    
    public void shoot(float otherX, float otherY) {
    	// A little algorithm to send the jigglers towards the camera point
    	float dx = otherX - x;
    	float dy = otherY - y;
    	float d = parent.dist(otherX,otherY,x,y);
    	dx /= d;
    	dy /= d;
    	vx += dx;
    	vy += dy;
    }
	
	// Display
    public void draw(){
		parent.fill(0,150);
		parent.stroke(0);
		parent.rectMode(PConstants.CENTER);
		parent.rect(x,y,w,w);
	}
    
    
    public boolean flying() {
    	if (vx*vx + vy*vy > 1) {
    		return true;
    	} else {
    		return false;
    	}
    }
	
}
