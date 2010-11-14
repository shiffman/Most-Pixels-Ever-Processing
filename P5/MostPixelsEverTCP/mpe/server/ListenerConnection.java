/**
 * A Listener Connection for the Server
 * @author Shiffman and Kairalla
 *
 */

package mpe.server;

import java.io.IOException;
import java.net.Socket;

public class ListenerConnection extends Connection {
	String inputString = "";
    ListenerConnection(Socket socket_, MPEServer p) {
    	super(socket_, p);
        uniqueName = "Listener Conn" + socket.getRemoteSocketAddress();
    }

    public void run() {
  	  int readIn = 0;
        while (running) {
            try {
            	  String msg = null;
				readIn = in.read();
				if (readIn == -1){
                    killMe();
                    break;
				} else {
					msg = read((char)readIn);
					if (msg != null){
						if (MPEPrefs.DEBUG) print ("received from backdoor: " + msg);
						//only attach to message if everyone's connected.
						if (parent.allConnected){
			            parent.newMessage = true;
			            parent.message += msg+":";
						}
					}
				}
                
            } catch (IOException e) {
                System.out.println("Listener connection just died." + e);
                killMe();
                break;
            }            
        }

    }

    private void print(String string) {
        System.out.println("Connection: "+string);

    }
    
    private String read(char in){
  	  String back = null;
  		if (in =='\n'){
  			back = new String(inputString);
  			inputString = "";
  		} else {
  			inputString = inputString+in;
  		}
  		return back;
  	}
    /**
     * prints string as a stream of characters to client
     * @param out
     */
    public void send(String out) {
  	  out+="\n";
  	try {
  		os.write(out.getBytes());
  		os.flush();
  	} catch (IOException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
  }
    public void killMe(){
        System.out.println("Removing Listener Connection " + clientID);
        running = false;
        parent.killListenerConnection();
    }

}

