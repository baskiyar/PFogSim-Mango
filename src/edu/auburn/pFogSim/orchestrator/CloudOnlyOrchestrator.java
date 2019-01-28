/**
 * alternate orchestrator for comparison against the puddle orchestrator
 * 
 * this orchestrator only assigns tasks to the cloud
 */

package edu.auburn.pFogSim.orchestrator;

import java.util.ArrayList;
import java.util.LinkedList;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.SimLogger;

public class CloudOnlyOrchestrator extends EdgeOrchestrator {

	private static String node = "Datacenter_0";
	EdgeHost cHost;
	
	public CloudOnlyOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}
	
	@Override
	public void initialize() {
		cHost = (EdgeHost)(SimManager.getInstance().getLocalServerManager().findHostById(4));
	}

	/**
	 * get the id of the appropriate host
	 */
	@Override
	public int getDeviceToOffload(Task task) {
		try {
			System.out.println("Task: Cloud orchestrator: assigned Host Id:  " + getHost(task).getId());
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
			EdgeVM assignedVm = (EdgeVM) (getHost(task).getVmList().get(0)); 
			System.out.println("Task: Cloud orchestrator: assigned VM Id:  " + assignedVm.getId());
			return assignedVm;
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
		MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(task.getMobileDeviceId());
		task.setPath(mb.getPath());
		return mb.getHost();
	}
	/**
	 * set cloud
	 * modified by Qian
	 * @param Datacenter _cloud
	 */
	@Override
	public void setCloud(Datacenter _cloud) {
		if (_cloud.getName().equals(node)) {
			cloud = _cloud;
		}
	}

	/* 
	 * @ author Qian Wang
	 * @ author Shehenaz Shaik
	 * (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
 
		EdgeHost cloudHost = (EdgeHost) cloud.getHostList().get(0);
		cloudHost = (EdgeHost)cHost; // Shaik added - for test
		if (goodHost(cloudHost, mobile)) {
			LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(cloudHost, mobile);
			mobile.setPath(path);
			mobile.setHost(cloudHost);
			mobile.makeReservation();
		}
		else
			System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
	}

	/**
	 * @return the node
	 */
	public static String getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public static void setNode(String node) {
		CloudOnlyOrchestrator.node = node;
	}
	
}
