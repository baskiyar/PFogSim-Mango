/*
 * Title:        EdgeCloudSim - Simulation Logger
 * 
 * Description: 
 * SimLogger is responsible for storing simulation events/results
 * in to the files in a specific format.
 * Format is decided in a way to use results in matlab efficiently.
 * If you need more results or another file format, you should modify
 * this class.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.orchestrator.HAFAOrchestrator;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
//import edu.boun.edgecloudsim.edge_server.EdgeHost;
//import edu.boun.edgecloudsim.utils.*;
//import edu.auburn.pFogSim.Puddle.Puddle;
//import edu.auburn.pFogSim.netsim.*;
import edu.boun.edgecloudsim.energy.EnergyModel;


/**
 * 
 * @author szs0117
 *
 */
public class SimLogger {
	public static enum TASK_STATUS {
		CREATED, UPLOADING, PROCESSING, DOWNLOADING, COMPLETED, REJECTED_DUE_TO_VM_CAPACITY, REJECTED_DUE_TO_BANDWIDTH, UNFINISHED_DUE_TO_BANDWIDTH, UNFINISHED_DUE_TO_MOBILITY, ASSIGNED_HOST, REJECTED_DUE_TO_LACK_OF_NODE_CAPACITY, REJECTED_DUE_TO_LACK_OF_NETWORK_BANDWIDTH, REJECTED_DUE_TO_UNACCEPTABLE_LATENCY
	}

	private static boolean fileLogEnabled;
	private static boolean printLogEnabled;
	private static String filePrefix;
	private static String outputFolder;
	private Map<Integer, LogItem> taskMap;
	private LinkedList<VmLoadLogItem> vmLoadList;
	private LinkedList<FNMipsUtilLogItem> fnMipsUtilList; // shaik added
	private LinkedList<FNNwUtilLogItem> fnNwUtilList; // shaik added
	private File centerLogFile;
	PrintWriter centerFileW;
	private ArrayList<Integer> utlizationArray;
	private static File textFile;
	private static FileOutputStream fos;
	private static PrintStream ps;
	
	private static SimLogger singleton = new SimLogger();
	
	
	/*
	 * A private Constructor prevents any other class from instantiating.
	 */
	private SimLogger() {
		fileLogEnabled = false;
		printLogEnabled = false;
	}

	
	/* Static 'instance' method */
	public static SimLogger getInstance() {
		return singleton;
	}
	

	/**
	 * 
	 */
	public static void enableFileLog() {
		fileLogEnabled = true;
	}

	
	/**
	 * @throws IOException 
	 * 
	 */
	public static void enablePrintLog() throws IOException {
		printLogEnabled = true;
		
	}


	/**
	 * 
	 * @return
	 */
	public static boolean isFileLogEnabled() {
		return fileLogEnabled;
	}

	
	/**
	 * 
	 */
	public static void disablePrintLog() {
		printLogEnabled = false;
	}

	
	/**
	 * 
	 * @param bw
	 * @param line
	 * @throws IOException
	 */
	private void appendToFile(BufferedWriter bw, String line) throws IOException {
		bw.write(line);
		bw.newLine();
	}
	
	public File getConsoleTxtFile() {
		return this.textFile;
	}
	
	

	
	/**
	 * 
	 * @param msg
	 */
	public static void printLine(String msg) {
		if (printLogEnabled)
			System.out.println(msg);
			ps.println(msg);
	}

	
	/**
	 * 
	 * @param msg
	 */
	public static void print(String msg) {
		if (printLogEnabled)
			System.out.print(msg);
			ps.print(msg);
	}
	
	public static void fileInitialize(String outputFolder) throws IOException {
		textFile = new File(outputFolder, Long.toString(System.currentTimeMillis()) + "_console.txt");
		//textFile = new File("_consoleOut" + Long.toString(System.currentTimeMillis()) + ".txt");
		textFile.createNewFile();
		fos = new FileOutputStream(textFile);
		ps = new PrintStream(fos);
	}
	
	/**
	 * @param outFolder
	 * @param fileName
	 * @throws IOException 
	 */
	public void simStarted(String outFolder, String fileName) throws IOException {
		filePrefix = fileName;
		outputFolder = outFolder;
		
		taskMap = new HashMap<Integer, LogItem>();
		vmLoadList = new LinkedList<VmLoadLogItem>();
		fnMipsUtilList = new LinkedList<FNMipsUtilLogItem>(); // shaik added
		fnNwUtilList = new LinkedList<FNNwUtilLogItem>(); // shaik added
		utlizationArray = new ArrayList<Integer>();
		try {
			centerLogFile = new File(outputFolder, filePrefix + "_Cost_Logger.txt");
			centerFileW = new PrintWriter(centerLogFile);
		} catch (Exception e) {
			System.out.println("Centralize Logger File Cannot Find");
		}
	}
	
	
	/**
	 * @author szs0117
	 * @param time
	 * @param hostId
	 * @param hostLevel
	 * @param fnMipsUtil
	 */
	public void addFNMipsUtilizationLog(double time, int hostId, int hostLevel, double fnMipsUtil) {
		fnMipsUtilList.add(new FNMipsUtilLogItem(time, hostId, hostLevel, fnMipsUtil));
	}		

		
	/**
	 * @author szs0117
	 * @param time
	 * @param hostId
	 * @param hostLevel
	 * @param fnBwUtil
	 */
	public void addFNNwUtilizationLog(double time, int hostId, int hostLevel, double fnBwUtil) {
		fnNwUtilList.add(new FNNwUtilLogItem(time, hostId, hostLevel, fnBwUtil));
	}		

	
	/**
	 * 
	 * @return
	 */
	public PrintWriter getCentralizeLogPrinter() {
		return centerFileW;
	}

	
	/**
	 * 
	 * @param taskStartTime
	 * @param taskId
	 * @param taskType
	 * @param taskLength
	 * @param taskInputType
	 * @param taskOutputSize
	 */
	public void addLog(double taskStartTime, int taskId, int taskType, int taskLength, int taskInputType,
			int taskOutputSize) {
		// printLine(taskId+"->"+taskStartTime);
		taskMap.put(taskId, new LogItem(taskStartTime, taskType, taskLength, taskInputType, taskOutputSize));
	}

	
	/**
	 * 
	 * @param taskId
	 * 
	 * @param taskUploadTime
	 */
	public void uploadStarted(int taskId, double taskUploadTime) {
		taskMap.get(taskId).taskUploadStarted(taskUploadTime);
	}

	
	/**
	 * 
	 * @param taskId
	 * @param datacenterId
	 * @param hostId
	 * @param vmId
	 * @param vmType
	 */
	public void uploaded(int taskId, int datacenterId, int hostId, int vmId, int vmType) {
		taskMap.get(taskId).taskUploaded(datacenterId, hostId, vmId, vmType);
	}

	
	/**
	 * 
	 * @param taskId
	 * @param taskDownloadTime
	 */
	public void downloadStarted(int taskId, double taskDownloadTime) {
		taskMap.get(taskId).taskDownloadStarted(taskDownloadTime);
	}

	
	/**
	 * 
	 * @param taskId
	 * @param taskEndTime
	 */
	public void downloaded(int taskId, double taskEndTime) {
		taskMap.get(taskId).taskDownloaded(taskEndTime);
	}
	
	
	/**
	 * 
	 * @param taskId
	 * @param taskEndTime
	 * @param cost
	 */
	public void downloaded(int taskId, double taskEndTime, double cost) {
		taskMap.get(taskId).taskDownloaded(taskEndTime, cost);
	}

	
	// Shaik added
	/**
	 * 
	 * @param taskId
	 * @param taskRejectTime
	 * @param taskStatus
	 */
	public void taskRejected(int taskId, double taskRejectTime, SimLogger.TASK_STATUS taskStatus) {
		taskMap.get(taskId).taskRejectedStatus(taskRejectTime, taskStatus);
	}
	
	
	/**
	 * 
	 * @param taskId
	 * @param taskRejectTime
	 */
	public void rejectedDueToVMCapacity(int taskId, double taskRejectTime) {
		taskMap.get(taskId).taskRejectedDueToVMCapacity(taskRejectTime);
	}

	
	/**
	 * 
	 * @param taskId
	 * @param taskRejectTime
	 * @param vmType
	 */
	public void rejectedDueToBandwidth(int taskId, double taskRejectTime, int vmType) {
		taskMap.get(taskId).taskRejectedDueToBandwidth(taskRejectTime, vmType);
	}

	
	/**
	 * 
	 * @param taskId
	 * @param taskRejectTime
	 */
	public void failedDueToBandwidth(int taskId, double taskRejectTime) {
		taskMap.get(taskId).taskFailedDueToBandwidth(taskRejectTime);
	}

	
	/**
	 * 
	 * @param taskId
	 * @param time
	 */
	public void failedDueToMobility(int taskId, double time) {
		taskMap.get(taskId).taskFailedDueToMobility(time);
	}

	
	/**
	 * 
	 * @param time
	 * @param load
	 */
	public void addVmUtilizationLog(double time, double load) {
		vmLoadList.add(new VmLoadLogItem(time, load));
	}
	
	
	/**
	 * 
	 * @param taskId
	 * @param dist
	 */
	public void addHostDistanceLog(int taskId, double dist) {
		taskMap.get(taskId).setDistance(dist);
	}
	
	public void addUserDistanceLog(int taskId, double dist) {
		taskMap.get(taskId).setDistanceToUser(dist);
	}
	
	
	/**
	 * 
	 * @param taskId
	 * @param hops
	 */
	public void addHops(int taskId, int hops) {
		taskMap.get(taskId).setHops(hops);
	}
	
