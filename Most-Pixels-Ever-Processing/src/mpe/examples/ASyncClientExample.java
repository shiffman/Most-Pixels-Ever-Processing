package mpe.examples;

import mpe.client.*;
import processing.core.*;

public class ASyncClientExample extends PApplet {
    //--------------------------------------

    TCPClient client;

    //--------------------------------------
    static public void main(String args[]) {
        PApplet.main(new String[] { "mpe.examples.ASyncClientExample" });
    }

    //--------------------------------------
    public void setup() {
        size(320,240);
        // make a new Client using an INI file
        client = new TCPClient("asynch.xml", this);
        
        // IMPORTANT, YOU MUST START THE CLIENT!
        client.start();
    }
    
    public void draw() {
    	
    }



    //--------------------------------------
    // Adds a Ball to the stage at the position of the mouse click.
    public void mousePressed() {
        // never include a ":" when broadcasting your message
        int x = mouseX*2;
        int y = mouseY*2;
        client.broadcast(x + "," + y);
    }
}
