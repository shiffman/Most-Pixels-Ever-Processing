
#include "tnNetworkManager.h"
#include "tnMovieModule.h"
#include "testApp.h"

//---------------------------------------------
tnNetworkManager::tnNetworkManager(){

	udp.Create();
	bConnected = false;
	networkPacketStr = new char [sizeof(networkPacket)];

	int setBlocking = udp.SetNonBlocking(true);
	if(!setBlocking) printf("failed to change blocking state\n");
	
}

//-------------------------------------------------------
tnNetworkManager::~tnNetworkManager(){

	udp.Close();
}

//-------------------------------------------------------
void tnNetworkManager::setup(int mID){

	moduleID = mID;

    loadIdSettings();

    if (bAmServer)          connect();
    else                    bind();

    bShowErrors             = false;
    errorMessage            = "there is no communciation\nfrom the server";
    delaySecsForError       = 4.0f;
    lastTimeIGotNetworkData = ofGetElapsedTimef();
    bFirstUpdate            = true;

}


void tnNetworkManager::setParent(tnMovieModule & parent_){
//void tnNetworkManager::setParent(testApp & parent_){

	parent = &parent_;
}


//-------------------------------------------------------
void tnNetworkManager::update(){

    //---------------------------------------
    // timer for errors
    if (!bAmServer){
        if (bFirstUpdate){
            lastTimeIGotNetworkData = ofGetElapsedTimef();
            bFirstUpdate = false;
        }
    }
    //---------------------------------------

	

    if (bAmServer){
    
		// send last server packet
		broadcast(serverPacket);
    
	} else {
        
		string temp;
        bool bKeepReading = true;
        bool bDidWeReceiveOnce = false;
		
		//printf("\n", 0);
		
		// keep reading until nothing left
		
        while(bKeepReading){
		
			//printf("test %i\n", 0);
		
            bKeepReading = receive(temp);
			
            if (bKeepReading == true){
			
               bDidWeReceiveOnce = true;
            }
		}
		
		// NOTE: does this process only take the last message?
		
        if(bDidWeReceiveOnce){
            
			//this is where to do state change detection.
			
			if(temp.size()>0)  { 
			
				//printf("received (outside) %s\n", temp.c_str());
				
				//mode = temp.compare("play") == 0 ? 1 : 0;
				
				if(temp.compare("play")==0){
				
					mode = 1;
				
				} else if (temp.compare("load")==0){
				
					mode = 0;
					
				} else if (temp.compare("stop")==0){
				
					// what happens in this case? should we make another mode
					// mode = 3;
					mode = 0;
				
				} else if (temp.substr(0, 4).compare("fade")==0){
				
					// breakup message string and tell scheduler to doManualFade()
					vector<string> params = breakMessageIntoParts(temp);
					parent->appScheduler.doManualFade(atof(params.at(1).c_str()), atof(params.at(2).c_str()), atoi(params.at(3).c_str()), params.at(4));
				
				} else if (temp.substr(0, 9).compare("audio_vol")==0){
				
					// breakup message string and tell scheduler to doManualAudioFade()
					vector<string> params = breakMessageIntoParts(temp);
					parent->appScheduler.doManualAudioFade(atof(params.at(1).c_str()), atof(params.at(2).c_str()), atoi(params.at(3).c_str()));
					
				}
			}
        }
    }
}

vector<string> tnNetworkManager::breakMessageIntoParts(string message){
	
	//string udpInstruction = "play,fade=1.0,color=white,vol=1.0";
	vector<string> parts;
	
	std::string::size_type part_start( 0 );
	std::string::size_type part_end( 0 );
	
	do
	{
		// Find position of next char
		part_end = message.find( ',', part_start );
		
		// Calculate the part length
		std::string::size_type part_length( 0 );
		part_length = part_end - part_start;
		
		// Obtain part substring
		std::string part( message.substr(part_start, part_length) );
		
		// append to parts vector
		parts.push_back( part );
		
		// Move start ready to find next part
		part_start = part_end + 1;
	}
	while ( std::string::npos != part_end );
	   
	for(int i=0; i<parts.size(); i++){
		
		printf("%s\n", parts.at(i).c_str());
	}
	
	return parts;
}


