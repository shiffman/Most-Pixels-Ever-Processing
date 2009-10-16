
 
#ifndef MPE_TCP_CLIENT
#define MPE_TCP_CLIENT

#include "ofMain.h" 
#include "ofxNetwork.h"
#include "ofxXmlSettings.h"
 
//#define OF_ADDON_USING_OFXNETWORK
//#define OF_ADDON_USING_OFXXMLSETTINGS

class testApp;

class tcpClient {

	public:

		tcpClient();
		
		void setup(string iniFileURL);
		void setParent(testApp & parent_);
		void update();
		void draw();
		
		void setMasterDimensions(int _mWidth, int _mHeight);
		void setLocalDimensions(int _xOffset, int _yOffset, int _lWidth, int _lHeight);
		
		int getLWidth();
		int getLHeight();
		int getMWidth();
		int getXoffset();
		int getYoffset();
		int getMHeight();
		
		void placeScreen();
		
		int getClientID();
		
		void run();
		void start();
		void done();
		void quit();
		
		void send(string msg);
		void broadcast(string msg);
		
		bool messageAvailable();
		string getDataMessage(); // JR NOTE was string[]
		
		void splitString(string input_, char c_, string *& output, int & length);
		int getFrameCount();
				
		testApp * parent;

		string host; // QUESTION - what is the differece between host and ipNum?
		string ipNum;
		int portNum;

		ofxTCPClient client;

		char * msg; // byte array from UDP
		
		// this is used for communication to let the server know which client is speaking
		// and how to order the screens
		int clientID; 
		
		//the total number of screens
		int numScreens; 
		
		int mWidth; //master width
		int mHeight; //master height
		int lWidth; //local width
		int lHeight; //local height
		int xOffset;
		int yOffset;
		bool bIsDone; //flipped to true when we're done rendering.
		
		//public boolean moveOn = false; //tells parent to loop back
		int fps;
		bool bIsConnected;
		bool bIsRunning;
		bool bDrawNewFrame;
		int frameCount;
		bool rendering;
		// Ok, adding something so that a client can get a dataMessage as part of a frameEvent
		string dataMessage;
		int messageCount;

		bool bMessageAvailable;  // Is a message available?

		/**
		 * If DEBUG is true, the client will print lots of messages about what it is doing.
		 * Set with debug=1; in your INI file.
		 */
		bool DEBUG;

		/**
		 * True if all the other clients are connected.  Maybe this doesn't need to be public.
		 */
		bool allConnected;
		

	private:

		void loadIniFile(string fileString);
		//void out(string string);
		bool read(string & serverInput);
		void print(string string);
		void setServer(string _server);
		
		void setID(int _ID);
		void setPort(int _port);
		
		void setLocalDimensions(int w, int h);
		void setOffsets(int w, int h);
		
		// for the server
        char * networkPacketStr;
};

class MPEError {
   public:
      
	  const char* error;
      
	  MPEError(const char* arg) : error(arg) { }
	  
	  void print(){
	  
		printf("MPEError %s\n", error);
	  }
	  
};

#endif