package edu.auburn.pFogSim.clustering;

public class FogParentChild {

	
	
}//end class FogParentChild


/*
 * 1. Need two separate data structures to maintain information of clusters belonging to upper & lower layers
 * 2. Create proximity matrix for all nodes
 * 3. For each cluster in lower layer, calculate proximity-max link to each cluster in upper layer
 * 4. Identify the upper layer cluster whose link distance is minimum
 * 5. save the information in a data structure. This defines the parent / child connection
 * 6. Repeat steps 3,4,5 for each cluster in lower layer
 * 7. Repeat step 6 for all adjacent layers.
 * */
 