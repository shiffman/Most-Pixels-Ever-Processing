#include "testApp.h"

//--------------------------------------------------------------
void testApp::setup(){

	ofBackground( 40, 100, 40 );

	// open an outgoing connection to HOST:PORT
	sender1.setup( HOST, PORT );
	sender2.setup( HOST, PORT+1);

}

//--------------------------------------------------------------
void testApp::update(){

}

//--------------------------------------------------------------
void testApp::draw(){
	// display instructions
    ofDrawBitmapString("Press a",10,160);

}

//--------------------------------------------------------------
void testApp::keyPressed  (int key){
	if ( key =='a' || key == 'A' ){
		ofxOscMessage m;
		m.setAddress( "/movie/state" );
		m.addIntArg( 1 );
		sender1.sendMessage( m );
		sender2.sendMessage( m );
		cout << "message sent\n";

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

