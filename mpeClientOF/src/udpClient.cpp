

#include "udpClient.h"
#include "testApp.h"
#include <stdio.h>
#include <stdlib.h>

/**
 * The MPE Client
 * The Client class registers itself with a server
 * and receives messages related to frame rendering and data input
 * <http://mostpixelsever.com>
 * @author Shiffman and Kairalla
 * @author openFrameworks port by Rotsztain
 */
 
udpClient::udpClient(){

	repeatTime = 50;

	//serverPort = 9002;
	//clientPort;
	data = new char[65535];

	// this is used for communication to let the server know which client is speaking and how to order the screens
	clientID = 0; 
	numScreens; //the total number of screens
	
	// screen parameters
	mWidth = -1; //master width
	mHeight = -1; //master height
	lWidth = 640; //local width
	lHeight = 480; //local height
	xOffset = 0;
	yOffset = 0;
	bIsDone = false; //flipped to true when we're done rendering.
	
	//public boolean moveOn = false; // tells parent to loop back
	fps = -1;
	bIsRunning = false;
	frameCount = 0;
	rendering = false;
	bIsConnected = false;
	bDrawNewFrame = false;

	// Are we broadcasting?
	broadcastingData = false;
	
	// Do we need to wait to broadcast b/c we have alreaddy done so this frame?
	waitToSend = false;

	// what's come in?
	bMessageAvailable = false;  // Is a message available?
	bImageAvailable=false; // Is an image available?
	bIntsAvailable=false;     // Is an int array available?
	bBytesAvailable=false;    // is a byte array avaialble?
	bSayDoneAgain = false;  // Do we need to say we're done after a lot of data has been sen
	messageCount = 0;

	//If DEBUG is true, the client will print lots of messages about what it is doing.
	// Set with debug=1; in your INI file.
	DEBUG = false;

	// True if all the other clients are connected.  Maybe this doesn't need to be public.
	allConnected = false;
	
	// initialize udp 
	udp.Create();
	
	// set to non blocking (threaded)
	int setBlocking = udp.SetNonBlocking(true);
	if(!setBlocking) printf("failed to change blocking state\n");

}

// ---------------------------------------------------------------------

void udpClient::setup(string iniFileURL){

	loadIniFile(iniFileURL);
	
	ofSetWindowShape(lWidth, lHeight);
	
	
}

// ---------------------------------------------------------------------

void udpClient::setParent(testApp & parent_){

	parent = &parent_;
}

// ---------------------------------------------------------------------

