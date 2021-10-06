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
public class XYVectorMobility extends MobilityModel {
	private List<TreeMap<Double, Location>> treeMapArray;
	private List<TreeMap<Double, Location>> userTreeMapArray;
	private double MAX_WIDTH;
	private double MIN_WIDTH;
	private double MAX_HEIGHT;
	private double MIN_HEIGHT;
	private NetworkTopology network = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology();

		
	/**
	 * 
	 * @param _numberOfMobileDevices
	 * @param _simulationTime
	 */
	public XYVectorMobility(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * 
	 * @param y1
	 * @param x1
	 * @param y2
	 * @param x2
	 * @return
	 */
	public double measure(double y1, double x1, double y2, double x2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}
	
	
	/**
	 * 
	 */
	@Override
	public void initialize() {
		//this.MAX_LONG = SimManager.MAX_LONG;
		//this.MAX_LAT = SimManager.MAX_LAT;
		double[] simSpace = SimSettings.getInstance().getSimulationSpace();
		this.MIN_WIDTH = simSpace[0];
		this.MAX_WIDTH = simSpace[1];
		this.MIN_HEIGHT = simSpace[2];
		this.MAX_HEIGHT = simSpace[3];
		boolean movingDevices = SimSettings.getInstance().areMobileDevicesMoving();

		
		treeMapArray = new ArrayList<TreeMap<Double, Location>>();
		userTreeMapArray = new ArrayList<TreeMap<Double, Location>>();

				
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
			
			//Picks a random wireless access point to start at
			int randDatacenterId = SimUtils.getRandomNumber(0, accessPoints.size()-1);
			int wlan_id = accessPoints.get(randDatacenterId).getWlanId();
			double x_pos = accessPoints.get(randDatacenterId).getLocation().getXPos();
			double y_pos = accessPoints.get(randDatacenterId).getLocation().getYPos();
			double alt = accessPoints.get(randDatacenterId).getLocation().getAltitude();
            int randDatacenterId2 = SimUtils.getRandomNumber(0, accessPoints.size()-1);
			int wlan_id2 = accessPoints.get(randDatacenterId2).getWlanId();
			double x_pos2 = accessPoints.get(randDatacenterId2).getLocation().getXPos();
			double y_pos2 = accessPoints.get(randDatacenterId2).getLocation().getYPos();
			double alt2 = accessPoints.get(randDatacenterId2).getLocation().getAltitude();
			


			
			//start locating user from 10th seconds
			treeMapArray.get(i).put((double)10, new Location(wlan_id, x_pos, y_pos, alt));
			userTreeMapArray.get(i).put((double)10, new Location(wlan_id2, x_pos2, y_pos2,alt2));

		}
		Random rng = new Random(SimSettings.getInstance().getRandomSeed());
		for(int i=0; i<numberOfMobileDevices; i++) {
			TreeMap<Double, Location> treeMap = treeMapArray.get(i);
			//Make random numbers to make the vectors
			double lat_mov, long_mov, alt_mov;
			if(movingDevices)
			{
				lat_mov = 5 * (rng.nextDouble()- 0.5) * 0.000001; //Approximates movement of 5 meters * (random constant < 1)
				long_mov = 5 * (rng.nextDouble() - 0.5) * 0.000001; //Same for right
				alt_mov = 5 * (rng.nextDouble()-0.5)*0.000001;

			}
			else {
				lat_mov = 0;
				long_mov = 0;
				alt_mov = 0;

			}
			//double up = 0, right = 0;
			while(treeMap.lastKey() < SimSettings.getInstance().getSimulationTime()) {		
				
				if(movingDevices)
				{
					double x_pos = treeMap.lastEntry().getValue().getXPos();
					double y_pos = treeMap.lastEntry().getValue().getYPos();
					double z_pos = treeMap.lastEntry().getValue().getAltitude();
					int wlan_id = treeMap.lastEntry().getValue().getServingWlanId();
					  
					if(x_pos + lat_mov > this.MAX_WIDTH || x_pos + lat_mov < this.MIN_WIDTH) lat_mov = lat_mov * -1;
					if(y_pos + long_mov > this.MAX_HEIGHT || y_pos + long_mov < this.MIN_HEIGHT) long_mov = long_mov * -1;
					double distance = 0, minDistance = Double.MAX_VALUE;
					NodeSim closestNode = new NodeSim();
					for(NodeSim node : accessPoints)
					{
						distance = DataInterpreter.measure(node.getLocation().getYPos(), node.getLocation().getXPos(), node.getLocation().getAltitude(), y_pos, x_pos, z_pos);
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
					treeMap.put(treeMap.lastKey()+1, new Location(wlan_id, x_pos + lat_mov, y_pos + long_mov, z_pos+alt_mov));		
					//SimLogger.printLine("Length = " + treeMap.size());
				}
				else {
					treeMap.put(treeMap.lastKey() + 1,  treeMap.lastEntry().getValue());
				}
			}
		}
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
		return treeMapArray.size();
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
	 * @return the mAX_WIDTH
	 */
	public double getMAX_WIDTH() {
		return MAX_WIDTH;
	}

	
	/**
	 * @param mAX_WIDTH the mAX_WIDTH to set
	 */
	public void setMAX_WIDTH(double mAX_WIDTH) {
		MAX_WIDTH = mAX_WIDTH;
	}

	
	/**
	 * @return the mIN_WIDTH
	 */
	public double getMIN_WIDTH() {
		return MIN_WIDTH;
	}

	
	/**
	 * @param mIN_WIDTH the mIN_WIDTH to set
	 */
	public void setMIN_WIDTH(double mIN_WIDTH) {
		MIN_WIDTH = mIN_WIDTH;
	}

	
	/**
	 * @return the mAX_HEIGHT
	 */
	public double getMAX_HEIGHT() {
		return MAX_HEIGHT;
	}

	
	/**
	 * @param mAX_HEIGHT the mAX_HEIGHT to set
	 */
	public void setMAX_HEIGHT(double mAX_HEIGHT) {
		MAX_HEIGHT = mAX_HEIGHT;
	}

	
	/**
	 * @return the mIN_HEIGHT
	 */
	public double getMIN_HEIGHT() {
		return MIN_HEIGHT;
	}

	
	/**
	 * @param mIN_HEIGHT the mIN_HEIGHT to set
	 */
	public void setMIN_HEIGHT(double mIN_HEIGHT) {
		MIN_HEIGHT = mIN_HEIGHT;
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
