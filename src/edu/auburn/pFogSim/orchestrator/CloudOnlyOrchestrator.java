/**
 * alternate orchestrator for comparison against the puddle orchestrator
 * 
 * this orchestrator only assigns tasks to the cloud
 */

package edu.auburn.pFogSim.orchestrator;

import java.util.LinkedList;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.SimLogger;

public class CloudOnlyOrchestrator extends EdgeOrchestrator {

	
	public CloudOnlyOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}
	
	@Override
	public void initialize() {

	}

	@Override
	public int getDeviceToOffload(Task task) {
		NodeSim des = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getLocalServerManager().findHostById(cloud.getHostList().get(0).getId()).getLocation(), false);
		NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()), false);
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(src, des);
		task.setPath(path);
		return cloud.getHostList().get(0).getId();
	}

	@Override
	public EdgeVM getVmToOffload(Task task) {
		NodeSim des = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getLocalServerManager().findHostById(cloud.getHostList().get(0).getId()).getLocation(), false);
		NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()), false);
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(src, des);
		task.setPath(path);
		//Qian confirm the cloud level.
		SimLogger.printLine("cloud level: "+((EdgeHost) cloud.getHostList().get(0)).getLevel());
		return ((EdgeVM) cloud.getHostList().get(0).getVmList().get(0));
	}
	
}
