
package edu.auburn.pFogSim.Puddle;

import edu.auburn.pFogSim.Exceptions.*;
import java.util.ArrayList;
import java.util.List;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.Location;
import edu.auburn.pFogSim.Radix.BinaryHeap;
import edu.auburn.pFogSim.Radix.DistRadix;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.auburn.pFogSim.util.MobileDevice;

import java.util.LinkedList;
//import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;


/**
 * @author Jacob I Hall; Qian Wang; Shehenaz Shaik.
 * puddle class for separating nodes into logical hierarchies
 */
public class Puddle {
	
	//head = some head
	//list = list of nodes on level
	//node up = next puddle head location
	//list of nodes downward puddle heads
	
	private EdgeHost head;//head itself is not contained in the members list of a puddle
	private ArrayList<EdgeHost> members;
	private Puddle up;
	private ArrayList<Puddle> down;
	private int availRam;
	private double availMIPS;
	private int availPES;
	private int maxRam;
	private double maxMIPS;
	private int maxPES;
	private double totalCapacity;
	private double maxCapacity;
	private int level;
	private int puddleId;
	private int parentPuddleId;
	private ArrayList<Integer> childPuddleIds;
	
	
	/**
	 * 
	 */
	public Puddle() {
		members = new ArrayList<EdgeHost>();
		down = new ArrayList<Puddle>();
		childPuddleIds = new ArrayList<Integer>();
	}
	
	
	/**
	 * get the puddle head of the parent puddle
	 * @return
	 */
	public Puddle getParent() {
		return up;
	}
	
	
	/**
	 * get the head of this puddle
	 * @return
	 */
	public EdgeHost getHead() {
		return head;
	}
	
	
	/**
	 * get the heads of the children puddles
	 * @return
	 */
	public ArrayList<Puddle> getChildren() {
		return down;
	}
	
	
	/**
	 * get the members of this puddle
	 * @return
	 */
	public ArrayList<EdgeHost> getMembers() {
		return members;
	}
	
	
	/**
	 * choose next available puddle member as the new puddle head<br>
	 */
	public void chooseNewHead() {
		EdgeHost newHead = head;
		if (head == null) {
			head = members.get(0);
			//members.remove(head); //this causes bad juju, don't do it
			return;
		}
		for (int i = 0; newHead.equals(head) && i < members.size(); i++) {
			newHead = members.get(i);
		}
		
		if (newHead.equals(head)) {
			throw new EmptyPuddleException();
		}
		members.add(head);
		head = newHead;
		//members.remove(head); //see above
	}
	
	
	/**
	 * set the puddle head, the head should be a current member of the puddle<br>
	 * if the input host is not a member of the puddle throw an IllegalArgumentException
	 * @param _head
	 */
	public void setHead(EdgeHost _head) {
		if (members.contains(_head)) {
			head = _head;
			//members.remove(head);
		}
		else {
			throw new IllegalArgumentException();
		}
		
	}
	
	
	/**
	 * set the puddle members, all puddle members must be of the same layer as this puddle
	 * @param _members
	 */
	public void setMembers(List<EdgeHost> _members) {
		members = new ArrayList<EdgeHost>();
		if(_members.size() > 0)
		{
			for (EdgeHost child : _members) {
				if (child.getLevel() == getLevel()) {
					members.add(child);
				}
				else {
					//throw new BadPuddleParentageException("Child: " + child.getLevel() + ", member: " + getLevel());
					//don't throw the exception, just skip the node...
				}
			}
		}
	}
	
	
	/**
	 * set the children of the puddle, all children must be exactly on layer outwards 
	 * @param _down
	 */
	public void setDown(List<Puddle> _down) {
		down = new ArrayList<Puddle>();
		for (Puddle child : _down) {
			if (child.getLevel() == getLevel() - 1) {
				down.add(child);
			}
			else {
				throw new BadPuddleParentageException("Child: " + child.getLevel() + ", Parent: " + getLevel());
			}
		}
	}
	
	
	/**
	 * add a puddle to the children of this puddle
	 * @param puddle
	 */
	public void addDown(Puddle puddle) {
		down.add(puddle);
	}
	
	
	/**
	 * link this puddle with its parent. parent must be exactly one layer inwards<br>
	 * @param _up
	 */
	public void setUp(Puddle _up) {
		if (_up.getLevel() != getLevel() + 1) {
			throw new BadPuddleParentageException("Child: " + getLevel() + ", Parent: " + _up.getLevel());
		}
		up = _up;
		up.addDown(this);
	}
	
	
	/**
	 * update the total number of available resources in this puddle<br>
	 * as well as the max resources available on a single instance
	 */
	public void updateResources() {//we don't really use this-- use updateCapacity() instead
		availRam = 0;
		availMIPS = 0;
		availPES = 0;
		maxRam = 0;
		maxMIPS = 0;
		maxPES = 0;
		for (EdgeHost node : members) {
			availRam += node.getRam();
			availMIPS += node.getAvailableMips();
			availPES += node.getNumberOfFreePes();
			if (node.getRam() > maxRam) {
				maxRam = node.getRam();
			}
			if (node.getAvailableMips() > maxMIPS) {
				maxMIPS = node.getAvailableMips();
			}
			if (node.getNumberOfFreePes() > maxPES) {
				maxPES = node.getNumberOfFreePes();
			}
		}
	}
	
	
	/**
	 * return whether this puddle can handle the input task
	 * @param app
	 * @return
	 */
	public boolean canHandle(Task app) {
		updateCapacity();
		double appCapacity = ((CpuUtilizationModel_Custom)app.getUtilizationModelCpu()).predictUtilization(((EdgeVM)head.getVmList().get(0)).getVmType());
		if (appCapacity > totalCapacity || appCapacity > maxCapacity) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * return whether this puddle can handle the mobile device
	 * @author Qian
	 *	@param mobile
	 *	@return
	 */
	public boolean canHandle(MobileDevice mobile) {
		for (EdgeHost host: members) {
			if (host.canHandle(mobile)) {
				return true;
			}	
		}
		return false;
	}
	
	
	/**
	 * update the total capacity and max single instance capacity for this puddle
	 */
	public void updateCapacity() {
		double tempCap = 0;
		totalCapacity = 0;
		maxCapacity = 0;
		for (EdgeHost node : members) {
			tempCap = 100.0 - node.getVmList().get(0).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
			totalCapacity += tempCap;
			if (maxCapacity < tempCap) {
				maxCapacity = tempCap;
			}
		}
	}
	
	
	/**
	 * get the layer that this puddle belongs to
	 * @return
	 */
	public int getLevel() {
		return level;
	}
	
	
	/**
	 * get the list nodes sorted by distance from the reference point
	 * @param pair
	 * @return
	 */
	public LinkedList<EdgeHost> getClosestNodes(Location pair) {
		//DistRadix rad = new DistRadix(members, pair);
		BinaryHeap rad = new BinaryHeap(members.size(), pair, members);
		LinkedList<EdgeHost> nodes = rad.sortNodes();
		return nodes;
	}
	
	
	/**
	 * set the level of this puddle
	 * @param lvl
	 */
	public void setLevel(int lvl) {
		level = lvl;
	}
	
	
	/**
	 * @author Qian
	 * set parent and children list for every nodes 
	 */
	public void setNodeParentAndChildern() {
		if (SimSettings.getInstance().getClusterType()) {
			if (this.up != null) {
				for (EdgeHost node: members) {
					double minDistance = Double.MAX_VALUE;
					for (EdgeHost parentNode: up.getMembers()) {
						double distance = DataInterpreter.measure(
								node.getLocation().getXPos(), node.getLocation().getYPos(), node.getLocation().getAltitude(), 
									parentNode.getLocation().getXPos(), parentNode.getLocation().getYPos(), parentNode.getLocation().getAltitude());
						if (distance < minDistance) {
							node.setParent(parentNode);
						}
					}
					node.getParent().setChild(node);
				}
			}
		}
		else {
			//Note: this part of if-else is not verified, as the tests so far were done only for distance based Puddle formation. this block may contain bugs / issues. 
			if (this.up != null) {
				for (EdgeHost node: members) {
					double minLatency = Double.MAX_VALUE;
					for (EdgeHost parentNode: up.getMembers()) {
						double latency = ((ESBModel)SimManager.getInstance().getNetworkModel()).getDelay(node, parentNode);
						if (latency < minLatency) {
							node.setParent(parentNode);
						}
					}
					node.getParent().setChild(node);
				}
			}
		}
	}
	
	
	/**
	 * @return the down
	 */
	public ArrayList<Puddle> getDown() {
		return down;
	}
	
	
	/**
	 * @param down the down to set
	 */
	public void setDown(ArrayList<Puddle> down) {
		this.down = down;
	}
	
	
	/**
	 * @return the availRam
	 */
	public int getAvailRam() {
		return availRam;
	}
	
	
	/**
	 * @param availRam the availRam to set
	 */
	public void setAvailRam(int availRam) {
		this.availRam = availRam;
	}
	
	
	/**
	 * @return the availMIPS
	 */
	public double getAvailMIPS() {
		return availMIPS;
	}
	
	
	/**
	 * @param availMIPS the availMIPS to set
	 */
	public void setAvailMIPS(double availMIPS) {
		this.availMIPS = availMIPS;
	}
	
	
	/**
	 * @return the availPES
	 */
	public int getAvailPES() {
		return availPES;
	}
	
	
	/**
	 * @param availPES the availPES to set
	 */
	public void setAvailPES(int availPES) {
		this.availPES = availPES;
	}
	
	
	/**
	 * @return the maxRam
	 */
	public int getMaxRam() {
		return maxRam;
	}
	
	
	/**
	 * @param maxRam the maxRam to set
	 */
	public void setMaxRam(int maxRam) {
		this.maxRam = maxRam;
	}
	
	
	/**
	 * @return the maxMIPS
	 */
	public double getMaxMIPS() {
		return maxMIPS;
	}
	
	
	/**
	 * @param maxMIPS the maxMIPS to set
	 */
	public void setMaxMIPS(double maxMIPS) {
		this.maxMIPS = maxMIPS;
	}
	
	
	/**
	 * @return the maxPES
	 */
	public int getMaxPES() {
		return maxPES;
	}
	
	
	/**
	 * @param maxPES the maxPES to set
	 */
	public void setMaxPES(int maxPES) {
		this.maxPES = maxPES;
	}
	
	
	/**
	 * @return the totalCapacity
	 */
	public double getTotalCapacity() {
		return totalCapacity;
	}
	
	
	/**
	 * @param totalCapacity the totalCapacity to set
	 */
	public void setTotalCapacity(double totalCapacity) {
		this.totalCapacity = totalCapacity;
	}
	
	
	/**
	 * @return the maxCapacity
	 */
	public double getMaxCapacity() {
		return maxCapacity;
	}
	
	
	/**
	 * @param maxCapacity the maxCapacity to set
	 */
	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	
	
	/**
	 * @return the up
	 */
	public Puddle getUp() {
		return up;
	}
	
	
	/**
	 * @param members the members to set
	 */
	public void setMembers(ArrayList<EdgeHost> members) {
		this.members = members;
	}
	
	
	/**
	 * @return the puddleId
	 */
	public int getPuddleId() {
		return puddleId;
	}
	
	
	/**
	 * @param puddleId the puddleId to set
	 */
	public void setPuddleId(int puddleId) {
		this.puddleId = puddleId;
	}
	
	
	/**
	 * @return the parentPuddleId
	 */
	public int getParentPuddleId() {
		return parentPuddleId;
	}
	
	
	/**
	 * @param parentPuddleId the parentPuddleId to set
	 */
	public void setParentPuddleId(int parentPuddleId) {
		this.parentPuddleId = parentPuddleId;
	}
	
	
	/**
	 * @return the childPuddleIds
	 */
	public ArrayList<Integer> getChildPuddleIds() {
		return childPuddleIds;
	}
	
	
	/**
	 * @param childPuddleIds the childPuddleIds to set
	 */
	public void setChildPuddleIds(ArrayList<Integer> childPuddleIds) {
		this.childPuddleIds = childPuddleIds;
	}
}