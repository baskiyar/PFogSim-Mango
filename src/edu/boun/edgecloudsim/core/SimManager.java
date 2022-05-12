/*
 * Title:        EdgeCloudSim - Simulation Manager
 * 
 * Description: 
 * SimManager is an singleton class providing many abstract classeses such as
 * Network Model, Mobility Model, Edge Orchestrator to other modules
 * Critical simulation related information would be gathered via this class 
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.core;

import java.io.IOException;
import java.util.List;
import org.cloudbus.cloudsim.Datacenter;
//import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
//import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
//import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.edge_server.VmAllocationPolicy_Custom;
import edu.auburn.pFogSim.netsim.NetworkTopology;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.auburn.pFogSim.mobility.MobilityModel;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.EdgeTask;
import edu.boun.edgecloudsim.utils.SimLogger;


/**
 * 
 * @author szs0117
 *
 */
public class SimManager extends SimEntity {
	private static final int CREATE_TASK = 0;
	private static final int CHECK_ALL_VM = 1;
	private static final int GET_LOAD_LOG = 2;
	private static final int PRINT_PROGRESS = 3;
	private static final int STOP_SIMULATION = 4;
	
	public static final int MAX_WIDTH = 1000;
	public static final int MAX_HEIGHT = 1000;
	
	//List of ids for wireless access points devices are connected to, max devices right now is 2100
	private int numOfMobileDevice;
	private NetworkModel networkModel;
	private MobilityModel mobilityModel;
	private ScenarioFactory scenarioFactory;
	private EdgeOrchestrator edgeOrchestrator;
	private EdgeServerManager edgeServerManager;
	private LoadGeneratorModel loadGeneratorModel;
	private MobileDeviceManager mobileDeviceManager;
	private NetworkTopology networkTopology;
	private int[] wapIdList = new int [numOfMobileDevice];

	private static SimManager instance = null;
	
	
	/**
	 * Constructor
	 * - Generates workload for all mobile devices.
	 * - Configures network topology
	 * - Identify orchestrator policy
	 * - Create physical servers (fog nodes) and mobile devices
	 * @param _scenarioFactory
	 * @param _numOfMobileDevice
	 * @param _simScenario
	 * @throws Exception
	 */
	public SimManager(ScenarioFactory _scenarioFactory, int _numOfMobileDevice, String _simScenario) throws Exception {
		super("SimManager");
		scenarioFactory = _scenarioFactory;
		numOfMobileDevice = _numOfMobileDevice;

		// Generate workload (tasks) for all mobile devices to be executed during the simulation test run.  
		SimLogger.print("Creating tasks...");
		loadGeneratorModel = scenarioFactory.getLoadGeneratorModel();
		loadGeneratorModel.initializeModel();
		SimLogger.printLine("Done.");
		
		
		//Generate network model
		networkModel = scenarioFactory.getNetworkModel();
		networkModel.initialize();
		
		//Identify orchestrator policy 
		edgeOrchestrator = scenarioFactory.getEdgeOrchestrator();
		//edgeOrchestrator.initialize();
		
		//Create Physical Servers
		edgeServerManager = new EdgeServerManager();

		//Create Client Manager
		mobileDeviceManager = new MobileDeviceManager();
		
		wapIdList = new int [numOfMobileDevice];
		instance = this;
	}
	
	
	/**
	 * Returns current instance of SimManager 
	 * @return
	 */
	public static SimManager getInstance(){
		return instance;
	}
	
	
	/**
	 * Triggering CloudSim to start simulation
	 */
	public void startSimulation() throws Exception{
		//Starts the simulation
		SimLogger.print("Starting " + super.getName() + ":\n\t");
		
		//Start Edge Servers & Generate VMs
		SimLogger.print("Start Edge Servers & Generate VMs...");
		edgeServerManager.startDatacenters();
		edgeServerManager.createVmList(mobileDeviceManager.getId());
		SimLogger.printLine("Done.");
		
		// Initialize service placement approach 
		SimLogger.print("Initializing Edge Orchestrator...");
		edgeOrchestrator.initialize();
		SimLogger.printLine("Done.");

		
		//Create devices
		SimLogger.print("Creating device locations...");
		mobilityModel = scenarioFactory.getMobilityModel();
		mobilityModel.initialize();
		SimLogger.printLine("Done.");
		
		//Assign hosts to devices, as selected by Service placement approach
		mobileDeviceManager.creatMobileDeviceList(numOfMobileDevice);
		for (MobileDevice mobile: mobileDeviceManager.getMobileDevices()) {
			edgeOrchestrator.assignHost(mobile);
		}
		
		CloudSim.startSimulation();
	}
	
	

	
	
