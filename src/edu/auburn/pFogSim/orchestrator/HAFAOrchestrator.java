package edu.auburn.pFogSim.orchestrator;

/*
 * @author Shehenaz Shaik
 * @author Qian Wang
 * @author Jacob Hall
 * 
 * For those who come after...
 *  * If you are here to change the puddle orchestrator to the true HAFA architecture, (i.e. change it from optimized 
 * on distance to optimized on cost) then the only changes that need to be made are in DistRadix. The only thing 
 * that needs to be changed is the metric upon which we sort and select nodes. See DistRadix for more info.
 * 
 */
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.auburn.pFogSim.netsim.NetworkTopology;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
//import edu.boun.edgecloudsim.utils.SimLogger;
import javafx.util.Pair;
import edu.auburn.pFogSim.Puddle.Puddle;
import edu.auburn.pFogSim.Radix.BinaryHeap;
import edu.auburn.pFogSim.Radix.DistRadix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Datacenter;
//import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;


/**
 * implementation of Edge Orchestrator for using puddles
 * @author Shehenaz Shaik
 * @author Jacob I Hall 
 * @author Qian Wang
 * 
 */
public class HAFAOrchestrator extends EdgeOrchestrator {

	ArrayList<EdgeHost> allHosts;
	HashMap<NodeSim,HashMap<NodeSim, LinkedList<NodeSim>>> pathTable;
	ESBModel networkModel;
	
	// To capture HAFA metrics
	public int[] numProspectiveHosts;
	public int[] numMessages;
	public int[] numPuddlesSearched;
	
