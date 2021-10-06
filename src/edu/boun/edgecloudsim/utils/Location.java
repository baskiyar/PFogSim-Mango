/*
 * Title:        EdgeCloudSim - Location
 * 
 * Description:  Location class used in EdgeCloudSim
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.utils;

import edu.boun.edgecloudsim.core.SimSettings;


/**
 * 
 * @author szs0117
 *
 */
public class Location implements Comparable {
	private double xPos;
	private double yPos;
	private double altitude;
	private int servingWlanId;
	private double bandwidth;
	SimSettings.PLACE_TYPES placeType;
	
	
	/**
	 * 
	 * @param _placeType
	 * @param _servingWlanId
	 * @param _xPos
	 * @param _yPos
	 */
	public Location(SimSettings.PLACE_TYPES _placeType, int _servingWlanId, double _xPos, double _yPos){
		servingWlanId = _servingWlanId;
		placeType=_placeType;
		xPos = _xPos;
		yPos = _yPos;
	}
	
	
	/**
	 * 
	 * @param _servingWlanId
	 * @param _xPos
	 * @param _yPos
	 */
	public Location(int _servingWlanId, double _xPos, double _yPos, double _altitude) {
		servingWlanId = _servingWlanId;
		xPos = _xPos;
		yPos = _yPos;
		altitude = _altitude;
	}
	
	
	/**
	 * 
	 * @param _xPos
	 * @param _yPos
	 */
	public Location(double _xPos, double _yPos, double _altitude) {
		xPos = _xPos;
		yPos = _yPos;
		altitude = _altitude;
	}
	
	public Location (double _xPos, double _yPos) {
		xPos = _xPos;
		yPos = _yPos;
		altitude = 0;
	}

	
	/**
	 * 
	 */
	public Location() {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * 
	 */
	@Override
	public boolean equals(Object other){
		boolean result = false;
	    if (other == null || !(other instanceof Location)) {
	    	return false;
	    }
	    if (other == this) {
	    	return true;
	    }
	    
	    Location otherLocation = (Location)other;
	    if(xPos == otherLocation.getXPos() && yPos == otherLocation.getYPos() && altitude == otherLocation.altitude) {
	    	result = true;
	    }
	    return result;
	}

	
	/**
	 * 
	 * @return
	 */
	public int getServingWlanId(){
		return servingWlanId;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public SimSettings.PLACE_TYPES getPlaceType(){
		return placeType;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getXPos(){
		return xPos;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getYPos(){
		return yPos;
	}
	
	
	public double getAltitude() {
		return altitude;
	}
	
	
	/**
	 * 
	 */
	public int compareTo(Object _in) {
		Location in = (Location) _in;
		if (xPos == in.getXPos() && yPos == in.getYPos() && altitude ==  in.altitude) {
			return 0;
		}
		else if (xPos > in.getXPos()) {
			return 1;
		}
		else return -1;
	}
	
	
	/**
	 * 
	 */
	public String toString() {
		return xPos + ", " + yPos + "," + altitude;
	}
	
	
	/**
	 * 
	 * @param bw
	 */
	public void setBW(double bw) {
		bandwidth = bw;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getBW() {
		return bandwidth;
	}

	
	/**
	 * @return the xPos
	 */
	public double getxPos() {
		return xPos;
	}

	
	/**
	 * @param xPos the xPos to set
	 */
	public void setxPos(double xPos) {
		this.xPos = xPos;
	}

	
	/**
	 * @return the yPos
	 */
	public double getyPos() {
		return yPos;
	}

	
	/**
	 * @param yPos the yPos to set
	 */
	public void setyPos(double yPos) {
		this.yPos = yPos;
	}

	
	/**
	 * @return the bandwidth
	 */
	public double getBandwidth() {
		return bandwidth;
	}

	
	/**
	 * @param bandwidth the bandwidth to set
	 */
	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}

	
	/**
	 * @param servingWlanId the servingWlanId to set
	 */
	public void setServingWlanId(int servingWlanId) {
		this.servingWlanId = servingWlanId;
	}

	
	/**
	 * @param placeType the placeType to set
	 */
	public void setPlaceType(SimSettings.PLACE_TYPES placeType) {
		this.placeType = placeType;
	}
}
