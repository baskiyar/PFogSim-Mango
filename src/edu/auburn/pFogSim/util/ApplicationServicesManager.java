package edu.auburn.pFogSim.util;

import java.util.ArrayList;
import java.util.List;

import edu.boun.edgecloudsim.core.SimSettings;


/**
 * This class is a Application manager used by EdgeVM class
 * @author Qian Wang
 * version 1.0
 */
public class ApplicationServicesManager {
	private List<ApplicationService> appServicesList;
	
	
	/**
	 * constructor
	 */
	public ApplicationServicesManager() {
		appServicesList = new ArrayList<ApplicationService>();
	}
	
	
	/**
	 * add a new application service to VM
	 * @param serviceId
	 * @param applicationId
	 * @param applicationType
	 */
	public void addNewAppService(int serviceId, int applicationId, SimSettings.APP_TYPES applicationType) {
		appServicesList.add(new ApplicationService(serviceId, applicationType));
	}
	
	
	/**
	 * Application services list getter
	 * @return appServicesList
	 */
	public List<ApplicationService> getAppServicesList() {
		return appServicesList;
	}

	
	/**
	 * @param appServicesList the appServicesList to set
	 */
	public void setAppServicesList(List<ApplicationService> appServicesList) {
		this.appServicesList = appServicesList;
	}
	
}
