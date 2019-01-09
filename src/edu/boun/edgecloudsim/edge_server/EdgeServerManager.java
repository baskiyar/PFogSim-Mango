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
import edu.boun.edgecloudsim.utils.LinesComponent;
//import edu.boun.edgecloudsim.utils.LinesComponent;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;
import edu.auburn.pFogSim.Puddle.Puddle;
import edu.auburn.pFogSim.clustering.*;
import edu.auburn.pFogSim.netsim.*;
import edu.auburn.pFogSim.orchestrator.CloudOnlyOrchestrator;
import edu.auburn.pFogSim.orchestrator.PuddleOrchestrator;

public class EdgeServerManager {
	private List<Datacenter> localDatacenters;
	private List<List<EdgeVM>> vmList;
	private List<EdgeHost> hostList;// we use this for mobile fog nodes, this is NOT a  list of all edgehosts in the network
	private int hostIdCounter;
	private NetworkTopology networkTopology;
	private static EdgeServerManager instance = null;
	
	//CJ Added these to make the lists of all the nodes and respective links 
	//	to pass to topology constructor
	private List<NodeSim> nodesForTopography = new ArrayList<NodeSim>();
	private List<Link> linksForTopography = new ArrayList<Link>();

	public EdgeServerManager() {
		localDatacenters=new ArrayList<Datacenter>();
		vmList = new ArrayList<List<EdgeVM>>();
		vmList.add(new ArrayList<EdgeVM>());
		hostIdCounter = 1;
		instance = this;
	}
	
	public static EdgeServerManager getInstance()
	{
		return instance;
	}

	public List<List<EdgeVM>> getVMS() {
		return vmList;
	}
	
	public List<EdgeVM> getVmList(int hostId){
		return vmList.get(hostId);
	}
	
	public List<Datacenter> getDatacenterList(){
		return localDatacenters;
	}
	
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
			Location leftCoor = new Location(x_pos1, y_pos1);
			
