package edu.auburn.pFogSim.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

import edu.boun.edgecloudsim.core.SimSettings;


/**
 * 
 * @author szs0117
 *
 */
public class DataInterpreter {
	private static int MAX_LEVELS = 7;
	private static String[] files= {
			"data/Google_Cloud_DC.csv", 
			"data/Chicago_CityHall.csv", 
			"data/Chicago_Universities.csv", 
			"data/Chicago_Wards.csv", 
			"data/Chicago_Libraries.csv", 
			"data/Chicago_Connect.csv", 
			"data/Chicago_Schools.csv"};
	private static String[][] nodeSpecs = new String[MAX_LEVELS][20];// the specs for all layers of the fog devices
	private static ArrayList<Double[]> nodeList = new ArrayList<Double[]>();
	private static ArrayList<Double[]> tempList = new ArrayList<Double[]>();
	private static ArrayList<Double[]> universitiesCircle = new ArrayList<Double[]>();
	private static ArrayList<Double[]> universitiesList = new ArrayList<Double[]>();
	
	//This will return as height/y is LAT and width/x is LONG
	private static double MIN_LAT = -100000, MAX_LAT = -100000, MIN_LONG = -100000, MAX_LONG = -100000; //Just instantiated so the first gps coord sets these
	
	private static boolean universitiesYet = false;
	private static boolean universitiesLinked = false;
    private static String inputType = "gps";
    private static boolean movingMobileDevices = false;
	
