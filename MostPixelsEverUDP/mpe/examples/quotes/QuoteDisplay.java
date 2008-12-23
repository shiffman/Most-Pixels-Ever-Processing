/**
 * Simple Bouncing Ball Demo
 * <http://code.google.com/p/mostpixelsever/>
 */

package mpe.examples.quotes;

import mpe.client.*;
import processing.core.*;

public class QuoteDisplay extends PApplet {
    //--------------------------------------
    static final int ID = 0;
    
    static final String CARTMAN_URL = "http://www.smacie.com/randomizer/southpark/cartman.html";
    static final String HOMER_URL = "http://www.smacie.com/randomizer/simpsons/homer.html";
    
    static final int MARGIN = 10;

    //--------------------------------------
    UDPClient client;
	boolean start = false;
	
	PFont font;
	int lineHeight;
    int numLines;
    String quote = "Loading...";
	
	//--------------------------------------
	public void setup() {
		// set up the client
	    client = new UDPClient(sketchPath("ini_quotes/mpeSc" + ID + ".ini"), this);
		size(client.getLWidth(), client.getLHeight());
		randomSeed(3030);
		
		// init the drawing functions
		smooth();
	    noStroke();
		fill(255, 128, 0);
		
		// init the text functions
		String[] fonts = PFont.list();
        font = createFont(fonts[(int)random(0, fonts.length)], 48);
		textFont(font, 48);
		textAlign(CENTER);
		lineHeight = (int)(textAscent() + textDescent());
		
		// start the client
		client.start();
		noLoop();
	}
	
	//--------------------------------------
    public void draw() {
		//frame.setLocation(100 + ID*client.getLWidth(), 300);
		
		if (start) {
			client.placeScreen();
			
			    background(255);
			    text(format(quote), client.getMWidth()/2, (client.getMHeight() - lineHeight*numLines)/2);
			
			client.done();
		} 

        noLoop();
	}
    
  //--------------------------------------
    // adds newlines before displaying the text to make sure it fits in the window
    public String format(String txt) {
        numLines = 1;
        
        if (textWidth(txt) <= (client.getMWidth() - MARGIN*2)) {
            return txt;
        }

        String[] words = txt.split("\\s");
        String formatTxt = "";
        String line = "";
        int i = 0;
        while (i < words.length) {
            while (i < words.length && textWidth(line + words[i]) <= (client.getMWidth() - MARGIN*2)) {
                line += " " + words[i];
                i++;
            }
            // add a new line
            formatTxt += line + "\n";
            numLines++;
            line = "";
        }
        
        numLines--;
        return formatTxt;
    }


    //--------------------------------------
    public void frameEvent(UDPClient c) {
        if (c.messageAvailable()) {
            String[] msg = c.getDataMessage();
            println(msg);
            quote = msg[0];
        }
        start = true;
        loop();
    }
    
    //--------------------------------------
      public String getQuote(String button) {
          String quote;

          if (button == "Cartman")
              quote = join(loadStrings(CARTMAN_URL), "");
          else
              quote = join(loadStrings(HOMER_URL), "");
          
          quote = split(quote, "<font face=\"Comic Sans MS\">")[3];
          quote = split(quote, "</font>")[0];
          
          //println(quote);
          
          return quote;
      }
    
    //--------------------------------------
    public void keyPressed() {
        if (key == 'c') {
            client.broadcast(getQuote("Cartman"));
        } 
        
        else if (key == 'h') {
            client.broadcast(getQuote("Homer"));
        }
    }
    
    //--------------------------------------
    static public void main(String args[]) {
        PApplet.main(new String[] { "mpe.examples.quotes.QuoteDisplay" });
    }
}
