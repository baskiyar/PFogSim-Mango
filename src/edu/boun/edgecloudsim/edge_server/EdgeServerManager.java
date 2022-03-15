/*
 * Title:        EdgeCloudSim - Edge Server Manager
 * 
 * Description: 
 * EdgeServerManager is responsible for creating datacenters, hosts and VMs.
 * It also provides the list of VMs running on the hosts.
 * This information is critical for the edge orchestrator.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
//import edu.boun.edgecloudsim.utils.LinesComponent;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.auburn.pFogSim.Puddle.Puddle;
import edu.auburn.pFogSim.Radix.BinaryHeap;
import edu.auburn.pFogSim.clustering.*;
import edu.auburn.pFogSim.netsim.*;
import edu.auburn.pFogSim.orchestrator.HAFAOrchestrator;


/**
 * 
 * @author szs0117
 *
 */
public class EdgeServerManager {
	private static final int MAX_HAFA_LEVEL = 7;
	private static final int ALTITUDE_INDEX = 2;
	private static final int LATITUDE_INDEX = 1;
	private static final int LONGITUDE_INDEX = 0;
	private List<Datacenter> localDatacenters;
	private List<List<EdgeVM>> vmList;
	private List<EdgeHost> hostList;// we use this for mobile fog nodes, this is NOT a  list of all edgehosts in the network
	private int hostIdCounter;
	private NetworkTopology networkTopology;
	private static EdgeServerManager instance = null;
	public  Puddle[][] puddles;  //-- make it private - later
	
