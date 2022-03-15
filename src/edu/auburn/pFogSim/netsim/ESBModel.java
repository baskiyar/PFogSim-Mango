/**
 * Equal Share Bandwidth Model
 * Considers that each device receives an equal share of a location's available bandwidth
 * @author jih0007@auburn.edu
 */

package edu.auburn.pFogSim.netsim;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.Exceptions.BlackHoleException;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

import java.util.LinkedList;


/**
 * 
 * @author szs0117
 *
 */
public class ESBModel extends NetworkModel {
	private static final String NOT_ADJACENT = "not adjacent";
	private static final int BYTES_PER_KB = 1024;
	private static final int BITS_PER_BYTE = 8;
	private static final double ONE_HUNDRED_PERCENT = 100;
	private static final double LATENCY_MULTIPLIER = 0.01/1000;	// defined in Link.java
	private static final double DISTANCE_THRESHOLD	= 20.0;
	private double WlanPoissonMean; //seconds
	private double WanPoissonMean; //seconds
	private double avgTaskInputSize; //bytes
	private double avgTaskOutputSize; //bytes
	private int maxNumOfClientsInPlace;
	private NetworkTopology networkTopology;
	private static ESBModel instance = null;
	private Router router;
	
	
	/**
	 * 
	 */
	public ESBModel() {
		super();
	}
	
	
	/**
	 * 
	 * @param _numberOfMobileDevices
	 */
	public ESBModel(int _numberOfMobileDevices) {
		super(_numberOfMobileDevices);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static ESBModel getInstance() {
		if(instance == null) {
			instance = new ESBModel();
		}
		return instance;
	}
	
	
	/**
	 * 
	 */
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
			double weight = SS.getTaskLookUpTable()[taskType.ordinal()][SimSettings.AppStat.USAGE_PERCENTAGE.ordinal()]/ONE_HUNDRED_PERCENT;
			if(weight != 0) {
				WlanPoissonMean += (SS.getTaskLookUpTable()[taskType.ordinal()][SimSettings.AppStat.POISSON_MEAN.ordinal()])*weight;
				
				double percentageOfCloudCommunication = SS.getTaskLookUpTable()[taskType.ordinal()][SimSettings.AppStat.CLOUD_SELECT_PROBABILITY.ordinal()];
				WanPoissonMean += (WlanPoissonMean)*(ONE_HUNDRED_PERCENT/percentageOfCloudCommunication)*weight;
				
				avgTaskInputSize += SS.getTaskLookUpTable()[taskType.ordinal()][SimSettings.AppStat.AVG_DATA_UPLOAD.ordinal()]*weight;
				
				avgTaskOutputSize += SS.getTaskLookUpTable()[taskType.ordinal()][SimSettings.AppStat.AVG_DATA_DOWNLOAD.ordinal()]*weight;
				
				numOfTaskType++;
			}
		}
		WlanPoissonMean = WlanPoissonMean/numOfTaskType;
		avgTaskInputSize = avgTaskInputSize/numOfTaskType;
		avgTaskOutputSize = avgTaskOutputSize/numOfTaskType;
		router = new Router();
	}

	
	/*
	 * Shaik - modified -- to solve the issue of failed tasks with cloud host having Host Id '0'.  
	 * Include details of approach here. 
	 * */
	@Override
	public double getUploadDelay(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest, SimSettings.CLOUD_TRANSFER isCloud) {
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
			if (isCloud == SimSettings.CLOUD_TRANSFER.CLOUD_DOWNLOAD)
				// then source is the cloud host (with Host Id '0'), not mobile device
				accessPointLocation = SimManager.getInstance().getLocalServerManager().findHostById(sourceDeviceId).getLocation();
			else	
				accessPointLocation = SimManager.getInstance().getMobilityModel().getLocation(sourceDeviceId,CloudSim.clock());
		}
		catch (IndexOutOfBoundsException e) {
			sourceDeviceId *= -1;
			accessPointLocation = SimManager.getInstance().getLocalServerManager().findHostById(sourceDeviceId).getLocation();
		}
		try {
			if (isCloud == SimSettings.CLOUD_TRANSFER.CLOUD_UPLOAD)
				// then destination is the cloud host (with Host Id '0'), not mobile device
				destPointLocation = SimManager.getInstance().getLocalServerManager().findHostById(destDeviceId).getLocation();
			else	
				destPointLocation = SimManager.getInstance().getMobilityModel().getLocation(destDeviceId,CloudSim.clock());
			
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
		source = new Location(accessPointLocation.getXPos(), accessPointLocation.getYPos(), accessPointLocation.getAltitude());
		destination = new Location(destPointLocation.getXPos(), destPointLocation.getYPos(), destPointLocation.getAltitude());
		
		if(wifiSrc) {
			src = networkTopology.findNode(source, true);
		}
		else {
			src = networkTopology.findNode(source, false);
		}
		if(wifiDest) {
			dest = networkTopology.findNode(destination, true);
		}
		else {
			dest = networkTopology.findNode(destination, false);
		}
	    path = router.findPath(networkTopology, src, dest);
		delay += getWlanUploadDelay(src.getLocation(), dataSize, CloudSim.clock()) + SimSettings.ROUTER_PROCESSING_DELAY; // Time required to send a packet of (dataSize) KB through the (src) location.
		if (SimSettings.getInstance().traceEnable()) {
			SimLogger.printLine("**********Task Delay**********");
			SimLogger.printLine("Start node ID:\t" + src.getWlanId());
		}
		while (!path.isEmpty()) {
			current = path.poll();
			nextHop = path.peek();
			if (nextHop == null) {
				break;
			}
			double proDelay = current.traverse(nextHop); // Returns latency to next node, or -1 if not adjacent. 
			if (proDelay < 0) {						   	// Under what circumstances would the next hop in the path be non-adjacent?
				SimLogger.printLine(NOT_ADJACENT); 	// Should we break here or take some other appropriate action?
				continue;
			}											// If not, we're going to add -1 to the delay. Doesn't that seem like a bad idea?
			double conDelay = getWlanUploadDelay(nextHop.getLocation(), dataSize, CloudSim.clock() + delay); // Time required to send (dataSize) KB through the (nextHop) location.
			delay += (proDelay + conDelay + SimSettings.ROUTER_PROCESSING_DELAY);
			if (SimSettings.getInstance().traceEnable()) {
				SimLogger.printLine("Path node:\t" + current.getWlanId() + "\tPropagation Delay:\t" + proDelay +"\tCongestion delay:\t" + conDelay + "\tTotal accumulative delay:\t" + delay);
			}
		}
		if (SimSettings.getInstance().traceEnable()) {
			SimLogger.printLine("Target Node ID:\t" + dest.getWlanId());
		}
		return delay;
	}
	

    /**
    * destination device is always mobile device in our simulation scenarios!
    */
	@Override
	public double getDownloadDelay(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest, SimSettings.CLOUD_TRANSFER isCloud) {
		return getUploadDelay(sourceDeviceId, destDeviceId, dataSize, wifiSrc, wifiDest, isCloud);//getUploadDelay has been made bi-directional
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getMaxNumOfClientsInPlace(){
		return maxNumOfClientsInPlace;
	}
	
	
	/**
	 * 
	 * @param deviceLocation
	 * @param time // UNUSED!
	 * @return
	 */
	private int getDeviceCount(Location deviceLocation){
		EdgeHost host = SimManager.getInstance().getLocalServerManager().findHostByLoc(deviceLocation.getXPos(), deviceLocation.getYPos(), deviceLocation.getAltitude());
		if (host == null)
			SimLogger.printLine("Null Host");
		return host.getCustomers().size();
	}
	
	
	/**
	 * shaik - updated
	 * calculate congestion delay.
	 * @param propogationDelay
	 * @param bandwidth
	 * @param PoissonMean
	 * @param avgTaskSize
	 * @param deviceCount
	 * @return
	 */
	private double calculateESB(double propogationDelay, double bandwidth /*Kbps*/, double avgTaskSize /*KB*/, int deviceCount){
		double Bps=0;
		
		avgTaskSize = avgTaskSize * BYTES_PER_KB; //convert from KB to Byte
		
		Bps = bandwidth * BYTES_PER_KB / BITS_PER_BYTE; //convert from Kbps to Byte per seconds
		
		double result = 0.0;
		if (deviceCount >= 1)
			result = (avgTaskSize * (deviceCount-1)) / Bps;
		result += propogationDelay;
		return result;
	}
	
	
	/**
	 * Returns the sum of (transfer time for a given chunk of data at specified location's available bandwidth speed)
	 *  + (delay due to bandwidth consumed by other tasks transmitted through that node)  
	 * @param loc
	 * @param dataSize (KB)
	 * @param time
	 * @return
	 */
	private double getWlanUploadDelay(Location loc, double dataSize /*KB*/, double time) {
		// calculate data transfer time at network node
		double transferTime = dataSize * BITS_PER_BYTE / loc.getBW(); 
		// calculate congestion delay at network node
		double congestionDelay = calculateESB(0, loc.getBW(), (avgTaskInputSize+avgTaskOutputSize), getDeviceCount(loc)); 
		//return the sum
		return (transferTime + congestionDelay);
	}

	/**
	 * Ziyan - added 
	 * @overload getWlanUploadDelay
	 * @param isClose
	 * @return 
	 */
	
	private double getWlanUploadDelay(boolean isClose, Location loc, double dataSize /*Kbit*/, double time) {
		
		double bandwidth = loc.getBW(); /* Kb */
		
		
		// Removed due to console spam, uncomment to see closeness data
		/*
		// check if two nodes are close
		if (isClose) {
			
			// the bandwidth should be 100M
			bandwidth = 819200; 

			System.out.println("\n" + loc + "\n" + "bandwidth updated: " + bandwidth);
		}
		*/

		// calculate data transfer time at network node
		//double transferTime = dataSize * 8 / loc.getBW(); 
		double transferTime = dataSize * 8 / bandwidth; 
		// calculate congestion delay at network node
		//double congestionDelay = calculateESB(0, loc.getBW(), WlanPoissonMean, (avgTaskInputSize+avgTaskOutputSize), getDeviceCount(loc, time)); 
		double congestionDelay = calculateESB(0, bandwidth, (avgTaskInputSize+avgTaskOutputSize), getDeviceCount(loc)); 
		//return the sum
		return (transferTime + congestionDelay);
	}
	
	//Qian add for get congestion delay
	/**
	 * 
	 * @param loc
	 * @param time
	 * @return
	 */
	public double getCongestionDelay(Location loc, double time) {
		return getWlanUploadDelay(loc, (avgTaskInputSize+avgTaskOutputSize), time);
	}
	
	
	/**
	 * 
	 * @param _networkTopology
	 */
	public void setNetworkTopology(NetworkTopology _networkTopology) {
		networkTopology = _networkTopology;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public NetworkTopology getNetworkTopology() {
		return networkTopology;
	}

	
	/**
	 * 
	 */
	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * 
	 */
	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * 
	 */
	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * 
	 */
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
	
	public int getHopsBack(Task task, int hostID, boolean sepa) {
		NodeSim dest = networkTopology.findNode(SimManager.getInstance().getLocalServerManager().findHostById(hostID).getLocation(), false);
		NodeSim src;
		if (sepa) {
			src = networkTopology.findNode(SimManager.getInstance().getMobilityModel().getLocation(task.getDesMobileDeviceId(),CloudSim.clock()), false);
		} else {
			src = networkTopology.findNode(SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()), false);
		}
		return router.findPath(networkTopology, dest, src).size();
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
		//Pathfinding should not need to run both ways, i.e. path to j from i is the same as i -> j. This is faster.
		NodeSim[] nodes = networkTopology.getNodes().toArray(new NodeSim[0]);
		for (int i = 0; i < nodes.length-1; i++) {
			for (int j = i; j < nodes.length; j++) {
				try {
					router.findPath(networkTopology, nodes[i], nodes[j]);
				}catch(BlackHoleException e){
					errors++;
					SimLogger.printLine(nodes[i].toString() + ", " + nodes[j].toString());
					//router.findPath(networkTopology, src, dest);
				}
			}
		}
//		for (NodeSim src : networkTopology.getNodes()) {
//			for (NodeSim dest : networkTopology.getNodes()) {
//				try {
//					router.findPath(networkTopology, src, dest);
//				}
//				catch (BlackHoleException e) {
//					errors++;
//					//router.findPath(networkTopology, src, dest);
//				}
//			}
//		}
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
	 * @author Qian, Ziyan 
	 * Qian added for get delay(Congestion + Propagation) between two nodes using two locations
	 * Ziyan modified - use different bandwidths for calculating propagation delay
	 * @param one - first location
	 * @param two - second location
	 * @return delay between two locations
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
		source = new Location(one.getLocation().getXPos(), one.getLocation().getYPos(), one.getLocation().getAltitude());
		destination = new Location(two.getLocation().getXPos(), two.getLocation().getYPos(),two.getLocation().getAltitude());
		src = networkTopology.findNode(source, false);
		dest = networkTopology.findNode(destination, false);
	    path = router.findPath(networkTopology, src, dest);
	    delay += getWlanUploadDelay(src.getLocation(), (avgTaskInputSize+avgTaskOutputSize), CloudSim.clock()) + SimSettings.ROUTER_PROCESSING_DELAY;
	    while (!path.isEmpty()) {
			current = path.poll();
			nextHop = path.peek();
			if (nextHop == null) {
				break;
			}
			if (current.traverse(nextHop) < 0) {
				SimLogger.printLine(NOT_ADJACENT);
			}
			double proDelay = current.traverse(nextHop);
			
			double distance = proDelay / LATENCY_MULTIPLIER; // in Link.java, proDelay = dist * LATENCY_MULTIPLIER
			double conDelay = getWlanUploadDelay(distance < DISTANCE_THRESHOLD, nextHop.getLocation(), (avgTaskInputSize+avgTaskOutputSize), CloudSim.clock() + delay); 			// if distance < 20 => isClose == true
			delay += (proDelay + conDelay + SimSettings.ROUTER_PROCESSING_DELAY);
	    }
		return delay;
	}
	
	
	/**
	 * @author Qian, Ziyan 
	 * Qian added for get delay(Congestion + Propagation) between two nodes using two locations
	 * Ziyan modified - use different bandwidths for calculating propagation delay
	 * @param one - first location
	 * @param two - second location
	 * @return delay between two locations
	 */
	public double getDelay(Location one, Location two) {
		double delay = 0;
		NodeSim src;
		NodeSim dest;
		NodeSim current;
		NodeSim nextHop;
		LinkedList<NodeSim> path = null;
		src = networkTopology.findNode(one, false);
		dest = networkTopology.findNode(two, false);
	    path = router.findPath(networkTopology, src, dest);
	    delay += getWlanUploadDelay(src.getLocation(), (avgTaskInputSize+avgTaskOutputSize), CloudSim.clock()) + SimSettings.ROUTER_PROCESSING_DELAY;
	    while (!path.isEmpty()) {
			current = path.poll();
			nextHop = path.peek();
			if (nextHop == null) {
				break;
			}
			if (current.traverse(nextHop) < 0) {
				SimLogger.printLine(NOT_ADJACENT);
			}
			double proDelay = current.traverse(nextHop);
			
			double distance = proDelay / LATENCY_MULTIPLIER; // in Link.java, proDelay = dist * LATENCY_MULTIPLIER

			double conDelay = getWlanUploadDelay(distance < DISTANCE_THRESHOLD, nextHop.getLocation(), (avgTaskInputSize+avgTaskOutputSize), CloudSim.clock() + delay); 			// if distance < 20 => isClose == true
			delay += (proDelay + conDelay + SimSettings.ROUTER_PROCESSING_DELAY);
	    }
		return delay;
	}

	
	/**
	 * @return the wlanPoissonMean
	 */
	public double getWlanPoissonMean() {
		return WlanPoissonMean;
	}

	
	/**
	 * @param wlanPoissonMean the wlanPoissonMean to set
	 */
	public void setWlanPoissonMean(double wlanPoissonMean) {
		WlanPoissonMean = wlanPoissonMean;
	}

	
	/**
	 * @return the wanPoissonMean
	 */
	public double getWanPoissonMean() {
		return WanPoissonMean;
	}
	

	/**
	 * @param wanPoissonMean the wanPoissonMean to set
	 */
	public void setWanPoissonMean(double wanPoissonMean) {
		WanPoissonMean = wanPoissonMean;
	}

	
	/**
	 * @return the avgTaskInputSize
	 */
	public double getAvgTaskInputSize() {
		return avgTaskInputSize;
	}

	
	/**
	 * @param avgTaskInputSize the avgTaskInputSize to set
	 */
	public void setAvgTaskInputSize(double avgTaskInputSize) {
		this.avgTaskInputSize = avgTaskInputSize;
	}

	
	/**
	 * @return the avgTaskOutputSize
	 */
	public double getAvgTaskOutputSize() {
		return avgTaskOutputSize;
	}

	
	/**
	 * @param avgTaskOutputSize the avgTaskOutputSize to set
	 */
	public void setAvgTaskOutputSize(double avgTaskOutputSize) {
		this.avgTaskOutputSize = avgTaskOutputSize;
	}

	
	/**
	 * @return the router
	 */
	public Router getRouter() {
		return router;
	}

	
	/**
	 * @param router the router to set
	 */
	public void setRouter(Router router) {
		this.router = router;
	}

	
	/**
	 * @param maxNumOfClientsInPlace the maxNumOfClientsInPlace to set
	 */
	public void setMaxNumOfClientsInPlace(int maxNumOfClientsInPlace) {
		this.maxNumOfClientsInPlace = maxNumOfClientsInPlace;
	}

	
	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(ESBModel instance) {
		ESBModel.instance = instance;
	}
	
}
