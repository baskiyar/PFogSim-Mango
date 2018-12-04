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

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.CloudSim;

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
	protected static boolean goodHost(EdgeHost host, Task task) {
		double hostCap = 100.0 - host.getVmList().get(0).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
		double taskCap = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(((EdgeVM) host.getVmList().get(0)).getVmType());
		return hostCap >= taskCap;
	}
	
	public void setCloud(Datacenter _cloud ) {
		cloud = _cloud;
	}
}
