import java.net.InetAddress;
import java.util.HashMap;

public class DVRImpl {

	
	public static int[][] applyDVR(RoutingTable currentRouter, RoutingTable neighbor,HashMap<InetAddress, Integer> ipToIndex){
		int index=ipToIndex.get(currentRouter.getId());
		int[][] table = currentRouter.getTable();
		
		int nbrIndex= ipToIndex.get(neighbor.getId());	
		int[][] nbrTable = neighbor.getTable();

		//Finding the minimum cost to current router in nbr routing table
		int minCost=-1;
		
		minCost=nbrTable[index][index];
		
		
		int[] distanceVector = new int[4];
		
		for (int i = 0; i < nbrTable.length; i++) {
			int minCostForRow=-1;
			int row=-1,col=-1;
			for (int j = 0; j < nbrTable.length; j++) {
				if(!(i==nbrIndex || j==nbrIndex)){
					if(minCostForRow<0 && nbrTable[i][j]!=0){
						minCostForRow=nbrTable[i][j];
						row=i;
						col=j;
					}else if(nbrTable[i][j]<minCostForRow && nbrTable[i][j]!=0){
						minCostForRow=nbrTable[i][j];
						row=i;
						col=j;
					}
				}
			}
			if(minCostForRow>0){
				if(minCostForRow==table[nbrIndex][nbrIndex]){
					if(row!=col){
						distanceVector[i]=minCostForRow+minCost;
					}
				}
				else if(minCostForRow!=table[nbrIndex][nbrIndex])
					distanceVector[i]=minCostForRow+minCost;
			}
		}
		
		//updating the current router table
		for (int i = 0; i < table.length; i++) {
			if(distanceVector[i]>0 && table[i][nbrIndex]==0){
				table[i][nbrIndex]=distanceVector[i];
			}else if(table[i][nbrIndex]!=0 && distanceVector[i]==0){
				continue;
			}else if(table[i][nbrIndex]!=0 && distanceVector[i]!=0 && table[i][nbrIndex]!=Integer.MAX_VALUE){
				table[i][nbrIndex]=Math.min(table[i][nbrIndex], distanceVector[i]);
			}
		}
		
		return table;
	}
	
}
