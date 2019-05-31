/*
 * Title:        EdgeCloudSim - Simulation Settings class
 * 
 * Description: 
 * SimSettings provides system wide simulation settings. It is a
 * singleton class and provides all necessary information to other modules.
 * If you need to use another simulation setting variable in your
 * config file, add related getter methot in this class.
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.utils.SimLogger;

//import edu.auburn.pFogSim.netsim.*;


/**
 * 
 * @author szs0117
 *
 */
public class SimSettings {
	private static SimSettings instance = null;
	private Document edgeDevicesDoc = null;
	private Document linksDoc = null;
	
	//enumerations for the VM, application, and place.
	//if you want to add different types on your config file,
	//you may modify current types or add new types here. 
	public static enum VM_TYPES { EDGE_VM, CLOUD_VM }
	public static enum APP_TYPES { AUGMENTED_REALITY, HEALTH_APP, HEAVY_COMP_APP, INFOTAINMENT_APP, COGNITIVE_ASSISTANCE, REMOTE_HEALTHCARE, MACHINE_LEARNING }
	public static enum PLACE_TYPES { ATTRACTIVENESS_L1, ATTRACTIVENESS_L2, ATTRACTIVENESS_L3 }
	public static enum CLOUD_TRANSFER { IGNORE, CLOUD_UPLOAD, CLOUD_DOWNLOAD }
	
	//predifined IDs for cloud components.
	public static int CLOUD_DATACENTER_ID = 1000000; // Shaik modified - prior value 1000
	public static int CLOUD_HOST_ID = CLOUD_DATACENTER_ID + 1;
	public static int CLOUD_VM_ID = CLOUD_DATACENTER_ID + 2;
	
	//predifined IDs for edge devices
	public static int EDGE_ORCHESTRATOR_ID = 2000000;  // Shaik modified - prior value 2000
	public static int GENERIC_EDGE_DEVICE_ID = EDGE_ORCHESTRATOR_ID + 1;
	
	//generic ID for mobile device
	public static int MOBILE_DEVICE_ID = 3000000;   // Shaik modified - prior value 3000

	public static double ROUTER_PROCESSING_DELAY = 0.020; // Assumed 20 millisec constant delay per network hop - modify as appropriate for other test environments.
	public static double MAX_NODE_MIPS_UTIL_ALLOWED = 1.0; // Maximum allowed node mips utilization before the requests spill over to a different fog node.
	
	//delimiter for output file.
	public static String DELIMITER = ";";
	
    private double SIMULATION_TIME; //minutes unit in properties file
    private double WARM_UP_PERIOD; //minutes unit in properties file
    private double INTERVAL_TO_GET_VM_LOAD_LOG; //minutes unit in properties file
    private double INTERVAL_TO_GET_VM_LOCATION_LOG; //minutes unit in properties file
    private boolean FILE_LOG_ENABLED; //boolean to check file logging option
    private boolean DEEP_FILE_LOG_ENABLED; //boolean to check deep file logging option
    
    //Qian enable the trace
    private boolean TRACE_ENABLED;
    
    //Qian added for select how to make puddle cluster. true - distance; false - latency
    private boolean clusterType = true; 
    
    //Qian added for service replacement
    private boolean serviceReplacement = false;

    private int MIN_NUM_OF_MOBILE_DEVICES;
    private int MAX_NUM_OF_MOBILE_DEVICES;
    private int MOBILE_DEVICE_COUNTER_SIZE;
    
    private int NUM_OF_EDGE_DATACENTERS;
    private int NUM_OF_EDGE_HOSTS;
    private int NUM_OF_EDGE_VMS;
    
    private double WAN_PROPOGATION_DELAY; //seconds unit in properties file
    private double LAN_INTERNAL_DELAY; //seconds unit in properties file
    private int BANDWITH_WLAN; //Mbps unit in properties file
    private int BANDWITH_WAN; //Mbps unit in properties file
    private int BANDWITH_GSM; //Mbps unit in properties file

    private int MIPS_FOR_CLOUD; //MIPS
    
    //Qian selected nodes
    private String[] SELECTED_NODES;
    private int[] selectedHostIds; // Shaik added
	private int currentSelection = 0;
    
    //Qian added for selected Level
    private String[] SELECTED_LEVELS;
    private int[] selectedLayerIds; // Shaik added

    private int currentLevelIndex = 0;
    
    private String[] SIMULATION_SCENARIOS;
    private String[] ORCHESTRATOR_POLICIES;
    
    // mean waiting time (minute) is stored for each place types
    private double[] mobilityLookUpTable;
    
    private double[] simulationSpace;
    
