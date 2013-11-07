package p3d;

import mpe.client.*;
import processing.core.*;

public class ManualBox extends PApplet {
    //--------------------------------------
    final int ID = 0;

    TCPClient client;
    
    float angle = 0.0f;
    
    //--------------------------------------
    static public void main(String args[]) {
        PApplet.main(new String[] { "p3d.ManualBox" });
    }
    
    //--------------------------------------
    public void setup() {
        // Make a new Client with an XML file.  
        client = new TCPClient(this, "mpe" + ID + ".xml", false);
        
        // The size is determined by the client's local width and height
        size(client.getLWidth(), client.getLHeight(),P3D);
        client.enable3D(true);        
        
        resetEvent(client);
        
        // IMPORTANT, YOU MUST START THE CLIENT!
        client.start();
    }

    //--------------------------------------
    //Start over
    public void resetEvent(TCPClient c) {
    	angle = 0.0f;
    }

    //--------------------------------------
    public void draw() {
        if (client.isRendering()) {
            // before we do anything, the client must place itself within the 
            //  larger display (this is done with translate, so use push/pop if 
            //  you want to overlay any info on all screens)
            client.placeScreen3D();
            
            // clear the screen
            background(255);

            for (int x = 0; x < client.getMWidth(); x += 100) {
            	pushMatrix();
            	translate(x,client.getMHeight()/2);
            	stroke(0);
            	noFill();
            	rotate(angle);
            	box(75);
            	popMatrix();
            }
            
            angle += 0.02f;
            
            // alert the server that you've finished drawing a frame
            client.done();
        }
    }
   
}
