import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

class ListenerClass extends Thread {

	ServerSocket serverSoc;
	Socket nbrSoc;
	Router router;

	public ListenerClass(Router router) {
		System.out.println("entered in the listener constructor");
		this.router=router;
		try {
			serverSoc = new ServerSocket(5555);
		} catch (IOException e) {
			System.out.println("Cannot create server socket");
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			while (true) {
				nbrSoc = serverSoc.accept();
				ObjectInputStream inputRoutingTable= new ObjectInputStream(nbrSoc.getInputStream());
				RoutingTable tab;
				try {
					tab = (RoutingTable) inputRoutingTable.readObject();
						synchronized(router){
							try {
								router.addToQueue(tab);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}