			NodeList rightLinksList = linkElement.getElementsByTagName("right");
			Node rightLinks = rightLinksList.item(0);
			Element rightLinkss = (Element)rightLinks;
			double x_pos2 = Double.parseDouble(rightLinkss.getElementsByTagName("x_pos").item(0).getTextContent());
			double y_pos2 = Double.parseDouble(rightLinkss.getElementsByTagName("y_pos").item(0).getTextContent());
			Location rightCoor = new Location(x_pos2, y_pos2);
			
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
		if (SimManager.getInstance().getEdgeOrchestrator() instanceof PuddleOrchestrator) {
			SimLogger.print("\n\tMaking Cluster Object...");
			FogHierCluster clusterObject = new FogHierCluster((ArrayList<NodeSim>)nodesForTopography);
			//Sets network topology and uses it to make the Puddle Objects
			networkTopology.setPuddles(makePuddles(clusterObject));
			SimLogger.printLine("Done,");
			/*LinesComponent comp = new LinesComponent();
			comp.drawNetworkTopology(5);
			comp.drawNetworkTopology(4);
			comp.drawNetworkTopology(3);*/

		}
		checkUniqueDC();
	}

	public void createVmList(int brockerId){
		int hostCounter=0;
		int vmCounter=0;
		
		//SimLogger.printLine("createVmList reached");
		
		//Create VMs for each hosts
		Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
		NodeList datacenterList = doc.getElementsByTagName("datacenter");
		for (int i = 0; i < datacenterList.getLength(); i++) {
			Node datacenterNode = datacenterList.item(i);
			Element datacenterElement = (Element) datacenterNode;
			String arch = datacenterElement.getAttribute("arch");
			NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
			for (int j = 0; j < hostNodeList.getLength(); j++) {
				
				vmList.add(hostCounter, new ArrayList<EdgeVM>());
				
				Node hostNode = hostNodeList.item(j);
				Element hostElement = (Element) hostNode;
				NodeList vmNodeList = hostElement.getElementsByTagName("VM");
				for (int k = 0; k < vmNodeList.getLength(); k++) {
					Node vmNode = vmNodeList.item(k);					
					Element vmElement = (Element) vmNode;

					String vmm = vmElement.getAttribute("vmm");
					
					int numOfCores = Integer.parseInt(vmElement.getElementsByTagName("core").item(0).getTextContent());
					double mips = Double.parseDouble(vmElement.getElementsByTagName("mips").item(0).getTextContent());
					double ram = Double.parseDouble(vmElement.getElementsByTagName("ram").item(0).getTextContent());
					long storage = Long.parseLong(vmElement.getElementsByTagName("storage").item(0).getTextContent());
					long bandwidth = SimSettings.getInstance().getWlanBandwidth() / (hostNodeList.getLength()+vmNodeList.getLength());
					
					
					//VM Parameters		
					EdgeVM vm = new EdgeVM(vmCounter, brockerId, mips, numOfCores, (int) ram, bandwidth, storage, vmm, new CloudletSchedulerTimeShared());
					vm.setVmType(SimSettings.VM_TYPES.EDGE_VM);
					vm.setArch(arch);
					vmList.get(hostCounter).add(vm);
					vmCounter++;
				}

				hostCounter++;
			}
		}
	}
	
	public void terminateDatacenters(){
		for (Datacenter datacenter : localDatacenters) {
			datacenter.shutdownEntity();
		}
	}

	//average utilization of all VMs
	public double getAvgUtilization(){
		double totalUtilization = 0;
		double vmCounter = 0;
		
		// for each datacenter...
		for(int i= 0; i<localDatacenters.size(); i++) {
			List<? extends Host> list = localDatacenters.get(i).getHostList();
			// for each host...
			for (int j=0; j < list.size(); j++) {
				Host host = list.get(j);
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
			long bandwidth = (long) SimSettings.getInstance().getWlanBandwidth() / hostNodeList.getLength();
			
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
				newNode = new NodeSim(x_pos, y_pos, level, wlan_id, wap, moving, new Location(dx, dy));
			}
			else 
			{
				newNode = new NodeSim(x_pos, y_pos, level, wlan_id, wap);
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
			Location loc = new Location(wlan_id, x_pos, y_pos);
			loc.setBW(bw);
			host.setPlace(loc);
			host.setLevel(level);
			hostList.add(host);
			hostIdCounter++;
		}
		

		return hostList;
	}
	/**
	 * interpret the clusters into puddles<br>
	 * added by pFogSim
	 * @param clusters
	 * @return
	 */
	public ArrayList<Puddle> makePuddles(FogHierCluster clusters) {
		EdgeHost host;
		double x, y;
		Puddle puddle;
		FogCluster cluster;
		ArrayList<EdgeHost> hosts;
		double staticLatency = Double.MAX_VALUE;
		Puddle[][] puds = new Puddle[clusters.getClusters().size()][];//2D array: 1stD is the level, 2ndD is the puddles in that layer
		for (int k = 0; k < clusters.getClusters().size(); k++) {//for each layer in the system
			cluster = clusters.getClusters().get(k);//extract the layer
			puds[k] =  new Puddle[cluster.getCluster().length];//set the list of puddles for that layer
			for (int i = 0; i < cluster.getCluster().length; i++) {//for each puddle in the layer
				puddle = new Puddle();
				puddle.setLevel(k + 1);
				hosts = new ArrayList<EdgeHost>();
				for (int j = 0; j < cluster.getCluster()[i].length; j++) {//for each host in the puddle
					x = cluster.getCluster()[i][j][0];
					y = cluster.getCluster()[i][j][1];
					host = findHostByLoc(x, y);
					if (host != null) {
						hosts.add(host);
					}
				}
				puddle.setMembers(hosts);
				puddle.chooseNewHead();
				puddle.updateResources();
				//puddle.updateCapacity();
				puds[k][i] = puddle;
			}
		}
		//now we need to set the proper parent-child relationships
		double temp;
		int level = 1;
		int index = 0;
		for (int k = 0; k < puds.length - 1; k++) {//for each layer
			for (int i = 0; i < puds[k].length; i++) {//for each puddle in the layer
				for (int j = 0; j < puds[k+1].length; j++) {//search the next layer up for the closest puddle (by latency)
					temp = Router.findRoute(networkTopology, networkTopology.findNode(puds[k][i].getHead().getLocation(), false), networkTopology.findNode(puds[k+1][j].getHead().getLocation(), false));
					/*if you are trying to debug this line shit has gone horribly horribly wrong...
					 *that being said, lets figure out what's going on...
					 *
					 *we need temp to be the static latency between this puddle head and the one we are testing for parentage
					 *to that effect we have Router.findRoute(NetworkTopology network, NodeSim src, NodeSim, dest)
					 *we have the NetworkTopology readily available (YAY!) the others not so much :(
					 *we need to convert the heads of the puddles into NodeSims, easiest way to do that is by location
					 *we can thus take the locations of the puddle heads and find their corresponding NodeSim objects 
					 */
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
		ArrayList<Puddle> results = new ArrayList<Puddle>();
		for (int k = 0; k < puds.length; k++) {
			for (int i = 0; i < puds[k].length; i++) {
				results.add(puds[k][i]);//convert the 2D array to list
			}
		}
		
		//Qian added for puddle id and set parent and children for each node
		for (int i = 0; i < results.size(); i++) {
			results.get(i).setNodeParentAndChildern();
			for (EdgeHost tempHost: results.get(i).getMembers()) {
				tempHost.setPuddleId(i);
			}
		}
		return results;
	}
	/**
	 * find a given host by location<br>
	 * added by pFogSim
	 * @param double1
	 * @param double2
	 * @return
	 */
	public EdgeHost findHostByLoc(Double double1, Double double2) {
		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			if (((EdgeHost) node.getHostList().get(0)).getLocation().getXPos() == double1 && ((EdgeHost) node.getHostList().get(0)).getLocation().getYPos() == double2) {
				return ((EdgeHost) node.getHostList().get(0));
			}
		}
		return findMovingHost(double1, double2);
	}
	
	public EdgeHost findMovingHost(Double double1, Double double2) {
		for (EdgeHost node : hostList) {
			if(node.getLocation().getXPos() == double1 && node.getLocation().getYPos() == double2) {
				return node;
			}
		}
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
	
	public void setHosts(List<EdgeHost> hosts) {
		hostList.addAll(hosts);
	}
	
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
}
