import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

public class Router extends Thread {

	private InetAddress ipAddr;
	private RoutingTable routingTable;
	private HashMap<InetAddress, Integer> neighbors;
	private static HashMap<InetAddress, Integer> ipToIndex;
	private static HashMap<Integer, InetAddress> indexToIp;
	static LinkedBlockingQueue<RoutingTable> inputQueue= new LinkedBlockingQueue<RoutingTable>();

	/**
	 * @return the ipAddr
	 */
	public InetAddress getIpAddr() {
		return ipAddr;
	}

	/**
	 * @param ipAddr
	 *            the ipAddr to set
	 */
	public void setIpAddr(InetAddress ipAddr) {
		this.ipAddr = ipAddr;
	}

	/**
	 * @return the routingTable
	 */
	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	/**
	 * @param routingTable
	 *            the routingTable to set
	 */
	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}

	/**
	 * @return the neighbors
	 */
	public HashMap<InetAddress, Integer> getNeighbors() {
		return neighbors;
	}

	/**
	 * @param neighbors
	 *            the neighbors to set
	 */
	public void setNeighbors(HashMap<InetAddress, Integer> neighbors) {
		this.neighbors = neighbors;
	}

	public Router() {
		// Initializing the table and neighbors list
		this.routingTable = new RoutingTable();
		this.neighbors = new HashMap<InetAddress, Integer>();
		ipToIndex = new HashMap<InetAddress, Integer>();
		
		indexToIp = new HashMap<Integer, InetAddress>();

		// Assigning the index to IpAddresses
		try {
			ipToIndex.put(InetAddress.getByName("129.21.22.196"), 0);
			ipToIndex.put(InetAddress.getByName("129.21.30.37"), 3);
			ipToIndex.put(InetAddress.getByName("129.21.34.80"), 1);
			ipToIndex.put(InetAddress.getByName("129.21.37.49"), 2);
			
			indexToIp.put(0,InetAddress.getByName("129.21.22.196"));
			indexToIp.put(3,InetAddress.getByName("129.21.30.37"));
			indexToIp.put(1,InetAddress.getByName("129.21.34.80"));
			indexToIp.put(2,InetAddress.getByName("129.21.37.49"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		System.out.println("Please enter the IP Address of the Router Instance");
		Scanner sc = new Scanner(System.in);
		// Ip address of the router as input
		try {
			this.ipAddr = InetAddress.getByName(sc.next());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			System.out.println("Enter the Neighbor IpAddress and the cost to them, separated by space");
			String nbrIpAddr = sc.next();
			Integer cost = sc.nextInt();
			try {
				neighbors.put(InetAddress.getByName(nbrIpAddr), cost);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Do you want to enter another neighbor? (Y/N)");
			String option = sc.next();
			if (option.equals("N") || option.equals("n")) {
				break;
			} else if (option.equals("Y") || option.equals("y")) {
				continue;
			} else {
				System.out.println("Incorrect Choice");
			}
		}

		// make the first tables for each router
		this.routingTable = intialize(ipToIndex.get(ipAddr), neighbors, routingTable,ipAddr);
		printTable(routingTable.getTable());

		// Timer to send routing updates to neighbors
		Timer timer = new Timer();
		timer.schedule(new PropagatorClass(this.ipAddr, this.routingTable, this.neighbors), 1 * 1000, 1);

		while (true) {
			if(inputQueue!= null){
			try {
				synchronized (inputQueue) {
					RoutingTable updatedTable = processReceivedRoutingTable(this.routingTable, inputQueue.take());
					this.routingTable = updatedTable;
					System.out.println("Update for Router with ID:" + routingTable.getId());
					printTable(this.routingTable.getTable());
				}
			} catch (InterruptedException ex) {
			}
			}
		}
	}

	public static synchronized RoutingTable processReceivedRoutingTable(RoutingTable info, RoutingTable nbrInfo) {
		int[][] updatedTable=DVRImpl.applyDVR(info, nbrInfo, ipToIndex);
		info.setTable(updatedTable);
		return info;
	}

	public static int[][] setSameRowsInf(int index, int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (i == index || j == index) {
					matrix[i][j] = Integer.MAX_VALUE;
				}
			}
		}
		return matrix;
	}

	public void addToQueue(RoutingTable nbrRoutingInfo) throws InterruptedException {
		inputQueue.put(nbrRoutingInfo);
	}

	public static RoutingTable intialize(int ind, HashMap<InetAddress, Integer> neighbors, RoutingTable routingTable, InetAddress ipAddr) {
		int[][] returnMat = setSameRowsInf(ind, routingTable.getTable());
		routingTable.setTable(returnMat);
		routingTable.setId(ipAddr);

		if (neighbors != null) {
			Set<InetAddress> nbrsIpSet = neighbors.keySet();
			Iterator<InetAddress> iterator = nbrsIpSet.iterator();

			while (iterator.hasNext()) {
				InetAddress nxtNbrIp = iterator.next();
				Integer cost = neighbors.get(nxtNbrIp);
				int index = ipToIndex.get(nxtNbrIp);
				int[][] table = routingTable.getTable();
				table[index][index] = cost;
			}
		}

		return routingTable;
	}

	public static void printTable(int[][] table) {
		System.out.println("Destination Ip --> SubnetMask --> Next Hop      --> Cost");
		
		for (int i = 0; i < table.length; i++) {
			int min=Integer.MAX_VALUE;
			int minIndex = Integer.MAX_VALUE;
			for (int j = 0; j < table.length; j++) {
				if(min>table[i][j] && table[i][j]!=0){
					min=table[i][j];
					minIndex=j;
				}
			}
			if(min==Integer.MAX_VALUE){
				System.out.print("INF\t"+"    --> "+"255.255.255.0 --> "+ "INF\t"+"        --> "+ "INF\t");System.out.println("");
			}else{
			System.out.print(getCIDR(indexToIp.get(i).toString())+" --> "+"255.255.255.0 --> "+ indexToIp.get(minIndex).toString()+" "+ "--> "+ min);
			System.out.println("");
			}
			
		}
		
		
	/*	for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table.length; j++) {
				System.out.print(table[i][j] + " ");
			}
			System.out.println("");
		}*/
		System.out.println("------/-------");
	}

	
	public static String getCIDR(String ip){
		ip=ip.substring(1);
		StringBuilder sb= new StringBuilder();
		String[] arr=ip.split("\\.");
		for (int i = 0; i < arr.length-1; i++) {
			sb.append(arr[i]);
			sb.append(".");
		}
		return sb.toString()+"0";
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		Router router = new Router();
		Thread thrd = new Thread(router, "1");
		thrd.start();

		// initialize the instance of listener socket class
		ListenerClass listener = new ListenerClass(router);
		Thread thrd2 = new Thread(listener, "2");
		thrd2.start();
		
	}
}
