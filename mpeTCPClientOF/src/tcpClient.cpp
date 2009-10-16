#include "tcpClient.h"
#include "testApp.h"
#include <stdio.h>
#include <stdlib.h>

/**
 * The MPE Client
 * The Client class registers itself with a server
 * and receives messages related to frame rendering and data input
 * <http://mostpixelsever.com>
 */

tcpClient::tcpClient(){
	
	//serverPort = 9002;
	//clientPort;
	//data = new char[65535];
	
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
	
	
	// what's come in?
	bMessageAvailable = false;  // Is a message available?
	messageCount = 0;
	
	//If DEBUG is true, the client will print lots of messages about what it is doing.
	// Set with debug=1; in your INI file.
	DEBUG = false;
	
	// True if all the other clients are connected.  Maybe this doesn't need to be public.
	allConnected = false;
}

// ---------------------------------------------------------------------

void tcpClient::setup(string iniFileURL){
	
	loadIniFile(iniFileURL);
	ofSetWindowShape(lWidth, lHeight);
}

// ---------------------------------------------------------------------

void tcpClient::setParent(testApp & parent_){
	
	parent = &parent_;
}

// ---------------------------------------------------------------------

void tcpClient::update(){
	
	if(bIsConnected){
		
		// send 'S' to server
		// set the destination for the messages
		//inet_aton(ipNum.c_str(), &udp.saClient.sin_addr);
		//udp.saClient.sin_port = htons(portNum); //portNum;
		
		string serverInput;
        bool bKeepReading = true;
        bool bDidWeReceiveOnce = false;
		
		bool received = false;
		// keep reading until nothing left
        //while(bKeepReading){
            received = read(serverInput);
            //if (bKeepReading == true){
				//bDidWeReceiveOnce = true;
            //}
		//}
		
		// NOTE: this process only takes the last message
		// this should be changed!
		
        if(received){
			
			if (DEBUG) printf("Receiving: %s\n",  serverInput.c_str());
			
			//A "G" startbyte will trigger a frameEvent.
			//If it's a B, we also have to get a byteArray
			//An I for int array
			
			char c = serverInput.at(0);
			if (c == 'G') {
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
					
					//free(dataMessage);
					
					int splitty = -1;
					for (int i = 0; i < serverInput.size(); i++) {
						char c = serverInput.at(i);
						if (c == ':') {
							splitty = i;
							break;
						}
					}
					
					//printf("splitty: %i\n",splitty);
					
					dataMessage = serverInput.substr(splitty+1);
					
					// create data message array
					//splitString(info[1], ',', dataMessage, messageCount);
					
					bMessageAvailable = true;
				} else {
					bMessageAvailable = false;
				}
				
				// compare frame count (if the same, draw new frame)
				if(frameCount == fc) {
					bDrawNewFrame = true;
					frameCount++;
				} else {
				    printf("Framecount is off? %d %d\n",frameCount,fc);	
				}
			}
		}
		
	} else {
		// wait 2 seconds 
		sleep(2);
		start();
	}
}


// ---------------------------------------------------------------------

// Loads the Settings from the Client INI file

