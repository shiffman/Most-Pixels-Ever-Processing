package xploitr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mpe.config.FileParser;

public class ClientLauncher {

	static String path = "/Users/daniel/Desktop/bigscreens/";
	static int listenPort = 9005;

	static String lastExecuted = "";
	static int textDelay = 5000;

	public static void main(String[] args) throws IOException {

		init();
		while (!server()) {
			System.out.println("Restarting Server!");
		}

		System.out.println("Quitting");

	}

	public static boolean server() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(listenPort);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + listenPort + ".");
			System.exit(1);
		}

		System.out.println("Waiting for controller.");
		Socket clientSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			clientSocket = serverSocket.accept();
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			System.out.println("Controller connected.");

			String inputLine, outputLine;
			//outputLine = "Success.";
			//out.println(outputLine);
			while ((inputLine = in.readLine()) != null) {
				System.out.println("Controller says: "  + inputLine);
				if (inputLine.indexOf("kill") > -1) {
					killProgram();
				} else {
					if (inputLine.charAt(0) == '*') {
						runProgram(inputLine.substring(1,inputLine.length()),false);
					} else {
						runProgram(inputLine,true);
					}
				}
			}

			out.close();
			in.close();
			clientSocket.close();
			serverSocket.close();
			return false;
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}
		return true;
	}

	public static void runProgram(String toExecute, boolean text) {
		System.out.println("Launching " + toExecute);
		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec("open " + path + toExecute);
			lastExecuted = toExecute;

			Thread.sleep(500);
			if (text) {
				String s = toExecute.substring(0,toExecute.indexOf("/",1));
				rt.exec("open " + path + s + "/TextIntro.app");
				Thread.sleep(textDelay);
				killText();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void killProgram() {
		System.out.println("Killing app");
		try {
			Process p = Runtime.getRuntime().exec("ps -Aww");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			while ((str = in.readLine()) != null)  {
				//if (str.indexOf("bigscreens") > 0) {
				if (str.indexOf(lastExecuted) > 0) {
					System.out.println(str);
					break;
				}
			}
			//String id = str.substring(0,str.indexOf("  "));
			//Pattern pat = Pattern.compile("\\s+(\\d++)\\s??");
			Pattern pat = Pattern.compile("(\\d++)\\s??");
			Matcher m = pat.matcher(str);
			m.find();
			String id = m.group(1);//str.substring(0,str.indexOf("  "));
			System.out.println(id.trim());
			Runtime rt = Runtime.getRuntime();
			rt.exec("kill " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void killText() {
		System.out.println("Killing text intro");
		try {
			Process p = Runtime.getRuntime().exec("ps -Aww");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			while ((str = in.readLine()) != null)  {
				if (str.indexOf("TextIntro") > 0) {
					System.out.println(str);
					break;
				}
			}
			Pattern pat = Pattern.compile("(\\d++)\\s??");
			Matcher m = pat.matcher(str);
			m.find();
			String id = m.group(1);//str.substring(0,str.indexOf("  "));
			System.out.println(id.trim());
			Runtime rt = Runtime.getRuntime();
			rt.exec("kill " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void checkProgram() {
		System.out.println("Checking if running");
		try {
			Process p = Runtime.getRuntime().exec("ps -Aww");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			boolean found = false;
			while ((str = in.readLine()) != null)  {
				if (str.indexOf(lastExecuted) > 0) {
					System.out.println("Still running: " +  str);
					found = true;
					break;
				}
			}
			if (!found) {
				System.out.println("Not found! Relaunch!");
				runProgram(lastExecuted,false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void init() {
		FileParser fp = new FileParser("client.ini");
		if (fp.fileExists()) {
			path = fp.getStringValue("path");
			System.out.println("Path is: " + path);
			listenPort = fp.getIntValue("port");
			System.out.println("Port is: " + listenPort);
			textDelay = fp.getIntValue("text")*1000;
			System.out.println("Text Delay: " + textDelay);

		} else {
			System.out.println("Couldn't find ini file.");
		}
	}
}