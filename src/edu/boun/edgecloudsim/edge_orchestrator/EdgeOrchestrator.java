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
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.Task;


/**
 * 
 * @author szs0117
 *
 */
public abstract class EdgeOrchestrator {
	protected String policy;
	protected String simScenario;
	protected Datacenter cloud;
	
	// Metrics
	protected int avgNumMessages;
	protected int avgNumProspectiveHosts;
	
	public EdgeOrchestrator(String _policy, String _simScenario){
		policy = _policy;
		simScenario = _simScenario;
		this.avgNumProspectiveHosts = 0;
		this.avgNumMessages = 0;		
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
		//if (host == null)
			//return false;
		
		System.out.print(host.getId()+" ");
		if (!host.isMIPSAvailable(mb) || !host.isBWAvailable(mb) || !host.isLatencySatisfactory(mb)) {
			return false;
		}
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(host, mb);
		for (NodeSim node: path) {
			EdgeHost tempHost = SimManager.getInstance().getLocalServerManager().findHostByWlanId(node.getLocation().getServingWlanId());
			if (!tempHost.isBWAvailable(mb)) {
				return false;
			}
		}
		System.out.println(" is good.");
		return true;
		
	}
	
	
	/**
	 * 
	 * @param _cloud
	 */
	public void setCloud(Datacenter _cloud ) {
		cloud = _cloud;
	}

	
	/**
	 * @author Qian
	 *	@param mobile
	 */
	public abstract void assignHost(MobileDevice mobile);

	
	/**
	 * @return the policy
	 */
	public String getPolicy() {
		return policy;
	}

	
	/**
	 * @param policy the policy to set
	 */
	public void setPolicy(String policy) {
		this.policy = policy;
	}

	
	/**
	 * @return the simScenario
	 */
	public String getSimScenario() {
		return simScenario;
	}

	
	/**
	 * @param simScenario the simScenario to set
	 */
	public void setSimScenario(String simScenario) {
		this.simScenario = simScenario;
	}

	
	/**
	 * @return the cloud
	 */
	public Datacenter getCloud() {
		return cloud;
	}
	
	
	/**
	 * 
	 * @param deviceId
	 * @param hostCount
	 */
	public abstract void addNumProspectiveHosts(int deviceId, int hostCount);
	
		
	/**
	 * 
	 * @return
	 */
	public abstract double getAvgNumProspectiveHosts();

		
	/**
	 * 
	 * @param deviceId
	 * @param msgCount
	 */
	public abstract void addNumMessages (int deviceId, int msgCount);
	
	
	/**
	 * 
	 * @return
	 */
	public abstract double getAvgNumMessages();
	
	
	/**
	 * 
	 * @param deviceId
	 * @param pudCount
	 */
	public abstract void addNumPuddlesSearched(int deviceId, int pudCount);
	
	
	/**
	 * 
	 * @return
	 */
	public abstract double getAvgNumPuddlesSearched();
	
	
	/**
	 * Return detailed metrics of HAFA orchestrator - Number of prospective hosts per service request (device)
	 */
	public int[] getNumProspectiveHosts() {	return null; }
	

	/**
	 * Return detailed metrics of HAFA orchestrator - Number of messages exchanged per service request (device)
	 */
	public int[] getNumMessages() {	return null; }


	/**
	 * Return detailed metrics of HAFA orchestrator - Number of Puddles searched per service request (device)
	 */
	public int[] getNumPuddlesSearched() { return null; }

	
	
}
