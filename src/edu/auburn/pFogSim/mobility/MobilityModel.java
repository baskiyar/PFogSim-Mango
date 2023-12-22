/*
 * Title:        EdgeCloudSim - Mobility Model
 * 
 * Description: 
 * MobilityModel is an abstract class which is used for calculating the
 * location of each mobile devices with respect to the time. For those who
 * wants to add a custom Mobility Model to EdgeCloudSim should extend
 * this class and provide a concrete instance via ScenarioFactory
 *               
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.auburn.pFogSim.mobility; //This line was the only changed by Auburn

import edu.boun.edgecloudsim.utils.Location;


/**
 * 
 * @author szs0117
 *
 */
public abstract class MobilityModel {
	protected int numberOfMobileDevices;
	protected double simulationTime;
	
	
	/**
	 * 
	 * @param _numberOfMobileDevices
	 * @param _simulationTime
	 */
	public MobilityModel(int _numberOfMobileDevices, double _simulationTime){
		numberOfMobileDevices=_numberOfMobileDevices;
		simulationTime=_simulationTime;
	};
	
	
	/*
	 * calculate location of the devices according to related mobility model
	 */
	public abstract void initialize();
	
	
	/*
	 * returns location of a device at a certain time
	 */
	public abstract Location getLocation(int deviceId, double time);
	public abstract int getWlanId(int deviceId, double time);
	public abstract int getWlanId(int deviceId);
	public abstract int getSize();
	public abstract int getTreeMapSize();

	
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
	

	/**
	 * @return the simulationTime
	 */
	public double getSimulationTime() {
		return simulationTime;
	}

	
	/**
	 * @param simulationTime the simulationTime to set
	 */
	public void setSimulationTime(double simulationTime) {
		this.simulationTime = simulationTime;
	}
}
