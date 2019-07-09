package edu.auburn.pFogSim.clustering;

import java.io.PrintWriter;
//import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

import org.cloudbus.cloudsim.core.CloudSim;

import javafx.util.Pair;
//import java.util.HashMap;

//import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
//import edu.boun.edgecloudsim.utils.*;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.DataInterpreter;


/**
 * 
 * @author szs0117
 *
 */
public class FogCluster {
	private String[] lines = null;
	private Double[][] points = null;
	private double[][] proximityMatrix = null;
	private double maxClusterHeight;// Qian: add for set max distance or latency for clustering.
	private int clusterNumber;// = 20; // Defines number of clusters to generate.
	private Double[][][] cluster = new Double[clusterNumber][][];
	private int level;
	
	
	/**
	 * @return the cluster
	 */
	public Double[][][] getCluster() {
		return cluster;
	} // end getCluster


	/*
	 * Method - stdInput from EdgeServerManager
	 * 
	 */
	public void stdInput(ArrayList<Location> arrayList) 
	{
		
		ArrayList<Double[]> _points = new ArrayList<Double[]>();
		
		for(Location pair : arrayList)
		{
			Double[] point = new Double[3];
			point[0] = pair.getXPos();
			point[1] = pair.getYPos();
			point[2] = pair.getAltitude();
			
			_points.add(point);
		}
		this.points = (Double[][])_points.toArray(new Double[_points.size()][]);
		
	}
		
	
	/**
	 *  Method - csvInput
	 * 
	 */
	/*public void csvInput(String fn){
		try
		{
			java.util.List lines = new ArrayList(); 
			java.util.List points = new ArrayList();
			
			// Read data points from DataSet file 
			BufferedReader reader = new BufferedReader(new FileReader(fn));
			String line;
			while ((line = reader.readLine())!=null){
				lines.add(line);
				
				String[] pointString = line.split(",");
				Integer[] point = new Integer[2];
				point[0] = Integer.parseInt(pointString[0].trim());
				point[1] = Integer.parseInt(pointString[1].trim());
				
				System.out.println(point[0].getClass()+","+point[1]);
				points.add(point);
				
			}// end while

			this.points = (Double[][])points.toArray(new Integer[points.size()][]);
			
			reader.close();
		} catch (Exception e){
			e.printStackTrace(System.err);
		}
		
	}//end csvInput()
*/	
	
	
	/**
	 * @param clusterNumber the clusterNumber to set
	 */
	public void setClusterNumber(int clusterNumber) {
		this.clusterNumber = clusterNumber;
	}
	
	
	/**
	 * @author Qian
	 * set max distance or latency for cluster
	 * @param max
	 */
	public void setClusterHeight(double max) {
		this.maxClusterHeight = max;
	}


