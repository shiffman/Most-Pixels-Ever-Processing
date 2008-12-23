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

import processing.core.*;

public class UDPClient extends Thread {
    /** If DEBUG is true, the client will print lots of messages about what it is doing.
     * Set with debug=1; in your INI file. */
    public static boolean DEBUG = true;
    
	public static int repeatTime = 50; 

	// UDP stuff
	FileParser fp;
    String hostName;
	int serverPort = 9002;
    DatagramSocket socket;
	InetAddress address;
	int clientPort;
	byte[] data = new byte[65535];

	PApplet p5parent;
	MpeDataListener parent;
	Method frameEventMethod;
	BroadcastDoneThread bdt;
    
	/** The id is used for communication with the server, to let it know which 
     *  client is speaking and how to order the screens. */
    int id = 0;
	/** The total number of screens. */
    int numScreens;
	
	/** The master width. */
    protected int mWidth = -1;
    /** The master height. */
    protected int mHeight = -1;
    
    /** The local width. */
    protected int lWidth = 640;
    /** The local height. */
    protected int lHeight = 480;
    
    protected int xOffset = 0;
    protected int yOffset = 0;
	
	boolean running = false;
	boolean useProcessing = false;
	int frameCount = 0;
	boolean rendering = false;
	
	/** True if all the other clients are connected. */
    // FIXME Maybe this doesn't need to be public.
    public boolean allConnected = false;
    
    protected boolean messageAvailable;  // Is a message available?
    protected boolean intsAvailable;     // Is an int array available?
    protected boolean bytesAvailable;    // is a byte array available?
    protected String[] dataMessage;      // data that has come in
    protected int[] ints;                // ints that have come in
    protected byte[] bytes;              // bytes that have come in
    
    // 3D variables
    protected float fieldOfView = 30.0f;
    protected float cameraZ;

    /**
     * Builds a UDPClient using an INI file and a parent PApplet.
     * 
     * The parent PApplet must have a method called "frameEvent(UDPClient c)", 
     * which handles syncing up the frame rate on the multiple screens.  A 
     * typical implementation may look like this:
     *
     * public void frameEvent(UDPClient c) {
     *   if (!started) started = true;
     *   loop();
     * }
     * 
     * The "draw()" function will look something like this:
     * 
     * public void draw() {
     *   if (started) {
     *     client.placeScreen();
     *       
     *       // draw some stuff  
     *       background(255);
     *       ellipse(client.getMWidth() / 2, client.getMHeight() / 2, 50, 50);
     *      
     *     client.done();
     *   } 
     *
     *   noLoop();
     * }
     *
     * @param _fileString the path to the INI file 
     * @param _p the parent PApplet
     */
	public UDPClient(String _fileString, PApplet _p) {
		useProcessing = true;
		p5parent = _p;
		cameraZ = (p5parent.height/2.0f) / PApplet.tan(PConstants.PI * fieldOfView/360.0f);
		
		loadIniFile(_fileString);
		connect(hostName, serverPort, id);
		
		// look for a method called "frameEvent" in the parent PApplet, with one
        // argument of type SyncClient
        try {
            frameEventMethod = p5parent.getClass().getMethod("frameEvent",
                    new Class[] { UDPClient.class });
        } catch (Exception e) {
            System.out.println("You are missing the frameEvent() method. " + e);
        }
	}

	/**
     * Builds a UDPClient using an INI file and a parent MpeDataListener.
     * 
     * The parent MpeDataListener must have a method called 
     * "frameEvent(UDPClient c)", which handles syncing up the frame rate on the
     * multiple screens.  A typical implementation may look like this:
     *
     * public void frameEvent(UDPClient c) {
     *   if (!started) started = true;
     *   loop();
     * }
     *
     * @param _fileString the path to the INI file 
     * @param _p the parent MpeDataListener
     */
	public UDPClient(String _fileString, MpeDataListener _p) {
		parent = _p;
		loadIniFile(_fileString);
		connect(hostName, serverPort, id);
	}