	private File xmlFile = null;
	private FileWriter xmlFW = null;
	private BufferedWriter xmlBR = null;
	
	
	/**
	 * Static method - to measure distance between two GPS locations.
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static double measure(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
	    double R = 6378.137; // Radius of earth in KM
	    double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
	    double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
	    //Haversine Formula
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double d = R * c;
	    return d * 1000; // meters
	}
	
	public static double measure(double lat1, double lon1, double alt1, double lat2, double lon2, double alt2){  // generally used geo measurement function
		double d = DataInterpreter.measure(lat1,lon1,lat2,lon2);
		//Pythagoras
		double dist = Math.sqrt((d*d)+((alt1-alt2)*(alt1-alt2)));    
		return dist;
	}
	
	
	/**
	 * Creates input files for node and link configurations - as per specified system configuration.
	 * @throws IOException
	 */
	public static void readFile() throws IOException {
		FileReader dataFR = null;
		BufferedReader dataBR = null;
		PrintWriter node = new PrintWriter("node_test.xml", "UTF-8");
	    PrintWriter links = new PrintWriter("links_test.xml", "UTF-8");
		
	    node.println("<?xml version=\"1.0\"?>");
	    links.println("<?xml version=\"1.0\"?>");
	    node.println("<edge_devices>");
	    links.println("<links>");
	    
		String rawNode = null;
		String[] nodeLoc = new String[4];
		Double[] temp = new Double[4];
		int counter = 0;
		int prevCounter = 0;
		for(int i = 0; i < MAX_LEVELS; i++)
		{
			
			try {
				dataFR = new FileReader(files[i]);
				dataBR = new BufferedReader(dataFR);
			}
			catch (FileNotFoundException e) {
			}
			dataBR.readLine(); //Gets rid of title data
			while(dataBR.ready()) {

				rawNode = dataBR.readLine();
				nodeLoc = rawNode.split(",");
				temp[0] = (double)counter; //ID
				temp[2] = Double.parseDouble(nodeLoc[1]); //Y Coord
				temp[1] = Double.parseDouble(nodeLoc[2]); //X Coord
				temp[3] = Double.parseDouble(nodeLoc[3]); //Altitude
				if(MAX_LONG == -100000 || temp[1] > MAX_LONG)	MAX_LONG = temp[1];		
				if(MIN_LONG == -100000 || temp[1] < MIN_LONG)	MIN_LONG = temp[1];	
				if(MAX_LAT == -100000 || temp[2] > MAX_LAT)	MAX_LAT = temp[2];	
				if(MIN_LAT == -100000 || temp[2] < MIN_LAT)	MIN_LAT = temp[2];	
				
				//Add to output file		    
			    node.println(String.format("<datacenter arch=\"%s\" os=\"%s\" vmm=\"%s\">\n", nodeSpecs[MAX_LEVELS - i - 1][0], nodeSpecs[MAX_LEVELS - i - 1][1], nodeSpecs[MAX_LEVELS - i - 1][2]));
			    node.println(String.format("<costPerBw>%s</costPerBw>\n\t<costPerSec>%s</costPerSec>\n\t<costPerMem>%s</costPerMem>\n\t<costPerStorage>%s</costPerStorage>", nodeSpecs[MAX_LEVELS - i - 1][3], nodeSpecs[MAX_LEVELS - i - 1][4], nodeSpecs[MAX_LEVELS - i - 1][5], nodeSpecs[MAX_LEVELS - i - 1][6]));
			    //Qian change level start from 1
			    node.println(String.format("<location>\n\t<x_pos>%s</x_pos>\n\t<y_pos>%s</y_pos>\n\t<altitude>%s</altitude>\n\t<level>%s</level>\t<wlan_id>%s</wlan_id>\n\t<wap>%s</wap>\n\t<moving>%s</moving>\n\t<bandwidth>%s</bandwidth>\n\t<dx>%s</dx>\n\t<dy>%s</dy>/n</location>", nodeLoc[2], nodeLoc[1],nodeLoc[3], MAX_LEVELS - i, counter, nodeSpecs[MAX_LEVELS - i - 1][7], nodeSpecs[MAX_LEVELS - i - 1][8], nodeSpecs[MAX_LEVELS - i - 1][13], nodeLoc[4], nodeLoc[5]));
			    node.println(String.format("<host>\n\t<core>%s</core>\n\t<mips>%s</mips>\n\t<ram>%s</ram>\n\t<storage>%s</storage>\n", nodeSpecs[MAX_LEVELS - i - 1][9], nodeSpecs[MAX_LEVELS - i - 1][10], nodeSpecs[MAX_LEVELS - i - 1][11], nodeSpecs[MAX_LEVELS - i - 1][12]));
			    node.println(String.format("\t<VM vmm=\"%s\">\n\t\t\t<core>%s</core>\n\t\t\t<mips>%s</mips>\n\t\t\t<ram>%s</ram>\n\t\t\t<storage>%s</storage>\n\t\t</VM>\n\t</host>\n</datacenter>", nodeSpecs[MAX_LEVELS - i - 1][2], nodeSpecs[MAX_LEVELS - i - 1][9], nodeSpecs[MAX_LEVELS - i - 1][10], nodeSpecs[MAX_LEVELS - i - 1][11], nodeSpecs[MAX_LEVELS - i - 1][12]));
	
				
				//Make link to previous closest node on higher level
				if(!nodeList.isEmpty())
				{
					double minDistance = Double.MAX_VALUE;
					int index = -1;
					double distance = 0;
					//Go through all nodes one level up and find the closest
					for(int j = 0; j < nodeList.size(); j++)
					{

						distance = measure(nodeList.get(j)[2], nodeList.get(j)[1], nodeList.get(j)[3], temp[2], temp[1], temp[3]);
						if(distance < minDistance)
						{
							minDistance = distance;
							index = j;
						}
					}
					minDistance = Double.MAX_VALUE;
					if(index >= 0)
					{
						if(nodeList.get(index).equals(temp)) 
						{
							System.exit(0);
						}
						double dis = measure(temp[2], temp[1], temp[3], nodeList.get(index)[2], nodeList.get(index)[1], nodeList.get(index)[3]) / 1000;
						double latency = dis * 0.01;
						links.println("<link>\n" + 
					    		"		<name>L" + nodeList.get(index)[0] + "_" + temp[0] + "</name>\n" + 
					    		"		<left>\n" + 
					    		"			<x_pos>" + temp[1] + "</x_pos>\n" + 
					    		"			<y_pos>" + temp[2] + "</y_pos>\n" +
					    		"			<altitude>" + temp[3] + "</altitude>" +
					    		"		</left>\n" + 
					    		"		<right>\n" + 
					    		"			<x_pos>" + nodeList.get(index)[1] + "</x_pos>\n" + 
					    		"			<y_pos>" + nodeList.get(index)[2] + "</y_pos>\n" +
					    		"			<altitude>" + nodeList.get(index)[3] + "</altitude>" +
					    		"		</right>\n" + 
					    		"		<left_latency>" + latency + "</left_latency>\n" + 
					    		"		<right_latency>" + latency + "</right_latency>\n" + 
					    		"	</link>");
						
						
					}
				}

				

				tempList.add(new Double[] {(double)temp[0], (double)temp[1], (double)temp[2], (double)temp[3]});
				counter++;
				
				

			}
			
			prevCounter = counter;

			//move tempList to nodeList

			// Include additional links among border routers
			if(i == 2) // Universities fog layer
			{
				// For each university, find the nearest and second nearest and create links to the two identified ones. 
				for(Double[] input : tempList)
				{
					double minDistance = Double.MAX_VALUE;
					double secondminDistance = Double.MAX_VALUE;
					int index1 = -1, index2 = -1;
					double distance = 0;
					
					
					//Go through all nodes and find the closest
					for(int j = 0; j < tempList.size(); j++)
					{

						distance = measure(tempList.get(j)[2], tempList.get(j)[1], tempList.get(j)[3], input[2], input[1], input[3]);

						if(distance < minDistance && distance != 0)
						{
							secondminDistance = minDistance;
							index2 = index1;
							minDistance = distance;
							index1 = j;
						}

						else if(distance < secondminDistance && distance != 0)
						{
							secondminDistance = distance;
							index2 = j;
						}
					}
					minDistance = Double.MAX_VALUE;
					secondminDistance = Double.MAX_VALUE;
					if(index1 >= 0)
					{
						if(tempList.get(index1).equals(temp)) 
						{
							System.exit(0);
						}
						double dis = measure(input[2], input[1], input[3], tempList.get(index1)[2], tempList.get(index1)[1], tempList.get(index1)[3]) / 1000;
						double latency = dis * 0.01;
						links.println("<link>\n" + 
					    		"		<name>L" + tempList.get(index1)[0] + "_" + input[0] + "</name>\n" + 
						   		"		<left>\n" + 
					    		"			<x_pos>" + input[1] + "</x_pos>\n" + 
						   		"			<y_pos>" + input[2] + "</y_pos>\n" + 
					   			"			<altitude>" + input[3] + "</altitude>" +
						   		"		</left>\n" + 
						   		"		<right>\n" + 
						    	"			<x_pos>" + tempList.get(index1)[1] + "</x_pos>\n" + 
						   		"			<y_pos>" + tempList.get(index1)[2] + "</y_pos>\n" + 
						   		"			<altitude>" + tempList.get(index1)[3] + "</altitude>" +
						   		"		</right>\n" + 
						   		"		<left_latency>"+latency+"</left_latency>\n" + 
						   		"		<right_latency>"+latency+"</right_latency>\n" + 
						   		"	</link>");
						

						}
					if(index2 >= 0)
					{
						if(tempList.get(index2).equals(temp)) 
						{
							System.exit(0);
						}
						double dis = measure(input[2], input[1], input[3], tempList.get(index2)[2], tempList.get(index2)[1], tempList.get(index2)[3]) / 1000;
						double latency = dis * 0.01;
						links.println("<link>\n" + 
					    		"		<name>L" + tempList.get(index2)[0] + "_" + input[0] + "</name>\n" + 
						   		"		<left>\n" + 
					    		"			<x_pos>" + input[1] + "</x_pos>\n" + 
						   		"			<y_pos>" + input[2] + "</y_pos>\n" +
						   		"			<altitude>" + input[3] + "</altitude>" +
						   		"		</left>\n" + 
						   		"		<right>\n" + 
						    	"			<x_pos>" + tempList.get(index2)[1] + "</x_pos>\n" + 
						   		"			<y_pos>" + tempList.get(index2)[2] + "</y_pos>\n" +
						   		"			<altitude>" + tempList.get(index2)[3] + "</altitude>" +
						   		"		</right>\n" + 
						   		"		<left_latency>"+latency+"</left_latency>\n" + 
						   		"		<right_latency>"+latency+"</right_latency>\n" + 
						   		"	</link>");
						
						
						}
				}
			}

			// Save the list of universities.
			if(i == 2) { 
				universitiesList.clear();
				for(Double[] input : tempList) 	{
					universitiesList.add(new Double[] {(double)input[0], (double)input[1], (double)input[2], (double)input[3]});
				}
				universitiesCircle.clear();
			}

			// Qian - create universities circle to let Connect centers and Schools to connect to nearest University / Ward / Library.
			if(i == 2 || i == 3 || i == 4) { 
				for(Double[] input : tempList) 	{
					universitiesCircle.add(new Double[] {(double)input[0], (double)input[1], (double)input[2], (double)input[3]});
				}
			}
			
			// If the next set of nodes are Wards / Libraries, link to nearest university.
			if (i == 2 || i==3) { 
				nodeList.clear();
				for(Double[] input : universitiesList) 	{
					nodeList.add(new Double[] {(double)input[0], (double)input[1], (double)input[2], (double)input[3]});
				}
			}

			// If the next set of nodes are Connect centers / Schools, use universities circle as next higher layer.
			else if (i == 4 || i==5) { 
				nodeList.clear();
				for(Double[] input : universitiesCircle) 	{
					nodeList.add(new Double[] {(double)input[0], (double)input[1], (double)input[2], (double)input[3]});
				}
			}
			
			// else link to a nearest node of next higher layer
			else{
				nodeList.clear();
				for(Double[] input : tempList) 	{
					nodeList.add(new Double[] {(double)input[0], (double)input[1], (double)input[2], (double)input[3]});
				}
			}
			
			// Prepare to process info for next layer fog nodes
			tempList.clear();
			

		}// end - for MAX_LEVELS
		
		node.println("</edge_devices>");
		links.println("</links>");
		node.close();
		links.close();

		
		return;
	}
	
	
	/**
	 * Constructor
	 * @throws IOException
	 */
	public DataInterpreter() throws IOException {
		initialize();
		readFile();
	}
	
	
	/**
	 * Get simulation space rectangular border coordinates.
	 * @return
	 */
	public static double[] getSimulationSpace()
	{
		return new double[] {MIN_LONG, MAX_LONG, MIN_LAT, MAX_LAT}; 
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static int getMaxLevels() {
		return MAX_LEVELS;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static String getInputType() {
		return inputType;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static boolean areMobileDevicesMoving() {
		return movingMobileDevices;
	}
	
	
	/**
	 * Initialize() method.
	 * the great beast...<br><br>
	 * 
	 * this is where we define the specs for the machines on the network.<br>
	 * if you have a file to read these from go ahead, we, however, are defining them by hand.<br>
	 * the first dimension of the array represents the fog layer<br>
	 * the second dimension represents the particular attribute<br><br>
	 * 
	 *   0 - architecture<br>
	 *   1 - operating systems<br>
	 *   2 - virtual machine manager<br>
	 *   3 - cost per Bandwidth<br>
	 *   4 - cost per second<br>
	 *   5 - cost per memory -- we don't actually use this<br> 
	 *   6 - cost per storage -- we don't actually use this<br> 
	 *   7 - is node a wifi access point<br>
	 *   8 - is fog node moving<br>
	 *   9 - number of cores for the machine<br>
	 *  10 - million instructions per second (mips)<br>
	 *  11 - ram<br>
	 *  12 - storage
	 *  13 - bandwidth - Kbps
	 *  14 - idle power consumption of router (watt)<br>
	 *  15 - energy for downloads (nJ/bit)<br>
	 *  16 - energy for uploads (nJ/bit)<br>. IF DOWNLOAD AND UPLOAD nJ/bit BECOME DIFFERENT FOR ANY LAYER, CHANGE getDownloadEnergy in EnergyModel.java
	 *  17 - max power consumption of router (watt)<br>
	 *  18 - idle power consumption of fog node (watt)<br>
	 *  19 - max power consumption of fog node (watt)<br>
	 */
	public static void initialize() {
		double tenGbRouterCost = 151.67/2692915200.0 * 100; // $/Mb numbers taken from cisco ASR 901 10G router at $151.67 per month
		double oneGbRouterCost = 88.23/269291520.0 * 100; // $/Mb numbers taken from cisco ASR 901 1G router at $88.23 per month
		double hundredGbRouterCost = 646.51/26929152000.0 * 100; // $/Mb numbers taken from cisco ASR 1013 100G router at $646.51 per month
		// Shaik modified - Multiplied above three costs for routers' data transfer by 1000 to reflect the service provider costs & profit, in addition to router monthly lease fee. 
		
		try {

			String propertiesFile = "scripts/sample_application/config/default_config.properties";
			InputStream input = new FileInputStream(propertiesFile);
			Properties prop = new Properties();
			prop.load(input);
			nodeSpecs[MAX_LEVELS - 1][0] = prop.getProperty("nodeSpecs_1_0");
			nodeSpecs[MAX_LEVELS - 1][1] = prop.getProperty("nodeSpecs_1_1");
			nodeSpecs[MAX_LEVELS - 1][2] = prop.getProperty("nodeSpecs_1_2");
			nodeSpecs[MAX_LEVELS - 1][3] = hundredGbRouterCost + "";
			nodeSpecs[MAX_LEVELS - 1][4] = prop.getProperty("nodeSpecs_1_4") + ""; // Shaik modified to half of city center's cost (low cost due to scale). prev = "0.000014"
			nodeSpecs[MAX_LEVELS - 1][5] = prop.getProperty("nodeSpecs_1_5");
			nodeSpecs[MAX_LEVELS - 1][6] = prop.getProperty("nodeSpecs_1_6");
			nodeSpecs[MAX_LEVELS - 1][7] = "true";
			nodeSpecs[MAX_LEVELS - 1][8] = Boolean.toString(SimSettings.getInstance().isMOVING_CLOUD());
			nodeSpecs[MAX_LEVELS - 1][9] = prop.getProperty("nodeSpecs_1_9"); // Shaik modified to 1/100th - prev = 2867200
			nodeSpecs[MAX_LEVELS - 1][10] = prop.getProperty("nodeSpecs_1_10"); // Shaik modified to 1/100th (52224000) - prev = 4874240000 // same m/c as that as WARD
			nodeSpecs[MAX_LEVELS - 1][11] = prop.getProperty("nodeSpecs_1_11");
			nodeSpecs[MAX_LEVELS - 1][12] = prop.getProperty("nodeSpecs_1_12");
			nodeSpecs[MAX_LEVELS - 1][13] = prop.getProperty("nodeSpecs_1_13"); // Shaik modified to 1/100th - prev = 104857600 // Shaik fixed back to 100% value
			nodeSpecs[MAX_LEVELS - 1][14] = prop.getProperty("nodeSpecs_1_14"); // Cameron and Matthew modified to add idle power (watt)
			nodeSpecs[MAX_LEVELS - 1][15] = prop.getProperty("nodeSpecs_1_15"); // Cameron and Matthew modified to add energy for downloads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 1][16] = prop.getProperty("nodeSpecs_1_16"); // Cameron and Matthew modified to add energy for uploads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 1][17] = prop.getProperty("nodeSpecs_1_17"); // Cameron and Matthew modified to add max power consumption (watt)
			nodeSpecs[MAX_LEVELS - 1][18] = prop.getProperty("nodeSpecs_1_18"); // Cameron and Matthew modified to add idle power consumption (watt)
			nodeSpecs[MAX_LEVELS - 1][19] = prop.getProperty("nodeSpecs_1_19"); // Cameron and Matthew modified to add max power consumption (watt)

			nodeSpecs[MAX_LEVELS - 2][0] = prop.getProperty("nodeSpecs_1_0");
			nodeSpecs[MAX_LEVELS - 2][1] = prop.getProperty("nodeSpecs_1_1");
			nodeSpecs[MAX_LEVELS - 2][2] = prop.getProperty("nodeSpecs_1_2");
			nodeSpecs[MAX_LEVELS - 2][3] = hundredGbRouterCost + "";
			nodeSpecs[MAX_LEVELS - 2][4] = prop.getProperty("nodeSpecs_1_4"); // Shaik modified - prev = "0.037"
			nodeSpecs[MAX_LEVELS - 2][5] = prop.getProperty("nodeSpecs_1_5");
			nodeSpecs[MAX_LEVELS - 2][6] = prop.getProperty("nodeSpecs_1_6");
			nodeSpecs[MAX_LEVELS - 2][7] = prop.getProperty("nodeSpecs_1_7");
			nodeSpecs[MAX_LEVELS - 2][8] = Boolean.toString(SimSettings.getInstance().isMOVING_CITY_HALL());
			nodeSpecs[MAX_LEVELS - 2][9] = prop.getProperty("nodeSpecs_1_9"); // Shaik modified to 1/100th - prev = 28672
			nodeSpecs[MAX_LEVELS - 2][10] = prop.getProperty("nodeSpecs_1_10"); // Shaik modified to 1/100th (522240) - prev = 48742400 // same m/c as that as WARD
			nodeSpecs[MAX_LEVELS - 2][11] = prop.getProperty("nodeSpecs_1_11");
			nodeSpecs[MAX_LEVELS - 2][12] = prop.getProperty("nodeSpecs_1_12");
			nodeSpecs[MAX_LEVELS - 2][13] = prop.getProperty("nodeSpecs_1_13"); // Shaik modified to 1/100th - prev = 104857600 // Shaik fixed back to 100% value
			nodeSpecs[MAX_LEVELS - 2][14] = prop.getProperty("nodeSpecs_1_14"); // Cameron and Matthew modified to add idle power (watt)
			nodeSpecs[MAX_LEVELS - 2][15] = prop.getProperty("nodeSpecs_1_15"); // Cameron and Matthew modified to add energy for downloads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 2][16] = prop.getProperty("nodeSpecs_1_16"); // Cameron and Matthew modified to add energy for uploads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 2][17] = prop.getProperty("nodeSpecs_1_17"); // Cameron and Matthew modified to add max power consumption (watt)
			nodeSpecs[MAX_LEVELS - 2][18] = prop.getProperty("nodeSpecs_1_18"); // Cameron and Matthew modified to add idle power consumption (watt)
			nodeSpecs[MAX_LEVELS - 2][19] = prop.getProperty("nodeSpecs_1_19"); // Cameron and Matthew modified to add max power consumption (watt)
			
			nodeSpecs[MAX_LEVELS - 3][0] = prop.getProperty("nodeSpecs_3_0");
			nodeSpecs[MAX_LEVELS - 3][1] = prop.getProperty("nodeSpecs_3_1");
			nodeSpecs[MAX_LEVELS - 3][2] = prop.getProperty("nodeSpecs_3_2");
			nodeSpecs[MAX_LEVELS - 3][3] = tenGbRouterCost + "";
			nodeSpecs[MAX_LEVELS - 3][4] = prop.getProperty("nodeSpecs_3_4"); // Shaik modified - prev = "0.0093"
			nodeSpecs[MAX_LEVELS - 3][5] = prop.getProperty("nodeSpecs_3_5");
			nodeSpecs[MAX_LEVELS - 3][6] = prop.getProperty("nodeSpecs_3_6");
			nodeSpecs[MAX_LEVELS - 3][7] = prop.getProperty("nodeSpecs_3_7");
			nodeSpecs[MAX_LEVELS - 3][8] = Boolean.toString(SimSettings.getInstance().isMOVING_UNIVERSITY());
			nodeSpecs[MAX_LEVELS - 3][9] = prop.getProperty("nodeSpecs_3_9"); // Shaik modified to 1/100th - prev = 7168
			nodeSpecs[MAX_LEVELS - 3][10] = prop.getProperty("nodeSpecs_3_10"); // Shaik modified to 1/100th (130560) - prev = 12185600 // same m/c as that as WARD
			nodeSpecs[MAX_LEVELS - 3][11] =prop.getProperty("nodeSpecs_3_11");
			nodeSpecs[MAX_LEVELS - 3][12] = prop.getProperty("nodeSpecs_3_12");
			nodeSpecs[MAX_LEVELS - 3][13] = prop.getProperty("nodeSpecs_3_13"); // Shaik modified to 1/100th - prev = 10485760 // Shaik fixed back to 100% value
			nodeSpecs[MAX_LEVELS - 3][14] = prop.getProperty("nodeSpecs_3_14"); // Cameron and Matthew modified to add idle power (watt)
			nodeSpecs[MAX_LEVELS - 3][15] = prop.getProperty("nodeSpecs_3_15"); // Cameron and Matthew modified to add energy for downloads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 3][16] = prop.getProperty("nodeSpecs_3_16"); // Cameron and Matthew modified to add energy for uploads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 3][17] = prop.getProperty("nodeSpecs_3_17"); // Cameron and Matthew modified to add max power consumption (watt)
			nodeSpecs[MAX_LEVELS - 3][18] = prop.getProperty("nodeSpecs_3_18"); // Cameron and Matthew modified to add idle power consumption (watt)
			nodeSpecs[MAX_LEVELS - 3][19] =prop.getProperty("nodeSpecs_3_19"); // Cameron and Matthew modified to add max power consumption (watt)
			
			nodeSpecs[MAX_LEVELS - 4][0] = prop.getProperty("nodeSpecs_4_0");
			nodeSpecs[MAX_LEVELS - 4][1] = prop.getProperty("nodeSpecs_4_1");
			nodeSpecs[MAX_LEVELS - 4][2] = prop.getProperty("nodeSpecs_4_2");
			nodeSpecs[MAX_LEVELS - 4][3] = tenGbRouterCost + "";
			nodeSpecs[MAX_LEVELS - 4][4] = prop.getProperty("nodeSpecs_4_4"); // Shaik modified - prev = "0.0336"
			nodeSpecs[MAX_LEVELS - 4][5] = prop.getProperty("nodeSpecs_4_5");
			nodeSpecs[MAX_LEVELS - 4][6] = prop.getProperty("nodeSpecs_4_6");
			nodeSpecs[MAX_LEVELS - 4][7] = prop.getProperty("nodeSpecs_4_7");
			nodeSpecs[MAX_LEVELS - 4][8] = Boolean.toString(SimSettings.getInstance().isMOVING_WARD());
			nodeSpecs[MAX_LEVELS - 4][9] = prop.getProperty("nodeSpecs_4_9"); // Shaik modified to 1/100th - prev = 768
			nodeSpecs[MAX_LEVELS - 4][10] = prop.getProperty("nodeSpecs_4_10"); // Shaik modified to 1/100th - prev = 1305600
			nodeSpecs[MAX_LEVELS - 4][11] = prop.getProperty("nodeSpecs_4_11");
			nodeSpecs[MAX_LEVELS - 4][12] = prop.getProperty("nodeSpecs_4_12");
			nodeSpecs[MAX_LEVELS - 4][13] = prop.getProperty("nodeSpecs_4_13"); // Shaik modified to 1/100th - prev = 10485760 // Shaik fixed back to 100% value
			nodeSpecs[MAX_LEVELS - 4][14] = prop.getProperty("nodeSpecs_4_14"); // Cameron and Matthew modified to add idle power (watt)
			nodeSpecs[MAX_LEVELS - 4][15] = prop.getProperty("nodeSpecs_4_15"); // Cameron and Matthew modified to add energy for downloads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 4][16] = prop.getProperty("nodeSpecs_4_16"); // Cameron and Matthew modified to add energy for uploads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 4][17] = prop.getProperty("nodeSpecs_4_17"); // Cameron and Matthew modified to add max power consumption (watt)
			nodeSpecs[MAX_LEVELS - 4][18] = prop.getProperty("nodeSpecs_4_18"); // Cameron and Matthew modified to add idle power consumption (watt)
			nodeSpecs[MAX_LEVELS - 4][19] = prop.getProperty("nodeSpecs_4_19"); // Cameron and Matthew modified to add max power consumption (watt)
			
			nodeSpecs[MAX_LEVELS - 5][0] = prop.getProperty("nodeSpecs_5_0");
			nodeSpecs[MAX_LEVELS - 5][1] = prop.getProperty("nodeSpecs_5_1");
			nodeSpecs[MAX_LEVELS - 5][2] = prop.getProperty("nodeSpecs_5_2");
			nodeSpecs[MAX_LEVELS - 5][3] = tenGbRouterCost + "";
			nodeSpecs[MAX_LEVELS - 5][4] = prop.getProperty("nodeSpecs_5_4"); // Shaik modified - prev = "0.00016"
			nodeSpecs[MAX_LEVELS - 5][5] = prop.getProperty("nodeSpecs_5_5");
			nodeSpecs[MAX_LEVELS - 5][6] = prop.getProperty("nodeSpecs_5_6");
			nodeSpecs[MAX_LEVELS - 5][7] = prop.getProperty("nodeSpecs_5_7");
			nodeSpecs[MAX_LEVELS - 5][8] = Boolean.toString(SimSettings.getInstance().isMOVING_LIBRARY());
			nodeSpecs[MAX_LEVELS - 5][9] = prop.getProperty("nodeSpecs_5_9"); // Shaik modified to 1/100th - prev = 192
			nodeSpecs[MAX_LEVELS - 5][10] = prop.getProperty("nodeSpecs_5_10"); // Shaik modified to 1/100th - prev = 326400
			nodeSpecs[MAX_LEVELS - 5][11] = prop.getProperty("nodeSpecs_5_11");
			nodeSpecs[MAX_LEVELS - 5][12] = prop.getProperty("nodeSpecs_5_12");
			nodeSpecs[MAX_LEVELS - 5][13] = prop.getProperty("nodeSpecs_5_13"); // Shaik modified to 1/100th - prev = 10485760 // Shaik fixed back to 100% value
			nodeSpecs[MAX_LEVELS - 5][14] = prop.getProperty("nodeSpecs_5_14"); // Cameron and Matthew modified to add idle power (watt)
			nodeSpecs[MAX_LEVELS - 5][15] = prop.getProperty("nodeSpecs_5_15"); // Cameron and Matthew modified to add energy for downloads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 5][16] = prop.getProperty("nodeSpecs_5_16"); // Cameron and Matthew modified to add energy for uploads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 5][17] = prop.getProperty("nodeSpecs_5_17"); // Cameron and Matthew modified to add max power consumption (watt)
			nodeSpecs[MAX_LEVELS - 5][18] = prop.getProperty("nodeSpecs_5_18"); // Cameron and Matthew modified to add idle power consumption (watt)
			nodeSpecs[MAX_LEVELS - 5][19] = prop.getProperty("nodeSpecs_5_19"); // Cameron and Matthew modified to add max power consumption (watt)
			
			nodeSpecs[MAX_LEVELS - 6][0] = prop.getProperty("nodeSpecs_6_0");
			nodeSpecs[MAX_LEVELS - 6][1] = prop.getProperty("nodeSpecs_6_1");
			nodeSpecs[MAX_LEVELS - 6][2] = prop.getProperty("nodeSpecs_6_2");
			nodeSpecs[MAX_LEVELS - 6][3] = oneGbRouterCost + "";
			nodeSpecs[MAX_LEVELS - 6][4] = prop.getProperty("nodeSpecs_6_4"); // Shaik modified - prev = "0.0012"
			nodeSpecs[MAX_LEVELS - 6][5] = prop.getProperty("nodeSpecs_6_5");
			nodeSpecs[MAX_LEVELS - 6][6] = prop.getProperty("nodeSpecs_6_6");
			nodeSpecs[MAX_LEVELS - 6][7] = prop.getProperty("nodeSpecs_6_7");
			nodeSpecs[MAX_LEVELS - 6][8] = Boolean.toString(SimSettings.getInstance().isMOVING_COMMUNITY_CENTER());
			nodeSpecs[MAX_LEVELS - 6][9] = prop.getProperty("nodeSpecs_6_9"); // Shaik modified to 1/100th - prev = 128
			nodeSpecs[MAX_LEVELS - 6][10] = prop.getProperty("nodeSpecs_6_10"); // Shaik modified to 1/100th - prev = 217600
			nodeSpecs[MAX_LEVELS - 6][11] = prop.getProperty("nodeSpecs_6_11");
			nodeSpecs[MAX_LEVELS - 6][12] = prop.getProperty("nodeSpecs_6_12");
			nodeSpecs[MAX_LEVELS - 6][13] = prop.getProperty("nodeSpecs_6_13"); // Shaik modified to 1/100th - prev = 1048576 // Shaik fixed back to 100% value
			nodeSpecs[MAX_LEVELS - 6][14] = prop.getProperty("nodeSpecs_6_14"); // Cameron and Matthew modified to add idle power (watt)
			nodeSpecs[MAX_LEVELS - 6][15] = prop.getProperty("nodeSpecs_6_15"); // Cameron and Matthew modified to add energy for downloads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 6][16] = prop.getProperty("nodeSpecs_6_16"); // Cameron and Matthew modified to add energy for uploads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 6][17] = prop.getProperty("nodeSpecs_6_17"); // Cameron and Matthew modified to add max power consumption (watt)
			nodeSpecs[MAX_LEVELS - 6][18] = prop.getProperty("nodeSpecs_6_18"); // Cameron and Matthew modified to add idle power consumption (watt)
			nodeSpecs[MAX_LEVELS - 6][19] = prop.getProperty("nodeSpecs_6_19"); // Cameron and Matthew modified to add max power consumption (watt)
			
			nodeSpecs[MAX_LEVELS - 7][0] = prop.getProperty("nodeSpecs_7_0");
			nodeSpecs[MAX_LEVELS - 7][1] = prop.getProperty("nodeSpecs_7_1");
			nodeSpecs[MAX_LEVELS - 7][2] = prop.getProperty("nodeSpecs_7_2");
			nodeSpecs[MAX_LEVELS - 7][3] = oneGbRouterCost + "";
			nodeSpecs[MAX_LEVELS - 7][4] = prop.getProperty("nodeSpecs_7_4"); // Shaik modified - prev = "0.0003"
			nodeSpecs[MAX_LEVELS - 7][5] = prop.getProperty("nodeSpecs_7_5");
			nodeSpecs[MAX_LEVELS - 7][6] = prop.getProperty("nodeSpecs_7_6");
			nodeSpecs[MAX_LEVELS - 7][7] = prop.getProperty("nodeSpecs_7_7");
			nodeSpecs[MAX_LEVELS - 7][8] = Boolean.toString(SimSettings.getInstance().isMOVING_SCHOOL());
			nodeSpecs[MAX_LEVELS - 7][9] =prop.getProperty("nodeSpecs_7_9"); // Shaik modified to 1/100th - prev = 32
			nodeSpecs[MAX_LEVELS - 7][10] = prop.getProperty("nodeSpecs_7_10"); // Shaik modified to 1/100th - prev = 54400
			nodeSpecs[MAX_LEVELS - 7][11] = prop.getProperty("nodeSpecs_7_11");
			nodeSpecs[MAX_LEVELS - 7][12] = prop.getProperty("nodeSpecs_7_12");
			nodeSpecs[MAX_LEVELS - 7][13] = prop.getProperty("nodeSpecs_7_13"); // Shaik modified to 1/100th - prev = 1048576 // Shaik fixed back to 100% value
			nodeSpecs[MAX_LEVELS - 7][14] =prop.getProperty("nodeSpecs_7_14"); // Cameron and Matthew modified to add idle power (watt)
			nodeSpecs[MAX_LEVELS - 7][15] = prop.getProperty("nodeSpecs_7_15"); // Cameron and Matthew modified to add energy for downloads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 7][16] = prop.getProperty("nodeSpecs_7_16"); // Cameron and Matthew modified to add energy for uploads (nJ/bit)
			nodeSpecs[MAX_LEVELS - 7][17] = prop.getProperty("nodeSpecs_7_17"); // Cameron and Matthew modified to add max power consumption (watt)
			nodeSpecs[MAX_LEVELS - 7][18] = prop.getProperty("nodeSpecs_7_18"); // Cameron and Matthew modified to add idle power consumption (watt)
			nodeSpecs[MAX_LEVELS - 7][19] = prop.getProperty("nodeSpecs_7_19"); // Cameron and Matthew modified to add max power consumption (watt)
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	
	/**
	 * @return the mAX_LEVELS
	 */
	public static int getMAX_LEVELS() {
		return MAX_LEVELS;
	}

	
	/**
	 * @param mAX_LEVELS the mAX_LEVELS to set
	 */
	public static void setMAX_LEVELS(int mAX_LEVELS) {
		MAX_LEVELS = mAX_LEVELS;
	}

	
	/**
	 * @return the files
	 */
	public static String[] getFiles() {
		return files;
	}

	
	/**
	 * @param files the files to set
	 */
	public static void setFiles(String[] files) {
		DataInterpreter.files = files;
	}

	
	/**
	 * @return the nodeSpecs
	 */
	public static String[][] getNodeSpecs() {
		return nodeSpecs;
	}

	
	/**
	 * @param nodeSpecs the nodeSpecs to set
	 */
	public static void setNodeSpecs(String[][] nodeSpecs) {
		DataInterpreter.nodeSpecs = nodeSpecs;
	}

	
	/**
	 * @return the nodeList
	 */
	public static ArrayList<Double[]> getNodeList() {
		return nodeList;
	}

	
	/**
	 * @param nodeList the nodeList to set
	 */
	public static void setNodeList(ArrayList<Double[]> nodeList) {
		DataInterpreter.nodeList = nodeList;
	}

	
	/**
	 * @return the tempList
	 */
	public static ArrayList<Double[]> getTempList() {
		return tempList;
	}

	
	/**
	 * @param tempList the tempList to set
	 */
	public static void setTempList(ArrayList<Double[]> tempList) {
		DataInterpreter.tempList = tempList;
	}

	
	/**
	 * @return the universitiesCircle
	 */
	public static ArrayList<Double[]> getUniversitiesCircle() {
		return universitiesCircle;
	}

	
	/**
	 * @param universitiesCircle the universitiesCircle to set
	 */
	public static void setUniversitiesCircle(ArrayList<Double[]> universitiesCircle) {
		DataInterpreter.universitiesCircle = universitiesCircle;
	}

	
	/**
	 * @return the mIN_LAT
	 */
	public static double getMIN_LAT() {
		return MIN_LAT;
	}

	
	/**
	 * @param mIN_LAT the mIN_LAT to set
	 */
	public static void setMIN_LAT(double mIN_LAT) {
		MIN_LAT = mIN_LAT;
	}

	
	/**
	 * @return the mAX_LAT
	 */
	public static double getMAX_LAT() {
		return MAX_LAT;
	}

	
	/**
	 * @param mAX_LAT the mAX_LAT to set
	 */
	public static void setMAX_LAT(double mAX_LAT) {
		MAX_LAT = mAX_LAT;
	}

	
	/**
	 * @return the mIN_LONG
	 */
	public static double getMIN_LONG() {
		return MIN_LONG;
	}

	
	/**
	 * @param mIN_LONG the mIN_LONG to set
	 */
	public static void setMIN_LONG(double mIN_LONG) {
		MIN_LONG = mIN_LONG;
	}

	
	/**
	 * @return the mAX_LONG
	 */
	public static double getMAX_LONG() {
		return MAX_LONG;
	}

	
	/**
	 * @param mAX_LONG the mAX_LONG to set
	 */
	public static void setMAX_LONG(double mAX_LONG) {
		MAX_LONG = mAX_LONG;
	}

	
	/**
	 * @return the universitiesYet
	 */
	public static boolean isUniversitiesYet() {
		return universitiesYet;
	}

	
	/**
	 * @param universitiesYet the universitiesYet to set
	 */
	public static void setUniversitiesYet(boolean universitiesYet) {
		DataInterpreter.universitiesYet = universitiesYet;
	}

	
	/**
	 * @return the universitiesLinked
	 */
	public static boolean isUniversitiesLinked() {
		return universitiesLinked;
	}

	
	/**
	 * @param universitiesLinked the universitiesLinked to set
	 */
	public static void setUniversitiesLinked(boolean universitiesLinked) {
		DataInterpreter.universitiesLinked = universitiesLinked;
	}

	
	/**
	 * @return the movingMobileDevices
	 */
	public static boolean isMovingMobileDevices() {
		return movingMobileDevices;
	}

	
	/**
	 * @param movingMobileDevices the movingMobileDevices to set
	 */
	public static void setMovingMobileDevices(boolean movingMobileDevices) {
		DataInterpreter.movingMobileDevices = movingMobileDevices;
	}

	
	/**
	 * @return the xmlFile
	 */
	public File getXmlFile() {
		return xmlFile;
	}

	
	/**
	 * @param xmlFile the xmlFile to set
	 */
	public void setXmlFile(File xmlFile) {
		this.xmlFile = xmlFile;
	}

	
	/**
	 * @return the xmlFW
	 */
	public FileWriter getXmlFW() {
		return xmlFW;
	}

	
	/**
	 * @param xmlFW the xmlFW to set
	 */
	public void setXmlFW(FileWriter xmlFW) {
		this.xmlFW = xmlFW;
	}

	
	/**
	 * @return the xmlBR
	 */
	public BufferedWriter getXmlBR() {
		return xmlBR;
	}

	
	/**
	 * @param xmlBR the xmlBR to set
	 */
	public void setXmlBR(BufferedWriter xmlBR) {
		this.xmlBR = xmlBR;
	}

	
	/**
	 * @param inputType the inputType to set
	 */
	public static void setInputType(String inputType) {
		DataInterpreter.inputType = inputType;
	}
}
