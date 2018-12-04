package edu.auburn.pFogSim.clustering;

//import edu.boun.edgecloudsim.utils.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

import edu.auburn.pFogSim.netsim.*;
import edu.boun.edgecloudsim.utils.Location;

public class FogHierCluster {
	private ArrayList<FogCluster> clusterList = new ArrayList<FogCluster>();
	
	
	public FogHierCluster(ArrayList<NodeSim> nodes) {
		
		HashMap<Integer, ArrayList<Location>> levelMap = new HashMap<Integer, ArrayList<Location>>();
		int level = 1000;
		double x_pos = -1.0, y_pos = -1.0;
		//ArrayList<Location> tempList;
		for(int r = 0; r < 20; r++)
			levelMap.put(r, new ArrayList<Location>());
		//Add all nodes to levelMap based on their level values
		for(NodeSim node : nodes)
		{
			level = node.getLevel();
			x_pos = node.getLocation().getXPos();
			y_pos = node.getLocation().getYPos();
			Location pair = new Location(x_pos, y_pos);
			levelMap.get(level).add(pair);
			//SimLogger.printLine("node added!");
			/*if(levelMap.size() != 0)
			{
				
			}*/
			//else
			//{
				//levelMap.put(level, new ArrayList<Pair<Integer, Integer>>());
				//Pair<Integer, Integer> pair = new Pair<Integer, Integer>(x_pos, y_pos);
				//levelMap.get(level).add(pair);
			//}
		}
		int length = levelMap.size();
		//SimLogger.printLine("Length = " + length);
		int removed = 0;
		for (int i = 1; i < length; i++) {
			if(levelMap.get(i).size() == 0) {
				levelMap.remove(i);
				removed++;
			}
		}
		//SimLogger.printLine("Removed = " + removed);
		//Add all clusters we are making out of each layer to the clusterList
		for(int leveliter = 1; leveliter < levelMap.size(); leveliter++)
		{
			//SimLogger.printLine("Size = " + levelMap.get(leveliter));
			FogCluster fc = new FogCluster(levelMap.get(leveliter));
			clusterList.add(fc);
		}
		
		//Make the clusters
		makeClusters();
	}
	
	private void makeClusters() 
	{
		//Now, for each set of clusters in adjacent layers, repeat the following:
				//Say clusters in layer-3 & layer-4
		double distance = 0;
		double clusterMaxDistance = 0 ;
		double minDistance = Double.MAX_VALUE;
		int parent = 0;
		int[] parentCluster;
		int j;
		for(int i = clusterList.size() - 2; i > 0; i--)
		{
				j = i - 1;
				int clusterNumber3 = clusterList.get(j).getCluster().length;
				int clusterNumber4 = clusterList.get(i).getCluster().length;
				
				Double[][][] clusterSet3 = clusterList.get(j).getCluster();
				Double[][][] clusterSet4 = clusterList.get(i).getCluster();
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
							double x1 = clusterSet3[cLower][cLoweri][0];
							double y1 = clusterSet3[cLower][cLoweri][1];
							
							
							//To each point of 'cUpper' cluster
							for (int cUpperj=0; cUpperj<clusterSet4[cUpper].length; cUpperj++){
								// Get point coordinates
								double x2 = clusterSet4[cUpper][cUpperj][0];
								double y2 = clusterSet4[cUpper][cUpperj][1];
														
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
				//System.out.println("ChildCluster"+"   "+"ParentCluster");
				//for (int cLower=0; cLower<clusterNumber3; cLower++){
					//System.out.println("         "+cLower+"   "+"         "+parentCluster[cLower]);
				//}// end for cLower-Print
		}

	}
	
	public ArrayList<FogCluster> getClusters() {
		return clusterList;
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
		

		
		SimLogger.printLine("No issues so far...");
		
	}// End main
*/
	
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
 