package edu.auburn.pFogSim.util;
import edu.boun.edgecloudsim.core.SimSettings;


/**
 * This class is application service class. For every fog node, it has one VM.
 * Inside this VM, there are several application services.
 * Each application service handle one application task (cloudlet in CloudSim).
 * @author Qian Wang
 * version: 1.1
 */
public class ApplicationService {
	private int serviceId;
	private SimSettings.APP_TYPES type;
	private boolean avaliable;
	
	
	/**
	 * ApplicaitonService constructor
	 * @param serviceId
	 * @param applicationId
	 * @param applicationType
	 */
	public ApplicationService(int serviceId, SimSettings.APP_TYPES applicationType) {
		this.serviceId = serviceId;
		this.type = applicationType;
		avaliable = true;
	}
	
	
	/**
	 * serviceId getter
	 * @return serviceId
	 */
	public int getServiceId() {
		return serviceId;
	}
	
	
	/**
	 * serviceId setter
	 * @param serviceId
	 */
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	
	
	/**
	 * ApplicationType getter
	 * @return ApplicationType
	 */
	public SimSettings.APP_TYPES getApplicationType() {
		return type;
	}
	
	
	/**
	 * ApplicationType setter
	 * @param applicationType
	 */
	public void setApplicationType(SimSettings.APP_TYPES applicationType) {
		this.type = applicationType;
	}
	
	
	/**
	 * Check application service is available or not
	 */
	public boolean isAvaliable() {
		return avaliable;
	}
	
	
	/**
	 * Set availability of this Application Service
	 * @param avaliable
	 */
	public void setAvaliable(boolean avaliable) {
		this.avaliable = avaliable;
	}

	
	/**
	 * @return the type
	 */
	public SimSettings.APP_TYPES getType() {
		return type;
	}

	
	/**
	 * @param type the type to set
	 */
	public void setType(SimSettings.APP_TYPES type) {
		this.type = type;
	}

}
