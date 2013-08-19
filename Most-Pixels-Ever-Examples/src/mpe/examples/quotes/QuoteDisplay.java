/**
 * Quotes Demo
 * The QuoteDisplay is an synchronized client displays the quotes received from
 * the server.
 * <http://code.google.com/p/mostpixelsever/>
 * 
 * @author Elie Zananiri
 */

package mpe.examples.quotes;

import mpe.client.*;
import processing.core.*;

public class QuoteDisplay extends PApplet {

	// --------------------------------------
	static final int MARGIN = 10;

    final int ID = 0;

	// --------------------------------------
	TCPClient client;

	PFont font;
	int lineHeight;
	int numLines;
	String quote = "Loading...";

	// --------------------------------------
	public void setup() {
		// set up the client
		client = new TCPClient(this, "mpe" + ID + ".xml");
		size(client.getLWidth(), client.getLHeight());
		randomSeed(3030);

		// init the drawing functions
		smooth();
		noStroke();
		fill(255, 128, 0);
		background(255);

		// init the text functions
		String[] fonts = PFont.list();
		font = createFont(fonts[(int) random(0, fonts.length)], 48);
		textFont(font, 48);
		textAlign(CENTER);
		lineHeight = (int) (textAscent() + textDescent());

		resetEvent(client);
		// start the client
		client.start();
	}

	// --------------------------------------
	// Start over
	public void resetEvent(TCPClient c) {
		// This example has nothing to reset
	}

	// --------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
	}
	
	//--------------------------------------
	// Separate data event
	public void dataEvent(TCPClient c) {
		String[] msg = c.getDataMessage();
		println(msg);
		quote = msg[0];	
	}

	// --------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {
		// You can read incoming messages here as well
		/*if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			println(msg);
			quote = msg[0];
		}*/

		// clear the screen
		background(255);

		// draw the current quote
		text(format(quote), client.getMWidth() / 2,
				(client.getMHeight() - lineHeight * numLines) / 2);
	}

	// --------------------------------------
	// Adds newlines before displaying the text to make sure it fits in the
	// window
	public String format(String txt) {
		numLines = 1;

		if (textWidth(txt) <= (client.getMWidth() - MARGIN * 2)) {
			return txt;
		}

		String[] words = txt.split("\\s");
		String formatTxt = "";
		String line = "";
		int i = 0;
		while (i < words.length) {
			while (i < words.length
					&& textWidth(line + words[i]) <= (client.getMWidth() - MARGIN * 2)) {
				line += " " + words[i];
				i++;
			}
			// add a new line
			formatTxt += line + "\n";
			numLines++;
			line = "";
		}

		numLines--;
		return formatTxt;
	}

	// --------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "mpe.examples.quotes.QuoteDisplay" });
	}
}
