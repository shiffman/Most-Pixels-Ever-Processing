/**
 * Class to display a String headline
 * @author Shiffman
 */

package feeds;

import mpe.client.TCPClient;
import processing.core.PApplet;
import processing.core.PFont;

public class Headline {
	
	// So we know about the PApplet and the Client
	PApplet parent;
	TCPClient client;
    
	// Location and speed
	float x = 0; 
    float y = 0; 
    float vy;
    
    // Font and String
    PFont f;
    String headline = "";
	
    // Initialize all variables in the constructor
    public Headline(PApplet _parent, TCPClient _c, String s, float _x, float _y){
		parent = _parent;
		client = _c;
		x = _x;
		y = _y;
		f = parent.createFont("Georgia",16,true);
		headline = s;
		vy = -2f;  // Headline starts moving up
	}
    
    public void move() {
    	// velocity increases exponentially
    	vy *= 1.02f;
    	y += vy;
    }
	
    // Display the headline
	public void draw(){
		parent.smooth();
		parent.fill(0);
		parent.textFont(f);
		parent.text(headline,x,y);
	}

	// If the headline is way off screen, we don't need it
	public boolean finished() {
		if (y < -100) {
			return true;
		}
		return false;
	}
}