	/**
	 * This method calculates Proximity matrix.
	 *  
	 */
	void calcProximity(){
		
		double x1=0.0, y1=0.0, x2=0.0, y2=0.0,a1=0,a2=0;
		double distance, delay;
		Location first;
		Location second;
		NodeSim firstNode, secondNode, current, nextHop;
		
		// assume n data points ; n = size
		// declare an nxn array of double type
		
		int n = points.length;
		//System.out.println("Number of points: "+n);
		
		proximityMatrix = new double[n][n];
		
		if(SimSettings.getInstance().getClusterType()) {//Qian changed for cluster type. {TRUE - Distance; FALSE - Latency}
			System.out.println("Populating proximity matrix with distances.");
			for (int i=0; i<n-1; i++){// distance based
				// First point
				x1 = points[i][0];
				y1 = points[i][1];
				a1 = points[i][2];
				
				for (int j=i; j<n; j++){
					//Second point
					x2 = points[j][0];
					y2 = points[j][1];
					a2 = points[j][2];
					
					//Calculate distance
					//distance = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
					distance = DataInterpreter.measure(x1, y1,a1, x2, y2,a2); //Qian added
					//--printlater--System.out.print(distance+" , ");
					
					//Update entry in proximityMatrix
					proximityMatrix[i][j] = distance;
					proximityMatrix[j][i] = distance;
					
				}// end for j
				//--printlater--System.out.println();
			}//end for i
		}
		else {
			// Qian added for cluster type
			for (int i = 0; i < n; i++) {// latency based.
				x1 = points[i][0];
				y1 = points[i][1];
				a1 = points[i][2];
				first = new Location(x1, y1, a1);
				firstNode = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(first, false);
				delay = 0;
				//delay = ((ESBModel)SimManager.getInstance().getNetworkModel()).getCongestionDelay(first, CloudSim.clock());
				for (int j = 0; j < n; j++) {
					x2 = points[j][0];
					y2 = points[j][1];
					a2 = points[j][2];
					
					second = new Location(x2, y2, a2);
					secondNode = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(second, false);
					LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(firstNode, secondNode);
					while (!path.isEmpty()) {
						current = path.poll();
						nextHop = path.peek();
						if (nextHop == null) {
							break;
						}
						if (current.traverse(nextHop) < 0) {
							SimLogger.printLine("not adjacent");
						}
						double proDelay = current.traverse(nextHop);
						//double conDelay = ((ESBModel)SimManager.getInstance().getNetworkModel()).getCongestionDelay(nextHop.getLocation(), CloudSim.clock() + delay);
						delay += proDelay;
						proximityMatrix[i][j] = delay;
					}
				}
			}
		}
		writeMatricToFile(SimSettings.getInstance().getClusterType(), level, n);
		
	}//end calcProximity()

	
	/**
	 * @author szs0117
	 * Create clusters
	 * @param 
	 */	
	public void learnByMaxHeight(){
		
		HierarchicalClustering hc = new HierarchicalClustering(new CompleteLinkage(proximityMatrix));
		
		//Create clusters
		int[] membership = hc.partition(this.maxClusterHeight);
		// membership[a]=b, signifies that point 'a' belongs to cluster 'b' 

		clusterNumber = membership.length;  // Shaik added
		
		// Get member count of each of the clusters
		int[] clusterSize = new int[clusterNumber];
		for (int i=0; i< membership.length; i++){
			clusterSize[membership[i]]++;
			SimLogger.printLine("i membership[i] clusterSize: "+i+"   "+membership[i]+"   "+clusterSize[membership[i]]);
		} 
		
		// Get the number of clusters with >=1 members
		for(int i=0; i < membership.length; i++) {
			SimLogger.printLine("Cluster id: "+i+" ClusterSize: "+clusterSize[i]);	
			
			// Identify the number of clusters formed based on specified criteria of maximum height (distance/latency)
			if (clusterSize[i]== 0) {
				clusterNumber = i;
				break;
			}
		}
		SimLogger.printLine("Identified number of clusters formed: "+clusterNumber);
				
		cluster = new Double[clusterNumber][][];
		//System.out.println("clusterNumber is: "+clusterNumber);
		for (int k = 0; k < clusterNumber; k++){
			//System.out.println("k clusterSize[k]: "+k+"   "+clusterSize[k]);
			cluster[k] = new Double[clusterSize[k]][2];
			
			for (int i = 0, j = 0; i < points.length; i++){
				if (membership[i] == k){
					cluster[k][j++] = points[i];
				}// end if				
			}// end for i,j
			
			// These are classified as a cluster; print these separately. 
			System.out.println("\n\n Cluster Number: " + k +"\n");
			for (int i=0; i<clusterSize[k]; i++){
				System.out.println(cluster[k][i][0]+" , "+cluster[k][i][1]);
			}// end for i
		}// end for k
	}// end learnByMaxHeight()
		
	
	/**
	 * This method creates 'clusterNumber' number of clusters from given set of points.
	 */
	public void learn(){
		
		HierarchicalClustering hc = new HierarchicalClustering(new CompleteLinkage(proximityMatrix));

		//Create clusters
		int[] membership = hc.partition(clusterNumber);
		// membership[a]=b, signifies that point 'a' belongs to cluster 'b' 
		
		// Get member count of each of the clusters
		int[] clusterSize = new int[clusterNumber];
		for (int i=0; i< membership.length; i++){
			clusterSize[membership[i]]++;
		} 
		
		cluster = new Double[clusterNumber][][];
		for (int k = 0; k < clusterNumber; k++){
			//System.out.println("k clusterSize[k]: "+k+"   "+clusterSize[k]);
			cluster[k] = new Double[clusterSize[k]][2];
			
			for (int i = 0, j = 0; i < points.length; i++){
				if (membership[i] == k){
					cluster[k][j++] = points[i];
				}// end if				
			}// end for i,j
			
			// These are classified as a cluster; print these separately. 
			System.out.println("\n\n Cluster Number: " + k +"\n");
			for (int i=0; i<clusterSize[k]; i++){
				System.out.println(cluster[k][i][0]+" , "+cluster[k][i][1]);
			}// end for i
		}// end for k		
	}// end learn()
	
	
	/**
	 * Constructor
	 */
	public FogCluster(String fn, int cNum) {
		super();
		setClusterNumber(cNum);
		//csvInput(fn);
		calcProximity();
		learn();		
		
	}// end Constructor FogHierCluster()
	
	
	/**
	 * This method creates 'clusterCount' number of clusters from given list of locations. 
	 * @param arrayList
	 * @param fogLevel
	 * @param clusterCount
	 * 
	 * @author Shaik
	 */
	public FogCluster(ArrayList<Location> arrayList, int fogLevel, int clusterCount) {
		//arrayList is a list of all the Locations on the map a device exists
		super();
		this.level = fogLevel;
		
		if(arrayList.size() < 4)
			setClusterNumber(arrayList.size());
		else
			setClusterNumber(arrayList.size() / 4);
		
		// Specify the number of clusters to create.
		setClusterNumber(clusterCount);
		
		// Populate array of 'points' from given list of locations.
		stdInput(arrayList);
		
		// Calculate proximity matrix i.e. distances between each pair of points.
		calcProximity();
		
		// Group points into clusters.
		if(arrayList.size() > 0)
			learn();
		
		
	}
	
	
	/**
	 * Constructor
	 * @param arrayList
	 * @param foglevel
	 * @param max
	 */
	public FogCluster(ArrayList<Location> arrayList, int foglevel, double max) {
		//arrayList is a list of all the Locations on the map a device exists
		super();
		this.level = foglevel;
		
		// Specify the number of clusters to create, based on maximum distance/latency between members of each cluster.
		setClusterHeight(max); 

		// Populate array of 'points' from given list of locations.
		stdInput(arrayList);

		// Calculate proximity matrix i.e. distances between each pair of points.
		calcProximity();
		
		// If there s only one point, then place it in a singleton cluster.
		if (arrayList.size() == 1){
			setClusterNumber(1);
			cluster = new Double[clusterNumber][][];
			cluster[0] = new Double[1][2];
			this.cluster[0][0] = points[0];			
		}
		
		// If >1 points, group them into clusters.
		else if(arrayList.size() > 0) {
			learnByMaxHeight();
		}
		
	} // end FogCluster (max)