    // following values are stored for each applications defined in applications.xml
    // [00] usage percentage (%)
    // [01] prob. of selecting cloud (%)
    // [02] poisson mean (sec)
    // [03] active period (sec)
    // [04] idle period (sec)
    // [05] avg data upload (KB)
    // [06] avg data download (KB)
    // [07] avg task length (MI) //Note: (KI), not MI - updated by Shaik
    // [08] required # of cores
    // [09] vm utilization (%)
    // [10] max latency (milliseconds)
    private double[][] taskLookUpTable = new double[APP_TYPES.values().length][11];
    private int MAX_LEVELS;
    private String inputType;
    private boolean mobileDevicesMoving;
    
    
    /**
     * 
     */
	private SimSettings() {
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static SimSettings getInstance() {
		if(instance == null) {
			instance = new SimSettings();
		}
		return instance;
	}
	
	
	/**
	 * Reads configuration file and stores information to local variables
	 * @param propertiesFile
	 * @return
	 */
	public boolean initialize(String propertiesFile, String edgeDevicesFile, String applicationsFile, String linksFile){
		boolean result = false;
		InputStream input = null;
		try {
			input = new FileInputStream(propertiesFile);

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			SIMULATION_TIME = (double)60 * Double.parseDouble(prop.getProperty("simulation_time")); //seconds
			WARM_UP_PERIOD = (double)60 * Double.parseDouble(prop.getProperty("warm_up_period")); //seconds
			INTERVAL_TO_GET_VM_LOAD_LOG = (double)60 * Double.parseDouble(prop.getProperty("vm_load_check_interval")); //seconds
			INTERVAL_TO_GET_VM_LOCATION_LOG = (double)60 * Double.parseDouble(prop.getProperty("vm_location_check_interval")); //seconds
			FILE_LOG_ENABLED = Boolean.parseBoolean(prop.getProperty("file_log_enabled"));
			DEEP_FILE_LOG_ENABLED = Boolean.parseBoolean(prop.getProperty("deep_file_log_enabled"));
			//Qian get trace enable property
			TRACE_ENABLED = Boolean.parseBoolean(prop.getProperty("trace_enabled"));
			
			MIN_NUM_OF_MOBILE_DEVICES = Integer.parseInt(prop.getProperty("min_number_of_mobile_devices"));
			MAX_NUM_OF_MOBILE_DEVICES = Integer.parseInt(prop.getProperty("max_number_of_mobile_devices"));
			MOBILE_DEVICE_COUNTER_SIZE = Integer.parseInt(prop.getProperty("mobile_device_counter_size"));
			
			WAN_PROPOGATION_DELAY = Double.parseDouble(prop.getProperty("wan_propogation_delay"));
			LAN_INTERNAL_DELAY = Double.parseDouble(prop.getProperty("lan_internal_delay"));
			BANDWITH_WLAN = 1000 * Integer.parseInt(prop.getProperty("wlan_bandwidth"));
			BANDWITH_WAN = 1000 * Integer.parseInt(prop.getProperty("wan_bandwidth"));
			BANDWITH_GSM =  1000 * Integer.parseInt(prop.getProperty("gsm_bandwidth"));

			//It is assumed that
			//-Storage and RAM are unlimited in cloud
			//-Each task is executed with maximum capacity (as if there is no task in the cloud) 
			MIPS_FOR_CLOUD = Integer.parseInt(prop.getProperty("mips_for_cloud"));
			
			//Qian get selected nodes from conf file.
			SELECTED_NODES = prop.getProperty("selected_nodes").split(",");
			
			//Shaik added
			selectedHostIds = new int[SELECTED_NODES.length];
			int selHostIdsIndex = 0;
			for (String selNode : SELECTED_NODES) 
				selectedHostIds[selHostIdsIndex++] = Integer.parseInt(selNode);
			
			//Qian get selected levels from conf file
			SELECTED_LEVELS = prop.getProperty("selected_levels").split(",");

			//Shaik added
			selectedLayerIds = new int[SELECTED_LEVELS.length];
			int selLayerIdsIndex = 0;
			for (String selLayer : SELECTED_LEVELS) 
				selectedLayerIds[selLayerIdsIndex++] = Integer.parseInt(selLayer);

			ORCHESTRATOR_POLICIES = prop.getProperty("orchestrator_policies").split(",");
			
			SIMULATION_SCENARIOS = prop.getProperty("simulation_scenarios").split(",");
			
			//avg waiting time in a place (min)
			double place1_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L1_mean_waiting_time"));
			double place2_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L2_mean_waiting_time"));
			double place3_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L3_mean_waiting_time"));
			
			//mean waiting time (minute)
			mobilityLookUpTable = new double[]{
				place1_mean_waiting_time, //ATTRACTIVENESS_L1
				place2_mean_waiting_time, //ATTRACTIVENESS_L2
				place3_mean_waiting_time  //ATTRACTIVENESS_L3
		    };
			

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					result = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		parseApplicatinosXML(applicationsFile);
		parseEdgeDevicesXML(edgeDevicesFile);
		parseLinksXML(linksFile);
		
		return result;
	}
	
	
    /**
	 * @return the selectedHostIds
	 */
	public int[] getSelectedHostIds() {
		return selectedHostIds;
	}

	
	/**
	 * @param selectedHostIds the selectedHostIds to set
	 */
	public void setSelectedHostIds(int[] selectedHostIds) {
		this.selectedHostIds = selectedHostIds;
	}
	
	
	/**
	 * @author Qian
	 * @return cluster type distance base or latency base
	 */
	public boolean getClusterType() {
		return this.clusterType;
	}
	
	
	/**
	 * @author Qian
	 *	@return
	 */
	public boolean getServiceReplacement() {
		return this.serviceReplacement;
	}
	
	
	/**
	 * returns the parsed XML document for edge_devices.xml
	 */
	public Document getEdgeDevicesDocument(){
		return edgeDevicesDoc;
	}

	
	/**
	 * 
	 * @return
	 */
	public Document getLinksDocument() {
		return linksDoc;
	}
	
	
	/**
	 * returns simulation time (in seconds unit) from properties file
	 */
	public double getSimulationTime()
	{
		return SIMULATION_TIME;
	}
	

	/**
	 * returns warm up period (in seconds unit) from properties file
	 */
	public double getWarmUpPeriod()
	{
		return WARM_UP_PERIOD; 
	}
	

	/**
	 * returns VM utilization log collection interval (in seconds unit) from properties file
	 */
	public double getVmLoadLogInterval()
	{
		return INTERVAL_TO_GET_VM_LOAD_LOG; 
	}

	
	/**
	 * returns VM location log collection interval (in seconds unit) from properties file
	 */
	public double getVmLocationLogInterval()
	{
		return INTERVAL_TO_GET_VM_LOCATION_LOG; 
	}

	
	/**
	 * returns deep statistics logging status from properties file
	 */
	public boolean getDeepFileLoggingEnabled()
	{
		return DEEP_FILE_LOG_ENABLED; 
	}

	
	/**
	 * returns deep statistics logging status from properties file
	 */
	public boolean getFileLoggingEnabled()
	{
		return FILE_LOG_ENABLED; 
	}
	
	
	/**
	 * returns WAN propogation delay (in second unit) from properties file
	 */
	public double getWanPropogationDelay()
	{
		return WAN_PROPOGATION_DELAY;
	}

	
	/**
	 * returns internal LAN propogation delay (in second unit) from properties file
	 */
	public double getInternalLanDelay()
	{
		return LAN_INTERNAL_DELAY;
	}

	
	/**
	 * returns WLAN bandwidth (in Mbps unit) from properties file
	 */
	public int getWlanBandwidth()
	{
		return BANDWITH_WLAN;
	}

	
	/**
	 * returns WAN bandwidth (in Mbps unit) from properties file
	 */
	public int getWanBandwidth()
	{
		return BANDWITH_WAN; 
	}

	
	/**
	 * returns GSM bandwidth (in Mbps unit) from properties file
	 */
	public int getGsmBandwidth()
	{
		return BANDWITH_GSM;
	}
	
	
	/**
	 * returns the minimum number of the mobile devices used in the simulation
	 */
	public int getMinNumOfMobileDev()
	{
		return MIN_NUM_OF_MOBILE_DEVICES;
	}

	
	/**
	 * returns the maximunm number of the mobile devices used in the simulation
	 */
	public int getMaxNumOfMobileDev()
	{
		return MAX_NUM_OF_MOBILE_DEVICES;
	}

	
	/**
	 * returns the number of increase on mobile devices
	 * while iterating from min to max mobile device
	 */
	public int getMobileDevCounterSize()
	{
		return MOBILE_DEVICE_COUNTER_SIZE;
	}

	
	/**
	 * returns the number of edge datacenters
	 */
	public int getNumOfEdgeDatacenters()
	{
		return NUM_OF_EDGE_DATACENTERS;
	}

	
	/**
	 * returns the number of edge hosts running on the datacenters
	 */
	public int getNumOfEdgeHosts()
	{
		return NUM_OF_EDGE_HOSTS;
	}

	
	/**
	 * returns the number of edge VMs running on the hosts
	 */
	public int getNumOfEdgeVMs()
	{
		return NUM_OF_EDGE_VMS;
	}

	
	/**
	 * returns MIPS of the central cloud
	 */
	public int getMipsForCloud()
	{
		return MIPS_FOR_CLOUD;
	}

	
	/**
	 * returns simulation screnarios as string
	 */
	public String[] getSimulationScenarios()
	{
		return SIMULATION_SCENARIOS;
	}

	
	/**
	 * returns orchestrator policies as string
	 */
	public String[] getOrchestratorPolicies()
	{
		return ORCHESTRATOR_POLICIES;
	}
	
	
	/**
	 * returns mobility characteristic within an array
	 * the result includes mean waiting time (minute) or each place type
	 */ 
	public double[] getMobilityLookUpTable()
	{
		return mobilityLookUpTable;
	}

	
	/**
	 * returns application characteristic within two dimensional array
	 * the result includes the following values for each application type
	 *  [0] usage percentage (%)
	 *  [1] prob. of selecting cloud (%)
	 *  [2] poisson mean (sec)
	 *  [3] active period (sec)
	 *  [4] idle period (sec)
	 *  [5] avg data upload (KB)
	 *  [6] avg data download (KB)
	 *  [7] avg task length (MI)
	 *  [8] required # of cores
	 *  [9] vm utilization (%)
	 * [10] max latency (Seconds)
	 */ 
	public double[][] getTaskLookUpTable()
	{
		return taskLookUpTable;
	}
	
	
	/**
	 * 
	 * @param element
	 * @param key
	 */
	private void isAttribtuePresent(Element element, String key) {
        String value = element.getAttribute(key);
        if (value.isEmpty() || value == null){
        	throw new IllegalArgumentException("Attribure '" + key + "' is not found in '" + element.getNodeName() +"'");
        }
	}

	
	/**
	 * 
	 * @param element
	 * @param key
	 */
	private void isElementPresent(Element element, String key) {
		try {
			String value = element.getElementsByTagName(key).item(0).getTextContent();
	        if (value.isEmpty() || value == null){
	        	throw new IllegalArgumentException("Element '" + key + "' is not found in '" + element.getNodeName() +"'");
	        }
		} catch (Exception e) {
			throw new IllegalArgumentException("Element '" + key + "' is not found in '" + element.getNodeName() +"'");
		}
	}
	
	
	/**
	 * 
	 * @param filePath
	 */
	private void parseApplicatinosXML(String filePath)
	{
		Document doc = null;
		try {	
			File devicesFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(devicesFile);
			doc.getDocumentElement().normalize();

			NodeList appList = doc.getElementsByTagName("application");
			for (int i = 0; i < appList.getLength(); i++) {
				Node appNode = appList.item(i);
	
				Element appElement = (Element) appNode;
				isAttribtuePresent(appElement, "name");
				isElementPresent(appElement, "usage_percentage");
				isElementPresent(appElement, "prob_cloud_selection");
				isElementPresent(appElement, "poisson_interarrival");
				isElementPresent(appElement, "active_period");
				isElementPresent(appElement, "idle_period");
				isElementPresent(appElement, "data_upload");
				isElementPresent(appElement, "data_download");
				isElementPresent(appElement, "task_length");
				isElementPresent(appElement, "required_core");
				isElementPresent(appElement, "vm_utilization");
				isElementPresent(appElement, "delay_sensitivity");

				String appName = appElement.getAttribute("name");
				SimSettings.APP_TYPES appType = APP_TYPES.valueOf(appName);
				double usage_percentage = Double.parseDouble(appElement.getElementsByTagName("usage_percentage").item(0).getTextContent());
				double prob_cloud_selection = Double.parseDouble(appElement.getElementsByTagName("prob_cloud_selection").item(0).getTextContent());
				double poisson_interarrival = Double.parseDouble(appElement.getElementsByTagName("poisson_interarrival").item(0).getTextContent());
				double active_period = Double.parseDouble(appElement.getElementsByTagName("active_period").item(0).getTextContent());
				double idle_period = Double.parseDouble(appElement.getElementsByTagName("idle_period").item(0).getTextContent());
				double data_upload = Double.parseDouble(appElement.getElementsByTagName("data_upload").item(0).getTextContent());
				double data_download = Double.parseDouble(appElement.getElementsByTagName("data_download").item(0).getTextContent());
				double task_length = Double.parseDouble(appElement.getElementsByTagName("task_length").item(0).getTextContent());
				double required_core = Double.parseDouble(appElement.getElementsByTagName("required_core").item(0).getTextContent());
				double vm_utilization = Double.parseDouble(appElement.getElementsByTagName("vm_utilization").item(0).getTextContent());
				double delay_sensitivity = Double.parseDouble(appElement.getElementsByTagName("delay_sensitivity").item(0).getTextContent());
				
			    taskLookUpTable[appType.ordinal()][0] = usage_percentage; //usage percentage [0-100]
			    taskLookUpTable[appType.ordinal()][1] = prob_cloud_selection; //prob. of selecting cloud [0-100]
			    taskLookUpTable[appType.ordinal()][2] = poisson_interarrival; //poisson mean (sec)
			    taskLookUpTable[appType.ordinal()][3] = active_period; //active period (sec)
			    taskLookUpTable[appType.ordinal()][4] = idle_period; //idle period (sec)
			    taskLookUpTable[appType.ordinal()][5] = data_upload; //avg data upload (KB)
			    taskLookUpTable[appType.ordinal()][6] = data_download; //avg data download (KB)
			    taskLookUpTable[appType.ordinal()][7] = task_length; //avg task length (MI)
			    taskLookUpTable[appType.ordinal()][8] = required_core; //required # of core
			    taskLookUpTable[appType.ordinal()][9] = vm_utilization; //vm utilization [0-100]
			    taskLookUpTable[appType.ordinal()][10] = delay_sensitivity; //delay_sensitivity (seconds)
			    
			}
	
		} catch (Exception e) {
			SimLogger.printLine("Edge Devices XML cannot be parsed! Terminating simulation...");
			e.printStackTrace();
			System.exit(0);
		}
	}

	
	/**
	 * 
	 * @param filePath
	 */
	private void parseEdgeDevicesXML(String filePath)
	{
		try {	
			File devicesFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			edgeDevicesDoc = dBuilder.parse(devicesFile);
			edgeDevicesDoc.getDocumentElement().normalize();

			NodeList datacenterList = edgeDevicesDoc.getElementsByTagName("datacenter");
			for (int i = 0; i < datacenterList.getLength(); i++) {
			    NUM_OF_EDGE_DATACENTERS++;
				Node datacenterNode = datacenterList.item(i);
	
				Element datacenterElement = (Element) datacenterNode;
				isAttribtuePresent(datacenterElement, "arch");
				isAttribtuePresent(datacenterElement, "os");
				isAttribtuePresent(datacenterElement, "vmm");
				isElementPresent(datacenterElement, "costPerBw");
				isElementPresent(datacenterElement, "costPerSec");
				isElementPresent(datacenterElement, "costPerMem");
				isElementPresent(datacenterElement, "costPerStorage");

				Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
				//isElementPresent(location, "attractiveness");
				isElementPresent(location, "wlan_id");
				isElementPresent(location, "x_pos");
				isElementPresent(location, "y_pos");

				NodeList hostList = datacenterElement.getElementsByTagName("host");
				for (int j = 0; j < hostList.getLength(); j++) {
				    NUM_OF_EDGE_HOSTS++;
					Node hostNode = hostList.item(j);
					
					Element hostElement = (Element) hostNode;
					isElementPresent(hostElement, "core");
					isElementPresent(hostElement, "mips");
					isElementPresent(hostElement, "ram");
					isElementPresent(hostElement, "storage");

					NodeList vmList = hostElement.getElementsByTagName("VM");
					for (int k = 0; k < vmList.getLength(); k++) {
					    NUM_OF_EDGE_VMS++;
						Node vmNode = vmList.item(k);
						
						Element vmElement = (Element) vmNode;
						isAttribtuePresent(vmElement, "vmm");
						isElementPresent(vmElement, "core");
						isElementPresent(vmElement, "mips");
						isElementPresent(vmElement, "ram");
						isElementPresent(vmElement, "storage");
					}
				}
			}
	
		} catch (Exception e) {
			SimLogger.printLine("Edge Devices XML cannot be parsed! Terminating simulation...");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	/**
	 * 
	 * @param filePath
	 */
	public void parseLinksXML(String filePath) 
	{
		try {
		File linksFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		linksDoc = dBuilder.parse(linksFile);
		linksDoc.getDocumentElement().normalize();
		
		NodeList linksList = linksDoc.getElementsByTagName("link");
		for(int i = 0; i < linksList.getLength(); i++) {
			//SimLogger.printLine("No issues yet...cross your fingers!");			
		}
		
		}
		catch (Exception e) {
			SimLogger.printLine("Links XML cannot be parsed! Terminating simulation...");
			e.printStackTrace();
			System.exit(0);
		}		
	}

	
	/**
	 * 
	 * @param _simulationSpace
	 */
	public void setSimulationSpace(double[] _simulationSpace) {
		// TODO Auto-generated method stub
		this.simulationSpace = _simulationSpace;
	}

	
	/**
	 * 
	 * @return
	 */
	public double[] getSimulationSpace() {
		// TODO Auto-generated method stub
		return this.simulationSpace;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getMaxLevels() {
		return MAX_LEVELS;
	}
	
	
	/**
	 * 
	 * @param _MAX_LEVELS
	 */
	public void setMaxLevels(int _MAX_LEVELS) {
		this.MAX_LEVELS = _MAX_LEVELS;
	}
	
	
	/**
	 * 
	 * @param _inputType
	 */
	public void setInputType(String _inputType) {
		this.inputType = _inputType;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getInputType() {
		return this.inputType;
	}
	
	
	/**
	 * 
	 * @param _moving
	 */
	public void setMobileDevicesMoving(boolean _moving) {
		this.mobileDevicesMoving = _moving;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean areMobileDevicesMoving() {
		return this.mobileDevicesMoving;
	}
	
	
	/**
	 * Qian get trace enabled infomation 
	 * @return
	 */
	public boolean traceEnable() {
		return TRACE_ENABLED;
	}

	
	/**
	 * Qian get next selected node number
	 * @return
	 */
	public int nextSelectedNode() {
		int nodeID = Integer.parseInt(SELECTED_NODES[currentSelection]);
		currentSelection++;
		if (currentSelection == SELECTED_NODES.length) {
			currentSelection = 0;
		}
		return nodeID;
	}
	
	
	/**
	 * Qian get next selected level
	 * @return
	 */
	public int nextSelectedLevel() {
		int level = Integer.parseInt(SELECTED_LEVELS[currentLevelIndex]);
		currentLevelIndex++;
		if (currentLevelIndex == SELECTED_LEVELS.length) {
			currentLevelIndex = 0;
		}
		return level;
	}

	
	/**
	 * @return the edgeDevicesDoc
	 */
	public Document getEdgeDevicesDoc() {
		return edgeDevicesDoc;
	}

	
	/**
	 * @param edgeDevicesDoc the edgeDevicesDoc to set
	 */
	public void setEdgeDevicesDoc(Document edgeDevicesDoc) {
		this.edgeDevicesDoc = edgeDevicesDoc;
	}

	
	/**
	 * @return the linksDoc
	 */
	public Document getLinksDoc() {
		return linksDoc;
	}

	
	/**
	 * @param linksDoc the linksDoc to set
	 */
	public void setLinksDoc(Document linksDoc) {
		this.linksDoc = linksDoc;
	}

	
	/**
	 * @return the cLOUD_DATACENTER_ID
	 */
	public static int getCLOUD_DATACENTER_ID() {
		return CLOUD_DATACENTER_ID;
	}

	
	/**
	 * @param cLOUD_DATACENTER_ID the cLOUD_DATACENTER_ID to set
	 */
	public static void setCLOUD_DATACENTER_ID(int cLOUD_DATACENTER_ID) {
		CLOUD_DATACENTER_ID = cLOUD_DATACENTER_ID;
	}

	
	/**
	 * @return the cLOUD_HOST_ID
	 */
	public static int getCLOUD_HOST_ID() {
		return CLOUD_HOST_ID;
	}

	
	/**
	 * @param cLOUD_HOST_ID the cLOUD_HOST_ID to set
	 */
	public static void setCLOUD_HOST_ID(int cLOUD_HOST_ID) {
		CLOUD_HOST_ID = cLOUD_HOST_ID;
	}

	
	/**
	 * @return the cLOUD_VM_ID
	 */
	public static int getCLOUD_VM_ID() {
		return CLOUD_VM_ID;
	}

	
	/**
	 * @param cLOUD_VM_ID the cLOUD_VM_ID to set
	 */
	public static void setCLOUD_VM_ID(int cLOUD_VM_ID) {
		CLOUD_VM_ID = cLOUD_VM_ID;
	}

	
	/**
	 * @return the eDGE_ORCHESTRATOR_ID
	 */
	public static int getEDGE_ORCHESTRATOR_ID() {
		return EDGE_ORCHESTRATOR_ID;
	}

	
	/**
	 * @param eDGE_ORCHESTRATOR_ID the eDGE_ORCHESTRATOR_ID to set
	 */
	public static void setEDGE_ORCHESTRATOR_ID(int eDGE_ORCHESTRATOR_ID) {
		EDGE_ORCHESTRATOR_ID = eDGE_ORCHESTRATOR_ID;
	}

	
	/**
	 * @return the gENERIC_EDGE_DEVICE_ID
	 */
	public static int getGENERIC_EDGE_DEVICE_ID() {
		return GENERIC_EDGE_DEVICE_ID;
	}

	
	/**
	 * @param gENERIC_EDGE_DEVICE_ID the gENERIC_EDGE_DEVICE_ID to set
	 */
	public static void setGENERIC_EDGE_DEVICE_ID(int gENERIC_EDGE_DEVICE_ID) {
		GENERIC_EDGE_DEVICE_ID = gENERIC_EDGE_DEVICE_ID;
	}

	
	/**
	 * @return the mOBILE_DEVICE_ID
	 */
	public static int getMOBILE_DEVICE_ID() {
		return MOBILE_DEVICE_ID;
	}

	
	/**
	 * @param mOBILE_DEVICE_ID the mOBILE_DEVICE_ID to set
	 */
	public static void setMOBILE_DEVICE_ID(int mOBILE_DEVICE_ID) {
		MOBILE_DEVICE_ID = mOBILE_DEVICE_ID;
	}

	
	/**
	 * @return the dELIMITER
	 */
	public static String getDELIMITER() {
		return DELIMITER;
	}

	
	/**
	 * @param dELIMITER the dELIMITER to set
	 */
	public static void setDELIMITER(String dELIMITER) {
		DELIMITER = dELIMITER;
	}

	
	/**
	 * @return the sIMULATION_TIME
	 */
	public double getSIMULATION_TIME() {
		return SIMULATION_TIME;
	}
	

	/**
	 * @param sIMULATION_TIME the sIMULATION_TIME to set
	 */
	public void setSIMULATION_TIME(double sIMULATION_TIME) {
		SIMULATION_TIME = sIMULATION_TIME;
	}

	
	/**
	 * @return the wARM_UP_PERIOD
	 */
	public double getWARM_UP_PERIOD() {
		return WARM_UP_PERIOD;
	}

	
	/**
	 * @param wARM_UP_PERIOD the wARM_UP_PERIOD to set
	 */
	public void setWARM_UP_PERIOD(double wARM_UP_PERIOD) {
		WARM_UP_PERIOD = wARM_UP_PERIOD;
	}

	
	/**
	 * @return the iNTERVAL_TO_GET_VM_LOAD_LOG
	 */
	public double getINTERVAL_TO_GET_VM_LOAD_LOG() {
		return INTERVAL_TO_GET_VM_LOAD_LOG;
	}

	
	/**
	 * @param iNTERVAL_TO_GET_VM_LOAD_LOG the iNTERVAL_TO_GET_VM_LOAD_LOG to set
	 */
	public void setINTERVAL_TO_GET_VM_LOAD_LOG(double iNTERVAL_TO_GET_VM_LOAD_LOG) {
		INTERVAL_TO_GET_VM_LOAD_LOG = iNTERVAL_TO_GET_VM_LOAD_LOG;
	}

	
	/**
	 * @return the iNTERVAL_TO_GET_VM_LOCATION_LOG
	 */
	public double getINTERVAL_TO_GET_VM_LOCATION_LOG() {
		return INTERVAL_TO_GET_VM_LOCATION_LOG;
	}

	
	/**
	 * @param iNTERVAL_TO_GET_VM_LOCATION_LOG the iNTERVAL_TO_GET_VM_LOCATION_LOG to set
	 */
	public void setINTERVAL_TO_GET_VM_LOCATION_LOG(double iNTERVAL_TO_GET_VM_LOCATION_LOG) {
		INTERVAL_TO_GET_VM_LOCATION_LOG = iNTERVAL_TO_GET_VM_LOCATION_LOG;
	}

	
	/**
	 * @return the fILE_LOG_ENABLED
	 */
	public boolean isFILE_LOG_ENABLED() {
		return FILE_LOG_ENABLED;
	}

	
	/**
	 * @param fILE_LOG_ENABLED the fILE_LOG_ENABLED to set
	 */
	public void setFILE_LOG_ENABLED(boolean fILE_LOG_ENABLED) {
		FILE_LOG_ENABLED = fILE_LOG_ENABLED;
	}

	
	/**
	 * @return the dEEP_FILE_LOG_ENABLED
	 */
	public boolean isDEEP_FILE_LOG_ENABLED() {
		return DEEP_FILE_LOG_ENABLED;
	}

	
	/**
	 * @param dEEP_FILE_LOG_ENABLED the dEEP_FILE_LOG_ENABLED to set
	 */
	public void setDEEP_FILE_LOG_ENABLED(boolean dEEP_FILE_LOG_ENABLED) {
		DEEP_FILE_LOG_ENABLED = dEEP_FILE_LOG_ENABLED;
	}

	
	/**
	 * @return the tRACE_ENABLED
	 */
	public boolean isTRACE_ENABLED() {
		return TRACE_ENABLED;
	}

	
	/**
	 * @param tRACE_ENABLED the tRACE_ENABLED to set
	 */
	public void setTRACE_ENABLED(boolean tRACE_ENABLED) {
		TRACE_ENABLED = tRACE_ENABLED;
	}

	
	/**
	 * @return the mIN_NUM_OF_MOBILE_DEVICES
	 */
	public int getMIN_NUM_OF_MOBILE_DEVICES() {
		return MIN_NUM_OF_MOBILE_DEVICES;
	}

	
	/**
	 * @param mIN_NUM_OF_MOBILE_DEVICES the mIN_NUM_OF_MOBILE_DEVICES to set
	 */
	public void setMIN_NUM_OF_MOBILE_DEVICES(int mIN_NUM_OF_MOBILE_DEVICES) {
		MIN_NUM_OF_MOBILE_DEVICES = mIN_NUM_OF_MOBILE_DEVICES;
	}

	
	/**
	 * @return the mAX_NUM_OF_MOBILE_DEVICES
	 */
	public int getMAX_NUM_OF_MOBILE_DEVICES() {
		return MAX_NUM_OF_MOBILE_DEVICES;
	}

	
	/**
	 * @param mAX_NUM_OF_MOBILE_DEVICES the mAX_NUM_OF_MOBILE_DEVICES to set
	 */
	public void setMAX_NUM_OF_MOBILE_DEVICES(int mAX_NUM_OF_MOBILE_DEVICES) {
		MAX_NUM_OF_MOBILE_DEVICES = mAX_NUM_OF_MOBILE_DEVICES;
	}

	
	/**
	 * @return the mOBILE_DEVICE_COUNTER_SIZE
	 */
	public int getMOBILE_DEVICE_COUNTER_SIZE() {
		return MOBILE_DEVICE_COUNTER_SIZE;
	}

	
	/**
	 * @param mOBILE_DEVICE_COUNTER_SIZE the mOBILE_DEVICE_COUNTER_SIZE to set
	 */
	public void setMOBILE_DEVICE_COUNTER_SIZE(int mOBILE_DEVICE_COUNTER_SIZE) {
		MOBILE_DEVICE_COUNTER_SIZE = mOBILE_DEVICE_COUNTER_SIZE;
	}

	
	/**
	 * @return the nUM_OF_EDGE_DATACENTERS
	 */
	public int getNUM_OF_EDGE_DATACENTERS() {
		return NUM_OF_EDGE_DATACENTERS;
	}

	
	/**
	 * @param nUM_OF_EDGE_DATACENTERS the nUM_OF_EDGE_DATACENTERS to set
	 */
	public void setNUM_OF_EDGE_DATACENTERS(int nUM_OF_EDGE_DATACENTERS) {
		NUM_OF_EDGE_DATACENTERS = nUM_OF_EDGE_DATACENTERS;
	}

	
	/**
	 * @return the nUM_OF_EDGE_HOSTS
	 */
	public int getNUM_OF_EDGE_HOSTS() {
		return NUM_OF_EDGE_HOSTS;
	}

	
	/**
	 * @param nUM_OF_EDGE_HOSTS the nUM_OF_EDGE_HOSTS to set
	 */
	public void setNUM_OF_EDGE_HOSTS(int nUM_OF_EDGE_HOSTS) {
		NUM_OF_EDGE_HOSTS = nUM_OF_EDGE_HOSTS;
	}

	
	/**
	 * @return the nUM_OF_EDGE_VMS
	 */
	public int getNUM_OF_EDGE_VMS() {
		return NUM_OF_EDGE_VMS;
	}

	
	/**
	 * @param nUM_OF_EDGE_VMS the nUM_OF_EDGE_VMS to set
	 */
	public void setNUM_OF_EDGE_VMS(int nUM_OF_EDGE_VMS) {
		NUM_OF_EDGE_VMS = nUM_OF_EDGE_VMS;
	}

	
	/**
	 * @return the wAN_PROPOGATION_DELAY
	 */
	public double getWAN_PROPOGATION_DELAY() {
		return WAN_PROPOGATION_DELAY;
	}

	
	/**
	 * @param wAN_PROPOGATION_DELAY the wAN_PROPOGATION_DELAY to set
	 */
	public void setWAN_PROPOGATION_DELAY(double wAN_PROPOGATION_DELAY) {
		WAN_PROPOGATION_DELAY = wAN_PROPOGATION_DELAY;
	}

	
	/**
	 * @return the lAN_INTERNAL_DELAY
	 */
	public double getLAN_INTERNAL_DELAY() {
		return LAN_INTERNAL_DELAY;
	}

	
	/**
	 * @param lAN_INTERNAL_DELAY the lAN_INTERNAL_DELAY to set
	 */
	public void setLAN_INTERNAL_DELAY(double lAN_INTERNAL_DELAY) {
		LAN_INTERNAL_DELAY = lAN_INTERNAL_DELAY;
	}

	
	/**
	 * @return the bANDWITH_WLAN
	 */
	public int getBANDWITH_WLAN() {
		return BANDWITH_WLAN;
	}

	
	/**
	 * @param bANDWITH_WLAN the bANDWITH_WLAN to set
	 */
	public void setBANDWITH_WLAN(int bANDWITH_WLAN) {
		BANDWITH_WLAN = bANDWITH_WLAN;
	}

	
	/**
	 * @return the bANDWITH_WAN
	 */
	public int getBANDWITH_WAN() {
		return BANDWITH_WAN;
	}

	
	/**
	 * @param bANDWITH_WAN the bANDWITH_WAN to set
	 */
	public void setBANDWITH_WAN(int bANDWITH_WAN) {
		BANDWITH_WAN = bANDWITH_WAN;
	}

	
	/**
	 * @return the bANDWITH_GSM
	 */
	public int getBANDWITH_GSM() {
		return BANDWITH_GSM;
	}

	
	/**
	 * @param bANDWITH_GSM the bANDWITH_GSM to set
	 */
	public void setBANDWITH_GSM(int bANDWITH_GSM) {
		BANDWITH_GSM = bANDWITH_GSM;
	}

	
	/**
	 * @return the mIPS_FOR_CLOUD
	 */
	public int getMIPS_FOR_CLOUD() {
		return MIPS_FOR_CLOUD;
	}

	
	/**
	 * @param mIPS_FOR_CLOUD the mIPS_FOR_CLOUD to set
	 */
	public void setMIPS_FOR_CLOUD(int mIPS_FOR_CLOUD) {
		MIPS_FOR_CLOUD = mIPS_FOR_CLOUD;
	}

	
	/**
	 * @return the sELECTED_NODES
	 */
	public String[] getSELECTED_NODES() {
		return SELECTED_NODES;
	}

	
	/**
	 * @param sELECTED_NODES the sELECTED_NODES to set
	 */
	public void setSELECTED_NODES(String[] sELECTED_NODES) {
		SELECTED_NODES = sELECTED_NODES;
	}

	
	/**
	 * @return the currentSelection
	 */
	public int getCurrentSelection() {
		return currentSelection;
	}

	
	/**
	 * @param currentSelection the currentSelection to set
	 */
	public void setCurrentSelection(int currentSelection) {
		this.currentSelection = currentSelection;
	}

	
	/**
	 * @return the sELECTED_LEVELS
	 */
	public String[] getSELECTED_LEVELS() {
		return SELECTED_LEVELS;
	}

	
	/**
	 * @param sELECTED_LEVELS the sELECTED_LEVELS to set
	 */
	public void setSELECTED_LEVELS(String[] sELECTED_LEVELS) {
		SELECTED_LEVELS = sELECTED_LEVELS;
	}

	
	/**
	 * @return the currentLevelIndex
	 */
	public int getCurrentLevelIndex() {
		return currentLevelIndex;
	}

	
	/**
	 * @param currentLevelIndex the currentLevelIndex to set
	 */
	public void setCurrentLevelIndex(int currentLevelIndex) {
		this.currentLevelIndex = currentLevelIndex;
	}

	
	/**
	 * @return the sIMULATION_SCENARIOS
	 */
	public String[] getSIMULATION_SCENARIOS() {
		return SIMULATION_SCENARIOS;
	}

	
	/**
	 * @param sIMULATION_SCENARIOS the sIMULATION_SCENARIOS to set
	 */
	public void setSIMULATION_SCENARIOS(String[] sIMULATION_SCENARIOS) {
		SIMULATION_SCENARIOS = sIMULATION_SCENARIOS;
	}

	
	/**
	 * @return the oRCHESTRATOR_POLICIES
	 */
	public String[] getORCHESTRATOR_POLICIES() {
		return ORCHESTRATOR_POLICIES;
	}

	
	/**
	 * @param oRCHESTRATOR_POLICIES the oRCHESTRATOR_POLICIES to set
	 */
	public void setORCHESTRATOR_POLICIES(String[] oRCHESTRATOR_POLICIES) {
		ORCHESTRATOR_POLICIES = oRCHESTRATOR_POLICIES;
	}

	
	/**
	 * @return the mAX_LEVELS
	 */
	public int getMAX_LEVELS() {
		return MAX_LEVELS;
	}

	
	/**
	 * @param mAX_LEVELS the mAX_LEVELS to set
	 */
	public void setMAX_LEVELS(int mAX_LEVELS) {
		MAX_LEVELS = mAX_LEVELS;
	}

	
	/**
	 * @return the mobileDevicesMoving
	 */
	public boolean isMobileDevicesMoving() {
		return mobileDevicesMoving;
	}

	
	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(SimSettings instance) {
		SimSettings.instance = instance;
	}

	
	/**
	 * @param clusterType the clusterType to set
	 */
	public void setClusterType(boolean clusterType) {
		this.clusterType = clusterType;
	}
	

	/**
	 * @param serviceReplacement the serviceReplacement to set
	 */
	public void setServiceReplacement(boolean serviceReplacement) {
		this.serviceReplacement = serviceReplacement;
	}

	
	/**
	 * @param mobilityLookUpTable the mobilityLookUpTable to set
	 */
	public void setMobilityLookUpTable(double[] mobilityLookUpTable) {
		this.mobilityLookUpTable = mobilityLookUpTable;
	}

	
	/**
	 * @param taskLookUpTable the taskLookUpTable to set
	 */
	public void setTaskLookUpTable(double[][] taskLookUpTable) {
		this.taskLookUpTable = taskLookUpTable;
	}

	
	/**
	 * @return the selectedLayerIds
	 */
	public int[] getSelectedLayerIds() {
		return selectedLayerIds;
	}

	
	/**
	 * @param selectedLayerIds the selectedLayerIds to set
	 */
	public void setSelectedLayerIds(int[] selectedLayerIds) {
		this.selectedLayerIds = selectedLayerIds;
	}
}