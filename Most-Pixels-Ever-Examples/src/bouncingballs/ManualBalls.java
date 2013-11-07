/**
 * Simple Bouncing Ball Demo
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 */

package bouncingballs;

import java.util.ArrayList;
import mpe.client.*;
import processing.core.*;

public class ManualBalls extends PApplet {
    //--------------------------------------
    final int ID = 0;

    ArrayList<Ball>balls;
    TCPClient client;
    
    //--------------------------------------
    static public void main(String args[]) {
        PApplet.main(new String[] { "bouncingballs.ManualBalls" });
    }
    
    //--------------------------------------
    public void setup() {
        // Make a new Client with an XML file.  
        client = new TCPClient(this, "mpe" + ID + ".xml", false);
        
        // The size is determined by the client's local width and height
        size(client.getLWidth(), client.getLHeight());
                
        smooth();
        noStroke();
        
        resetEvent(client);
        
        // IMPORTANT, YOU MUST START THE CLIENT!
        client.start();
    }

    //--------------------------------------
    //Start over
    public void resetEvent(TCPClient c) {
    	
        // the random seed must be identical for all clients
        randomSeed(1);
        background(100);

        // add a "randomly" placed ball
        balls = new ArrayList<Ball>();
        Ball ball = new Ball(random(client.getMWidth()), random(client.getMHeight()));
        balls.add(ball);
    }

    //--------------------------------------
    public void draw() {
        if (client.isRendering()) {
            // before we do anything, the client must place itself within the 
            //  larger display (this is done with translate, so use push/pop if 
            //  you want to overlay any info on all screens)
            client.placeScreen();
            // clear the screen
            background(100);

            // move and draw all the balls
            for (int i = 0; i < balls.size(); i++) {
                Ball ball = (Ball) balls.get(i);
                ball.calc();
                ball.draw();
            }

            // alert the server that you've finished drawing a frame
            client.done();
        }
    }
    
    //--------------------------------------
    // Separate data event
    public void dataEvent(TCPClient c) {
		String[] msg = c.getDataMessage();
		String[] xy = msg[0].split(",");
		float x = Integer.parseInt(xy[0]);
		float y = Integer.parseInt(xy[1]);
		balls.add(new Ball(x, y));
    }
    
    //--------------------------------------
    // Triggered by the client whenever a new frame should be rendered.
    public void frameEvent(TCPClient c) {
        // You can read incoming messages here as well
        /*if (c.messageAvailable()) {
            String[] msg = c.getDataMessage();
            String[] xy = msg[0].split(",");
            float x = Integer.parseInt(xy[0]);
            float y = Integer.parseInt(xy[1]);
            balls.add(new Ball(x, y));
        }*/
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
        float d = 10;    // diameter
        
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
