#include "mpeClientTCP.h"

//--------------------------------------------------------------
mpeClientTCP::mpeClientTCP() {
    setDefaults(); 
}

//--------------------------------------------------------------
void mpeClientTCP::setup(string _fileString, mpeClientListener* _parent, bool _autoMode) {
    parent   = _parent;
    autoMode = _autoMode;
    
    loadIniFile(_fileString);
    ofSetWindowShape(lWidth, lHeight);
    
    if (autoMode) {
        ofAddListener(ofEvents.draw, this, &mpeClientTCP::_draw);
    }
}

//--------------------------------------------------------------
// Called automatically by PApplet.draw() when using auto mode.
//--------------------------------------------------------------
void mpeClientTCP::draw() {
    if (isThreadRunning() && rendering) {
        placeScreen();
        parent->frameEvent();
        done();
    }
}

//--------------------------------------------------------------
// Loads the settings from the Client INI file.
//--------------------------------------------------------------
void mpeClientTCP::loadIniFile(string _fileString) {
	out("Loading settings from file " + _fileString);
    
	ofxXmlSettings xmlReader;
    if (!xmlReader.loadFile(_fileString)) 
        err("ERROR loading XML file!");
	
    // parse INI file
    hostName   = xmlReader.getValue("settings:server:ip", "127.0.0.1", 0);
    serverPort = xmlReader.getValue("settings:server:port", 7887, 0);
	id         = xmlReader.getValue("settings:client_id", -1, 0);
	
	setLocalDimensions(xmlReader.getValue("settings:local_dimensions:width",  640, 0), 
					   xmlReader.getValue("settings:local_dimensions:height", 480, 0));
    
    setOffsets(xmlReader.getValue("settings:local_location:x", 0, 0),
               xmlReader.getValue("settings:local_location:y", 0, 0));
	
	setMasterDimensions(xmlReader.getValue("settings:master_dimensions:width",  640, 0), 
						xmlReader.getValue("settings:master_dimensions:height", 480, 0));
	
	if (xmlReader.getValue("settings:go_fullscreen", "false", 0).compare("true") == 0)
		ofSetFullscreen(true);
    
	if(xmlReader.getValue("settings:offset_window", "false", 0).compare("true") == 0)
		ofSetWindowPosition(xOffset, yOffset);
	
	if (xmlReader.getValue("settings:debug", 0, 0) == 1) 
        DEBUG = true;
	
    out("Settings: server = " + hostName + ":" + ofToString(serverPort) + ",  id = " + ofToString(id)
        + ", local dimensions = " + ofToString(lWidth) + ", " + ofToString(lHeight)
        + ", location = " + ofToString(xOffset) + ", " + ofToString(yOffset));
}

//--------------------------------------------------------------
// Sets the dimensions for the local display.
//--------------------------------------------------------------
void mpeClientTCP::setLocalDimensions(int _lWidth, int _lHeight) {
    if (_lWidth > -1 && _lHeight > -1) {
        lWidth = _lWidth;
        lHeight = _lHeight;
    }
}

//--------------------------------------------------------------
// Sets the offsets for the local display.
//--------------------------------------------------------------
void mpeClientTCP::setOffsets(int _xOffset, int _yOffset) {
    if (_xOffset > -1 && _yOffset > -1) {
        xOffset = _xOffset;
        yOffset = _yOffset;
    }
}

//--------------------------------------------------------------
// Sets the dimensions for the local display.
// The offsets are used to determine what part of the Master Dimensions to render.
// For example, if you have two screens, each 100x100, and the master dimensions are 200x100
// then you would set
//  client 0: setLocalDimensions(0, 0, 100, 100);
//  client 1: setLocalDimensions(100, 0, 100, 100);
// for a 10 pixel overlap you would do:
//  client 0: setLocalDimensions(0, 0, 110, 100);
//  client 1: setLocalDimensions(90, 0, 110, 100);
//--------------------------------------------------------------
void mpeClientTCP::setLocalDimensions(int _xOffset, int _yOffset, int _lWidth, int _lHeight) {
    setOffsets(_xOffset, _yOffset);
    setLocalDimensions(_lWidth, _lHeight);
}

