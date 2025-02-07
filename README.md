# pFogSim : A Simulator For Evaluating Dynamic and Layered Fog-Computing Environments
Last Edit by Chengyu Tang on 2023-03-12

## PFogSim-v3 -- Changes so far: 

Java:
- Output folders are automatically created if they don't already exist.
- Dijkstra's shortest paths for network nodes are saved when calculated and looked up when needed, rather than being recalculated each time a single path is requested. This removes a massive calculation load from each initialization.
- Configurable settings have been moved to the "default_config.properties" file.
- Contents of output folders are no longer deleted each time the simulator is run. Instead, a new subfolder is created to store the new results.

MATLAB:
- Fixed numerous bugs in plotGenericResult module.
- Added configuration class with descriptively named properties to replace calls to getConfiguration(#) function.
- Added validation to configuration properties.
- Added autoConfig method to configuration class to automatically detect appropriate settings for plotting based on names of files present in sim_results folder.
- Added plotAllPlots function to easily generate all available plots for all available simulation data with a single button click.
- The plotGenericResult function's return value is now the figure it creates.
- The plotAllPlots function now stores all generated plots as individual pages in a single PDF file.

## **What is pFogSim?**

 - pFogSim (/p/-fôg-/sɪm/) is a play off iFogSim (another popular simulator built on CloudSim)
	- **p** is for **P**uddles, the HAFA (Hierarchical Autonomous Fog Architecture) representation of Fog Networks found here (insert link for paper when it comes out)
	- **Fog** is from i**Fog**Sim ([found here](https://github.com/Cloudslab/iFogSim)) since it provided a lot of inspiration for this project
	- **Sim** is from EdgeCloud**Sim** ([found here](https://github.com/CagataySonmez/EdgeCloudSim)) since it provides a significant back-bone to make the project off of
	- All of these are from the popular CloudSim ([found here](https://github.com/Cloudslab/cloudsim))
 - A simulator made to handle large-scale FOG networks with the HAFA Puddle Strategy to help evaluate the potential advantages/disadvantages within user-customizable scenarios
 - Simulator is still in progress but what is seen here should already be present and tested in the simulator

# Project Participants
## Faculty supervisor

Dr. Sanjeev Baskiyar <br />
Professor <br />
Department of Computer Science and Software Engineering<br />
Samuel Ginn College of Engineering<br />
Auburn University<br />
Auburn, AL 36849<br />
baskisa@auburn.edu

This research was supported by NSF award OAC 1659845.

## Student Contributors (at Auburn University)
1. Shehenaz Shaik  		(AU Graduate Research Assistant)
2. Jacob Hall     		(REU Participant)
3. Clayton Johnson 		(REU Participant)
4. Qian Wang        		(AU Graduate Student)
5. Craigory Coppolla 		(REU Participant)
6. Jordon Cox			(REU Participant)
7. Matthew Merck		(REU Participant)
8. Cameron Berry		(REU Participant)
9. Roy Harmon			(AU Graduate Student)
10. Chengyu Tang		(AU Graduate Student)
11. Mohammad Zuaiter	(AU Graduate Student)

Prior effort on development of simulator for fog computing environment by extending iFogSim was contributed by following members. That project is incomplete and has been abandoned. 

1. Shehenaz Shaik(AU Graduate Research Assistant)
2. Jessica Knezha(REU Participant)
3. Avraham Rynderman(REU Participant)
4. William McCarthy(REU Participant)
5. Denver Strong(REU Participant)


## **Quick Summary**
 - General Outline of Classes
![Class Diagram](https://github.com/AgentEnder/pFogSim/blob/master/Class_Interactions.jpg)
 - This may not appear to be straight-forward, however it will make more sense down below

# **Usage**

## Requirements
- Java 1.8. Other Java versions are not tested.
- MATLAB >= 2021b.

## How to Run
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
		- ~~pFogSim/sim_results/ite(#) directory should exist at time of run (or run with appropriate permissions)~~
		- ~~pFogSim/sim_results/consoleruns directory should also exist.~~ (directories will be automatically created)
## Creating Custom Scenarios
 - Customizable Files: 
 	- Change DataInterpreter to fit data sets into XML formats
	- All files that may need to be customized if desired:
		- node_test.xml, link_test.xml
		- [config files](https://github.com/jihall77/pFogSim/tree/master/scripts/sample_application/config)
		- Everything will run appropriately

## Visualization
Simulation results can be visulized using MATLAB scripts in the [`scripts/Analysis/matlab/`](https://github.com/baskiyar/PFogSim-Mango/tree/master/scripts/Analysis/matlab) directory. It is recommended to use `plotALLPlots.m` to create multiple plots at once. To use this script, change the `config.FolderPath` variable to where the output files are stored or put all the output files into the same directory as the script. Comment and uncomment the corresponding lines according to your needs. It will scan the whole directory and use all `*_GENERIC.log` files to makes the plots. All the plots will be put into one PDF file named `{date}_{time}.pdf` stored in the previously provided directory (`pwd` by default). The file [`doc/available_data_from_output_files`](https://github.com/baskiyar/PFogSim-Mango/tree/master/doc/available_data_from_output_files) provides interpretations of the output files.

# **General Outline + Comments**: 
There are a ton of function calls not mentioned here that are necessary for the simulator to function, however are unnecessary to discuss in the context of the simulator as a whole.
In honor of proper coding etiquette:
```
less is more
```
And with that said, here is everything on pFogSim. Most of the follow can be gathered from the code and attempts have been made to consistently document files, however documentation appears to be inadequate when using other simulators. If there needs to be further documentation, please let us know and it will be added quickly.

### **The Flow**

#### [DataInterpreter](#datainterpreter) → [EdgeServerManager](#edgeservermanager) → [GPSVectorMobility](#GPSVectorMobility) → [NetworkTopology](#networktopology) → [SimManager](#simmanager) → [SimLogger](#simlogger)

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
	- All device positions are then updated for the next moment in time for the simulator and WAPs are updated to connect to the closest nearby.
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