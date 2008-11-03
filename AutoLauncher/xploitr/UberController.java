package xploitr;

import java.io.*;
import java.net.*;

import mpe.config.ConsoleReader;
import mpe.config.FileParser;

public class UberController {

	static int connectionPort = 9005;
	static String[] connectionHosts;// = {"localhost"}; // Put all of the IP numbers of the hosts in this array.
	static String[] apps;// = {"localhost"}; // Put all of the IP numbers of the hosts in this array.
	static int[] delay;
	static int currentApp = 0;

	public static void main(String[] args) throws IOException, InterruptedException {

		ConsoleReader reader = new ConsoleReader(System.in);

		System.out.println("What file do you want to load?");
		System.out.print("%: ");
		String line = reader.readLine();
		init(line);

		ControllerThread[] connections = new ControllerThread[connectionHosts.length];

		System.out.println("Connecting to " + connections.length + " clients");
		for (int i = 0; i < connections.length; i++) {
			connections[i] = new ControllerThread(connectionHosts[i],connectionPort);
			connections[i].start();
		}

		System.out.println("Starting first app");
		for (int i = 0; i < connections.length; i++) {
			connections[i].broadcast(apps[currentApp]);
		}

		long startTime = System.currentTimeMillis();

		boolean running = true;

		while (running) {
			long now = System.currentTimeMillis();
			if (now - startTime > delay[currentApp]) {
				for (int i = 0; i < connections.length; i++) {
					connections[i].broadcast("kill");
				}
				System.out.println("Killing " + apps[currentApp]);
				currentApp = currentApp + 1;
				if (currentApp == apps.length) {
					running = false;
					break;
				}
				Thread.sleep(1000);
				System.out.println("Launching " + apps[currentApp]);
				for (int i = 0; i < connections.length; i++) {
					connections[i].broadcast(apps[currentApp]);
				}
				startTime = System.currentTimeMillis();
			}
			Thread.sleep(1000);
		}
		Thread.sleep(5000);
		System.out.println("Quitting");
	}

	public static void init(String file) {
		FileParser fp = new FileParser(file);
		if (fp.fileExists()) {
			String hosts = fp.getStringValue("hosts");
			connectionHosts = hosts.split(",");
			for (int i = 0; i < connectionHosts.length; i++) {
				System.out.println("Client IP: " + connectionHosts[i]);
			}
			String appls = fp.getStringValue("apps");
			apps = appls.split(",");
			String delayString = fp.getStringValue("delay");
			String[] delayArray = delayString.split(",");
			delay = new int[delayArray.length];
			for (int i = 0; i < delay.length; i++) {
				delay[i] = Integer.parseInt(delayArray[i])*1000;
			}

			for (int i = 0; i < apps.length; i++) { 
				System.out.println("Apps to Launch: " + apps[i] + " " + delay[i]);
			}
		} else {
			System.out.println("Couldn't find ini file.");
			System.exit(0);
		}
	}

}