	/**
	 * 
	 * @return
	 */
	public ScenarioFactory getScenarioFactory(){
		return scenarioFactory;
	}
	
	//Added by Qian
	/**
	 * 
	 * @return
	 */
	public LoadGeneratorModel getLoadGeneratorModel() {
		return loadGeneratorModel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getNumOfMobileDevice(){
		return numOfMobileDevice;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public NetworkModel getNetworkModel(){
		return networkModel;
	}

	
	/**
	 * 
	 * @return
	 */
	public MobilityModel getMobilityModel(){
		return mobilityModel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public EdgeOrchestrator getEdgeOrchestrator(){
		return edgeOrchestrator;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public EdgeServerManager getLocalServerManager(){
		return edgeServerManager;
	}

	
	/**
	 * 
	 * @return
	 */
	public MobileDeviceManager getMobileDeviceManager(){
		return mobileDeviceManager;
	}
	
	
	/**
	 * 
	 */
	@Override
	public void startEntity() {
		
		for(int i=0; i<edgeServerManager.getDatacenterList().size(); i++)
			mobileDeviceManager.submitVmList(edgeServerManager.getVmList(i));
		
		//Creation of tasks are scheduled here!
		for(int i=0; i< loadGeneratorModel.getTaskList().size(); i++)
		{
			schedule(getId(), loadGeneratorModel.getTaskList().get(i).startTime, CREATE_TASK, loadGeneratorModel.getTaskList().get(i));
		}
		
		//Get all of the initial wireless access points ids for all the mobile devices

		for(int i = 0; i < mobilityModel.getSize(); i++)
		{
			wapIdList[i] = mobilityModel.getWlanId(i);
		}
		
		//Periodic event loops starts from here!
		schedule(getId(), 5, CHECK_ALL_VM);
		schedule(getId(), SimSettings.getInstance().getSimulationTime()/100, PRINT_PROGRESS);
		schedule(getId(), SimSettings.getInstance().getVmLoadLogInterval(), GET_LOAD_LOG);
		schedule(getId(), SimSettings.getInstance().getSimulationTime(), STOP_SIMULATION);
		// SimLogger.printLine("Done.");
		// SimLogger.printLine("Executing");
	}

	
	/**
	 * 
	 */
	@Override
	public void processEvent(SimEvent ev) {
		synchronized(this){
			switch (ev.getTag()) {
			case CREATE_TASK:
				try {
					EdgeTask edgeTask = (EdgeTask) ev.getData();
					mobileDeviceManager.submitTask(edgeTask);						
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				break;
			case CHECK_ALL_VM:
				// SimLogger.printLine("CHECK_ALL_VM reached");
				int totalNumOfVm = SimSettings.getInstance().getNumOfEdgeVMs();
				if(VmAllocationPolicy_Custom.getCreatedVmNum() != totalNumOfVm) {
					int vms = 0;
					for (List<EdgeVM> host: edgeServerManager.getVMS()) {
						for (EdgeVM vm : host) {
							if (vm.getHost() != null) {
								vms++;
							}
						}
					}
					SimLogger.printLine("Total # of Vms : " + totalNumOfVm);
					SimLogger.printLine("Vms Made : " + VmAllocationPolicy_Custom.getCreatedVmNum());
					SimLogger.printLine("Real VMs : " + vms);
					SimLogger.printLine("All VMs cannot be created! Terminating simulation...");
					System.exit(0);
				}
				else SimLogger.printLine("All VMs could be created!");
				break;
			case GET_LOAD_LOG:
				SimLogger.getInstance().addVmUtilizationLog(CloudSim.clock(),edgeServerManager.getAvgUtilization());
				// for each fog node - capture current utilization - Shaik added
				for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
					SimLogger.getInstance().addFNMipsUtilizationLog(CloudSim.clock(), ((EdgeHost) node.getHostList().get(0)).getId(), ((EdgeHost)node.getHostList().get(0)).getLevel(), ((EdgeHost)node.getHostList().get(0)).getFnMipsUtilization()); // Shaik added *100 - to report 100 times of current utlization, as node mips utilization is limited to 1% for our test environment.// Harmon removed after logging >100% utilization.
					SimLogger.getInstance().addFNNwUtilizationLog(CloudSim.clock(), ((EdgeHost) node.getHostList().get(0)).getId(), ((EdgeHost)node.getHostList().get(0)).getLevel(), ((EdgeHost)node.getHostList().get(0)).getFnNwUtilization());
				}	
				schedule(getId(), SimSettings.getInstance().getVmLoadLogInterval(), GET_LOAD_LOG);
				break;
			case PRINT_PROGRESS:
				//Updates the positions of FOG Devices if necessary
				/*HashSet<Link> links = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().getLinks();
				Set<NodeSim> nodes = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().getMobileNodes();
				
				//ArrayList<EdgeHost> movingNodes = new ArrayList<EdgeHost>();
				//EdgeHost moving = null;
				for(NodeSim node : nodes) {
					//Update positions
					Location currentLoc = node.getLocation();
					//This is testing to see if any mobile fog nodes are moving past the edge of the simulation space
					if(currentLoc.getXPos() + node.getVector().getXPos() > MAX_WIDTH) node.setVector(new Location(node.getVector().getXPos() * -1, node.getVector().getYPos()));
					if(currentLoc.getYPos() + node.getVector().getYPos() > MAX_HEIGHT) node.setVector(new Location(node.getVector().getXPos(), node.getVector().getYPos() * -1));

					//Change links
					for(Link link : links) {
						if(link.getLeftLink().equals(currentLoc)) {
							//Sets that location to what it will be in a bit
							link.setLeftLink(new Location(currentLoc.getXPos() + node.getVector().getXPos(), currentLoc.getYPos() + node.getVector().getYPos()));
							//SimLogger.printLine("Left Link changed");
						}
						else if(link.getRightLink().equals(currentLoc)) {
							//Sets that location to what it will be in a bit
							link.setRightLink(new Location(currentLoc.getXPos() + node.getVector().getXPos(), currentLoc.getYPos() + node.getVector().getYPos()));
							//SimLogger.printLine("Right Link changed");
						}
							
					//Change nodes
					//moving = EdgeServerManager.getInstance().findHostByLoc(node.getLocation().getXPos(), node.getLocation().getYPos());
					
					//moving.setPlace(new Location(node.getWlanId(), node.getLocation().getXPos(), node.getLocation().getYPos()));
					//movingNodes.add(moving);
					}
					node.setLocation(new Location(currentLoc.getXPos() + node.getVector().getXPos(), currentLoc.getYPos() + node.getVector().getYPos()));
					//newNodes.add(node);
				}
				for(Link link : links) {
					newLinks.add(link);
				}
				((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology().setMobileNode(nodes);*/
				
				//-----------------Rerun clustering and puddles only if there are mobile FogNodes------------------------------
				
				//FogHierCluster clusterObject = new FogHierCluster(newNodes);
				//List<Puddle> puds = networkTopology.getPuddles();
				//networkTopology = new NetworkTopology(newNodes, newLinks);
				/*if(!((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology().cleanNodes()) {
					SimLogger.printLine("Topology is not valid");
					System.exit(0);
				}*/
				//networkTopology.setPuddles(puds);
				//edgeServerManager.setHosts(movingNodes);
				//Sets network topology and uses it to make the Puddle Objects
				//((ESBModel) SimManager.getInstance().getNetworkModel()).setNetworkTopology(networkTopology);
				//networkTopology.setPuddles(edgeServerManager.makePuddles(clusterObject));
				
				//Goes through all devices and checks to see if WAP ids have changed
				//	Currently checks devices every percentage of progress, dt changes based on how large your simulation time is
				double time = CloudSim.clock();
				for(int q = 0; q < mobilityModel.getSize(); q++) {
					//If the id has changed, update the value in our list and move the cloudlet to a more appropriate VM
					if(wapIdList[q] != mobilityModel.getWlanId(q, time)) {
						wapIdList[q] = mobilityModel.getWlanId(q, time);
						if (mobileDeviceManager.getCloudletList().size() > q) {
							Task task = (Task) mobileDeviceManager.getCloudletList().get(q);
							task.setSubmittedLocation(mobilityModel.getLocation(q, time));
							mobileDeviceManager.migrateTask(task);
						}
					}
				}
				//Prints progress
				int progress = (int)((CloudSim.clock()*100)/SimSettings.getInstance().getSimulationTime());
				if(progress % 10 == 0)
					SimLogger.print(Integer.toString(progress));
				else
					SimLogger.print(".");
				if(CloudSim.clock() < SimSettings.getInstance().getSimulationTime())
					schedule(getId(), SimSettings.getInstance().getSimulationTime()/100, PRINT_PROGRESS);
				break;
			case STOP_SIMULATION:
				SimLogger.printLine("100");
				CloudSim.terminateSimulation();
				try {
					SimLogger.getInstance().simStopped();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
				break;
			default:
				Log.printLine(getName() + ": unknown event type");
				break;
			}
		}
	}
	
	
	/**
	 * 
	 */
	@Override
	public void shutdownEntity() {
		edgeServerManager.terminateDatacenters();
	}

	

		
	/**
	 * @return the edgeServerManager
	 */
	public EdgeServerManager getEdgeServerManager() {
		return edgeServerManager;
	}

	
	/**
	 * @param edgeServerManager the edgeServerManager to set
	 */
	public void setEdgeServerManager(EdgeServerManager edgeServerManager) {
		this.edgeServerManager = edgeServerManager;
	}

	
	/**
	 * @return the networkTopology
	 */
	public NetworkTopology getNetworkTopology() {
		return networkTopology;
	}

	
	/**
	 * @param networkTopology the networkTopology to set
	 */
	public void setNetworkTopology(NetworkTopology networkTopology) {
		this.networkTopology = networkTopology;
	}

	


	
	/**
	 * @return the wapIdList
	 */
	public int[] getWapIdList() {
		return wapIdList;
	}

	
	/**
	 * @param wapIdList the wapIdList to set
	 */
	public void setWapIdList(int[] wapIdList) {
		this.wapIdList = wapIdList;
	}

	
	/**
	 * @return the createTask
	 */
	public static int getCreateTask() {
		return CREATE_TASK;
	}

	
	/**
	 * @return the checkAllVm
	 */
	public static int getCheckAllVm() {
		return CHECK_ALL_VM;
	}

	
	/**
	 * @return the getLoadLog
	 */
	public static int getGetLoadLog() {
		return GET_LOAD_LOG;
	}

	
	/**
	 * @return the printProgress
	 */
	public static int getPrintProgress() {
		return PRINT_PROGRESS;
	}

	
	/**
	 * @return the stopSimulation
	 */
	public static int getStopSimulation() {
		return STOP_SIMULATION;
	}

	
	/**
	 * @return the maxWidth
	 */
	public static int getMaxWidth() {
		return MAX_WIDTH;
	}

	
	/**
	 * @return the maxHeight
	 */
	public static int getMaxHeight() {
		return MAX_HEIGHT;
	}

	
	/**
	 * @param numOfMobileDevice the numOfMobileDevice to set
	 */
	public void setNumOfMobileDevice(int numOfMobileDevice) {
		this.numOfMobileDevice = numOfMobileDevice;
	}

	
	/**
	 * @param networkModel the networkModel to set
	 */
	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}

	
	/**
	 * @param mobilityModel the mobilityModel to set
	 */
	public void setMobilityModel(MobilityModel mobilityModel) {
		this.mobilityModel = mobilityModel;
	}

	
	/**
	 * @param scenarioFactory the scenarioFactory to set
	 */
	public void setScenarioFactory(ScenarioFactory scenarioFactory) {
		this.scenarioFactory = scenarioFactory;
	}

	
	/**
	 * @param edgeOrchestrator the edgeOrchestrator to set
	 */
	public void setEdgeOrchestrator(EdgeOrchestrator edgeOrchestrator) {
		this.edgeOrchestrator = edgeOrchestrator;
	}

	
	/**
	 * @param loadGeneratorModel the loadGeneratorModel to set
	 */
	public void setLoadGeneratorModel(LoadGeneratorModel loadGeneratorModel) {
		this.loadGeneratorModel = loadGeneratorModel;
	}

	
	/**
	 * @param mobileDeviceManager the mobileDeviceManager to set
	 */
	public void setMobileDeviceManager(MobileDeviceManager mobileDeviceManager) {
		this.mobileDeviceManager = mobileDeviceManager;
	}

	
	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(SimManager instance) {
		SimManager.instance = instance;
	}
}