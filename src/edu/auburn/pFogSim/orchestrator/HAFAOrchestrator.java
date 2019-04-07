package edu.auburn.pFogSim.orchestrator;

/*
 * @author Shehenaz Shaik
 * @author Qian Wang
 * @author Jacob Hall
 * 
 * For those who come after...
 *  * If you are here to change the puddle orchestrator to the true HAFA architecture, (i.e. change it from optimized 
 * on distance to optimized on cost) then the only changes that need to be made are in DistRadix. The only thing 
 * that needs to be changed is the metric upon which we sort and select nodes. See DistRadix for more info.
 * 
 */
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.auburn.pFogSim.netsim.NetworkTopology;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.auburn.pFogSim.util.MobileDevice;
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
 * @author Qian Wang
 * @author Shehenaz Shaik
 *
 */
public class HAFAOrchestrator extends EdgeOrchestrator {

	/**
	 * constructor
	 * @param _policy
	 * @param _simScenario
	 */
	public HAFAOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}

	@Override
	public void initialize() {
		
	}
	/**
	 * get the id of the appropriate host
	 */

	@Override
	public int getDeviceToOffload(Task task) {
		try {
			return getHost(task).getId();
		}
		catch (NullPointerException e) {
			return -1;
		}
	}

	/**
	 * the the appropriate VM to run on
	 */
	@Override
	public EdgeVM getVmToOffload(Task task) {
		try {
			return ((EdgeVM) getHost(task).getVmList().get(0));
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * get the closest level 0 puddle as a staring point
	 * modified by Qian
	 * @param task
	 * @return
	 */
	private Puddle getNearest0Pud(MobileDevice mb) {
		NetworkTopology network = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology();
		Puddle puddle = null;
		EdgeHost host;
		Location loc = mb.getLocation();
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
		MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(task.getMobileDeviceId());
		task.setPath(mb.getPath());
		return mb.getHost();		
	}

	/**
	 * find an alternate puddle of a given level
	 * modified by Qian
	 * @param task
	 * @param level
	 * @return
	 */
	private Puddle nextBest(MobileDevice mb, int level) {
		NetworkTopology network = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology();
		Puddle puddle = null;
		EdgeHost host;
		Location loc = mb.getLocation();
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
			if(newDist < distance && pud.canHandle(mb)) {
				distance = newDist;
				puddle = pud;
			}
		}
		return puddle;
	}

	/* (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
		if (1==1) 
			return;

		Puddle puddle = getNearest0Pud(mobile);//start with the closest level0 puddle
		Puddle nextBestPuddle = null;
		ArrayList<Puddle> puds = new ArrayList<Puddle>();
		ArrayList<EdgeHost> hosts = new ArrayList<EdgeHost>();
		LinkedList<EdgeHost> candidates;
		EdgeHost host;
		DistRadix radix;
		while(!puddle.canHandle(mobile)) {//if that puddle can't handle the task ask try to find an alternate, then find the lowest level that can handle the task
			nextBestPuddle = nextBest(mobile, puddle.getLevel());
			if (nextBestPuddle != null) {
				break;
			}
			if(puddle.getParent() == null)
			{
				EdgeHost cloudHost = puddle.getHead();
				LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(cloudHost, mobile);
				mobile.setPath(path);
				mobile.setHost(cloudHost);
				mobile.makeReservation();
				return;
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
		radix = new DistRadix(hosts, mobile.getLocation());
		candidates = radix.sortNodesByLatency();//sort those nodes by Latency
		host = candidates.poll();
		try {
			while(!goodHost(host, mobile)) {
				host = candidates.poll();//find the closest node capable of handling the task
			}
		}
		catch (NullPointerException e) {
			host = (EdgeHost) cloud.getHostList().get(0);
		}
		/*if (host.getLevel() == 4) {
			SimLogger.printLine("lvl 4 assigned");
		}*/
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(host, mobile);
		mobile.setPath(path);
		mobile.setHost(host);
		mobile.makeReservation();
		return;
	}
	
}