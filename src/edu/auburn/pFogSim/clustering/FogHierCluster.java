package edu.auburn.pFogSim.clustering;

//import edu.boun.edgecloudsim.utils.*;
import javafx.util.Pair;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import edu.auburn.pFogSim.netsim.*;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.boun.edgecloudsim.utils.Location;


/**
 * 
 * @author szs0117
 *
 */
public class FogHierCluster {
	private ArrayList<FogCluster> clusterList = new ArrayList<FogCluster>();
	public int[][] parentCluster;
	
	
	/**
	 * Constructor
	 * @param nodes
	 */
	public FogHierCluster(ArrayList<NodeSim> nodes) {
		
		HashMap<Integer, ArrayList<Location>> levelMap = new HashMap<Integer, ArrayList<Location>>();
		int level = 1000;
		double x_pos = -1.0, y_pos = -1.0, a_pos = -1.0;
		int[] clusterCount = {100, 40, 20, 10, 3, 1, 1}; // Shaik added - number of clusters to be created in a given layer.
		double[] maxLatency = {2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0}; // Shaik added - max latency between any two nodes in a given layer is 2 msec.
		double[] maxDistance = {200, 500, 750, 1250, 1000, 3000, 4000}; // Shaik added - max distance between any two nodes in a given layer is 2 msec.
		
		ArrayList<Location> newList = new ArrayList<>();
		for(int r = 1; r <= 7; r++)
			levelMap.put(r, new ArrayList<Location>()); 
		//Add all nodes to levelMap based on their level values
		for(NodeSim node : nodes)
		{
			level = node.getLevel();
			x_pos = node.getLocation().getXPos();
			y_pos = node.getLocation().getYPos();
			a_pos = node.getLocation().getAltitude();
			
			Location pair = new Location(x_pos, y_pos, a_pos);
			if (levelMap.containsKey(level)) {
				levelMap.get(level).add(pair);
			}
			else {
				newList = new ArrayList<>();
				newList.add(pair);
				levelMap.put(level, newList);
			}
		}

		//Create clusters of nodes at each fog layer & save the configuration in clusterList
		for(int levelIter = 1; levelIter <= 7; levelIter++)
		{
			//FogCluster fc = new FogCluster(levelMap.get(levelIter), levelIter, clusterCount[levelIter-1]); // Create specified number of clusters in each layer.
			//FogCluster fc = new FogCluster(levelMap.get(levelIter), levelIter, maxLatency[levelIter-1]); // Clusters defined by maximum latency among members of each cluster.
			FogCluster fc = new FogCluster(levelMap.get(levelIter), levelIter, maxDistance[levelIter-1]); // Clusters defined by maximum latency among members of each cluster.

			clusterList.add(fc);
		}
		
		//Identify parent/child relationships among clusters belonging to adjacent layers.
		makeClusters();
		
		//Log the configuration 
		writeClustersToFile();
	}// end FogHierCluster()
	
	
	/**
	 * Create HAFA Architecture inter-layer links
	 * @author Shaik
	 */
	private void makeClusters() 
	{
		double distance = 0;
		double clusterMaxDistance = 0 ;
		double minDistance = Double.MAX_VALUE;
		parentCluster = new int[clusterList.size()][];
		int j;

		//Now, for each set of clusters in adjacent layers, repeat the following:
				//Say clusters in layer-3 & layer-4
		
		for(int i = clusterList.size() - 2; i > 0; i--)
		{
			//entry pairs considered are (i-j) = 5-4, 4-3, 3-2, 2-1, 1-0
			//corresponding layer pairs considered are = 6-5, 5-4, 4-3, 3-2, 2-1
			
				j = i - 1; //i - upper layer index; j-lower layer index
				
				// clusterList element at index p has clusters info for layer p+1
				int clusterNumber3 = clusterList.get(j).getCluster().length;
				int clusterNumber4 = clusterList.get(i).getCluster().length;
				
				parentCluster[j] = new int[clusterNumber3];
				
				Double[][][] clusterSet3 = clusterList.get(j).getCluster();
				Double[][][] clusterSet4 = clusterList.get(i).getCluster();
				//parentCluster = new int[clusterNumber3];
				
				//For each cluster in lower layer, do the following
				for (int cLower=0; cLower<clusterNumber3; cLower++){
					minDistance = Double.MAX_VALUE;
					
					//For each cluster in upper layer, do the following
					for(int cUpper=0; cUpper<clusterNumber4; cUpper++){
						
						clusterMaxDistance = 0;
						//Calculate the ('max' for CompleteLink) distance between cluster from lower layer 'cLower'
						//and cluster from higher layer 'cUpper'
						// i.e. find the distance between each point of 'cLower' cluster 
						// and each point of 'cUpper' cluster
						// Note the maximum distance
						
						//From each point of 'cLower' cluster
						for (int cLoweri=0; cLoweri<clusterSet3[cLower].length; cLoweri++){
							// Get point coordinates
							double x1 = clusterSet3[cLower][cLoweri][0];
							double y1 = clusterSet3[cLower][cLoweri][1];
							double a1 = clusterSet3[cLower][cLoweri][2];
							
							//To each point of 'cUpper' cluster
							for (int cUpperj=0; cUpperj<clusterSet4[cUpper].length; cUpperj++){
								// Get point coordinates
								double x2 = clusterSet4[cUpper][cUpperj][0];
								double y2 = clusterSet4[cUpper][cUpperj][1];
								double a2 = clusterSet4[cUpper][cUpperj][2];
														
								//find the distance
								//distance = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
								distance = DataInterpreter.measure(x1, y1, a1, x2,y2,a2);
								//System.out.println(distance);
								
								// Save the maximum distance
								if (distance > clusterMaxDistance){
									clusterMaxDistance = distance;
								}
								
							}// end for cUpperj
						}// end for cLoweri

						//If this is the closer upper layer cluster, then this is a better parent cluster
						if (clusterMaxDistance < minDistance){
							minDistance = clusterMaxDistance;
							parentCluster[j][cLower] = cUpper; 
						}
						
					}// end for cUpper
				}// end for cLower
				
				
				//Print Parent/Child relationships
				System.out.println("\nChildCluster"+"   "+"ParentCluster");
				for (int cLower=0; cLower<clusterNumber3; cLower++){
					System.out.println("         "+cLower+"   "+"         "+parentCluster[j][cLower]);
				}// end for cLower-Print
				
		}
		
		// Assign parent Puddle Ids for fog level 6 (city center) and fog level7 (cloud).
		parentCluster[5] = new int[1];
		parentCluster[5][0] = 0;
		parentCluster[6] = new int[1];
		parentCluster[6][0] = -1;

	}// end makeClusters()
	
	
	/**
	 * Return list of clusters.
	 * @return
	 */
	public ArrayList<FogCluster> getClusters() {
		return clusterList;
	}

	
	/**
	 * @return the clusterList
	 */
	public ArrayList<FogCluster> getClusterList() {
		return clusterList;
	}

	
	/**
	 * @param clusterList the clusterList to set
	 */
	public void setClusterList(ArrayList<FogCluster> clusterList) {
		this.clusterList = clusterList;
	}
	
/*	private static void makeClusters() {
		double distance = 0;
		double clusterMaxDistance = 0 ;
		double minDistance = Double.MAX_VALUE;
		int parent = 0;
		int[] parentCluster;
		
		//System.out.println("makeClusters reached");
		
		
		int clusterNumber1 = 100;
		String fileName1 = new String("C:\\Users\\cpj0009\\git\\pFogSim\\src\\edu\\auburn\\pFogSim\\kmcluster\\LocData-L1-500");
		FogCluster fc1 = new FogCluster(fileName1, clusterNumber1);
		Integer[][][] clusterSet1 = fc1.getCluster(); 
		for (int i=0; i<clusterNumber1; i++){
			//System.out.println("ClusterId  ClusterSize: "+i+"   "+clusterSet1[i].length);
		}		
		System.out.println("ClusterSet1 = " + clusterSet1[0][0][1]);
		int clusterNumber2 = 41;
		String fileName2 = new String("C:\\Users\\cpj0009\\git\\pFogSim\\src\\edu\\auburn\\pFogSim\\kmcluster\\LocData-L2-200");
		FogCluster fc2 = new FogCluster(fileName2, clusterNumber2);
		Integer[][][] clusterSet2 = fc2.getCluster();
		for (int i=0; i<clusterNumber2; i++){
			//System.out.println("ClusterId  ClusterSize: "+i+"   "+clusterSet2[i].length);
		}		
		//System.out.println("ClusterSet2 = " + clusterSet2);
		int clusterNumber3 = 20;
		String fileName3 = new String("C:\\Users\\cpj0009\\git\\pFogSim\\src\\edu\\auburn\\pFogSim\\kmcluster\\LocData-L3-50");
		FogCluster fc3 = new FogCluster(fileName3, clusterNumber3);
		Integer[][][] clusterSet3 = fc3.getCluster();
		for (int i=0; i<clusterNumber3; i++){
			//System.out.println("ClusterId  ClusterSize: "+i+"   "+clusterSet3[i].length);
		}		
		//System.out.println("ClusterSet3 = " + clusterSet3);
		int clusterNumber4 = 3;
		String fileName4 = new String("C:\\Users\\cpj0009\\git\\pFogSim\\src\\edu\\auburn\\pFogSim\\kmcluster\\LocData-L4-10");
		FogCluster fc4 = new FogCluster(fileName4, clusterNumber4);
		Integer[][][] clusterSet4 = fc4.getCluster();
		for (int i=0; i<clusterNumber4; i++){
			//System.out.println("ClusterId  ClusterSize: "+i+"   "+clusterSet4[i].length);
		}
		//System.out.println("ClusterSet4 = " + clusterSet4);
		
		//Now, for each set of clusters in adjacent layers, repeat the following:
		//Say clusters in layer-3 & layer-4
		
		parentCluster = new int[clusterNumber3];
		
		//For each cluster in lower layer, do the following
		for (int cLower=0; cLower<clusterNumber3; cLower++){
			minDistance = Double.MAX_VALUE;
			parent = 0;
			
			//For each cluster in upper layer, do the following
			for(int cUpper=0; cUpper<clusterNumber4; cUpper++){
				
				clusterMaxDistance = 0;
				//Calculate the ('max' for CompleteLink) distance between cluster from lower layer 'cLower'
				//and cluster from higher layer 'cUpper'
				// i.e. find the distance between each point of 'cLower' cluster 
				// and each point of 'cUpper' cluster
				// Note the maximum distance
				
				//From each point of 'cLower' cluster
				for (int cLoweri=0; cLoweri<clusterSet3[cLower].length; cLoweri++){
					// Get point coordinates
					int x1 = clusterSet3[cLower][cLoweri][0];
					int y1 = clusterSet3[cLower][cLoweri][1];
					
					//To each point of 'cUpper' cluster
					for (int cUpperj=0; cUpperj<clusterSet4[cUpper].length; cUpperj++){
						// Get point coordinates
						int x2 = clusterSet4[cUpper][cUpperj][0];
						int y2 = clusterSet4[cUpper][cUpperj][1];
												
						//find the distance
						distance = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
						////System.out.println(distance);
						
						// Save the maximum distance
						if (distance > clusterMaxDistance){
							clusterMaxDistance = distance;
						}
						
					}// end for cUpperj
				}// end for cLoweri

				//If this is the closer upper layer cluster, then this is a better parent cluster
				if (clusterMaxDistance < minDistance){
					minDistance = clusterMaxDistance;
					parentCluster[cLower] = cUpper; 
				}
				
			}// end for cUpper
		}// end for cLower
		
		//Print Parent/Child relationships
		System.out.println("ChildCluster"+"   "+"ParentCluster");
		for (int cLower=0; cLower<clusterNumber3; cLower++){
			System.out.println("         "+cLower+"   "+"         "+parentCluster[cLower]);
		}// end for cLower-Print

		
		
		//Now, for each set of clusters in adjacent layers, repeat the following:
		//Say clusters in layer-2 & layer-3
		
		parentCluster = new int[clusterNumber2];
		
		//For each cluster in lower layer, do the following
		for (int cLower=0; cLower<clusterNumber2; cLower++){
			minDistance = Double.MAX_VALUE;
			parent = 0;
			
			//For each cluster in upper layer, do the following
			for(int cUpper=0; cUpper<clusterNumber3; cUpper++){
				
				clusterMaxDistance = 0;
				//Calculate the ('max' for CompleteLink) distance between cluster from lower layer 'cLower'
				//and cluster from higher layer 'cUpper'
				// i.e. find the distance between each point of 'cLower' cluster 
				// and each point of 'cUpper' cluster
				// Note the maximum distance
				
				//From each point of 'cLower' cluster
				for (int cLoweri=0; cLoweri<clusterSet2[cLower].length; cLoweri++){
					// Get point coordinates
					int x1 = clusterSet2[cLower][cLoweri][0];
					int y1 = clusterSet2[cLower][cLoweri][1];
					
					//To each point of 'cUpper' cluster
					for (int cUpperj=0; cUpperj<clusterSet3[cUpper].length; cUpperj++){
						// Get point coordinates
						int x2 = clusterSet3[cUpper][cUpperj][0];
						int y2 = clusterSet3[cUpper][cUpperj][1];
												
						//find the distance
						distance = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
						////System.out.println(distance);
						
						// Save the maximum distance
						if (distance > clusterMaxDistance){
							clusterMaxDistance = distance;
						}
						
					}// end for cUpperj
				}// end for cLoweri

				//If this is the closer upper layer cluster, then this is a better parent cluster
				if (clusterMaxDistance < minDistance){
					minDistance = clusterMaxDistance;
					parentCluster[cLower] = cUpper; 
				}
				
			}// end for cUpper
		}// end for cLower
		
		//Print Parent/Child relationships
		System.out.println("ChildCluster"+"   "+"ParentCluster");
		for (int cLower=0; cLower<clusterNumber2; cLower++){
			System.out.println("         "+cLower+"   "+"         "+parentCluster[cLower]);
		}// end for cLower-Print
		

		
		
		//Now, for each set of clusters in adjacent layers, repeat the following:
		//Say clusters in layer-1 & layer-2
		
		parentCluster = new int[clusterNumber1];
		
		//For each cluster in lower layer, do the following
		for (int cLower=0; cLower<clusterNumber1; cLower++){
			minDistance = Double.MAX_VALUE;
			parent = 0;
			
			//For each cluster in upper layer, do the following
			for(int cUpper=0; cUpper<clusterNumber2; cUpper++){
				
				clusterMaxDistance = 0;
				//Calculate the ('max' for CompleteLink) distance between cluster from lower layer 'cLower'
				//and cluster from higher layer 'cUpper'
				// i.e. find the distance between each point of 'cLower' cluster 
				// and each point of 'cUpper' cluster
				// Note the maximum distance
				
				//From each point of 'cLower' cluster
				for (int cLoweri=0; cLoweri<clusterSet1[cLower].length; cLoweri++){
					// Get point coordinates
					int x1 = clusterSet1[cLower][cLoweri][0];
					int y1 = clusterSet1[cLower][cLoweri][1];
					
					//To each point of 'cUpper' cluster
					for (int cUpperj=0; cUpperj<clusterSet2[cUpper].length; cUpperj++){
						// Get point coordinates
						int x2 = clusterSet2[cUpper][cUpperj][0];
						int y2 = clusterSet2[cUpper][cUpperj][1];
												
						//find the distance
						distance = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
						////System.out.println(distance);
						
						// Save the maximum distance
						if (distance > clusterMaxDistance){
							clusterMaxDistance = distance;
						}
						
					}// end for cUpperj
				}// end for cLoweri

				//If this is the closer upper layer cluster, then this is a better parent cluster
				if (clusterMaxDistance < minDistance){
					minDistance = clusterMaxDistance;
					parentCluster[cLower] = cUpper; 
				}
				
			}// end for cUpper
		}// end for cLower
		
		//Print Parent/Child relationships
		System.out.println("ChildCluster"+"   "+"ParentCluster");
		for (int cLower=0; cLower<clusterNumber1; cLower++){
			System.out.println("         "+cLower+"   "+"         "+parentCluster[cLower]);
		}// end for cLower-Print
		

		

		
	}// End main
*/
	
	
	/**
	 * Save cluster info to file in XML format.
	 */
	public void writeClustersToFile() {
		PrintWriter writer;
		try {
			for (int i = 0; i < clusterList.size(); i++ ) {
				int level = i + 1;
				writer = new PrintWriter("sim_results/Clusters/level" + level + ".xml");
				writer.println("<?xml version=\"1.0\"?>");
				Double[][][] cluster = clusterList.get(i).getCluster();
				for (int j = 0; j < cluster.length; j++) {
					writer.println("<cluster"+ j +">");
					for (int k = 0; k < cluster[j].length; k++) {
						writer.println("<location>");
						writer.println("<x_pos>" + cluster[j][k][0] + "</x_pos>");
						writer.println("<y_pos>" + cluster[j][k][1] + "</y_pos>");
						writer.println("<altitude>" + cluster[j][k][2] + "</altitude>");
						writer.println("</location>");
					}
					writer.println("</cluster"+ j + ">");
				}
				writer.close();
			}
		}
		catch(Exception e) {
			
		}
	}
}// end class FogHierCluster

