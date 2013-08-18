package feeds;

import processing.core.*;
import processing.data.XML;

// Separate thread for making XML requests

public class XMLRequest extends Thread {
	boolean isAvailable; // Is the thread available?
	boolean isRunning; // Is the thread running? Yes or no?
	int wait; // How many milliseconds should we wait in between executions?
	String url; // XML request url
	XML xml; // XML object

	PApplet parent;

	// Constructor, create the thread
	// It is not running by default
	XMLRequest(PApplet p, int _wait, String _url) {
		parent = p;
		wait = _wait;
		isRunning = false;
		url = _url;
	}

	public XML getXML() {
		return xml;
	}

	// Overriding "start()"
	public void start() {
		// Set running equal to true
		isRunning = true;
		// Print messages
		PApplet.println("Starting thread (will execute every " + wait
				+ " milliseconds.)");
		// Do whatever start does in Thread, don't forget this!
		super.start();
	}

	// We must implement run, this gets triggered by start()
	public void run() {

		while (isRunning) {
			xml = parent.loadXML(url);
			// New data is available!
			isAvailable = true;	
			try {
				// Wait five seconds
				sleep((long) (wait));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		PApplet.println("Data returned from: " + url + "!"); // The thread is
																// done when
		// we get to the end of
		// run()
	}

	// Our method that quits the thread
	public void quit() {
		System.out.println("Quitting.");
		isRunning = false; // Setting running to false ends the loop in run()
		// In case the thread is waiting. . .
		interrupt();
	}

	public boolean getAvailability() {
		return isAvailable;
	}
}