	public void addHopsBack(int taskId, int hops) {
		//System.out.println(hops);
		taskMap.get(taskId).setHopsToUser(hops);
	}
	
	//	
	int[] totalNodesNmuberInEachLevel = {0, 0, 0, 0, 0, 0, 0};

	//
	private int[] levelFogNodeCount = {0, 0, 0, 0, 0, 0, 0};
	
	
	/**
	 * 
	 */
	private void getTotalFogNodesCountInEachLevel() {
		HashSet<NodeSim> nodes = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology().getNodes();
		for (NodeSim node: nodes) {
			totalNodesNmuberInEachLevel[node.getLevel() - 1]++;
		}
	}
	

	/**
	 * Qian add method for counting fog nodes utilization
	 * @param hostId
	 * @param host
	 */
	public void addNodeUtilization(int hostId, EdgeHost host) {
		if (!utlizationArray.contains(hostId)) {
			utlizationArray.add(hostId);
			this.levelFogNodeCount[host.getLevel() - 1]++;
		}
	}
	
	private int[] levelCloudletCount = {0, 0, 0, 0, 0, 0, 0, 0};
	
	
	/**
	 * 
	 * @param level
	 */
	public void addCloudletToLevel(int level) {this.levelCloudletCount[level]++;}
	
	
	/**
	 * 
	 * @throws IOException
	 */
	public void simStopped() throws IOException {
		int numOfAppTypes = SimSettings.getInstance().getTaskLookUpTable().length;

		File successFile = null, failFile = null, vmLoadFile = null, fnMipsUtilFile = null, fnNwUtilFile = null, locationFile = null, distFile = null, distBackFile = null, hopFile = null, hopsBackFile = null, hafaNumHostsFile = null, hafaNumMsgsFile = null, hafaNumPuddlesFile = null, energyUsageFile = null;
		FileWriter successFW = null, failFW = null, vmLoadFW = null, fnMipsUtilFW = null, fnNwUtilFW = null, locationFW = null, distFW = null, distBackFW = null, hopFW = null, hopsBackFW = null, hafaNumHostsFW = null, hafaNumMsgsFW = null, hafaNumPuddlesFW = null, energyUsageFW = null;
		BufferedWriter successBW = null, failBW = null, vmLoadBW = null, fnMipsUtilBW = null, fnNwUtilBW = null, locationBW = null, distBW = null, distBackBW = null, hopBW = null, hopsBackBW = null, hafaNumHostsBW = null, hafaNumMsgsBW = null, hafaNumPuddlesBW = null, energyUsageBW = null;

		/*File[] vmLoadFileClay = new File[numOfAppTypes]; 
		FileWriter[] vmLoadFWClay = new FileWriter[numOfAppTypes];
		BufferedWriter[] vmLoadBWClay = new BufferedWriter[numOfAppTypes];*/
		
		// Save generic results to file for each app type. last index is average
		// of all app types
		File[] genericFiles = new File[numOfAppTypes + 1];
		FileWriter[] genericFWs = new FileWriter[numOfAppTypes + 1];
		BufferedWriter[] genericBWs = new BufferedWriter[numOfAppTypes + 1];

		// extract following values for each app type. last index is average of
		// all app types
		int[] uncompletedTask = new int[numOfAppTypes + 1];
		int[] uncompletedTaskOnCloud = new int[numOfAppTypes + 1];
		int[] uncompletedTaskOnCloudlet = new int[numOfAppTypes + 1];

		int[] completedTask = new int[numOfAppTypes + 1];
		int[] completedTaskOnCloud = new int[numOfAppTypes + 1];
		int[] completedTaskOnCloudlet = new int[numOfAppTypes + 1];

		int[] failedTask = new int[numOfAppTypes + 1];
		int[] failedTaskOnCloud = new int[numOfAppTypes + 1];
		int[] failedTaskOnCloudlet = new int[numOfAppTypes + 1];

		double[] networkDelay = new double[numOfAppTypes + 1];
		double[] wanDelay = new double[numOfAppTypes + 1];
		double[] lanDelay = new double[numOfAppTypes + 1];

		double[] serviceTime = new double[numOfAppTypes + 1];
		double[] serviceTimeOnCloud = new double[numOfAppTypes + 1];
		double[] serviceTimeOnCloudlet = new double[numOfAppTypes + 1];

		double[] processingTime = new double[numOfAppTypes + 1];
		double[] processingTimeOnCloud = new double[numOfAppTypes + 1];
		double[] processingTimeOnCloudlet = new double[numOfAppTypes + 1];

		double[] cost = new double[numOfAppTypes + 1];
		int[] failedTaskDuetoBw = new int[numOfAppTypes + 1];
		int[] failedTaskDuetoLanBw = new int[numOfAppTypes + 1];
		int[] failedTaskDuetoWanBw = new int[numOfAppTypes + 1];
		int[] failedTaskDuetoMobility = new int[numOfAppTypes + 1];
		int[] rejectedTaskDueToVmCapacity = new int[numOfAppTypes + 1];
		
		//Shaik added
		int[] rejectedTaskDueToLackofNodeCapacity = new int[numOfAppTypes+1];
		int[] rejectedTaskDueToLackofNetworkBandwidth = new int[numOfAppTypes+1];
		int[] rejectedTaskDueToUnacceptableLatency = new int[numOfAppTypes+1];
		
		double[] totalDist = new double[numOfAppTypes + 1];
		double[] totalUserDist = new double[numOfAppTypes +1];
		int[] totalHops = new int[numOfAppTypes + 1];
		int[] totalHopsBack = new int[numOfAppTypes + 1];
		int[] numTasksPerAppType = new int[numOfAppTypes + 1];

		//Modify the following array lengths depending on number of layers in test fog environment. Currently, it is 7 layered.
		double[] fogLayerAvgMipsUtil = {0, 0, 0, 0, 0, 0, 0}; // Shaik added
		double[] fogLayerTotalMipsUtil = {0, 0, 0, 0, 0, 0, 0}; // Shaik added
		double[] fogLayerEntryMipsCount = {0, 0, 0, 0, 0, 0, 0}; // Shaik added

		double[] fogLayerAvgNwUtil = {0, 0, 0, 0, 0, 0, 0}; // Shaik added
		double[] fogLayerTotalNwUtil = {0, 0, 0, 0, 0, 0, 0}; // Shaik added
		double[] fogLayerEntryNwCount = {0, 0, 0, 0, 0, 0, 0}; // Shaik added

		// open all files and prepare them for write
		if (fileLogEnabled) {
			if (SimSettings.getInstance().getDeepFileLoggingEnabled()) {
				successFile = new File(outputFolder, filePrefix + "_SUCCESS.log");
				successFW = new FileWriter(successFile, true);
				successBW = new BufferedWriter(successFW);

				failFile = new File(outputFolder, filePrefix + "_FAIL.log");
				failFW = new FileWriter(failFile, true);
				failBW = new BufferedWriter(failFW);
			}

			vmLoadFile = new File(outputFolder, filePrefix + "_VM_LOAD.log");
			vmLoadFW = new FileWriter(vmLoadFile, true);
			vmLoadBW = new BufferedWriter(vmLoadFW);

			// shaik added
			fnMipsUtilFile = new File(outputFolder, filePrefix + "_HOST_MIPS_UTILIZATION.log");
			fnMipsUtilFW = new FileWriter(fnMipsUtilFile, true);
			fnMipsUtilBW = new BufferedWriter(fnMipsUtilFW);

			// shaik added
			fnNwUtilFile = new File(outputFolder, filePrefix + "_HOST_NETWORK_UTILIZATION.log");
			fnNwUtilFW = new FileWriter(fnNwUtilFile, true);
			fnNwUtilBW = new BufferedWriter(fnNwUtilFW);

			locationFile = new File(outputFolder, filePrefix + "_LOCATION.log");
			locationFW = new FileWriter(locationFile, true);
			locationBW = new BufferedWriter(locationFW);
			
			distFile = new File(outputFolder, filePrefix + "_DISTANCES.log");
			distFW = new FileWriter(distFile, true);
			distBW = new BufferedWriter(distFW);
			
			distBackFile = new File(outputFolder, filePrefix + "_DISTANCES_BACK.log");
			distBackFW = new FileWriter(distBackFile, true);
			distBackBW = new BufferedWriter(distBackFW);
			
			hopFile = new File(outputFolder, filePrefix + "_HOPS.log");
			hopFW = new FileWriter(hopFile, true);
			hopBW = new BufferedWriter(hopFW);
			
			hopsBackFile = new File(outputFolder, filePrefix + "_HOPS_BACK.log");
			hopsBackFW = new FileWriter(hopsBackFile, true);
			hopsBackBW = new BufferedWriter(hopsBackFW);
			
			hafaNumHostsFile = new File(outputFolder, filePrefix + "_NUMHOSTS.log");
			hafaNumHostsFW = new FileWriter(hafaNumHostsFile, true);
			hafaNumHostsBW = new BufferedWriter(hafaNumHostsFW);
			
			hafaNumMsgsFile = new File(outputFolder, filePrefix + "_NUMMSGS.log");
			hafaNumMsgsFW = new FileWriter(hafaNumMsgsFile, true);
			hafaNumMsgsBW = new BufferedWriter(hafaNumMsgsFW);
			
			hafaNumPuddlesFile = new File(outputFolder, filePrefix + "_NUMPUDDLES.log");
			hafaNumPuddlesFW = new FileWriter(hafaNumPuddlesFile, true);
			hafaNumPuddlesBW = new BufferedWriter(hafaNumPuddlesFW);
			
			energyUsageFile = new File(outputFolder, filePrefix + "_ENERGY_USAGE.log");
			energyUsageFW = new FileWriter(energyUsageFile, true);
			energyUsageBW = new BufferedWriter(energyUsageFW);
			
			
			
			
			
			/*for(int i = 0; i < numOfAppTypes; i++)
			{
				vmLoadFileClay[i] = new File(outputFolder, "CLAYSTESTFILE" + "_" + i + ".log");
				vmLoadFWClay[i] = new FileWriter(vmLoadFileClay[i], true);
				vmLoadBWClay[i] = new BufferedWriter(vmLoadFWClay[i]);
				appendToFile(vmLoadBWClay[i], "#network delay\tsimulation time");
			}*/
			for (int i = 0; i < numOfAppTypes + 1; i++) {
				String fileName = "ALL_APPS_GENERIC.log";

				if (i < numOfAppTypes) {
					// if related app is not used in this simulation, just discard it
					if (SimSettings.getInstance().getTaskLookUpTable()[i][0] == 0)
						continue;

					fileName = SimSettings.APP_TYPES.values()[i] + "_GENERIC.log";
				}

				genericFiles[i] = new File(outputFolder, filePrefix + "_" + fileName);
				genericFWs[i] = new FileWriter(genericFiles[i], true);
				genericBWs[i] = new BufferedWriter(genericFWs[i]);
				appendToFile(genericBWs[i], "#auto generated file!");
			}

			if (SimSettings.getInstance().getDeepFileLoggingEnabled()) {
				appendToFile(successBW, "#auto generated file!");
				appendToFile(failBW, "#auto generated file!");
			}

			appendToFile(vmLoadBW, "#auto generated file!");
			appendToFile(locationBW, "#auto generated file!");
			appendToFile(distBW, "#auto generated file!");
			appendToFile(hopBW, "#auto generated file!");
			appendToFile(distBackBW, "#auto generated file!");
			appendToFile(hopsBackBW, "#auto generated file!");
		
		}
		
		// Print warm up tasks.
		int warmUpTasks = 0;
		
		// extract the result of each task and write it to the file if required
		for (Map.Entry<Integer, LogItem> entry : taskMap.entrySet()) {
			Integer key = entry.getKey();
			LogItem value = entry.getValue();

			//Ignore all Warmup tasks - Shaik updated
			if (value.isInWarmUpPeriod()) {
				warmUpTasks++;
				continue;
			}				
	
			numTasksPerAppType[value.getTaskType()]++;
			
			// track the number of successfully COMPLETED tasks 			
			if (value.getStatus() == SimLogger.TASK_STATUS.COMPLETED) {
				completedTask[value.getTaskType()]++;

				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal())
					completedTaskOnCloud[value.getTaskType()]++;
				else
					completedTaskOnCloudlet[value.getTaskType()]++;
			} else {
				failedTask[value.getTaskType()]++;

				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal())
					failedTaskOnCloud[value.getTaskType()]++;
				else
					failedTaskOnCloudlet[value.getTaskType()]++;
			}

