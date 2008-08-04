/**
 * A regular old Connection to the server (from an MPE client)
 * @author Shiffman and Kairalla
 */

package mpe.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CyclicBarrier;


public class Connection {
	
	boolean ready = false;
	long lastHeardFrom = 0;
    int clientID = -1;
    String uniqueName;
    String msg = "";
    boolean running = true;
    UDPServer parent;
    // UDP Stuff!
	InetAddress address;
	int port;

    Connection(int id, String address_, int port_, UDPServer p) {
    	clientID = id;
    	
        parent = p;
        
        try {
			address = InetAddress.getByName(address_);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        port = port_;
        uniqueName = address + " " + port;
        
        lastHeardFrom = System.currentTimeMillis();
        
        System.out.println(uniqueName + " connection object made.");
    }

    private void print(String string) {
        System.out.println("Connection: "+string);

    }
    
    public void killMe(){
        System.out.println("Removing Connection " + clientID);
        parent.drop(clientID);
        running = false;
        if (parent.allDisconected()) parent.resetFrameCount();
    }

    // Trying out no synchronize
    public void send(String msg) {
        if (mpePrefs.DEBUG) System.out.println("Sending: " + uniqueName + ": " + msg);
        
        byte[] data = msg.getBytes();
        
        DatagramPacket packet = new DatagramPacket(data,data.length,address,port);
		packet.setData(data);
		try {
			parent.frontDoor.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void setReady(boolean ready_) {
    	ready = ready_;
    }
    
    public void active() {
    	lastHeardFrom = System.currentTimeMillis();
    }
    
    public boolean isReady() {
    	return ready;
    }

	public boolean isDead() {
		long now = System.currentTimeMillis();
		if (now - lastHeardFrom > UDPServer.disconnectTime) return true;
		else return false;
	}

}