void tcpClient::loadIniFile(string fileString){
	
	
	//--------------------------------------------- get configs
	
    ofxXmlSettings xmlReader;
	printf("filename: %s\n",fileString.c_str());
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
void tcpClient::setMasterDimensions(int _mWidth, int _mHeight) {
	
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

void tcpClient::setLocalDimensions(int _xOffset, int _yOffset, int _lWidth, int _lHeight) {
	
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
int tcpClient::getLWidth() {
	
	return lWidth;
}

// ---------------------------------------------------------------------

/**
 * returns local height.
 * @return local height in pixels
 */
int tcpClient::getLHeight() {
	
	return lHeight;
}

// ---------------------------------------------------------------------

/**
 * returns master width.
 * @return master width in pixels
 */
int tcpClient::getMWidth() {
	
	return mWidth;
}

// ---------------------------------------------------------------------

/**
 * returns x offset of frame.
 * @return x offset of frame.
 */
int tcpClient::getXoffset() {
	
	return xOffset;
}

// ---------------------------------------------------------------------

/**
 * returns y offset of frame.
 * @return y offset of frame.
 */
int tcpClient::getYoffset() {
	
	return yOffset;
}

// ---------------------------------------------------------------------

/**
 * returns master height.
 * @return master height in pixels
 */
int tcpClient::getMHeight() {
	
	return mHeight;
}

// ---------------------------------------------------------------------

/**
 * Places the viewing area for this screen. This must be called at the beginning
 * of the render loop.  If you are using Processing, you would typicall place it at
 * the beginning of your draw() function.
 *
 */
void tcpClient::placeScreen() {
	
	glTranslatef(xOffset * -1, yOffset * -1, 0);
}

// ---------------------------------------------------------------------

/**
 * Sends a "Done" command to the server. This must be called at the end of your draw loop.
 *
 */
void tcpClient::done() {
	rendering = false;
	char clientIDString[5];
	char frameCountString[1000];
	sprintf(clientIDString, "%i", clientID);
	sprintf(frameCountString, "%i", frameCount);
	string msg = "D," + string(clientIDString) + "," + string(frameCountString);
	send(msg);
}

// ---------------------------------------------------------------------

/**
 * Returns this screen's ID
 * @return screen's ID #
 */
int tcpClient::getClientID() {
	
	return clientID;
}

// ---------------------------------------------------------------------

/**
 * Returns bool of data found
 * @return bool of data found
 */

bool tcpClient::read(string & serverInput) {
	bool bDidWeGetAnything = false;
	//printf("Waiting to receive.\n");
	string str = client.receiveRaw();
	if (str.length() > 0){
		serverInput = str;
		bDidWeGetAnything = true;
	}
	//printf("Raw receive: %s\n",str.c_str());
	return bDidWeGetAnything;
}

// ---------------------------------------------------------------------

/**
 * This method should only be called internally by Thread.start().
 */

void tcpClient::run() {
	if (DEBUG) print("I'm running!");
	//let server that this client is ready to start.
	// send the message
	//send("S" + clientID);
	//done();
	try {
		while (bIsRunning) {
			
			
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

void tcpClient::start() {
	
	try {
		// --------------------- client binds
	    client.setVerbose(true);
		bIsConnected = client.setup(ipNum, portNum);
		if (bIsConnected) {
			if(DEBUG) printf("TCP connection bound (port %i)\n\n", portNum);
			bIsRunning = true;
			char initMessage[30];
			sprintf(initMessage, "S%i", clientID);
			send(initMessage); 
		} else { 
			printf("Error: TCP failed to connect to port %i\n\n", portNum);
		}
	} catch (MPEError e) {
		e.print();
	}
	//NOTE super.start();
}

// ---------------------------------------------------------------------

void tcpClient::send(string msg) {
	try {
		if (DEBUG) printf("sending %s\n", msg.c_str());
		msg += "\n";
		client.sendRaw(msg);
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
void tcpClient::broadcast(string msg) {
	// add a T to send message
	msg = "T"+ msg;
	send(msg);
}
// ---------------------------------------------------------------------

// ---------------------------------------------------------------------

/**
 * Stops the client thread.  You don't really need to do this ever.
 */  
void tcpClient::quit() {
	printf("Quitting.");
	bIsRunning = false; // Setting running to false ends the loop in run()
	//interrupt(); // In case the thread is waiting. . .
}

// ---------------------------------------------------------------------

void tcpClient::print(string string) {
	
	printf("MPE CLIENT: %s",  string.c_str());
}

// ---------------------------------------------------------------------

void tcpClient::setServer(string _server) {
	
	if (_server.compare("") != 0) host = _server;
}

// ---------------------------------------------------------------------

void tcpClient::setLocalDimensions(int w, int h) {
	
	if (w > -1 && h > -1) {
		lWidth = w;
		lHeight = h;
	}
}

// ---------------------------------------------------------------------

void tcpClient::setOffsets(int w, int h) {
	
	if (w > -1 && h > -1) {
		xOffset = w;
		yOffset = h;
	}
}

// ---------------------------------------------------------------------

void tcpClient::setID(int _ID) {
	
	if (_ID > -1) clientID = _ID;
}

// ---------------------------------------------------------------------

void tcpClient::setPort(int _port) {
	
	if (_port > -1) portNum = _port;
}


// ---------------------------------------------------------------------

/**
 * Returns true of false based on whether a String message is available
 * This should be used inside frameEvent() since messages are tied to specific frames
 * @return true if new String message 
 */

bool tcpClient::messageAvailable() {
	
	return bMessageAvailable;
}

// ---------------------------------------------------------------------

/**
 * This should be used inside frameEvent() since messages are tied to specific frames
 * Only should be called after checking that {@link #messageAvailable()}  returns true
 * @return an array of messages from the server
 */
string tcpClient::getDataMessage() {
	
	return dataMessage;
}

// ---------------------------------------------------------------------

/**
 * @return the total number of frames rendered
 */  
int tcpClient::getFrameCount() {
	
	return frameCount;
}

// ---------------------------------------------------------------------

/**
 * @return the total number of frames rendered
 */  

void tcpClient::splitString(string input_, char c_, string *& output, int & length){
	
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