	/**
	 * Loads the settings from the client INI file.
	 * 
	 * @param _fileString the path to the INI file 
	 */
	private void loadIniFile(String _fileString){
		fp = new FileParser(_fileString);
		
		if (fp.fileExists()) {
            // parse INI file
            setServer(fp.getStringValue("server"));
            setPort(fp.getIntValue("port"));
            setID(fp.getIntValue("id"));
            
            int[] localDim = fp.getIntValues("localScreenSize");
            setLocalDimensions(localDim[0], localDim[1]);
            
            int[] offsets = fp.getIntValues("localLocation");
            setOffsets(offsets[0], offsets[1]); 
            
            int[] masterDims = fp.getIntValues("masterDimensions");
            setMasterDimensions(masterDims[0], masterDims[1]);
            
            out("Settings: server = " + hostName + ":" 
                    + serverPort + ",  id = " + id
                    + ", local dimensions = " + lWidth + ", " + lHeight
                    + ", location = " + xOffset + ", " + yOffset);
            
            if (fp.getIntValue("debug") == 1) DEBUG = true;
		}
	}
	
	/**
     * Connects to the server.
     * 
     * @param _hostName the server host name
     * @param _serverPort the server port
     * @param _id the client id
     */
	private void connect(String _hostName, int _serverPort, int _id) {
	    // set the server address and port
        setServer(_hostName);
        setPort(_serverPort);
        setID(_id);
	}
	
