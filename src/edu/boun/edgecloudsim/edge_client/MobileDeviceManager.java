/*
 * Title:        EdgeCloudSim - Mobile Device Manager
 * 
 * Description: 
 * MobileDeviceManager is responsible for submitting the tasks to the related
 * device by using the Edge Orchestrator. It also takes proper actions 
 * when the execution of the tasks are finished.
 * By default, MobileDeviceManager sends tasks to the edge servers or
 * cloud servers. If you want to use different topology, for example
 * MAN edge server, you should modify the flow defined in this class.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
//import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import edu.auburn.pFogSim.mobility.GPSVectorMobility;
import edu.auburn.pFogSim.netsim.*;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.energy.EnergyModel;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.EdgeTask;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
//import javafx.util.Pair;

//import edu.auburn.pFogSim.netsim.NetworkTopology;


/**
 * 
 * @author szs0117
 *
 */
public class MobileDeviceManager extends DatacenterBroker {
	private static final int BASE = 100000; //start from base in order not to conflict cloudsim tag!
	private static final int REQUEST_RECEIVED_BY_CLOUD = BASE + 1;
	private static final int REQUEST_PROCESSED_BY_CLOUD = BASE + 2;
	private static final int REQUEST_RECIVED_BY_EDGE_DEVICE = BASE + 3;
	private static final int RESPONSE_RECEIVED_BY_MOBILE_DEVICE = BASE + 4;
//	private static final int REQUEST_RECIVED_BY_EDGE_ORCHESTRATOR = BASE + 5;
	private int taskIdCounter=0;
	private ArrayList<MobileDevice> mobileDevices;
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public MobileDeviceManager() throws Exception {
		super("Global_Broker");
	}

	
	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		//do nothing!
	}
	
	
	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		NetworkModel networkModel = SimManager.getInstance().getNetworkModel();
		Task task = (Task) ev.getData();

		Location currentLocation = SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock());
		boolean sepa = false;
		//Qian added for sensor generated tasks getting download destination. Uncomment code on line 491 (Producer/Consumer)
		if (task.sens && SimSettings.getInstance().getDeviceSeparation()) {
			sepa = true;
			currentLocation = SimManager.getInstance().getMobilityModel().getLocation(task.getDesMobileDeviceId(),CloudSim.clock());
		} 
		
		//if(task.getSubmittedLocation().equals(currentLocation))
		//{
			SimSettings.CLOUD_TRANSFER isCloud = (task.getAssociatedHostId() == 0)?SimSettings.CLOUD_TRANSFER.CLOUD_DOWNLOAD:SimSettings.CLOUD_TRANSFER.IGNORE;
			double WlanDelay = networkModel.getDownloadDelay(task.getAssociatedHostId() * -1, task.getMobileDeviceId(), task.getCloudletOutputSize(), false, task.wifi, isCloud);
			//calculate the dynamic router energy of the path along which the task is transferred
			double downloadEnergy = EnergyModel.getDownloadEnergy(task.getAssociatedHostId() * -1, task.getMobileDeviceId(), task.getCloudletOutputSize(), false, task.wifi, isCloud);

			if (sepa) {
				WlanDelay = networkModel.getDownloadDelay(task.getAssociatedHostId() * -1, task.getDesMobileDeviceId(), task.getCloudletOutputSize(), false, task.wifi, isCloud);
				//if separation, compute downloadEnergy with slightly different parameters 
				downloadEnergy = EnergyModel.getDownloadEnergy(task.getAssociatedHostId() * -1, task.getDesMobileDeviceId(), task.getCloudletOutputSize(), false, task.wifi, isCloud);
			}
			//add this download energy to the router dynamic field and the total energy field of the EnergyModel class
			EnergyModel.appendRouterEnergy(downloadEnergy);

			SimLogger.getInstance().addHops(task.getCloudletId(), ((ESBModel) networkModel).getHops(task, task.getAssociatedHostId()));
			SimLogger.getInstance().addHopsBack(task.getCloudletId(), ((ESBModel) networkModel).getHopsBack(task, task.getAssociatedHostId(), sepa));
			/*if (((ESBModel) networkModel).getHops(task, task.getAssociatedHostId()) == 0) {
				((ESBModel) networkModel).getHops(task, task.getAssociatedHostId());
				networkModel.getDownloadDelay(task.getAssociatedHostId() * -1, task.getMobileDeviceId(), task.getCloudletOutputSize(), false, task.wifi);
			}*/
			
			if (SimSettings.getInstance().traceEnable()) {
				SimLogger.printLine("WlanDelay: "+ WlanDelay+ "  taskmaxDelay: "+task.getMaxDelay());
				if (WlanDelay < 0)
					SimLogger.printLine("FAILED DUE TO BANDWIDTH during Download in processCloudletReturn");
				if (WlanDelay > task.getMaxDelay())
					SimLogger.printLine("FAILED DUE TO LATENCY during Download in processCloudletReturn");				
			}
			
			if(WlanDelay >= 0 && WlanDelay <= task.getMaxDelay()) {
				networkModel.downloadStarted(currentLocation, SimSettings.GENERIC_EDGE_DEVICE_ID);
				schedule(getId(), WlanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
				SimLogger.getInstance().downloadStarted(task.getCloudletId(), WlanDelay);
			}
			else {
				SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock());
			}
	}
	
	
	/**
	 * 
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null! Terminating simulation...");
			System.exit(0);
			return;
		}
		
		NetworkModel networkModel = SimManager.getInstance().getNetworkModel();
		switch (ev.getTag()) {
			case REQUEST_RECEIVED_BY_CLOUD:
			{
				Task task = (Task) ev.getData();
				
				networkModel.uploadFinished(task.getSubmittedLocation(), SimSettings.CLOUD_DATACENTER_ID);
				
				//save related host id
				task.setAssociatedHostId(SimSettings.CLOUD_HOST_ID);
				
				SimLogger.getInstance().uploaded(task.getCloudletId(),
						SimSettings.CLOUD_DATACENTER_ID,
						SimSettings.CLOUD_HOST_ID,
						SimSettings.CLOUD_VM_ID,
						SimSettings.VM_TYPES.CLOUD_VM.ordinal());
				
				//calculate computational delay in cloud
				double ComputationDelay = (double)task.getCloudletLength() / (double)SimSettings.getInstance().getMipsForCloud();
				
				schedule(getId(), ComputationDelay, REQUEST_PROCESSED_BY_CLOUD, task);
				
				break;
			}
			case REQUEST_PROCESSED_BY_CLOUD:
			{
				Task task = (Task) ev.getData();

				//SimLogger.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + task.getCloudletId() + " received");
				//SimLogger.printLine("SourceID: " + SimSettings.CLOUD_DATACENTER_ID);
				SimSettings.CLOUD_TRANSFER isCloud = SimSettings.CLOUD_TRANSFER.IGNORE;
				double WanDelay = networkModel.getDownloadDelay(SimSettings.CLOUD_DATACENTER_ID, task.getMobileDeviceId(), task.getCloudletOutputSize(), false, task.wifi, isCloud);
				if(WanDelay >= 0 && WanDelay <= task.getMaxDelay())
				{
					Location currentLocation = SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()+WanDelay);
					int currWifiloc = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(currentLocation.getXPos(), currentLocation.getYPos(),currentLocation.getAltitude(), true).getWlanId();
					int actualLoc = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(task.getSubmittedLocation().getXPos(), task.getSubmittedLocation().getYPos(),task.getSubmittedLocation().getAltitude(), true).getWlanId();
					if(actualLoc  == currWifiloc)
					{
						networkModel.downloadStarted(task.getSubmittedLocation(), SimSettings.CLOUD_DATACENTER_ID);
						schedule(getId(), WanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
						SimLogger.getInstance().downloadStarted(task.getCloudletId(), WanDelay);
					}
					else
					{
						//SimLogger.printLine("task cannot be finished due to mobility of user!");
						//SimLogger.printLine("device: " +task.getMobileDeviceId()+" - submitted " + task.getSubmissionTime() + " @ " + task.getSubmittedLocation().getXPos() + " handled " + CloudSim.clock() + " @ " + currentLocation.getXPos());
						SimLogger.getInstance().failedDueToMobility(task.getCloudletId(), CloudSim.clock());
						SimLogger.printLine("!");
					}
				}
				else
				{
					SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock());
				}
				break;
			}
			/*
			case REQUEST_RECIVED_BY_EDGE_ORCHESTRATOR:
			{
				Task task = (Task) ev.getData();
				double internalDelay = networkModel.getDownloadDelay(
						SimSettings.EDGE_ORCHESTRATOR_ID,
						SimSettings.GENERIC_EDGE_DEVICE_ID,
						task.getCloudletOutputSize());
						
				networkModel.downloadStarted(
						SimSettings.EDGE_ORCHESTRATOR_ID,
						SimSettings.GENERIC_EDGE_DEVICE_ID,
						task.getCloudletOutputSize());

				submitTaskToEdgeDevice(task,internalDelay);

				break;
			}
			*/
			case REQUEST_RECIVED_BY_EDGE_DEVICE:
			{
				Task task = (Task) ev.getData();
				
				networkModel.uploadFinished(task.getSubmittedLocation(), SimSettings.GENERIC_EDGE_DEVICE_ID);
				
				submitTaskToEdgeDevice(task,0);
				
				break;
			}
			case RESPONSE_RECEIVED_BY_MOBILE_DEVICE: // Shaik updated
			{
				Task task = (Task) ev.getData();
				
				if(task.getAssociatedHostId() == SimSettings.CLOUD_HOST_ID)
					networkModel.downloadFinished(task.getSubmittedLocation(), SimSettings.CLOUD_DATACENTER_ID);
				else
					networkModel.downloadFinished(task.getSubmittedLocation(), SimSettings.GENERIC_EDGE_DEVICE_ID);
				//Find edgedevice + get cost
				//double cost = task.getActualCPUTime() * task.getCostPerSec();
				LinkedList<NodeSim> path = task.getPath();
				int hostID = task.getAssociatedHostId();
				EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostById(hostID);
				double cost = 0;

				if (path == null || path.size() == 0) {
					//double bwCost = (task.getCloudletFileSize() + task.getCloudletOutputSize()) * k.getCostPerBW(); 
					double bwCost = ((task.getCloudletFileSize() + task.getCloudletOutputSize())*8 / (double)1024) * k.getCostPerBW(); //Data size in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified
					//double exCost = task.getActualCPUTime() * task.getCostPerSec();
					double exCost = (double)task.getCloudletLength() / (k.getPeList().get(0).getMips()) * k.getCostPerSec(); // Shaik modified - May 09, 2019.

					cost = cost + bwCost;
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Task exec: Level:\t" + k.getLevel() + "\tNode:\t" + k.getId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + (task.getCloudletFileSize() + task.getCloudletOutputSize()) + "\tBWCostPerSec:\t" + k.getCostPerBW());
					cost = cost + exCost;
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Task exec: Destination:\t"+ k.getId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Task actual CPU Time:\t" + task.getActualCPUTime() + "\tMipsCostPerSec:\t" + task.getCostPerSec());
				}
				else {
					for (NodeSim node: path) {
						k = SimManager.getInstance().getLocalServerManager().findHostByLoc(node.getLocation().getXPos(), node.getLocation().getYPos(),node.getLocation().getAltitude());
						//double bwCost = (task.getCloudletFileSize() + task.getCloudletOutputSize()) * k.getCostPerBW();
						double bwCost = ((task.getCloudletFileSize() + task.getCloudletOutputSize())*8 / (double)1024) * k.getCostPerBW(); //Data size in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified
						cost = cost + bwCost;
						//SimLogger.getInstance().getCentralizeLogPrinter().println("Task exec: Level:\t" + k.getLevel() + "\tNode:\t" + k.getId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
						//SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + (task.getCloudletFileSize() + task.getCloudletOutputSize()) + "\tBWCostPerSec:\t" + k.getCostPerBW());
					}
					k = SimManager.getInstance().getLocalServerManager().findHostById(hostID);
					double exCost = (double)task.getCloudletLength() / (k.getPeList().get(0).getMips()) * k.getCostPerSec(); // Shaik modified - May 09, 2019.
					double time = (double)task.getCloudletLength() / (k.getPeList().get(0).getMips());
					//calculate dynamic energy (joules) of computation of the task at the end fog node
					double dynamicEnergy = EnergyModel.calculateDynamicEnergyConsumption(task, k, time);
					EnergyModel.appendFogNodeEnergy(dynamicEnergy);
					//double exCost = task.getActualCPUTime() * task.getCostPerSec(); //Shaik - Note: This includes task processing delay + queuing delay at fog node. We do not want to charge the tenant for queuing delay as well, as the delay itself is bad enough, adding extra cost for task execution would make it worse.  
					cost = cost + exCost;
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Task exec: Destination:\t"+ k.getId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Task actual CPU Time:\t" + task.getActualCPUTime() + "\tMipsCostPerSec:\t" + task.getCostPerSec());
				}
				//Qian change cost = latency cost + processing cost
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Task logged total cost:   "+ cost);
				Log.printLine("Task logged total cost:   "+ cost);
				SimLogger.getInstance().downloaded(task.getCloudletId(), CloudSim.clock(), cost);
				
				//Shaik added
				Location devLoc = task.getSubmittedLocation();
				Location hostLoc = SimManager.getInstance().getLocalServerManager().findHostById(task.getAssociatedHostId()).getLocation();
				double hostDistance = DataInterpreter.measure(hostLoc.getYPos(), hostLoc.getXPos(), devLoc.getYPos(), devLoc.getXPos());
				
				double consumerDistance = hostDistance;
				if (task.sens && SimSettings.getInstance().getDeviceSeparation()) {
					int desID = task.getDesMobileDeviceId();
					GPSVectorMobility mb = ((GPSVectorMobility)SimManager.getInstance().getMobilityModel());
					Location consumerLoc = mb.getLocation(desID, task.getFinishTime());
					consumerDistance = DataInterpreter.measure(hostLoc.getYPos(), hostLoc.getXPos(), consumerLoc.getYPos(), consumerLoc.getXPos());
				}
				
				SimLogger.getInstance().addHostDistanceLog(task.getCloudletId(), hostDistance);
				SimLogger.getInstance().addUserDistanceLog(task.getCloudletId(), consumerDistance);
				//Shaik added
				double taskPerceivedDelay = SimLogger.getInstance().getTaskPerceivedDelay(task.getCloudletId());
				//System.out.println("taskPerceivedDelay: after execution: "+taskPerceivedDelay);
				if (taskPerceivedDelay > task.getMaxDelay()) {
					SimLogger.getInstance().taskRejected(task.getCloudletId(), CloudSim.clock(), SimLogger.TASK_STATUS.REJECTED_DUE_TO_UNACCEPTABLE_LATENCY ); // Shaik added
					//System.out.println("submitTask: Task: "+task.getCloudletId()+"  Assigned Host: "+task.getAssociatedHostId()+" - task rejected due to unacceptable latency.");
					
					if (SimSettings.getInstance().traceEnable()) {
						SimLogger.printLine("submitTask: Task: "+task.getCloudletId()+"  Assigned Host: "+task.getAssociatedHostId()+" - task rejected due to unacceptable latency.");
					}
				}
	
				break;
			}
			default:
				SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker. Terminating simulation...");
				System.exit(0);
				break;
		}
	}
	
	
	/**
	 * 
	 * @param task
	 * @param delay
	 */
	public void submitTaskToEdgeDevice(Task task, double delay) {
		//select a VM
		EdgeVM selectedVM = SimManager.getInstance().getEdgeOrchestrator().getVmToOffload(task);
		
		if(selectedVM != null){
			
			/*if (((EdgeHost) selectedVM.getHost()).getLevel() == 4) {
				SimLogger.printLine("we are on lvl 4");
			}*/
			//save related host id
			task.setAssociatedHostId(selectedVM.getHost().getId());
			
			//bind task to related VM
			getCloudletList().add(task);
			bindCloudletToVm(task.getCloudletId(),selectedVM.getId());
			
			//SimLogger.printLine(CloudSim.clock() + ": Cloudlet#" + task.getCloudletId() + " is submitted to VM#" + task.getVmId());
			schedule(getVmsToDatacentersMap().get(task.getVmId()), delay, CloudSimTags.CLOUDLET_SUBMIT, task);
			SimLogger.getInstance().addCloudletToLevel(((EdgeHost) selectedVM.getHost()).getLevel());
			SimLogger.getInstance().uploaded(task.getCloudletId(), selectedVM.getHost().getDatacenter().getId(),
					selectedVM.getHost().getId(),
					selectedVM.getId(),
					selectedVM.getVmType().ordinal());

			if (SimSettings.getInstance().traceEnable()) {
				SimLogger.printLine("submitTaskToEdgeDevice: Task: "+task.getCloudletId()+"  Assigned Host: "+task.getAssociatedHostId()+"  Selected VM: "+ selectedVM.getId());
			}

		}
		else{
			//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
			SimLogger.getInstance().taskRejected(task.getCloudletId(), CloudSim.clock(),getMobileDevices().get(task.getMobileDeviceId()).getAssignHostStatus());

			if (SimSettings.getInstance().traceEnable()) {
				SimLogger.printLine("submitTaskToEdgeDevice: Task: "+task.getCloudletId()+"  Assigned Host: "+task.getAssociatedHostId()+" - task rejected due to host unavailability");
			}
			
		}

	}
	
	
	/**
	 * 
	 * @param edgeTask
	 */
	public void submitTask(EdgeTask edgeTask) {
		NetworkModel networkModel = SimManager.getInstance().getNetworkModel();
		
		//create a task
		Task task = createTask(edgeTask);
		
		Location currentLocation = SimManager.getInstance().getMobilityModel().
				getLocation(task.getMobileDeviceId(),CloudSim.clock());
		
		//set location of the mobile device which generates this task
		task.setSubmittedLocation(currentLocation);

		//add related task to log list
		SimLogger.getInstance().addLog(CloudSim.clock(),
				task.getCloudletId(),
				task.getTaskType().ordinal(),
				(int)task.getCloudletLength(),
				(int)task.getCloudletFileSize(),
				(int)task.getCloudletOutputSize());

		int nextHopId = SimManager.getInstance().getEdgeOrchestrator().getDeviceToOffload(task);
		if (nextHopId < 0) {
			//SimLogger.getInstance().rejectedDueToVMCapacity(task.getCloudletId(), CloudSim.clock()); // Shaik commented
			SimLogger.getInstance().taskRejected(task.getCloudletId(), CloudSim.clock(),getMobileDevices().get(task.getMobileDeviceId()).getAssignHostStatus()); // Shaik added
			// Shaik added	
			if (SimSettings.getInstance().traceEnable()) {
				SimLogger.printLine("submitTask: Task: "+task.getCloudletId()+"  Assigned Host: "+task.getAssociatedHostId()+" - task rejected due to host unavailability");
			}

			return;
		}
		//Qian add host to utilization map
		SimLogger.getInstance().addNodeUtilization(nextHopId, SimManager.getInstance().getLocalServerManager().findHostById(nextHopId));
		//Qian put path to each task.
		NodeSim des = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getLocalServerManager().findHostById(nextHopId).getLocation(), false);
		NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()), false);
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(src, des);
		task.setPath(path);
		if(nextHopId == SimSettings.CLOUD_DATACENTER_ID){
			
			SimSettings.CLOUD_TRANSFER isCloud = SimSettings.CLOUD_TRANSFER.IGNORE;
			double WanDelay = networkModel.getUploadDelay(task.getMobileDeviceId(), nextHopId * -1, task.getCloudletFileSize(), task.wifi, false, isCloud);
			//calculate the dynamic router energy used to upload a task from a mobile device to its destination. 
			double uploadEnergy = EnergyModel.getUploadEnergy(task.getMobileDeviceId(), nextHopId * -1, task.getCloudletFileSize(), task.wifi, false, isCloud);
			if(WanDelay>0){
				networkModel.uploadStarted(currentLocation, nextHopId);
				schedule(getId(), WanDelay, REQUEST_RECEIVED_BY_CLOUD, task);
				SimLogger.getInstance().uploadStarted(task.getCloudletId(),WanDelay);
			}
			else
			{
				//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
				SimLogger.getInstance().rejectedDueToBandwidth(
						task.getCloudletId(),
						CloudSim.clock(),
						SimSettings.VM_TYPES.CLOUD_VM.ordinal());
			}
		}
