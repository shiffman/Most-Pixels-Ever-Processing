/**
 * MPE Messainge Demo with simpleML library grabbing XML feed
 * @author Shiffman
 */

package feeds;

import java.util.ArrayList;

import mpe.client.TCPClient;

import processing.core.PApplet;
import processing.data.XML;

public class Headlines extends PApplet {

	// MPE Client Stuff
	final int ID = 0;
	TCPClient client;

	// XML stuff
	XMLRequest yahoo;
	final String url = "http://rss.news.yahoo.com/rss/topstories";

	// ArrayList of Headline objects
	ArrayList<Headline> headlines;

	// We will store the headlines we retrieve in a String
	String[] yahoos;
	// The counter will go through the headlines one at a time
	// -1 means no headlines yet
	int counter;

	boolean started;

	// --------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "feeds.Headlines" });
	}

	// --------------------------------------
	public void setup() {
		// make a new Client using an XML file
		client = new TCPClient(this, "mpe" + ID + ".xml");

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());
		resetEvent(client);
		client.start();
	}

	// --------------------------------------
	// Start over
	public void resetEvent(TCPClient c) {
		// the random seed must be identical for all clients
		randomSeed(1);

		headlines = new ArrayList<Headline>();
		counter = -1;
		started = false;

		// Creating and starting the request
		// Only if we are client 0!!! (one thing we could do is have different
		// clients read from different feeds)
		PApplet.println("ID: " + client.getID());
		if (client.getID() == 0) {
			yahoo = new XMLRequest(this, 5000, url);
			yahoo.start();
		}
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
		
		// For every message a new object is made
		for (int i = 0; i < msg.length; i++) {
			Headline headline = new Headline(this, client, msg[i],
					random(client.getMWidth()), client.getMHeight());
			headlines.add(headline);
		}
	}

	// --------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {

		/*if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			// For every message a new object is made
			for (int i = 0; i < msg.length; i++) {
				Headline headline = new Headline(this, client, msg[i],
						random(client.getMWidth()), client.getMHeight());
				headlines.add(headline);
			}
		}*/

		background(255);

		// Deal with all of the headline objects
		// We iterate backwards b/c we are deleting
		for (int i = headlines.size() - 1; i >= 0; i--) {
			Headline headline = (Headline) headlines.get(i);
			headline.move();
			headline.draw();
			// Deleting ones that are off the screen
			if (headline.finished()) {
				headlines.remove(i);
			}
		}

		// Here is the funny business
		// If we are client #0, and there are headlines available
		// We broadcast a new one every 30 frames
		// (We don't have to do it this way, we could just send them all at
		// once, just an arbitrary method!)
		if (client.getID() == 0) {
			if (counter >= 0 && client.getFrameCount() % 30 == 0) {

				String headline = yahoos[counter];
				headline = headline.replaceAll(":", " "); // Make sure there are
															// no
															// semi-colons!!
				client.broadcast(headline); // send out the String
				counter++; // Go to the next one
				// If we're at the end, reset
				if (counter == yahoos.length) {
					counter = -1;
				}
			}

			// Get more data when counter is reset to -1
			if (counter < 0 && yahoo.getAvailability()) {
				parse(yahoo.getXML());
			}
		}

	}

	// When the request is complete
	public void parse(XML xml) {
		// Retrieving an array of all XML elements inside "<title*>" tags
		XML[] titles = xml.getChildren("channel/item/title");
		yahoos = new String[titles.length];
		for (int i = 0; i < yahoos.length; i++) {
			yahoos[i] = titles[i].getContent();
		}
		// Counter restarts at 0
		counter = 0;
	}
}
