/**
 * NetworkTopology Class for encapsulating a set of nodes and edges in a network
 * @author jih0007
 */
package edu.auburn.pFogSim.netsim;

import edu.auburn.pFogSim.netsim.Link;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.auburn.pFogSim.Puddle.Puddle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javafx.util.Pair;

public class NetworkTopology {
	private HashSet<Link> links;
	private HashSet<NodeSim> nodes;
	private HashSet<NodeSim> mobileNodes;
	private TreeSet<Location> coords;
	private ArrayList<Puddle> pond;
	/**
	 * Constructor
	 * @param inNodes
	 * @param inLinks
	 */
	public NetworkTopology(List<NodeSim> inNodes, List<Link> inLinks) {
		links = new HashSet<Link>();
		nodes = new HashSet<NodeSim>();
		coords = new TreeSet<Location>();
		mobileNodes = new HashSet<NodeSim>();
		for (NodeSim node : inNodes) {
			addNode(node);
		}
		for (Link link : inLinks) {
			addLink(link);
		}
	}
	/**
	 * add a node<br>
	 * if the node is null throw an IllegalArgumentException
	 * @param in
	 */
	public void addNode(NodeSim in) {
		if (in == null) {
			throw new IllegalArgumentException();
		}
		if (in.getLocation().getYPos() == 41.9058599) {
			SimLogger.printLine("test");
		}
		if (!coords.add(in.getLocation())) {
			for (NodeSim node : nodes) {
				if (node.getLocation().equals(in.getLocation())) {
					node.combine(in);
					return;
				}
			}
		}
		if (!in.isMoving()){
			nodes.add(in);
		}
		else {
			mobileNodes.add(in);
		}
		
	}
	/**
	 * add a link<br>
	 * link must be associated to 2 nodes to be added (always add nodes first!)<br>
	 * if the link is null throw an IllegalArgumentException
	 * @param in
	 * @return
	 */
	public boolean addLink(Link in) {
		if (in == null) {
			throw new IllegalArgumentException();
		}
		int counter = 0;
		for (NodeSim node : nodes) {
			if (in.validateCoords() && node.validateLink(in)) {
				node.addLink(in);
				counter++;
			}
		}
		if (counter == 2) {
			return links.add(in);
		}
		else {
			return false;
		}
	}
	/**
	 * run after all nodes and links have been added<br>
	 * all nodes must have at least one link<br>
	 * all links must be associated with 2 nodes
	 * @return
	 */
	public boolean validateTopology() {
		if (nodes == null || links == null) {
			return false;
		}
		try {
			for (NodeSim node : nodes) {
				if (node.getEdges().size() == 0) {
					return false;
				}
			}
			for (Link link : links) {
				if (!coords.contains(link.getRightLink()) || !coords.contains(link.getLeftLink())
						|| !link.validateCoords() || !link.validateLat()) {
					coords.add(link.getLeftLink());
					return false;
				}
			}
			return true;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	/**
	 * get the list of nodes
	 * @return
	 */
	public HashSet<NodeSim> getNodes() {
		return nodes;
	}
	/**
	 * get the list of links
	 * @return
	 */
	public HashSet<Link> getLinks() {
		return links;
	}
	/**
	 * cleans any bad links out of the topology
	 * @return
	 */
	public boolean cleanNodes() {
		try {
			//SimLogger.printLine("for loop reached");
			for (NodeSim node : nodes) {
				while (node.removeLink(node.validateNode())) {
					//SimLogger.printLine(node.toString());
				}
			}
			return validateTopology();
		}
		catch (Exception e) {
			SimLogger.printLine("false 5");
			return false;
		}
	}
	/**
	 * find the node closest to the given location, intermediate method of getting node associated with mobile device<br>
	 * @param d
	 * @param e
	 * @param wifi
	 * @return the closest node to the location
	 */
	public NodeSim findNode(double d, double e, boolean wifi) {
		NodeSim closest = null;
		double distanceNew = Double.MAX_VALUE;
		double distance = Double.MAX_VALUE;
		for (NodeSim node : getNodes()) {
			if (wifi && !node.isWifiAcc()) {
				continue;
			}
			if (d == node.getLocation().getXPos() && e == node.getLocation().getYPos()) {
				closest = node;
				return closest;
			}
			distanceNew = Math.sqrt(Math.pow((double) (d - node.getLocation().getXPos()), 2) + Math.pow((double) (e - node.getLocation().getYPos()), 2));
			if (distanceNew < distance) {
				distance = distanceNew;
				closest = node;
			}
		}
		return closest;
	}
	/**
	 * find the node closest to the given location, intermediate method of getting node associated with mobile device<br>
	 * @param loc
	 * @param wifi
	 * @return the closest node to the location
	 */
	public NodeSim findNode(Location loc, boolean wifi) {
		return findNode(loc.getXPos(), loc.getYPos(), wifi);
	}
	/**
	 * set the list of puddles on the topology
	 * @param puddles
	 */
	public void setPuddles(List<Puddle> puddles) {
		pond = new ArrayList<Puddle>();
		pond.addAll(puddles);
	}
	/**
	 * get the list of puddles for the topology
	 * @return
	 */
	public List<Puddle> getPuddles() {
		return (List<Puddle>) pond;
	}
	/**
	 * get the set of mobile fog nodes
	 * @return
	 */
	public Set<NodeSim> getMobileNodes() {
		return mobileNodes;
	}
	
	public void setMobileNode(Set<NodeSim> in) {
		mobileNodes = new HashSet<NodeSim>();
		mobileNodes.addAll(in);
	}
}
