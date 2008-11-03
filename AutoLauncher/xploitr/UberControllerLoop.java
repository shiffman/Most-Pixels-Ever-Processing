package xploitr;

import java.io.*;
import java.net.*;

import mpe.config.ConsoleReader;
import mpe.config.FileParser;

public class UberControllerLoop {

	static int connectionPort = 9005;
	static String[] connectionHosts;// = {"localhost"}; // Put all of the IP numbers of the hosts in this array.
	static String[] apps;// = {"localhost"}; // Put all of the IP numbers of the hosts in this array.
	static int[] delay;
	static int currentApp = 0;
	static ControllerThread[] connections;

	public static void main(String[] args) throws IOException, InterruptedException {

		ConsoleReader reader = new ConsoleReader(System.in);

		System.out.println("What file do you want to load?");
		System.out.print("%: ");
		String line = reader.readLine();
		init(line);

		System.out.println("Where in the loop do you want to start?");
		System.out.print("%: ");
		line = reader.readLine();

		try {
			currentApp = Integer.parseInt(line);
			if (currentApp < 0) currentApp = 0;
			if (currentApp > apps.length-1) currentApp = apps.length-1;
			System.out.println("Starting at: " + currentApp);
		} catch (Exception e) {
			System.out.println("That's not a number.");
		}


		connections = new ControllerThread[connectionHosts.length];

		System.out.println("Connecting to " + connections.length + " clients");
		for (int i = 0; i < connections.length; i++) {
			connections[i] = new ControllerThread(connectionHosts[i],connectionPort);
			connections[i].start();
		}

		System.out.println("Starting first app");
		for (int i = 0; i < connections.length; i++) {
			connections[i].broadcast(apps[currentApp]);
		}


		boolean running = true;

		LoopThread lt = new LoopThread();
		lt.start();


		while (running) {
			menu();
			System.out.print("%: ");
			line = reader.readLine().trim();
			if (line.equals("q")) {
				lt.kill();
				lt.quit();
				running = false;
			} else if (line.equals("n")) {
				lt.nextProject();
			} else {
				try {
					int i = Integer.parseInt(line);
					if (i < 0) i = 0;
					if (i > apps.length-1) i = apps.length-1;
					System.out.println("Skipping to: " + i);
					lt.anyProject(i);
				} catch (Exception e) {
					System.out.println("Invalid command.");
				}
				
			}
		}
		
		System.out.println("finished");
		System.exit(0);


	}


	public static void menu() {
		System.out.println("\n\n");
		for (int i = 0; i < apps.length; i++) { 
			System.out.println("Press " + i + " for: " + apps[i]);
		}
		System.out.println("Type n to skip to next project.");
		System.out.println("Type q to quit.");
		System.out.println("Type # to skip to any project.");
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
				System.out.println(i + ": " + apps[i] + " " + delay[i]/1000);
			}
		} else {
			System.out.println("Couldn't find ini file.");
			System.exit(0);
		}
	}

}