/**
 * Local only Orchestrator for comparison against Puddle algorithm.
 * 
 * This orchestrator uses the centralized approach to selecting a VM.
 * @author Jacob hall
 * @author Shehenaz Shaik
 * @author Qian Wang
 */
package edu.auburn.pFogSim.orchestrator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.Radix.DistRadix;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;


/**
 * 
 * @author szs0117
 *
 */
public class LocalOnlyOrchestrator extends EdgeOrchestrator {

	ArrayList<EdgeHost> hosts;
	
	
	/**
	 * 
	 * @param _policy
	 * @param _simScenario
	 */
	public LocalOnlyOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}
	
	
	/**
	 * get all the hosts in the network into one list
	 */
	@Override
	public void initialize() {
		hosts = new ArrayList<EdgeHost>();
		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			// Shaik modified - changed level 1 to level 0 for local-only
			//Qian change it back to 1
			//For change
			if (((EdgeHost) node.getHostList().get(0)).getLevel() == 1) {
				hosts.add(((EdgeHost) node.getHostList().get(0)));
			}
		}
		
		this.avgNumProspectiveHosts = 1;
		this.avgNumMessages = this.avgNumProspectiveHosts * 2; // For each service request (i.e. per device), each host receives resource availability request & sends response.

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
	 *
	 */
	@Override
	public EdgeVM getVmToOffload(Task task) {
		try {
			EdgeVM tempVM = ((EdgeVM) (getHost(task).getVmList().get(0)));
			return tempVM;
			
		}
		catch (NullPointerException e) {
			return null;
		}
	}
	
	

	
	/**
	 * find the host
	 * @author Qian
	 * @return the local host (as same as the submit host)
	 */
	private EdgeHost getHost(Task task) {
		MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(task.getMobileDeviceId());
		task.setPath(mb.getPath());
		return mb.getHost();
	}
	
	
	/*
	 * @author Qian Wang
	 * @author Shehenaz Shaik
	 * (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
		
		//Get the WAP Id of mobile device
		int wapId = mobile.getLocation().getServingWlanId();
		
		// Get local host
		EdgeHost localHost = (EdgeHost) (SimManager.getInstance().getLocalServerManager().findHostByWlanId(wapId));
		if(localHost == null) {
			SimLogger.printLine("Null Host");
		}
		if (goodHost(localHost,mobile)) {
			LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(localHost, mobile);
			mobile.setPath(path);
			mobile.setHost(localHost);
			mobile.makeReservation();
			System.out.println("  Assigned host: " + localHost.getId());
		}
		else
			System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+ wapId +"  Assigned host:  NULL");
	}

	
	/**
	 * @return the hosts
	 */
	public ArrayList<EdgeHost> getHosts() {
		return hosts;
	}
	
	
	/**
	 * @param hosts the hosts to set
	 */
	public void setHosts(ArrayList<EdgeHost> hosts) {
		this.hosts = hosts;
	}
	
	
	/**
	 * 
	 * @param deviceId
	 * @param hostCount
	 */
	public void addNumProspectiveHosts(int deviceId, int hostCount) {
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumProspectiveHosts() {
		return ((double)this.avgNumProspectiveHosts);		
	}
		

	/**
	 * @param deviceId
	 * @param msgCount
	 */
	public void addNumMessages(int deviceId, int msgCount) {
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumMessages() {
		return ((double)this.avgNumMessages);		
	}
		
	
	/**
	 * 
	 * @param deviceId
	 * @param pudCount
	 */
	public void addNumPuddlesSearched(int deviceId, int pudCount) {
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumPuddlesSearched() {
		return ((double)0);		
	}
	
	
}
