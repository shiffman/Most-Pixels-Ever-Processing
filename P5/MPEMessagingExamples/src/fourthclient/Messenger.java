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
	AsyncClient client;
	PFont font;

	boolean message = false;

	//--------------------------------------
	public void setup() {
		// set up the client
		client = new AsyncClient("localhost",9003);
		size(350, 200);

		smooth();
		frameRate(30);
		font = createFont("Arial", 12);
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
