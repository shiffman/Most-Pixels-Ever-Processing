/**
 * Simple Bouncing Ball Demo.
 * <http://code.google.com/p/mostpixelsever/>
 */

package mpe.examples;

import java.util.ArrayList;
import mpe.client.*;
import processing.core.*;

public class BouncingBalls extends PApplet {
    //--------------------------------------
	final int ID = 0;

	ArrayList balls;
	UDPClient client;

	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "mpe.examples.BouncingBalls" });
	}
	
	//--------------------------------------
    public void setup() {
        // make a new Client using an INI file
        // sketchPath() is used so that the INI file is local to the sketch
        client = new UDPClient(sketchPath("ini_balls/mpeSc"+ID+".ini"), this, false);
        
        // the size is determined by the client's local width and height
        size(client.getLWidth(), client.getLHeight());
        
        // the random seed must be identical for all clients
        randomSeed(1);
        
        smooth();
        background(255);
        
        // add 10 "randomly" placed balls
        balls = new  ArrayList();
        for (int i = 0; i < 10; i++) {
            Ball ball= new Ball(random(client.getMWidth()), random(client.getMHeight()));
            balls.add(ball);
        }
        
        // IMPORTANT, YOU MUST START THE CLIENT!
        client.start();
    }
    
    //--------------------------------------
    public void draw() {
        frame.setLocation(100 + ID*client.getLWidth(), 300);
        
        if (client.isRendering()) {
            // before we do anything, the client must place itself within the 
            //  larger display (this is done with translate, so use push/pop if 
            //  you want to overlay any info on all screens)
            client.placeScreen();
            
            // clear the screen
            background(255);
            
            // move and draw all the balls
            for (int i = 0; i < balls.size(); i++) {
                Ball ball = (Ball)balls.get(i);
                ball.calc();
                ball.draw();
            }
            
            // alert the server that you've finished drawing a frame
            client.done();
        } 
    }
    
    //--------------------------------------
    // Triggered by the client whenever a new frame should be rendered.
    public void frameEvent(UDPClient c) {
        // read any incoming messages
        if (c.messageAvailable()) {
            String[] msg = c.getDataMessage();
            String[] xy = msg[0].split(",");
            float x = Integer.parseInt(xy[0]);
            float y = Integer.parseInt(xy[1]);
            balls.add(new Ball(x, y));
        }
    }
    
    //--------------------------------------
    // Adds a Ball to the stage at the position of the mouse click.
    public void mousePressed() {
        // never include a ":" when broadcasting your message
        int x = mouseX + client.getXoffset();
        int y = mouseY + client.getYoffset();
        client.broadcast(x + "," + y);
    }

    //--------------------------------------
    // A Ball moves and bounces off walls.
    class Ball {
        //--------------------------------------
        float x = 0;     // center x position
        float y = 0;     // center y position
        float xDir = 1;  // x velocity
        float yDir = 1;  // y velocity
        float d = 24;    // diameter
        
        //--------------------------------------
        public Ball(float _x, float _y) {
            x = _x;
            y = _y;
            xDir = random(-5,5);
            yDir = random(-5,5);
        }

        //--------------------------------------
        // Moves and changes direction if it hits a wall.
        public void calc() {
            if (x < 0 || x > client.getMWidth())  xDir *= -1;
            if (y < 0 || y > client.getMHeight()) yDir *= -1;
            x += xDir;
            y += yDir;
        }
        
        //--------------------------------------
        public void draw() {
            stroke(0);
            fill(0, 100);
            ellipse(x, y, d, d);
        }
    }
}