void udpClient::update(){
	
	if(bIsConnected){
	
		// send 'S' to server
		// set the destination for the messages
		//inet_aton(ipNum.c_str(), &udp.saClient.sin_addr);
		//udp.saClient.sin_port = htons(portNum); //portNum;
		
		
		
		string serverInput;
        bool bKeepReading = true;
        bool bDidWeReceiveOnce = false;
		
		// keep reading until nothing left
		
        while(bKeepReading){
			
            bKeepReading = read(serverInput);
			
            if (bKeepReading == true){
				
				bDidWeReceiveOnce = true;
            }
		}
		
		// NOTE: this process only takes the last message
		// this should be changed!
		
        if(bDidWeReceiveOnce){
			
			if (DEBUG) printf("Receiving: %s\n",  serverInput.c_str());
			
			// This is a hack for now.  this will block only once but it will allow
			// everything to start at once.
			
			if (serverInput.at(0) == 'M' && mWidth == -1) {
				serverInput = serverInput.substr(1);
				string * mdim;
				int stringLength;
				splitString(serverInput, ',', mdim, stringLength);
				//mWidth = atoi(mdim[0].c_str());
				//mHeight = atoi(mdim[1].c_str());
			}
			
			//A "G" startbyte will trigger a frameEvent.
			//If it's a B, we also have to get a byteArray
			//An I for int array
			
			char c = serverInput.at(0);
			
			if (c == 'G' || c == 'B' || c == 'I') {
			
				if (!allConnected) {
					if (DEBUG) print("all connected!\n");
					allConnected = true;
				}
				
				// Split into frame message and data message
				string * info;
				int infoLength;
				splitString(serverInput, ':', info, infoLength); // revise to only split by first colon
				
				string * frameInfo;
				int frameInfoLength;
				splitString(info[0], ',', frameInfo, frameInfoLength);
				int fc = atoi(frameInfo[1].c_str());

								
				
				// There is a message here with the frameEvent
				if (infoLength > 1) {
					
					// what is the first character here?
					// NOTE: this could be problematic is the first string message is P
					// NOTE: perhaps messages should start with M
					
					
					
					if (info[1].at(0) == 'P') {
					
						// if there's an image (NOTE: not working)
						
						int colonPos = serverInput.find_first_of(":");
						imgMessage = serverInput.substr(colonPos+1, serverInput.size()-colonPos-1);
						bImageAvailable = true;					
						
						//printf("dataMessage %s\n", dataMessage.c_str());
						
					} else {
						
						free(dataMessage);
						
						// create data message array
						splitString(info[1], ',', dataMessage, messageCount);
						
						bMessageAvailable = true;
						
					}
					
				} else {
				
					bImageAvailable = false;
					bMessageAvailable = false;
				}
				
				//messageAvailable();
				
				//imageAvailable();
				
				// compare frame count (if the same, draw new frame)
				if(frameCount == fc) bDrawNewFrame = true;
				
				frameCount++;
				
				
			}
		}
		
		// send heartbeat
		
		char heartbeat[11];
		sprintf(heartbeat, "H,%i,%i", clientID, frameCount);
		
		send(heartbeat);
		
	} else {
	
		// wait 2 seconds 
		sleep(2);
		
		start();
	}
}


// ---------------------------------------------------------------------

// Loads the Settings from the Client INI file
 
void udpClient::loadIniFile(string fileString){


	//--------------------------------------------- get configs
	
    ofxXmlSettings xmlReader;
	
	bool result = xmlReader.loadFile(fileString);
	
	if(!result) printf("error loading xml file\n");
	
	clientID = xmlReader.getValue("settings:client_id", -1, 0);
	
	ipNum = xmlReader.getValue("settings:server:ip", "127.0.0.1", 0);
	
	portNum = xmlReader.getValue("settings:server:port", 7887, 0);

	setMasterDimensions(
		xmlReader.getValue("settings:master_dimensions:width", 640, 0), 
		xmlReader.getValue("settings:master_dimensions:height", 240, 0)
	);
	
	setLocalDimensions(
		xmlReader.getValue("settings:local_location:x", 0, 0),
		xmlReader.getValue("settings:local_location:y", 0, 0),
		xmlReader.getValue("settings:local_dimensions:width", 640, 0), 
		xmlReader.getValue("settings:local_dimensions:height", 240, 0)
	);
	
	if(xmlReader.getValue("settings:go_fullscreen", "false", 0).compare("true") == 0) {
		ofSetFullscreen(true);
	}
	
	// set the position based on the x/y offset
	if(xmlReader.getValue("settings:offset_window", "false", 0).compare("true") == 0) {
	
		ofSetWindowPosition(xOffset, yOffset);
	}
	
	if(xmlReader.getValue("settings:debug", 0, 0) == 1) DEBUG = true;

	if(DEBUG){
	    
		printf("\nMPE Client is in debug mode\n");
		printf("client id: %i\n", clientID);
		printf("server info: ip %s, port %i\n", ipNum.c_str(), portNum);
		printf("local dimensions: %i x %i\n", lWidth, lHeight);
		printf("local position: %i,%i\n", xOffset, yOffset);
		printf("local dimensions: %i x %i\n\n", mWidth, mHeight);
	}
}



// ---------------------------------------------------------------------

/**
 * Sets the master dimensions for the Video Wall. This is used to calculate
 * what is rendered.
 * 
 * @param _mWidth
 *            The master width
 * @param _mHeight
 *            The master height
 */