	//CJ Added these to make the lists of all the nodes and respective links 
	//	to pass to topology constructor
	private List<NodeSim> nodesForTopography = new ArrayList<NodeSim>();
	private List<Link> linksForTopography = new ArrayList<Link>();

	
	/**
	 * Constructor
	 */
	public EdgeServerManager() {
		localDatacenters=new ArrayList<Datacenter>();
		vmList = new ArrayList<List<EdgeVM>>();
		vmList.add(new ArrayList<EdgeVM>());
		hostIdCounter = 0; // Shaik modified from hostIdCounter = 1; 
		instance = this;
	}
	
	
	/**
	 * Return current instance object
	 * @return
	 */
	public static EdgeServerManager getInstance()
	{
		return instance;
	}

	
	/**
	 * 
	 * @return
	 */
	public List<List<EdgeVM>> getVMS() {
		return vmList;
	}
	
	
	/**
	 * 
	 * @param hostId
	 * @return
	 */
	public List<EdgeVM> getVmList(int hostId){
		return vmList.get(hostId);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public List<Datacenter> getDatacenterList(){
		return localDatacenters;
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void startDatacenters() throws Exception{
		//create random number generator for each place
		Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
		NodeList datacenterList = doc.getElementsByTagName("datacenter");
		for (int i = 0; i < datacenterList.getLength(); i++) {
			Node datacenterNode = datacenterList.item(i);
			Element datacenterElement = (Element) datacenterNode;
			localDatacenters.add(createDatacenter(i, datacenterElement));
		}
		
		//CJ Adding the links after the nodes, essentially the same process as with nodes
		doc = SimSettings.getInstance().getLinksDocument();
		NodeList linksList = doc.getElementsByTagName("link");
		for(int i = 0; i < linksList.getLength(); i++) {
			
			Node links = linksList.item(i);
			Element linkElement = (Element) links;
			
			
			NodeList leftLinksList = linkElement.getElementsByTagName("left");
			Node leftLinks = leftLinksList.item(0);
			Element leftLinkss = (Element)leftLinks;
			double x_pos1 = Double.parseDouble(leftLinkss.getElementsByTagName("x_pos").item(0).getTextContent());
			double y_pos1 = Double.parseDouble(leftLinkss.getElementsByTagName("y_pos").item(0).getTextContent());
			double alt1 = Double.parseDouble(leftLinkss.getElementsByTagName("altitude").item(0).getTextContent());
			Location leftCoor = new Location(x_pos1, y_pos1, alt1);
			
			NodeList rightLinksList = linkElement.getElementsByTagName("right");
			Node rightLinks = rightLinksList.item(0);
			Element rightLinkss = (Element)rightLinks;
			double x_pos2 = Double.parseDouble(rightLinkss.getElementsByTagName("x_pos").item(0).getTextContent());
			double y_pos2 = Double.parseDouble(rightLinkss.getElementsByTagName("y_pos").item(0).getTextContent());
			double alt2 = Double.parseDouble(rightLinkss.getElementsByTagName("altitude").item(0).getTextContent());
			Location rightCoor = new Location(x_pos2, y_pos2, alt2);
			
			double left_lat = Double.parseDouble(linkElement.getElementsByTagName("left_latency").item(0).getTextContent());
			double right_lat = Double.parseDouble(linkElement.getElementsByTagName("right_latency").item(0).getTextContent());
			
			Link newLink = new Link(rightCoor,leftCoor, right_lat, left_lat);
			linksForTopography.add(newLink);
		}
		networkTopology = new NetworkTopology(nodesForTopography, linksForTopography);
		if(!networkTopology.cleanNodes()) {
			SimLogger.printLine("Topology is not valid");
			System.exit(0);
		}
		
		((ESBModel) SimManager.getInstance().getNetworkModel()).setNetworkTopology(networkTopology);
		//((ESBModel) SimManager.getInstance().getNetworkModel()).gravityWell();

		// If HAFA Orchestrator, configure HAFA logical architecture
		if (SimManager.getInstance().getEdgeOrchestrator() instanceof HAFAOrchestrator) {
			SimLogger.print("\n\t Creating clusters of fog nodes...");
			
			// Organize fog nodes belonging to each layer into clusters based on predefined criteria.
			FogHierCluster clusterObject = new FogHierCluster((ArrayList<NodeSim>)nodesForTopography);
			
			//Populate puddles with cluster members; Save configuration in network topology
			networkTopology.setPuddles(makePuddles(clusterObject));
			
			SimLogger.printLine("HAFA Achitecture configuration completed.");

		}// end HAFA Arch
		checkUniqueDC();
	}

	
	/**
	 * 
	 * @param brokerID
	 */
	public void createVmList(int brokerID){
		int hostCounter=0;
		int vmCounter=0;
		
		// SimLogger.printLine("createVmList reached");
		
		//Create VMs for each hosts
		Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
		NodeList datacenterList = doc.getElementsByTagName("datacenter");
		for (int dataCenterIdx = 0; dataCenterIdx < datacenterList.getLength(); dataCenterIdx++) {
			Node datacenterNode = datacenterList.item(dataCenterIdx);
			Element datacenterElement = (Element) datacenterNode;
			String arch = datacenterElement.getAttribute("arch");
			NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
			
			Element location = (Element)datacenterElement.getElementsByTagName("location").item(0); // shaik added
			
			for (int hostNodeIdx = 0; hostNodeIdx < hostNodeList.getLength(); hostNodeIdx++) {
				
				vmList.add(hostCounter, new ArrayList<EdgeVM>());
				
				Node hostNode = hostNodeList.item(hostNodeIdx);
				Element hostElement = (Element) hostNode;
				NodeList vmNodeList = hostElement.getElementsByTagName("VM");
				for (int vmNodeIdx = 0; vmNodeIdx < vmNodeList.getLength(); vmNodeIdx++) {
					Node vmNode = vmNodeList.item(vmNodeIdx);					
					Element vmElement = (Element) vmNode;

					String vmm = vmElement.getAttribute("vmm");
					
					int numOfCores = Integer.parseInt(vmElement.getElementsByTagName("core").item(0).getTextContent());
					double mips = Double.parseDouble(vmElement.getElementsByTagName("mips").item(0).getTextContent());
					double ram = Double.parseDouble(vmElement.getElementsByTagName("ram").item(0).getTextContent());
					long storage = Long.parseLong(vmElement.getElementsByTagName("storage").item(0).getTextContent());
					//long bandwidth = SimSettings.getInstance().getWlanBandwidth() / (hostNodeList.getLength()+vmNodeList.getLength());
					long bandwidth = Long.parseLong(location.getElementsByTagName("bandwidth").item(0).getTextContent()); // shaik added
					
					//VM Parameters		
					EdgeVM vm = new EdgeVM(vmCounter, brokerID, mips, numOfCores, (int) ram, bandwidth, storage, vmm, new CloudletSchedulerTimeShared());
					vm.setVmType(SimSettings.VM_TYPES.EDGE_VM);
					vm.setArch(arch);
					vmList.get(hostCounter).add(vm);
					
					// added if statement to catch odd cases, but should never be seen
					if (vmCounter != hostCounter)
						SimLogger.printLine("Created EdgeVM with id: "+vmCounter+" from config of host with id: "+hostCounter);
					
					vmCounter++;
				}

				hostCounter++;
			}
		}
		// SimLogger.printLine("createVmList - completed");
	}
	
	
	/**
	 * 
	 */
	public void terminateDatacenters(){
		for (Datacenter datacenter : localDatacenters) {
			datacenter.shutdownEntity();
		}
	}

	
	/**
	 * average utilization of all VMs
	 * @return
	 */
	public double getAvgUtilization(){
		double totalUtilization = 0;
		double vmCounter = 0;
		
		// for each datacenter...
		for(int localDataCenterIdx= 0; localDataCenterIdx<localDatacenters.size(); localDataCenterIdx++) {
			List<? extends Host> localDataCenterHosts = localDatacenters.get(localDataCenterIdx).getHostList();
			// for each host...
			for (int localHostIdx=0; localHostIdx < localDataCenterHosts.size(); localHostIdx++) {
				Host host = localDataCenterHosts.get(localHostIdx);
				List<EdgeVM> vmArray = SimManager.getInstance().getLocalServerManager().getVmList(host.getId());
				//for each vm...
				for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
					totalUtilization += vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
					vmCounter++;
				}
			}
		}
		return totalUtilization / vmCounter;
	}

	
	/**
	 * 
	 * @param index
	 * @param datacenterElement
	 * @return
	 * @throws Exception
	 */
	private Datacenter createDatacenter(int index, Element datacenterElement) throws Exception{
		String arch = datacenterElement.getAttribute("arch");
		String os = datacenterElement.getAttribute("os");
		String vmm = datacenterElement.getAttribute("vmm");
		double costPerBw = Double.parseDouble(datacenterElement.getElementsByTagName("costPerBw").item(0).getTextContent());
		double costPerSec = Double.parseDouble(datacenterElement.getElementsByTagName("costPerSec").item(0).getTextContent());
		double costPerMem = Double.parseDouble(datacenterElement.getElementsByTagName("costPerMem").item(0).getTextContent());
		double costPerStorage = Double.parseDouble(datacenterElement.getElementsByTagName("costPerStorage").item(0).getTextContent());
		
		hostList = createHosts(datacenterElement);
		
		String name = "Datacenter_" + Integer.toString(index);
		double time_zone = 3.0;         // time zone this resource located
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, costPerSec, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
	
		VmAllocationPolicy vm_policy = SimManager.getInstance().getScenarioFactory().getVmAllocationPolicy(hostList,index);
		datacenter = new Datacenter(name, characteristics, vm_policy, storageList, 0);
		
		
		SimManager.getInstance().getEdgeOrchestrator().setCloud(datacenter);

		return datacenter;
	}
	
	
	/**
	 * 
	 * @param datacenterElement
	 * @return
	 */
	private List<EdgeHost> createHosts(Element datacenterElement){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more Machines
		List<EdgeHost> hostList = new ArrayList<EdgeHost>();
		
		double costPerBW = Double.parseDouble(datacenterElement.getElementsByTagName("costPerBw").item(0).getTextContent());
		double costPerSec = Double.parseDouble(datacenterElement.getElementsByTagName("costPerSec").item(0).getTextContent());
		
		Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
		//String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
		int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
		double x_pos = Double.parseDouble(location.getElementsByTagName("x_pos").item(0).getTextContent());
		double y_pos = Double.parseDouble(location.getElementsByTagName("y_pos").item(0).getTextContent());
		double altitude = Double.parseDouble(location.getElementsByTagName("altitude").item(0).getTextContent());
		int level =Integer.parseInt(location.getElementsByTagName("level").item(0).getTextContent());
		boolean wap = Boolean.parseBoolean(location.getElementsByTagName("wap").item(0).getTextContent());
		boolean moving = Boolean.parseBoolean(location.getElementsByTagName("moving").item(0).getTextContent());
		double bw = Double.parseDouble(location.getElementsByTagName("bandwidth").item(0).getTextContent());
		double dx = 0.0, dy = 0.0;
		if(moving)
		{
			dx = Double.parseDouble(location.getElementsByTagName("dx").item(0).getTextContent());
			dy = Double.parseDouble(location.getElementsByTagName("dy").item(0).getTextContent());
		}
		//SimSettings.PLACE_TYPES placeType = SimUtils.stringToPlace(attractiveness);

		NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
		for (int j = 0; j < hostNodeList.getLength(); j++) {
			Node hostNode = hostNodeList.item(j);
			
			Element hostElement = (Element) hostNode;
			int numOfCores = Integer.parseInt(hostElement.getElementsByTagName("core").item(0).getTextContent());
			double mips = Double.parseDouble(hostElement.getElementsByTagName("mips").item(0).getTextContent());
			double ram = Double.parseDouble(hostElement.getElementsByTagName("ram").item(0).getTextContent());
			long storage = Long.parseLong(hostElement.getElementsByTagName("storage").item(0).getTextContent());
			long bandwidth = Long.parseLong(location.getElementsByTagName("bandwidth").item(0).getTextContent());
			//long bandwidth = (long) SimSettings.getInstance().getWlanBandwidth() / hostNodeList.getLength();
			
			// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
			//    create a list to store these PEs before creating
			//    a Machine.
			List<Pe> peList = new ArrayList<Pe>();

			// 3. Create PEs and add these into the list.
			//for a quad-core machine, a list of 4 PEs is required:
			for(int i=0; i<numOfCores; i++){
				peList.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
			}
			
			//Make NodeSim object with the input x/y positions and add that to the list of nodes
			NodeSim newNode;
			if(moving)
			{
				newNode = new NodeSim(x_pos, y_pos, altitude, level, wlan_id, wap, moving, new Location(dx, dy));
			}
			else 
			{
				newNode = new NodeSim(x_pos, y_pos, altitude, level, wlan_id, wap);
			}
			newNode.getLocation().setBW(bw);
			nodesForTopography.add(newNode);
			
			
			//4. Create Hosts with its id and list of PEs and add them to the list of machines
			EdgeHost host = new EdgeHost(
					hostIdCounter,
					new RamProvisionerSimple((int) ram),
					new BwProvisionerSimple(bandwidth), //kbps
					storage,
					peList,
					new VmSchedulerSpaceShared(peList),
					costPerBW,
					costPerSec
				);
			Location loc = new Location(wlan_id, x_pos, y_pos, altitude);
			loc.setBW(bw);
			host.setPlace(loc);
			host.setLevel(level);
			hostList.add(host);
			hostIdCounter++;
		}
		

		return hostList;
	}
	
	
	/**
	 * Interpret the clusters into puddles<br>
	 * Create Puddles and populate them with identified cluster members.
	 * added by pFogSim
	 * @param clusters
	 * @return
	 */
	public ArrayList<Puddle> makePuddles(FogHierCluster clusters) {
		FogCluster cluster;
		EdgeHost host;
		ArrayList<EdgeHost> hosts;
		Puddle puddle;
		
		double x, y, a;		
		double staticLatency = Double.MAX_VALUE;
		
		//Create Puddles
		puddles = new Puddle[clusters.getClusters().size()][];//2D array: 1stD is the level, 2ndD is the puddles in that layer
		
		//1. Assign hosts (cluster members) to Puddles (clusters) 
		for (int k = 0; k < clusters.getClusters().size(); k++) {
			//For each fog layer in the system
			
			//Get the collection of clusters configured for the fog layer.
			cluster = clusters.getClusters().get(k);

			// Create an array of Puddles for this fog layer - to reflect the set of clusters configured.
			puddles[k] =  new Puddle[cluster.getCluster().length];
			
			// Populate each Puddle of this layer with EdgeHosts (fog nodes) belonging to this layer.
			for (int i = 0; i < cluster.getCluster().length; i++) {
				
				puddle = new Puddle();
				puddle.setLevel(cluster.getLevel());
				puddle.setPuddleId(i);
				System.out.print("\nFog level: "+cluster.getLevel()+" Puddle Id: "+i+" Hosts: ");
				hosts = new ArrayList<EdgeHost>();
				for (int j = 0; j < cluster.getCluster()[i].length; j++) {//for each host in the puddle
					x = cluster.getCluster()[i][j][LONGITUDE_INDEX];
					y = cluster.getCluster()[i][j][LATITUDE_INDEX];
					a = cluster.getCluster()[i][j][ALTITUDE_INDEX];
					host = findHostByLoc(x, y, a);
					if (host != null) {
						host.setPuddleId(i);
						hosts.add(host);
						System.out.print(host.getId()+" ");
					}
				}// end for j - members of each cluster
				
				puddle.setMembers(hosts);
				puddle.chooseNewHead();
				
				//Assign Parent relationships among Puddles belonging to adjacent fog layers.
				puddle.setParentPuddleId(clusters.parentCluster[k][i]);
				
				//Assign parent and children host nodes from connected parent/child puddles
				puddle.setNodeParentAndChildern();
				
				//Track resources in Puddles
				puddle.updateResources();
				//puddle.updateCapacity();
				
				//Save Puddle configuration
				puddles[k][i] = puddle;
				
			}// end for i - clusters belonging to each fog layer
			
		}// end for k - fog layers

		//Troubleshooting: print sizes of puddles array
		for (int k=0; k<puddles.length; k++) {
			System.out.println("Puddles row index: "+k+" length: "+puddles[k].length);
		}
		
		//Assign Child relationships among Puddles belonging to adjacent fog layers.
		for (int k=0; k<puddles.length; k++) {
			for (int i=0; i<puddles[k].length; i++) {

				// Skip cloud, as it belongs to highest fog layer and does not have a parent.
				if (puddles[k][i].getLevel() == MAX_HAFA_LEVEL)
					continue;
				
				// for each puddle, get its parent info
				int childPudId = puddles[k][i].getPuddleId();
				int parentPudId = puddles[k][i].getParentPuddleId();
				int childLayer = puddles[k][i].getLevel();
				
				System.out.println("Fog level: "+(childLayer+1)+" Parent Puddle Id: "+parentPudId);
				// add itself to the list of children of that parent
				findPuddleById(childLayer+1, parentPudId).getChildPuddleIds().add(childPudId);

			}// end for i
		}// end for k		
		
		
		//Assign Parent-Child relationships among Puddles belonging to adjacent fog layers. - Procedure included above.
/*		double temp;
		int level = 1;
		int index = 0;
		for (int k = 0; k < puds.length - 1; k++) {//for each layer
			for (int i = 0; i < puds[k].length; i++) {//for each puddle in the layer
				for (int j = 0; j < puds[k+1].length; j++) {//search the next layer up for the closest puddle (by latency)
					temp = Router.findRoute(networkTopology, networkTopology.findNode(puds[k][i].getHead().getLocation(), false), networkTopology.findNode(puds[k+1][j].getHead().getLocation(), false));
					///*if you are trying to debug this line shit has gone horribly horribly wrong...
					// *that being said, lets figure out what's going on...
					// *
					// *we need temp to be the static latency between this puddle head and the one we are testing for parentage
					// *to that effect we have Router.findRoute(NetworkTopology network, NodeSim src, NodeSim, dest)
					// *we have the NetworkTopology readily available (YAY!) the others not so much :(
					// *we need to convert the heads of the puddles into NodeSims, easiest way to do that is by location
					// *we can thus take the locations of the puddle heads and find their corresponding NodeSim objects 
					// 
					if (temp < staticLatency) {
						staticLatency = temp;
						level = k + 1;
						index = j;
					}
				}
				puds[k][i].setUp(puds[level][index]);//assign parentage
				staticLatency = Double.MAX_VALUE;//reset this for the next run
			}
		}
		*/
		
		ArrayList<Puddle> results = new ArrayList<Puddle>();
		for (int k = 0; k < puddles.length; k++) {
			for (int i = 0; i < puddles[k].length; i++) {
				results.add(puddles[k][i]);//convert the 2D array to list
			}
		}
		
		return results;
	}
	
	
	/**
	 * @author Shaik
	 * @param pud
	 * @param reqFLevel
	 * @return
	 */
	public ArrayList<EdgeHost> getCousins(Puddle pud, int reqFLevel, int deviceId){
		ArrayList<EdgeHost> cousinHosts = new ArrayList<EdgeHost>();
		ArrayList<Puddle> cousinPuddles = new ArrayList<Puddle>();
		ArrayList<Integer> childPudIds;
		Puddle childPud, parentPud;
		int parentPudId, childPudId, reqPudId, parentLevel;
		
		//System.out.print(" : Getting cousins of Puddle : "+pud.getPuddleId()+" at level: "+pud.getLevel()+" : ");
		
		//Get requesting Puddle Id
		reqPudId = pud.getPuddleId();
		
		// Initialize parentPudId - to identify the first iteration in while loop below.
		parentPudId = -1;
		parentPud = null;
		
		// Move up the Puddle Tree to get the cousins' info
		while (cousinPuddles.size() == 0) {

			if (parentPudId != -1) {
				pud = parentPud;
				//System.out.print(" : Getting cousins of Puddle : "+pud.getPuddleId()+" at level: "+pud.getLevel()+" : ");
			}
			
			// Get parent puddle level
			parentLevel = pud.getLevel()+1;

			//Note: HAFA organization should be a single-rooted tree, not a forest.
			if (parentLevel > MAX_HAFA_LEVEL) {
				System.out.println("Done.");
				//System.out.println("Error. Invalid fog level for parent.");
				return cousinHosts;
			}

			// Get parent puddle
			parentPudId = pud.getParentPuddleId();
			parentPud = findPuddleById(parentLevel, parentPudId);

			// Send message to parent
			SimManager.getInstance().getEdgeOrchestrator().addNumMessages(deviceId, 1);
	
			// Get parent's child puddles 
			// Get sibling puddles only i.e. do not consider current puddle
			childPudIds = parentPud.getChildPuddleIds();
			for (int i=0; i<childPudIds.size(); i++) {
				childPudId = childPudIds.get(i);
				if (childPudId == reqPudId) {
					// Note: This subtree is searched earlier. Ignore this. 
					continue;
				}
				childPud = findPuddleById(parentPud.getLevel()-1, childPudIds.get(i));
				cousinPuddles.add(childPud);
				//System.out.print(" : Cousin added : "+childPud.getPuddleId()+" at level: "+childPud.getLevel()+" : ");		
				//System.out.print(" : cousinPuddles count before : "+cousinPuddles.size());				
			}
		}

		//System.out.print(" : cousinPuddles count : "+cousinPuddles.size());				
	
		// Process each element in cousinPuddles list
		while (cousinPuddles.size() != 0) {

			// Forward message to each cousin Puddle in Puddle subtree
			SimManager.getInstance().getEdgeOrchestrator().addNumMessages(deviceId, 1);

			//System.out.print(" : cousinPuddles count after : "+cousinPuddles.size());				

			// Get first element from list
			Puddle cousinPud = cousinPuddles.get(0);
			//System.out.print(" : Processing Cousin puddle : "+cousinPud.getPuddleId()+" at level: "+cousinPud.getLevel()+" : ");				
			
			// if the puddle belongs to required fog level, add all its members to cousinHosts list.
			if (cousinPud.getLevel() == reqFLevel) {
				cousinHosts.addAll(cousinPud.getMembers());
				
				// This Puddle is being searched for prospective good Host
				SimManager.getInstance().getEdgeOrchestrator().addNumPuddlesSearched(deviceId, 1);
				
				// This Puddle will send response back to requesting PuddleHead.
				SimManager.getInstance().getEdgeOrchestrator().addNumMessages(deviceId, 1);
			}
			else {
			// if not, get all its children puddles and add them to cousinPuddles list for further processing.
				childPudIds = cousinPud.getChildPuddleIds();
				for (int i=0; i<childPudIds.size(); i++) {
					childPud = findPuddleById(cousinPud.getLevel()-1, (int)childPudIds.get(i));
					cousinPuddles.add(childPud);
					//System.out.print(" : Cousin added : "+childPud.getPuddleId()+" at level: "+childPud.getLevel()+" : ");	
				}
			}
			cousinPuddles.remove(0);
		}
		
		// return cousinHosts list		
		return cousinHosts;
	}
	
	
	/**
	 * Returns Puddle object corresponding to given Puddle ID.
	 * @param fLevel
	 * @param pudId
	 * @return
	 */
	public Puddle findPuddleById(int fLevel, int pudId){
		
		// Get the list of all nodes belonging to specified layer
		for (int k=0; k<puddles.length; k++) {
			
			if (puddles[k][0].getLevel() != fLevel)
				continue;
			
			// Identify the Puddle corresponding to given Puddle Id
			for (int i=0; i<puddles[k].length; i++) {
				if (puddles[k][i].getPuddleId() == pudId) 
					return puddles[k][i];
			}		
		}
		return null;
	}
	
	
	/**
	 * Find nearest fog node belonging to given layer, from specified location 
	 * @param fLevel
	 * @param xLoc
	 * @param yLoc
	 * @return
	 * 
	 * @author Shaik
	 */
	public EdgeHost findNearestHostByLayer(int fLevel, Location loc) {
		EdgeHost nearest = null;
		ArrayList<EdgeHost> hostList = new ArrayList<EdgeHost>();
		//Double minDistance = Double.MAX_VALUE; 
		//Double distance;
		
		// Get the list of all nodes belonging to the specified layer
		for (int k=0; k<puddles.length; k++) {
			
			if (puddles[k][0].getLevel() != fLevel)
				continue;
			
			// This kth array of Puddles belong to fLevel.
			// Consolidate the list of Hosts, from each Puddle of this fog layer
			for (int i=0; i<puddles[k].length; i++) {
				hostList.addAll(puddles[k][i].getMembers());
			}
			break;			
		}
		
		//System.out.print("Fog level: "+fLevel+" hostList count: "+hostList.size());
		//DistRadix sort = new DistRadix(hostList, loc);//use radix sort based on distance from task
		BinaryHeap sort = new BinaryHeap(hostList.size(), loc, hostList);
		LinkedList<EdgeHost> nodes = sort.sortNodes();
		//System.out.print("nodes size: " + nodes.size() + "  Device Id: " + mobile.getId() + "  WAP Id: " + mobile.getLocation().getServingWlanId());
		nearest = nodes.poll();
		//System.out.println(" Nearest fog node to mobile device: "+nearest.getId());

		// Return the nearest node
		return nearest;
	}
	
	
	/**
	 * find a given host by location<br>
	 * added by pFogSim
	 * @param double1
	 * @param double2
	 * @return
	 */
	public EdgeHost findHostByLoc(Double x, Double y, Double z) {
		Location match = new Location(x,y,z);

		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			
			if (((EdgeHost) node.getHostList().get(0)).getLocation().equals(match)) {
				return ((EdgeHost) node.getHostList().get(0));
			}
		}
		return findMovingHost(x, y, z);
	}
	
	
	/**
	 * 
	 * @param double1
	 * @param double2
	 * @return
	 */
	public EdgeHost findMovingHost(Double x, Double y, Double z) {
		Location match = new Location(x,y,z);

		for (EdgeHost node : hostList) {
			
			if(node.getLocation().equals(match)) {
				
				return node;
			}
		}
		System.out.println("Error. Host not found for given GPS coordinates."); // Shaik added
		return null;
	}
	
	
	/**
	 * find a given host by id<br>
	 * added by pFogSim
	 * @param id
	 * @return EdgeHost
	 */
	public EdgeHost findHostById(int id) {
		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			if (node.getHostList().get(0).getId() == id) {
				return ((EdgeHost) node.getHostList().get(0));
			}
		}
		return null;
	}
	
	
	/**
	 * find a EdgeHost by it's wlan_id
	 * @author Qian
	 *	@param id
	 *	@return
	 */
	public EdgeHost findHostByWlanId(int id) {
		
		for (Datacenter node: SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			EdgeHost host = (EdgeHost) node.getHostList().get(0);
			
			if (host.getLocation().getServingWlanId()== id) {
				return host;
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param hosts
	 */
	public void setHosts(List<EdgeHost> hosts) {
		hostList.addAll(hosts);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean checkUniqueDC() {
		for (int i = 0; i < localDatacenters.size(); i++) {
			for (int j = 0; j < localDatacenters.size(); j++) {
				if (((EdgeHost) localDatacenters.get(i).getHostList().get(0)).getLocation().equals(((EdgeHost) localDatacenters.get(j).getHostList().get(0)).getLocation())) {
					if (localDatacenters.get(i) != localDatacenters.get(j)) {
						if (((EdgeHost) localDatacenters.get(i).getHostList().get(0)).getLevel() < ((EdgeHost) localDatacenters.get(j).getHostList().get(0)).getLevel()) {
							localDatacenters.remove(i);
						}
						else {
							localDatacenters.remove(j);
						}
					}
				}
			}
		}
		return true;
	}

	
	/**
	 * @return the localDatacenters
	 */
	public List<Datacenter> getLocalDatacenters() {
		return localDatacenters;
	}

	
	/**
	 * @param localDatacenters the localDatacenters to set
	 */
	public void setLocalDatacenters(List<Datacenter> localDatacenters) {
		this.localDatacenters = localDatacenters;
	}

	
	/**
	 * @return the vmList
	 */
	public List<List<EdgeVM>> getVmList() {
		return vmList;
	}

	
	/**
	 * @param vmList the vmList to set
	 */
	public void setVmList(List<List<EdgeVM>> vmList) {
		this.vmList = vmList;
	}

	
	/**
	 * @return the hostList
	 */
	public List<EdgeHost> getHostList() {
		return hostList;
	}

	
	/**
	 * @param hostList the hostList to set
	 */
	public void setHostList(List<EdgeHost> hostList) {
		this.hostList = hostList;
	}

	
	/**
	 * @return the hostIdCounter
	 */
	public int getHostIdCounter() {
		return hostIdCounter;
	}

	
	/**
	 * @param hostIdCounter the hostIdCounter to set
	 */
	public void setHostIdCounter(int hostIdCounter) {
		this.hostIdCounter = hostIdCounter;
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
	 * @return the nodesForTopography
	 */
	public List<NodeSim> getNodesForTopography() {
		return nodesForTopography;
	}

	
	/**
	 * @param nodesForTopography the nodesForTopography to set
	 */
	public void setNodesForTopography(List<NodeSim> nodesForTopography) {
		this.nodesForTopography = nodesForTopography;
	}

	
	/**
	 * @return the linksForTopography
	 */
	public List<Link> getLinksForTopography() {
		return linksForTopography;
	}

	
	/**
	 * @param linksForTopography the linksForTopography to set
	 */
	public void setLinksForTopography(List<Link> linksForTopography) {
		this.linksForTopography = linksForTopography;
	}

	
	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(EdgeServerManager instance) {
		EdgeServerManager.instance = instance;
	}
}// end class EdgeServerManager
