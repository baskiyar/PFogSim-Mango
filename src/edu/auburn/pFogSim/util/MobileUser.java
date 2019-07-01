package edu.auburn.pFogSim.util;

import edu.boun.edgecloudsim.utils.Location;

/**
 * 
 * @author jwc0045
 *
 */

public class MobileUser {

	private Location location;
	private int id;
	private int desMobileDeviceID;
	
	
	
	public MobileUser(Location location, int id, int desMobileDeviceID) {
		this.location = location;
		this.id = id;
		this.desMobileDeviceID = desMobileDeviceID;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getDesMobileDeviceID() {
		return desMobileDeviceID;
	}
	
	public void setDesMobileDeviceID(int desMobileDeviceID) {
		this.desMobileDeviceID = desMobileDeviceID;
	}
	
	
	
}
