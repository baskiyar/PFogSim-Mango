/*
 * Title:        EdgeCloudSim - EdgeTask
 * 
 * Description: 
 * A custom class used in Load Generator Model to store tasks information
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.utils;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.APP_TYPES;


/**
 * 
 * @author szs0117
 *
 */
public class EdgeTask {
    public APP_TYPES taskType;
    public double startTime;
    public long length, inputFileSize, outputFileSize;
    public int pesNumber;
    public int mobileDeviceId;
    public int desMobileDeviceId; // added by Qian for sperating data sources and consumers.
    public boolean wifi; //added by pFogSim for asking whether a task requires a wifi access point
    public boolean sens; //added by pFogSim to say whether a device is a sensor
    public boolean act;  //added by pFogSim to say whether a device is an actuator
    
    
    /**
     * 
     * @param _mobileDeviceId
     * @param _taskType
     * @param _startTime
     * @param expRngList
     * @param _wifi
     * @param _sens
     * @param _act
     */
    public EdgeTask(int _mobileDeviceId, APP_TYPES _taskType, double _startTime, ExponentialDistribution[][] expRngList, 
    				boolean _wifi, boolean _sens, boolean _act) {
    	mobileDeviceId=_mobileDeviceId;
    	startTime=_startTime;
    	taskType=_taskType;
    	
    	inputFileSize = (long)expRngList[_taskType.ordinal()][0].sample();
    	outputFileSize =(long)expRngList[_taskType.ordinal()][1].sample();
    	length = (long)expRngList[_taskType.ordinal()][2].sample();
    	
    	pesNumber = (int)SimSettings.getInstance().getTaskLookUpTable()[_taskType.ordinal()][8];
    	
    	wifi = _wifi;
    	sens =  _sens;
    	act = _act;
	}
    
    
    /**
     * 
     * @param deviceId
     */
    public void setDesMobileDeviceId(int deviceId) {
    	this.desMobileDeviceId = deviceId;
    }

    
	/**
	 * @return the taskType
	 */
	public APP_TYPES getTaskType() {
		return taskType;
	}

	
	/**
	 * @param taskType the taskType to set
	 */
	public void setTaskType(APP_TYPES taskType) {
		this.taskType = taskType;
	}

	
	/**
	 * @return the startTime
	 */
	public double getStartTime() {
		return startTime;
	}

	
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	
	/**
	 * @return the length
	 */
	public long getLength() {
		return length;
	}

	
	/**
	 * @param length the length to set
	 */
	public void setLength(long length) {
		this.length = length;
	}

	
	/**
	 * @return the inputFileSize
	 */
	public long getInputFileSize() {
		return inputFileSize;
	}

	
	/**
	 * @param inputFileSize the inputFileSize to set
	 */
	public void setInputFileSize(long inputFileSize) {
		this.inputFileSize = inputFileSize;
	}

	
	/**
	 * @return the outputFileSize
	 */
	public long getOutputFileSize() {
		return outputFileSize;
	}

	
	/**
	 * @param outputFileSize the outputFileSize to set
	 */
	public void setOutputFileSize(long outputFileSize) {
		this.outputFileSize = outputFileSize;
	}

	
	/**
	 * @return the pesNumber
	 */
	public int getPesNumber() {
		return pesNumber;
	}

	
	/**
	 * @param pesNumber the pesNumber to set
	 */
	public void setPesNumber(int pesNumber) {
		this.pesNumber = pesNumber;
	}

	
	/**
	 * @return the mobileDeviceId
	 */
	public int getMobileDeviceId() {
		return mobileDeviceId;
	}

	
	/**
	 * @param mobileDeviceId the mobileDeviceId to set
	 */
	public void setMobileDeviceId(int mobileDeviceId) {
		this.mobileDeviceId = mobileDeviceId;
	}

	
	/**
	 * @return the wifi
	 */
	public boolean isWifi() {
		return wifi;
	}

	
	/**
	 * @param wifi the wifi to set
	 */
	public void setWifi(boolean wifi) {
		this.wifi = wifi;
	}

	
	/**
	 * @return the sens
	 */
	public boolean isSens() {
		return sens;
	}

	
	/**
	 * @param sens the sens to set
	 */
	public void setSens(boolean sens) {
		this.sens = sens;
	}

	
	/**
	 * @return the act
	 */
	public boolean isAct() {
		return act;
	}

	
	/**
	 * @param act the act to set
	 */
	public void setAct(boolean act) {
		this.act = act;
	}

	
	/**
	 * @return the desMobileDeviceId
	 */
	public int getDesMobileDeviceId() {
		return desMobileDeviceId;
	}
}
