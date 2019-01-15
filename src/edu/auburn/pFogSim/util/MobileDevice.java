/**
 * 
 */
package edu.auburn.pFogSim.util;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.utils.Location;

/**
 * @author Qian
 *class for mobile device
 */
public class MobileDevice {

	private Location location;
	private int id;
	private EdgeHost host = null;
	private SimSettings.APP_TYPES appType;
	private double inteArrTime;
	
	/**
	 * @author Qian
	 * constructor
	 * @param id
	 */
	public MobileDevice(int id, SimSettings.APP_TYPES _appType) {
		this.id = id;
		this.appType = _appType;
	}
	
	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @author Qian
	 * constructor
	 * @param loc
	 * @param id
	 */
	public MobileDevice(Location loc, int id) {
		this.location = loc;
		this.id = id;
	}
	
	/**
	 * @author Qian
	 * assign host
	 * @param _host
	 */
	public void setHost(EdgeHost _host) {
		this.host = _host;
	}
	
	/**
	 * @author Qian
	 * id setter
	 *	@param _id
	 */
	public void setId(int _id) {
		this.id = _id;
	}
	
	/**
	 * id getter
	 * @author Qian
	 *	@return
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * host getter
	 * @author Qian
	 *	@return
	 */
	public EdgeHost getHost() {
		return this.host;
	}
	
	/**
	 * get Bandwith information
	 * @author Qian
	 *	@return
	 */
	public double getBWRequirement() {
		double bw = SimSettings.getInstance().getTaskLookUpTable()[appType.ordinal()][5];
		bw = bw + SimSettings.getInstance().getTaskLookUpTable()[appType.ordinal()][6];
		bw = 1 / inteArrTime * bw;
		return bw;
	}
	/**
	 * get task length information
	 * @author Qian
	 *	@return
	 */
	public long getTaskLengthRequirement() {
		long length = (long) SimSettings.getInstance().getTaskLookUpTable()[appType.ordinal()][7];
		length = (long) (1 / inteArrTime * length);
		return length;
	}
	
	
}
