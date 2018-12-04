package edu.auburn.pFogSim.orchestrator;
/*
 * For those who come after...
 * 
 * If you are here to change the puddle orchestrator to the true HAFA architecture, (i.e. change it from optimized 
 * on distance to optimized on cost) then the only changes that need to be made are in DistRadix. The only thing 
 * that needs to be changed is the metric upon which we sort and select nodes. See DistRadix for more info.
 * @jih0007
 */
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.auburn.pFogSim.netsim.NetworkTopology;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
//import edu.boun.edgecloudsim.utils.SimLogger;
import javafx.util.Pair;
import edu.auburn.pFogSim.Puddle.Puddle;
import edu.auburn.pFogSim.Radix.DistRadix;

import java.util.ArrayList;
import java.util.LinkedList;

import org.cloudbus.cloudsim.Datacenter;
//import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
/**
 * implementation of Edge Orchestrator for using puddles
 * @author Jacob I Hall jih0007@auburn.edu
 *
 */
public class PuddleOrchestrator extends EdgeOrchestrator {

	/**
	 * constructor
	 * @param _policy
	 * @param _simScenario
	 */
	
	
	public PuddleOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}
	@Override
	public void initialize() {
		
	}

	/**
	 * Get the id of host to send the device to.<br>
	 * Find the appropriate puddle, then ask the puddle for a host.
	 */
	@Override
	public int getDeviceToOffload(Task task) {
		EdgeHost host = getHost(task);
		double dist = DataInterpreter.measure(host.getLocation().getYPos(), host.getLocation().getXPos(), task.getSubmittedLocation().getYPos(), task.getSubmittedLocation().getXPos());
		SimLogger.getInstance().addHostDistanceLog(task.getCloudletId(), dist);
		return host.getId();
	}
	/**
	 * get the VM to place the task on
	 */
	@Override
	public EdgeVM getVmToOffload(Task task) {
		return ((EdgeVM) getHost(task).getVmList().get(0));
	}
	/**
	 * get the closest level 0 puddle as a staring point
	 * @param task
	 * @return
	 */
	private Puddle getNearest0Pud(Task task) {
		NetworkTopology network = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology();
		Puddle puddle = null;
		EdgeHost host;
		Location loc = task.getSubmittedLocation();
		double distance = Double.MAX_VALUE;
		double newDist;
		ArrayList<Puddle> pud0s = new ArrayList<Puddle>();
		for (Puddle pud : network.getPuddles()) {//search through the list of puddles and pull out all the layer 0 ones
			if (pud.getLevel() == 1) {
				pud0s.add(pud);
			}
		}
		for (Puddle pud : pud0s) {//choose the puddle whose head has the least distance to the task
			host = pud.getClosestNodes(loc).getFirst();
			newDist = Math.sqrt((Math.pow(loc.getXPos() - host.getLocation().getXPos(), 2) + Math.pow(loc.getYPos() - host.getLocation().getYPos(), 2)));
			if(newDist < distance) {
				distance = newDist;
				puddle = pud;
			}
		}
		return puddle;
	}
	
	/**
	 * find a proper host to place the task
	 * @param task
	 * @return
	 */
	private EdgeHost getHost(Task task) {
		Puddle puddle = getNearest0Pud(task);//start with the closest level0 puddle
		Puddle nextBestPuddle = null;
		ArrayList<Puddle> puds = new ArrayList<Puddle>();
		ArrayList<EdgeHost> hosts = new ArrayList<EdgeHost>();
		LinkedList<EdgeHost> candidates;
		EdgeHost host;
		DistRadix radix;
		while(!puddle.canHandle(task)) {//if that puddle can't handle the task ask try to find an alternate, then find the lowest level that can handle the task
			nextBestPuddle = nextBest(task, puddle.getLevel());
			if (nextBestPuddle != null) {
				break;
			}
			if(puddle.getParent() == null)
			{
				return puddle.getHead();
				//throw new IllegalArgumentException();
			}
			puddle = puddle.getParent();
			/*if (puddle == null) {
				//Assign to cloud instead
				
				throw new IllegalArgumentException();
			}*/
		}
		while(puddle != null) {
			if (nextBestPuddle != null) {//if we had to use an alternate puddle, use it and lose it
				puds.add(nextBestPuddle);
				nextBestPuddle = null;
				puddle = puddle.getParent();
				continue;
			}
			puds.add(puddle);//collect the line of puddles from all layers capable of handling the task
			puddle = puddle.getParent();
		}
		for (Puddle pud : puds) {
			hosts.addAll(pud.getMembers());//get all of the nodes from those puddles
		}
		radix = new DistRadix(hosts, new Location(task.getSubmittedLocation().getXPos(), task.getSubmittedLocation().getYPos()));
		candidates = radix.sortNodes();//sort those nodes by distance
		host = candidates.poll();
		try {
			while(!goodHost(host, task)) {
				host = candidates.poll();//find the closest node capable of handling the task
			}
		}
		catch (NullPointerException e) {
			host = (EdgeHost) cloud.getHostList().get(0);
		}
		/*if (host.getLevel() == 4) {
			SimLogger.printLine("lvl 4 assigned");
		}*/
		return host;
		
	}
	/**
	 * find an alternate puddle of a given level
	 * @param task
	 * @param level
	 * @return
	 */
	private Puddle nextBest(Task task, int level) {
		NetworkTopology network = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology();
		Puddle puddle = null;
		EdgeHost host;
		Location loc = task.getSubmittedLocation();
		double distance = Double.MAX_VALUE;
		double newDist;
		ArrayList<Puddle> pudis = new ArrayList<Puddle>();
		for (Puddle pud : network.getPuddles()) {//search through the list of puddles and pull out all the layer i ones
			if (pud.getLevel() == level) {
				pudis.add(pud);
			}
		}
		for (Puddle pud : pudis) {//choose the puddle whose head has the least distance to the task and can handle it
			host = pud.getClosestNodes(loc).getFirst();
			newDist = Math.sqrt((Math.pow(loc.getXPos() - host.getLocation().getXPos(), 2) + Math.pow(loc.getYPos() - host.getLocation().getYPos(), 2)));
			if(newDist < distance && pud.canHandle(task)) {
				distance = newDist;
				puddle = pud;
			}
		}
		return puddle;
	}
	

}
