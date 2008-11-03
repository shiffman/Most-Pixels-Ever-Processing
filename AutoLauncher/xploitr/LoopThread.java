package xploitr;


public class LoopThread extends Thread {

	boolean running = false;
	long startTime;

	public LoopThread() {

	}

	public void start() {
		running = true;
		startTime = System.currentTimeMillis();
		super.start();
	}


	public void anyProject(int i) {
		UberControllerLoop.currentApp = i-1;
		nextProject();
	}
	
	public void kill() {
		for (int i = 0; i < UberControllerLoop.connections.length; i++) {
			UberControllerLoop.connections[i].broadcast("kill");
		}
		quit();
	}

	public void nextProject() {
		System.out.println("Kill!");// + UberControllerLoop.apps[UberControllerLoop.currentApp]);
		UberControllerLoop.currentApp = (UberControllerLoop.currentApp + 1) % UberControllerLoop.apps.length;
		for (int i = 0; i < UberControllerLoop.connections.length; i++) {
			UberControllerLoop.connections[i].broadcast("kill");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Launching " + UberControllerLoop.apps[UberControllerLoop.currentApp]);
		for (int i = 0; i < UberControllerLoop.connections.length; i++) {
			UberControllerLoop.connections[i].broadcast(UberControllerLoop.apps[UberControllerLoop.currentApp]);
		}
		startTime = System.currentTimeMillis();
	}


	public void run() {
		while (running) {
			long now = System.currentTimeMillis();
			if (now - startTime > UberControllerLoop.delay[UberControllerLoop.currentApp]) {
				nextProject();
				UberControllerLoop.menu();
				System.out.print("%: ");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("Interrupting thread");
				//e.printStackTrace();
			}
		}
	}

	public void quit() {
		interrupt();
		running = false;
	}
}