			// Track additional metrics per task
			// If task is COMPLETED
			if (value.getStatus() == SimLogger.TASK_STATUS.COMPLETED) {
				
				// Get info to calculate 'Average distance to host'
				totalDist[value.getTaskType()] += value.getHostDist();
				totalUserDist[value.getTaskType()] += value.getDistanceToUser();
				distBW.write(value.getHostDist() + ",");
				distBackBW.write(value.getDistanceToUser() + ",");
				
				// Get info to calculate 'Average number of hops to host'
				totalHops[value.getTaskType()] += value.getHops();
				totalHopsBack[value.getTaskType()] += value.getHopsToUser();
				hopBW.write(value.getHops() + ",");
				hopsBackBW.write(value.getHopsToUser() + ",");
				
				
				cost[value.getTaskType()] += value.getCost();
				serviceTime[value.getTaskType()] += value.getServiceTime();
				networkDelay[value.getTaskType()] += value.getNetworkDelay();
				processingTime[value.getTaskType()] += (value.getServiceTime() - value.getNetworkDelay());

				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal()) {
					wanDelay[value.getTaskType()] += value.getNetworkDelay();
					serviceTimeOnCloud[value.getTaskType()] += value.getServiceTime();
					processingTimeOnCloud[value.getTaskType()] += (value.getServiceTime() - value.getNetworkDelay());
				} 
				else {
					lanDelay[value.getTaskType()] += value.getNetworkDelay();
					serviceTimeOnCloudlet[value.getTaskType()] += value.getServiceTime();
					processingTimeOnCloudlet[value.getTaskType()] += (value.getServiceTime() - value.getNetworkDelay());
				}

				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(successBW, value.toString(key));
			}
			
			// If task is REJECTED_DUE_TO_VM_CAPACITY
			else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY) {
				rejectedTaskDueToVmCapacity[value.getTaskType()]++;
				
				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(failBW, value.toString(key));
			}
			
			// If task is REJECTED_DUE_TO_LACK_OF_NODE_CAPACITY
			else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_LACK_OF_NODE_CAPACITY) {
				rejectedTaskDueToLackofNodeCapacity[value.getTaskType()]++;
				
				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(failBW, value.toString(key));
			}
			
			// If task is REJECTED_DUE_TO_BANDWIDTH or UNFINISHED_DUE_TO_BANDWIDTH
			else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH
					|| value.getStatus() == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH) {
				failedTaskDuetoBw[value.getTaskType()]++;
				
				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal())
					failedTaskDuetoWanBw[value.getTaskType()]++;
				else
					failedTaskDuetoLanBw[value.getTaskType()]++;

				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(failBW, value.toString(key));
			}
			
			// If task is REJECTED_DUE_TO_LACK_OF_NETWORK_BANDWIDTH
			else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_LACK_OF_NETWORK_BANDWIDTH) {
				rejectedTaskDueToLackofNetworkBandwidth[value.getTaskType()]++;
				
				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(failBW, value.toString(key));
			}
			
			// If task is REJECTED_DUE_TO_UNACCEPTABLE_LATENCY
			else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_UNACCEPTABLE_LATENCY) {
				rejectedTaskDueToUnacceptableLatency[value.getTaskType()]++;
				
				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(failBW, value.toString(key));
			}

			// If task is UNFINISHED_DUE_TO_MOBILITY
			else if (value.getStatus() == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY) {
				failedTaskDuetoMobility[value.getTaskType()]++;
				
				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(failBW, value.toString(key));
			}
			
			// If task execution is left incomplete
			else {
				uncompletedTask[value.getTaskType()]++;
				failedTask[value.getTaskType()]--;
				
				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal()) {
					uncompletedTaskOnCloud[value.getTaskType()]++;
					failedTaskOnCloud[value.getTaskType()]--;
				}	
				else {
					uncompletedTaskOnCloudlet[value.getTaskType()]++;
					failedTaskOnCloudlet[value.getTaskType()]--;	
				}
			}
			
		}//end for entry
		
		// calculate total values
		uncompletedTask[numOfAppTypes] = IntStream.of(uncompletedTask).sum();
		uncompletedTaskOnCloud[numOfAppTypes] = IntStream.of(uncompletedTaskOnCloud).sum();
		uncompletedTaskOnCloudlet[numOfAppTypes] = IntStream.of(uncompletedTaskOnCloudlet).sum();

		completedTask[numOfAppTypes] = IntStream.of(completedTask).sum();
		completedTaskOnCloud[numOfAppTypes] = IntStream.of(completedTaskOnCloud).sum();
		completedTaskOnCloudlet[numOfAppTypes] = IntStream.of(completedTaskOnCloudlet).sum();

		failedTask[numOfAppTypes] = IntStream.of(failedTask).sum();
		failedTaskOnCloud[numOfAppTypes] = IntStream.of(failedTaskOnCloud).sum();
		failedTaskOnCloudlet[numOfAppTypes] = IntStream.of(failedTaskOnCloudlet).sum();

		networkDelay[numOfAppTypes] = DoubleStream.of(networkDelay).sum();
		lanDelay[numOfAppTypes] = DoubleStream.of(lanDelay).sum();
		wanDelay[numOfAppTypes] = DoubleStream.of(wanDelay).sum();

		serviceTime[numOfAppTypes] = DoubleStream.of(serviceTime).sum();
		serviceTimeOnCloud[numOfAppTypes] = DoubleStream.of(serviceTimeOnCloud).sum();
		serviceTimeOnCloudlet[numOfAppTypes] = DoubleStream.of(serviceTimeOnCloudlet).sum();

		processingTime[numOfAppTypes] = DoubleStream.of(processingTime).sum();
		processingTimeOnCloud[numOfAppTypes] = DoubleStream.of(processingTimeOnCloud).sum();
		processingTimeOnCloudlet[numOfAppTypes] = DoubleStream.of(processingTimeOnCloudlet).sum();

		cost[numOfAppTypes] = DoubleStream.of(cost).sum();
		failedTaskDuetoBw[numOfAppTypes] = IntStream.of(failedTaskDuetoBw).sum();
		failedTaskDuetoWanBw[numOfAppTypes] = IntStream.of(failedTaskDuetoWanBw).sum();
		failedTaskDuetoLanBw[numOfAppTypes] = IntStream.of(failedTaskDuetoLanBw).sum();
		failedTaskDuetoMobility[numOfAppTypes] = IntStream.of(failedTaskDuetoMobility).sum();
		rejectedTaskDueToVmCapacity[numOfAppTypes] = IntStream.of(rejectedTaskDueToVmCapacity).sum();
		
		//Shaik added
		rejectedTaskDueToLackofNodeCapacity[numOfAppTypes] = IntStream.of(rejectedTaskDueToLackofNodeCapacity).sum();
		rejectedTaskDueToLackofNetworkBandwidth[numOfAppTypes] = IntStream.of(rejectedTaskDueToLackofNetworkBandwidth).sum();
		rejectedTaskDueToUnacceptableLatency[numOfAppTypes] = IntStream.of(rejectedTaskDueToUnacceptableLatency).sum();
		
		totalDist[numOfAppTypes] = DoubleStream.of(totalDist).sum();
		totalUserDist[numOfAppTypes] = DoubleStream.of(totalUserDist).sum();
		totalHops[numOfAppTypes] = IntStream.of(totalHops).sum();
		totalHopsBack[numOfAppTypes] = IntStream.of(totalHopsBack).sum();
		numTasksPerAppType[numOfAppTypes] = IntStream.of(numTasksPerAppType).sum(); // Shaik modified
		
		// calculate server load - This value may not be valid for HAFA test environment. May ignore. 
		double totalVmLoad = 0;
		for (VmLoadLogItem entry : vmLoadList) {
			totalVmLoad += entry.getLoad();
			if (fileLogEnabled)
				appendToFile(vmLoadBW, entry.toString());
		}
		
		// Shaik added
		// calculate Average Mips utilization of all fog nodes		
		double totalFnMipsUtil = 0;		
		for (FNMipsUtilLogItem entry : fnMipsUtilList) {		
			totalFnMipsUtil += entry.getFnMipsUtil();	
			if (fileLogEnabled)	
				appendToFile(fnMipsUtilBW, entry.toString());
			
			// Capture metrics per layer
			fogLayerTotalMipsUtil[entry.getHostLevel()-1] += entry.getFnMipsUtil();
			fogLayerEntryMipsCount[entry.getHostLevel()-1]++;	
		}
		
		// Shaik added
		// calculate Average Network utilization of all fog nodes		
		double totalFnNwUtil = 0;		
		for (FNNwUtilLogItem entry : fnNwUtilList) {		
			totalFnNwUtil += entry.getFnNwUtil();	
			if (fileLogEnabled)	
				appendToFile(fnNwUtilBW, entry.toString());

			// Capture metrics per layer
			fogLayerTotalNwUtil[entry.getHostLevel()-1] += entry.getFnNwUtil();
			fogLayerEntryNwCount[entry.getHostLevel()-1]++;	
		}		

		// Shaik added
		// calculate Average node utilization per fog layer		
		for (int i=0; i < fogLayerAvgMipsUtil.length; i++ ) {
			fogLayerAvgMipsUtil[i] = fogLayerTotalMipsUtil[i] / fogLayerEntryMipsCount[i];
		}
		
		// Shaik added
		// calculate Average network utilization per fog layer		
		for (int i=0; i < fogLayerAvgNwUtil.length; i++ ) {
			fogLayerAvgNwUtil[i] = fogLayerTotalNwUtil[i] / fogLayerEntryNwCount[i];
		}
		
		// Average fog node utilization per layer
		double totalMipsUtil = 0;
		for (int i = 0; i < fogLayerAvgMipsUtil.length; i++) {
			totalMipsUtil += (double)fogLayerAvgMipsUtil[i];
		}
		double avgMipsUtilPrcnt = totalMipsUtil / (double)fogLayerAvgMipsUtil.length;

		// Average fog network utilization per layer
		double totalNwUtil = 0;
		for (int i = 0; i < fogLayerAvgNwUtil.length; i++) {
			totalNwUtil += (double)fogLayerAvgNwUtil[i];
		}
		double avgNwUtilPrcnt = totalNwUtil / (double)fogLayerAvgNwUtil.length;

		/* HAFA Metrics - Analysis */
		int devCount = SimManager.getInstance().getNumOfMobileDevice();
		
		int[] numProsHosts = new int[devCount];
		int[] numMsgs = new int[devCount];
		int[] numPuds = new int[devCount];

		
		if (SimManager.getInstance().getEdgeOrchestrator() instanceof HAFAOrchestrator ) {
			
			// Retrieve information regarding # of hosts, msgs, & Puddles for each service request (device)
			numProsHosts = SimManager.getInstance().getEdgeOrchestrator().getNumProspectiveHosts();
			numMsgs = SimManager.getInstance().getEdgeOrchestrator().getNumMessages();
			numPuds = SimManager.getInstance().getEdgeOrchestrator().getNumPuddlesSearched();
			
			// Print info to corresponding files
			for (int i=0; i<devCount; i++) {
				appendToFile(hafaNumHostsBW, Integer.toString(numProsHosts[i]) + SimSettings.DELIMITER);
				appendToFile(hafaNumMsgsBW, Integer.toString(numMsgs[i]) + SimSettings.DELIMITER);
				appendToFile(hafaNumPuddlesBW, Integer.toString(numPuds[i]) + SimSettings.DELIMITER);
			}			
		}
		
		
