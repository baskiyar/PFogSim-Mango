/*
 * Title:        EdgeCloudSim - Task
 * 
 * Description: 
 * Task adds app type, task submission location, mobile device id and host id
 * information to CloudSim's Cloudlet class.
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_client;

import java.util.LinkedList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Location;

public class Task extends Cloudlet {
	private SimSettings.APP_TYPES type;
	private Location submittedLocation;
	private int mobileDeviceId;
	private int hostIndex;
	private double maxDelay;
	private LinkedList<NodeSim> path; // Qian the path from start to destination  
	public boolean wifi; //added by pFogSim for asking whether a task requires a wifi access point
    public boolean sens; //added by pFogSim to say whether a device is a sensor
    public boolean act;  //added by pFogSim to say whether a device is an actuator

	public Task(int _mobileDeviceId, int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, boolean _wifi, boolean _sens, boolean _act) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		
		mobileDeviceId = _mobileDeviceId;
		wifi = _wifi;
    	sens =  _sens;
    	act = _act;
    	maxDelay = 0.600;
    	path = null;
	}

	
	public void setSubmittedLocation(Location _submittedLocation){
		submittedLocation =_submittedLocation;
	}
	
	public void setAssociatedHostId(int _hostIndex){
		hostIndex=_hostIndex;
	}

	public void setTaskType(SimSettings.APP_TYPES _type){
		type=_type;
	}

	public int getMobileDeviceId(){
		return mobileDeviceId;
	}
	
	public Location getSubmittedLocation(){
		return submittedLocation;
	}
	
	public int getAssociatedHostId(){
		return hostIndex;
	}

	public SimSettings.APP_TYPES getTaskType(){
		return type;
	}
	/**
	 * get the maximum latency that this task will tolerate in seconds. default value is 0.600 seconds (600 miliseconds)
	 * @return
	 */
	public double getMaxDelay() {
		return maxDelay;
	}
	/**
	 * set the maximum latency that this task will tolerate
	 * @param in
	 */
	public void setMaxDelay(double in) {
		maxDelay = in;
	}
	
	/**
	 * set path for task
	 * @param LinkedList path form source to destination
	 */
	public void setPath(LinkedList<NodeSim> _path) {
		this.path = _path;
	}
	
	/**
	 * get path
	 */
	public LinkedList<NodeSim> getPath() {
		return path;
	}
}	
