#ifndef _TEST_APP
#define _TEST_APP


#include "ofMain.h"
#include "ofAddons.h"

#include "udpClient.h"
#include "ball.h"

class testApp : public ofSimpleApp {
	
	public:
		
		void setup();
		void update();
		void draw();
		
		void keyPressed(int key);
		void keyReleased(int key);
		void mouseMoved(int x, int y );
		void mouseDragged(int x, int y, int button);
		void mousePressed(int x, int y, int button);
		void mouseReleased();
		
		void addBall(int x, int y);
		
		udpClient client;
		ball * bouncingBalls;
		int activeBallCount;
		int maxBalls;
};

#endif
	