//--------------------------------------------------------------
// Sets the master dimensions for the Video Wall. 
// This is used to calculate what is rendered.
//--------------------------------------------------------------
void mpeClientTCP::setMasterDimensions(int _mWidth, int _mHeight) {
    if (_mWidth > -1 && _mHeight > -1) {
        mWidth = _mWidth;
        mHeight = _mHeight;
    }
}

//--------------------------------------------------------------
// Sets the field of view of the camera when rendering in 3D.
// Note that this has no effect when rendering in 2D.
//--------------------------------------------------------------
void mpeClientTCP::setFieldOfView(float val) {
    fieldOfView = val;
    cameraZ = (ofGetHeight() / 2.f) / tanf(M_PI * fieldOfView/360.f);
}

//--------------------------------------------------------------
// Places the viewing area for this screen. This must be called at the 
// beginning of the render loop.  If you are using Processing, you would 
// typically place it at the beginning of your draw() function.
//--------------------------------------------------------------
void mpeClientTCP::placeScreen() {
    if (bEnable3D) {
        placeScreen3D();
    } else {
        placeScreen2D();
    }
}

//--------------------------------------------------------------
// If you want to enable or disable 3D manually in automode
//--------------------------------------------------------------
void mpeClientTCP::enable3D(bool _b) {
    bEnable3D = _b;
}

//--------------------------------------------------------------
// Places the viewing area for this screen when rendering in 2D.
//--------------------------------------------------------------
void mpeClientTCP::placeScreen2D() {
    glTranslatef(xOffset * -1, yOffset * -1, 0);
}

//--------------------------------------------------------------
// Places the viewing area for this screen when rendering in 3D.
//--------------------------------------------------------------
void mpeClientTCP::placeScreen3D() {
    gluLookAt(mWidth/2.f, mHeight/2.f, cameraZ,
              mWidth/2.f, mHeight/2.f, 0, 
              0, 1, 0);
    
    
    // The frustum defines the 3D clipping plane for each Client window!
    float mod = .1f;
    float left   = (xOffset - mWidth/2)*mod;
    float right  = (xOffset + lWidth - mWidth/2)*mod;
    float top    = (yOffset - mHeight/2)*mod;
    float bottom = (yOffset + lHeight-mHeight/2)*mod;
    float near   = cameraZ*mod;
    float far    = 10000;
    glFrustum(left, right,
              top, bottom,
              near, far);
}

//--------------------------------------------------------------
// Restores the viewing area for this screen when rendering in 3D.
//--------------------------------------------------------------
void mpeClientTCP::restoreCamera() {
    gluLookAt(ofGetWidth()/2.f, ofGetHeight()/2.f, cameraZ,
              ofGetWidth()/2.f, ofGetHeight()/2.f, 0, 
              0, 1, 0);
    
    float mod = .1f;
    glFrustum(-(ofGetWidth()/2.f)*mod, (ofGetWidth()/2.f)*mod,
              -(ofGetHeight()/2.f)*mod, (ofGetHeight()/2.f)*mod,
              cameraZ*mod, 10000);
}

//--------------------------------------------------------------
// Checks whether the given point is on screen.
//--------------------------------------------------------------
bool mpeClientTCP::isOnScreen(float _x, float _y) {
    return (_x > xOffset && 
            _x < (xOffset + lWidth) && 
            _y > yOffset &&
            _y < (yOffset + lHeight));
}

//--------------------------------------------------------------
// Checks whether the given rectangle is on screen.
//--------------------------------------------------------------
bool mpeClientTCP::isOnScreen(float _x, float _y, float _w, float _h) {
    return (isOnScreen(_x, _y) || 
            isOnScreen(_x + _w, _y) ||
            isOnScreen(_x + _w, _y + _h) ||
            isOnScreen(_x, _y + _h));
}

