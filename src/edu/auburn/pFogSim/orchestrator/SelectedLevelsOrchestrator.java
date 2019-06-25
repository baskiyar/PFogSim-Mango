package edu.auburn.pFogSim.orchestrator;

import java.util.*;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.Radix.DistRadix;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;


/**
 * Note: This class needs to be tested thoroughly. There may be bugs / issues.
 * @author szs0117
 *
 */
public class SelectedLevelsOrchestrator extends EdgeOrchestrator {
	
	private Map<Integer, ArrayList<EdgeHost>> hostLevelMap;
	
	ArrayList<EdgeHost> allHosts, selLayerHosts;
	HashMap<NodeSim,HashMap<NodeSim, LinkedList<NodeSim>>> pathTable;
	ESBModel networkModel;

	
	/**
	 * 
	 * @param _policy
	 * @param _simScenario
	 */
	public SelectedLevelsOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}

	
	/**
	 * 
	 */
	@Override
	public void initialize() {

		// Group the hosts based on the fog layer they belong to
		hostLevelMap = new HashMap<Integer, ArrayList<EdgeHost>>();
		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			EdgeHost host = (EdgeHost) node.getHostList().get(0);
			if (hostLevelMap.containsKey(host.getLevel())) {
				hostLevelMap.get(host.getLevel()).add(host);
			}
			else {
				ArrayList<EdgeHost> newLevel = new ArrayList<EdgeHost>();
				newLevel.add(host);
				hostLevelMap.put(host.getLevel(), newLevel);
			}
		}
		
		//Get of set of all prospective hosts, i.e. those from selected Layers 
		selLayerHosts = new ArrayList<EdgeHost>();
		SimLogger.print("List of given Fog layers: ");
		for (int i : SimSettings.getInstance().getSelectedLayerIds()) {
			SimLogger.print(i+" ");
			selLayerHosts.addAll(hostLevelMap.get(i));
		}
		

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
		
		this.avgNumProspectiveHosts = selLayerHosts.size();
		this.avgNumMessages = this.avgNumProspectiveHosts * 2; // For each service request (i.e. per device), each host receives resource availability request & sends response.
		
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
	 * the the appropriate VM to run on
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
	 * 
	 * @param task
	 * @return
	 */
	private EdgeHost getHost(Task task) {
		MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(task.getMobileDeviceId());
		task.setPath(mb.getPath());
		return mb.getHost();
	}

	
	/**
	 * 
	 */
	/* (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
		
		// Find the total cost of execution at each prospective fog node
		// cost of execution and cost of data transfer depends on the type of application accessed from mobile device
		Map<Double, List<NodeSim>> costMap = new HashMap<>();
		NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(mobile.getLocation(), false);
		
		// get the set of paths for all nodes from current location of mobile device
		Map<NodeSim, LinkedList<NodeSim>> desMap = pathTable.get(src);
		
		// Prune the set of paths to include only the prospective fog nodes from given selectedNodes list
		Map<NodeSim, LinkedList<NodeSim>> selectedDesMap = new HashMap<>();
		// get the list of host ids
		SimLogger.print("List of given Hosts: ");
		for (EdgeHost selLayerHost : selLayerHosts) {
			SimLogger.print(selLayerHost.getId()+" ");
			// for each host id, get the NodeSim object
			Location hostLoc = selLayerHost.getLocation();
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
				//des = ((ESBModel)(SimManager.getInstance().getNetworkModel())).getNetworkTopology().findNode(task.getSubmittedLocation(), false);
				//cost = (mobile.getTaskLengthRequirement() / k.getTotalMips() * k.getCostPerSec() + mobile.getBWRequirement() * k.getCostPerBW());
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
				}
				//des = path.peekLast();
				EdgeHost desHost = SimManager.getInstance().getLocalServerManager().findHostByLoc(des.getLocation().getXPos(), des.getLocation().getYPos(), des.getLocation().getAltitude());
				//double exCost = desHost.getCostPerSec() * (mobile.getTaskLengthRequirement() / desHost.getTotalMips());
				double exCost = (double)mobile.getTaskLengthRequirement() / (desHost.getPeList().get(0).getMips()) * desHost.getCostPerSec(); // Shaik modified - May 07, 2019.

				cost = cost + exCost;
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
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
		EdgeHost selHost = null;
		selHost = hostsSortedByCost.poll();
		
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
	}

	
	/**
	 * @return the hostLevelMap
	 */
	public Map<Integer, ArrayList<EdgeHost>> getHostLevelMap() {
		return hostLevelMap;
	}

	
	/**
	 * @param hostLevelMap the hostLevelMap to set
	 */
	public void setHostLevelMap(Map<Integer, ArrayList<EdgeHost>> hostLevelMap) {
		this.hostLevelMap = hostLevelMap;
	}
	
	
	/**
	 * 
	 * @param deviceId
	 * @param hostCount
	 */
	public void addNumProspectiveHosts(int deviceId, int hostCount) {
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumProspectiveHosts() {
		return ((double)this.avgNumProspectiveHosts);		
	}
		

	/**
	 * @param deviceId
	 * @param msgCount
	 */
	public void addNumMessages(int deviceId, int msgCount) {
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumMessages() {
		return ((double)this.avgNumMessages);		
	}
		
	
	/**
	 * 
	 * @param deviceId
	 * @param pudCount
	 */
	public void addNumPuddlesSearched(int deviceId, int pudCount) {
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumPuddlesSearched() {
		return ((double)0);		
	}
	
	
}
