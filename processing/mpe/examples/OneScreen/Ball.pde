/**
 * Ball class for simple bouncing ball demo
 * <http://http://code.google.com/p/mostpixelsever/>
 * @author Shiffman
 */

class Ball {

  float x = 0;    // Ellipse x location
  float y = 0;    // Ellipse y location
  float xdir = 1; // x velocity
  float ydir = 1; // y velocity

  float r = 24;   // size

  Ball(float _x, float _y){
    xdir = random(-5,5);
    ydir = random(-5,5);
    x = _x;
    y = _y;
  }

  // A simple bounce across the screen
  void calc(){
    if (x < 0 || x > width) xdir *= -1;
    if (y < 0 || y > height) ydir *= -1;
    x += xdir;
    y += ydir;
  }

  void display(){
    stroke(0);
    fill(0,100);
    ellipse(x,y,r,r);
  }
}

