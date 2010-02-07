#pragma once

#include "ofMain.h" 

class Ball {

	public:
		Ball(float _x, float _y, float _mWidth, float _mHeight) {
            x = _x;
            y = _y;
            xDir = ofRandom(-5, 5);
            yDir = ofRandom(-5, 5);
            d = 10;
            mWidth  = _mWidth;
            mHeight = _mHeight;
        }
    
        //--------------------------------------
        // Moves and changes direction if it hits a wall.
        void calc() {
            if (x < 0 || x > mWidth)  xDir *= -1;
            if (y < 0 || y > mHeight) yDir *= -1;
            x += xDir;
            y += yDir;
        }
        
        //--------------------------------------
        void draw() {
            ofSetColor(0, 0, 0, 100);
            ofFill();
            ofCircle(x, y, d);
            ofSetColor(0);
            ofNoFill();
            ofCircle(x, y, d);
        }

    private:
		float x; 
		float y; 
		float xDir;
		float yDir;
        float d;
        float mWidth;
        float mHeight;
    
};