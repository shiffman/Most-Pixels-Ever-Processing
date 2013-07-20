/**
 * A regular old Connection to the server (from an MPE client)
 * @author Shiffman and Kairalla
 */

package mpe.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Connection extends Thread {
    Socket socket;

    InputStream in;
    OutputStream os;
    //DataInputStream dis;
    BufferedReader brin;
    DataOutputStream dos;

    //CyclicBarrier barrier;

    int clientID = -1;
    String uniqueName;

    String msg = "";

    boolean running = true;
    MPEServer parent;

    Connection(Socket socket_, MPEServer p) {
        //barrier = new CyclicBarrier(2);
        socket = socket_;
        parent = p;
        uniqueName = "Conn" + socket.getRemoteSocketAddress();
        // Trade the standard byte input stream for a fancier one that allows for more than just bytes (dano)
        try {
            in = socket.getInputStream();
            //dis = new DataInputStream(in);
            brin = new BufferedReader(new InputStreamReader(in));
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
        } catch (IOException e) {
            System.out.println("couldn't get streams" + e);
        }
    }

    void read(String msg) {
        //if (mpePrefs.DEBUG) System.out.println("Raw receive: " + this + ": " + msg);


        // A little bit of a hack, it seems there are blank messages sometimes??
        char startsWith =  ' ';
        if (msg.length() > 0) startsWith = msg.charAt(0);

        switch(startsWith){
        // For Starting Up
        case 'S':
            if (MPEPrefs.DEBUG) System.out.println(msg);
            //do this with regex eventually, but for now..
            //(DS: also, probably just use delimiter and split?)
            //this is in serious need of error checking.
            int start = 1;
            clientID = Integer.parseInt(msg.substring(start));
            System.out.println("Connecting Client " + clientID);
            parent.connected[clientID] = true;
            boolean all = true;
            for (int i = 0; i < parent.connected.length; i++){  //if any are false then wait
                if (parent.connected[i] == false) {
                	all = false;
                	break;
                }
            }
            parent.allConnected = all;
            if (parent.allConnected) parent.triggerFrame();
            break;
            //is it receiving a "done"?
        case 'D':   
            if (parent.allConnected) {
                // Networking protocol could be optimized to deal with bytes instead of chars in a String?
                String[] info = msg.split(",");
                clientID = Integer.parseInt(info[1]);
                int fc = Integer.parseInt(info[2]);
                if (MPEPrefs.DEBUG) System.out.println("Receive: " + clientID + ": " + fc + "  match: " + parent.frameCount);
                if (fc == parent.frameCount) {
                    parent.setReady(clientID);
                    //if (parent.isReady()) parent.triggerFrame();
                }
            }
            break;
            //is it receiving a "daTa"?
        case 'T':   
            if (MPEPrefs.DEBUG) print ("adding message to next frameEvent: " + msg);
            parent.newMessage = true;
            parent.message += msg.substring(1,msg.length())+":";
            //parent.sendAll(msg);
            break;
        /*case 'B':
            // Reading in byte arrays
            parent.dataload = true;
            try {
                int len = dis.readInt();
                if (mpePrefs.DEBUG) System.out.println("Reading byte array, size: :  " + len);
                byte[] data = new byte[len];
                dis.read(data, 0, len);
                parent.newBytes = true;
                parent.bytes = data;
            } catch (IOException e) {
                e.printStackTrace();
            }
            parent.dataload = false;
            break;
        case 'I':
            parent.dataload = true;
            // Reading in int arrays
            try {
                int len = dis.readInt();
                if (mpePrefs.DEBUG) System.out.println("Reading int array, size: :  " + len);
                int[] data = new int[len];
                for (int i = 0; i < data.length; i++) {
                    data[i] = dis.readInt();
                }
                //if (mpePrefs.DEBUG) System.out.println("Anything left? " + dis.available());
                parent.newInts = true;
                parent.ints = data;
            } catch (IOException e) {
                e.printStackTrace();
            }
            parent.dataload = false;
            break;*/       
        }
    }

    public void run() {
        while (running) {
            //String msg = null;
            try {
                String input = brin.readLine();
                if (input != null) {
                    read(input);
                } else {
                    killMe();
                    // running = false; ?? or break?
                    break;
                }

                /*try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            } catch (IOException e) {
                System.out.println("Someone left?  " + e);
                killMe();
                break;
            }            
        }

    }

    private void print(String string) {
        System.out.println("Connection: "+string);

    }

    public void killMe(){
        System.out.println("Removing Connection " + clientID);
        parent.killConnection(this);
        parent.drop(clientID);
        running = false;
        if (parent.allDisconected()) parent.resetFrameCount();
    }

    // Trying out no synchronize
    public void send(String msg) {
        if (MPEPrefs.DEBUG) System.out.println("Sending: " + this + ": " + msg);
        try {
        	msg+="\n";
            dos.write(msg.getBytes());
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Trying out no synchronize
    public void sendBytes(byte[] b) {
        if (MPEPrefs.DEBUG) System.out.println("Sending " + b.length + " bytes");
        try {
            dos.writeInt(b.length);
            dos.write(b,0,b.length);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Trying out no synchronize
    public void sendInts(int[] ints) {
        if (MPEPrefs.DEBUG) System.out.println("Sending " + ints.length + " ints");
        try {
            dos.writeInt(ints.length);
            for (int i = 0; i < ints.length; i++) {
                dos.writeInt(ints[i]);
            }
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

