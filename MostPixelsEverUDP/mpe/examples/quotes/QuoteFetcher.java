/**
 * Simple Bouncing Ball Demo
 * <http://code.google.com/p/mostpixelsever/>
 */

package mpe.examples.quotes;

import mpe.client.*;
import processing.core.*;

public class QuoteFetcher extends PApplet {
    //--------------------------------------
    static final int ID = 2;

    static final int B_MARGIN = 50;
    static final int B_SIZE = 100;

    static final String CARTMAN_URL = "http://www.smacie.com/randomizer/southpark/cartman.html";
    static final String HOMER_URL = "http://www.smacie.com/randomizer/simpsons/homer.html";

    //--------------------------------------
    AsyncClient client;
    PFont font;

    //--------------------------------------
    public void setup() {
        // set up the client
        client = new AsyncClient();
        size(350, 200);

        // init the drawing functions
        smooth();

        // init the text functions
        String[] fonts = PFont.list();
        font = createFont(fonts[(int)random(0, fonts.length)], 12);
        textFont(font, 18);
        textAlign(CENTER, CENTER);
    }

    //--------------------------------------
    public void draw() {
        background(255);

        // draw the buttons
        stroke(0);
        if (inside("Cartman"))
            fill(255, 128, 0);
        else
            fill(128, 255, 0);
        rect(B_MARGIN, B_MARGIN, B_SIZE, B_SIZE);

        if (inside("Homer"))
            fill(255, 128, 0);
        else
            fill(128, 255, 0);
        rect(B_MARGIN*2 + B_SIZE, B_MARGIN, B_SIZE, B_SIZE);

        // draw the labels
        noStroke();
        fill(0);
        text("Cartman", B_MARGIN + B_SIZE/2, B_MARGIN + B_SIZE/2);
        text("Homer", B_MARGIN*2 + B_SIZE*3/2, B_MARGIN + B_SIZE/2);

        // draw the title
        text("The Quote Fetcher", width/2, B_MARGIN/2);
    }

    //--------------------------------------
    public void mousePressed() {
        if (inside("Cartman")) {
            client.broadcast(getQuote("Cartman"));
        } 

        else if (inside("Homer")) {
            client.broadcast(getQuote("Homer"));
        }
    }

    //--------------------------------------
    public boolean inside(String button) {
        if (button == "Cartman") {
            if (mouseX > B_MARGIN && mouseX < B_MARGIN + B_SIZE && 
                    mouseY > B_MARGIN && mouseY < B_MARGIN + B_SIZE)
                return true;
            return false;
        }

        else {
            if (mouseX > B_MARGIN*2 + B_SIZE && mouseX < B_MARGIN*2 + B_SIZE*2 &&
                    mouseY > B_MARGIN && mouseY < B_MARGIN + B_SIZE)
                return true;
            return false;
        }
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

        println(quote);

        return quote;
    }

    //--------------------------------------
    static public void main(String args[]) {
        PApplet.main(new String[] { "mpe.examples.quotes.QuoteFetcher" });
    }
}
