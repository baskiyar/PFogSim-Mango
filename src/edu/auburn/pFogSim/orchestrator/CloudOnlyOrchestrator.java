/**
 * alternate orchestrator for comparison against the puddle orchestrator
 * 
 * this orchestrator only assigns tasks to the cloud
 */

package edu.auburn.pFogSim.orchestrator;

import java.util.ArrayList;
import java.util.LinkedList;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.SimLogger;

public class CloudOnlyOrchestrator extends EdgeOrchestrator {

	private static String node = "Datacenter_0";
	EdgeHost cHost;
	
	public CloudOnlyOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}
	
	@Override
	public void initialize() {
		try {
		cHost = (EdgeHost)(SimManager.getInstance().getLocalServerManager().findHostById(0));
		}
		catch (NullPointerException e) {
			return;
		}
	}

	/**
	 * get the id of the appropriate host
	 */
	@Override
	public int getDeviceToOffload(Task task) {
		try {
			//System.out.println("Task: Cloud orchestrator: assigned Host Id:  " + getHost(task).getId());
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
			EdgeVM assignedVm = (EdgeVM) (getHost(task).getVmList().get(0)); 
			//System.out.println("Task: Cloud orchestrator: assigned VM Id:  " + assignedVm.getId());
			return assignedVm;
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	/**
	 * find the host
	 * @param task
	 * @return
	 */
	private EdgeHost getHost(Task task) {
		MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(task.getMobileDeviceId());
		task.setPath(mb.getPath());
		return mb.getHost();
	}
	/**
	 * set cloud
	 * modified by Qian
	 * @param Datacenter _cloud
	 */
	@Override
	public void setCloud(Datacenter _cloud) {
		if (_cloud.getName().equals(node)) {
			cloud = _cloud;
		}
	}

	/* 
	 * @ author Qian Wang
	 * @ author Shehenaz Shaik
	 * (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
 
		EdgeHost cloudHost = (EdgeHost) cloud.getHostList().get(0);
		cloudHost = (EdgeHost)cHost; // Shaik added
		if (goodHost(cloudHost, mobile)) {
			LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(cloudHost, mobile);
			mobile.setPath(path);
			mobile.setHost(cloudHost);
			mobile.makeReservation();
			
			//-- Shaik - Following code component is to track the expected total cost.
			///*
			double cost=0;
			NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(mobile.getLocation(), false);
			NodeSim des = ((ESBModel)(SimManager.getInstance().getNetworkModel())).getNetworkTopology().findNode(cloudHost.getLocation(), false);

			ESBModel networkModel = (ESBModel)(SimManager.getInstance().getNetworkModel());
			LinkedList<NodeSim> tpath = new LinkedList<>();
			tpath = networkModel.findPath(src, des);
			
			if (tpath == null || tpath.size() == 0) {
				EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(mobile.getLocation().getXPos(), mobile.getLocation().getYPos());
				
				double bwCost = mobile.getBWRequirement() * k.getCostPerBW(); 
				double exCost = (double)mobile.getTaskLengthRequirement() / k.getTotalMips() * k.getCostPerSec();
				cost = cost + bwCost;
				SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + des.getLevel() + "\tNode:\t" + des.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
				SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
				cost = cost + exCost;
				SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
				SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPU Time:\t" + ((double)mobile.getTaskLengthRequirement() / k.getTotalMips()) + "\tMipsCostPerSec:\t" + k.getCostPerSec());
			}
			else {
				SimLogger.getInstance().getCentralizeLogPrinter().println("**********Path From " + src.getWlanId() + " To " + des.getWlanId() + "**********");
				for (NodeSim node: tpath) {
					EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(node.getLocation().getXPos(), node.getLocation().getYPos());
					double bwCost = mobile.getBWRequirement() * k.getCostPerBW();
					cost = cost + bwCost;
					SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + node.getLevel() + "\tNode:\t" + node.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
					SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
				}				
				EdgeHost desHost = SimManager.getInstance().getLocalServerManager().findHostByLoc(des.getLocation().getXPos(), des.getLocation().getYPos());
				double exCost = desHost.getCostPerSec() * 
						((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips());
				cost = cost + exCost;
				SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
				SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPU Time:\t" + ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips()) + "\tMipsCostPerSec:\t" + desHost.getCostPerSec());
			}
			//*/						
		}
		else
			System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
	}

	/**
	 * @return the node
	 */
	public static String getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public static void setNode(String node) {
		CloudOnlyOrchestrator.node = node;
	}
	
}
