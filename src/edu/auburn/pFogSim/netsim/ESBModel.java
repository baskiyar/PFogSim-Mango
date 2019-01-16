/**
 * Equal Share Bandwidth Model
 * Considers that each device receives an equal share of a location's available bandwidth
 * @author jih0007@auburn.edu
 */

package edu.auburn.pFogSim.netsim;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.Exceptions.BlackHoleException;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ESBModel extends NetworkModel {
	private double WlanPoissonMean; //seconds
	private double WanPoissonMean; //seconds
	private double avgTaskInputSize; //bytes
	private double avgTaskOutputSize; //bytes
	private int maxNumOfClientsInPlace;
	private NetworkTopology networkTopology;
	private static ESBModel instance = null;
	private Router router;
	
	public ESBModel() {
		super();
	}
	
	public ESBModel(int _numberOfMobileDevices) {
		super(_numberOfMobileDevices);
	}
	
	public static ESBModel getInstance() {
		if(instance == null) {
			instance = new ESBModel();
		}
		return instance;
	}
	
	@Override
	public void initialize() {
		WlanPoissonMean=0;
		WanPoissonMean=0;
		avgTaskInputSize=0;
		avgTaskOutputSize=0;
		maxNumOfClientsInPlace=0;
		
		//Calculate interarrival time and task sizes
		double numOfTaskType = 0;
		SimSettings SS = SimSettings.getInstance();
		for (SimSettings.APP_TYPES taskType : SimSettings.APP_TYPES.values()) {
			double weight = SS.getTaskLookUpTable()[taskType.ordinal()][0]/(double)100;
			if(weight != 0) {
				WlanPoissonMean += (SS.getTaskLookUpTable()[taskType.ordinal()][2])*weight;
				
				double percentageOfCloudCommunication = SS.getTaskLookUpTable()[taskType.ordinal()][1];
				WanPoissonMean += (WlanPoissonMean)*((double)100/percentageOfCloudCommunication)*weight;
				
				avgTaskInputSize += SS.getTaskLookUpTable()[taskType.ordinal()][5]*weight;
				
				avgTaskOutputSize += SS.getTaskLookUpTable()[taskType.ordinal()][6]*weight;
				
				numOfTaskType++;
			}
		}
		WlanPoissonMean = WlanPoissonMean/numOfTaskType;
		avgTaskInputSize = avgTaskInputSize/numOfTaskType;
		avgTaskOutputSize = avgTaskOutputSize/numOfTaskType;
		router = new Router();
	}


	@Override
	public double getUploadDelay(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest) {
		double delay = 0;
		Location accessPointLocation = null;
		Location destPointLocation = null;
		/*changed by pFogSim--
		 * OK... so this looks really stupid, but its not... mostly
		 * unfortunately mobile devices and host devices use the same range of id's
		 * and this is far too deep to go through the process of separating them
		 * as such, any time that a host device is sent into this method it is multiplied by -1
		 * this will cause and index out of bounds exception when searching for a mobile location
		 * when you get such exception, flip the sign of the id and search it as a host
		 */
		try {
			accessPointLocation = SimManager.getInstance().getMobilityModel().getLocation(sourceDeviceId,CloudSim.clock());
		}
		catch (IndexOutOfBoundsException e) {
			sourceDeviceId *= -1;
			accessPointLocation = SimManager.getInstance().getLocalServerManager().findHostById(sourceDeviceId).getLocation();
			//SimLogger.printLine(accessPointLocation.toString());
		}
		try {
			destPointLocation = SimManager.getInstance().getMobilityModel().getLocation(destDeviceId,CloudSim.clock());
			//SimLogger.printLine(destPointLocation.toString());
		}
		catch (IndexOutOfBoundsException e) {
			destDeviceId *= -1;
			destPointLocation = SimManager.getInstance().getLocalServerManager().findHostById(destDeviceId).getLocation();
		}
		Location source;
		Location destination;
		NodeSim src;
		NodeSim dest;
		NodeSim current;
		NodeSim nextHop;
		LinkedList<NodeSim> path = null;
		source = new Location(accessPointLocation.getXPos(), accessPointLocation.getYPos());
		destination = new Location(destPointLocation.getXPos(), destPointLocation.getYPos());
		
		if(wifiSrc) {
			src = networkTopology.findNode(source, true);
		}
		else {
			src = networkTopology.findNode(source, false);
			//SimLogger.printLine(src.toString());
		}
		if(wifiDest) {
			dest = networkTopology.findNode(destination, true);
		}
		else {
			dest = networkTopology.findNode(destination, false);
		}
		//SimLogger.printLine(src.toString() + " " + dest.toString());
	    path = router.findPath(networkTopology, src, dest);
	   // SimLogger.printLine(path.size() + "");
		delay += getWlanUploadDelay(src.getLocation(), CloudSim.clock());
		if (SimSettings.getInstance().traceEnalbe()) {
			SimLogger.getInstance().printLine("**********Task Delay**********");
			SimLogger.getInstance().printLine("Start node ID:\t" + src.getWlanId());
		}
		while (!path.isEmpty()) {
			current = path.poll();
			nextHop = path.peek();
			if (nextHop == null) {
				break;
			}
			if (current.traverse(nextHop) < 0) {
				SimLogger.printLine("not adjacent");
			}
			double proDelay = current.traverse(nextHop);
			double conDelay = getWlanUploadDelay(nextHop.getLocation(), CloudSim.clock() + delay);
			delay += (proDelay + conDelay);
			if (SimSettings.getInstance().traceEnalbe()) {
				SimLogger.getInstance().printLine("Path node:\t" + current.getWlanId() + "\tPropagation Delay:\t" + proDelay +"\tCongestion delay:\t" + conDelay + "\tTotal accumulative delay:\t" + delay);
			}
		}
		if (SimSettings.getInstance().traceEnalbe()) {
			SimLogger.getInstance().printLine("Target Node ID:\t" + dest.getWlanId());
		}
		return delay;
	}

    /**
    * destination device is always mobile device in our simulation scenarios!
    */
	@Override
	public double getDownloadDelay(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest) {
		return getUploadDelay(sourceDeviceId, destDeviceId, dataSize, wifiSrc, wifiDest);//getUploadDelay has been made bi-directional
	}
	
	public int getMaxNumOfClientsInPlace(){
		return maxNumOfClientsInPlace;
	}
	
	private int getDeviceCount(Location deviceLocation, double time){
		int deviceCount = 0;
		
		for(int i=0; i<numberOfMobileDevices; i++) {
			Location location = SimManager.getInstance().getMobilityModel().getLocation(i,time);
			if(location.equals(deviceLocation))
				deviceCount++;
		}
		
		//record max number of client just for debugging
		if(maxNumOfClientsInPlace<deviceCount)
			maxNumOfClientsInPlace = deviceCount;
		
		return deviceCount;
	}
	//calculate congestion delay.
	private double calculateESB(double propogationDelay, double bandwidth /*Kbps*/, double PoissonMean, double avgTaskSize /*KB*/, int deviceCount){
		double Bps=0;
		
		avgTaskSize = avgTaskSize * (double)1024; //convert from KB to Byte
		
		Bps = bandwidth * (double)1024 / (double)8; //convert from Kbps to Byte per seconds
		double result = (avgTaskSize * deviceCount) / Bps;
		result += propogationDelay;
		return result;
	}
	
	private double getWlanUploadDelay(Location loc, double time) {
		return calculateESB(0, loc.getBW(), WlanPoissonMean, avgTaskInputSize, getDeviceCount(loc, time));
	}
	
	//Qian add for get congestion delay
	public double getCongestionDelay(Location loc, double time) {
		return getWlanUploadDelay(loc, time);
	}
	
	public void setNetworkTopology(NetworkTopology _networkTopology) {
		networkTopology = _networkTopology;
	}
	
	public NetworkTopology getNetworkTopology() {
		return networkTopology;
	}

	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downloadFinished(Location accessPointLocation, int sourceDeviceId) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * get the number of hops from task to the machine it is running on
	 * @param task
	 * @param hostID
	 * @return
	 */
	public int getHops(Task task, int hostID) {
		NodeSim dest = networkTopology.findNode(SimManager.getInstance().getLocalServerManager().findHostById(hostID).getLocation(), false);
		NodeSim src = networkTopology.findNode(SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()), false);
		return router.findPath(networkTopology, src, dest).size();
	}
	/**
	 * The gravity well is where we search for and find black holes. For a description <br>
	 * of black holes and what they are see BlackHoleException. Here we will search every <br>
	 * possible route on the network to find black holes, once you find the black hole <br>
	 * (usually its one node in particular that causes problems) you can do whatever you <br> 
	 * want with it (I prefer just deleting the node from the data set) if you are looking <br>
	 * for black holes, this method should be run once as soon as the network topology has <br>
	 * been passed to the network model. There is a commented call in EdgeServerManager line 132<br>
	 * just uncomment that call and debug when needed.
	 */
	public void gravityWell() {
		int errors = 0;
		for (NodeSim src : networkTopology.getNodes()) {
			for (NodeSim dest : networkTopology.getNodes()) {
				try {
					router.findPath(networkTopology, src, dest);
				}
				catch (BlackHoleException e) {
					errors++;
					SimLogger.printLine(src.toString() + ", " + dest.toString());
					//router.findPath(networkTopology, src, dest);
				}
			}
		}
		if (errors > 0) {
			SimLogger.printLine("Errors: " + errors);
			gravityWell();
		}
	}
	/**
	 * find path between two NodeSim
	 * @author Qian
	 *	@param src
	 *	@param dec
	 *	@return
	 */
	public LinkedList<NodeSim> findPath(NodeSim src, NodeSim dec) {
		return router.findPath(networkTopology, src, dec);
	}
	/**
	 * find path from mobile device to host
	 * @author Qian
	 *	@param host
	 *	@param task
	 *	@return
	 */
	public LinkedList<NodeSim> findPath(EdgeHost host, MobileDevice task) {
		NodeSim des = networkTopology.findNode(host.getLocation(), false);
		NodeSim src = networkTopology.findNode(task.getLocation(), false);
		return findPath(src, des);
	}
	/**
	 * @author Qian
	 * added for get delay(Congestion + Propagation) between two nodes
	 * @param one
	 * @param two
	 * @return delaty between two EdgeNodes
	 */
	public double getDelay(EdgeHost one, EdgeHost two) {
		double delay = 0;
		Location source;
		Location destination;
		NodeSim src;
		NodeSim dest;
		NodeSim current;
		NodeSim nextHop;
		LinkedList<NodeSim> path = null;
		source = new Location(one.getLocation().getXPos(), one.getLocation().getYPos());
		destination = new Location(two.getLocation().getXPos(), two.getLocation().getYPos());
		src = networkTopology.findNode(source, false);
		dest = networkTopology.findNode(destination, false);
	    path = router.findPath(networkTopology, src, dest);
	    delay += getWlanUploadDelay(src.getLocation(), CloudSim.clock());
	    while (!path.isEmpty()) {
			current = path.poll();
			nextHop = path.peek();
			if (nextHop == null) {
				break;
			}
			if (current.traverse(nextHop) < 0) {
				SimLogger.printLine("not adjacent");
			}
			double proDelay = current.traverse(nextHop);
			double conDelay = getWlanUploadDelay(nextHop.getLocation(), CloudSim.clock() + delay);
			delay += (proDelay + conDelay);
	    }
		return delay;
	}
	/**
	 * @author Qian
	 * added for get delay(Congestion + Propagation) between two nodes using two locations
	 * @param one - first location
	 * @param two - second location
	 * @return delay between two locations
	 */
	public double getDleay(Location one, Location two) {
		double delay = 0;
		NodeSim src;
		NodeSim dest;
		NodeSim current;
		NodeSim nextHop;
		LinkedList<NodeSim> path = null;
		src = networkTopology.findNode(one, false);
		dest = networkTopology.findNode(two, false);
	    path = router.findPath(networkTopology, src, dest);
	    delay += getWlanUploadDelay(src.getLocation(), CloudSim.clock());
	    while (!path.isEmpty()) {
			current = path.poll();
			nextHop = path.peek();
			if (nextHop == null) {
				break;
			}
			if (current.traverse(nextHop) < 0) {
				SimLogger.printLine("not adjacent");
			}
			double proDelay = current.traverse(nextHop);
			double conDelay = getWlanUploadDelay(nextHop.getLocation(), CloudSim.clock() + delay);
			delay += (proDelay + conDelay);
	    }
		return delay;
	}
	
}
