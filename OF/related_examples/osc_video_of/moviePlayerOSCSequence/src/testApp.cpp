#include "testApp.h"

//--------------------------------------------------------------
void testApp::setup(){
	ofBackground(255,255,255);

	state = false;
	
	cout << "listening for osc messages on port " << PORT << "\n";
	receiver.setup( PORT );

	movie1.loadMovie("movies/fingers.mov");
	movie1.play();
	movie2.loadMovie("movies/station.mov");
	movie2.play();
}

//--------------------------------------------------------------
void testApp::update(){
    movie1.idleMovie();
    movie2.idleMovie();
	
	while( receiver.hasWaitingMessages() ) {
		ofxOscMessage m;
		receiver.getNextMessage( &m );
		if ( m.getAddress() == "/movie/state" ) {
			state = !state;
			movie1.firstFrame();
			movie2.firstFrame();
			cout << "message received\n";

		}
	}
	
	
}

//--------------------------------------------------------------
void testApp::draw(){

	ofSetHexColor(0xFFFFFF);
	if (state) movie1.draw(0,0,320,240);
	else movie2.draw(0,0,320,240);
}

//--------------------------------------------------------------
void testApp::keyPressed  (int key){
    switch(key){
        case ' ':
			//state = !state;
        break;
    }
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
