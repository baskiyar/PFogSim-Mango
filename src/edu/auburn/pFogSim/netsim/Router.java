/**
 * Router Class for finding a path from one node to another on a given network model
 * @author jih0007
 */

package edu.auburn.pFogSim.netsim;

import edu.auburn.pFogSim.Exceptions.BlackHoleException;
//import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.Location;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
// import javafx.util.Pair;
//import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;


/**
 * 
 * @author szs0117
 *
 */
public class Router {
	private HashMap<String, LinkedList<NodeSim>> database;
	
	private HashMap<NodeSim, HashMap<NodeSim, LinkedList<NodeSim>>> pathFinder = new HashMap<>();
	
	/**
	 * 
	 */
	public Router() {
		database = new HashMap<String, LinkedList<NodeSim>>();
	}
	
	
	/**
	 * return a path from the src to destination as a linked list
	 * @param network
	 * @param src
	 * @param dest
	 * @return
	 */
	public LinkedList<NodeSim> findPath(NetworkTopology network, NodeSim src, NodeSim dest ) {
		LinkedList<NodeSim> travelQueue;
		LinkedList<NodeSim> path = new LinkedList<NodeSim>();
		String route;
		if (src.equals(dest)) {
			LinkedList<NodeSim> result = new LinkedList<NodeSim>();
			result.add(dest);
			return result;
		}
		route = src.toString() + "->" + dest.toString();
		/*if(database.containsKey(route)) {
			//SimLogger.printLine("Found Faster Path");
			LinkedList<NodeSim> better = database.get(route);
			return database.get(route);
		}*/
		Dijkstra router = getDijkstra();//getAPathFinder();
		router._dest = dest; 
		if (pathFinder.containsKey(src) && pathFinder.get(src).containsKey(dest) && !pathFinder.get(src).get(dest).isEmpty()) {
			return pathFinder.get(src).get(dest);
		}
//		if (pathFinder.containsKey(dest) && pathFinder.get(dest).containsKey(src) && !pathFinder.get(dest).get(src).isEmpty()) {
//			if (!pathFinder.containsKey(src)) {
//				pathFinder.put(src, new HashMap<>());
//			}
//			LinkedList<NodeSim> reversePath = pathFinder.get(dest).get(src);
//			LinkedList<NodeSim> newPath = new LinkedList<>();
//			while (!reversePath.isEmpty()) {
//				newPath.addFirst(reversePath.pollLast());
//			}
//			pathFinder.get(src).put(dest, newPath);
//			return pathFinder.get(src).get(dest);
//		}
		router.runDijkstra((Set<NodeSim>) network.getNodes(), src);
		HashSet<Pair<NodeSim, Pair<Double, NodeSim>>> completedCopy = new HashSet<>();
		completedCopy = (HashSet<Pair<NodeSim, Pair<Double, NodeSim>>>) router.completed.clone();
		for (Pair<NodeSim, Pair<Double, NodeSim>> nodePair : router.verts.keySet()) {
			NodeSim node = nodePair.getKey();
			if (!pathFinder.containsKey(src)) {
				pathFinder.put(src, new HashMap<>());
			}
			if (!pathFinder.get(src).containsKey(node) || pathFinder.get(src).get(node).isEmpty()) {
				pathFinder.get(src).put(node, router.getPath(node));
				router.completed = (HashSet<Pair<NodeSim, Pair<Double, NodeSim>>>) completedCopy.clone();
			}
//			if (!pathFinder.containsKey(node)) {
//				pathFinder.put(node, new HashMap<>());
//			}
//			if (!pathFinder.get(node).containsKey(src)) {
//				LinkedList<NodeSim> reversePath = pathFinder.get(src).get(node);
//				LinkedList<NodeSim> newPath = new LinkedList<>();
//				while (!reversePath.isEmpty()) {
//					newPath.addFirst(reversePath.pollLast());
//				}
//				pathFinder.get(node).put(src, newPath);
//			}
		}
		return pathFinder.get(src).get(dest);
//		travelQueue = router.getPath(dest);
		/*path.addAll(travelQueue);
		database.put(route, new LinkedList<NodeSim>(path));
		path.pollLast();
		while(!path.isEmpty()) {
			route = src.toString() + "->" + path.peekLast().toString();
			database.put(route, new LinkedList<NodeSim>(path));
			path.pollLast();
		}*/
//		return travelQueue;
		//return router.getLatency(dest);
	}
	
	
	/**
	 * get a Dijkstra object to run the pathfinding
	 * @return
	 */
	public static Dijkstra getAPathFinder() {
		Router rout = new Router();
		return rout.getDijkstra();
	}
	
	
	/**
	 * get a dijkstra object
	 * @return
	 */
	public Dijkstra getDijkstra() {
		return new Dijkstra();
	}
	