void udpClient::setMasterDimensions(int _mWidth, int _mHeight) {

	mWidth = _mWidth;
	mHeight = _mHeight;
}

// ---------------------------------------------------------------------

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
 
void udpClient::setLocalDimensions(int _xOffset, int _yOffset, int _lWidth, int _lHeight) {

	xOffset = _xOffset;
	yOffset = _yOffset;
	lWidth = _lWidth;
	lHeight = _lHeight;
}

// ---------------------------------------------------------------------

/**
 * returns local width.
 * @return local width in pixels
 */
int udpClient::getLWidth() {

	return lWidth;
}

// ---------------------------------------------------------------------

/**
 * returns local height.
 * @return local height in pixels
 */
int udpClient::getLHeight() {

	return lHeight;
}

// ---------------------------------------------------------------------

/**
 * returns master width.
 * @return master width in pixels
 */
int udpClient::getMWidth() {

	return mWidth;
}

// ---------------------------------------------------------------------

/**
 * returns x offset of frame.
 * @return x offset of frame.
 */
int udpClient::getXoffset() {

	return xOffset;
}

// ---------------------------------------------------------------------

/**
 * returns y offset of frame.
 * @return y offset of frame.
 */
int udpClient::getYoffset() {

	return yOffset;
}

// ---------------------------------------------------------------------

/**
 * returns master height.
 * @return master height in pixels
 */
int udpClient::getMHeight() {

	return mHeight;
}

// ---------------------------------------------------------------------

/**
 * Places the viewing area for this screen. This must be called at the beginning
 * of the render loop.  If you are using Processing, you would typicall place it at
 * the beginning of your draw() function.
 *
 */
void udpClient::placeScreen() {

	glTranslatef(xOffset * -1, yOffset * -1, 0);
}

// ---------------------------------------------------------------------

/**
 * Sends a "Done" command to the server. This must be called at the end of your draw loop.
 *
 */
void udpClient::done() {
	//System.out.println("Sending Done = true");
	//bdt.sendingDone = true;
	//bdt.interrupt();
	
	
	
	rendering = false;
	
	
	
	char clientIDString[5];
	char frameCountString[1000];
	
	sprintf(clientIDString, "%i", clientID);
	sprintf(frameCountString, "%i", frameCount);
	
	string msg = "D," + string(clientIDString) + "," + string(frameCountString);
	send(msg);

	/*if (broadcastingData) {
		sayDoneAgain = true;
	} else {
		
		done = true;
	}*/
}

// ---------------------------------------------------------------------

/**
 * Returns this screen's ID
 * @return screen's ID #
 */
int udpClient::getClientID() {

	return clientID;
}

// ---------------------------------------------------------------------

/**
 * Returns bool of data found
 * @return bool of data found
 */

bool udpClient::read(string & serverInput) {

	bool bDidWeGetAnything = false;

	char pBuff[1024];

	int res = udp.Receive(pBuff, 1024);

	if (res > 0){
	
		string rec;
	
		rec.assign(pBuff);
		
		if(rec.size()>0){
		
			serverInput = rec;
			
			bDidWeGetAnything = true;
		}
	}

	return bDidWeGetAnything;
}

// ---------------------------------------------------------------------

/**
 * This method should only be called internally by Thread.start().
 */

void udpClient::run() {

	if (DEBUG) print("I'm running!");
	//let server that this client is ready to start.
	
	// send the message
	send("S" + clientID);
	
	//done();
	try {
		while (bIsRunning) {
		
			/*

			// NOTE: Read UDP
			DatagramPacket packet = new DatagramPacket(data,data.length);
			socket.receive(packet);

			string msg = new string(data,0,packet.getLength());

			if (msg.compare("") == 0) {
				//running = false;
				break;
			} else {
			
				printf("mess %s", msg.c_str());
				read(msg);
			}

			// Do we need this sleep or are we just slowing
			// ourselves down for no reason??
			try {
				// NOTE: commented out
				// Thread.sleep(5);
			} catch (MPEError e) {
				
				e.print();
			}
			
			*/

		}

	} catch (MPEError e) {
		e.print();
	}
}

