import java.io.Serializable;
import java.net.InetAddress;

public class RoutingTable implements Serializable{
	
	/**
	 * SerialVersion ID
	 */
	private static final long serialVersionUID = 1L;

	private InetAddress id;
	
	public int[][] table;
	
	public RoutingTable() {
		//Initializing the routing table for each router
		this.table=new int[4][4];
	}

	/**
	 * @return the id
	 */
	public InetAddress getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(InetAddress id) {
		this.id = id;
	}

	/**
	 * @return the table
	 */
	public int[][] getTable() {
		return table;
	}

	/**
	 * @param table the table to set
	 */
	public void setTable(int[][] table) {
		this.table = table;
	}

	
}
