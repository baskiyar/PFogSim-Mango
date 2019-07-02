/*
 * Title:        EdgeCloudSim - Basic Edge Orchestrator implementation
 * 
 * Description: 
 * BasicEdgeOrchestrator implements basic algorithms which are
 * first/next/best/worst/random fit algorithms while assigning
 * requests to the edge devices.
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_orchestrator;

import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimUtils;


/**
 * 
 * @author szs0117
 *
 */
public class BasicEdgeOrchestrator extends EdgeOrchestrator {
	private int numberOfHost; //used by load balancer
	private int lastSelectedHostIndex; //used by load balancer
	private int[] lastSelectedVmIndexes; //used by each host individually
	
	
	/**
	 * 
	 * @param _policy
	 * @param _simScenario
	 */
	public BasicEdgeOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}

	
	/**
	 * 
	 */
	@Override
	public void initialize() {
		numberOfHost=SimSettings.getInstance().getNumOfEdgeHosts();
		
		lastSelectedHostIndex = -1;
		lastSelectedVmIndexes = new int[numberOfHost];
		for(int i=0; i<numberOfHost; i++)
			lastSelectedVmIndexes[i] = -1;
	}

	
	/**
	 * 
	 */
	@Override
	public int getDeviceToOffload(Task task) {
		int result = SimSettings.GENERIC_EDGE_DEVICE_ID;
		if(!simScenario.equals("SINGLE_TIER")){
			//decide to use cloud or cloudlet VM
			int CloudVmPicker = SimUtils.getRandomNumber(0, 100);
			
			if(CloudVmPicker <= SimSettings.getInstance().getTaskLookUpTable()[task.getTaskType().ordinal()][1])
				result = SimSettings.CLOUD_DATACENTER_ID;
			else
				result = SimSettings.GENERIC_EDGE_DEVICE_ID;
		}
		
		return result;
	}
	
	
	/**
	 * 
	 */
	@Override
	public EdgeVM getVmToOffload(Task task) {
		if(simScenario.equals("TWO_TIER_WITH_EO"))
			return selectVmOnLoadBalancer(task);
		else
			return selectVmOnHost(task);
	}
	
	
	/**
	 * 
	 * @param task
	 * @return
	 */
	public EdgeVM selectVmOnHost(Task task){
		EdgeVM selectedVM = null;
		
		Location deviceLocation = SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(), CloudSim.clock());
		//in our scenarios, serving wlan ID is equal to the host id
		//because there is only one host in one place
		int relatedHostId=deviceLocation.getServingWlanId();
		List<EdgeVM> vmArray = SimManager.getInstance().getLocalServerManager().getVmList(relatedHostId);
		
		if(policy.equalsIgnoreCase("RANDOM_FIT")){
			int randomIndex = SimUtils.getRandomNumber(0, vmArray.size()-1);
			double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(randomIndex).getVmType());
			double targetVmCapacity = (double)100 - vmArray.get(randomIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
			if(requiredCapacity <= targetVmCapacity)
				selectedVM = vmArray.get(randomIndex);
		}
		else if(policy.equalsIgnoreCase("WORST_FIT")){
			double selectedVmCapacity = 0; //start with min value
			for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
				double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
				double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
				if(requiredCapacity <= targetVmCapacity && targetVmCapacity > selectedVmCapacity){
					selectedVM = vmArray.get(vmIndex);
					selectedVmCapacity = targetVmCapacity;
				}
			}
		}
		else if(policy.equalsIgnoreCase("BEST_FIT")){
			double selectedVmCapacity = 101; //start with max value
			for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
				double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
				double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
				if(requiredCapacity <= targetVmCapacity && targetVmCapacity < selectedVmCapacity){
					selectedVM = vmArray.get(vmIndex);
					selectedVmCapacity = targetVmCapacity;
				}
			}
		}
		else if(policy.equalsIgnoreCase("FIRST_FIT")){
			for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
				double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
				double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
				if(requiredCapacity <= targetVmCapacity){
					selectedVM = vmArray.get(vmIndex);
					break;
				}
			}
		}
		else if(policy.equalsIgnoreCase("NEXT_FIT")){
			int tries = 0;
			while(tries < vmArray.size()){
				lastSelectedVmIndexes[relatedHostId] = (lastSelectedVmIndexes[relatedHostId]+1) % vmArray.size();
				double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(lastSelectedVmIndexes[relatedHostId]).getVmType());
				double targetVmCapacity = (double)100 - vmArray.get(lastSelectedVmIndexes[relatedHostId]).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
				if(requiredCapacity <= targetVmCapacity){
					selectedVM = vmArray.get(lastSelectedVmIndexes[relatedHostId]);
					break;
				}
				tries++;
			}
		}
		
		return selectedVM;
	}

	
	/**
	 * 
	 * @param task
	 * @return
	 */
	public EdgeVM selectVmOnLoadBalancer(Task task){
		EdgeVM selectedVM = null;
		
		if(policy.equalsIgnoreCase("RANDOM_FIT")){
			int randomHostIndex = SimUtils.getRandomNumber(0, numberOfHost-1);
			List<EdgeVM> vmArray = SimManager.getInstance().getLocalServerManager().getVmList(randomHostIndex);
			int randomIndex = SimUtils.getRandomNumber(0, vmArray.size()-1);
			
			double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(randomIndex).getVmType());
			double targetVmCapacity = (double)100 - vmArray.get(randomIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
			if(requiredCapacity <= targetVmCapacity)
				selectedVM = vmArray.get(randomIndex);
		}
		else if(policy.equalsIgnoreCase("WORST_FIT")){
			double selectedVmCapacity = 0; //start with min value
			for(int hostIndex=0; hostIndex<numberOfHost; hostIndex++){
				List<EdgeVM> vmArray = SimManager.getInstance().getLocalServerManager().getVmList(hostIndex);
				for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
					double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
					double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
					if(requiredCapacity <= targetVmCapacity && targetVmCapacity > selectedVmCapacity){
						selectedVM = vmArray.get(vmIndex);
						selectedVmCapacity = targetVmCapacity;
					}
				}
			}
		}
		else if(policy.equalsIgnoreCase("BEST_FIT")){
			double selectedVmCapacity = 101; //start with max value
			for(int hostIndex=0; hostIndex<numberOfHost; hostIndex++){
				List<EdgeVM> vmArray = SimManager.getInstance().getLocalServerManager().getVmList(hostIndex);
				for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
					double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
					double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
					if(requiredCapacity <= targetVmCapacity && targetVmCapacity < selectedVmCapacity){
						selectedVM = vmArray.get(vmIndex);
						selectedVmCapacity = targetVmCapacity;
					}
				}
			}
		}
		else if(policy.equalsIgnoreCase("FIRST_FIT")){
			for(int hostIndex=0; hostIndex<numberOfHost; hostIndex++){
				List<EdgeVM> vmArray = SimManager.getInstance().getLocalServerManager().getVmList(hostIndex);
				for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
					double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
					double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
					if(requiredCapacity <= targetVmCapacity){
						selectedVM = vmArray.get(vmIndex);
						break;
					}
				}
			}
		}
		else if(policy.equalsIgnoreCase("NEXT_FIT")){
			int hostCheckCounter = 0;	
			while(selectedVM == null && hostCheckCounter < numberOfHost){
				int tries = 0;
				lastSelectedHostIndex = (lastSelectedHostIndex+1) % numberOfHost;

				List<EdgeVM> vmArray = SimManager.getInstance().getLocalServerManager().getVmList(lastSelectedHostIndex);
				while(tries < vmArray.size()){
					lastSelectedVmIndexes[lastSelectedHostIndex] = (lastSelectedVmIndexes[lastSelectedHostIndex]+1) % vmArray.size();
					double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(lastSelectedVmIndexes[lastSelectedHostIndex]).getVmType());
					double targetVmCapacity = (double)100 - vmArray.get(lastSelectedVmIndexes[lastSelectedHostIndex]).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
					if(requiredCapacity <= targetVmCapacity){
						selectedVM = vmArray.get(lastSelectedVmIndexes[lastSelectedHostIndex]);
						break;
					}
					tries++;
				}

				hostCheckCounter++;
			}
		}
		
		return selectedVM;
	}

	
	/**
	 * @return the numberOfHost
	 */
	public int getNumberOfHost() {
		return numberOfHost;
	}

	
	/**
	 * @param numberOfHost the numberOfHost to set
	 */
	public void setNumberOfHost(int numberOfHost) {
		this.numberOfHost = numberOfHost;
	}

	
	/**
	 * @return the lastSelectedHostIndex
	 */
	public int getLastSelectedHostIndex() {
		return lastSelectedHostIndex;
	}

	
	/**
	 * @param lastSelectedHostIndex the lastSelectedHostIndex to set
	 */
	public void setLastSelectedHostIndex(int lastSelectedHostIndex) {
		this.lastSelectedHostIndex = lastSelectedHostIndex;
	}

	
	/**
	 * @return the lastSelectedVmIndexes
	 */
	public int[] getLastSelectedVmIndexes() {
		return lastSelectedVmIndexes;
	}

	
	/**
	 * @param lastSelectedVmIndexes the lastSelectedVmIndexes to set
	 */
	public void setLastSelectedVmIndexes(int[] lastSelectedVmIndexes) {
		this.lastSelectedVmIndexes = lastSelectedVmIndexes;
	}


	/**
	 * 
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addNumProspectiveHosts(int deviceId, int hostCount) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public double getAvgNumProspectiveHosts() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void addNumMessages(int deviceId, int msgCount) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public double getAvgNumMessages() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void addNumPuddlesSearched(int deviceId, int pudCount) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public double getAvgNumPuddlesSearched() {
		// TODO Auto-generated method stub
		return 0;
	}
}