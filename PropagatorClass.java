import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

public class PropagatorClass extends TimerTask{

	Socket toNbrs;
	InetAddress ipAddress;
	RoutingTable routingTable;
	HashMap<InetAddress, Integer> neighbors;
	
	public PropagatorClass(InetAddress IpAddress,RoutingTable updatedTable, HashMap<InetAddress, Integer> neighbors){
		this.ipAddress=IpAddress;
		this.routingTable=updatedTable;
		this.neighbors=neighbors;
		this.run();
	}

	private void sendRoutingInfo(InetAddress ipAddress, RoutingTable routingTable, int port) throws UnknownHostException, IOException {
		toNbrs = new Socket(ipAddress,5555);
		ObjectOutputStream outputRoutingTable = new ObjectOutputStream(toNbrs.getOutputStream());
		outputRoutingTable.writeObject(routingTable);
		toNbrs.close();
	}

	@Override
	public void run() {
		this.execute(this.ipAddress, this.routingTable, this.neighbors);
	}
	
	public void execute(InetAddress IpAddress,RoutingTable updatedTable, HashMap<InetAddress, Integer> neighbors){
		Set<InetAddress> keySet= neighbors.keySet();
		Iterator<InetAddress> iterator = keySet.iterator();
		while(iterator.hasNext()){
			InetAddress destIp= iterator.next();
			try {
				sendRoutingInfo(destIp,updatedTable,33);
			} catch (IOException e) {
			}
		}
	}
	
}