// ---------------------------------------------------------------------

/**
 * This method must be called when the client applet starts up.
 * It will tell the server it is ready.
 */
 
void udpClient::start() {

	try {
	
		// --------------------- client binds

		char * ipStr = (char *) ipNum.c_str();
		
		//bIsConnected = udp.BindMcast(ipStr, portNum+1);
		bIsConnected = udp.Connect(ipStr, portNum);
		//bIsConnected = udp.Bind(portNum+1+clientID);
		
		if (bIsConnected) {
		
			if(DEBUG) printf("UDP connection bound (port %i)\n\n", portNum+1+clientID);
			
			bIsRunning = true;
			
			char initMessage[30];
			sprintf(initMessage, "S%i", clientID);
			
			//printf("%s\n", initMessage);
			
			send(initMessage); 
		
		} else { 
		
			printf("Error: UDP failed to connect to port %i\n\n", portNum);
		}
		

	} catch (MPEError e) {
	
		e.print();
	}
	
	
	
	//NOTE super.start();
}

// ---------------------------------------------------------------------

// UNSYNCHRONIZE!
void udpClient::send(string msg) {
	
	try {

		// copy the contents of the string to a byte array
		strcpy(updPacketStr, msg.c_str());
		
		if (DEBUG) printf("sending %s\n", updPacketStr);
	
		// send the char array
		int res = udp.Send(updPacketStr, msg.size());
		
		if(res <= 0) printf("Error sending packet\n");
		
	} catch (MPEError e) {
	
		e.print();
	}
}

// ---------------------------------------------------------------------

/**
 * broadcasts a string to all screens
 * Do not use a colon (':') in your message!!!
 * @param msg
 */
void udpClient::broadcast(string msg) {

	// add a T to send message
	msg = "T"+ msg;
	send(msg);
}

// ---------------------------------------------------------------------

/**
 * broadcasts an image to all screens
 * uses PImage
 * @param msg
 */
void udpClient::broadcastImage(ofImage img) {

	/*
	
	NOTE
	
	int nPixels = img.width * img.height;
	int imgFormat = img.format;
	
	int pixelFormat;
	
	if(imgFormat==PApplet.RGB) {
		pixelFormat = 3;
	} else if(imgFormat==PApplet.ARGB){
		pixelFormat = 4;
	} else {
		pixelFormat = 1;
	}
	
	// create byte array for image
	// save 4 bytes for width, 4 bytes for height, 1 byte for color type
	char * intByte = new byte[nPixels * pixelFormat + 9];

	int r, g, b, a;
	
	// get image pixels
	// if RGB
	
	if(imgFormat == 1){
	
		for(int i=0; i<nPixels; i++){
			
			int c = img.pixels[i];
			r = (int)p5parent.red(c);
			g = (int)p5parent.green(c);
			b = (int)p5parent.blue(c);
			
			int pos = i*3+9;
			
			intByte[pos] = (byte)(r >> 0);
			intByte[pos+1] = (byte)(g >> 0);
			intByte[pos+2] = (byte)(b >> 0);
		}
	
	} else if(imgFormat == 2){
		
		for(int i=0; i<nPixels; i++){
			
			int c = img.pixels[i];
			r = (int)p5parent.red(c);
			g = (int)p5parent.green(c);
			b = (int)p5parent.blue(c);
			a = (int)p5parent.alpha(c);
			
			int pos = i*4+9;
			
			intByte[pos] = (byte)(r >> 0);
			intByte[pos+1] = (byte)(g >> 0);
			intByte[pos+2] = (byte)(b >> 0);
			intByte[pos+3] = (byte)(a >> 0);
		}
	}
	
	// add width & height to byte array
	int w = img.width;
	int h = img.height;
	
	for(int i=0; i<4; i++){
		
		intByte[i] = (byte)(w >> (8 * i));
		intByte[i+4] = (byte)(h >> (8 * i));
	}
	
	intByte[8] = (byte)(imgFormat >> 0);
	
	// convert byte array to string
	string imageAsString = new String(intByte);
	string message = "P";
	
	// add P for image and send
	message += imageAsString;
	//System.out.println("Sending: " + message);
	send(message);
	
	*/
}

