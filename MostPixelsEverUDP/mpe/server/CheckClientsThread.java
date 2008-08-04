
/**
 * The MPE "Send All" Thread
 * <http://mostpixelsever.com>
 * @author Shiffman
 */

package mpe.server;


public class CheckClientsThread extends Thread {

	UDPServer parent;
	boolean running = false;
	
	public CheckClientsThread(UDPServer p) {
		parent = p;
	}

	public void start() {
		running = true;
		super.start();
	}

	public void run() {
		while (running) {
			try {
				Thread.sleep(UDPServer.disconnectTime);
			} catch (InterruptedException e) {
				//System.out.println("Interrupting!");
				//e.printStackTrace();
			}
			// Check all clients
			// if (mpePrefs.DEBUG) System.out.println("Check to see if anyone disconnected.");
			for (int i = 0; i < parent.connections.length; i++) {
				Connection c = parent.connections[i];
				if (c != null && c.isDead()) {
					c.killMe();
				}
			}
		}
	}

	public void quit() {
		if (mpePrefs.DEBUG) System.out.println("Stopping client checking thread.");
		running = false; // Setting running to false ends the loop in run()
		interrupt(); // In case the thread is waiting. . .
	}
}