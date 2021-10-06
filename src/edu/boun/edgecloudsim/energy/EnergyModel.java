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

import java.util.Set;
import java.util.HashSet;


/**
 * Class for energy consumption measurement in PFogSim. Measures idle and dynamic energy of network and fog nodes in system
 * Written by Matthew Merck and Cameron Berry during the Auburn REU in Distributed and Parallel Computing 2019
 * 
 * @author mlm0175
 * @author cmb0220
 *
 */
public class EnergyModel {
	
	//All of these private values are values to be logged.
	//NOTE: totalRouterEnergy and totalFogNodeEnergy denote the total DYNAMIC energy consumption of network and fog nodes (joules)
	//totalIdleEnergy is total idle energy consumption of fog+network
	//totalEnergy is totalIdleEnergy + totalRouterEnergy + totalFogNodeEnergy
	private static double totalRouterEnergy = 0;
	private static double totalFogNodeEnergy = 0;
	private static double totalIdleEnergy = 0;	
	private static double totalEnergy = 0; 
	

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
		Router router = ((ESBModel) SimManager.getInstance().getNetworkModel()).getRouter();
	    path = router.findPath(networkTopology, src, dest);
	    
	    //////////////////////////////////////////////////////////////PATH IS CALCULATED ABOVE AS IN getUploadDelay. 
	    //ENERGY OF PATH IS CALCULATED BELOW
	    
	   // SimLogger.printLine(path.size() + "");
	//	energy += getWlanUploadDelay(src.getLocation(), dataSize, CloudSim.clock()) + SimSettings.ROUTER_PROCESSING_DELAY;
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
			if (current.traverse(nextHop) < 0) {
				SimLogger.printLine("not adjacent");
			}

			//get level of the current network node, and find its corresponding nJperBit value form DataInterpreter
			int level = current.getLevel();
			double nJperBit = Double.parseDouble(DataInterpreter.getNodeSpecs()[DataInterpreter.getMAX_LEVELS() - level][15]);
			//convert dataSize of task to bits, and multiply by nJperBit to get total nanojoules of transfer
			energy += (dataSize * 8000) * nJperBit; //dataSize is in kilobytes. multiply by 8000 to convert to bits
		}
		if (SimSettings.getInstance().traceEnable()) {
			SimLogger.printLine("Target Node ID:\t" + dest.getWlanId());
		}
		return energy / 1000000000; //energy in nano joules for download of entire path converted to joules by dividing by 1e+9
	}
	
	//uploadEnergy and downloadEnergy are basically the same because nJperBit upload and download are the same.
	//if these values become different, fully implement this method
	public static double getUploadEnergy(int sourceDeviceId, int destDeviceId, double dataSize, boolean wifiSrc, boolean wifiDest, SimSettings.CLOUD_TRANSFER isCloud) {
		return getDownloadEnergy(sourceDeviceId, destDeviceId, dataSize, wifiSrc, wifiDest, isCloud);
	}
	
	//Returns total idle energy (joules) of all fog nodes (power for each node * total simulation time) 
	public static void calculateTotalIdleEnergy() {
		NetworkTopology networkTopology = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology(); 
		HashSet<NodeSim> nodes = networkTopology.getNodes();	
		double totalEnergy = 0;
		double totalTimeSeconds = SimSettings.getInstance().getSIMULATION_TIME();
		/*Important: this simulator assumes that routers and fog nodes are paired. As a result, the network topology contains nodes that
		 * represent one fog node, and one network node. DataInterpreter.java contains idle power consumption values for network and fog nodes
		 * of all levels, so for each NodeSim in NetworkTopology, we add the router and fog power to the total power
		 * */
		for (NodeSim node: nodes) {
			int level = node.getLevel();
			double idleFogWatts = Double.parseDouble(DataInterpreter.getNodeSpecs()[DataInterpreter.getMAX_LEVELS() - level][18]);
			double idleRouterWatts = Double.parseDouble(DataInterpreter.getNodeSpecs()[DataInterpreter.getMAX_LEVELS() - level][14]);

			totalEnergy += idleFogWatts;
			totalEnergy += idleRouterWatts;
		}
		
		//After getting all power values, we multiply them by the time in seconds to get energy in joules
		double idle = totalEnergy * totalTimeSeconds;
		totalIdleEnergy = idle;
		System.out.println(nodes.size() + "number");
		EnergyModel.totalEnergy += idle;
	}
	
	//calculates the dynamic energy consumption of a task computation
	public static double calculateDynamicEnergyConsumption(Task task, EdgeHost device, double time) {
//		double exCost = (double)task.getCloudletLength() / (k.getPeList().get(0).getMips()) * k.getCostPerSec()
		int numCores = Integer.parseInt(DataInterpreter.getNodeSpecs()[DataInterpreter.getMAX_LEVELS() - device.getLevel()][9]);
		double idlePower = Double.parseDouble(DataInterpreter.getNodeSpecs()[DataInterpreter.getMAX_LEVELS() - device.getLevel()][18]);
		double maxPower = Double.parseDouble(DataInterpreter.getNodeSpecs()[DataInterpreter.getMAX_LEVELS() - device.getLevel()][19]);
		double powerConsumptionFunction = (maxPower - idlePower) / numCores;
		double coresRequired = SimSettings.getInstance().getTaskLookUpTable()[task.getTaskType().ordinal()][8];
		/*this result is computed by using Equation (4) located on page 6 of Estimating Energy Consumption of Cloud, Fog and
			Edge Computing Infrastructures by Ehsan Ahvar.  */
		/*the powerConsumptionFunction represents the rough amount of energy that a fog node uses to utilize another core.
		 * we then multiply this by the number of cores required for a certain task to get the tasks dynamic energy consumption.
		 * as of right now, however, each task only requires one core.
		 */
		double totalPower = powerConsumptionFunction * coresRequired; 
		double joules = totalPower * time;
		return joules;
	}
	
	//We use append here to mean "add to"
	private static void appendTotalEnergy(double num) {
		totalEnergy += num;
	}
	
	public static void appendRouterEnergy(double num) {
		totalRouterEnergy += num;
		appendTotalEnergy(num);
	}
	
	public static void appendFogNodeEnergy(double num) {
		totalFogNodeEnergy += num;
		appendTotalEnergy(num);
	}

	public static double getTotalEnergy() {
		return totalEnergy;
	}
	
	public static double getTotalRouterEnergy() {
		return totalRouterEnergy;
	}
	
	public static double getTotalFogNodeEnergy() {
		return totalFogNodeEnergy;
	}
	
	public static double getIdleEnergy() {
		return totalIdleEnergy;
	}
	
}