	/**
	 * Write distance and latency matrices to file - for troubleshooting and reference.
	 * @param k
	 * @param level
	 * @param length
	 */
	public void writeMatricToFile(boolean k, int level, int length) {
		try {
			PrintWriter matrixWriter;
			if (k) {
				matrixWriter = new PrintWriter("sim_results/DistanceAndLatenceMatrix/Distance_" + level + ".txt");
			}
			else {
				matrixWriter = new PrintWriter("sim_results/DistanceAndLatenceMatrix/Latency_" + level + ".txt");
			}
			for (int i = 0; i < length; i++) {
				for (int j = 0; j < length; j++) {
					matrixWriter.print(proximityMatrix[i][j] + "\t");
				}
				matrixWriter.println();
			}
			matrixWriter.close();
		}
		catch (Exception e) {
			
		}
	}


	/**
	 * @return the lines
	 */
	public String[] getLines() {
		return lines;
	}


	/**
	 * @param lines the lines to set
	 */
	public void setLines(String[] lines) {
		this.lines = lines;
	}


	/**
	 * @return the points
	 */
	public Double[][] getPoints() {
		return points;
	}


	/**
	 * @param points the points to set
	 */
	public void setPoints(Double[][] points) {
		this.points = points;
	}


	/**
	 * @return the proximityMatrix
	 */
	public double[][] getProximityMatrix() {
		return proximityMatrix;
	}


	/**
	 * @param proximityMatrix the proximityMatrix to set
	 */
	public void setProximityMatrix(double[][] proximityMatrix) {
		this.proximityMatrix = proximityMatrix;
	}


	/**
	 * @return the maxClusterHeight
	 */
	public double getMaxClusterHeight() {
		return maxClusterHeight;
	}


	/**
	 * @param maxClusterHeight the maxClusterHeight to set
	 */
	public void setMaxClusterHeight(double maxClusterHeight) {
		this.maxClusterHeight = maxClusterHeight;
	}


	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}


	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}


	/**
	 * @return the clusterNumber
	 */
	public int getClusterNumber() {
		return clusterNumber;
	}


	/**
	 * @param cluster the cluster to set
	 */
	public void setCluster(Double[][][] cluster) {
		this.cluster = cluster;
	}

}// end class FogCluster


