/**
 * Quotes Demo
 * The QuoteFetcher is an asynchronous client which fetches the quotes from a 
 * server and broadcasts them for the QuoteDisplays to display.
 * <http://code.google.com/p/mostpixelsever/>
 * 
 * @author Elie Zananiri
 */

package fourthclient;

import mpe.client.*;
import processing.core.*;

public class Messenger extends PApplet {

	//--------------------------------------
	TCPClient client;
	PFont font;
	

	boolean message = false;

	//--------------------------------------
	public void setup() {
		// set up the client
		client = new TCPClient(this, "asynch.xml");
		size(350, 200);

		smooth();
		frameRate(30);
		font = createFont("Arial", 12);
		
		client.start();
	}

	//--------------------------------------
	public void draw() {
		if (message) {
			background(0);
			message = false;
		} else {
			background(255);
		}
		fill(0);
		textFont(font, 18);
		textAlign(CENTER, CENTER);
		text("Click the mouse to launch a ball",width/2,height/2);
	}
	
	// If you turn on receiving messages you can get them this way
    public void dataEvent(TCPClient c) {
    	println("Raw message: " + c.getRawMessage());
    	if (c.messageAvailable()) {
    		String[] msgs = c.getDataMessage();
    		for (int i = 0; i < msgs.length; i++) {
    			println("Parsed message: " + msgs[i]);
    		}
    	}
    	
    }

	//--------------------------------------
	public void mousePressed() {
		message = true;
		client.broadcast("C");
	}



	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "fourthclient.Messenger" });
	}
}
