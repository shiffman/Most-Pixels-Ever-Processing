#include "testApp.h"

//--------------------------------------------------------------
void testApp::setup() {
	// initialize app
	ofSetFrameRate(30);
    ofEnableSmoothing();
    ofEnableAlphaBlending();
	ofSetBackgroundAuto(false);
    
	client.setup("settings.xml", this, false);
	
    // set the random seed
	ofSeedRandom(1);
    
    // add a "randomly" placed ball
    Ball* ball = new Ball(ofRandom(0, client.getMWidth()), ofRandom(0, client.getMHeight()), client.getMWidth(), client.getMHeight());
    balls.push_back(ball);
    
	// start client
    client.start();
}

//--------------------------------------------------------------
void testApp::update() {	
}

//--------------------------------------------------------------
void testApp::draw() {
    if (client.isRendering()) {
        // before we do anything, the client must place itself within the 
        //  larger display (this is done with translate, so use push/pop if 
        //  you want to overlay any info on all screens)
        client.placeScreen();
        // clear the screen
        ofBackground(255, 255, 255);
        
        // move and draw all the balls
        for (int i = 0; i < balls.size(); i++) {
            balls[i]->calc();
            balls[i]->draw();
        }
        
        // alert the server that you've finished drawing a frame
        client.done();
    }
}

//--------------------------------------------------------------
// Because we are NOT running in auto mode, no drawing can be
//  made in this callback function!
//--------------------------------------------------------------
void testApp::frameEvent() {
    // read any incoming messages
    if (client.messageAvailable()) {
        vector<string> msg = client.getDataMessage();
        vector<string> xy = ofSplitString(msg[0], ",");
        float x = ofToInt(xy[0]);
        float y = ofToInt(xy[1]);
        Ball* ball = new Ball(x, y, client.getMWidth(), client.getMHeight());
        balls.push_back(ball);
    }
}

//--------------------------------------------------------------
void testApp::keyPressed(int key) {
}

//--------------------------------------------------------------
void testApp::keyReleased(int key) {
}

//--------------------------------------------------------------
void testApp::mouseMoved(int x, int y) {
}

//--------------------------------------------------------------
void testApp::mouseDragged(int x, int y, int button) {
}

//--------------------------------------------------------------
void testApp::mousePressed(int x, int y, int button) {
    // never include a ":" when broadcasting your message
    x += client.getXoffset();
    y += client.getYoffset();
    client.broadcast(ofToString(x) + "," + ofToString(y));
}

//--------------------------------------------------------------
void testApp::mouseReleased(int x, int y, int button) {
}

//--------------------------------------------------------------
void testApp::windowResized(int w, int h) {
}

