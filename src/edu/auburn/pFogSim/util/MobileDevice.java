/**
 * 
 */
package edu.auburn.pFogSim.util;

import java.util.LinkedList;

import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;


/**
 * @author Qian
 *class for mobile device
 */
public class MobileDevice {

	private Location location;
	private int id;
	private int desMobileUser;
	private EdgeHost host = null;
	private SimLogger.TASK_STATUS assignHostStatus;
	
	
	/**
	 * @return the assignHostStatus
	 */
	public SimLogger.TASK_STATUS getAssignHostStatus() {
		return assignHostStatus;
	}

	
	/**
	 * @param assignHostStatus the assignHostStatus to set
	 */
	public void setAssignHostStatus(SimLogger.TASK_STATUS assignHostStatus) {
		this.assignHostStatus = assignHostStatus;
	}

	private SimSettings.APP_TYPES appType;
	private LinkedList<NodeSim> path;
	
	
	/**
	 * @return the path
	 */
	public LinkedList<NodeSim> getPath() {
		return path;
	}

	
	/**
	 * @param path the path to set
	 */
	public void setPath(LinkedList<NodeSim> path) {
		this.path = path;
	}

	
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
		double inteArrTime  = SimSettings.getInstance().getTaskLookUpTable()[appType.ordinal()][2];
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
		double inteArrTime  = SimSettings.getInstance().getTaskLookUpTable()[appType.ordinal()][2];
		long length = (long) SimSettings.getInstance().getTaskLookUpTable()[appType.ordinal()][7];
		length = (long) (1 / inteArrTime * length);
		return length;
	}
	
	
	/**
	 * get task latency information
	 * @author Shehenaz Shaik
	 *	@return
	 */
	public double getLatencyRequirement() {
		double latencyReq  = SimSettings.getInstance().getTaskLookUpTable()[appType.ordinal()][10];
		return latencyReq;
	}
	
	
	/**
	 * 
	 */
	public void makeReservation() {
		host.makeReservation(this);
		for (NodeSim node: path) {
			EdgeHost interHost = SimManager.getInstance().getLocalServerManager().findHostByWlanId(node.getLocation().getServingWlanId());
			interHost.reserveBW(this);
		}
		setAssignHostStatus(SimLogger.TASK_STATUS.ASSIGNED_HOST);
	}

	
	/**
	 * @return the appType
	 */
	public SimSettings.APP_TYPES getAppType() {
		return appType;
	}

	
	/**
	 * @param appType the appType to set
	 */
	public void setAppType(SimSettings.APP_TYPES appType) {
		this.appType = appType;
	}


	public int getDesMobileUser() {
		return desMobileUser;
	}


	public void setDesMobileUser(int desMobileUser) {
		this.desMobileUser = desMobileUser;
	}
	
}
