
 
#ifndef MPE_UDP_CLIENT
#define MPE_UDP_CLIENT

#define UDP_MSG_SIZE 1024

#include "ofMain.h" 
 
#define OF_ADDON_USING_OFXNETWORK
#define OF_ADDON_USING_OFXXMLSETTINGS

#include "ofAddons.h"

class testApp;

class udpClient {

	public:

		udpClient();
		
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
		void broadcastImage(ofImage img);
		
		bool imageAvailable();
		ofImage getImage();
		
		bool messageAvailable();
		string * getDataMessage(); // JR NOTE was string[]
		
		bool intsAvailable();
		int * getInts(); // JR NOTE was byte
		
		bool bytesAvailable();
		char * getBytes(); // JR NOTE was byte
		
		void splitString(string input_, char c_, string *& output, int & length);
		
		int getFrameCount();
		
		int repeatTime;
		
		testApp * parent;

		string host; // QUESTION - what is the differece between host and ipNum?
		string ipNum;
		int portNum;

		ofxUDPManager udp;

		char updPacketStr[UDP_MSG_SIZE]; // byte array from UDP
		char * data; // image data
		
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
		string * dataMessage;
		string imgMessage;
		int messageCount;

		// Are we broadcasting?
		bool broadcastingData;
		
		// Do we need to wait to broadcast b/c we have alreaddy done so this frame?
		bool waitToSend;

		bool bMessageAvailable;  // Is a message available?
		bool bImageAvailable; // Is an image available?
		bool bIntsAvailable;     // Is an int array available?
		bool bBytesAvailable;    // is a byte array avaialble?
		bool bSayDoneAgain;  // Do we need to say we're done after a lot of data has been sent
		int * ints;                // ints that have come in
		char * bytes;              // bytes that have come in

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