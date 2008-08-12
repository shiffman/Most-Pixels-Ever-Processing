/**
 * The MPE Client
 * The Client class registers itself with a server
 * and receives messages related to frame rendering and data input
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 */

package mpe.client;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import mpe.config.FileParser;

import processing.core.PApplet;

public class UDPClient extends Thread {
	
	public static int repeatTime = 20;
	
    FileParser fp;

    String host;
    
    BroadcastDoneThread bdt;
    
    // New UDP Stuff!
	DatagramSocket socket;
	InetAddress address;
    int serverPort = 9002;
	int clientPort;
	byte[] data = new byte[65535];
	
    
    PApplet p5parent;
    MpeDataListener parent;
    int id = 0; /*this is used for communication to let the server know which client is speaking
	 and how to order the screens*/
    int numScreens; //the total number of screens
    Method frameEventMethod;
    int mWidth = -1; //master width
    int mHeight = -1; //master height
    int lWidth = 640; //local width
    int lHeight = 480; //local height
    int xOffset = 0;
    int yOffset = 0;
    boolean done = false; //flipped to true when we're done rendering.
    //public boolean moveOn = false; //tells parent to loop back
    int fps = -1;
    boolean running = false;
    boolean useProcessing = false;
    int frameCount = 0;
    boolean rendering = false;
    // Ok, adding something so that a client can get a dataMessage as part of a frameEvent
    String[] dataMessage;
    
    // Are we broadcasting?
    boolean broadcastingData = false;
    // Do we need to wait to broadcast b/c we have alreaddy done so this frame?
    boolean waitToSend = false;

    boolean messageAvailable;  // Is a message available?
    boolean intsAvailable;     // Is an int array available?
    boolean bytesAvailable;    // is a byte array avaialble?
    boolean sayDoneAgain = false;  // Do we need to say we're done after a lot of data has been sent
    int[] ints;                // ints that have come in
    byte[] bytes;              // bytes that have come in
    
    // public fields, do we need them to be public?
    /**
     * If DEBUG is true, the client will print lots of messages about what it is doing.
     * Set with debug=1; in your INI file.
     */
    public static boolean DEBUG = false;

    /**
     * True if all the other clients are connected.  Maybe this doesn't need to be public.
     */
    public boolean allConnected = false;


    /**
     * Client is constructed with an init file location, and the parent PApplet.
     * The parent PApplet must have a method called "frameEvent(Client c)".
     *
     * The frameEvent handles syncing up the frame rate on the
     * multiple screens.  A typical implementation may look like this:
     *
     * 	public void frameEvent(Client c){
     *  if (!started) started = true;
     *    redraw();
     *  }
     *
     */
    public UDPClient(String fileString, Object p) {
        useProcessing = true;
        p5parent = (PApplet) p;
        loadIniFile(fileString);
        Client(host, serverPort, id);
    }
    
    /**
     * Client is constructed with an init file location, and the parent MpeDataListener.
     * The parent must have a method called "frameEvent(Client c)".
     *
     * The frameEvent handles syncing on the
     * multiple screens.  A typical implementation may look like this:
     *
     *  public void frameEvent(Client c){
     *    if (!started) started = true;
     *    // Do your computation and paint to the screen here
     *  }
     *
     */
    public UDPClient(String fileString, MpeDataListener p) {
        parent = p;
        loadIniFile(fileString);
        Client(host, serverPort, id);
    }

