/**
 * Ball class for Sound example
 * <http://mostpixelsever.com>
 * @author Shiffman
 */

package fourthclient;

import mpe.client.TCPClient;
import processing.core.PApplet;

public class Ball {
	
	PApplet parent;
	TCPClient client;
    
	float x = 0; //ellipse x location
    float y = 0; //ellipse y location
    float xvel = 1; //x velocity
    float yvel = 1; //y velocity
    
    float gravity = 0.5f;
	
    public Ball(PApplet _parent, TCPClient _c, float _x, float _y){
		parent = _parent;
		client = _c;
        xvel = parent.random(10,20);
        yvel = parent.random(-2,2);
		x = _x;
		y = _y;
	}

    //a simple bounce across the screen
    public void calc(){
    	if (y > client.getMHeight()) {
    		yvel *= -1;
    		y = client.getMHeight();
    	}
    	x += xvel;
    	y += yvel;
    	
    	yvel += gravity;
    }
	
	public void draw(){
		parent.smooth();
		parent.fill(0);
		parent.ellipse(x,y,16,16);
	}
	
}
