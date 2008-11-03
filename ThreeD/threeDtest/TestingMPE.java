/**
 * Big Screens Week 3 exercise
 * Convert to multi-screen with <http://www.mostpixelsever.com>
 * @author Shiffman
 */

//This one should be really easy!
//Just moves a rectangle across the screen. . .

package threeDtest;

import mpe.client.UDPClient;
import processing.core.PApplet;

import processing.opengl.*;

public class TestingMPE extends PApplet {

	static public void main(String args[]) {
		PApplet.main(new String[] { "threeDtest.TestingMPE"});
	}	

	float theta = 0;
	final int ID = 1;
	// A client object
	UDPClient client;

	// Variables will be part of client
	float FOV=30; // initial field of view
	float cameraZ;

	// This is triggered by the client whenever a new frame should be rendered
	public void frameEvent(UDPClient c){
		started = true;
		redraw();
	}

	boolean started = false;

	public void setup() {
		// Make a new Client with an INI file.  
		// sketchPath() is used so that the INI file is local to the sketch
		client = new UDPClient(sketchPath("mpe"+ID+".ini"),this);
		// The size is determined by the client's local width and height
		size(client.getLWidth(), client.getLHeight(),OPENGL);
		client.start();
		noLoop();
		// will be part of client
		cameraZ=(height/2.0f)/tan(PI*FOV/360.0f);
	}

	// All this code will go in the library eventually
	public void setupCamera() {

		int mWidth = client.getMWidth();
		int mHeight = client.getMHeight();
		int lWidth = client.getLWidth();
		int lHeight = client.getLHeight();
		int localX = client.getXoffset();
		int localY = client.getYoffset();

		camera(mWidth/2.0f, mHeight/2.0f, cameraZ,
				mWidth/2.0f, mHeight/2.0f, 0, 0, 1, 0);


		// The frustum defines the 3D clipping plane for each Client window!
		float mod = 1f/10f;
		float left   = (localX - mWidth/2)*mod;
		float right  = (localX + lWidth - mWidth/2)*mod;
		float top    = (localY - mHeight/2)*mod;
		float bottom = (localY + lHeight-mHeight/2)*mod;
		float near   = cameraZ*mod;
		float far    = 10000;
		frustum(left,right,top,bottom,near,far);
	}

	// This would be a normal camera function (to be put in library)
	public void restoreCamera() {
		float mod=1/10f;
		camera(width/2.0f, height/2.0f, cameraZ,
				width/2.0f, height/2.0f, 0, 0, 1, 0);
		frustum(-(width/2)*mod, (width/2)*mod,
				-(height/2)*mod, (height/2)*mod,
				cameraZ*mod, 10000);
	}


	public void draw() {
		background(0);


		if (started) {
			setupCamera();

			if (mousePressed) {
				restoreCamera(); // you can see the "normal" view
				client.placeScreen();
			}

			// Draw a spinning cube
			translate(320,120);
			rectMode(CENTER);
			noFill();
			stroke(255);
			rotateY(theta);
			rotateX(theta/3.0f);
			rotateZ(theta/5.0f);
			box(100);
			theta += 0.05f;

			client.done();
		}
	}
}