//		else if(nextHopId == SimSettings.EDGE_ORCHESTRATOR_ID){
//			double WlanDelay = networkModel.getUploadDelay(task.getMobileDeviceId(), nextHopId, task.getCloudletFileSize());
//			
//			if(WlanDelay > 0){
//				networkModel.uploadStarted(task.getMobileDeviceId(), nextHopId, task.getCloudletFileSize());
//				schedule(getId(), WlanDelay, REQUEST_RECIVED_BY_EDGE_ORCHESTRATOR, task);
//				SimLogger.getInstance().uploadStarted(task.getCloudletId(),WlanDelay);
//			}
//			else {
//				SimLogger.getInstance().rejectedDueToBandwidth(
//						task.getCloudletId(),
//						CloudSim.clock(),
//						SimSettings.VM_TYPES.EDGE_VM.ordinal());
//			}
//		}
		else /*(nextHopId == SimSettings.GENERIC_EDGE_DEVICE_ID)*/ {
			SimSettings.CLOUD_TRANSFER isCloud = (nextHopId== 0)?SimSettings.CLOUD_TRANSFER.CLOUD_UPLOAD:SimSettings.CLOUD_TRANSFER.IGNORE;
			double WlanDelay = networkModel.getUploadDelay(task.getMobileDeviceId(), nextHopId * -1, task.getCloudletFileSize(), task.wifi, false, isCloud);
			//calculate the dynamic router energy used to upload a task from a mobile device to its destination.
			double uploadEnergy = EnergyModel.getUploadEnergy(task.getMobileDeviceId(), nextHopId * -1, task.getCloudletFileSize(), task.wifi, false, isCloud);
			EnergyModel.appendRouterEnergy(uploadEnergy);
			if (SimSettings.getInstance().traceEnable()) {
				SimLogger.printLine("WlanDelay: "+ WlanDelay+ "  taskmaxDelay: "+task.getMaxDelay());
				if (WlanDelay < 0)
					SimLogger.printLine("submitTask: FAILED DUE TO BANDWIDTH during Upload in submitTask");
				if (WlanDelay > task.getMaxDelay())
					SimLogger.printLine("submitTask: FAILED DUE TO LATENCY during Upload in submitTask");				
			}
			
			if(WlanDelay > 0){
				networkModel.uploadStarted(currentLocation, nextHopId);
				schedule(getId(), WlanDelay, REQUEST_RECIVED_BY_EDGE_DEVICE, task);
				SimLogger.getInstance().uploadStarted(task.getCloudletId(),WlanDelay);
			}
			else {
				SimLogger.getInstance().rejectedDueToBandwidth(
						task.getCloudletId(),
						CloudSim.clock(),
						SimSettings.VM_TYPES.EDGE_VM.ordinal());
				//double WlanDelay2 = networkModel.getUploadDelay(task.getMobileDeviceId(), nextHopId * -1, task.getCloudletFileSize(), task.wifi, false);
			}
		}
		/*else {
			SimLogger.printLine("Unknown nextHopId! Terminating simulation...");
			System.exit(0);
		}*/
	}
	
	
	/**
	 * 
	 * @param edgeTask
	 * 
	 * @return
	 */
	public Task createTask(EdgeTask edgeTask){
		UtilizationModel utilizationModel = new UtilizationModelFull(); /*UtilizationModelStochastic*/
		UtilizationModel utilizationModelCPU = SimManager.getInstance().getScenarioFactory().getCpuUtilizationModel(edgeTask.taskType);

		Task task = new Task(edgeTask.mobileDeviceId, ++taskIdCounter,
				edgeTask.length, edgeTask.pesNumber,
				edgeTask.inputFileSize, edgeTask.outputFileSize,
				utilizationModelCPU, utilizationModel, utilizationModel, edgeTask.wifi, edgeTask.sensor, edgeTask.actuator);
		
		//set the owner of this task
		task.setUserId(this.getId());
		task.setTaskType(edgeTask.taskType);
		task.setMaxDelay(SimSettings.getInstance().getTaskLookUpTable()[task.getTaskType().ordinal()][10]);
		
		
		//Qian add for sensor generated task getting destination uncomment the code inside if statement.
		//Also please uncomment the line 81. And IdleActiveLoadGenerator.java line 121
		if (edgeTask.sensor && SimSettings.getInstance().getDeviceSeparation()) {
			task.setDesMobileDeviceId(edgeTask.desMobileDeviceId);
		}
		return task;
	}

	
	/**
	 * 
	 */
	public void taskEnded(){
		clearDatacenters();
		finishExecution();
	}
	
	
	/**
	 * 
	 * @param task
	 */
	public void migrateTask(Task task) {
		EdgeVM vm = SimManager.getInstance().getEdgeOrchestrator().getVmToOffload(task);
		task.setAssociatedHostId(vm.getHost().getId());
		bindCloudletToVm(task.getCloudletId(),vm.getId());
		schedule(getVmsToDatacentersMap().get(task.getVmId()), 0, CloudSimTags.CLOUDLET_SUBMIT, task);
	}
	
	
	/**
	 * create mobile devices
	 * @author Qian
	 * @param number
	 */
	public void creatMobileDeviceList(int number) {
		mobileDevices = new ArrayList<>();
		List<EdgeTask> taskList = SimManager.getInstance().getLoadGeneratorModel().getTaskList();
		for (int i =0; i < number; i++) {
			MobileDevice mb = new MobileDevice(i, taskList.get(i).taskType);
			Location lc = ((GPSVectorMobility)SimManager.getInstance().getMobilityModel()).getLastMobileDeviceLocation(i);
			mb.setLocation(lc);
			mobileDevices.add(mb);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<MobileDevice> getMobileDevices() {
		return this.mobileDevices;
	}

	
	/**
	 * @return the taskIdCounter
	 */
	public int getTaskIdCounter() {
		return taskIdCounter;
	}

	
	/**
	 * @param taskIdCounter the taskIdCounter to set
	 */
	public void setTaskIdCounter(int taskIdCounter) {
		this.taskIdCounter = taskIdCounter;
	}

	
	/**
	 * @return the base
	 */
	public static int getBase() {
		return BASE;
	}

	
	/**
	 * @return the requestReceivedByCloud
	 */
	public static int getRequestReceivedByCloud() {
		return REQUEST_RECEIVED_BY_CLOUD;
	}

	
	/**
	 * @return the requestProcessedByCloud
	 */
	public static int getRequestProcessedByCloud() {
		return REQUEST_PROCESSED_BY_CLOUD;
	}

	
	/**
	 * @return the requestRecivedByEdgeDevice
	 */
	public static int getRequestRecivedByEdgeDevice() {
		return REQUEST_RECIVED_BY_EDGE_DEVICE;
	}

	
	/**
	 * @return the responseReceivedByMobileDevice
	 */
	public static int getResponseReceivedByMobileDevice() {
		return RESPONSE_RECEIVED_BY_MOBILE_DEVICE;
	}

	
	/**
	 * @param mobileDevices the mobileDevices to set
	 */
	public void setMobileDevices(ArrayList<MobileDevice> mobileDevices) {
		this.mobileDevices = mobileDevices;
	}
}