// ---------------------------------------------------------------------

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
/*
public synchronized void broadcastIntArray(int[] intArray) {
	// We won't send an int array more than
	// once during any given "frame"
	try {
		byte[] data = intArray.getBytes();
		DatagramPacket packet = new DatagramPacket(data,data.length,address,serverPort);
		packet.setData(data);
		socket.send(packet);
	} catch (IOException e) {
		e.printStackTrace();
	}
}
*/

// ---------------------------------------------------------------------

/**
 * Stops the client thread.  You don't really need to do this ever.
 */  
void udpClient::quit() {
	printf("Quitting.");
	bIsRunning = false; // Setting running to false ends the loop in run()
	//interrupt(); // In case the thread is waiting. . .
}

// ---------------------------------------------------------------------

void udpClient::print(string string) {

	printf("MPE CLIENT: %s",  string.c_str());
}

// ---------------------------------------------------------------------

void udpClient::setServer(string _server) {

	if (_server.compare("") != 0) host = _server;
}

// ---------------------------------------------------------------------

void udpClient::setLocalDimensions(int w, int h) {

	if (w > -1 && h > -1) {
		lWidth = w;
		lHeight = h;
	}
}

// ---------------------------------------------------------------------

void udpClient::setOffsets(int w, int h) {

	if (w > -1 && h > -1) {
		xOffset = w;
		yOffset = h;
	}
}

// ---------------------------------------------------------------------

void udpClient::setID(int _ID) {

	if (_ID > -1) clientID = _ID;
}

// ---------------------------------------------------------------------

void udpClient::setPort(int _port) {
	
	if (_port > -1) portNum = _port;
}

// ---------------------------------------------------------------------

/**
 * Returns true of false based on whether a String message is available
 * This should be used inside frameEvent() since messages are tied to specific frames
 * @return true if new String message 
 */
bool udpClient::imageAvailable() {

	return bImageAvailable;
}

// ---------------------------------------------------------------------

/**
 * This should be used inside frameEvent() since messages are tied to specific frames
 * Only should be called after checking that {@link #messageAvailable()}  returns true
 * @return an array of messages from the server
 */
ofImage udpClient::getImage() {
	
	// NOTE: this is just a shot in the dark!
	// Q: why is there an array of messages?
	string imageAsString = imgMessage; //"";//dataMessage[0];
	
	/*
	for(int i=0; i<dataMessage.length; i++){
		//System.out.println(dataMessage[i]);
		
		//imageAsString+=dataMessage[i];
	}
	*/
	
	/* NOTE
	
	
	char * newImageByteArray = imageAsString.getBytes();
	
	System.out.println("Receiving byte size "+newImageByteArray.length);
	
	// get image width & height
	int newImageW = 0;
	int newImageH = 0;
	int newImgFormat = 0;
	
	for(int i=0; i<4; i++){
		
		newImageH += newImageByteArray[i+4] << 8 * i;
		newImageW += newImageByteArray[i] << 8 * i;
	}
	newImgFormat += newImageByteArray[8] << 0;
	
	*/
	
	// create new image (OOPS)
	ofImage imageFromBytes;
	
	/*
	//imageFromBytes = p5parent.createImage(30, 30, 1);
	
	//System.out.println("new image info: " +newImageH+" "+newImageW+" "+newImgFormat);
	
	
	
	imageFromBytes = p5parent.createImage(newImageW, newImageH, PConstants.RGB);
	
	
	
	// get pixel data
	@SuppressWarnings("unused")
	int r, g, b, a;
	
	*/
	
	/*
	
	if(newImgFormat==1){
	
		
		
	} else if(newImgFormat==2){
	
		for(int i=0; i < imageFromBytes.pixels.length; i++) {		
			int pos = i*4 + 9;
			r = newImageByteArray[pos] & 0xFF;
			g = newImageByteArray[pos+1] & 0xFF;
			b = newImageByteArray[pos+2] & 0xFF;
			a = newImageByteArray[pos+3] & 0xFF;
			//imageFromBytes.pixels[pos] = p5parent.color(r, g, b, a); 
		}
	}
	
	*/
	
	//System.out.println("pixel count: "+imageFromBytes.pixels.length);
	/*
	
	for(int i=0; i < imageFromBytes.pixels.length; i++) {
		int pos = i*3 + 9;
		r = newImageByteArray[pos] & 0xFF;
		g = newImageByteArray[pos+1] & 0xFF;
		b = newImageByteArray[pos+2] & 0xFF;
		
		//System.out.println(i+": "+r+" "+g+" "+b);
		imageFromBytes.pixels[i] = p5parent.color(r, g, b); 
	}
	
	*/
	
	return imageFromBytes;
}

