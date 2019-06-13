package edu.auburn.pFogSim.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.utils.SimLogger;


/**
 * 
 * @author szs0117
 *
 */
public class DataInterpreter {
	private static int MAX_LEVELS = 7;
	private static String[] files= {
			"Google_Cloud_DC.csv", 
			"Chicago_CityHall.csv", 
			"Chicago_Universities.csv", 
			"Chicago_Wards.csv", 
			"Chicago_Libraries.csv", 
			"Chicago_Connect.csv", 
			"Chicago_Schools.csv"};
	private static String[][] nodeSpecs = new String[MAX_LEVELS][14];// the specs for all layers of the fog devices
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
		String[] nodeLoc = new String[3];
		Double[] temp = new Double[3];
		int counter = 0;
		int prevCounter = 0;
		for(int i = 0; i < MAX_LEVELS; i++)
		{
			
			try {
				dataFR = new FileReader(files[i]);
				dataBR = new BufferedReader(dataFR);
			}
			catch (FileNotFoundException e) {
				//SimLogger.printLine("Bad File Name");
			}
			dataBR.readLine(); //Gets rid of title data
			while(dataBR.ready()) {

				////SimLogger.printLine("Importing " + files[i]);
				rawNode = dataBR.readLine();
				nodeLoc = rawNode.split(",");
				temp[0] = (double)counter; //ID
				temp[2] = Double.parseDouble(nodeLoc[1]); //Y Coord
				temp[1] = Double.parseDouble(nodeLoc[2]); //X Coord
				if(MAX_LONG == -100000 || temp[1] > MAX_LONG)	MAX_LONG = temp[1];		
				if(MIN_LONG == -100000 || temp[1] < MIN_LONG)	MIN_LONG = temp[1];	
				if(MAX_LAT == -100000 || temp[2] > MAX_LAT)	MAX_LAT = temp[2];	
				if(MIN_LAT == -100000 || temp[2] < MIN_LAT)	MIN_LAT = temp[2];	
				
				//Add to output file		    
			    node.println(String.format("<datacenter arch=\"%s\" os=\"%s\" vmm=\"%s\">\n", nodeSpecs[MAX_LEVELS - i - 1][0], nodeSpecs[MAX_LEVELS - i - 1][1], nodeSpecs[MAX_LEVELS - i - 1][2]));
			    node.println(String.format("<costPerBw>%s</costPerBw>\n\t<costPerSec>%s</costPerSec>\n\t<costPerMem>%s</costPerMem>\n\t<costPerStorage>%s</costPerStorage>", nodeSpecs[MAX_LEVELS - i - 1][3], nodeSpecs[MAX_LEVELS - i - 1][4], nodeSpecs[MAX_LEVELS - i - 1][5], nodeSpecs[MAX_LEVELS - i - 1][6]));
			    //Qian change level start from 1
			    node.println(String.format("<location>\n\t<x_pos>%s</x_pos>\n\t<y_pos>%s</y_pos>\n\t<level>%s</level>\t<wlan_id>%s</wlan_id>\n\t<wap>%s</wap>\n\t<moving>%s</moving>\n\t<bandwidth>%s</bandwidth>/n</location>", nodeLoc[2], nodeLoc[1], MAX_LEVELS - i, counter, nodeSpecs[MAX_LEVELS - i - 1][7], nodeSpecs[MAX_LEVELS - i - 1][8], nodeSpecs[MAX_LEVELS - i - 1][13]));
			    node.println(String.format("<host>\n\t<core>%s</core>\n\t<mips>%s</mips>\n\t<ram>%s</ram>\n\t<storage>%s</storage>\n", nodeSpecs[MAX_LEVELS - i - 1][9], nodeSpecs[MAX_LEVELS - i - 1][10], nodeSpecs[MAX_LEVELS - i - 1][11], nodeSpecs[MAX_LEVELS - i - 1][12]));
			    node.println(String.format("\t<VM vmm=\"%s\">\n\t\t\t<core>%s</core>\n\t\t\t<mips>%s</mips>\n\t\t\t<ram>%s</ram>\n\t\t\t<storage>%s</storage>\n\t\t</VM>\n\t</host>\n</datacenter>", nodeSpecs[MAX_LEVELS - i - 1][2], nodeSpecs[MAX_LEVELS - i - 1][9], nodeSpecs[MAX_LEVELS - i - 1][10], nodeSpecs[MAX_LEVELS - i - 1][11], nodeSpecs[MAX_LEVELS - i - 1][12]));
	
				if(i == 2) { 
						//SimLogger.printLine("University Fog node Id (prior to list add): "+counter+" Lat: "+temp[1]+" Lon: "+temp[2]);
				}
				//SimLogger.printLine("");

				
			    if (counter == 643) {
			    	//SimLogger.print("");
			    }
				//Make link to previous closest node on higher level
				if(!nodeList.isEmpty())
				{
					double minDistance = Double.MAX_VALUE;
					int index = -1;
					double distance = 0;
					//Go through all nodes one level up and find the closest
					for(int j = 0; j < nodeList.size(); j++)
					{
						//SimLogger.printLine("Layer: "+(i+1)+"    nodeList.size = " + nodeList.size());

						distance = measure(nodeList.get(j)[2], nodeList.get(j)[1], temp[2], temp[1]);
						//SimLogger.print("\nFog node Id: "+counter+" - Layer i: "+i+" - Parent Node Id: "+nodeList.get(j)[0]+" - Distance: " + distance);
						if(distance < minDistance)
						{
							minDistance = distance;
							index = j;
							//SimLogger.print(" - New min distance: " + minDistance);
						}
					}
					//SimLogger.print("\n\n\n");
					minDistance = Double.MAX_VALUE;
					if(index >= 0)
					{
						if(nodeList.get(index).equals(temp)) 
						{
							//SimLogger.printLine("Yep, they're the same thing");
							System.exit(0);
						}
						double dis = measure(temp[2], temp[1], nodeList.get(index)[2], nodeList.get(index)[1]) / 1000;
						double latency = dis * 0.01;
						links.println("<link>\n" + 
					    		"		<name>L" + nodeList.get(index)[0] + "_" + temp[0] + "</name>\n" + 
					    		"		<left>\n" + 
					    		"			<x_pos>" + temp[1] + "</x_pos>\n" + 
					    		"			<y_pos>" + temp[2] + "</y_pos>\n" + 
					    		"		</left>\n" + 
					    		"		<right>\n" + 
					    		"			<x_pos>" + nodeList.get(index)[1] + "</x_pos>\n" + 
					    		"			<y_pos>" + nodeList.get(index)[2] + "</y_pos>\n" + 
					    		"		</right>\n" + 
					    		"		<left_latency>" + latency + "</left_latency>\n" + 
					    		"		<right_latency>" + latency + "</right_latency>\n" + 
					    		"	</link>");
						
						//SimLogger.printLine("Link: "+nodeList.get(index)[0]+" - "+temp[0]);
						
					}
				}

				if(i == 2) { 
				//	SimLogger.printLine("University Fog node Id (just before list add): "+counter+" Lat: "+temp[1]+" Lon: "+temp[2]);
					//for (int kk=0; kk<tempList.size(); kk++)
					//	SimLogger.printLine("University Fog node Id: "+tempList.get(kk)[0]+" Lat: "+tempList.get(kk)[1]+" Lon: "+tempList.get(kk)[2]);
				}

				tempList.add(new Double[] {(double)temp[0], (double)temp[1], (double)temp[2]});
				//tempList.add(temp);				
				counter++;
				
				if(i == 2) { 
				//	SimLogger.printLine("University Fog node Id (after list add): "+counter+" Lat: "+temp[1]+" Lon: "+temp[2]);
					//for (int kk=0; kk<tempList.size(); kk++)
						//SimLogger.printLine("University Fog node Id: "+tempList.get(kk)[0]+" Lat: "+tempList.get(kk)[1]+" Lon: "+tempList.get(kk)[2]);
				}
				//SimLogger.printLine("");

			}
			
			////SimLogger.printLine("Level : " + i + "\n\t" + prevCounter + " -> " + counter);
			prevCounter = counter;
			////SimLogger.printLine("nodeList" + nodeList.toString());
			////SimLogger.printLine("tempList" + tempList.toString());
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
						//SimLogger.printLine("nodeList.size = " + nodeList.size());

						distance = measure(tempList.get(j)[2], tempList.get(j)[1], input[2], input[1]);
						//SimLogger.print("\nFog node Id (from): "+input[0]+" Fog node Id (to): "+tempList.get(j)[0]+" - Distance: " + distance+" - MinDistance: " + minDistance+" - SecMinDistance: " + secondminDistance);

						if(distance < minDistance && distance != 0)
						{
							secondminDistance = minDistance;
							index2 = index1;
							minDistance = distance;
							index1 = j;
							//SimLogger.print(" - new minDistance: "+minDistance+" - new secondminDistance: "+ secondminDistance);
						}

						else if(distance < secondminDistance && distance != 0)
						{
							secondminDistance = distance;
							index2 = j;
							//SimLogger.print(" - new secondminDistance: "+ secondminDistance);
						}
					}
					minDistance = Double.MAX_VALUE;
					secondminDistance = Double.MAX_VALUE;
					if(index1 >= 0)
					{
						//SimLogger.getInstance().print("Find first min index1: " + index1);
						if(tempList.get(index1).equals(temp)) 
						{
							//SimLogger.printLine("Yep, they're the same thing");
							System.exit(0);
						}
						double dis = measure(input[2], input[1], tempList.get(index1)[2], tempList.get(index1)[1]) / 1000;
						double latency = dis * 0.01;
						links.println("<link>\n" + 
					    		"		<name>L" + tempList.get(index1)[0] + "_" + input[0] + "</name>\n" + 
						   		"		<left>\n" + 
					    		"			<x_pos>" + input[1] + "</x_pos>\n" + 
						   		"			<y_pos>" + input[2] + "</y_pos>\n" + 
						   		"		</left>\n" + 
						   		"		<right>\n" + 
						    	"			<x_pos>" + tempList.get(index1)[1] + "</x_pos>\n" + 
						   		"			<y_pos>" + tempList.get(index1)[2] + "</y_pos>\n" + 
						   		"		</right>\n" + 
						   		"		<left_latency>"+latency+"</left_latency>\n" + 
						   		"		<right_latency>"+latency+"</right_latency>\n" + 
						   		"	</link>");
						
						//SimLogger.printLine("\nLink: "+tempList.get(index1)[0]+" - "+input[0]);

						}
					if(index2 >= 0)
					{
						if(tempList.get(index2).equals(temp)) 
						{
							//SimLogger.printLine("Yep, they're the same thing");
							System.exit(0);
						}
						//SimLogger.getInstance().print("Find second min index2: " + index2);
						double dis = measure(input[2], input[1], tempList.get(index2)[2], tempList.get(index2)[1]) / 1000;
						double latency = dis * 0.01;
						links.println("<link>\n" + 
					    		"		<name>L" + tempList.get(index2)[0] + "_" + input[0] + "</name>\n" + 
						   		"		<left>\n" + 
					    		"			<x_pos>" + input[1] + "</x_pos>\n" + 
						   		"			<y_pos>" + input[2] + "</y_pos>\n" + 
						   		"		</left>\n" + 
						   		"		<right>\n" + 
						    	"			<x_pos>" + tempList.get(index2)[1] + "</x_pos>\n" + 
						   		"			<y_pos>" + tempList.get(index2)[2] + "</y_pos>\n" + 
						   		"		</right>\n" + 
						   		"		<left_latency>"+latency+"</left_latency>\n" + 
						   		"		<right_latency>"+latency+"</right_latency>\n" + 
						   		"	</link>");
						
						//SimLogger.printLine("Link: "+tempList.get(index2)[0]+" - "+input[0]);
						
						}
				}
			}

			// Save the list of universities.
			if(i == 2) { 
				universitiesList.clear();
				for(Double[] input : tempList) 	{
					universitiesList.add(new Double[] {(double)input[0], (double)input[1], (double)input[2]});
					//SimLogger.printLine("University Fog node Id: "+input[0]+" Lat: "+input[1]+" Lon: "+input[2]);
				}
				universitiesCircle.clear();
			}

			// Qian - create universities circle to let Connect centers and Schools to connect to nearest University / Ward / Library.
			if(i == 2 || i == 3 || i == 4) { 
				for(Double[] input : tempList) 	{
					universitiesCircle.add(new Double[] {(double)input[0], (double)input[1], (double)input[2]});
				}
			}
			
			// If the next set of nodes are Wards / Libraries, link to nearest university.
			if (i == 2 || i==3) { 
				nodeList.clear();
				for(Double[] input : universitiesList) 	{
					nodeList.add(new Double[] {(double)input[0], (double)input[1], (double)input[2]});
				}
			}

			// If the next set of nodes are Connect centers / Schools, use universities circle as next higher layer.
			else if (i == 4 || i==5) { 
				nodeList.clear();
				for(Double[] input : universitiesCircle) 	{
					nodeList.add(new Double[] {(double)input[0], (double)input[1], (double)input[2]});
				}
			}
			
			// else link to a nearest node of next higher layer
			else{
				nodeList.clear();
				//nodeList.addAll(tempList);
				for(Double[] input : tempList) 	{
					nodeList.add(new Double[] {(double)input[0], (double)input[1], (double)input[2]});
				}
			}
			
			// Prepare to process info for next layer fog nodes
			tempList.clear();
			
			////SimLogger.printLine("nodeList" + nodeList.toString());
			////SimLogger.printLine("tempList" + tempList.toString());
		}// end - for MAX_LEVELS
		
		node.println("</edge_devices>");
		links.println("</links>");
		node.close();
		links.close();
		//SimLogger.printLine("Distance b/t : 41.975456,-87.71409\t and \t41.985456,-87.71409\n === " + measure(-87.71409,41.975456, -87.71408, 41.975446));
		
		//SimLogger.printLine("Min Long : " + MIN_LONG);
		//SimLogger.printLine("Max Long : " + MAX_LONG);
		//SimLogger.printLine("Min Lat : " + MIN_LAT);
		//SimLogger.printLine("Max Lat : " + MAX_LAT);
		//SimManager.getInstance().setSimulationSpace(MIN_LONG, MAX_LONG, MIN_LAT, MAX_LAT);
		
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
	 */
	public static void initialize() {
		double tenGbRouterCost = 151.67/2692915200.0 * 100; // $/Mb numbers taken from cisco ASR 901 10G router at $151.67 per month
		double oneGbRouterCost = 88.23/269291520.0 * 100; // $/Mb numbers taken from cisco ASR 901 1G router at $88.23 per month
		double hundredGbRouterCost = 646.51/26929152000.0 * 100; // $/Mb numbers taken from cisco ASR 1013 100G router at $646.51 per month
		// Shaik modified - Multiplied above three costs for routers' data transfer by 1000 to reflect the service provider costs & profit, in addition to router monthly lease fee. 
		
		nodeSpecs[MAX_LEVELS - 1][0] = "Cloud";
		nodeSpecs[MAX_LEVELS - 1][1] = "Linux";
		nodeSpecs[MAX_LEVELS - 1][2] = "Xen";
		nodeSpecs[MAX_LEVELS - 1][3] = hundredGbRouterCost + "";
		nodeSpecs[MAX_LEVELS - 1][4] = "0.00659722" + ""; // Shaik modified to half of city center's cost (low cost due to scale). prev = "0.000014"
		nodeSpecs[MAX_LEVELS - 1][5] = "0.05";
		nodeSpecs[MAX_LEVELS - 1][6] = "0.1";
		nodeSpecs[MAX_LEVELS - 1][7] = "true";
		nodeSpecs[MAX_LEVELS - 1][8] = "false";
		nodeSpecs[MAX_LEVELS - 1][9] = "28672"; // Shaik modified to 1/100th - prev = 2867200
		//nodeSpecs[MAX_LEVELS - 1][9] = "500";
		nodeSpecs[MAX_LEVELS - 1][10] = "13056000"; // Shaik modified to 1/100th (52224000) - prev = 4874240000 // same m/c as that as WARD
		nodeSpecs[MAX_LEVELS - 1][11] = "164926744166400";
		//nodeSpecs[MAX_LEVELS - 1][11] = "1500";
		nodeSpecs[MAX_LEVELS - 1][12] = "1046898278400";
		nodeSpecs[MAX_LEVELS - 1][13] = "104857600"; // Shaik modified to 1/100th - prev = 104857600 // Shaik fixed back to 100% value
		nodeSpecs[MAX_LEVELS - 1][14] = "11000"; //Cameron and Matthew modified to add idle power (watt) 
		nodeSpecs[MAX_LEVELS - 1][15] = "12.6"; //Cameron and Matthew modified to add energy for downloads (nJ/bit)
		nodeSpecs[MAX_LEVELS - 1][16] = "12.6"; //	Cameron and Matthew modified to add energy for uploads (nJ/bit)
		
		nodeSpecs[MAX_LEVELS - 2][0] = "City Hall";
		nodeSpecs[MAX_LEVELS - 2][1] = "Linux";
		nodeSpecs[MAX_LEVELS - 2][2] = "Xen";
		nodeSpecs[MAX_LEVELS - 2][3] = hundredGbRouterCost + "";
		nodeSpecs[MAX_LEVELS - 2][4] = "0.01319444"; // Shaik modified - prev = "0.037"
		nodeSpecs[MAX_LEVELS - 2][5] = "0.05";
		nodeSpecs[MAX_LEVELS - 2][6] = "0.1";
		nodeSpecs[MAX_LEVELS - 2][7] = "true";
		nodeSpecs[MAX_LEVELS - 2][8] = "false";
		nodeSpecs[MAX_LEVELS - 2][9] = "286"; // Shaik modified to 1/100th - prev = 28672
		//nodeSpecs[MAX_LEVELS - 2][9] = "500";
		nodeSpecs[MAX_LEVELS - 2][10] = "1305600"; // Shaik modified to 1/100th (522240) - prev = 48742400 // same m/c as that as WARD
		nodeSpecs[MAX_LEVELS - 2][11] = "1649267441664";
		//nodeSpecs[MAX_LEVELS - 2][11] = "1500";
		nodeSpecs[MAX_LEVELS - 2][12] = "10468982784";
		nodeSpecs[MAX_LEVELS - 2][13] = "104857600"; // Shaik modified to 1/100th - prev = 104857600 // Shaik fixed back to 100% value
		nodeSpecs[MAX_LEVELS - 2][14] = "11000"; //Cameron and Matthew modified to add idle power (watt) 
		nodeSpecs[MAX_LEVELS - 2][15] = "12.6"; //Cameron and Matthew modified to add energy for downloads (nJ/bit)
		nodeSpecs[MAX_LEVELS - 2][16] = "12.6"; //	Cameron and Matthew modified to add energy for uploads (nJ/bit)
		
		nodeSpecs[MAX_LEVELS - 3][0] = "University";
		nodeSpecs[MAX_LEVELS - 3][1] = "Linux";
		nodeSpecs[MAX_LEVELS - 3][2] = "Xen";
		nodeSpecs[MAX_LEVELS - 3][3] = tenGbRouterCost + "";
		nodeSpecs[MAX_LEVELS - 3][4] = "0.01319444"; // Shaik modified - prev = "0.0093"
		nodeSpecs[MAX_LEVELS - 3][5] = "0.05";
		nodeSpecs[MAX_LEVELS - 3][6] = "0.1";
		nodeSpecs[MAX_LEVELS - 3][7] = "true";
		nodeSpecs[MAX_LEVELS - 3][8] = "false";
		nodeSpecs[MAX_LEVELS - 3][9] = "71"; // Shaik modified to 1/100th - prev = 7168
		//nodeSpecs[MAX_LEVELS - 3][9] = "500";
		nodeSpecs[MAX_LEVELS - 3][10] = "816000"; // Shaik modified to 1/100th (130560) - prev = 12185600 // same m/c as that as WARD
		nodeSpecs[MAX_LEVELS - 3][11] = "412316860416";
		//nodeSpecs[MAX_LEVELS - 3][11] = "1500";
		nodeSpecs[MAX_LEVELS - 3][12] = "2617245696";
		nodeSpecs[MAX_LEVELS - 3][13] = "10485760"; // Shaik modified to 1/100th - prev = 10485760 // Shaik fixed back to 100% value
		nodeSpecs[MAX_LEVELS - 3][14] = "4000"; //Cameron and Matthew modified to add idle power (watt) 
		nodeSpecs[MAX_LEVELS - 3][15] = "37"; //Cameron and Matthew modified to add energy for downloads (nJ/bit)
		nodeSpecs[MAX_LEVELS - 3][16] = "37"; //	Cameron and Matthew modified to add energy for uploads (nJ/bit)
		
		nodeSpecs[MAX_LEVELS - 4][0] = "Ward";
		nodeSpecs[MAX_LEVELS - 4][1] = "Linux";
		nodeSpecs[MAX_LEVELS - 4][2] = "Xen";
		nodeSpecs[MAX_LEVELS - 4][3] = tenGbRouterCost + "";
		nodeSpecs[MAX_LEVELS - 4][4] = "0.01319444"; // Shaik modified - prev = "0.0336"
		nodeSpecs[MAX_LEVELS - 4][5] = "0.05";
		nodeSpecs[MAX_LEVELS - 4][6] = "0.1";
		nodeSpecs[MAX_LEVELS - 4][7] = "true";
		nodeSpecs[MAX_LEVELS - 4][8] = "false";
		nodeSpecs[MAX_LEVELS - 4][9] = "7"; // Shaik modified to 1/100th - prev = 768
		nodeSpecs[MAX_LEVELS - 4][10] = "544000"; // Shaik modified to 1/100th - prev = 1305600
		nodeSpecs[MAX_LEVELS - 4][11] = "100663296";
		//nodeSpecs[MAX_LEVELS - 4][11] = "1500";
		nodeSpecs[MAX_LEVELS - 4][12] = "1677721600";
		nodeSpecs[MAX_LEVELS - 4][13] = "10485760"; // Shaik modified to 1/100th - prev = 10485760 // Shaik fixed back to 100% value
		nodeSpecs[MAX_LEVELS - 4][14] = "4000"; //Cameron and Matthew modified to add idle power (watt) 
		nodeSpecs[MAX_LEVELS - 4][15] = "37"; //Cameron and Matthew modified to add energy for downloads (nJ/bit)
		nodeSpecs[MAX_LEVELS - 4][16] = "37"; //	Cameron and Matthew modified to add energy for uploads (nJ/bit)
		
		nodeSpecs[MAX_LEVELS - 5][0] = "Library";
		nodeSpecs[MAX_LEVELS - 5][1] = "Linux";
		nodeSpecs[MAX_LEVELS - 5][2] = "Xen";
		nodeSpecs[MAX_LEVELS - 5][3] = tenGbRouterCost + "";
		nodeSpecs[MAX_LEVELS - 5][4] = "0.01319444"; // Shaik modified - prev = "0.00016"
		nodeSpecs[MAX_LEVELS - 5][5] = "0.05";
		nodeSpecs[MAX_LEVELS - 5][6] = "0.1";
		nodeSpecs[MAX_LEVELS - 5][7] = "true";
		nodeSpecs[MAX_LEVELS - 5][8] = "false";
		nodeSpecs[MAX_LEVELS - 5][9] = "2"; // Shaik modified to 1/100th - prev = 192 
		nodeSpecs[MAX_LEVELS - 5][10] = "326400"; // Shaik modified to 1/100th - prev = 326400 
		nodeSpecs[MAX_LEVELS - 5][11] = "25165824";
		//nodeSpecs[MAX_LEVELS - 5][11] = "1500";
		nodeSpecs[MAX_LEVELS - 5][12] = "167772160";
		nodeSpecs[MAX_LEVELS - 5][13] = "10485760"; // Shaik modified to 1/100th - prev = 10485760 // Shaik fixed back to 100% value
		nodeSpecs[MAX_LEVELS - 5][14] = "4000"; //Cameron and Matthew modified to add idle power (watt) 
		nodeSpecs[MAX_LEVELS - 5][15] = "37"; //Cameron and Matthew modified to add energy for downloads (nJ/bit)
		nodeSpecs[MAX_LEVELS - 5][16] = "37"; //	Cameron and Matthew modified to add energy for uploads (nJ/bit)
		
		nodeSpecs[MAX_LEVELS - 6][0] = "Community Center";
		nodeSpecs[MAX_LEVELS - 6][1] = "Linux";
		nodeSpecs[MAX_LEVELS - 6][2] = "Xen";
		nodeSpecs[MAX_LEVELS - 6][3] = oneGbRouterCost + "";
		nodeSpecs[MAX_LEVELS - 6][4] = "0.01319444"; // Shaik modified - prev = "0.0012"
		nodeSpecs[MAX_LEVELS - 6][5] = "0.05";
		nodeSpecs[MAX_LEVELS - 6][6] = "0.1";
		nodeSpecs[MAX_LEVELS - 6][7] = "true";
		nodeSpecs[MAX_LEVELS - 6][8] = "false";
		nodeSpecs[MAX_LEVELS - 6][9] = "1"; // Shaik modified to 1/100th - prev = 128
		nodeSpecs[MAX_LEVELS - 6][10] = "217600"; // Shaik modified to 1/100th - prev = 217600
		nodeSpecs[MAX_LEVELS - 6][11] = "16384";
		nodeSpecs[MAX_LEVELS - 6][12] = "167772160";
		nodeSpecs[MAX_LEVELS - 6][13] = "1048576"; // Shaik modified to 1/100th - prev = 1048576 // Shaik fixed back to 100% value
		nodeSpecs[MAX_LEVELS - 6][14] = "1589"; //Cameron and Matthew modified to add idle power (watt) 
		nodeSpecs[MAX_LEVELS - 6][15] = "31.7"; //Cameron and Matthew modified to add energy for downloads (nJ/bit)
		nodeSpecs[MAX_LEVELS - 6][16] = "31.7"; //	Cameron and Matthew modified to add energy for uploads (nJ/bit)
		
		nodeSpecs[MAX_LEVELS - 7][0] = "School";
		nodeSpecs[MAX_LEVELS - 7][1] = "Linux";
		nodeSpecs[MAX_LEVELS - 7][2] = "Xen";
		nodeSpecs[MAX_LEVELS - 7][3] = oneGbRouterCost + ""; 
		nodeSpecs[MAX_LEVELS - 7][4] = "0.01319444"; // Shaik modified - prev = "0.0003"
		nodeSpecs[MAX_LEVELS - 7][5] = "1";
		nodeSpecs[MAX_LEVELS - 7][6] = "1";
		nodeSpecs[MAX_LEVELS - 7][7] = "true";
		nodeSpecs[MAX_LEVELS - 7][8] = "false";
		nodeSpecs[MAX_LEVELS - 7][9] = "1"; // Shaik modified to 1/100th - prev = 32
		nodeSpecs[MAX_LEVELS - 7][10] = "54400"; // Shaik modified to 1/100th - prev = 54400 
		nodeSpecs[MAX_LEVELS - 7][11] = "4096";
		nodeSpecs[MAX_LEVELS - 7][12] = "41943040";
		nodeSpecs[MAX_LEVELS - 7][13] = "1048576"; // Shaik modified to 1/100th - prev = 1048576 // Shaik fixed back to 100% value
		nodeSpecs[MAX_LEVELS - 7][14] = "1589"; //Cameron and Matthew modified to add idle power (watt) 
		nodeSpecs[MAX_LEVELS - 7][15] = "31.7"; //Cameron and Matthew modified to add energy for downloads (nJ/bit)
		nodeSpecs[MAX_LEVELS - 7][16] = "31.7"; //	Cameron and Matthew modified to add energy for uploads (nJ/bit)

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
