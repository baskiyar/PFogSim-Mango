/*
 * Title:        EdgeCloudSim - Sample Application
 * 
 * Description:  Sample application for EdgeCloudSim
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.sample_application;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

//import com.sun.xml.internal.bind.v2.runtime.output.SAXOutput;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

import edu.auburn.pFogSim.util.*;

public class mainApp {
	
	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		/*
		 * Try to get FogHierClust.java to run
		 * 
		 */
		
		// Comment the following line for detailed logging
		//Log.disable();

		//enable console output and file output of this application
		SimLogger.enablePrintLog();
		DataInterpreter.initialize();
		try {
			DataInterpreter.readFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//CJ added linksFile to supply the link xml file, had to adjust all constructors that
		//	use these file to seamlessly use it
		int iterationNumber = 3; // index for the list of n scenarios in properties file is from 0..n-1
		String configFile = "";
		String outputFolder = "";
		String edgeDevicesFile = "";
		String applicationsFile = "";
		//String linksFile = "scripts/sample_application/config/links_test.xml";
		//String linksFile = "small_link_test.xml";
		String linksFile = "links_test.xml";

		if (args.length == 5){
			configFile = args[0];
			edgeDevicesFile = args[1];
			applicationsFile = args[2];
			outputFolder = args[3];
			iterationNumber = Integer.parseInt(args[4]);
		}
		else{
			SimLogger.printLine("Simulation setting file, output folder and iteration number are not provided! Using default ones...");
			configFile = "scripts/sample_application/config/default_config.properties";
			applicationsFile = "scripts/sample_application/config/applications.xml";
			//edgeDevicesFile = "scripts/sample_application/config/edge_devices_test.xml";
			//edgeDevicesFile = "small_node_test.xml";
			edgeDevicesFile = "node_test.xml";
			outputFolder = "sim_results/ite" + iterationNumber;
		}

		//load settings from configuration file
		SimSettings SS = SimSettings.getInstance();
		if(SS.initialize(configFile, edgeDevicesFile, applicationsFile, linksFile) == false){
			SimLogger.printLine("cannot initialize simulation settings!");
			System.exit(0);
		}
		
		if(SS.getFileLoggingEnabled()){
			SimLogger.enableFileLog();
			SimUtils.cleanOutputFolder(outputFolder);
		}
		SS.setSimulationSpace(DataInterpreter.getSimulationSpace());
		SS.setMaxLevels(DataInterpreter.getMaxLevels());
		SS.setInputType(DataInterpreter.getInputType());
		SS.setMobileDevicesMoving(iterationNumber >= 9); // 9 is the count of scenarios in properties file.
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date SimulationStartDate = Calendar.getInstance().getTime();
		String now = df.format(SimulationStartDate);
		SimLogger.printLine("Simulation started at " + now);
		SimLogger.printLine("----------------------------------------------------------------------");

		for(int j=SS.getMinNumOfMobileDev(); j<=SS.getMaxNumOfMobileDev(); j+=SS.getMobileDevCounterSize())
		{
			for(int k=0; k<1; k++)
			{
				for(int i=0; i<1; i++)
				{
					String simScenario = SS.getSimulationScenarios()[iterationNumber%9]; // 9 is the count of scenarios in properties file. 
					String orchestratorPolicy = SS.getOrchestratorPolicies()[i];
					Date ScenarioStartDate = Calendar.getInstance().getTime();
					now = df.format(ScenarioStartDate);
					
					SimLogger.printLine("Scenario started at " + now);
					SimLogger.printLine("Scenario: " + simScenario + " - Policy: " + orchestratorPolicy + " - #iteration: " + iterationNumber);
					SimLogger.printLine("Duration: " + SS.getSimulationTime()/3600 + " hour(s) - Poisson: " + SS.getTaskLookUpTable()[0][2] + " - #devices: " + j);
					SimLogger.getInstance().simStarted(outputFolder,"SIMRESULT_" + simScenario + "_"  + orchestratorPolicy + "_" + j + "DEVICES");
					
					try
					{
						// First step: Initialize the CloudSim package. It should be called
						// before creating any entities.
						int num_user = 2;   // number of grid users
						Calendar calendar = Calendar.getInstance();
						boolean trace_flag = false;  // mean trace events
				
						// Initialize the CloudSim library
						CloudSim.init(num_user, calendar, trace_flag, 0.01);
						SimLogger.printLine("CloudSim.init reached");
						// Generate EdgeCloudsim Scenario Factory
						ScenarioFactory sampleFactory = new SampleScenarioFactory(j,SS.getSimulationTime(), orchestratorPolicy, simScenario);
						SimLogger.printLine("ScenarioFactory reached");
						// Generate EdgeCloudSim Simulation Manager
						SimManager manager = new SimManager(sampleFactory, j, simScenario);
						SimLogger.printLine("SimManager reached");
						// Start simulation
						manager.startSimulation();
					}
					catch (Exception e)
					{
						SimLogger.printLine("The simulation has been terminated due to an unexpected error");
						e.printStackTrace();
						System.exit(0);
					}
					
					Date ScenarioEndDate = Calendar.getInstance().getTime();
					now = df.format(ScenarioEndDate);
					SimLogger.printLine("Scenario finished at " + now +  ". It took " + SimUtils.getTimeDifference(ScenarioStartDate,ScenarioEndDate));
					SimLogger.printLine("----------------------------------------------------------------------");
				}//End of orchestrators loop
			}//End of scenarios loop
		}//End of mobile devices loop

		Date SimulationEndDate = Calendar.getInstance().getTime();
		now = df.format(SimulationEndDate);
		SimLogger.printLine("Simulation finished at " + now +  ". It took " + SimUtils.getTimeDifference(SimulationStartDate,SimulationEndDate));
	}
}
