package assignments.lab06;

public class CtrlCHandler {
	
	public CtrlCHandler(Server server) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					System.out.println("Server closing");
					server.close();
				} catch (Exception e) {
					System.out.println("An exception has occurred when closing server");
					e.printStackTrace();
					System.exit(1);
				}
			}
		});
	}
}