//		//Qian Write require data into file
//		//**********************************
//		//**********************************
//		PrintWriter netWritor = new PrintWriter(filePrefix + "_NetWorkDelay.txt", "UTF-8");
//		PrintWriter serviceWritor = new PrintWriter(filePrefix + "_ServiceTime.txt", "UTF-8");
//		PrintWriter processingWritor = new PrintWriter(filePrefix + "_ProcessingTime.txt", "UTF-8");
//		PrintWriter costWritor = new PrintWriter(filePrefix + "_Cost.txt", "UTF-8");
//		for (int i = 0; i < numOfAppTypes + 1; i++) {
//			double _serviceTime = (completedTask[i] == 0) ? 0.0 : (serviceTime[i] / (double) completedTask[i]);
//			double _networkDelay = (completedTask[i] == 0) ? 0.0 : (networkDelay[i] / (double) completedTask[i]);
//			double _processingTime = (completedTask[i] == 0) ? 0.0 : (processingTime[i] / (double) completedTask[i]);
//			double _cost = (completedTask[i] == 0) ? 0.0 : (cost[i] / (double) completedTask[i]);
//			netWritor.println("NetworkDelay=" + networkDelay[i] + "\t" + "TaskNumber=" + completedTask[i] + "\t" + "Average=" + _networkDelay);
//			serviceWritor.println("ServiceTime=" + serviceTime[i] + "\t" + "TaskNumber=" + completedTask[i] + "\t" + "Average=" + _serviceTime);
//			processingWritor.println("ProcessingTime=" + processingTime[i] + "\t" + "TaskNumber=" + completedTask[i] + "\t" + "Average=" + _processingTime);
//			costWritor.println("Cost=" + cost[i] + "\t" + "TaskNumber=" + completedTask[i] + "\t" + "Average=" + _cost);
//		}
//		netWritor.close();
//		serviceWritor.close();
//		processingWritor.close();
//		costWritor.close();
		//**********************************
		//**********************************

		// Print metrics to log files
		if (fileLogEnabled) {

			// write location info of all mobile devices to file
			for (int t = 1; t < (SimSettings.getInstance().getSimulationTime()
					/ SimSettings.getInstance().getVmLocationLogInterval()); t++) {
				Double time = t * SimSettings.getInstance().getVmLocationLogInterval();
				if (time < SimSettings.getInstance().getWarmUpPeriod())
					continue;

				locationBW.write(time.toString()+"-");
				for (int i = 0; i < SimManager.getInstance().getNumOfMobileDevice(); i++) {
					Location loc = SimManager.getInstance().getMobilityModel().getLocation(i, time);
					locationBW.write(loc.getXPos()+","+loc.getYPos()+","+loc.getAltitude()+SimSettings.DELIMITER);
				}
				locationBW.newLine();
			}

			//calculate averages for various metrics 
			for (int i = 0; i < numOfAppTypes + 1; i++) {
				if (i < numOfAppTypes) {
					// if corresponding application is not used in this simulation, just discard it
					if (SimSettings.getInstance().getTaskLookUpTable()[i][0] == 0)
						continue;
				}

				// check if the divisor is zero in order to avoid division by zero problem
				double _serviceTime = (completedTask[i] == 0) ? 0.0 : (serviceTime[i] / (double) completedTask[i]);
				double _networkDelay = (completedTask[i] == 0) ? 0.0 : (networkDelay[i] / (double) completedTask[i]);
				double _processingTime = (completedTask[i] == 0) ? 0.0 : (processingTime[i] / (double) completedTask[i]);
				double _vmLoad = (vmLoadList.size() == 0) ? 0.0 : (totalVmLoad / (double) vmLoadList.size());
				//double _fnMipsUtil = (fnMipsUtilList.size() == 0) ? 0.0 : (totalFnMipsUtil / (double) fnMipsUtilList.size());
				double _fnMipsUtil = avgMipsUtilPrcnt;
				//double _fnNwUtil = (fnNwUtilList.size() == 0) ? 0.0 : (totalFnNwUtil / (double) fnNwUtilList.size());	
				double _fnNwUtil = avgNwUtilPrcnt;
				double _cost = (completedTask[i] == 0) ? 0.0 : (cost[i] / (double) completedTask[i]);
				double dist = (completedTask[i] == 0) ? 0.0 : (totalDist[i] / (double) completedTask[i]);
				double distBack = (completedTask[i] == 0) ? 0.0 : (totalUserDist[i] / (double) completedTask[i]);
				double hops = (completedTask[i] == 0) ? 0.0 : ((double) totalHops[i] / (double) completedTask[i]);
				double hopsBack = (completedTask[i] == 0) ? 0.0 : ((double) totalHopsBack[i] / (double) completedTask[i]);
				double avgNumHosts = SimManager.getInstance().getEdgeOrchestrator().getAvgNumProspectiveHosts();
				double avgNumMsgs = SimManager.getInstance().getEdgeOrchestrator().getAvgNumMessages();
				double avgNumPuds = SimManager.getInstance().getEdgeOrchestrator().getAvgNumPuddlesSearched();

				// write generic results
				String genericResult1 = Integer.toString(completedTask[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTask[i]) + SimSettings.DELIMITER 
						+ Integer.toString(uncompletedTask[i]) + SimSettings.DELIMITER 
						+ Integer.toString(failedTaskDuetoBw[i]) + SimSettings.DELIMITER
						+ Double.toString(_serviceTime) + SimSettings.DELIMITER 
						+ Double.toString(_processingTime) + SimSettings.DELIMITER 
						+ Double.toString(_networkDelay) + SimSettings.DELIMITER
						+ Double.toString(_vmLoad) + SimSettings.DELIMITER 
						+ Double.toString(_cost) + SimSettings.DELIMITER 
						+ Integer.toString(rejectedTaskDueToVmCapacity[i]) + SimSettings.DELIMITER 
						+ Integer.toString(failedTaskDuetoMobility[i]) + SimSettings.DELIMITER
						+ Double.toString(_fnMipsUtil) + SimSettings.DELIMITER 
						+ Double.toString(_fnNwUtil) + SimSettings.DELIMITER 
						+ Integer.toString(rejectedTaskDueToLackofNodeCapacity[i]) + SimSettings.DELIMITER 
						+ Integer.toString(rejectedTaskDueToLackofNetworkBandwidth[i]) + SimSettings.DELIMITER 
						+ Integer.toString(rejectedTaskDueToUnacceptableLatency[i]) + SimSettings.DELIMITER 
						+ Integer.toString(failedTask[numOfAppTypes] + completedTask[numOfAppTypes]); 
				
				// Capture failed tasks detailed info
				String genericResult8 = "";
				genericResult8 = 
						Integer.toString(failedTask[numOfAppTypes] + completedTask[numOfAppTypes]) 
						+ Integer.toString(failedTaskDuetoMobility[i]) + SimSettings.DELIMITER
						+ Integer.toString(rejectedTaskDueToLackofNodeCapacity[i]) + SimSettings.DELIMITER 
						+ Integer.toString(rejectedTaskDueToLackofNetworkBandwidth[i]) + SimSettings.DELIMITER 
						+ Integer.toString(rejectedTaskDueToUnacceptableLatency[i]) + SimSettings.DELIMITER; 

				
				// check if the divisor is zero in order to avoid division by zero problem
				double _lanDelay = (completedTaskOnCloudlet[i] == 0) ? 0.0
						: (lanDelay[i] / (double) completedTaskOnCloudlet[i]);
				double _serviceTimeOnCloudlet = (completedTaskOnCloudlet[i] == 0) ? 0.0
						: (serviceTimeOnCloudlet[i] / (double) completedTaskOnCloudlet[i]);
				double _processingTimeOnCloudlet = (completedTaskOnCloudlet[i] == 0) ? 0.0
						: (processingTimeOnCloudlet[i] / (double) completedTaskOnCloudlet[i]);
				String genericResult2 = Integer.toString(completedTaskOnCloudlet[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTaskOnCloudlet[i]) + SimSettings.DELIMITER
						+ Integer.toString(uncompletedTaskOnCloudlet[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTaskDuetoLanBw[i]) + SimSettings.DELIMITER
						+ Double.toString(_serviceTimeOnCloudlet) + SimSettings.DELIMITER
						+ Double.toString(_processingTimeOnCloudlet) + SimSettings.DELIMITER
						+ Double.toString(_lanDelay);

				// check if the divisor is zero in order to avoid division by zero problem
				double _wanDelay = (completedTaskOnCloud[i] == 0) ? 0.0
						: (wanDelay[i] / (double) completedTaskOnCloud[i]);
				double _serviceTimeOnCloud = (completedTaskOnCloud[i] == 0) ? 0.0
						: (serviceTimeOnCloud[i] / (double) completedTaskOnCloud[i]);
				double _processingTimeOnCloud = (completedTaskOnCloud[i] == 0) ? 0.0
						: (processingTimeOnCloud[i] / (double) completedTaskOnCloud[i]);
				String genericResult3 = Integer.toString(completedTaskOnCloud[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTaskOnCloud[i]) + SimSettings.DELIMITER
						+ Integer.toString(uncompletedTaskOnCloud[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTaskDuetoWanBw[i]) + SimSettings.DELIMITER
						+ Double.toString(_serviceTimeOnCloud) + SimSettings.DELIMITER
						+ Double.toString(_processingTimeOnCloud) + SimSettings.DELIMITER 
						+ Double.toString(_wanDelay);
				
				String genericResult4 = Double.toString(dist) 
						+ SimSettings.DELIMITER + Double.toString(hops)
						+ SimSettings.DELIMITER + Double.toString(avgNumHosts)
						+ SimSettings.DELIMITER + Double.toString(avgNumMsgs)
						+ SimSettings.DELIMITER + Double.toString(avgNumPuds);

				// Tasks executed per fog layer
				String genericResult5 = "";
				for(int level = 1; level <= SimSettings.getInstance().getMaxLevels(); level++) {
					genericResult5 += Integer.toString(levelCloudletCount[level]) + SimSettings.DELIMITER;
				}
					
				// Average fog node utilization per layer
				String genericResult6 = "";
				for (int index = 0; index < fogLayerAvgMipsUtil.length; index++) {
					genericResult6 += Double.toString(fogLayerAvgMipsUtil[index]) + SimSettings.DELIMITER;
				}

				// Average fog network utilization per layer
				String genericResult7 = "";
				for (int index = 0; index < fogLayerAvgNwUtil.length; index++) {
					genericResult7 += Double.toString(fogLayerAvgNwUtil[index]) + SimSettings.DELIMITER;
				}
				
				String genericResult9 = Double.toString(distBack) 
						+ SimSettings.DELIMITER + Double.toString(hopsBack);
				
				EnergyModel.calculateTotalIdleEnergy();
				energyUsageBW.write(Double.toString(EnergyModel.getTotalEnergy()) + ", ");
				energyUsageBW.write(Double.toString(EnergyModel.getTotalRouterEnergy()) + ", ");
				energyUsageBW.write(Double.toString(EnergyModel.getTotalFogNodeEnergy()) + ", ");
				energyUsageBW.write(Double.toString(EnergyModel.getIdleEnergy()));
				
				
				/*The 10th line of information in the generic results file is formatted as: 
				 * total energy;dynamic network energy;dynamic fog node energy;
				 */
				String genericResult10 = Double.toString(EnergyModel.getTotalEnergy()) + SimSettings.DELIMITER +
Double.toString(EnergyModel.getTotalRouterEnergy()) + SimSettings.DELIMITER + Double.toString(EnergyModel.getTotalFogNodeEnergy()) + 
SimSettings.DELIMITER;
				
		
				
				appendToFile(genericBWs[i], genericResult1);
				appendToFile(genericBWs[i], genericResult2);
				appendToFile(genericBWs[i], genericResult3);
				appendToFile(genericBWs[i], genericResult4);
				appendToFile(genericBWs[i], genericResult5);
				appendToFile(genericBWs[i], genericResult6);
				appendToFile(genericBWs[i], genericResult7);
				appendToFile(genericBWs[i], genericResult8);
				appendToFile(genericBWs[i], genericResult9);
				appendToFile(genericBWs[i], genericResult10);

			}
			
			/*this prints to the ENERGY_USAGE FILE. 
			 * IMPORTANT: the first FOUR values are correct for: total energy, dynamic network energy, dynamic fog energy, idle energy
			 * this is inside a for loop, so there may be more than four entries in the ENERGY_USAGE file -- ignore those
			 */
			EnergyModel.calculateTotalIdleEnergy();
			energyUsageBW.write(Double.toString(EnergyModel.getTotalEnergy()) + ", ");
			energyUsageBW.write(Double.toString(EnergyModel.getTotalRouterEnergy()) + ", ");
			energyUsageBW.write(Double.toString(EnergyModel.getTotalFogNodeEnergy()) + ", ");
			energyUsageBW.write(Double.toString(EnergyModel.getIdleEnergy()));

			// close open files
			if (SimSettings.getInstance().getDeepFileLoggingEnabled()) {
				successBW.close();
				failBW.close();
			}
			
			vmLoadBW.close();
			fnMipsUtilBW.close(); // Shaik added
			fnNwUtilBW.close(); // Shaik added
			locationBW.close();
			distBW.close();// Shaik added
			distBackBW.close();
			hopBW.close(); // Shaik added
			hopsBackBW.close();
			hafaNumHostsBW.close();
			hafaNumMsgsBW.close();
			hafaNumPuddlesBW.close();
			energyUsageBW.close();
			
			for (int i = 0; i < numOfAppTypes + 1; i++) {
				if (i < numOfAppTypes) {
					// if related app is not used in this simulation, just discard it
					if (SimSettings.getInstance().getTaskLookUpTable()[i][0] == 0)
						continue;
				}
				genericBWs[i].close();
			}
		} // end - print metrics to log files
		
		printLine("Mobile Devices Moving? : " + SimSettings.getInstance().areMobileDevicesMoving());
		printLine("# of tasks: " + (failedTask[numOfAppTypes] + completedTask[numOfAppTypes]));
		
		// Do not provide info regarding warmup tasks; Commenting line below. - Shaik modified
		printLine("# of warm up tasks: "+ warmUpTasks);

		// Shaik commented the following - Redundant info
		//printLine("# of failed tasks: " + failedTask[numOfAppTypes]);
		//printLine("# of completed tasks: " + completedTask[numOfAppTypes]);
		//printLine("# of uncompleted tasks: " + uncompletedTask[numOfAppTypes]);
		
		printLine("# of failed tasks due to vm capacity/LAN bw/mobility: "
				+ rejectedTaskDueToVmCapacity[numOfAppTypes]
				+ "/" + +failedTaskDuetoLanBw[numOfAppTypes] 
				+ "/" + failedTaskDuetoMobility[numOfAppTypes]);

		// Shaik added
		printLine("# of failed tasks due to lack of node capacity/lack of network bandwidth/unacceptable latency: "
				+ rejectedTaskDueToLackofNodeCapacity[numOfAppTypes]
				+ "/" + rejectedTaskDueToLackofNetworkBandwidth[numOfAppTypes]
				+ "/" + rejectedTaskDueToUnacceptableLatency[numOfAppTypes]);
	
		// Shaik added
		printLine("Submitted tasks: "
				+ (failedTask[numOfAppTypes] + completedTask[numOfAppTypes]+ uncompletedTask[numOfAppTypes] ) 
				+" ( Completed:" + completedTask[numOfAppTypes] 
				+ " / Failed:" + failedTask[numOfAppTypes] 
				+ " / Uncompleted:" + uncompletedTask[numOfAppTypes] );

		printLine("percentage of failed tasks: "
				+ String.format("%.6f", ((double) failedTask[numOfAppTypes] * (double) 100)
						/ (double) (completedTask[numOfAppTypes] + failedTask[numOfAppTypes]))
				+ "%");

		// Shaik modified the three lines below.
		/*printLine("average service time: "
				+ String.format("%.6f", serviceTime[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds. (" + "on Cloudlet: "
				+ String.format("%.6f", serviceTimeOnCloudlet[numOfAppTypes] / (double) completedTaskOnCloudlet[numOfAppTypes])
				+ ", " + "on Cloud: "
				+ String.format("%.6f", serviceTimeOnCloud[numOfAppTypes] / (double) completedTaskOnCloud[numOfAppTypes])
				+ ")");
		*/
		printLine("\naverage service time: "
				+ String.format("%.6f", serviceTime[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds.");

		/*printLine("average processing time: "
				+ String.format("%.6f", processingTime[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds. (" + "on Cloudlet: "
				+ String.format("%.6f", processingTimeOnCloudlet[numOfAppTypes] / (double) completedTaskOnCloudlet[numOfAppTypes])
				+ ", " + "on Cloud: " 
				+ String.format("%.6f", processingTimeOnCloud[numOfAppTypes] / (double) completedTaskOnCloud[numOfAppTypes])
				+ ")");
		*/
	
		printLine("average processing time: "
				+ String.format("%.6f", processingTime[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds.");

		/*printLine("average network delay: "
				+ String.format("%.6f", networkDelay[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds. (" + "LAN delay: "
				+ String.format("%.6f", lanDelay[numOfAppTypes] / (double) completedTaskOnCloudlet[numOfAppTypes])
				+ ", " + "WAN delay: "
				+ String.format("%.6f", wanDelay[numOfAppTypes] / (double) completedTaskOnCloud[numOfAppTypes]) + ")");
		*/
		
		printLine("average network delay: "
				+ String.format("%.6f", networkDelay[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds.");

/*		printLine("\naverage server utilization: " 
				+ String.format("%.6f", totalVmLoad / (double) vmLoadList.size()) + "%");
		printLine("average Fog server utilization: " 
				+ String.format("%.6f", totalFnMipsUtil / (double) fnMipsUtilList.size()) + "%"); // Shaik added
		printLine("average Fog network utilization: " 
				+ String.format("%.6f", totalFnNwUtil / (double) fnNwUtilList.size()) + "%"); // Shaik added
*/
		printLine("Tasks executed per fog layer: ");
		for(int i = 1; i <= SimSettings.getInstance().getMaxLevels(); i++) //From 1 to MAX_LEVELS because there won't be network apps running on mobile devices at level 0
			printLine(String.format("\tLayer %d:\t%d", i, levelCloudletCount[i]));
		
		// Shaik added
		double averageTaskCost = (completedTask[numOfAppTypes] == 0) ? 0.0 : (cost[numOfAppTypes] / (double) completedTask[numOfAppTypes]);
		printLine("\nAverage cost: $" + String.format("%.6f", averageTaskCost)); // Shaik updated
		
		printLine("Processing Time: " + processingTime[numOfAppTypes]);
		
		double averageDistance = (completedTask[numOfAppTypes] == 0) ? 0.0 : (totalDist[numOfAppTypes] / (double) completedTask[numOfAppTypes]);
		printLine("Average Distance from task to host: " + String.format("%.2f", averageDistance));

		double averageUserDistance = (completedTask[numOfAppTypes] == 0) ? 0.0 : (totalUserDist[numOfAppTypes] / (double) completedTask[numOfAppTypes]);
		printLine("Average Distance from host to user: " + String.format("%.2f", averageUserDistance));
		
		double averageHops = (completedTask[numOfAppTypes] == 0) ? 0.0 : (totalHops[numOfAppTypes] / (double) completedTask[numOfAppTypes]);
		printLine("Average number of hops from task to host: " + String.format("%.2f", averageHops));
		
		double averageHopsBack = (completedTask[numOfAppTypes] == 0) ? 0.0 : (totalHopsBack[numOfAppTypes] / (double) completedTask[numOfAppTypes]);
		printLine("Average number of hops from host to user: " + String.format("%.2f", averageHopsBack));
		
		double averageNumHosts = SimManager.getInstance().getEdgeOrchestrator().getAvgNumProspectiveHosts();
		printLine("Average number of prospective hosts considered for placement: " + String.format("%.2f", averageNumHosts));

		double averageNumMessages = SimManager.getInstance().getEdgeOrchestrator().getAvgNumMessages();
		printLine("Average number of messages exchanged for placement: " + String.format("%.2f", averageNumMessages));
		
		if (SimManager.getInstance().getEdgeOrchestrator() instanceof HAFAOrchestrator) {
			double averageNumPuddles = SimManager.getInstance().getEdgeOrchestrator().getAvgNumPuddlesSearched();
			printLine("Average number of Puddles searched for placement: " + String.format("%.2f", averageNumPuddles));
		}
		
		//Qian print average fog nodes utilization in each level.
		getTotalFogNodesCountInEachLevel();
		printLine("\nPercentage of fog nodes executing atleast one task:"); // Shaik modified
		for (int i = 0; i < 7; i++) {
			printLine("\tLevel " + (i + 1) + ": " + String.format("%.6f", ((double)levelFogNodeCount[i] / (double)totalNodesNmuberInEachLevel[i] * 100)) + " %");
		}
		
		printLine("\nAverage fog node utilization per layer:"); // Shaik added
		totalMipsUtil = 0;
		for (int i = 0; i < fogLayerAvgMipsUtil.length; i++) {
			printLine("\tLevel " + (i + 1) + ": " + String.format("%.6f", ((double)fogLayerAvgMipsUtil[i])));
			totalMipsUtil += (double)fogLayerAvgMipsUtil[i];
		}

		printLine("\nAverage fog network utilization per layer:"); // Shaik added
		totalNwUtil = 0;
		for (int i = 0; i < fogLayerAvgNwUtil.length; i++) {
			printLine("\tLevel " + (i + 1) + ": " + String.format("%.6f", ((double)fogLayerAvgNwUtil[i])));
			totalNwUtil += (double)fogLayerAvgNwUtil[i];
		}
		
		printLine("average Fog server utilization: " 
				+ String.format("%.6f", totalMipsUtil / (double)fogLayerAvgMipsUtil.length) + "%"); // Shaik added
		printLine("average Fog network utilization: " 
				+ String.format("%.6f", totalNwUtil / (double)fogLayerAvgNwUtil.length) + "%"); // Shaik added

		
		// clear related collections (map list etc.)
		taskMap.clear();
		vmLoadList.clear();
		fnMipsUtilList.clear(); // Shaik added
		fnNwUtilList.clear(); // Shaik added
		centerFileW.close();
	} // end simStopped()

	
	
	/**
	 * Return perceived delay i.e. service time of a given task after completion of its execution to verify if the task execution response took longer than allowed time to reach the user. 
	 * @param taskId
	 * @return
	 */
	public double getTaskPerceivedDelay (int taskId) {
		double perceivedDelay = taskMap.get(taskId).getServiceTime();
		return perceivedDelay;
	} 
	
	
	
	/**
	 * @return the printLogEnabled
	 */
	public static boolean isPrintLogEnabled() {
		return printLogEnabled;
	}

	
	/**
	 * @param printLogEnabled the printLogEnabled to set
	 */
	public static void setPrintLogEnabled(boolean printLogEnabled) {
		SimLogger.printLogEnabled = printLogEnabled;
	}

	
	/**
	 * @return the filePrefix
	 */
	public String getFilePrefix() {
		return filePrefix;
	}

	
	/**
	 * @param filePrefix the filePrefix to set
	 */
	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	
	/**
	 * @return the outputFolder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	
	/**
	 * @param outputFolder the outputFolder to set
	 */
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	
	/**
	 * @return the taskMap
	 */
	public Map<Integer, LogItem> getTaskMap() {
		return taskMap;
	}

	
	/**
	 * @param taskMap the taskMap to set
	 */
	public void setTaskMap(Map<Integer, LogItem> taskMap) {
		this.taskMap = taskMap;
	}

	
	/**
	 * @return the vmLoadList
	 */
	public LinkedList<VmLoadLogItem> getVmLoadList() {
		return vmLoadList;
	}

	
	/**
	 * @param vmLoadList the vmLoadList to set
	 */
	public void setVmLoadList(LinkedList<VmLoadLogItem> vmLoadList) {
		this.vmLoadList = vmLoadList;
	}

	
	/**
	 * @return the centerLogFile
	 */
	public File getCenterLogFile() {
		return centerLogFile;
	}

	
	/**
	 * @param centerLogFile the centerLogFile to set
	 */
	public void setCenterLogFile(File centerLogFile) {
		this.centerLogFile = centerLogFile;
	}

	
	/**
	 * @return the centerFileW
	 */
	public PrintWriter getCenterFileW() {
		return centerFileW;
	}

	
	/**
	 * @param centerFileW the centerFileW to set
	 */
	public void setCenterFileW(PrintWriter centerFileW) {
		this.centerFileW = centerFileW;
	}

	
	/**
	 * @return the utlizationArray
	 */
	public ArrayList<Integer> getUtlizationArray() {
		return utlizationArray;
	}

	
	/**
	 * @param utlizationArray the utlizationArray to set
	 */
	public void setUtlizationArray(ArrayList<Integer> utlizationArray) {
		this.utlizationArray = utlizationArray;
	}

	
	/**
	 * @return the singleton
	 */
	public static SimLogger getSingleton() {
		return singleton;
	}

	
	/**
	 * @param singleton the singleton to set
	 */
	public static void setSingleton(SimLogger singleton) {
		SimLogger.singleton = singleton;
	}

	
	/**
	 * @return the totalNodesNmuberInEachLevel
	 */
	public int[] getTotalNodesNmuberInEachLevel() {
		return totalNodesNmuberInEachLevel;
	}

	
	/**
	 * @param totalNodesNmuberInEachLevel the totalNodesNmuberInEachLevel to set
	 */
	public void setTotalNodesNmuberInEachLevel(int[] totalNodesNmuberInEachLevel) {
		this.totalNodesNmuberInEachLevel = totalNodesNmuberInEachLevel;
	}

	
	/**
	 * @return the levelFogNodeCount
	 */
	public int[] getLevelFogNodeCount() {
		return levelFogNodeCount;
	}

	
	/**
	 * @param levelFogNodeCount the levelFogNodeCount to set
	 */
	public void setLevelFogNodeCount(int[] levelFogNodeCount) {
		this.levelFogNodeCount = levelFogNodeCount;
	}

	
	/**
	 * @return the levelCloudletCount
	 */
	public int[] getLevelCloudletCount() {
		return levelCloudletCount;
	}

	
	/**
	 * @param levelCloudletCount the levelCloudletCount to set
	 */
	public void setLevelCloudletCount(int[] levelCloudletCount) {
		this.levelCloudletCount = levelCloudletCount;
	}

	
	/**
	 * @param fileLogEnabled the fileLogEnabled to set
	 */
	public static void setFileLogEnabled(boolean fileLogEnabled) {
		SimLogger.fileLogEnabled = fileLogEnabled;
	}
	
}


/**
 * 
 * @author szs0117
 *
 */
class VmLoadLogItem {
	
	private double time;
	private double vmLoad;

	
	/**
	 * 
	 * @param _time
	 * @param _vmLoad
	 */
	VmLoadLogItem(double _time, double _vmLoad) {
		time = _time;
		vmLoad = _vmLoad;
	}

	
	/**
	 * 
	 * @return
	 */
	public double getLoad() {
		return vmLoad;
	}

	
	/**
	 * 
	 */
	public String toString() {
		return time + SimSettings.DELIMITER + vmLoad;
	}
}


/**
 * @author szs0117
 *
 */
class FNMipsUtilLogItem {								
	private double time;							
	private int hostId;							
	private int hostLevel;							
	private double fnMipsUtil;							

	
	/**
	 * 
	 * @param _time
	 * @param _hostId
	 * @param _hostLevel
	 * @param _fnMipsUtil
	 */
	FNMipsUtilLogItem(double _time, int _hostId, int _hostLevel, double _fnMipsUtil) {							
		time = _time;						
		hostId = _hostId;						
		hostLevel = _hostLevel;						
		fnMipsUtil = _fnMipsUtil;						
	}							

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FNMipsUtilLogItem [time=" + time + ", hostId=" + hostId + ", hostLevel=" + hostLevel + ", fnMipsUtil="
				+ fnMipsUtil + "]";
	}

	
	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	
	/**
	 * @param time the time to set
	 */
	public void setTime(double time) {
		this.time = time;
	}

	
	/**
	 * @return the hostId
	 */
	public int getHostId() {
		return hostId;
	}

	
	/**
	 * @param hostId the hostId to set
	 */
	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	
	/**
	 * @return the hostLevel
	 */
	public int getHostLevel() {
		return hostLevel;
	}

	
	/**
	 * @param hostLevel the hostLevel to set
	 */
	public void setHostLevel(int hostLevel) {
		this.hostLevel = hostLevel;
	}

	
	/**
	 * @return the fnMipsUtil
	 */
	public double getFnMipsUtil() {
		return fnMipsUtil;
	}

	
	/**
	 * @param fnMipsUtil the fnMipsUtil to set
	 */
	public void setFnMipsUtil(double fnMipsUtil) {
		this.fnMipsUtil = fnMipsUtil;
	}
	
}								


/**
 * @author szs0117
 *
 */
class FNNwUtilLogItem {								
	private double time;							
	private int hostId;							
	private int hostLevel;							
	private double fnNwUtil;
	
	
	/**
	 * @param time
	 * @param hostId
	 * @param hostLevel
	 * @param fnNwUtil
	 */
	public FNNwUtilLogItem(double time, int hostId, int hostLevel, double fnNwUtil) {
		super();
		this.time = time;
		this.hostId = hostId;
		this.hostLevel = hostLevel;
		this.fnNwUtil = fnNwUtil;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FNNwUtilLogItem [time=" + time + ", hostId=" + hostId + ", hostLevel=" + hostLevel + ", fnNwUtil="
				+ fnNwUtil + "]";
	}

	
	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	
	/**
	 * @param time the time to set
	 */
	public void setTime(double time) {
		this.time = time;
	}

	
	/**
	 * @return the hostId
	 */
	public int getHostId() {
		return hostId;
	}

	
	/**
	 * @param hostId the hostId to set
	 */
	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	
	/**
	 * @return the hostLevel
	 */
	public int getHostLevel() {
		return hostLevel;
	}

	
	/**
	 * @param hostLevel the hostLevel to set
	 */
	public void setHostLevel(int hostLevel) {
		this.hostLevel = hostLevel;
	}

	
	/**
	 * @return the fnNwUtil
	 */
	public double getFnNwUtil() {
		return fnNwUtil;
	}

	
	/**
	 * @param fnNwUtil the fnNwUtil to set
	 */
	public void setFnNwUtil(double fnNwUtil) {
		this.fnNwUtil = fnNwUtil;
	}			

}


/**
 * 
 * @author szs0117
 *
 */
class LogItem {
	
	
	/**
	 * @return the datacenterId
	 */
	public int getDatacenterId() {
		return datacenterId;
	}

	
	/**
	 * @param datacenterId the datacenterId to set
	 */
	public void setDatacenterId(int datacenterId) {
		this.datacenterId = datacenterId;
	}

	
	/**
	 * @return the hostId
	 */
	public int getHostId() {
		return hostId;
	}

	
	/**
	 * @param hostId the hostId to set
	 */
	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	
	/**
	 * @return the vmId
	 */
	public int getVmId() {
		return vmId;
	}

	
	/**
	 * @param vmId the vmId to set
	 */
	public void setVmId(int vmId) {
		this.vmId = vmId;
	}

	
	/**
	 * @return the taskLenght
	 */
	public int getTaskLength() {
		return taskLength;
	}

	
	/**
	 * @param taskLength the taskLenght to set
	 */
	public void setTaskLength(int taskLength) {
		this.taskLength = taskLength;
	}

	
	/**
	 * @return the taskInputType
	 */
	public int getTaskInputType() {
		return taskInputType;
	}

	
	/**
	 * @param taskInputType the taskInputType to set
	 */
	public void setTaskInputType(int taskInputType) {
		this.taskInputType = taskInputType;
	}

	
	/**
	 * @return the taskOutputSize
	 */
	public int getTaskOutputSize() {
		return taskOutputSize;
	}

	
	/**
	 * @param taskOutputSize the taskOutputSize to set
	 */
	public void setTaskOutputSize(int taskOutputSize) {
		this.taskOutputSize = taskOutputSize;
	}

	
	/**
	 * @return the numberOfHops
	 */
	public int getNumberOfHops() {
		return numberOfHops;
	}

	
	/**
	 * @param numberOfHops the numberOfHops to set
	 */
	public void setNumberOfHops(int numberOfHops) {
		this.numberOfHops = numberOfHops;
	}

	
	/**
	 * @return the taskStartTime
	 */
	public double getTaskStartTime() {
		return taskStartTime;
	}

	
	/**
	 * @param taskStartTime the taskStartTime to set
	 */
	public void setTaskStartTime(double taskStartTime) {
		this.taskStartTime = taskStartTime;
	}

	
	/**
	 * @return the taskEndTime
	 */
	public double getTaskEndTime() {
		return taskEndTime;
	}

	
	/**
	 * @param taskEndTime the taskEndTime to set
	 */
	public void setTaskEndTime(double taskEndTime) {
		this.taskEndTime = taskEndTime;
	}

	
	/**
	 * @return the bwCost
	 */
	public double getBwCost() {
		return bwCost;
	}

	
	/**
	 * @param bwCost the bwCost to set
	 */
	public void setBwCost(double bwCost) {
		this.bwCost = bwCost;
	}

	
	/**
	 * @return the cpuCost
	 */
	public double getCpuCost() {
		return cpuCost;
	}

	
	/**
	 * @param cpuCost the cpuCost to set
	 */
	public void setCpuCost(double cpuCost) {
		this.cpuCost = cpuCost;
	}

	
	/**
	 * @return the taskCost
	 */
	public double getTaskCost() {
		return taskCost;
	}

	
	/**
	 * @param taskCost the taskCost to set
	 */
	public void setTaskCost(double taskCost) {
		this.taskCost = taskCost;
	}

	
	/**
	 * @return the distanceToHost
	 */
	public double getDistanceToHost() {
		return distanceToHost;
	}

	
	public double getDistanceToUser() {
		return distanceToUser;
	}
	
	/**
	 * @param distanceToHost the distanceToHost to set
	 */
	public void setDistanceToHost(double distanceToHost) {
		this.distanceToHost = distanceToHost;
	}

	public void setDistanceToUser(double distanceToUser) {
		this.distanceToUser = distanceToUser;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(SimLogger.TASK_STATUS status) {
		this.status = status;
	}

	
	/**
	 * @param vmType the vmType to set
	 */
	public void setVmType(int vmType) {
		this.vmType = vmType;
	}

	
	/**
	 * @param taskType the taskType to set
	 */
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}

	
	/**
	 * @param networkDelay the networkDelay to set
	 */
	public void setNetworkDelay(double networkDelay) {
		this.networkDelay = networkDelay;
	}

	
	/**
	 * @param isInWarmUpPeriod the isInWarmUpPeriod to set
	 */
	public void setInWarmUpPeriod(boolean isInWarmUpPeriod) {
		this.isInWarmUpPeriod = isInWarmUpPeriod;
	}

	private SimLogger.TASK_STATUS status;
	private int datacenterId;
	private int hostId;
	private int vmId;
	private int vmType;
	private int taskType;
	private int taskLength;
	private int taskInputType;
	private int taskOutputSize;
	private int numberOfHops;
	private int hopsToUser;
	private double taskStartTime;
	private double taskEndTime;
	private double networkDelay;
	private double bwCost;
	private double cpuCost;
	private boolean isInWarmUpPeriod;
	private double taskCost = 0;
	private double distanceToHost;
	private double distanceToUser;
	
	/**
	 * 
	 * @param _taskStartTime
	 * @param _taskType
	 * @param _taskLenght
	 * @param _taskInputType
	 * @param _taskOutputSize
	 */
	LogItem(double _taskStartTime, int _taskType, int _taskLenght, int _taskInputType, int _taskOutputSize) {
		taskStartTime = _taskStartTime;
		taskType = _taskType;
		taskLength = _taskLenght;
		taskInputType = _taskInputType;
		taskOutputSize = _taskOutputSize;
		status = SimLogger.TASK_STATUS.CREATED;
		taskEndTime = 0;

		if (_taskStartTime < SimSettings.getInstance().getWarmUpPeriod())
			isInWarmUpPeriod = true;
		else
			isInWarmUpPeriod = false;
	}
	
	
	/**
	 * 
	 * @param taskUploadTime
	 */
	public void taskUploadStarted(double taskUploadTime) {
		networkDelay += taskUploadTime;
		status = SimLogger.TASK_STATUS.UPLOADING;
	}

	
	/**
	 * 
	 * @param _datacenterId
	 * @param _hostId
	 * @param _vmId
	 * @param _vmType
	 */
	public void taskUploaded(int _datacenterId, int _hostId, int _vmId, int _vmType) {
		status = SimLogger.TASK_STATUS.PROCESSING;
		datacenterId = _datacenterId;
		hostId = _hostId;
		vmId = _vmId;
		vmType = _vmType;
	}

	
	/**
	 * 
	 * @param taskDownloadTime
	 */
	public void taskDownloadStarted(double taskDownloadTime) {
		networkDelay += taskDownloadTime;
		status = SimLogger.TASK_STATUS.DOWNLOADING;
	}

	
	/**
	 * 
	 * @param _taskEndTime
	 */
	public void taskDownloaded(double _taskEndTime) {
		taskEndTime = _taskEndTime;
		status = SimLogger.TASK_STATUS.COMPLETED;
	}
	
	
	/**
	 * 
	 * @param _taskEndTime
	 * @param cost
	 */
	public void taskDownloaded(double _taskEndTime, double cost) {
		taskEndTime = _taskEndTime;
		taskCost = cost;
		status = SimLogger.TASK_STATUS.COMPLETED;
	}
	
	// Shaik added
	/**
	 * 
	 * @param _taskRejectTime
	 * @param taskStatus
	 */
	public void taskRejectedStatus(double _taskRejectTime, SimLogger.TASK_STATUS taskStatus) {
		taskEndTime = _taskRejectTime;
		status = taskStatus;				
	}

	/**
	 * 
	 * @param _taskRejectTime
	 */
	public void taskRejectedDueToVMCapacity(double _taskRejectTime) {
		taskEndTime = _taskRejectTime;
		status = SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY;
	}

	
	/**
	 * 
	 * @param _taskRejectTime
	 * @param _vmType
	 */
	public void taskRejectedDueToBandwidth(double _taskRejectTime, int _vmType) {
		vmType = _vmType;
		taskEndTime = _taskRejectTime;
		status = SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH;
	}

	
	/**
	 * 
	 * @param _time
	 */
	public void taskFailedDueToBandwidth(double _time) {
		taskEndTime = _time;
		status = SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH;
	}

	
	/**
	 * 
	 * @param _time
	 */
	public void taskFailedDueToMobility(double _time) {
		taskEndTime = _time;
		status = SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY;
	}

	
	/**
	 * 
	 * @param _bwCost
	 * @param _cpuCos
	 */
	public void setCost(double _bwCost, double _cpuCos) {
		bwCost = _bwCost;
		cpuCost = _cpuCos;
	}

	
	/**
	 * 
	 * @return
	 */
	public boolean isInWarmUpPeriod() {
		return isInWarmUpPeriod;
	}

	
	/**
	 * 
	 * @return
	 */
	public double getCost() {
		//return bwCost + cpuCost;
		return taskCost;
	}

	
	/**
	 * 
	 * @return
	 */
	public double getNetworkDelay() {
		return networkDelay;
	}

	
	/**
	 * 
	 * @return
	 */
	public double getServiceTime() {
		return taskEndTime - taskStartTime;
	}

	
	/**
	 * 
	 * @return
	 */
	public SimLogger.TASK_STATUS getStatus() {
		return status;
	}

	
	/**
	 * 
	 * @return
	 */
	public int getVmType() {
		return vmType;
	}

	
	/**
	 * 
	 * @return
	 */
	public int getTaskType() {
		return taskType;
	}

	
	/**
	 * 
	 * @param taskId
	 * @return
	 */
	public String toString(int taskId) {
		String result = taskId + SimSettings.DELIMITER + datacenterId + SimSettings.DELIMITER + hostId
				+ SimSettings.DELIMITER + vmId + SimSettings.DELIMITER + vmType + SimSettings.DELIMITER + taskType
				+ SimSettings.DELIMITER + taskLength + SimSettings.DELIMITER + taskInputType + SimSettings.DELIMITER
				+ taskOutputSize + SimSettings.DELIMITER + taskStartTime + SimSettings.DELIMITER + taskEndTime
				+ SimSettings.DELIMITER;

		if (status == SimLogger.TASK_STATUS.COMPLETED)
			result += networkDelay;
		else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY)
			result += "1"; // failure reason 1
		else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH)
			result += "2"; // failure reason 2
		else if (status == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH)
			result += "3"; // failure reason 3
		else if (status == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY)
			result += "4"; // failure reason 4
		else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_LACK_OF_NODE_CAPACITY)
			result += "5"; // failure reason 5
		else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_LACK_OF_NETWORK_BANDWIDTH)
			result += "6"; // failure reason 6
		else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_UNACCEPTABLE_LATENCY)
			result += "7"; // failure reason 7
		else
			result += "0"; // default failure reason
		return result;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getHostDist() {
		return distanceToHost;
	}
	
	
	/**
	 * 
	 * @param in
	 */
	public void setDistance(double in) {
		distanceToHost = in;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getHops() {
		return numberOfHops;
	}
	
	public int getHopsToUser() {
		return hopsToUser;
	}
	
	/**
	 * 
	 * @param in
	 */
	public void setHops(int in) {
		numberOfHops = in;
	}
	
	public void setHopsToUser(int in) {
		hopsToUser = in;
	}



}