/*
 * Now, for each set of clusters in adjacent layers, repeat the following:
 * 		For each cluster in lower layer, do the following
 * 			Calculate the ('max' for CompleteLink) distance between this cluster and a cluster from higher layer
 * 			Repeat the above step for each cluster from higher layer
 * 			Identify the higher layer cluster with least distance
 * 			Mark it as the parent for lower layer cluster
 * 			Save this information in parent data structure specifying cluster id of parent cluster 
 * 				from higher layer in row i identifying cluster i of lower layer. 
 *  
 * */
//------------------------------------------------------------
/*
 * In this class, 
 * Create a string array with set of full path file names (each indicating 
 * 		fog node positions from a separate fog layer  
 * Create data structures of size equal to length of input file names list 
 * Repeat clustering operation for each set of fog nodes i.e. cluster nodes 
 * 		individually from each layer
 * Now we have the clustering details of each layer in the data structures 
 * 		- clusteringSet[][][]	//Array of size clusterNumber x clusterSize[i] x 2 specifying location of each fog node of cluster i in row i
 * 		- clusterNumber //Number of clusters in this layer
 * Save clustering information of all layers in data structures.
 * For each set of clusters, identify and group points belonging to a cluster 
 * 		(do we need to save this information? may be yes, to not repeat the process, 
 * 		but need additional storage space.)
 * Now, for each set of clusters in adjacent layers, repeat the following:
 * 		For each cluster in lower layer, do the following
 * 			Calculate the ('max' for CompleteLink) distance between this cluster and a cluster from higher layer
 * 			Repeat the above step for each cluster from higher layer
 * 			Identify the higher layer cluster with least distance
 * 			Mark it as the parent for lower layer cluster
 * 			Save this information in parent data structure specifying cluster id of parent cluster 
 * 				from higher layer in row i identifying cluster i of lower layer. 
 *  
 * */
 