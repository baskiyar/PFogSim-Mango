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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import edu.auburn.pFogSim.Puddle.Puddle;
//import edu.auburn.pFogSim.Puddle.Puddle;
import edu.auburn.pFogSim.Voronoi.src.kn.uni.voronoitreemap.diagram.PowerDiagram;
//import edu.auburn.pFogSim.clustering.FogCluster;
import edu.auburn.pFogSim.clustering.FogHierCluster;
import edu.auburn.pFogSim.netsim.Link;
import edu.auburn.pFogSim.netsim.NetworkTopology;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
//import edu.auburn.pFogSim.netsim.Router;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.auburn.pFogSim.mobility.MobilityModel;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.EdgeTask;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import javafx.util.Pair;

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
	private ArrayList<PowerDiagram> voronoiDiagramList = new ArrayList<PowerDiagram>();
	private int[] wapIdList = new int [numOfMobileDevice];

	private static SimManager instance = null;
	
	public SimManager(ScenarioFactory _scenarioFactory, int _numOfMobileDevice, String _simScenario) throws Exception {
		super("SimManager");
		scenarioFactory = _scenarioFactory;
		numOfMobileDevice = _numOfMobileDevice;

		SimLogger.print("Creating tasks...");
		loadGeneratorModel = scenarioFactory.getLoadGeneratorModel();
		loadGeneratorModel.initializeModel();
		SimLogger.printLine("Done, ");
		
		
		//Generate network model
		networkModel = scenarioFactory.getNetworkModel();
		networkModel.initialize();
		
		//Generate edge orchestrator
		edgeOrchestrator = scenarioFactory.getEdgeOrchestrator();
		//edgeOrchestrator.initialize();
		
		//Create Physical Servers
		edgeServerManager = new EdgeServerManager();

		//Create Client Manager
		mobileDeviceManager = new MobileDeviceManager();
		
		wapIdList = new int [numOfMobileDevice];
		instance = this;
	}
	
	public static SimManager getInstance(){
		return instance;
	}
	
	/**
	 * Triggering CloudSim to start simulation
	 */
	public void startSimulation() throws Exception{
		//Starts the simulation
		SimLogger.print(super.getName()+" is starting...");
		
		//Start Edge Servers & Generate VMs
		edgeServerManager.startDatacenters();
		edgeServerManager.createVmList(mobileDeviceManager.getId());
		edgeOrchestrator.initialize();
		SimLogger.print("\n\tCreating device locations...");
		mobilityModel = scenarioFactory.getMobilityModel();
		mobilityModel.initialize();
		SimLogger.printLine("Done,");
		
		//Qian: added for service replacement
		mobileDeviceManager.creatMobileDeviceList(numOfMobileDevice);
		for (MobileDevice mobile: mobileDeviceManager.getMobileDevices()) {
			edgeOrchestrator.assignHost(mobile);
		}
		
		CloudSim.startSimulation();
	}
	
	public void addToVoronoiDiagramList(PowerDiagram diagram)
	{
		this.voronoiDiagramList.add(diagram);
	}
	
	public PowerDiagram getVoronoiDiagramAtLevel(int level)
	{
		if(level < voronoiDiagramList.size())
			return voronoiDiagramList.get(level);
		return null;
	}
	
	public ScenarioFactory getScenarioFactory(){
		return scenarioFactory;
	}
	
	//Added by Qian
	public LoadGeneratorModel getLoadGeneratorModel() {
		return loadGeneratorModel;
	}
	
	public int getNumOfMobileDevice(){
		return numOfMobileDevice;
	}
	
	public NetworkModel getNetworkModel(){
		return networkModel;
	}

	public MobilityModel getMobilityModel(){
		return mobilityModel;
	}
	
	public EdgeOrchestrator getEdgeOrchestrator(){
		return edgeOrchestrator;
	}
	
	public EdgeServerManager getLocalServerManager(){
		return edgeServerManager;
	}

	public MobileDeviceManager getMobileDeviceManager(){
		return mobileDeviceManager;
	}
	
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
		//SimLogger.printLine("treeMap.size() = " + mobilityModel.getSize());

		for(int i = 0; i < mobilityModel.getSize(); i++)
		{
			wapIdList[i] = mobilityModel.getWlanId(i);
		}
		
		//Periodic event loops starts from here!
		schedule(getId(), 5, CHECK_ALL_VM);
		schedule(getId(), SimSettings.getInstance().getSimulationTime()/100, PRINT_PROGRESS);
		schedule(getId(), SimSettings.getInstance().getVmLoadLogInterval(), GET_LOAD_LOG);
		schedule(getId(), SimSettings.getInstance().getSimulationTime(), STOP_SIMULATION);
		SimLogger.printLine("Done.");
		SimLogger.printLine("Executing");
	}

	@Override
	public void processEvent(SimEvent ev) {
		synchronized(this){
			switch (ev.getTag()) {
			case CREATE_TASK:
				//SimLogger.printLine("CREATE_TASK reached");
				try {
					EdgeTask edgeTask = (EdgeTask) ev.getData();
					mobileDeviceManager.submitTask(edgeTask);						
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				break;
			case CHECK_ALL_VM:
				SimLogger.printLine("CHECK_ALL_VM reached");
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
	
	@Override
	public void shutdownEntity() {
		edgeServerManager.terminateDatacenters();
	}

	public ArrayList<PowerDiagram> getVoronoiDiagram() {
		// TODO Auto-generated method stub
		return this.voronoiDiagramList;
	}
}