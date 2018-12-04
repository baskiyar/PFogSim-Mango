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

public class Location implements Comparable {
	private double xPos;
	private double yPos;
	private int servingWlanId;
	private double bandwidth;
	SimSettings.PLACE_TYPES placeType;
	public Location(SimSettings.PLACE_TYPES _placeType, int _servingWlanId, double _xPos, double _yPos){
		servingWlanId = _servingWlanId;
		placeType=_placeType;
		xPos = _xPos;
		yPos = _yPos;
	}
	
	public Location(int _servingWlanId, double _xPos, double _yPos) {
		servingWlanId = _servingWlanId;
		xPos = _xPos;
		yPos = _yPos;
	}
	
	public Location(double _xPos, double _yPos) {
		xPos = _xPos;
		yPos = _yPos;
	}

	public Location() {
		// TODO Auto-generated constructor stub
	}

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
	    if(xPos == otherLocation.getXPos() && yPos == otherLocation.getYPos()) {
	    	result = true;
	    }
	    return result;
	}

	public int getServingWlanId(){
		return servingWlanId;
	}
	
	public SimSettings.PLACE_TYPES getPlaceType(){
		return placeType;
	}
	
	public double getXPos(){
		return xPos;
	}
	
	public double getYPos(){
		return yPos;
	}
	
	public int compareTo(Object _in) {
		Location in = (Location) _in;
		if (xPos == in.getXPos() && yPos == in.getYPos()) {
			return 0;
		}
		else if (xPos > in.getXPos()) {
			return 1;
		}
		else return -1;
	}
	
	public String toString() {
		return xPos + ", " + yPos;
	}
	
	public void setBW(double bw) {
		bandwidth = bw;
	}
	
	public double getBW() {
		return bandwidth;
	}
}