	/**
     * Sets the server address.
     * 
     * @param _hostName the server host name
     */
    protected void setServer(String _hostName) {
        if (_hostName != null) {
            hostName = _hostName;
            
            try {
                address = InetAddress.getByName(hostName);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
    
    /** @return the server address */
    public InetAddress getAddress() { return address; }
    
    /**
     * Sets the server port.
     * 
     * @param _serverPort the server port
     */
    protected void setPort(int _serverPort) {
        if (_serverPort > -1)
            serverPort = _serverPort;
    }
    
    /** @return the server port */
    public int getPort() { return serverPort; }
    
    /**
     * Sets the client ID and port.
     * 
     * @param _id the client id
     */
    protected void setID(int _id) {
        if (_id > -1) {
            id = _id;
            clientPort = serverPort + 1 + id;
        }
    }
    
    /** @return the client ID */
    public int getID() { return id; }

    /**
     * Sets the dimensions for the local display.
     * 
     * @param _lWidth The local width
     * @param _lHeight The local height
     */
    protected void setLocalDimensions(int _lWidth, int _lHeight) {
        if (_lWidth > -1 && _lHeight > -1) {
            lWidth = _lWidth;
            lHeight = _lHeight;
        }
    }
    
    /**
     * Sets the offsets for the local display.
     * 
     * @param _xOffset Offsets the display along x axis
     * @param _yOffset Offsets the display along y axis
     */
    protected void setOffsets(int _xOffset, int _yOffset) {
        if (_xOffset > -1 && _yOffset > -1) {
            xOffset = _xOffset;
            yOffset = _yOffset;
        }
    }
    
    /**
     * Sets the dimensions for the local display.
     * The offsets are used to determine what part of the Master Dimensions to render.
     * For example, if you have two screens, each 100x100, and the master dimensions are 200x100
     * then you would set
     *  client 0: setLocalDimensions(0, 0, 100, 100);
     *  client 1: setLocalDimensions(100, 0, 100, 100);
     * for a 10 pixel overlap you would do:
     *  client 0: setLocalDimensions(0, 0, 110, 100);
     *  client 1: setLocalDimensions(90, 0, 110, 100);
     * 
     * @param _xOffset Offsets the display along x axis
     * @param _yOffset Offsets the display along y axis
     * @param _lWidth The local width
     * @param _lHeight The local height
     */
    public void setLocalDimensions(int _xOffset, int _yOffset, int _lWidth, int _lHeight) {
        setOffsets(_xOffset, _yOffset);
        setLocalDimensions(_lWidth, _lHeight);
    }
    
    /**
     * Sets the master dimensions for the Video Wall. This is used to calculate
     * what is rendered.
     * 
     * @param _mWidth The master width
     * @param _mHeight he master height
     */
    public void setMasterDimensions(int _mWidth, int _mHeight) {
        if (_mWidth > -1 && _mHeight > -1) {
            mWidth = _mWidth;
            mHeight = _mHeight;
        }
    }
    
    /** @return the local width in pixels */
    public int getLWidth() { return lWidth; }

    /** @return the local height in pixels */
    public int getLHeight() { return lHeight; }

    /** @return the x-offset of frame in pixels */
    public int getXoffset() { return xOffset; }
    
    /** @return the y-offset of frame in pixels */
    public int getYoffset() { return yOffset; }

    /** @return the master width in pixels */
    public int getMWidth() { return mWidth; }

    /** @return the master height in pixels */
    public int getMHeight() { return mHeight; }
    
    /** @return the total number of frames rendered */  
    public int getFrameCount() { return frameCount; }
    
    /**
     * Sets the field of view of the camera when rendering in 3D.
     * Note that this has no effect when rendering in 2D.
     * 
     * @param val the value of the field of view
     */
    public void setFieldOfView(float val) {
        fieldOfView = val;
        
        if (p5parent != null) {
            cameraZ = (p5parent.height/2.0f) / PApplet.tan(PConstants.PI * fieldOfView/360.0f);

            if (!(p5parent.g instanceof PGraphics3D)) {
                out("MPE Warning: Rendering in 2D! fieldOfView has no effect!");
            }
        } else {
            out("MPE Warning: Not using Processing! fieldOfView has no effect!");
        }
    }
    
    /** @return the value of the field of view */
    public float getFieldOfView() { return fieldOfView; }

	/**
	 * Places the viewing area for this screen. This must be called at the 
	 * beginning of the render loop.  If you are using Processing, you would 
	 * typically place it at the beginning of your draw() function.
	 */
	public void placeScreen() {
	    if (p5parent.g instanceof PGraphics3D) {
	        placeScreen3D();
	    } else {
	        placeScreen2D();
	    }
	}
	
	/**
	 * Places the viewing area for this screen when rendering in 2D.
	 */
	public void placeScreen2D() {
	    p5parent.translate(xOffset * -1, yOffset * -1);
    }
	
	/**
     * Places the viewing area for this screen when rendering in 3D.
     */
	public void placeScreen3D() {
	    p5parent.camera(mWidth/2.0f, mHeight/2.0f, cameraZ,
                        mWidth/2.0f, mHeight/2.0f, 0, 
                        0, 1, 0);


	    // The frustum defines the 3D clipping plane for each Client window!
	    float mod = 1f/10f;
	    float left   = (xOffset - mWidth/2)*mod;
	    float right  = (xOffset + lWidth - mWidth/2)*mod;
	    float top    = (yOffset - mHeight/2)*mod;
	    float bottom = (yOffset + lHeight-mHeight/2)*mod;
	    float near   = cameraZ*mod;
	    float far    = 10000;
	    p5parent.frustum(left,right,top,bottom,near,far);
    }
	
	/**
     * Restores the viewing area for this screen when rendering in 3D.
     */
    public void restoreCamera() {
        p5parent.camera(p5parent.width/2.0f, p5parent.height/2.0f, cameraZ,
                        p5parent.width/2.0f, p5parent.height/2.0f, 0, 
                        0, 1, 0);
        
        float mod = 1/10.0f;
        p5parent.frustum(-(p5parent.width/2)*mod, (p5parent.width/2)*mod,
                         -(p5parent.height/2)*mod, (p5parent.height/2)*mod,
                         cameraZ*mod, 10000);
    }
    
	/**
     * Outputs a message to the console.
     * 
     * @param _str the message to output.
     */
    private void out(String _str) {
        print(_str);
    }
    
    /**
     * Outputs a message to the console.
     * 
     * @param _str the message to output.
     */
    private void print(String _str) {
        System.out.println("Client: " + _str);
    }
    
    /**
     * Outputs an error message to the console.
     * 
     * @param _str the message to output.
     */
    private void err(String _str) {
        System.err.println("Client: " + _str);
    }
    
    /**
     * This method must be called when the client PApplet starts up. It will 
     * tell the server it is ready.
     */
    public void start() {
        try {
            socket = new DatagramSocket(clientPort);
            // start the broadcasting thread
            bdt = new BroadcastDoneThread(this);
            bdt.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
        super.start();
    }
    
    /**
     * This method should only be called internally by Thread.start().
     */
    public void run() {
        if (DEBUG) out("Running!");
           
        try {
            while (running) {
                // read UDP packet
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);

                String msg = new String(data, 0, packet.getLength());

                if (msg == null) {
                    //running = false;
                    break;
                } else {
                    read(msg);
                }

                // FIXME Do we need this sleep or are we just slowing ourselves 
                // down for no reason??
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
     * Reads and parses a message from the server.
     * 
     * @param _serverInput the server message
     */
    private synchronized void read(String _serverInput) {
        if (DEBUG) System.out.println("Receiving: " + _serverInput);
       
        // FIXME This is a hack for now. It will block only once but will allow
        // everything to start at once.
        if (_serverInput.startsWith("M") && mWidth == -1) {
            _serverInput = _serverInput.substring(1);
            String[] mdim = _serverInput.split(",");
            mWidth = Integer.parseInt(mdim[0]);
            mHeight = Integer.parseInt(mdim[1]);
        }
        
        // a "G" startbyte will trigger a frameEvent
        char c = _serverInput.charAt(0);
        if (c == 'G') {
            if (!allConnected) {
                if (DEBUG) print("all connected!");
                allConnected = true;
                bdt.sendingStart = false;
            }
            // split into frame message and data message
            String[] info = _serverInput.split(":");
            String[] frameMessage = info[0].split(",");
            int fc = Integer.parseInt(frameMessage[1]);

            if (info.length > 1) {
                // there is a message here with the frameEvent
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

            if (fc == frameCount && !rendering) {
                rendering = true;
                bdt.sendingDone = false;
                frameCount++;
            
                if (useProcessing && frameEventMethod != null){
                    try {
                        // call the method with this object as the argument!
                        frameEventMethod.invoke(p5parent, new Object[] { this });
                    
                    } catch (Exception e) {
                        err("Could not invoke the \"frameEvent()\" method for some reason.");
                        e.printStackTrace();
                        frameEventMethod = null;
                    }
                } else {
                    parent.frameEvent(this);
                }

            } else {
                if (DEBUG) print("Extra message, frameCount: " + frameCount 
                        + " received from server: " + fc);
            }
        }
    }

    /**
     * Send a message to the server using UDP.
     * 
     * @param _msg the message to send
     */
    public void send(String _msg) {
        if (DEBUG) out("Sending: " + _msg);
        
        try {
            byte[] data = _msg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, serverPort);
            packet.setData(data);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Format a broadcast message and send it.
     * Do not use a colon ':' in your message!!!
     * 
     * @param _msg the message to broadcast
     */
    public void broadcast(String _msg) {
        // prepend the message with a "T"
        _msg = "T"+ _msg;
        send(_msg);
    }

    /**
     * Broadcasts a byte array to all screens.
     * Large arrays could cause performance issues depending on network speed.
     * 
     * @param data the array to broadcast
     */
    /*public void broadcastByteArray(byte[] data) {
        broadcastByteArray(data,data.length);
    }*/

    /**
     * Broadcasts a byte array to all screens.
     * Large arrays could cause performance issues depending on network speed.
     * 
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
     * Broadcasts an array of ints to all screens. Large arrays can cause 
     * performance problems.
     * 
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
     * Returns true of false based on whether a String message is available from
     *  the server.
     * This should be used inside "frameEvent()" since messages are tied to 
     * specific frames.
     * 
     * @return true if a new String message is available
     */
    public boolean messageAvailable() {
        return messageAvailable;
    }

    /**
     * Returns an array of messages from the server.
     * This should be used inside "frameEvent()" since messages are tied to 
     * specific frames. It also should only be called after checking that 
     * {@link #messageAvailable()} returns true.
     * 
     * @return an array of messages from the server
     */
    public String[] getDataMessage() {
        return dataMessage;
    }

    /**
     * Returns true of false based on whether integer data is available from the
     *  server.
     * This should be used inside "frameEvent()" since messages are tied to 
     * specific frames.
     *
     * @return true if an array of integers is available
     */    
    public boolean intsAvailable() {
        return intsAvailable;
    }
    
    /**
     * Returns an array of integers from the server.
     * This should be used inside "frameEvent()" since messages are tied to 
     * specific frames. It also should only be called after checking that 
     * {@link #intsAvailable()} returns true.
     * 
     * @return an array of ints from the server
     */  
    public int[] getInts() {
        return ints;
    }

    /**
     * Returns true of false based on whether byte data is available from the
     *  server.
     * This should be used inside "frameEvent()" since messages are tied to 
     * specific frames.
     *
     * @return true if an array of bytes is available
     */    
    public boolean bytesAvailable() {
        return bytesAvailable;
    }

    /**
     * Returns an array of bytes from the server.
     * This should be used inside "frameEvent()" since messages are tied to 
     * specific frames. It also should only be called after checking that 
     * {@link #bytesAvailable()} returns true.
     * 
     * @return an array of bytes from the server
     */   
    public byte[] getBytes() {
        return bytes;
    }   
    
    /**
     * Sends a "Done" command to the server. This must be called at the end of 
     * the draw loop.
     */
    public void done() {
        bdt.sendingDone = true;
        bdt.interrupt();
        rendering = false;
    }

    /**
     * Stops the client thread. You don't really need to do this ever.
     */  
    public void quit() {
        out("Quitting.");
        running = false;  // Setting running to false ends the loop in run()
        interrupt();      // In case the thread is waiting. . .
    }

}