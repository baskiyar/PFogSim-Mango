package edu.auburn.pFogSim.Radix;
/*
 * For those who come after...
 * 
 * If you are here to change the puddle orchestrator to the true HAFA architecture, (i.e. change it from optimized 
 * on distance to optimized on cost) then this is where the changes are needed. In order to make the true HAFA 
 * architecture DistRadix needs to sort on cpu cost + network cost instead distance. Fortunately, all the required 
 * information is in the EdgeHost so it should be easy to calculate and can be swapped out for distance. Unfortunately,
 * we are using this for distance in some other places. So there are two reasonable courses of action:
 * 
 * A. (not recommended) Find all places other than puddles that uses DistRadix and rewrite them. Then change radix 
 * 		to work on cost.
 * 
 * B. Add the ability to sort off of cost or distance and give option to choose your "comparator". Then adjust the method calls
 * 		to use the appropriate sorting metric.
 * 
 * Whatever you decide, the methods that make distance the metric are buildCoords() and buildDist(). I would
 * suggest studying these methods to understand what they do in order to implement cost as the sorting metric.
 * @jih0007 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedList;

import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;


/**
 * Class for implementing Radix sort for find the closest nodes
 * @author Jacob I Hall jih0007@auburn.edu
 * @author Shehenaz Shaik
 * @author Qian Wang
 */
public class DistRadix {
	
