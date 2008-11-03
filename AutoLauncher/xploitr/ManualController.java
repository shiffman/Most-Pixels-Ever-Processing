package xploitr;

import java.io.*;
import java.net.*;

import mpe.config.ConsoleReader;
import mpe.config.FileParser;

public class ManualController {

	static int connectionPort = 9005;
	static String[] connectionHosts;// = {"localhost"}; // Put all of the IP numbers of the hosts in this array.
	static String[] apps;// = {"localhost"}; // Put all of the IP numbers of the hosts in this array.

	public static void main(String[] args) throws IOException, InterruptedException {
		
		ConsoleReader reader = new ConsoleReader(System.in);

		System.out.println("What file do you want to load?");
		System.out.print("%: ");
		String file = reader.readLine();
		init(file);

		ControllerThread[] connections = new ControllerThread[connectionHosts.length];

		System.out.println("Connecting to " + connections.length + " clients");
		for (int i = 0; i < connections.length; i++) {
			connections[i] = new ControllerThread(connectionHosts[i],connectionPort);
			connections[i].start();
		}

		System.out.println("\n\n");

		while (true) {
			menu();
			System.out.print("%: ");
			String line = reader.readLine();
			if (line.trim().equals("k")) {
				System.out.println("Killing app");
				for (int i = 0; i < connections.length; i++) {
					connections[i].broadcast("kill");
				}
			} else {
				try {

					// If we only want to launch on one client
					String[] tokens = line.trim().split(",");
					if (tokens.length > 1) {
						int id = Integer.parseInt(tokens[1]);
						int client = Integer.parseInt(tokens[0]);
						connections[client].broadcast("*" + apps[id]);
					} else {
						int id = Integer.parseInt(line.trim());
						System.out.println("Launching " + apps[id]);
						for (int i = 0; i < connections.length; i++) {
							connections[i].broadcast(apps[id]);
						}
					}
				} catch (Exception e) {
					System.out.println("Error");
				}
			}
			System.out.println(line);
		}
	}


	public static void menu() {

		for (int i = 0; i < apps.length; i++) { 
			System.out.println("Press " + i + " for: " + apps[i]);
		}
		
		System.out.println("To launch on only one host, type host id comma app id");
		for (int i = 0; i < connectionHosts.length; i++) { 
			System.out.println(i + ",#" + " for " + connectionHosts[i]);
		}
		
		System.out.println("Type k to kill.");
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
			for (int i = 0; i < apps.length; i++) { 
				System.out.println("Apps to Launch: " + apps[i]);
			}
		} else {
			System.out.println("Couldn't find ini file.");
			System.exit(0);
		}
	}

}