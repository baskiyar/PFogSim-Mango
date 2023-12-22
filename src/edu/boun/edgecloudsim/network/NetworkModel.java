/*
 * Title:        EdgeCloudSim - Network Model
 * 
 * Description: 
 * NetworkModel is an abstract class which is used for calculating the
 * network delay from device to device. For those who wants to add a
 * custom Network Model to EdgeCloudSim should extend this class and
 * provide a concrete instance via ScenarioFactory
 *               
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.network;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Location;


/**
 * 
 * @author szs0117
 *
 */
public abstract class NetworkModel {
	protected int numberOfMobileDevices;

	
	/**
	 * 
	 */
	public NetworkModel() {}
	
	
	/**
	 * 
	 * @param _numberOfMobileDevices
	 */
	public NetworkModel(int _numberOfMobileDevices){
		numberOfMobileDevices=_numberOfMobileDevices;
	};
	
	
	/**
	* initializes costom network model
	*/
	public abstract void initialize();
	
	
    /**
    * calculates the upload delay from source to destination device
    */
	// Shaik updated
	public abstract double getUploadDelay(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest, SimSettings.CLOUD_TRANSFER isCloud);
	
	
    /**
    * calculates the download delay from source to destination device
    */
	//Shaik updated
	public abstract double getDownloadDelay(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest, SimSettings.CLOUD_TRANSFER isCloud);
	
	
    /**
    * Mobile device manager should inform network manager about the network operation
    * This information may be important for some network delay models
    */
	public abstract void uploadStarted(Location accessPointLocation, int destDeviceId);
	public abstract void uploadFinished(Location accessPointLocation, int destDeviceId);
	public abstract void downloadStarted(Location accessPointLocation, int sourceDeviceId);
	public abstract void downloadFinished(Location accessPointLocation, int sourceDeviceId);
	
	
	/**
	 * @return the numberOfMobileDevices
	 */
	public int getNumberOfMobileDevices() {
		return numberOfMobileDevices;
	}
	
	
	/**
	 * @param numberOfMobileDevices the numberOfMobileDevices to set
	 */
	public void setNumberOfMobileDevices(int numberOfMobileDevices) {
		this.numberOfMobileDevices = numberOfMobileDevices;
	}
}
