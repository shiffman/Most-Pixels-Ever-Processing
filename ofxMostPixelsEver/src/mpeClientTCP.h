#pragma once

#include "ofMain.h"

#include "ofxNetwork.h"
//#include "ofxThread.h"
#include "ofxXmlSettings.h"

//--------------------------------------------------------------
class mpeClientListener {
    public:
        virtual void frameEvent() = 0;
};

//--------------------------------------------------------------
class mpeClientTCP : public ofThread {

	public:
              mpeClientTCP();
        void  setup(string _fileString, mpeClientListener* _parent, bool _autoMode = true);
    
        void  start();
        void  stop();
    
        void  draw();
    
        int   getPort() { return serverPort; }
        int   getID()   { return id; }
    
        void  setLocalDimensions(int _xOffset, int _yOffset, int _lWidth, int _lHeight);
        void  setMasterDimensions(int _mWidth, int _mHeight);
    
        // return dimensions in pixels
        int   getLWidth()  { return lWidth; }
        int   getLHeight() { return lHeight; }
        int   getXoffset() { return xOffset; }
        int   getYoffset() { return yOffset; }
        int   getMWidth()  { return mWidth; }
        int   getMHeight() { return mHeight; }
    
        int   getFrameCount() { return frameCount; }
        float getFPS()        { return fps; }
        bool  isRendering()   { return rendering; }
    
        void  setFieldOfView(float _val);
        float getFieldOfView() { return fieldOfView; }
    
        void  placeScreen();
        void  enable3D(bool _b);
        void  placeScreen2D();
        void  placeScreen3D();
        void  restoreCamera();
    
        bool  isOnScreen(float _x, float _y);
        bool  isOnScreen(float _x, float _y, float _w, float _h);
    
        void  broadcast(string _msg);
    
        bool  messageAvailable() { return bMessageAvailable; }
        vector<string> getDataMessage() { return dataMessage; }
        bool  intsAvailable() { return bIntsAvailable; }
        vector<int> getInts() { return ints; }
        bool  bytesAvailable() { return bBytesAvailable; }
        vector<char> getBytes() { return bytes; }
    
        void  done();
        void  quit();
    
        bool  DEBUG;
        
    protected:
        void setDefaults() {
            DEBUG = true;
            
            id = 0;
            mWidth  = -1;
            mHeight = -1;
            lWidth  = 640;
            lHeight = 480;
            xOffset = 0;
            yOffset = 0;
            
            rendering = false;
            autoMode  = false;
            
            frameCount = 0;
            fps        = 0.f;
            lastMs     = 0;
            
            allConnected = false;
            
            bEnable3D    = false;
            fieldOfView = 30.f;
        }
    
        void _draw(ofEventArgs &e) { draw(); }
        void threadedFunction();
        
        void loadIniFile(string _fileString);
    
        void setLocalDimensions(int _lWidth, int _lHeight);
        void setOffsets(int _xOffset, int _yOffset);
    
        void  out(string _msg);
        void  print(string _msg);
        void  err(string _msg);

        //void run();
        void read(string _serverInput);
        void send(string _msg);
    
        mpeClientListener* parent;
    
        string       hostName;
        int          serverPort;
        ofxTCPClient tcpClient;
        
        /** The id is used for communication with the server, to let it know which 
         *  client is speaking and how to order the screens. */
        int id;
        /** The total number of screens. */
        int numScreens;
        
        /** The master width. */
        int mWidth;
        /** The master height. */
        int mHeight;
        
        /** The local width. */
        int lWidth;
        /** The local height. */
        int lHeight;
        
        int xOffset;
        int yOffset;
        
        bool rendering;
        bool autoMode;
        
        int   frameCount;
        float fps;
        long  lastMs;
        
        /** True if all the other clients are connected. */
        bool allConnected;
        
        bool bMessageAvailable;
        bool bIntsAvailable;
        bool bBytesAvailable;
        vector<string> dataMessage;
        vector<int>    ints;
        vector<char>   bytes;
        
        // 3D variables
        bool  bEnable3D;
        float fieldOfView;
        float cameraZ;
    
};