    /**
     * Loads the Settings from the Client INI file
     */
    private void loadIniFile(String fileString){
        fp = new FileParser(fileString);
        //parse ini file if it exists
        if (fp.fileExists()) {
            setPort(fp.getIntValue("port"));
            setID(fp.getIntValue("id"));
            setServer(fp.getStringValue("server"));
            int[] localDim = fp.getIntValues("localScreenSize");
            setLocalDimensions(localDim[0], localDim[1]);
            int[] offsets = fp.getIntValues("localLocation");
            setOffsets(offsets[0], offsets[1]); 
            // This somehow got lost w/ the separate client and server INI files
            int[] masterDims = fp.getIntValues("masterDimensions");
            this.setMasterDimensions(masterDims[0], masterDims[1]);
            out("Settings: server = " + host + ":" + serverPort + ",  id = " + id
                    + ", local dimensions = " + lWidth + ", " + lHeight
                    + ", location = " + xOffset + ", " + yOffset);
            int num = fp.getIntValue("debug");
            if (num == 1) DEBUG = true;
        }
    }
    private void Client(String host_, int port_, int _id) {
        host = host_;
        try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        serverPort = port_;
        id = _id;
        clientPort = serverPort + 1 + id;

        //use reflect if using processing applet, use interface for normal Java
        if (useProcessing){
            try {
                // Looking for a method called "frameEvent", with one argument of Client type
                frameEventMethod = p5parent.getClass().getMethod("frameEvent",
                        new Class[] { UDPClient.class });
            } catch (Exception e) {
                System.out.println("You are missing the frameEvent() method." + e);
            }
        } else {
            // In case we need to do something in the case of not Processing
        }
        
    }

    /**
     * Sets the master dimensions for the Video Wall. This is used to calculate
     * what is rendered.
     * 
     * @param _mWidth
     *            The master width
     * @param _mHeight
     *            The master height
     */
    public void setMasterDimensions(int _mWidth, int _mHeight) {
        mWidth = _mWidth;
        mHeight = _mHeight;
    }

    /**
     * Sets the dimensions for the local display.
     * The offsets are used to determine what part of the Master Dimensions to render.
     * For example, if you have two screens, each 100x100, and the master dimensions are 200x100
     * then you would set
     * 	client 0: setLocalDimensions(0,0,100,100)
     * 	client 1: setLocalDimensions(100,0,100,100)
     * for a 10 pixel overlap you would do:
     * 	client 0: setLocalDimensions(0,0,110,100)
     * 	client 1: setLocalDimensions(90,0,110,100);
     * 
     * @param _xOffset Offsets the display along x axis
     * @param _yOffset Offsets the display along y axis
     * @param _lWidth The local width
     * @param _lHeight The local height
     */
    public void setLocalDimensions(int _xOffset, int _yOffset, int _lWidth, int _lHeight) {
        xOffset = _xOffset;
        yOffset = _yOffset;
        lWidth = _lWidth;
        lHeight = _lHeight;
    }

    /**
     * returns local width.
     * @return local width in pixels
     */
    public int getLWidth() {
        return lWidth;
    }

    /**
     * returns local height.
     * @return local height in pixels
     */
    public int getLHeight() {
        return lHeight;
    }

    /**
     * returns master width.
     * @return master width in pixels
     */
    public int getMWidth() {
        return mWidth;
    }

    /**
     * returns x offset of frame.
     * @return x offset of frame.
     */
    public int getXoffset() {
        return xOffset;
    }
    /**
     * returns y offset of frame.
     * @return y offset of frame.
     */
    public int getYoffset() {
        return yOffset;
    }

    /**
     * returns master height.
     * @return master height in pixels
     */
    public int getMHeight() {
        return mHeight;
    }
    
    /**
     * Places the viewing area for this screen. This must be called at the beginning
     * of the render loop.  If you are using Processing, you would typicall place it at
     * the beginning of your draw() function.
     *
     */
    public void placeScreen() {
        p5parent.translate(xOffset * -1, yOffset * -1);
    }
    
    /**
     * Sends a "Done" command to the server. This must be called at the end of your draw loop.
     *
     */
    public void done() {
    	//System.out.println("Sending Done = true");
    	rendering = false;
    	bdt.interrupt();
    	bdt.sendingDone = true;
    	
        /*if (broadcastingData) {
            sayDoneAgain = true;
        } else {
            String msg = "D," + id + "," + frameCount;
            send(msg);
            done = true;
        }*/
    }

    /**
     * Returns this screen's ID
     * @return screen's ID #
     */
    public int getClientID() {
        return id;
    }

    private void out(String string) {
        System.out.println("Client: " + string);
    }

