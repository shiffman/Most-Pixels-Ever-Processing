#pragma once

#include "ofMain.h"
#include "ofxXmlSettings.h"
#include "ofxOsc.h"


class testApp : public ofBaseApp{

	public:

		void setup();
		void update();
		void draw();
		
		void keyPressed(int key);
		void keyReleased(int key);
		void mouseMoved(int x, int y );
		void mouseDragged(int x, int y, int button);
		void mousePressed(int x, int y, int button);
		void mouseReleased(int x, int y, int button);
		void windowResized(int w, int h);
		void dragEvent(ofDragInfo dragInfo);
		void gotMessage(ofMessage msg);		
	
		void loadSettings(string fileString);

		ofxOscReceiver	receiver;
	
		ofVideoPlayer 		movie;
	
		ofTrueTypeFont		font;
	
		bool started;
	
		int port;
	
		int movieWidth;
		int movieHeight;
		int movieX;
		int movieY;
	
		char * movieFile;
	
		bool fullscreen;
};

