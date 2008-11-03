/**
 * Ball class for simple bouncing ball demo
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 */

package mpe.examples;

public class Ball {
	
	BouncingBalls parent;
    
	float x = 0; //ellipse x location
    float y = 0; //ellipse y location
    float xdir = 1; //x velocity
    float ydir = 1; //y velocity
	
    Ball(BouncingBalls _parent, float _x, float _y){
		parent = _parent;
        xdir = parent.random(-5,5);
        ydir = parent.random(-5,5);
		x = _x;
		y = _y;
	}

    //a simple bounce across the screen
    public void calc(){
    	if (x < 0 || x > parent.client.getMWidth()) xdir *= -1;
    	if (y < 0 || y > parent.client.getMHeight()) ydir *= -1;
    	x += xdir;
    	y += ydir;
    }
	
	public void draw(){
		parent.ellipse(x,y,10,10);
	}
}
