/*
 * Title:        pFogSim derived from EdgeCloudSim GPSVectorMobility Model
 * 
 * Description: 
 * Simulates where the lowest-level devices (such as mobile devices) will be in the simulation space
 * which extends from (-1 * (MAX_LAT / 2) to MAX_LAT / 2 to make it MAX_LAT wide and permit
 * negative coordinates to resemble GPS coordinates as much as possible.
 * Devices are placed at a random Wireless Access Point (WAP) and given a random vector to move in.
 * 
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 */

package edu.auburn.pFogSim.mobility;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Random;


import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NetworkTopology;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;


/**
 * 
 * @author szs0117
 *
 */
public class GPSVectorMobility extends MobilityModel {
	private List<TreeMap<Double, Location>> treeMapArray;
	private List<TreeMap<Double, Location>> userTreeMapArray;
	private double MAX_LAT;
	private double MIN_LAT;
	private double MAX_LONG;
	private double MIN_LONG;
	private NetworkTopology network = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology();


	/**
	 * 
	 * @param _numberOfMobileDevices
	 * @param _simulationTime
	 */
	public GPSVectorMobility(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * 
	 */
	@Override
	public void initialize() {
		//this.MAX_LONG = SimManager.MAX_LONG;
		//this.MAX_LAT = SimManager.MAX_LAT;
		double[] simSpace = SimSettings.getInstance().getSimulationSpace();
		this.MIN_LONG = simSpace[0];
		this.MAX_LONG = simSpace[1];
		this.MIN_LAT = simSpace[2];
		this.MAX_LAT = simSpace[3];
		boolean movingDevices = SimSettings.getInstance().areMobileDevicesMoving();
		treeMapArray = new ArrayList<TreeMap<Double, Location>>();
		userTreeMapArray = new ArrayList<TreeMap<Double, Location>>();
		//Qian find key number of node with wlan_id #1071 
		int keyNumber = 0;
				
		//Go through network's list of nodes and pick out just the wireless access points
		ArrayList<NodeSim> accessPoints = new ArrayList<NodeSim>();
		for(NodeSim node : network.getNodes())
		{
			if(node.isWifiAcc()) 
				accessPoints.add(node);
		}
				
		//initialize tree maps and position of mobile devices
		for(int i=0; i<numberOfMobileDevices; i++) {
			treeMapArray.add(i, new TreeMap<Double, Location>());
			userTreeMapArray.add(i, new TreeMap<Double, Location>());
			
			//Picks a random wireless access point to start at
			//Shaik modified - to set all mobile devices to same host Fog node - int randDatacenterId = SimUtils.getRandomNumber(0, accessPoints.size()-1);
            int randDatacenterId = SimUtils.getRandomNumber(0, accessPoints.size()-1);
            int randDatacenterId2 = SimUtils.getRandomNumber(0, accessPoints.size()-1);
			int wlan_id = accessPoints.get(randDatacenterId).getWlanId();
			int wlan_id2 = accessPoints.get(randDatacenterId2).getWlanId();
			double x_pos = accessPoints.get(randDatacenterId).getLocation().getXPos();
			double y_pos = accessPoints.get(randDatacenterId).getLocation().getYPos();
			double alt = accessPoints.get(randDatacenterId).getLocation().getAltitude();
			double x_pos2 = accessPoints.get(randDatacenterId2).getLocation().getXPos();
			double y_pos2 = accessPoints.get(randDatacenterId2).getLocation().getYPos();
			double alt2 = accessPoints.get(randDatacenterId2).getLocation().getAltitude();
			
			//Qian find device #1071
			/*int wlan_id = 0;
			double x_pos = 0;
			double y_pos = 0;
			for (NodeSim node : accessPoints) {
				if (node.getLocation().getServingWlanId() == 1071) {
					wlan_id = node.getLocation().getServingWlanId();
					x_pos = node.getLocation().getXPos();
					y_pos = node.getLocation().getYPos();
					keyNumber = accessPoints.indexOf(node);
				}
			}
			*/
			//start locating user from 10th seconds
			treeMapArray.get(i).put((double)10, new Location(wlan_id, x_pos, y_pos, alt));
			userTreeMapArray.get(i).put((double)10, new Location(wlan_id2, x_pos2, y_pos2,alt2));
			
		}
		treeMapArray.addAll(userTreeMapArray);

		Random rng = new Random(SimSettings.getInstance().getRandomSeed());
		int iterations = numberOfMobileDevices*2;
		for(int i=0; i<iterations; i++) {
			TreeMap<Double, Location> treeMap = treeMapArray.get(i);
			//Make random numbers to make the vectors
			double lat_movement, long_movement, alt_movement;
			if(movingDevices)
			{
				lat_movement = 5 * (rng.nextDouble() - 0.5) * 0.000001; //Approximates movement of 5 meters * (random constant < 1)
				long_movement = 5 * (rng.nextDouble() - 0.5) * 0.000001; //Same for right
				alt_movement = 5 * (rng.nextDouble()-0.5)*0.000001;
			}
			else {
				lat_movement = 0;
				long_movement = 0;
				alt_movement = 0;
			}
			while(treeMap.lastKey() < SimSettings.getInstance().getSimulationTime()) {		
				
				if(movingDevices) 
				{
					Location entry = treeMap.lastEntry().getValue();
					double x_pos = treeMap.lastEntry().getValue().getXPos();
					double y_pos = treeMap.lastEntry().getValue().getYPos();				
					double alt = treeMap.lastEntry().getValue().getAltitude();
					int wlan_id = treeMap.lastEntry().getValue().getServingWlanId();
					
					  
					if(x_pos + long_movement > this.MAX_LONG || x_pos + long_movement < this.MIN_LONG) long_movement = long_movement * -1;
					if(y_pos + lat_movement > this.MAX_LAT || y_pos + lat_movement < this.MIN_LAT) lat_movement = lat_movement * -1;
					double distance = 0, minDistance = Double.MAX_VALUE;
					NodeSim closestNode = new NodeSim();
					for(NodeSim node : accessPoints)
					{
						distance = DataInterpreter.measure(node.getLocation().getYPos(), node.getLocation().getXPos(), node.getLocation().getAltitude(), y_pos, x_pos,alt);
						if (distance < minDistance) 
						{
							minDistance = distance;
							closestNode = node;
						}
					}
					wlan_id = closestNode.getWlanId();
					//This first argument kind of dictates the speed at which the device moves, higher it is, slower the devices are
					//	smaller value in there, the more it updates
					//As it is now, allows devices to change wlan_ids around 600 times in an hour
					treeMap.put(treeMap.lastKey()+1, new Location(wlan_id, x_pos + long_movement, y_pos + lat_movement, alt+alt_movement));
				}
				else {
					treeMap.put(treeMap.lastKey() + 1,  treeMap.lastEntry().getValue());
				}
			}
			//Qian print final start position of every mobile device
		}
		//Qian: Print begin address of 
		
	}

	
	/**
	 * 
	 */
	@Override
	public Location getLocation(int deviceId, double time) {
		TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);
		
		Entry<Double, Location> e = treeMap.floorEntry(time);
	    
	    if(e == null){
	    	SimLogger.printLine("impossible is occured! no location is found for the device!");
	    	System.exit(0);
	    }
	    
		return e.getValue();
	}
	
	
	/**
	 * 
	 */
	public int getWlanId(int deviceId, double time) 
	{
		int wlan_id = -1;
		
		if(time >= 0 && deviceId >= 0)
		{	
			TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);
			
			Entry<Double, Location> e = treeMap.floorEntry(time);
			
			try {
				wlan_id = e.getValue().getServingWlanId();
			} catch (NullPointerException exce)
			{
				SimLogger.printLine("NullPointerException at time : " + time + "\n\tFor Device #: " + deviceId);
				throw new NullPointerException();
			}
		}
		else throw new IllegalArgumentException();
		return wlan_id;
	}
	
	
	/**
	 * 
	 */
	public int getWlanId(int deviceId) 
	{
		int wlan_id = -1;
		
		if(deviceId >= 0)
		{	
			TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);
			Entry<Double, Location> e = treeMap.floorEntry(20.0); //This 20.0 is rather arbitrary, just gives 'starting' WlanId connection
			wlan_id = e.getValue().getServingWlanId();
		}
		else throw new IllegalArgumentException();
		return wlan_id;
	}
	
	
	/**
	 * 
	 */
	public int getSize()
	{
		return treeMapArray.size()/2;
		//return treeMapArray.get(1).size();
	}
	
	
	/**
	 * 
	 */
	public int getTreeMapSize() 
	{
		return treeMapArray.get(1).size();
	}
	
	
	/**
	 * get last location for a mobile device
	 * @author Qian
	 *	@param id
	 *	@return
	 */
	public Location getLastMobileDeviceLocation(int id) {
		return treeMapArray.get(id).lastEntry().getValue();
	}

	
	/**
	 * @return the treeMapArray
	 */
	public List<TreeMap<Double, Location>> getTreeMapArray() {
		return treeMapArray;
	}

	
	/**
	 * @param treeMapArray the treeMapArray to set
	 */
	public void setTreeMapArray(List<TreeMap<Double, Location>> treeMapArray) {
		this.treeMapArray = treeMapArray;
	}

	
	/**
	 * @return the mAX_LAT
	 */
	public double getMAX_LAT() {
		return MAX_LAT;
	}

	
	/**
	 * @param mAX_LAT the mAX_LAT to set
	 */
	public void setMAX_LAT(double mAX_LAT) {
		MAX_LAT = mAX_LAT;
	}

	
	/**
	 * @return the mIN_LAT
	 */
	public double getMIN_LAT() {
		return MIN_LAT;
	}

	
	/**
	 * @param mIN_LAT the mIN_LAT to set
	 */
	public void setMIN_LAT(double mIN_LAT) {
		MIN_LAT = mIN_LAT;
	}

	
	/**
	 * @return the mAX_LONG
	 */
	public double getMAX_LONG() {
		return MAX_LONG;
	}

	
	/**
	 * @param mAX_LONG the mAX_LONG to set
	 */
	public void setMAX_LONG(double mAX_LONG) {
		MAX_LONG = mAX_LONG;
	}

	
	/**
	 * @return the mIN_LONG
	 */
	public double getMIN_LONG() {
		return MIN_LONG;
	}

	
	/**
	 * @param mIN_LONG the mIN_LONG to set
	 */
	public void setMIN_LONG(double mIN_LONG) {
		MIN_LONG = mIN_LONG;
	}

	
	/**
	 * @return the network
	 */
	public NetworkTopology getNetwork() {
		return network;
	}

	
	/**
	 * @param network the network to set
	 */
	public void setNetwork(NetworkTopology network) {
		this.network = network;
	}
}
