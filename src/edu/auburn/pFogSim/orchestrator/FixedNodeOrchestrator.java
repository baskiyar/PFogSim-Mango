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
import edu.boun.edgecloudsim.edge_server.EdgeVM;

public class FixedNodeOrchestrator extends EdgeOrchestrator{
	
	private static String node = "Datacenter_1";
	
	public FixedNodeOrchestrator(String _policy, String _simScenario) {
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
		//Qian confirm the cloud level.
		//SimLogger.printLine("cloud level: "+((EdgeHost) cloud.getHostList().get(0)).getLevel());
		NodeSim des = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getLocalServerManager().findHostById(cloud.getHostList().get(0).getId()).getLocation(), false);
		NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()), false);
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(src, des);
		task.setPath(path);
		return ((EdgeVM) cloud.getHostList().get(0).getVmList().get(0));
	}
	
	@Override
	public void setCloud(Datacenter _cloud ) {
		if (_cloud.getName().equals(node)) {
			cloud = _cloud;
		}
	}
	
	
}