//--------------------------------------------------------------
// Outputs a message to the console.
//--------------------------------------------------------------
void mpeClientTCP::out(string _str) {
    print(_str);
}

//--------------------------------------------------------------
// Outputs a message to the console.
//--------------------------------------------------------------
void mpeClientTCP::print(string _str) {
    if (DEBUG)
        cout << "mpeClient: " << _str << endl;
}

//--------------------------------------------------------------
// Outputs an error message to the console.
//--------------------------------------------------------------
void mpeClientTCP::err(string _str) {
    cerr << "mpeClient: " << _str << endl;
}

//--------------------------------------------------------------
void mpeClientTCP::start() {
    tcpClient.setVerbose(DEBUG);
    if (!tcpClient.setup(hostName, serverPort)) {
        err("TCP failed to connect to port " + ofToString(serverPort));
        return;
    }
    
    out("TCP connection bound on port " + ofToString(serverPort));
    startThread(true, false);  // blocking, verbose
}

//--------------------------------------------------------------
void mpeClientTCP::threadedFunction() {
    out("Running!");
    
    // let the server know that this client is ready to start
    send("S" + ofToString(id));
    
    while (isThreadRunning()) {
        if (lock()) {
            string msg = tcpClient.receiveRaw();
            if (msg.length() > 0) {
                read(msg);
            }
            
            unlock();
            ofSleepMillis(5);
        }
    }
}

//--------------------------------------------------------------
// Reads and parses a message from the server.
//--------------------------------------------------------------
void mpeClientTCP::read(string _serverInput) {
    out("Receiving: " + _serverInput);
        
    char c = _serverInput.at(0);
    if (c == 'G' || c == 'B' || c == 'I') {
        if (!allConnected) {
            if (DEBUG) out("all connected!");
            allConnected = true;
        }
        // split into frame message and data message
        vector<string> info = ofSplitString(_serverInput, ":");
        vector<string> frameMessage = ofSplitString(info[0], ",");
        int fc = ofToInt(frameMessage[1]);
        
        if (info.size() > 1) {
            // there is a message here with the frame event
            info.erase(info.begin());
            dataMessage.clear();
            dataMessage = info;
            bMessageAvailable = true;
        } else {
            bMessageAvailable = false;
        }
        
        // assume no arrays are available
        bIntsAvailable  = false;
        bBytesAvailable = false; 
        
        if (fc == frameCount) {
            rendering = true;
            frameCount++;
            
            // calculate new framerate
            float ms = ofGetElapsedTimeMillis() - lastMs;
            fps = 1000.f / ms;
            lastMs = ofGetElapsedTimeMillis();
            
            if (!autoMode) {
                parent->frameEvent();
            }
        }
    }
}

//--------------------------------------------------------------
// Send a message to the server.
//--------------------------------------------------------------
void mpeClientTCP::send(string _msg) {
    out("Sending: " + _msg);
    
    _msg += "\n";
    tcpClient.sendRaw(_msg);
}

//--------------------------------------------------------------
// Format a broadcast message and send it.
// Do not use a colon ':' in your message!!!
//--------------------------------------------------------------
void mpeClientTCP::broadcast(string _msg) {
    _msg = "T" + _msg;
    send(_msg);
}

//--------------------------------------------------------------
// Sends a "Done" command to the server. This must be called at 
// the end of the draw loop.
//--------------------------------------------------------------
void mpeClientTCP::done() {
    //if (broadcastingData) {
    //    sayDoneAgain = true;
    //} else {
    
    rendering = false;
    string msg = "D," + ofToString(id) + "," + ofToString(frameCount);
    send(msg);
    //}
}

//--------------------------------------------------------------
// Stops the client thread.  You don't really need to do this ever.
//--------------------------------------------------------------
void mpeClientTCP::quit() {
    out("Quitting.");
    stopThread();
}


