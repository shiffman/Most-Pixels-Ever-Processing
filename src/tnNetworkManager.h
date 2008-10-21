#ifndef TN_NETWORK_MANAGER_H
#define TN_NETWORK_MANAGER_H

#include "ofMain.h"

#define OF_ADDON_USING_OFXNETWORK
#define OF_ADDON_USING_OFXXMLSETTINGS

#include "ofAddons.h"

//#include "networkSettings.h"

class testApp;
class tnMovieModule;

class tnNetworkManager {

    public:

        tnNetworkManager();
        ~tnNetworkManager();

        void update();
        void draw();
        void setup(int mID);
		
		vector<string> breakMessageIntoParts(string message);

		//void setParent(testApp & parent_);
		void setParent(tnMovieModule & parent_);

        ofTrueTypeFont * verdana; // grabbed from someone else to save memory.

        void loadMulticastSettings();
        void loadIdSettings();

		void connect();
		void bind();
		void broadcast(networkPacket data);
		bool receive(string & data);

        //-------------------------------------
        bool        bShowErrors;
        string      errorMessage;
        float       delaySecsForError;
        bool        bFirstUpdate;
        float       lastTimeIGotNetworkData;
		
        //-------------------------------------

        bool                    bAmServer;
		int						clientID;
		
		//int moduleID;

        // for the server
        networkPacket           serverPacket;

        //----------------------------------------------------------
		std::string 	        ipNum;
		unsigned short 	        portNum;
		ofxUDPManager 		    udp;
		bool 			        bConnected;
		char			        * networkPacketStr;
        //----------------------------------------------------------

		testApp * parent;
		//tnMovieModule * parent;
		
		int mode;

    protected:
    private:

};


#endif