    // UNSYNCHRONIZE!
    private void read(String serverInput) {
        if (DEBUG) System.out.println("Receiving: " + serverInput);
        /*
         * This is a hack for now.  this will block only once but it will allow
         * everything to start at once.
         */
        if (serverInput.startsWith("M") && mWidth == -1) {
            serverInput = serverInput.substring(1);
            String[] mdim = serverInput.split(",");
            mWidth = Integer.parseInt(mdim[0]);
            mHeight = Integer.parseInt(mdim[1]);
        }
        //A "G" startbyte will trigger a frameEvent.
        //If it's a B, we also have to get a byteArray
        //An I for int array
        char c = serverInput.charAt(0);
        if (c == 'G' || c == 'B' || c == 'I') {
            if (!allConnected) {
                if (DEBUG) print("all connected!");
                allConnected = true;
            }
            // Split into frame message and data message
            String[] info = serverInput.split(":");
            String[] frameMessage = info[0].split(",");
            int fc = Integer.parseInt(frameMessage[1]);

            // There is a message here with the frameEvent
            if (info.length > 1) {
                String[] dataInfo = new String[info.length-1];
                for (int k = 1; k < info.length; k++){
                    dataInfo[k-1]=info[k];
                }
                dataMessage = null;//clear
                dataMessage = dataInfo;
                messageAvailable = true;
            } else {
                messageAvailable = false;
            }

     
            // System.out.println("From server: " + fc + " me: " + frameCount);
            if (fc == frameCount && !rendering) {
            	rendering = true;
            	bdt.sendingDone = false;
            	frameCount++;
                // if (DEBUG) System.out.println("Matching " + fc);
                if (useProcessing && frameEventMethod != null){
                    try {
                        // Call the method with this object as the argument!
                        frameEventMethod.invoke(p5parent, new Object[] { this });
                    } catch (Exception e) {
                        // Error handling
                        System.err.println("I couldn't invoke frame method for some reason.");
                        e.printStackTrace();
                        frameEventMethod = null;
                    }
                } else {
                    parent.frameEvent(this);
                }
                
                
            } else {
                if (DEBUG)print("Extra message, mycount: " + frameCount + " received from server: " + fc);
            }
        }
    }
    
