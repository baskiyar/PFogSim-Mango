package edu.auburn.pFogSim.orchestrator;

import java.util.*;

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

public class SelectedLevelOrchestrator extends EdgeOrchestrator {
	
	private Map<Integer, ArrayList<EdgeHost>> hostLevelMap;
	
	public SelectedLevelOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
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
	
	private EdgeHost getHost(Task task) {
		int level = SimSettings.getInstance().nextSelectedLevel();
		ArrayList<EdgeHost> hosts = hostLevelMap.get(level);
		DistRadix sort = new DistRadix(hosts, task.getSubmittedLocation());//use radix sort based on distance from task
		LinkedList<EdgeHost> nodes = sort.sortNodesByLatency();
		EdgeHost host = nodes.poll();
		/*
		 * for (int i = 0; !goodHost(host, task) && i < 10; i++) {
		 *     host = nodes.poll();
		 * }
		 */
		while(!goodHost(host, task)) {
			host = nodes.poll();//find the closest node capable of handling the task
		}
		NodeSim des = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getLocalServerManager().findHostById(host.getId()).getLocation(), false);
		NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()), false);
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(src, des);
		task.setPath(path);
		return host;
	}

	/* (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
		// TODO Auto-generated method stub
		
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
}
