package edu.auburn.pFogSim.clustering;

//import java.io.*;
import java.util.ArrayList;
import javafx.util.Pair;
//import java.util.HashMap;

//import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.core.SimManager;
//import edu.boun.edgecloudsim.utils.*;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.auburn.pFogSim.Voronoi.src.kn.uni.voronoitreemap.diagram.*;

public class FogCluster {
	private String[] lines = null;
	private Double[][] points = null;
	private double[][] proximityMatrix = null;
	private int clusterNumber;// = 20; // Defines number of clusters to generate.
	private Double[][][] cluster = new Double[clusterNumber][][];
	
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
			Double[] point = new Double[2];
			point[0] = pair.getXPos();
			point[1] = pair.getYPos();
			
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
	 * Calculate Proximity matrix
	 * Method - calcProximity
	 * 
	 */
	void calcProximity(){
		
		double x1=0.0, y1=0.0, x2=0.0, y2=0.0;
		double distance;
		
		// assume n data points ; n = size
		// declare an nxn array of double type
		
		int n = points.length;
		//System.out.println("Number of points: "+n);
		
		proximityMatrix = new double[n][n];
		
		for (int i=0; i<n; i++){
			// First point
			x1 = points[i][0];
			y1 = points[i][1];
			
			for (int j=0; j<n; j++){
				//Second point
				x2 = points[j][0];
				y2 = points[j][1];
				
				//Calculate distance
				distance = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
				//System.out.println(distance);
				
				//Update entry in proximityMatrix
				proximityMatrix[i][j] = distance;
				
			}// end for j
				
		}//end for i
		
	}//end calcProximity()

	
	public void learn(){
		
		//HierarchicalClustering hc = new HierarchicalClustering(new SingleLinkage(proximityMatrix));
		HierarchicalClustering hc = new HierarchicalClustering(new CompleteLinkage(proximityMatrix));
		//SimLogger.printLine("clusterNumber is: "+clusterNumber);
		int[] membership = hc.partition(clusterNumber);
		//SimLogger.printLine("Membership : " + membership);
		int[] clusterSize = new int[clusterNumber];
		//SimLogger.printLine("ClusterSize : " + clusterSize);
		//System.out.println("membership[] length: "+membership.length);
		//SimLogger.printLine("membership.length : " + membership.length);
		for (int i=0; i< membership.length; i++){
			clusterSize[membership[i]]++;
			//SimLogger.printLine("i membership[i] clusterSize: "+i+"   "+membership[i]+"   "+clusterSize[membership[i]]);
		} 
		
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
			//System.out.println("\n\n Cluster Number: " + k +"\n");
			for (int i=0; i<clusterSize[k]; i++){
				//System.out.println(cluster[k][i][0]+" , "+cluster[k][i][1]);
			}// end for i
			
		}// end for k
		
		/* code prior to change
		 * 		for (int k=0; k<clusterNumber; k++){
			Integer[][] cluster = new Integer[clusterSize[k]][];
			
			for (int i=0,j=0; i<points.length; i++){
				if (membership[i] == k){
					cluster[j++] = points[i];
				}// end if				
			}// end for i,j
			
			// These are classified as a cluster; print these separately. 
			//System.out.println("\n\n Cluster Number: " + k +"\n");
			for (int i=0; i<clusterSize[k]; i++){
				//System.out.println(cluster[i][0]+" , "+cluster[i][1]);
			}// end for i
			
		}// end for k
		
		 * */
		
		/** Copied code here
		 for (int k = 0; k < clusterNumber; k++) {
	            double[][] cluster = new double[clusterSize[k]][];
	            for (int i = 0, j = 0; i < dataset[datasetIndex].length; i++) {
	                if (membership[i] == k) {
	                    cluster[j++] = dataset[datasetIndex][i];
	                }
	            }

	            plot.points(cluster, pointLegend, Palette.COLORS[k % Palette.COLORS.length]);
	        }
		*/
		
				
	}// end learn()
	
	
	/**
	 * Constructor
	 */
	public FogCluster(String fn, int cNum) {
		super();
		//SimLogger.printLine("String and int constructor FogCluster() reached");
		setClusterNumber(cNum);
		//csvInput(fn);
		calcProximity();
		learn();		
		
	}// end Constructor FogHierCluster()
	
	public FogCluster(ArrayList<Location> arrayList) {
		//arrayList is a list of all the Locations on the map a device exists
		super();
		//SimLogger.printLine("Blank constructor FogCluster() reached");
		//SimLogger.printLine("LevelList size = " + levelList.size());
		if(arrayList.size() < 4)
			setClusterNumber(arrayList.size());
		else
			setClusterNumber(arrayList.size() / 4);
		stdInput(arrayList);
		calcProximity();
		if(arrayList.size() > 0)
			learn();
		
		//Make the voronoi diagram for that level and add it to the list
		//PowerDiagram voronoi = new PowerDiagram(arrayList);
		//SimLogger.printLine("ArrayList : " + arrayList);
		SimManager.getInstance().addToVoronoiDiagramList(PowerDiagram.makeVoronoiDiagram(arrayList));
		
	}


	public static void main(String[] args) {
		/*
		int clusterNumber1 = 100;
		String fileName1 = new String("C:\\Users\\szs0117\\workspace\\tia\\src\\KMCluster\\LocData-L1-500");
		FogCluster fc1 = new FogCluster(fileName1, clusterNumber1);
		Integer[][][] clusters1 = fc1.getCluster(); 
		*//*
		int clusterNumber2 = 40;
		String fileName2 = new String("C:\\Users\\szs0117\\workspace\\tia\\src\\KMCluster\\LocData-L2-200");
		FogCluster fc2 = new FogCluster(fileName2, clusterNumber2);
		Integer[][][] clusters2 = fc2.getCluster();
		*//*
		int clusterNumber3 = 20;
		String fileName3 = new String("C:\\Users\\szs0117\\workspace\\tia\\src\\KMCluster\\LocData-L3-50");
		FogCluster fc3 = new FogCluster(fileName3, clusterNumber3);
		Integer[][][] clusters3 = fc3.getCluster();
	
		int clusterNumber4 = 3;
		String fileName4 = new String("C:\\Users\\szs0117\\workspace\\tia\\src\\KMCluster\\LocData-L4-10");
		FogCluster fc4 = new FogCluster(fileName4, clusterNumber4);
		Integer[][][] clusters4 = fc4.getCluster();
		
		*/
		
		
	}// End main

}// end class FogCluster