	// Pring HAFA metrics to files
	File hafaNumHostsFile = null, hafaNumMsgsFile = null, hafaNumPuddlesFile = null;
	FileWriter hafaNumHostsFW = null, hafaNumMsgsFW = null, hafaNumPuddlesFW = null;
	BufferedWriter hafaNumHostsBW = null, hafaNumMsgsBW = null, hafaNumPuddlesBW = null;
	
	
	/**
	 * constructor
	 * @param _policy
	 * @param _simScenario
	 */
	public HAFAOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);			
	}

	
	/**
	 * Return detailed metrics of HAFA orchestrator - Number of prospective hosts per service request (device)
	 */
	public int[] getNumProspectiveHosts() {	
		return this.numProspectiveHosts;
	}
	

	/**
	 * Return detailed metrics of HAFA orchestrator - Number of messages exchanged per service request (device)
	 */
	public int[] getNumMessages() {	
		return this.numMessages;
	}


	/**
	 * Return detailed metrics of HAFA orchestrator - Number of Puddles searched per service request (device)
	 */
	public int[] getNumPuddlesSearched() {	
		return this.numPuddlesSearched;
	}

	
	/**
	 * Method to configure and initialize HAFA Architecture and Puddle Tree
	 */
	@Override
	public void initialize() {
		
		allHosts = new ArrayList<EdgeHost>();
		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			allHosts.add(((EdgeHost) node.getHostList().get(0)));
		}
		pathTable = new HashMap<>();
		networkModel = (ESBModel)(SimManager.getInstance().getNetworkModel());
		for (NodeSim src: networkModel.getNetworkTopology().getNodes()) {
			HashMap<NodeSim, LinkedList<NodeSim>> tempMap = new HashMap<>();
			for (NodeSim des: networkModel.getNetworkTopology().getNodes()) {
				LinkedList<NodeSim> tempList = new LinkedList<>();
				tempList = networkModel.findPath(src, des);
				tempMap.put(des, tempList);
			}
			pathTable.put(src, tempMap);
		}		
		
		// Initialize metrics
		int devCount = SimManager.getInstance().getNumOfMobileDevice();
		numProspectiveHosts = new int[devCount];
		numMessages = new int[devCount];
		numPuddlesSearched = new int[devCount];
		
		for (int i=0; i<devCount; i++) {
			this.numProspectiveHosts[i] = 0;
			this.numMessages[i] = 0;
			this.numPuddlesSearched[i] = 0;
		}
	}
	
	
	/**
	 * 
	 * @param deviceId
	 * @param hostCount
	 */
	public void addNumProspectiveHosts(int deviceId, int hostCount) {
		this.numProspectiveHosts[deviceId] += hostCount;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumProspectiveHosts() {
		int devCount = SimManager.getInstance().getNumOfMobileDevice();
		int assignedDevCount = 0;
		int totalNumProspectiveHosts = 0;
		
		for (int i=0; i<devCount; i++) {
			MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(i);
			if (mb.getHost() != null) {
				assignedDevCount++;
				totalNumProspectiveHosts += this.numProspectiveHosts[i];
			}	
		}
		
		return ((double)totalNumProspectiveHosts / assignedDevCount );		
	}
		

	/**
	 * Messages are exchanged by PuddleHeads to forward a service placement request or to respond to one. 
	 * @param deviceId
	 * @param msgCount
	 */
	public void addNumMessages(int deviceId, int msgCount) {
		this.numMessages[deviceId] += msgCount;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumMessages() {
		int devCount = SimManager.getInstance().getNumOfMobileDevice();
		int assignedDevCount = 0;
		int totalNumMessages = 0;
		
		for (int i=0; i<devCount; i++) {
			MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(i);
			if (mb.getHost() != null) {
				assignedDevCount++;
				totalNumMessages += this.numMessages[i];
			}	
		}
		
		return ((double)totalNumMessages / assignedDevCount );		
	}
		
	
	/**
	 * 
	 * @param deviceId
	 * @param pudCount
	 */
	public void addNumPuddlesSearched(int deviceId, int pudCount) {
		this.numPuddlesSearched[deviceId] += pudCount;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumPuddlesSearched() {
		int devCount = SimManager.getInstance().getNumOfMobileDevice();
		int assignedDevCount = 0;
		int totalPuddlesSearched = 0;
		
		for (int i=0; i<devCount; i++) {
			MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(i);
			if (mb.getHost() != null) {
				assignedDevCount++;
				totalPuddlesSearched += this.numPuddlesSearched[i];
			}	
		}
		
		return ((double)totalPuddlesSearched / assignedDevCount );		
	}
	
	
	/**
	 * get the id of the appropriate host
	 */
	@Override
	public int getDeviceToOffload(Task task) {
		try {
			return getHost(task).getId();
		}
		catch (NullPointerException e) {
			return -1;
		}
	}
	

	/**
	 * get the appropriate VM to run on
	 */
	@Override
	public EdgeVM getVmToOffload(Task task) {
		try {
			return ((EdgeVM) getHost(task).getVmList().get(0));
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	

	/**
	 * find a proper host to place the task
	 * @param task
	 * @return
	 */
	private EdgeHost getHost(Task task) {
		MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(task.getMobileDeviceId());
		task.setPath(mb.getPath());
		return mb.getHost();		
	}
	

	/** 
	 * This method identifies an efficient fog node to host the application service for given mobile device. 
	 * @author Shaik
	 * (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
		ArrayList<EdgeHost> goodNodesPerLayer = new ArrayList<EdgeHost>();
		ArrayList<EdgeHost> prospectiveNodes = new ArrayList<EdgeHost>();
		
		// Part-A: Find one good node per fog layer, Repeat the following for each layer.
		for (int levelIter=1; levelIter <= 7; levelIter++) {
			
			// Find fog node nearest to mobile device, belonging to this fog layer.
			EdgeHost nearest = SimManager.getInstance().getEdgeServerManager().findNearestHostByLayer(levelIter, mobile.getLocation());
			
			// Find corresponding puddle members i.e. puddle nearest to mobile device belonging to this fog layer. 
			int pudId = nearest.getPuddleId();
			Puddle pud = SimManager.getInstance().getEdgeServerManager().findPuddleById(levelIter, pudId);
			
			// Request sent to nearest Puddle by device / fog broker 
			this.addNumMessages(mobile.getId(), 1);
			this.addNumPuddlesSearched(mobile.getId(), 1);
			
			prospectiveNodes = pud.getMembers();

			// All fog nodes belonging to a given layer have similar node capacity. hence, if the first one doesn't satisfy requirement, we ignore rest of the search for this layer.
			if ((prospectiveNodes.size() == 0) || !prospectiveNodes.get(0).isMIPSCapacitySufficient(mobile)) {
				System.out.print("Level: "+levelIter+" Node Mips capacity insufficient.");
				continue;
			}
			else {
				
				//set latencylimitflag to 1;
				
			// Identify the best (nearest) node to host the application among prospective nodes.
			System.out.print("Level: "+levelIter+" Prospective host:  ");
			while (prospectiveNodes.size() != 0) {
				
				this.addNumProspectiveHosts(mobile.getId(), prospectiveNodes.size());
				
				//DistRadix sort = new DistRadix(prospectiveNodes, mobile.getLocation()); //use radix sort based on distance from mobile device.
				BinaryHeap sort = new BinaryHeap(prospectiveNodes.size(), mobile.getLocation(), prospectiveNodes);
				LinkedList<EdgeHost> nodes = sort.sortNodes();
				EdgeHost prosHost = nodes.poll();
					
				
				
				// Find the nearest node capable of hosting the application.
				while(!goodHost(prosHost, mobile)) {
					
					// if mobile flag is not due-to-latency --> set latencylimitflag to 0;
					
					prosHost = nodes.poll(); 
					if (prosHost == null) {
						break;
					}
				}
				
				// Search successful in current subtree.
				if (prosHost != null) {
					// Good host found for current fog layer. Save it and initiate search for next higher layer.
					goodNodesPerLayer.add(prosHost);
					break;
				}
				
				// if latencylimitflag is 1 --> all prospective nodes has sufficient mips, but failed due to latency; 
				// give just one more chance to expand search, beyond which we assume the nodes are too far anyway ( heuristic) & may have higher latency
				// if latencylimitflag = 2; then print latency unacceptable for this layer; and break
				// else set latencylimitflag ++ i.e. set to 2 i.e. give a second chance to expand search; or set to 1 to restart the racking prcess. 
				// and continue executing the fllowing code..
				
				// Search unsuccessful, hence expand search to siblings/neighbors using parent subtree.
				// Get list of other prospective nodes belonging to this layer in the parent subtree
				prospectiveNodes = SimManager.getInstance().getEdgeServerManager().getCousins(pud, levelIter, mobile.getId());
				
				// If search unsuccessful in entire system
				// i.e. no node in this fog layer has sufficient resources to host the application.
				if (prospectiveNodes.size() == 0) {
					break;
				}				
				
				// Update root of subtree which is being searched
				pud = SimManager.getInstance().getEdgeServerManager().findPuddleById(pud.getLevel()+1, pud.getParentPuddleId());

			}
			}// end if
		}// end for - levelIter

		// Part-B: Find cost-optimal node from the set of good nodes identified one per fog layer
		
		// Find the total cost of execution at each prospective fog node
		// cost of execution and cost of data transfer depends on the type of application accessed from mobile device
		Map<Double, List<NodeSim>> costMap = new HashMap<>();
		NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(mobile.getLocation(), false);
		
		// get the set of paths for all nodes from current location of mobile device
		Map<NodeSim, LinkedList<NodeSim>> desMap = pathTable.get(src);
		
		// Prune the set of paths to include only the prospective fog nodes from goodNodesPerLayer list
		Map<NodeSim, LinkedList<NodeSim>> selectedDesMap = new HashMap<>();

		for (int i=0; i<goodNodesPerLayer.size(); i++) {
			
			// for each host, get the NodeSim object
			Location hostLoc = goodNodesPerLayer.get(i).getLocation();
			NodeSim hostNode = ((ESBModel)networkModel).getNetworkTopology().findNode(hostLoc, false);
			
			// for each such NodeSim object, retrieve the row and add it to selectedDesMap
			selectedDesMap.put(hostNode, desMap.get(hostNode));	
		}

		// continue with processing as earlier.
		for (Entry<NodeSim, LinkedList<NodeSim>> entry: selectedDesMap.entrySet()) {
			double cost = 0;
			NodeSim des = entry.getKey();
			LinkedList<NodeSim> path = entry.getValue();
			if (path == null || path.size() == 0) {
				EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(mobile.getLocation().getXPos(), mobile.getLocation().getYPos(), mobile.getLocation().getAltitude());
				
				//double bwCost = mobile.getBWRequirement() * k.getCostPerBW(); 
				double bwCost = (mobile.getBWRequirement()*8 / (double)1024) * k.getCostPerBW(); //mobile.getBWRequirement() in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified

				//double exCost = (double)mobile.getTaskLengthRequirement() / k.getTotalMips() * k.getCostPerSec();
				double exCost = (double)mobile.getTaskLengthRequirement() / (k.getPeList().get(0).getMips()) * k.getCostPerSec(); // Shaik modified - May 07, 2019.

				cost = cost + bwCost;
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + des.getLevel() + "\tNode:\t" + des.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
				cost = cost + exCost;
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPu Time:\t" + ((double)mobile.getTaskLengthRequirement() / k.getTotalMips()) + "\tMipsCostPerSec:\t" + k.getCostPerSec());

			}
			else {
				//SimLogger.getInstance().getCentralizeLogPrinter().println("**********Path From " + src.getWlanId() + " To " + des.getWlanId() + "**********");
				for (NodeSim node: path) {
					EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(node.getLocation().getXPos(), node.getLocation().getYPos(), node.getLocation().getAltitude());
					//double bwCost = mobile.getBWRequirement() * k.getCostPerBW();
					double bwCost = (mobile.getBWRequirement()*8 / (double)1024) * k.getCostPerBW(); //mobile.getBWRequirement() in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified

					cost = cost + bwCost;
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + node.getLevel() + "\tNode:\t" + node.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
				}				
				EdgeHost desHost = SimManager.getInstance().getLocalServerManager().findHostByLoc(des.getLocation().getXPos(), des.getLocation().getYPos(), des.getLocation().getAltitude());
				//double exCost = desHost.getCostPerSec() * ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips());
				double exCost = (double)mobile.getTaskLengthRequirement() / (desHost.getPeList().get(0).getMips()) * desHost.getCostPerSec(); // Shaik modified - May 07, 2019.

				cost = cost + exCost;
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPU time:\t" + ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips()) + "\tMipsCostPerSec:\t" + desHost.getCostPerSec());
			}
			
			if (costMap.containsKey(cost)) {
				if (!costMap.get(cost).contains(des)) {
					costMap.get(cost).add(des);
				}
			}
			else {
				ArrayList<NodeSim> desList = new ArrayList<>();
				desList.add(des);
				costMap.put(cost, desList);
			}
			
		}

		//Sort the list of prospective nodes by increasing cost 
		LinkedList<EdgeHost> hostsSortedByCost = new LinkedList<EdgeHost>();
		
		//Create a sorted list of costs
		List<Double> costList = new ArrayList<Double> (costMap.keySet());
		Collections.sort(costList);
		
		//Access the map entries in order sorted by cost
		EdgeHost host = null;
		for (Double totalCost : costList) {
			// Get the list of prospective nodes available at that cost
			for(NodeSim desNode: costMap.get(totalCost)) {
				host = SimManager.getInstance().getLocalServerManager().findHostByLoc(desNode.getLocation().getXPos(), desNode.getLocation().getYPos(), desNode.getLocation().getAltitude());
				hostsSortedByCost.add(host);
				//System.out.println("Hosts in sorted order of costs:  "+host.getId()+"  at cost:  "+totalCost);
			}
		}

		//Find a cost-optimal node with available resources
		if (hostsSortedByCost.size() == 0) {
			System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
			return;
		}
		
		EdgeHost selHost = null;
		selHost = hostsSortedByCost.poll();
		if (selHost == null) {
			System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
			return;
		}
		
		System.out.print("Prospective host:  ");
		while(!goodHost(selHost, mobile)) {
			selHost = hostsSortedByCost.poll();//find the next cost-optimal node capable of handling the task
			if (selHost == null) {
				break;
			}
		}
		if (selHost != null) {
			LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(selHost, mobile);
			mobile.setPath(path);
			mobile.setHost(selHost);
			mobile.makeReservation();
			System.out.println("  Assigned host: " + selHost.getId());
		}
		else
			System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");

		return;
	}	
	
}// end class HAFAOrchestrator