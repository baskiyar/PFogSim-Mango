package edu.auburn.pFogSim.Exceptions;

import edu.auburn.pFogSim.netsim.NodeSim;
/**
 * When running large networks sometimes there is an error in the setup of the topology. <br>
 * What happens is that somehow a pocket gets formed that is isolated from the rest of the<br>
 * network. This causes a NullPointer to pop up in routing. We have 3 days left in the program<br>
 * and I am low on sanity, so I dubbed the error a Black Hole.<br><br>
 * I am not entirely sure what all the causes for black holes are but I do know that co-located<br>
 * EdgeHosts ALWAYS cause black holes, so clean those out first. Beyond that they are a mystery. <br>
 * There is a method in ESBModel called gravityWell() that will find black holes, it already has <br>
 * a proper call in EdgeServerManager line 132, just uncomment it and it will find them for you. <br>
 * Once you find them I recommend removing the node from the data set (just easier, we have an ~1100 <br>
 * node network) barring that, you can try to adapt gravityWell() to try to fix them at runtime.
 * @author Jacob I Hall jih0007@auburn.edu
 *
 */
@SuppressWarnings("serial")
public class BlackHoleException extends RuntimeException {
	public NodeSim dest;
	public NodeSim src;
	
	public BlackHoleException(NodeSim _src, NodeSim _dest) {
		dest = _dest;
		src = _src;
	}
}
