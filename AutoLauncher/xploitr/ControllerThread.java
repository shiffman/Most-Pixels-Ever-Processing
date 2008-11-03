package xploitr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ControllerThread extends Thread {

	Socket socket;
	PrintWriter out = null;
	BufferedReader in = null;
	boolean running;


	public ControllerThread(String connectionHost, int connectionPort) {
		try {
			socket = new Socket(connectionHost, connectionPort);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Can't find host.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection.");
			System.exit(1);
		}
	}

	public void start() {
		running = true;
		super.start();
	}
	
	public void broadcast(String msg) {
		out.println(msg);
	}

	public void run() {
		String fromServer;
		while (running) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void quit() {
		interrupt();
		running = false;
	}
}
