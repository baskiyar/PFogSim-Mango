/*
 * Title:        EdgeCloudSim - EdgeHost
 * 
 * Description: 
 * EdgeHost adds location information over CloudSim's Host class
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.utils.Location;

public class EdgeHost extends Host {
	private Location location;
	private int level;//puddle level
	private double costPerBW;// Qian added for centralOrchestrator
	private double costPerSec;// Qian added for centralOrchestrator
	private int puddleId;// Qian added for puddle
	private EdgeHost parent;//Qian: added for puddle
	private ArrayList<EdgeHost> childern = null;//Qian: added for puddle
	private double reserveBW;//Qian: added for service replacement
	private long reserveMips;//Qian: added for service replacement
	private ArrayList<MobileDevice> customers;//Qian: added for service replacement 
	
	
	public EdgeHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		this.reserveBW = 0;
		this.reserveMips = 0;
		this.customers = new ArrayList<>();

	}
	//Qian add two parameters for centralOrchestrator
	public EdgeHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler, double _costPerBW, double _costPerSec) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		this.costPerBW = _costPerBW;
		this.costPerSec = _costPerSec;
		this.reserveBW = 0;
		this.reserveMips = 0;
		this.customers = new ArrayList<>();
	}
	
	public void setPlace(Location _location){
		location=_location;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int _level) {
		level = _level;
	}
	
	public double getCostPerBW() {
		return costPerBW;
	}
	
	public double getCostPerSec() {
		return costPerSec;
	}
	
	//Qian added for puddle
	public void setPuddleId(int id) {
		this.puddleId = id;
	} 
	
	//Qian added for puddle
	public int getPuddleId() {
		return this.puddleId;
	}
	
	/**
	 * @author Qian
	 * Add for puddle
	 * @param parent parent node in parent puddle
	 */
	public void setParent(EdgeHost _parent) {
		this.parent = _parent;
	}
	
	/**
	 * @author Qian
	 * added for puddle
	 * @return parent node in parent puddle
	 */
	public EdgeHost getParent() {
		return this.parent;
	}
	
	/**
	 * @author Qian
	 * added for puddle
	 * @param _child child node
	 */
	public void setChild(EdgeHost _child) {
		if (childern == null) {
			childern = new ArrayList<EdgeHost>();
		}
		childern.add(_child);
	}
	
	/**
	 * @author Qian
	 * @return children get children list.
	 */
	public ArrayList<EdgeHost> getChildern() {
		return this.childern;
	}
	/**
	 * Check if free Bandwidth available for certain mobile device
	 * @author Qian
	 * @param mb
	 * @return boolean
	 */
	public boolean isBWAvailable(MobileDevice mb) {
		double maxBW = this.getBw();
		double tempBW = reserveBW + mb.getBWRequirement();
		if (tempBW < maxBW) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * reserve Bandwith for certain mobile device
	 * @author Qian
	 *	@param mb
	 */
	public void reserveBW(MobileDevice mb) {
		reserveBW = reserveBW + mb.getBWRequirement();
	}
	
	/**
	 * Check if free MIPS available for certain mobile device
	 * @author Qian
	 *	@param mb
	 *	@return boolean
	 */
	public boolean isMIPSAvailable(MobileDevice mb) {
		long maxMips = this.getTotalMips();
		long tempLength = reserveMips + mb.getTaskLengthRequirement();
		if (tempLength < maxMips) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * reserve Mips for certain mobile device
	 * @author Qian
	 *	@param mb
	 */
	private void reserveCPUResource(MobileDevice mb) {
		reserveMips = reserveMips + mb.getTaskLengthRequirement(); 
	}
	
	/**
	 * make a reservation for a certain mobile device 
	 * @author Qian
	 *	@param mb
	 *	@return
	 */
	public void  makeReservation(MobileDevice mb) {
		reserveBW(mb);
		reserveCPUResource(mb);
		customers.add(mb);
		System.out.println("Mobile device: "+mb.getId()+"  Assigned host: "+this.getId());
	}
	/**
	 * @return the customers
	 */
	public ArrayList<MobileDevice> getCustomers() {
		return customers;
	}
	/**
	 * @param customers the customers to set
	 */
	public void setCustomers(ArrayList<MobileDevice> customers) {
		this.customers = customers;
	}
	/**
	 * for puddle canHandle
	 * @author Qian
	 *	@param mb
	 *	@return
	 */
	public boolean canHandle(MobileDevice mb) {
		if (!isMIPSAvailable(mb) || !isBWAvailable(mb)) {
			return false;
		}
		else {
			LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(this, mb);
			for (NodeSim node: path) {
				EdgeHost tempHost = SimManager.getInstance().getLocalServerManager().findHostByWlanId(node.getLocation().getServingWlanId());
				if (!tempHost.isBWAvailable(mb)) {
					return false;
				}
			}
			return true;
		}
	}
	/**
	 * @return the reserveBW
	 */
	public double getReserveBW() {
		return reserveBW;
	}
	/**
	 * @param reserveBW the reserveBW to set
	 */
	public void setReserveBW(double reserveBW) {
		this.reserveBW = reserveBW;
	}
	/**
	 * @return the reserveMips
	 */
	public long getReserveMips() {
		return reserveMips;
	}
	/**
	 * @param reserveMips the reserveMips to set
	 */
	public void setReserveMips(long reserveMips) {
		this.reserveMips = reserveMips;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	/**
	 * @param costPerBW the costPerBW to set
	 */
	public void setCostPerBW(double costPerBW) {
		this.costPerBW = costPerBW;
	}
	/**
	 * @param costPerSec the costPerSec to set
	 */
	public void setCostPerSec(double costPerSec) {
		this.costPerSec = costPerSec;
	}
	/**
	 * @param childern the childern to set
	 */
	public void setChildern(ArrayList<EdgeHost> childern) {
		this.childern = childern;
	}
}
