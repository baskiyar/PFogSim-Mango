/*
 * Title:        EdgeCloudSim - EdgeVM
 * 
 * Description: 
 * EdgeVM adds vm type information over CloudSim's VM class
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */
/**
 * Add new instance variable application services manager
 * Modify by Qian Wang 9/18/2018.
 */
package edu.boun.edgecloudsim.edge_server;


import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;
import edu.auburn.pFogSim.util.ApplicationServicesManager;
import edu.boun.edgecloudsim.core.SimSettings;

public class EdgeVM extends Vm {
	private SimSettings.VM_TYPES type;
	private ApplicationServicesManager appServicesManager;
	
	public EdgeVM(int id, int userId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		appServicesManager = new ApplicationServicesManager();
	}

	public void setVmType(SimSettings.VM_TYPES _type){
		type=_type;
	}
	
	public SimSettings.VM_TYPES getVmType(){
		return type;
	}
	
	/**
	 * Application services manager getter
	 * @return appServiceManamger
	 */
	public ApplicationServicesManager getApplicationServicesManager() {
		return appServicesManager;
	}
}
