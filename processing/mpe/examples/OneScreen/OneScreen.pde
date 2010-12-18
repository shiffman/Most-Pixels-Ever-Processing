/**
 * Simple Bouncing Ball Demo
 * <http://code.google.com/p/mostpixelsever/>
 * @author Shiffman
 */


// The list of balls
ArrayList balls;


void setup() {
  size(320,240);
  smooth();

  // Start with 10 balls
  balls = new  ArrayList();
  for (int i = 0; i < 10; i++) {
    Ball ball= new Ball(random(width),random(height));
    balls.add(ball);
  }
}


public void draw() {

  background(255);

  for (int i = 0; i < balls.size(); i++) {
    Ball ball = (Ball) balls.get(i);
    ball.calc();
    ball.display();
  }
}

public void mousePressed() {
  balls.add(new Ball(mouseX,mouseY));
}

