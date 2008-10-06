package bubbles;

import mpe.client.UDPClient;
import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.*;


public class LoopTest extends PApplet { 

	// The Main function
	static public void main(String args[]) {
		PApplet.main(new String[] {"bubbles.LoopTest"});
	}

	// This is triggered by the client whenever a new frame should be rendered
	public void mousePressed(){
		started = true;
		loop();
	}

	public void setup() {
		size(200,200);
		noLoop();
	}
	boolean started = false;
	int counter = 0;
	
	public void draw() {
		if (started) {
			println(counter);
			counter++;
		}
		noLoop();
	}

}
