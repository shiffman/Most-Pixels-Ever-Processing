/**
 * 3D Demo using automatic mode.
 * <http://code.google.com/p/mostpixelsever/>
 * 
 * @author Shiffman
 */

package mpe.examples;

import mpe.client.*;
import processing.core.*;
//import processing.opengl.*;

public class ThreeDTest extends PApplet {
    //--------------------------------------
    final int ID = 0;
    
    float theta = 0;
    TCPClient client;
    
    //--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "mpe.examples.ThreeDTest" });
	}
	
	//--------------------------------------
    public void setup() {
        // make a new Client with an INI file
        // sketchPath() is used so that the INI file is local to the sketch
        client = new TCPClient(sketchPath("ini_threeD/mpe"+ID+".ini"), this);
        
        // the size is determined by the client's local width and height
        size(client.getLWidth(), client.getLHeight(), P3D);
        background(0);
        
        // IMPORTANT, YOU MUST START THE CLIENT!
        client.start();
    }

    //--------------------------------------
    // Keep the motor running... draw() needs to be added in auto mode, even if
    // it is empty to keep things rolling.
    public void draw() {}
    
    //--------------------------------------
    // Triggered by the client whenever a new frame should be rendered.
    // All synchronized drawing should be done here when in auto mode.
    public void frameEvent(TCPClient c) {
        // clear the screen     
        background(0);

	    // draw a spinning cube
        translate(client.getMWidth()/2, client.getMHeight()/2);
        rectMode(CENTER);
        noFill();
        stroke(255);
        rotateY(theta);
        rotateX(theta/3.0f);
        rotateZ(theta/5.0f);
        box(100);
        theta += 0.05f;
	}
}
