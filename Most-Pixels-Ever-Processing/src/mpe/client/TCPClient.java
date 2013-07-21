/**
 * The MPE Client
 * The Client class registers itself with a server
 * and receives messages related to frame rendering and data input
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 */

package mpe.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.data.XML;
import processing.opengl.PGraphics3D;

public class TCPClient extends Thread {
	/** If DEBUG is true, the client will print lots of messages about what it is doing.
	 * Set with debug=1; in your INI file. */
	public static boolean VERBOSE = false;

	boolean asynchronous = false;
	boolean asynchreceive = false;

	boolean offsetWindow = false;

	// TCP stuff
	String hostName;
	int serverPort = 9002;
	Socket socket;
	InputStream is;
	BufferedReader brin;
	DataOutputStream dos;
	OutputStream os;

	PApplet p5parent;
	Method frameEventMethod;
	Method resetEventMethod;

	/** The id is used for communication with the server, to let it know which 
	 *  client is speaking and how to order the screens. */
	int id = 0;

	/** The master width. */
	protected int mWidth = -1;
	/** The master height. */
	protected int mHeight = -1;

	/** The local width. */
	protected int lWidth = 640;
	/** The local height. */
	protected int lHeight = 480;

	int xOffset = 0;
	int yOffset = 0;

	boolean running = false;
	boolean rendering = false;
	boolean autoMode = false;

	int   frameCount = 0;
	float fps = 0.f;
	long  lastMs = 0;

	boolean reset = false;

	// Are we broadcasting?
	// boolean broadcastingData = false;
	// Do we need to wait to broadcast b/c we have alreaddy done so this frame?
	// boolean waitToSend = false;
	// protected boolean sayDoneAgain = false;  // Do we need to say we're done after a lot of data has been sent

	/** True if all the other clients are connected. */
	// FIXME Maybe this doesn't need to be public.
	// public boolean allConnected = false;

	protected boolean messageAvailable;      // Is a message available?
	protected String[] dataMessage;          // data that has come in

	// 3D variables
	protected boolean enable3D = false;
	protected float fieldOfView = 30.0f;
	protected float cameraZ;

	/**
	 * Client is constructed with an init file location, and the parent PApplet.
	 * The parent PApplet must have a method called "frameEvent(Client c)".
	 *
	 * The frameEvent handles syncing up the frame rate on the
	 * multiple screens.  A typical implementation may look like this:
	 *
	 *  public void frameEvent(Client c){
	 *  if (!started) started = true;
	 *    redraw();
	 *  }
	 *
	 */
	public TCPClient(String _fileString, PApplet _p) {
		this(_fileString, _p, true);
	}

	public TCPClient(String _fileString, PApplet _p, boolean _autoMode) {
		p5parent = _p;

		// Autodetecting if we should use 3D or not
		enable3D = p5parent.g instanceof PGraphics3D;

		autoMode = _autoMode;
		cameraZ = (p5parent.height/2.0f) / PApplet.tan(PConstants.PI * fieldOfView/360.0f);

		loadSettings(_fileString);
		connect(hostName, serverPort, id);

		// look for a method called "frameEvent" in the parent PApplet, with one
		// argument of type Client
		try {
			frameEventMethod = p5parent.getClass().getMethod("frameEvent",
					new Class[] { TCPClient.class });
		} catch (Exception e) {
			System.out.println("You are missing the frameEvent() method. " + e);
		}

		if (autoMode) {
			p5parent.registerMethod("draw", this);
		}

	}

	/**
	 * Called automatically by PApplet.draw() when using auto mode.
	 */
	public void draw() {
		if (offsetWindow) {
			p5parent.frame.setLocation(id*lWidth,0);
		}
		if (running && rendering) {
			placeScreen();

			if (reset) {
				try {
					resetEventMethod.invoke(p5parent, new Object[] { this });
				} catch (Exception e) {
					err("Could not invoke the \"resetEvent()\" method for some reason.");
					e.printStackTrace();
					resetEventMethod = null;
				}
			} else if (frameEventMethod != null) {
				try {
					// call the method with this object as the argument!
					frameEventMethod.invoke(p5parent, new Object[] { this });
				} catch (Exception e) {
					err("Could not invoke the \"frameEvent()\" method for some reason.");
					e.printStackTrace();
					frameEventMethod = null;
				}
			}
			if (!asynchronous) {
				done();
			}
		}
	}

