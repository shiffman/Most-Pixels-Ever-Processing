#ifndef MPE_BALL
#define MPE_BALL

#include "ofMain.h" 

class testApp;

class ball {

	public:

		ball();

		void setParent(testApp & _parent);
		void move();
		void draw();
		void bounce();
	
		float x; //ellipse x location
		float y; //ellipse y location
		float xdir; //x velocity
		float ydir; //y velocity
		float r;
		
		testApp * parent;
};

#endif