//-------------------------------------------------------
void tnNetworkManager::draw(){

	if (bShowErrors && (!bAmServer)){
        ofSetColor(0xff0000);
        if ((ofGetElapsedTimef() - lastTimeIGotNetworkData) > delaySecsForError){
           glPushMatrix();
           glScalef(5,5,1);
           verdana->drawString(errorMessage,20,20);
           glPopMatrix();
        }
	}
}


//---------------------------------------------
void tnNetworkManager::loadIdSettings(){

    //--------------------------------------------- get configs
    ofxXmlSettings xmlReader;
	xmlReader.loadFile("configuration/networkId.xml");
	bAmServer = xmlReader.getValue("params:amServer", 0) == 0 ? false : true;

	//--------------------------------------------- get ipadress settings:

	//ipNum = std::string("10.1.2.212");
	//portNum = 7887;
	
	
	bool result = xmlReader.loadFile("configuration/networkSettings.xml");
	
	if(!result) printf("error loading xml file\n");
	
	ipNum = xmlReader.getValue("params:udp-connection:ip", "224.0.0.22", 0);
	int portNumTemp = -1;
	portNumTemp = xmlReader.getValue("params:udp-connection:port", 7887, 0);
	portNum = portNumTemp;
	
	//printf("udp settings %s %i\n", ipNum.c_str(), portNum);
	
	/*
	
	printf("--------------------------------------------------\n");
	printf("loaded multicast network settings:\n");
	printf("multicast IP address: %s\n", ipNum.c_str());
	printf("portNum: %i\n", portNum);
	//if (bAmServer == true)  printf("I am the SERVER\n");
	//else                    printf("I am the CLIENT\n");
	printf("--------------------------------------------------\n");
	
	*/
	
	//printf("multicast IP address: %s\n", ipNum.c_str());
	//printf("portNum: %i\n", portNum);
	
	//clientID = xmlReader.getValue("params:id", 666);
	
	//printf("clientID %i\n", clientID);
}


//---------------------------------------------
void tnNetworkManager::connect(){

	char * ipStr = (char *) ipNum.c_str();
	bConnected = udp.ConnectMcast(ipStr,portNum);
	if (bConnected) printf("conneted \n");
}

//---------------------------------------------
void tnNetworkManager::bind(){

	// --------------------- client binds

	//printf("---------- trying to bind --------- \n");
	char * ipStr = (char *) ipNum.c_str();
	
	bConnected = udp.BindMcast(ipStr, portNum+moduleID);
	//bConnected = udp.Bind(portNum);
	
	//printf("%s %i\n", ipNum.c_str(), portNum+moduleID);
	
	if (bConnected) {
	
		//printf("UDP connection bound %s %i\n", ipNum.c_str(), portNum+moduleID);
	
	} else { 
	
		printf("UDP failed to connect\n");
	}
	
	// udp.SetTimeoutReceive(0);

}


//---------------------------------------------
void tnNetworkManager::broadcast(networkPacket data){

	char * dataPtr = (char *)(&data);
	memcpy(networkPacketStr, dataPtr, sizeof(networkPacket));

	if (bConnected) {
		int res = udp.Send(networkPacketStr, sizeof(networkPacket));
	} else {
		connect();
	}
}

//---------------------------------------------
bool tnNetworkManager::receive(string & data){

	//char * dataPtr = (char *)(&data);
	//
	bool bDidWeGetAnything = false;
	
	//printf("recieved %i\n", res);
	
	if (bConnected) {
	
		char pBuff[1024];
		int res = udp.Receive(pBuff, 1024);
		
		//char pBuff[sizeof(networkPacket)];
		//int res = udp.Receive(pBuff, sizeof(networkPacket));
		
		
		
		//printf("recieved %i\n", res);
		if (res > 0){
		
			//printf("received %s\n", pBuff);
		
			string rec;
		
			rec.assign(pBuff);
			
			if(rec.size()>0){
			
				data = rec;
			}
		
		    bDidWeGetAnything = true;
			//printf("recieved %s\n", pBuff);
			//memcpy(data, pBuff, sizeof(networkPacket));
		}
	} else {
	
		//bind();
	}
	
	return bDidWeGetAnything;
}
