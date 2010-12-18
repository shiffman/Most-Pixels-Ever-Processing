/**
 * Ball class for simple bouncing ball demo
 * <http://http://code.google.com/p/mostpixelsever/>
 */

//--------------------------------------
// A Ball moves and bounces off walls.
class Ball {
  //--------------------------------------
  float x = 0;     // center x position
  float y = 0;     // center y position
  float xDir = 1;  // x velocity
  float yDir = 1;  // y velocity
  float d = 36;    // diameter

  //--------------------------------------
  public Ball(float _x, float _y) {
    x = _x;
    y = _y;
    xDir = random(-5,5);
    yDir = random(-5,5);
  }

  //--------------------------------------
  // Moves and changes direction if it hits a wall.
  public void calc() {
    if (x < 0 || x > client.getMWidth())  xDir *= -1;
    if (y < 0 || y > client.getMHeight()) yDir *= -1;
    x += xDir;
    y += yDir;
  }

  //--------------------------------------
  public void draw() {
    stroke(0);
    fill(0, 100);
    ellipse(x, y, d, d);
  }
}