	private ArrayList<EdgeHost> input;
	private TreeMap<Location, EdgeHost> coordMap;
	private HashMap<Double, Location> distMap;
	//private HashMap<Double, Location> latencyMap;//Qian: added for sort node by latency.
	private HashMap<Double, ArrayList<Location>> latencyMap; //Qian: modified for new requirement
	private Location ref;
	private ArrayList<Location> coords;
	private ArrayList<Integer> distances;
	//private double[] latencies; // Shaik added
	private int[] arrgs;
	
	
	/**
	 * constructor
	 * @param in  
	 * @param pair
	 */
	public DistRadix(List<EdgeHost> in, Location pair) {
		input = new ArrayList<EdgeHost>();
		coordMap = new TreeMap<Location, EdgeHost>();
		distMap = new HashMap<Double, Location>();
		latencyMap = new HashMap<>();//Qian added for latency
		coords = new ArrayList<Location>();
		distances = new ArrayList<Integer>();
		for (EdgeHost node : in) {
			input.add(node);
		}
		ref = pair;
	}
	
	
	/**
	 * map coords to nodes
	 */
	private void buildCoords() {
		for(EdgeHost node : input) {
			coordMap.put(new Location(node.getLocation().getXPos(), node.getLocation().getYPos(), node.getLocation().getAltitude()), node);
			coords.add(new Location(node.getLocation().getXPos(), node.getLocation().getYPos(),node.getLocation().getAltitude()));
		}
	}
	
	
	/**
	 * map distances to coords
	 */
	private void buildDist() {
		double dist = 0;
		for (Location loc : coords) {
			//dist = Math.sqrt((Math.pow(ref.getXPos() - loc.getXPos(), 2) + Math.pow(ref.getYPos() - loc.getYPos(), 2)));
			dist = DataInterpreter.measure(ref.getYPos(), ref.getXPos(), ref.getAltitude(), loc.getYPos(), loc.getXPos(), loc.getAltitude());
			dist = Math.floor(dist);
			while(distMap.keySet().contains(dist)) {
				dist += 0.001;
			}
			distMap.put(dist, loc);
			distances.add((int) (dist * 1000));
		}
	}
	
	
	/**
	 * @author Shaik
	 * modified by Qian
	 * map latency to coords
	 */
	private void buildLatencyMap() {
		double latency = 0;
		int index=0;
		//latencies = new double[coords.size()];
		for (Location loc: coords) {
			latency = ((ESBModel)SimManager.getInstance().getNetworkModel()).getDleay(ref, loc);
			// Shaik *** this may overwrite the previous entry with same latency. hence, entry-value should be a list of locs(of nodes) with same latency, rather than a single loc.
			//SimLogger.printLine("Latency: " + latency+"  Index: "+index);
			if (latencyMap.containsKey(latency)) {
				latencyMap.get(latency).add(loc);
			}
			else {
				ArrayList<Location> tempList = new ArrayList<>();
				tempList.add(loc);
				latencyMap.put(latency, tempList);
			}
			//latencies[index++] = latency;
		}
	}
	
	
	/**
	 * set the arrgs array
	 */
	private void setArrgs() {
		arrgs = new int[distances.size()];
		for (int i = 0; i < distances.size(); i++) {
			arrgs[i] = distances.get(i);
		}
	}
	
	
	/**
	 * find the max of the arrgs array
	 * @return
	 */
	private int maxArrg() {
		int max = 0;
		for (int i = 0; i < arrgs.length; i++) {
			if (arrgs[i] > max) {
				max = arrgs[i];
			}
		}
		return max;
	}
	
	
	/**
	 * perform counting sort
	 * @param arr
	 * @param n
	 * @param exp
	 */
	private void countSort(int arr[], int n, int exp)
    {
        int output[] = new int[n];//i don't have time to explain counting sort to you
        int i;					  //this is a private class fam... let it be
        int count[] = new int[10];
        Arrays.fill(count, 0);
 
        for (i = 0; i < n; i++) {
            count[ (arr[i]/exp)%10 ]++;
        }
 
        for (i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }
 
        for (i = n - 1; i >= 0; i--){
            output[count[ (arr[i]/exp)%10 ] - 1] = arr[i];
            count[ (arr[i]/exp)%10 ]--;
        }
 
        for (i = 0; i < n; i++) {
            arr[i] = output[i];
        }
    }
	
	
	/**
	 * perform radix sort
	 */
	private void radixSort() {
		int max = maxArrg();
		for (int i = 1; max/i > 0; i*=10) {
			countSort(arrgs, arrgs.length, i);
		}
	}
	
	
	/**
	 * get the list sorted by distance
	 * @return
	 */
	private LinkedList<EdgeHost> getList() {
		LinkedList<EdgeHost> output = new LinkedList<EdgeHost>();
		double dist = 0.0;
		Location loc;
		EdgeHost node;
		for (int i = 0; i < arrgs.length; i++) {
			dist = arrgs[i]/1000;
			loc = distMap.get(dist);
			node = coordMap.get(loc);
			output.add(node);
		}
		return output;
	}
	
	
	/**
	 * @author Shaik
	 * modified by Qian
	 * get the list sorted by latency
	 * @return
	 */
	private LinkedList<EdgeHost> getLatenciesList() {
		LinkedList<EdgeHost> output = new LinkedList<EdgeHost>();
		EdgeHost node;
		for (Map.Entry<Double, ArrayList<Location>> entry: latencyMap.entrySet()) {
			for (Location loc: entry.getValue()) {
				node = coordMap.get(loc);
				output.add(node);
				System.out.println("Latency Key is: "+entry.getKey());
			}
		}
//		for (int i = 0; i < coords.size(); i++) {
//			loc = latencyMap.get(latencies[i]); // Shaik *** When the entry-value is implemented as a 'loc' list - ensure that this operation removes the element after reading the value, so that next request for same latency key will return a different node accessible at same latency. Otherwise, only the first node in list will be returned always and not all nodes will be considered in assignHost() method.  
//			node = coordMap.get(loc);   
//			output.add(node);
//		}
		return output;
	}
	
	
	/**
	 * @author Shaik
	 * modified by Qian
	 * get the list of nodes sorted by latency
	 * @return
	 */
	private LinkedList<EdgeHost> getSortedNodesListByLatency() {

		//Return: List of nodes
		LinkedList<EdgeHost> output = new LinkedList<EdgeHost>();
		
		//Create a sorted list of latencies
		List<Double> latenciesList = new ArrayList<Double> (latencyMap.keySet());
		Collections.sort(latenciesList);
		
		//Access the map entries in order sorted by latencies
		EdgeHost node;
		//for (Map.Entry<Double, ArrayList<Location>> entry: latencyMap.entrySet()) {
		for (Double lat : latenciesList) {
			// Get the list of locations accessible at that latency
			for (Location loc: latencyMap.get(lat)) {
				node = coordMap.get(loc);
				output.add(node);
				//System.out.println("Latency Key is: "+lat);
			}
		}
//		for (int i = 0; i < coords.size(); i++) {
//			loc = latencyMap.get(latencies[i]); // Shaik *** When the entry-value is implemented as a 'loc' list - ensure that this operation removes the element after reading the value, so that next request for same latency key will return a different node accessible at same latency. Otherwise, only the first node in list will be returned always and not all nodes will be considered in assignHost() method.  
//			node = coordMap.get(loc);   
//			output.add(node);
//		}
		return output;
	}
	
	
	/**
	 * public facing method to get the list of sorted nodes
	 * @return
	 */
	public LinkedList<EdgeHost> sortNodes() {
		buildCoords();
		buildDist();
		setArrgs();
		radixSort();
		return getList();
	}
	
	
	/**
	 * @author Qian / Shaik
	 * public method to get list of sorted nodes by latency
	 * @return
	 */
	public LinkedList<EdgeHost> sortNodesByLatency() {
		buildCoords();
		//builtLatency();
		buildLatencyMap(); // Shaik update
		//setArrgs(); // Shaik update
		// radixSort(); // Shaik update
		//Arrays.sort(latencies); // Shaik update
		//return getLatenciesList(); // Shaik update
		return getSortedNodesListByLatency(); // Shaik modified
	}
	
	
	/**
	 * @return the input
	 */
	public ArrayList<EdgeHost> getInput() {
		return input;
	}
	
	
	/**
	 * @param input the input to set
	 */
	public void setInput(ArrayList<EdgeHost> input) {
		this.input = input;
	}
	
	
	/**
	 * @return the coordMap
	 */
	public TreeMap<Location, EdgeHost> getCoordMap() {
		return coordMap;
	}
	
	
	/**
	 * @param coordMap the coordMap to set
	 */
	public void setCoordMap(TreeMap<Location, EdgeHost> coordMap) {
		this.coordMap = coordMap;
	}
	
	
	/**
	 * @return the distMap
	 */
	public HashMap<Double, Location> getDistMap() {
		return distMap;
	}
	
	
	/**
	 * @param distMap the distMap to set
	 */
	public void setDistMap(HashMap<Double, Location> distMap) {
		this.distMap = distMap;
	}
	
	
	/**
	 * @return the latencyMap
	 */
	public HashMap<Double, ArrayList<Location>> getLatencyMap() {
		return latencyMap;
	}
	
	
	/**
	 * @param latencyMap the latencyMap to set
	 */
	public void setLatencyMap(HashMap<Double, ArrayList<Location>> latencyMap) {
		this.latencyMap = latencyMap;
	}
	
	
	/**
	 * @return the ref
	 */
	public Location getRef() {
		return ref;
	}
	
	
	/**
	 * @param ref the ref to set
	 */
	public void setRef(Location ref) {
		this.ref = ref;
	}
	
	
	/**
	 * @return the coords
	 */
	public ArrayList<Location> getCoords() {
		return coords;
	}
	
	
	/**
	 * @param coords the coords to set
	 */
	public void setCoords(ArrayList<Location> coords) {
		this.coords = coords;
	}
	
	
	/**
	 * @return the distances
	 */
	public ArrayList<Integer> getDistances() {
		return distances;
	}
	
	
	/**
	 * @param distances the distances to set
	 */
	public void setDistances(ArrayList<Integer> distances) {
		this.distances = distances;
	}
	
	
	/**
	 * @return the arrgs
	 */
	public int[] getArrgs() {
		return arrgs;
	}
	
	
	/**
	 * @param arrgs the arrgs to set
	 */
	public void setArrgs(int[] arrgs) {
		this.arrgs = arrgs;
	}
}
