/**
 * Centralized Orchestrator for comparison against Puddle algorithm
 * 
 * This orchestrator uses the centralized approach to selecting a VM but never associates a task to the cloud
 * @author jih0007@auburn.edu
 */
package edu.auburn.pFogSim.orchestrator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.Radix.DistRadix;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

public class EdgeOnlyOrchestrator extends EdgeOrchestrator {

	ArrayList<EdgeHost> hosts;
	
	public EdgeOnlyOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}
	/**
	 * get all the hosts in the network into one list
	 */
	@Override
	public void initialize() {
		hosts = new ArrayList<EdgeHost>();
		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			if (((EdgeHost) node.getHostList().get(0)).getLevel() == 1) {
				hosts.add(((EdgeHost) node.getHostList().get(0)));
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
	/**
	 * find the host
	 * @param task
	 * @return
	 */
	private EdgeHost getHost(Task task) {
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

}