	/*used for early testing only, we want the router to just provide the path not calculate latency
	public double getLatency() {
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
	*/
		
	
	/**
	 * used for early testing only, we want the router to just provide the path not calculate latency
	 * @param network
	 * @param src
	 * @param dest
	 * @return
	 */
	public static double findRoute(NetworkTopology network, NodeSim src, NodeSim dest ) {
		Dijkstra router = getAPathFinder();
		router._dest = dest;
		router.runDijkstra((Set<NodeSim>) network.getNodes(), src);
		//travelQueue = router.getPath(dest);
		return router.getLatency(dest);
		//return router.getLatency(dest);
	}
	
	
	/**
	 * implementation of Dijkstra's algorithm
	 * @author jih0007
	 *
	 */
	private class Dijkstra {
		private HashMap<Pair<NodeSim, Pair<Double, NodeSim>>, ArrayList<Pair<Double, NodeSim>>> verts;
		private HashMap<NodeSim, Pair<NodeSim, Pair<Double, NodeSim>>> getMap;
		private TreeMap<Location, NodeSim> index;
		private PriorityQueue<Pair<NodeSim, Pair<Double, NodeSim>>> queue;
		private HashSet<Pair<NodeSim, Pair<Double, NodeSim>>> completed;
		private NodeSim src;
		private NodeSim _dest;
		
		
		/**
		 * constructor
		 */
		public Dijkstra() {
			verts = new HashMap<Pair<NodeSim, Pair<Double, NodeSim>>, ArrayList<Pair<Double, NodeSim>>>();
			getMap = new HashMap<NodeSim, Pair<NodeSim, Pair<Double, NodeSim>>>();
			index = new TreeMap<Location, NodeSim>();
			queue = new PriorityQueue<Pair<NodeSim, Pair<Double, NodeSim>>>(25, new dijkstrasComparator());
			completed = new HashSet<Pair<NodeSim, Pair<Double, NodeSim>>>();
		}
		
		
		/**
		 * initialize graph
		 * @param nodes
		 * @param source
		 */
		private void initialize (Set<NodeSim> nodes, NodeSim source) {
			ArrayList<Pair<Double, NodeSim>> edges;
			for (NodeSim node : nodes) {
				index.put(node.getLocation(), node);
			}
			for (NodeSim node : nodes) {
				if (node.equals(source)) {
					src = node;
					edges = new ArrayList<Pair<Double, NodeSim>>();
					for (Link edge : node.getEdges()) {
						edges.add(new Pair<Double, NodeSim>(edge.getOutgoingLat(node.getLocation()), index.get(edge.getOutgoingLink(node.getLocation()))));
					}
					verts.put(new Pair<NodeSim, Pair<Double, NodeSim>>(node, new Pair<Double, NodeSim>(0.0, null)), edges);
					getMap.put(node, new Pair<NodeSim, Pair<Double, NodeSim>>(node, new Pair<Double, NodeSim>(0.0, null)));
					continue;
				}
				edges = new ArrayList<Pair<Double, NodeSim>>();
				for (Link edge : node.getEdges()) {
					edges.add(new Pair<Double, NodeSim>(edge.getOutgoingLat(node.getLocation()), index.get(edge.getOutgoingLink(node.getLocation()))));
				}
				verts.put(new Pair<NodeSim, Pair<Double, NodeSim>>(node, new Pair<Double, NodeSim>(Double.MAX_VALUE, null)), edges);
				getMap.put(node, new Pair<NodeSim, Pair<Double, NodeSim>>(node, new Pair<Double, NodeSim>(Double.MAX_VALUE, null)));
			}
		}
		
		
		/**
		 * perform relaxation operation of Dijkstra's algorithm
		 * @param u
		 * @param v
		 * @param w
		 */
		private void relax(Pair<NodeSim, Pair<Double, NodeSim>> u, Pair<NodeSim, Pair<Double, NodeSim>> v, Double w) {
			Pair<NodeSim, Pair<Double, NodeSim>> temp;
			if (v == null) {
				return;
			}
			if (v.getValue().getKey() > (u.getValue().getKey() + w)) {
				temp = new Pair<NodeSim, Pair<Double, NodeSim>>(v.getKey(), new Pair<Double, NodeSim>(u.getValue().getKey() + w, u.getKey()));
				queue.add(temp);
				queue.remove(v);
				getMap.put(temp.getKey(), temp);
				//getMap.remove(v);
				verts.put(temp, verts.get(v));
				verts.remove(v);
			}
		}
		
		
		/**
		 * run Dijkstra's algorithm
		 * @param nodes
		 * @param source
		 */
		public void runDijkstra(Set<NodeSim> nodes, NodeSim source) {
			initialize(nodes, source);
			Pair<NodeSim, Pair<Double, NodeSim>> u;
			ArrayList<Pair<NodeSim, Pair<Double, NodeSim>>> adj = new ArrayList<Pair<NodeSim, Pair<Double, NodeSim>>>();
			ArrayList<Double> w = new ArrayList<Double>();
			for (Pair<NodeSim, Pair<Double, NodeSim>> vertice : verts.keySet()) {
				queue.add(vertice);
			}
			while (!queue.isEmpty()) {
				u = queue.poll();
				/*if (u.getKey().getWlanId() == 184 && u.getKey().equals(_dest)) {
					SimLogger.printLine("this one");
				}*/
				completed.add(u);
				w = new ArrayList<Double>();
				adj = new ArrayList<Pair<NodeSim, Pair<Double, NodeSim>>>();
				for (Pair<Double, NodeSim> d : verts.get(u)) {
					w.add(d.getKey());
					adj.add(getMap.get(d.getValue()));
				}
				for (int i = 0; i < w.size(); i++) {
					relax(u, adj.get(i), w.get(i));
				}
			}
		}
		
		
		/**
		 * 
		 * @param dest
		 * @return
		 */
		public double getLatency(NodeSim dest) {
			for (Pair<NodeSim, Pair<Double, NodeSim>> node : completed) {
				if (node.getKey().equals(dest)) {
					return node.getValue().getKey();
				}
			}
			return -1;
		}
		
		
		/**
		 * get the actual path in the form of a linked list
		 * @param dest
		 * @return
		 */
		public LinkedList<NodeSim> getPath(NodeSim dest) {
			LinkedList<NodeSim> reversed = new LinkedList<NodeSim>();
			LinkedList<NodeSim> result = new LinkedList<NodeSim>();
			Pair<NodeSim, Pair<Double, NodeSim>> temp = null;
			NodeSim current;
			reversed.add(dest);
			current = dest;
			while (!current.equals(src)) {
				for (Pair<NodeSim, Pair<Double, NodeSim>> node : completed) {
					if (node.getKey().equals(current)) {
						current = node.getValue().getValue();
						if (current == null) {
							throw new BlackHoleException(src, current);
							//we aren't throwing this for funzies 
							//it will cause a NullPointerException in about 0.3 seconds
							//throwing here allows us to pass some data out if we want
							//use the gravityWell() method in ESBModel to find out what's going on
							//There is a commented call in EdgeServerManager line 176
							//just uncomment that call and it will show you what nodes are messing up.
						}
						reversed.add(current);
						temp = node;
						break;
					}
				}
				
				completed.remove(temp);
			}
			
			while(!reversed.isEmpty()) {
				result.addFirst(reversed.poll());
			}
			return result;
		}
		
	}
	
	
	/**
	 * comparator to use a min-queue
	 * @author jih0007
	 *
	 */
	private class dijkstrasComparator implements Comparator<Pair<NodeSim, Pair<Double, NodeSim>>> {
		public int compare(Pair<NodeSim, Pair<Double, NodeSim>> x, Pair<NodeSim, Pair<Double, NodeSim>> y) {
			return (int)((x.getValue().getKey() - y.getValue().getKey()) * 1000);
		}		
	}
	
	
	/**
	 * @return the database
	 */
	public HashMap<String, LinkedList<NodeSim>> getDatabase() {
		return database;
	}
	
	
	/**
	 * @param database the database to set
	 */
	public void setDatabase(HashMap<String, LinkedList<NodeSim>> database) {
		this.database = database;
	}
}

class Pair<K,V>{
	private K key;
	private V value;

	public Pair(K key, V value){
		this.key = key;
		this.value = value;
	}
	
	public K getKey(){
		return key;
	}

	public V getValue(){
		return value;
	}

	// Auto-generated by eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	// Auto-generated by eclipse
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}

