

#include "ball.h"
#include "testApp.h"

ball::ball(){

	x = 0; //ellipse x location
	y = 0; //ellipse y location
	xdir = 1; //x velocity
	ydir = 1; //y velocity

	r = 24; // size
}

/*

ball::ball(testApp & _parent, float _x, float _y){

	parent = &_parent;
	xdir = ofRandom(-5,5);
	ydir = ofRandom(-5,5);
	x = _x;
	y = _y;
	r = 24; // size
}

*/

void ball::setParent(testApp & _parent){

	parent = &_parent;
}
//a simple bounce across the screen
void ball::move(){
	
	x += xdir;
	y += ydir;
}


//a simple bounce across the screen
void ball::bounce(){
	if (x < 0 || x > parent->client.getMWidth()) xdir *= -1;
	if (y < 0 || y > parent->client.getMHeight()) ydir *= -1;
	
}

void ball::draw(){

	ofSetColor(255, 255, 255);
	ofCircle(x,y,r);
}

