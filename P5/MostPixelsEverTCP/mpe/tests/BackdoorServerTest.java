package mpe.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * just sends whatever is typed into the console after enter is hit.
 * @author chris
 *
 */
public class BackdoorServerTest {
	Socket socket;
	String host = "localhost";
	int port = 9003;
	InputStream is;
	OutputStream os;
	boolean running=true;
	
	public void run(){
		try {
			socket = new Socket (host,port);
			is = socket.getInputStream();
			os = socket.getOutputStream();
			while(running){
				int in = System.in.read();
				System.out.println("sending "+(char) in);
				os.write(in);
				os.flush();
				if ((char)in == 'q') running = false;
			}
			socket.close();
			System.exit(0);

		} catch (UnknownHostException e1) {
			e1.printStackTrace();
	        System.exit(1);
		} catch (IOException e1) {
			e1.printStackTrace();
	        System.exit(1);
		}
	}
	public static void main(String[] args){
		BackdoorServerTest bst = new BackdoorServerTest();
		bst.run();
	}
}
