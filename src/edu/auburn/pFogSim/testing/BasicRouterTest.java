/**
 * basic driver class for testing router and network models
 * @author jih0007
 */
package edu.auburn.pFogSim.testing;

import javafx.util.Pair;
import edu.auburn.pFogSim.netsim.*;
import java.util.*;

public class BasicRouterTest {
	public static void main(String[] args) {
		//make some coordinates
		Pair<Integer, Integer> c1 = new Pair<Integer, Integer>(1,1);
		Pair<Integer, Integer> c2 = new Pair<Integer, Integer>(2,2);
		Pair<Integer, Integer> c3 = new Pair<Integer, Integer>(3,3);
		Pair<Integer, Integer> c4 = new Pair<Integer, Integer>(4,4);
		Pair<Integer, Integer> c5 = new Pair<Integer, Integer>(5,5);
		Pair<Integer, Integer> c6 = new Pair<Integer, Integer>(5,6);
		Pair<Integer, Integer> c7 = new Pair<Integer, Integer>(4,7);
		//make some nodes on the coords
		NodeSim n1 = new NodeSim();
		NodeSim n2 = new NodeSim();
		NodeSim n3 = new NodeSim();
		NodeSim n4 = new NodeSim();
		NodeSim n5 = new NodeSim();
		NodeSim n6 = new NodeSim();
		NodeSim n7 = new NodeSim();
		//some lists, for calling methods
		LinkedList<NodeSim> path = new LinkedList<NodeSim>();
		LinkedList<Link> edges = new LinkedList<Link>();
		//place the nodes on the coords
		/*n1.setLocation(c1);
		n2.setLocation(c2);
		n3.setLocation(c3);
		n4.setLocation(c4);
		n5.setLocation(c5);
		n6.setLocation(c6);
		n7.setLocation(c7);
		//make some links
		Link l12 = new Link(c1, c2, 0.5, 0.5);
		Link l23 = new Link(c2, c3, 0.5, 0.5);
		Link l34 = new Link(c3, c4, 0.5, 0.5);
		Link l45 = new Link(c4, c5, 0.5, 0.5);
		Link l56 = new Link(c5, c6, 0.1, 0.1);
		Link l67 = new Link(c6, c7, 0.1, 0.1);
		Link l57 = new Link(c5, c7, 0.5, 0.5);
		Link l47 = new Link(c4, c7, 0.5, 1.5);
		//hook up the nodes and links
*/		/*n1.addLink(l12);
		n2.addLink(l12);
		n2.addLink(l23);
		n3.addLink(l23);
		n3.addLink(l34);
		n4.addLink(l34);
		n4.addLink(l45);
		n4.addLink(l47);
		n5.addLink(l45);
		n5.addLink(l56);
		n5.addLink(l57);
		n6.addLink(l56);
		n6.addLink(l67);
		n7.addLink(l47);
		n7.addLink(l57);
		n7.addLink(l67);
		//fill the lists
		path.add(n1);
		path.add(n2);
		path.add(n3);
		path.add(n4);
		path.add(n5);
		path.add(n6);
		path.add(n7);
		
		edges.add(l12);
		edges.add(l23);
		edges.add(l34);
		edges.add(l45);
		edges.add(l56);
		edges.add(l67);
		edges.add(l57);
		edges.add(l47);
		
		*/
		//call tests!
		NetworkTopology nTest = new NetworkTopology((List<NodeSim>) path, (List<Link>) edges);
		if (nTest.cleanNodes()) {
			System.out.println("Topology Works!");
		}
		
		Router router = new Router();
		
		System.out.println(getLatency(new Router().findPath(nTest, n7, n1)));
	}
	/**
	 * the thing that walks over the path returned from the router and calculates the latency
	 * @param travelQueue
	 * @return
	 */
	public static double getLatency(LinkedList<NodeSim> travelQueue) {
		double latency = 0.0;
		NodeSim current;
		NodeSim next;
		while (!travelQueue.isEmpty()) {
			current = travelQueue.pollFirst();
			next = travelQueue.peekFirst();
			if (next == null) {
				break;
			}
			if (current.traverse(next) >= 0) {
				latency += current.traverse(next);
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		return latency;
	}
}
