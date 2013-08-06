import mpe.client.*;

ArrayList<Ball> balls;
TCPClient client;

void setup() {
  // make a new Client using an XML file
  client = new TCPClient(this, "mpe.xml");

  // the size is determined by the client's local width and height
  size(client.getLWidth(), client.getLHeight());

  // the random seed must be identical for all clients
  randomSeed(1);
  balls = new ArrayList<Ball>();
  for (int i = 0; i < 5; i++) {
    Ball ball = new Ball(random(client.getMWidth()), random(client.getMHeight()));
    balls.add(ball);
  }

  // Starting the client
  client.start();
}

// Reset it called when the sketch needs to start over
void resetEvent(TCPClient c) {
  // the random seed must be identical for all clients
  randomSeed(1);
  balls = new ArrayList<Ball>();
  for (int i = 0; i < 5; i++) {
    Ball ball = new Ball(random(client.getMWidth()), random(client.getMHeight()));
    balls.add(ball);
  }
}

//--------------------------------------
// Keep the motor running... draw() needs to be added in auto mode, even if
// it is empty to keep things rolling.
void draw() {
}

//--------------------------------------
// Triggered by the client whenever a new frame should be rendered.
// All synchronized drawing should be done here when in auto mode.
void frameEvent(TCPClient c) {
  // clear the screen     
  background(255);

  // move and draw all the balls
  for (Ball b : balls) {
    b.calc();
    b.draw();
  }

  // read any incoming messages
  if (c.messageAvailable()) {
    String[] msg = c.getDataMessage();
    String[] xy = msg[0].split(",");
    float x = Integer.parseInt(xy[0]);
    float y = Integer.parseInt(xy[1]);
    balls.add(new Ball(x, y));
  }
}

//--------------------------------------
// Adds a Ball to the stage at the position of the mouse click.
void mousePressed() {
  // never include a ":" when broadcasting your message
  int x = mouseX + client.getXoffset();
  int y = mouseY + client.getYoffset();
  client.broadcast(x + "," + y);
}

