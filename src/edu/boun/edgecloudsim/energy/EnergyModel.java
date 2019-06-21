package edu.boun.edgecloudsim.energy;

import java.util.LinkedList;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.auburn.pFogSim.Exceptions.BlackHoleException;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.auburn.pFogSim.netsim.NetworkTopology;
import edu.auburn.pFogSim.netsim.Router;
import edu.auburn.pFogSim.netsim.ESBModel;

import java.util.HashSet;


/**
 * Class for energy measurement
 * Written by Matthew Merck and Cameron Berry
 * 
 * @author mlm0175
 * @author cmb0220
 *
 */
public class EnergyModel {
	
	//All private values are values to be logged
	private static double totalEnergy;

	//taken and modified from getUploadDelay in ESBModel.java
	public static double getDownloadEnergy(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest, SimSettings.CLOUD_TRANSFER isCloud) {
	
		double energy = 0;
		Location accessPointLocation = null;
		Location destPointLocation = null;

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
		
		NetworkTopology networkTopology = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology(); 
		
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
		//SimLogger.printLine(src.toString() + " " + dest.toString());
		Router router = ((ESBModel) SimManager.getInstance().getNetworkModel()).getRouter();
	    path = router.findPath(networkTopology, src, dest);
	    
	    //////////////////////////////////////////////////////////////PATH IS CALCULATED ABOVE. ENERGY OF PATH IS CALCULATED BELOW
	    
	   // SimLogger.printLine(path.size() + "");
	//	energy += getWlanUploadDelay(src.getLocation(), dataSize, CloudSim.clock()) + SimSettings.ROUTER_PROCESSING_DELAY;
		if (SimSettings.getInstance().traceEnable()) {
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
//			double proDelay = current.traverse(nextHop);
//			double conDelay = getWlanUploadDelay(nextHop.getLocation(), dataSize, CloudSim.clock() + energy);
//			energy += (proDelay + conDelay + SimSettings.ROUTER_PROCESSING_DELAY);
			int level = current.getLevel();
			double nJperBit = Double.parseDouble(DataInterpreter.getNodeSpecs()[DataInterpreter.getMAX_LEVELS() - level][15]);
			energy = (dataSize * 8000) * nJperBit; //dataSize is in kilobytes. multiply by 8000 to convert to bits
//			if (SimSettings.getInstance().traceEnable()) {
//				SimLogger.getInstance().printLine("Path node:\t" + current.getWlanId() + "\tPropagation Delay:\t" + proDelay +"\tCongestion delay:\t" + conDelay + "\tTotal accumulative delay:\t" + delay);
//			}
		}
		if (SimSettings.getInstance().traceEnable()) {
			SimLogger.getInstance().printLine("Target Node ID:\t" + dest.getWlanId());
		}
		return energy; //energy in nano joules for download of entire path
	}
	
	//uploadEnergy and downloadEnergy are basically the same because nJperBit upload and download are the same
	public static double getUploadEnergy(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest, SimSettings.CLOUD_TRANSFER isCloud) {
		return getDownloadEnergy(sourceDeviceId, destDeviceId, dataSize, wifiSrc, wifiDest, isCloud);
	}
	
	//Returns total idle energy of all fog nodes (power for each node * total simulation time) 
	public static double calculateTotalIdleEnergy() {
		NetworkTopology networkTopology = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology(); 
		HashSet<NodeSim> nodes = networkTopology.getNodes();	
		double totalEnergy = 0;
		double totalTime = SimSettings.getInstance().getSIMULATION_TIME();
		for (NodeSim node: nodes) {
			int level = node.getLevel();
			double idleWatts = Double.parseDouble(DataInterpreter.getNodeSpecs()[DataInterpreter.getMAX_LEVELS() - level][14]);
			totalEnergy += idleWatts;
		}
		return totalEnergy * totalTime;
	}
	
	//calculates the dynamic energy consumption of a task computation
	public static double calculateDynamicEnergyConsumption(Task task) {
		double exCost = (double)task.getCloudletLength() / (k.getPeList().get(0).getMips()) * k.getCostPerSec()
		return 0;
	}
}