// ---------------------------------------------------------------------

/**
 * Returns true of false based on whether a String message is available
 * This should be used inside frameEvent() since messages are tied to specific frames
 * @return true if new String message 
 */
 
bool udpClient::messageAvailable() {

	return bMessageAvailable;
}

// ---------------------------------------------------------------------

/**
 * This should be used inside frameEvent() since messages are tied to specific frames
 * Only should be called after checking that {@link #messageAvailable()}  returns true
 * @return an array of messages from the server
 */
string * udpClient::getDataMessage() {

	return dataMessage;
}

// ---------------------------------------------------------------------

/**
 * This should be used inside frameEvent() since data from server is tied to a specific frame
 * @return true is an array of integers is available from server
 */    
 
bool udpClient::intsAvailable() {

	return bIntsAvailable;
}

// ---------------------------------------------------------------------

/**
 * This should be used inside frameEvent() since data from server is tied to a specific frame
 * @return true is an array of bytes is available from server
 */    
bool udpClient::bytesAvailable() {

	return bBytesAvailable;
}

// ---------------------------------------------------------------------

/**
 * This should be used inside frameEvent() since data from server is tied to a specific frame
 * Only should be called after checking that {@link #bytesAvailable()} returns true
 * @return the array of bytes from the server
 */    
char * udpClient::getBytes() {

	return bytes;
}

// ---------------------------------------------------------------------

/**
 * This should be used inside frameEvent() since data from server is tied to a specific frame
 * Only should be called after checking that {@link #intsAvailable()}  returns true
 * @return the array of ints from the server
 */    
int * udpClient::getInts() {

	return ints;
}

// ---------------------------------------------------------------------

/**
 * @return the total number of frames rendered
 */  
int udpClient::getFrameCount() {

	return frameCount;
}

// ---------------------------------------------------------------------

/**
 * @return the total number of frames rendered
 */  

void udpClient::splitString(string input_, char c_, string *& output, int & length){

	int charCount = 0;
	
	for(int i=0; i<input_.size(); i++){
	
		if(input_.at(i) == c_) charCount++;
	}
	
	if(charCount>0){
	
		length = charCount+1;
		output = new string[charCount+1];
	
		int arrayStep = 0;
		int lastPos = 0;
		
		for(int i=0; i<input_.size(); i++){
		
			//printf("%c -- %c",  input_.at(i), c_);
		
			if(input_.at(i) == c_) {
			
				output[arrayStep] = input_.substr(lastPos, i-lastPos);
				
				//printf("array element %i = %s\n", arrayStep, output[arrayStep].c_str());
				lastPos = i+1;
				arrayStep++;
			}	
		}
		
		output[arrayStep] = input_.substr(lastPos, input_.size()-lastPos);
		//printf("array element %i = %s\n", arrayStep, output[arrayStep].c_str());
		
	} else {
	
		length = 1;
		output = new string[length];
		output[0] = input_;
		//printf("single element %s\n", output[0].c_str());
	}
}

