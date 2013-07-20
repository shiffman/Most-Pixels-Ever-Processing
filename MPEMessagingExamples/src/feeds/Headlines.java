/**
 * MPE Messainge Demo with simpleML library grabbing XML feed
 * @author Shiffman
 */

package feeds;

import java.util.ArrayList;

import mpe.client.TCPClient;

import processing.core.PApplet;
import simpleML.*; 

public class Headlines extends PApplet {

	// MPE Client Stuff
	final int ID = 1;
	TCPClient client;

	// simpleML request object
	XMLRequest xmlRequest; 
	// ArrayList of Headline objects
	ArrayList headlines;

	// We will store the headlines we retrieve in a String
	String[] yahoos;
	// The counter will go through the headlines one at a time
	// -1 means no headlines yet
	int counter = -1;

	boolean started = false;

	//--------------------------------------
	static public void main(String args[]) {
		PApplet.main(new String[] { "feeds.Headlines"});
	}	

	//--------------------------------------
	public void setup() {
		// make a new Client using an INI file
		// sketchPath() is used so that the INI file is local to the sketch
		client = new TCPClient(sketchPath("mpefiles/mpe"+ID+".ini"), this);

		// the size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight());

		// the random seed must be identical for all clients
		randomSeed(1);

		headlines = new ArrayList();

		// Creating and starting the request 
		// Only if we are client 0!!! (one thing we could do is have different clients read from different feeds)
		if (client.getID() == 0) {
			xmlRequest = new XMLRequest(this,"http://rss.news.yahoo.com/rss/topstories"); 
			xmlRequest.makeRequest(); 
		}
		client.start();
	} 

	//--------------------------------------
	// Keep the motor running... draw() needs to be added in auto mode, even if
	// it is empty to keep things rolling.
	public void draw() {
		frame.setLocation(client.getID()*client.getLWidth(),0);
	}

	//--------------------------------------
	// Triggered by the client whenever a new frame should be rendered.
	// All synchronized drawing should be done here when in auto mode.
	public void frameEvent(TCPClient c) {

		if (c.messageAvailable()) {
			String[] msg = c.getDataMessage();
			// For every message a new object is made
			for (int i = 0; i < msg.length; i++) {
				Headline headline = new Headline(this,client,msg[i], random(client.getMWidth()),client.getMHeight());
				headlines.add(headline);
			}
		}

		background(255);

		// Deal with all of the headline objects
		// We iterate backwards b/c we are deleting
		for (int i = headlines.size()-1; i >= 0; i--) {
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
		// (We don't have to do it this way, we could just send them all at once, just an arbitrary method!)
		if (client.getID() == 0 && counter > -1 && client.getFrameCount() % 30 == 0) {
			String headline = yahoos[counter];
			headline = headline.replaceAll(":", " ");  // Make sure there are no semi-colons!!
			client.broadcast(headline);  // send out the String
			counter++;  // Go to the next one
			// If we're at the end, reset
			if (counter == yahoos.length) {
				counter = -1;
				xmlRequest.makeRequest();  // and make the request again!
			}
		}
	}


	//	 When the request is complete 
	public void netEvent(XMLRequest ml) { 
		// Retrieving an array of all XML elements inside "<title*>" tags 
		yahoos = ml.getElementArray("title"); 
		// Counter restarts at 0
		counter = 0;
	} 
}
