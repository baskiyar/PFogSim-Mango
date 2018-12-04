# pFogSim : A Simulator For Evaluating Dynamic and Layered Fog-Computing Environments
Last Edit by Clayton Johnson on 7/17/2018

## **What is pFogSim?**

 - pFogSim (/p/-fôg-/sɪm/) is a play off iFogSim (another popular simulator built on CloudSim)
	- **p** is for **P**uddles, the HAFA (Hierarchical Autonomous Fog Architecture) representation of Fog Networks found here (insert link for paper when it comes out)
	- **Fog** is from i**Fog**Sim ([found here](https://github.com/Cloudslab/iFogSim)) since it provided a lot of inspiration for this project
	- **Sim** is from EdgeCloud**Sim** ([found here](https://github.com/CagataySonmez/EdgeCloudSim)) since it provides a significant back-bone to make the project off of
	- All of these are from the popular CloudSim ([found here](https://github.com/Cloudslab/cloudsim))
 - A simulator made to handle large-scale FOG networks with the HAFA Puddle Strategy to help evaluate the potential advantages/disadvantages within user-customizable scenarios
 - Simulator is still in progress but what is seen here should already be present and tested in the simulator

## **Quick Summary**
 - General Outline of Classes
![Class Diagram](https://github.com/jihall77/pFogSim/blob/master/class_diagram.png)
 - This may not appear to be straight-forward, however it will make more sense down below

## **How to Run** 
(May have to change some of the files mentioned to tailor for your stuff)
 - Two ways: 
	- [Scripts](https://github.com/jihall77/pFogSim/tree/master/scripts/sample_application) (Not too different from EdgeCloudSim)
		- May require changes in bash scripts to point at your desired files
		- Compile with compile.sh
		- Run single scenario with scenario_runner.sh
		- Run multiple scenarios with run_scenarios.sh
		```
		./run_scenarios.sh 1 1
		```
		- The numbers passed into the script are the number of cores and iterations desired, respectively
		- When these are run, they will create directories inside of the output file in the linked directory. A sample run may look like the following:
		```
		~$ ./run_scenarios 1 1
		~$ tail -f /pFogSim/scripts/sample_application/output/11-07-2018_11-52/default_config/ite1.log
		Simulation setting file, output folder and iteration number are not provided! Using default ones...
		Simulation started at 11/07/2018 11:52:21
		----------------------------------------------------------------------
		Scenario started at 11/07/2018 11:52:21
		Scenario: PUDDLE_ORCHESTRATOR - Policy: NEXT_FIT - #iteration: 1
		Duration: 0.5 hour(s) - Poisson: 5.0 - #devices: 500
		CloudSim.init reached
		ScenarioFactory reached
		Creating tasks...Done, 
		SimManager reached
		SimManager is starting...
		```
	- [mainApp.java](https://github.com/jihall77/pFogSim/tree/master/src/edu/boun/edgecloudsim/sample_application) (IDEs, we used Eclipse but any will work)
		- Will output to console
		- It is the same file given by EdgeCloudSim with some additions
		- pFogSim/sim_results/ite(#) directory should exist at time of run (or run with appropriate permissions)
## **Creating Custom Scenarios**
 - Customizable Files: 
 	- Change DataInterpreter to fit data sets into XML formats
	- All files that may need to be customized if desired:
		- node_test.xml, link_test.xml
		- [config files](https://github.com/jihall77/pFogSim/tree/master/scripts/sample_application/config)
		- Everything will run appropriately
## **General Outline + Comments**: 
There are a ton of function calls not mentioned here that are necessary for the simulator to function, however are unnecessary to discuss in the context of the simulator as a whole.
In honor of proper coding etiquette:
```
less is more
```
And with that said, here is everything on pFogSim. Most of the follow can be gathered from the code and attempts have been made to consistently document files, however documentation appears to be inadequate when using other simulators. If there needs to be further documentation, please let us know and it will be added quickly.

### **The Flow**

#### [DataInterpreter](#datainterpreter) → [EdgeServerManager](#edgeservermanager) → [GPSVectorMobility](#GPSVectorMobility) → [NetworkTopology](#networktopology) → [Clustering](#clustering) → [Puddles](#puddles) → [SimManager](#simmanager) → [SimLogger](#simlogger)

#### Note: When I say mobile devices, I mean mobile devices, sensors, actuators; anything that is on the lowest level of the network and is interacting with the network.
### DataInterpreter:
 - Hard-coded
 - Made to take CSV files from City of Chicago and translate to XML 
 - Need to change if want to make any large files
 - Defines the MIN/MAX space of simulation (So mobile devices don't leave the simulation space)
 - Customizable adapter for anything you want
 - [More Below](#dataintepreter-details)
  
### EdgeServerManager:
 - Reads links and nodes XML files -> Creates respective objects
 - Constructs network topology 
 - [More Below](#edgeservermanager-details)
 
### GPSVectorMobility:
 - Creates each mobile device starting at a random wireless access point (WAP)
 - Moves them according to random vectors that have been approximated to be around walking speed of 5km/h
 - Creates all of the mobile devices and all of their positions throughout the entire simulator. 
 - Also updates which WAP connected to based on proximity
 - [More Below](#GPSVectorMobility-details)

### NetworkTopology:
 - Defines network and has all static links in network
 - Links don't actually have to be static
 - FogNodes may move
 	- FogNodes may be removed
	- All will update in SimManager
 - Lets clustering be created
 - [More Below](#networktopology-details)
 
### Clustering:
 - Goes through each level and clusters local nodes together to allow for local nodes to share puddles
 - Hierarchical Clustering Algorithm
 - Creates ability for Puddles
 - [More Below](#clustering-details)
 
### Puddles:
 - Takes local puddles and attaches pieces to each other
 - [More Below](#puddles-details)
 
### SimManager: 
 - Creates all the tasks
 - Schedules the tasks that are waiting
 - Can update network topology -> clustering -> puddles if changes occur
	- Moving FogNodes or FogNode removal can be implemented here
 - Schedules end of simulation -> Lots -> SimLogger
 - [More Below](#simmanager-details)
 
### SimLogger: 
 - Prints all of the information to output files/console
 - Info gets stored here throughout simulation and gets executed here
 - [More Below](#simlogger-details)

## **Classes in Detail**:

### DataIntepreter Details:
 - We've separated the DataInterpreter from the rest of the code primarily to clean it up a little and allow for easier debugging
#### Elements:
 - int MAX_LEVELS : The highest number of levels being made in this network
 - String[] files : List of all files being imported to create levels
 - String[][] nodeSpecs : Array for typical FogNode specifications on each level
 	- It's assumed that an organization setting this network up would desire as much homogeneity within the network for simplicity reasons
 - ArrayList<Double[]> nodeList : When importing levels, this will hold all the nodes from the previous list (Or the list of nodes the imported list should connect to)
 - ArrayList<Double[]> tempList : When importing levels, this will be used to store the current list of nodes such that no level is lost. This is later copied over to nodeList if that is what our topology dictates
 - double MIN/MAX_LAT/LONG : Hold the boundary restrictions for the simulation space. This is updated to contain all of the nodes added such that none exceed the boundary of the simulated city
 - boolean universitiesYet : Completely for the base test built-in. Used to see if universities have been imported yet (This will make more sense later, I swear)
 - boolean universitiesLinked : Essentially serves the same purpose as the aforementioned universitiesYet but is used for determining whether universities have been linked into the network yet.
 - File, FileWriter, BufferWriterxmlFile/FW/BR : Importing files, (link to Java page with these)
#### Methods:
 - double measure : Takes pair of lat/long coordinates and determines distance between them using Haversine Formula. This is due to the fact that we aren't using rectangular coordinates to define the surface of a spherical object like the Earth.
 - void readFile() : Throws IO Exception since we are reading and writing files
 	- Variables:
		- PrintWriter node : the XML-converted file containing all nodes and respective information
		- PrintWriter links : the XML-converted file containing all links and respective information
		- String[] nodeLoc + Double[] temp : Temporary variables to hold input line containing information (Note that we don't care about the first value since they are rather arbitrary and will have no meaning in the simulator. For this, it has been replaced with **counter**.
		- int counter : Keeps track of how many FogNodes have been created and also functions as an ID for all nodes.
	- Functionality:
		- The levels in *files* are read from the Cloud (Level #6) down to the sensor/actuator/mobile device level (Level #0). This seeks to minimize the amount of information we must keep track of while importing additional files.
		- Each device at specified locations (imported from our files) is added to the *node* XML file with its respective level hardware specifications.
		- Each device is then **linked** with the closest device on the level above it using nodeList. In the case of Universities, they connect to the two closest Universities as well. All devices below Universities will also connect to the closest University as their primary connection to the network.
		- Once everything is run, you should have MIN/MAXES determined (these will be used by the MobilityModel later on) and the input files for the simulator are ready.
 - void initialize() : Initializes all the hardware specifications for the network. This is fairly network-specific and should be changed when one is creating their own simulations to test.
 ---
 
### EdgeServerManager Details:
 - EdgeServerManager should be fairly straight-forward in that if there is an error showing up here, it is most likely having to do with errors in the input files.
 #### What it does when it imports...:
  - Nodes:
  	- Creates DataCenters and Hosts for EdgeCloudSim/CloudSim to work with later. 
	- Creates NodeSim objects and collects them into a list (*nodesForTopography*)
		- Construction of NodeSim objects varies slightly based on certain attributes the FogNodes have.
  - Links:
  	- Creates Link objects and adds them to a list (*linksForTopography*)
  - Cluster:
  	- *nodesForTopography* is passed to FogHierCluster to create the clusters for each level which is passed to...
  - Puddles: 
  	- Takes in Cluster object and creates the HAFA Puddles
---

### GPSVectorMobility Details:
 - GPSVectorMobility is a little strange but is in charge of all the mobile devices. This one deserves significant detail.
 #### Elements:
  - List<TreeMap<Double, Location>> treeMapArray : Contains all of the positions and information for all mobile devices over the entire time duration of the simulator. This is accessed outside through other methods for the rest of the simulator to have access to.
  - MIN/MAX_LAT/LONG : Information passed from DataInterpreter to SimSettings and finally to here where it creates the boundaries for the mobile devices. 
  - NetworkTopology network : Holds the network topology and gives needed information on the system to the methods.
 #### Methods:
  - void initialize() : Essentially does everything, all other methods are to be "getters". 
  	- A list of access points is created for the system to keep track of which nodes can connect wirelessly with the mobile devices.
		- This is insanely helpful when the network grows with few access points.
	- All mobile devices are given random starting wireless access points with which they are connected.
	- All devices are given random vectors that change for every entry into the large array.
	- All device positions are then updated for the next moment in time for the simulator and WAPs are updated to connect to the closest nearby. Voronoi implementations should exist here to follow along with the HAFA structure however there have been issues with our implementation of the code. (Insert link here for Voronoi stuffs)
 - Quick note on the treeMapArray:
 	- This structure was made to hold all of the positions of all the mobile devices for all time *t* in the simulator.
	- Let's take a look at a specific line of code in the initialization function:
	```
	treeMap.put(treeMap.lastKey()+1, new Location(wlan_id, x_pos + right, y_pos + up));
	```
	- where wlan_id, x_pos + right, y_pos + up are the values to be entered for the next section.
	- This section of code is a bit strange and here's why:
		- treeMap.lastKey()+1 is to serve as the index in which we put the new Location stuff. How this works is placement within this map is relative to the previous time. What will happen in this line is that there will be a new updated position for these mobile devices for every second in the simulation. 
		- Ex. A simulation runs for 1hr => 60mins => 3600sec so treeMap.size() = 3600 when all is said and done.
		- This is helpful because now we have some frame of reference to make our *right/up* vectors. We can change either *right* and *up* or the *t* in treeMap.lastKey()+*t* to change how large the magnitude of the mobility vector is.
---		
		
### NetworkTopology Details:
 - iFogSim-esque Network Topology is used to help construct the Puddles and allow for a more-realistic simulation of real-life networks.
 #### Elements:
  - HashSet<Link> links : All of the Link objects in the network.
  - HashSet<NodeSim> nodes : All of the NodeSim objects in the network.
  - HashSet<NodeSim> mobileNodes : All of the NodeSim objects in the network that have the ability to move.
  - TreeSet<Location> coords : All of the locations in the network where there is an object.
  - ArrayList<Puddle> pond : A cute name for a grouping of Puddles in the network.
	
 #### Methods:
  - boolean validateTopology() : Ensures there are no dangling references (links with only one connection) or islands (nodes with no connections to the rest of network)
  - NodeSim findNode() : Locates the closest node to the given location. One can request specifically a wireless access point if desired.
---	
	
### Clustering Details:
 - This code was ported over from other work done at Auburn regarding HAFA Puddles and thus was plugged in minimally. 
 
 #### Order of Operations:
  - *FogHierCluster* object is made in *EdgeServerManager* and is passed the list of all nodes within the network. 
  - *FogHierCluster* creates a *FogCluster object* and passes it each layer of nodes. Clusters are made at each level based on their proximity to other nodes. The ending result for each layer is a bunch of connected nodes that are group in accordance to their location. Each cluster has a varying number of nodes within it, but this is expected because if there were constraints some strange and unintuitive designs may arise.
  - This section of the code is in need of cleaning to make sure there are no vestigial files laying around.
---  
  
### Puddles Details:
 - The simulator is meant to be tested with the HAFA Puddle Structure proposed by Sanjeev Baskiyar and Shehenaz Shaik at Auburn University. The simulator is made to test the usability of Puddles in these Fog Architectures, however may be extended to various other task-placement strategies such as Cloud-only or Edge-only (both of which are also built into this simulator). 
 - The following image should demonstrate how Puddles are supposed to work:
 ![Puddle Diagram](https://github.com/jihall77/pFogSim/blob/master/puddlelayout.jpg)
 - All of these Puddles and groups are logical units and don't require a change in the physical network. The overall goal of Puddles in the Fog Architecture is to shorten the physcial distance a task must travel to be executed. This is especially during execution of latency-sensative applications.
 #### Structure:
  - Each Puddle has a Puddle Head (denoted PH in the image) which keeps track of the local resource information. Every Puddle has one link going upwards in the network, or moving closer to the Cloud, and at least one link downward, closer to the mobile devices/users. This obviously excludes the Cloud (as it is the analogous to the root of a tree) and the bottom-level nodes (which are at the edge of the tree). Additionally, the Puddles may connect with other local Puddles sharing the same parent, allowing for improved service migration that maintains execution of tasks a short distance away from the user.
  ###### Definitely Read the Paper introducing this idea if you want to understand this idea in depth linked here (insert link)
--- 
 
### SimManager Details:
 - SimManager is essentially the 2nd main function in this program since it is the central command center for just about everything in this program.
 #### Order of Operations:
  - When constructed, SimManager creates the loadGeneratorModel and scenarioFactory (both of which are essential to create the simulation as per design).
  - When the *startSimulation* method is called, all of these values are ordered to initialize all of their respective parts and fully build the simulation. This later passes the order to *CloudSim* to start as well.
  - Once everything is up and running, the simulation's task scheduling is in charge. In order for something to occur in the simulation, tasks must be submitted to CloudSim where they will be either processed or passed to other sections of the simulator.
  - One section of the code that is seen frequently is the following section:
  ```
  //If the scheduled task has an id of PRINT_PROGRESS
  case PRINT_PROGRESS:
	//Updates the positions of FOG Devices if necessary
	HashSet<Link> links = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().getLinks();
	Set<NodeSim> nodes = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().getMobileNodes();
				
	ArrayList<Link> newLinks = new ArrayList<Link>();
	ArrayList<NodeSim> newNodes = new ArrayList<NodeSim>();
	for(NodeSim node : nodes) {
		//Update positions
		Location currentLoc = node.getLocation();
		if(currentLoc.getXPos() + node.getVector().getXPos() > MAX_WIDTH) 
			node.setVector(new Location(node.getVector().getXPos() * -1, node.getVector().getYPos()));
		if(currentLoc.getYPos() + node.getVector().getYPos() > MAX_HEIGHT) 
			node.setVector(new Location(node.getVector().getXPos(), node.getVector().getYPos() * -1));
			
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
		}
		node.setLocation(new Location(currentLoc.getXPos() + node.getVector().getXPos(), currentLoc.getYPos() + node.getVector().getYPos()));
	}
	((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology().setMobileNode(nodes);
	//Goes through all devices and checks to see if WAP ids have changed
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
  ```
  - This section of the code has two purposes: migrating services in the event of users/mobile devices relocating during the time of the simulation and printing the progress of the simulator to the output file (or console) to improve usability and user experience.
  - The first section checks if any mobile devices have changed WAPs and, if so, moves the location of the task being executed for said device. Upon migration, data transfer cost and bandwidth is accounted for to perform as realistically as possible.
  	- This service migration is meant to be similar to changing access points on a mobile device such as a phone. If phone-service data is used to download a webpage that requires consistent data input/output such as streaming, when this device changes to a Wi-Fi connection in the middle of the stream, the network will change the packet routes to account for the transfer. This service migration is an essential step in accounting for realistic networks in which heavy transfer rates combined with heavy data transfers are necessary. 
	- Service migration would most likely benefit from working on an id-based network instead of that of a location-based due to various complications that may arise. However, considering that having multiple wireless access points within a space of less than 10cm would be strange and potentially degrading to the signal strength due to excess noise in the system, there is little to no need to change over to id-based until this issue is arisen.
---

### SimLogger Details:
 - SimLogger accumulates details on the simulator and how it is running throughout execution. Many of the variables are output only once per simulation when *simStopped* is invoked at the end. The metrics here may easily be changed as that all of the information needed is made available in SimLogger.
 - This section of the code is fairly straight-forward and exists purely to output information to the output files and console.
