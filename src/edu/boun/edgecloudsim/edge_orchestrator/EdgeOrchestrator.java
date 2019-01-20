/*
 * Title:        EdgeCloudSim - Edge Orchestrator
 * 
 * Description: 
 * EdgeOrchestrator is an abstract class which is used for selecting VM
 * for each client requests. For those who wants to add a custom 
 * Edge Orchestrator to EdgeCloudSim should extend this class and provide
 * a concreate instance via ScenarioFactory
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_orchestrator;

import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;

import java.util.LinkedList;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;

public abstract class EdgeOrchestrator {
	protected String policy;
	protected String simScenario;
	protected Datacenter cloud;
	
	public EdgeOrchestrator(String _policy, String _simScenario){
		policy = _policy;
		simScenario = _simScenario;
	}
	
	/*
	 * initialize edge orchestrator if needed
	 */
	public abstract void initialize();
	
	/*
	 * decides where to offload
	 */
	public abstract int getDeviceToOffload(Task task);
	
	/*
	 * returns proper VM from the related edge orchestrator point of view
	 */
	public abstract EdgeVM getVmToOffload(Task task);
	
	/**
	 * is the host capable of servicing the task
	 * @param host
	 * @param task
	 * @return
	 */
	protected static boolean goodHost(EdgeHost host, MobileDevice mb) {
		if (!host.isMIPSAvailable(mb) || !host.isBWAvailable(mb)) {
			return false;
		}
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(host, mb);
		for (NodeSim node: path) {
			EdgeHost tempHost = SimManager.getInstance().getLocalServerManager().findHostByWlanId(node.getLocation().getServingWlanId());
			if (!tempHost.isBWAvailable(mb)) {
				return false;
			}
		}
		return true;
		
	}
	
	public void setCloud(Datacenter _cloud ) {
		cloud = _cloud;
	}

	/**
	 * @author Qian
	 *	@param mobile
	 */
	public abstract void assignHost(MobileDevice mobile);
}
