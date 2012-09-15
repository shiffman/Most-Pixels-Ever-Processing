#include "testApp.h"

//--------------------------------------------------------------
void testApp::setup(){
	// Load initial settings
	loadSettings("settings.xml");
	
	started = false;
	fullscreen = false;
	ofSetVerticalSync(true);
	
	
	// Set up OSC
	receiver.setup( port );
	std::cout << "listening for osc messages on port " << port << "\n";
	
	std::cout << movieFile;
	
	// Load movie
	movie.loadMovie(movieFile);
	movie.play();
	movie.idleMovie();

	
	ofBackground( 0, 0, 0 );
	
}

//--------------------------------------------------------------
void testApp::update(){
	
	if (started) {
		movie.idleMovie();
	}

	
	// Check for waiting messages
	while( receiver.hasWaitingMessages() )
	{
		// Get the next message
		ofxOscMessage m;
		receiver.getNextMessage( &m );
		
		// Check for movie position info
		if ( m.getAddress() == "/movie/position" )
		{
			float p = m.getArgAsFloat(0);
			if (!started) {
				started = true;
				movie.play();
				// No need to loop, we loop if the master loops
				movie.setLoopState(OF_LOOP_NONE);
			}
			// Set the position
			movie.setPosition(p);

		}
		
	}
}

//--------------------------------------------------------------
void testApp::draw(){

	if (fullscreen) {
		ofHideCursor();
	}
	movie.draw(movieX,movieY,movieWidth,movieHeight);
	
	// Display some debugging info
	//char buf[256];
	//sprintf( buf, "listening for osc messages on port %d", port );
	//ofDrawBitmapString( buf, 10, 20 );
}

//--------------------------------------------------------------
void testApp::keyPressed  (int key){

}

//--------------------------------------------------------------
void testApp::keyReleased(int key){

}

//--------------------------------------------------------------
void testApp::mouseMoved(int x, int y ){
}

//--------------------------------------------------------------
void testApp::mouseDragged(int x, int y, int button){
}

//--------------------------------------------------------------
void testApp::mousePressed(int x, int y, int button){

}


//--------------------------------------------------------------
void testApp::mouseReleased(int x, int y, int button){

}

//--------------------------------------------------------------
void testApp::windowResized(int w, int h){

}

//--------------------------------------------------------------
void testApp::gotMessage(ofMessage msg){

}

//--------------------------------------------------------------
void testApp::dragEvent(ofDragInfo dragInfo){ 

}

void testApp::loadSettings(string fileString){
	
	
	//--------------------------------------------- get configs
    ofxXmlSettings xmlReader;
	
	bool result = xmlReader.loadFile(fileString);
	
	if(!result) printf("error loading xml file\n");
	
	int w = xmlReader.getValue("settings:dimensions:width", 640, 0);
	int h = xmlReader.getValue("settings:dimensions:height", 480, 0);
	
	movieWidth = xmlReader.getValue("settings:dimensions:movieWidth", 640, 0);
	movieHeight = xmlReader.getValue("settings:dimensions:movieHeight", 480, 0);
	
	movieX = xmlReader.getValue("settings:dimensions:movieX", 640, 0);
	movieY = xmlReader.getValue("settings:dimensions:movieY", 480, 0);
	
	port = xmlReader.getValue("settings:port",9999,0);
	
	string filename = xmlReader.getValue("settings:movie:","test",0);
	movieFile = (char *) malloc(sizeof(char)*filename.length());
	strcpy(movieFile, filename.c_str());
	
	ofSetWindowShape(w, h);
	
	if(xmlReader.getValue("settings:go_fullscreen", "false", 0).compare("true") == 0) {
		fullscreen = true;
		ofSetFullscreen(true);
	}
	
	
}