	/**
	 * Loads the settings from the Client XML file.
	 * 
	 * @param filename the path to the XML file 
	 */
	private void loadSettings(String filename) {
		// parse XML file
		XML xml = p5parent.loadXML(filename);
		setServer(xml.getChild("server/ip").getContent());
		setPort(xml.getChild("server/port").getIntContent());
		setID(xml.getChild("id").getIntContent());

		XML asynch = xml.getChild("asynchronous");
		if (asynch != null) {
			String a = asynch.getContent();
			asynchronous = Boolean.parseBoolean(a);
			if (asynchronous) {
				XML receive = xml.getChild("asynchreceive");
				if (receive != null) {
					String r = receive.getContent();
					asynchreceive = Boolean.parseBoolean(r);
				}
			}
		}

		String v = xml.getChild("verbose").getContent();
		if (v.equals(true)) VERBOSE = true;
		else VERBOSE = false;

		// Implement name
		if (!asynchronous) {
			int w = xml.getChild("local_dimensions/width").getIntContent();
			int h = xml.getChild("local_dimensions/height").getIntContent();
			setLocalDimensions(w,h);

			int x = xml.getChild("local_location/x").getIntContent();
			int y = xml.getChild("local_location/y").getIntContent();
			setOffsets(x,y);

			int mw = xml.getChild("master_dimensions/width").getIntContent();
			int mh = xml.getChild("master_dimensions/height").getIntContent();
			setLocalDimensions(w,h);

			XML offset = xml.getChild("offset_window");
			if (offset != null) {
				offsetWindow = Boolean.parseBoolean(offset.getContent());
			}

			setMasterDimensions(mw,mh);

			out("Settings: server = " + hostName + ":" + serverPort + ",  id = " + id
					+ ", local dimensions = " + lWidth + ", " + lHeight
					+ ", location = " + xOffset + ", " + yOffset);
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
		if (_hostName != null)
			hostName = _hostName;
	}

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
	 * Sets the client ID.
	 * 
	 * @param _id the client id
	 */
	protected void setID(int _id) {
		if (_id > -1)
			id = _id;
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

	/** @return the client framerate */  
	public float getFPS() { return fps; }

	/** @return whether or not the client is rendering */  
	public boolean isRendering() { return rendering; }

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
		if (enable3D) {
			placeScreen3D();
		} else {
			placeScreen2D();
		}
	}

	/**
	 * If you want to enable or disable 3D manually in automode
	 */
	public void enable3D(boolean b) {
		enable3D = b;
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
	 * Checks whether the given point is on screen.
	 */
	public boolean isOnScreen(float x, float y) {
		return (x > xOffset && 
				x < (xOffset + lWidth) && 
				y > yOffset &&
				y < (yOffset + lHeight));
	}

	/**
	 * Checks whether the given rectangle is on screen.
	 */
	public boolean isOnScreen(float x, float y, float w, float h) {
		return (isOnScreen(x, y) || 
				isOnScreen(x + w, y) ||
				isOnScreen(x + w, y + h) ||
				isOnScreen(x, y + h));
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
			socket = new Socket(hostName, serverPort);
			is = socket.getInputStream();
			brin = new BufferedReader(new InputStreamReader(is));
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
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
		if (VERBOSE) out("Running!");

		// let the server know that this client is ready to start.
		send("S|" + id);

		try {
			while (running) {
				// read packet
				String msg = brin.readLine();//dis.readUTF();
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
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads and parses a message from the server.
	 * 
	 * @param _serverInput the server message
	 */
	// TODO UNSYNCHRONIZE!
	private void read(String msg) {
		if (VERBOSE) out("Receiving: " + msg);

		// a "G" startbyte will trigger a frameEvent.
		char c = msg.charAt(0);
		if (c == 'G' || c == 'R') {

			if (c == 'R') reset = true;

			String[] tokens = msg.split("\\|");
			int fc = Integer.parseInt(tokens[1]);
			if (tokens.length > 2) {
				// there is a message here with the frameEvent 
				String[] dataInfo = new String[tokens.length-2];
				for (int k = 0; k < dataInfo.length; k++){
					// Grabbing the message as everything after first comma
					// TODO offer ID
					int comma = tokens[k+2].indexOf(",");
					dataInfo[k] = tokens[k+2].substring(comma+1,tokens[k+2].length());
				}
				dataMessage = null;  // clear
				dataMessage = dataInfo;
				messageAvailable = true;
			} else {
				messageAvailable = false;
			}

			if (reset) {
				frameCount = 0;
			}

			if (fc == frameCount) {
				rendering = true;
				// calculate new framerate
				float ms = System.currentTimeMillis() - lastMs;
				fps = 1000.f / ms;
				lastMs = System.currentTimeMillis();
			} else {
				if (VERBOSE) print("Extra message, frameCount: " + frameCount 
						+ " received from server: " + fc);
			}
		}
	}

	/**
	 * Send a message to the server using UDP.
	 * 
	 * @param _msg the message to send
	 */
	// TODO UNSYNCHRONIZE!
	private void send(String _msg) {
		if (VERBOSE) out("Sending: " + _msg);
		_msg += "\n";
		try {
			dos.write(_msg.getBytes());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Format a broadcast message and send it.
	 * Do not use a colon '|' in your message!!!
	 * 
	 * @param _msg the message to broadcast
	 */
	public void broadcast(String _msg) {
		// prepend the message with a "T"
		_msg = "T|"+ _msg;
		send(_msg);
	}


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
	 * Sends a "Done" command to the server. This must be called at the end of 
	 * the draw loop.
	 */
	public void done() {
		rendering = false;
		reset = false;
		String msg = "D|" + id + "|" + frameCount;
		send(msg);
		frameCount++;
	}

	/**
	 * Stops the client thread.  You don't really need to do this ever.
	 */  
	public void quit() {
		out("Quitting.");
		running = false;  // Setting running to false ends the loop in run()
		interrupt();      // In case the thread is waiting. . .
	}

}