    /**
     * This method should only be called internally by Thread.start().
     */
    public void run() {
        if (DEBUG) print("I'm running!");
        //let server that this client is ready to start.
        send("S" + id);
        //done();
        try {
            while (running) {
            	
            	// Read UDP
            	DatagramPacket packet = new DatagramPacket(data,data.length);
    			socket.receive(packet);
    			
    			String msg = new String(data,0,packet.getLength());
    			
    			if (msg == null) {
                    //running = false;
                    break;
                } else {
                    read(msg);
                }

                // Do we need this sleep or are we just slowing
                // ourselves down for no reason??
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method must be called when the client applet starts up.
     * It will tell the server it is ready.
     */
    public void start() {
        try {
			socket = new DatagramSocket(clientPort);
			
			// Start the broadcasting thread
			bdt = new BroadcastDoneThread(this);
	        bdt.start();

			
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
        super.start();
    }

    // UNSYNCHRONIZE!
    private void send(String msg) {
        if (DEBUG) System.out.println("Sending: " + msg);
        try {
        	byte[] data = msg.getBytes();
        	DatagramPacket packet = new DatagramPacket(data,data.length,address,serverPort);
    		packet.setData(data);
			socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * broadcasts a string to all screens
     * Do not use a colon (':') in your message!!!
     * @param msg
     */
    public void broadcast(String msg) {
        msg = "T"+ msg;
        send(msg);
    }

    /**
     * broadcasts a byte array to all screens
     * Large arrays could cause performance issues
     * depending on network speed
     * @param data the array to broadcast
     */
    /*public void broadcastByteArray(byte[] data) {
    	broadcastByteArray(data,data.length);
    }*/
    
    /**
     * broadcasts a byte array to all screens
     * Large arrays could cause performance issues
     * depending on network speed
     * @param data
     * @param len how many elements of the array should be broadcasted, should not be larger than the array size 
     */  
    /*public void broadcastByteArray(byte[] data, int len) {
        // We won't send an int array more than
        // once during any given "frame"
        if (!waitToSend) {
            waitToSend = true;
            broadcastingData = true;
            String msg = "B";
            send(msg);
            if (DEBUG) System.out.println("Sending: " + data.length + " bytes.");
            try {
                dos.writeInt(len);
                dos.write(data, 0, len);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            broadcastingData = false;
            // If we finished while this was happening, we didn't say we were done
            // So we need to now
            if (sayDoneAgain) {
                done();
                sayDoneAgain = false;
            }
        } else {
            if (DEBUG) System.out.println("Gotta wait dude, haven't received the ints back yet.");
        }
    }*/


    /**
     * broadcasts an array of ints to all screens
     * Large arrays can cause performance problems
     * @param data the array to broadcast
     */      
    /*public synchronized void broadcastIntArray(int[] data) {
        // We won't send an int array more than
        // once during any given "frame"
        if (!waitToSend) {
            waitToSend = true;
            broadcastingData = true;
            String msg = "I";
            send(msg);
            if (DEBUG) System.out.println("Sending: " + data.length + " bytes.");
            try {
                dos.writeInt(data.length);
                for (int i = 0; i < data.length; i++) {
                    dos.writeInt(data[i]);
                }
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            broadcastingData = false;
            // If we finished while this was happening, we didn't say we were done
            // So we need to now
            if (sayDoneAgain) {
                done();  // HACK TO SAY WE'RE DONE AGAIN!!!
                sayDoneAgain = false;
            }
        } else {
            if (DEBUG) System.out.println("Gotta wait dude, haven't received the ints back yet.");
        }
    }*/


    /**
     * Stops the client thread.  You don't really need to do this ever.
     */  
    public void quit() {
        System.out.println("Quitting.");
        running = false; // Setting running to false ends the loop in run()
        interrupt(); // In case the thread is waiting. . .
    }

    private void print(String string) {
        System.out.println("MPE CLIENT: " + string);
    }


    private void setServer(String _server) {
        if (_server != null)
            host = _server;
    }

    private void setLocalDimensions(int w, int h) {
        if (w > -1 && h > -1) {
            lWidth = w;
            lHeight = h;
        }
    }

    private void setOffsets(int w, int h) {
        if (w > -1 && h > -1) {
            xOffset = w;
            yOffset = h;
        }
    }

    private void setID(int _ID) {
        if (_ID > -1)
            id = _ID;
    }

    private void setPort(int _port) {
        if (_port > -1)
            serverPort = _port;
    }

    

    /**
     * Returns true of false based on whether a String message is available
     * This should be used inside frameEvent() since messages are tied to specific frames
     * @return true if new String message 
     */
    public boolean messageAvailable() {
        return messageAvailable;
    }

    /**
     * This should be used inside frameEvent() since messages are tied to specific frames
     * Only should be called after checking that {@link #messageAvailable()}  returns true
     * @return an array of messages from the server
     */
    public String[] getDataMessage() {
        return dataMessage;
    }

    /**
     * This should be used inside frameEvent() since data from server is tied to a specific frame
     * @return true is an array of integers is available from server
     */    
    public boolean intsAvailable() {
        return intsAvailable;
    }

    /**
     * This should be used inside frameEvent() since data from server is tied to a specific frame
     * @return true is an array of bytes is available from server
     */    
    public boolean bytesAvailable() {
        return bytesAvailable;
    }

    /**
     * This should be used inside frameEvent() since data from server is tied to a specific frame
     * Only should be called after checking that {@link #bytesAvailable()} returns true
     * @return the array of bytes from the server
     */    
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * This should be used inside frameEvent() since data from server is tied to a specific frame
     * Only should be called after checking that {@link #intsAvailable()}  returns true
     * @return the array of ints from the server
     */    
    public int[] getInts() {
        return ints;
    }

    /**
     * @return the total number of frames rendered
     */  
    public int getFrameCount() {
        return frameCount;
    }

}