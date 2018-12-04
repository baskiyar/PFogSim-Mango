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
import java.util.List;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedList;

import edu.auburn.pFogSim.util.DataInterpreter;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.utils.Location;
/**
 * Class for implementing Radix sort for find the closest nodes
 * @author Jacob I Hall jih0007@auburn.edu
 */
public class DistRadix {
	
	private ArrayList<EdgeHost> input;
	private TreeMap<Location, EdgeHost> coordMap;
	private HashMap<Double, Location> distMap;
	private Location ref;
	private ArrayList<Location> coords;
	private ArrayList<Integer> distances;
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
			coordMap.put(new Location(node.getLocation().getXPos(), node.getLocation().getYPos()), node);
			coords.add(new Location(node.getLocation().getXPos(), node.getLocation().getYPos()));
		}
	}
	/**
	 * map distances to coords
	 */
	private void buildDist() {
		double dist = 0;
		for (Location loc : coords) {
			//dist = Math.sqrt((Math.pow(ref.getXPos() - loc.getXPos(), 2) + Math.pow(ref.getYPos() - loc.getYPos(), 2)));
			dist = DataInterpreter.measure(ref.getYPos(), ref.getXPos(), loc.getYPos(), loc.getXPos());
			dist = Math.floor(dist);
			while(distMap.keySet().contains(dist)) {
				dist += 0.001;
			}
			distMap.put(dist, loc);
			distances.add((int) (dist * 1000));
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
	 * get the sorted list
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
}
