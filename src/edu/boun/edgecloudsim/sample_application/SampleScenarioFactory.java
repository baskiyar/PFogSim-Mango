/*
 * Title:        EdgeCloudSim - Sample Scenario Factory
 * 
 * Description:  Sample factory providing the default
 *               instances of required abstract classes 
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.sample_application;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import edu.auburn.pFogSim.mobility.GPSVectorMobility;
import edu.auburn.pFogSim.mobility.MobilityModel;
import edu.auburn.pFogSim.mobility.XYVectorMobility;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.orchestrator.CentralOrchestrator;
import edu.auburn.pFogSim.orchestrator.CloudOnlyOrchestrator;
import edu.auburn.pFogSim.orchestrator.EdgeOnlyOrchestrator;
import edu.auburn.pFogSim.orchestrator.FixedNodeOrchestrator;
import edu.auburn.pFogSim.orchestrator.LocalOnlyOrchestrator;
import edu.auburn.pFogSim.orchestrator.PuddleOrchestrator;
import edu.auburn.pFogSim.orchestrator.SelectedLevelsOrchestrator;
import edu.auburn.pFogSim.orchestrator.SelectedNodesOrchestrator;
import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.APP_TYPES;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
//import edu.boun.edgecloudsim.edge_orchestrator.BasicEdgeOrchestrator;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.VmAllocationPolicy_Custom;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.task_generator.IdleActiveLoadGenerator;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.utils.SimLogger;

public class SampleScenarioFactory implements ScenarioFactory {
	private int numOfMobileDevice;
	private double simulationTime;
	private String orchestratorPolicy;
	private String simScenario;
	
	SampleScenarioFactory(int _numOfMobileDevice,
			double _simulationTime,
			String _orchestratorPolicy,
			String _simScenario){
		orchestratorPolicy = _orchestratorPolicy;
		numOfMobileDevice = _numOfMobileDevice;
		simulationTime = _simulationTime;
		simScenario = _simScenario;
	}
	
	@Override
	public LoadGeneratorModel getLoadGeneratorModel() {
		return new IdleActiveLoadGenerator(numOfMobileDevice, simulationTime, simScenario);
	}

	@Override
	public EdgeOrchestrator getEdgeOrchestrator() {
		if (simScenario.equals("PUDDLE_ORCHESTRATOR")) { 
			return new PuddleOrchestrator(orchestratorPolicy, simScenario);
		}
		else if (simScenario.equals("CENTRALIZED_ORCHESTRATOR")) {
			return new CentralOrchestrator(orchestratorPolicy, simScenario);
		}
		else if (simScenario.equals("CLOUD_ONLY")) {
			return new CloudOnlyOrchestrator(orchestratorPolicy, simScenario);
		}
		else if (simScenario.equals("EDGE_ONLY")) {
			return new EdgeOnlyOrchestrator(orchestratorPolicy, simScenario);
		}
		else if (simScenario.equals("LOCAL_ONLY")) {
			return new LocalOnlyOrchestrator(orchestratorPolicy, simScenario);
		}
		else if (simScenario.equals("FIXED_NODE")) {
			return new FixedNodeOrchestrator(orchestratorPolicy, simScenario);
		}
		else if (simScenario.equals("SELECTED_NODES")) {
			return new SelectedNodesOrchestrator(orchestratorPolicy, simScenario);
		}
		else if (simScenario.equals("SELECTED_LEVELS")) {
			return new SelectedLevelsOrchestrator(orchestratorPolicy, simScenario);
		}
		return null;
	}

	@Override
	public MobilityModel getMobilityModel() {
		switch(SimSettings.getInstance().getInputType())
		{
		case("gps"):
			return new GPSVectorMobility(numOfMobileDevice, simulationTime);
		case("xy"):
			return new XYVectorMobility(numOfMobileDevice, simulationTime);
		default:
			SimLogger.printLine("Error determining what type of input for Simulation: i.e \"gps\" or \"xy\"");
			return null;
		}
	}

	@Override
	public NetworkModel getNetworkModel() {
		return new ESBModel(numOfMobileDevice);
	}

	@Override
	public VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> hostList, int dataCenterIndex) {
		return new VmAllocationPolicy_Custom(hostList,dataCenterIndex);
	}

	@Override
	public UtilizationModel getCpuUtilizationModel(APP_TYPES _taskType) {
		return new CpuUtilizationModel_Custom(_taskType);
	}

	/**
	 * @return the numOfMobileDevice
	 */
	public int getNumOfMobileDevice() {
		return numOfMobileDevice;
	}

	/**
	 * @param numOfMobileDevice the numOfMobileDevice to set
	 */
	public void setNumOfMobileDevice(int numOfMobileDevice) {
		this.numOfMobileDevice = numOfMobileDevice;
	}

	/**
	 * @return the simulationTime
	 */
	public double getSimulationTime() {
		return simulationTime;
	}

	/**
	 * @param simulationTime the simulationTime to set
	 */
	public void setSimulationTime(double simulationTime) {
		this.simulationTime = simulationTime;
	}

	/**
	 * @return the orchestratorPolicy
	 */
	public String getOrchestratorPolicy() {
		return orchestratorPolicy;
	}

	/**
	 * @param orchestratorPolicy the orchestratorPolicy to set
	 */
	public void setOrchestratorPolicy(String orchestratorPolicy) {
		this.orchestratorPolicy = orchestratorPolicy;
	}

	/**
	 * @return the simScenario
	 */
	public String getSimScenario() {
		return simScenario;
	}

	/**
	 * @param simScenario the simScenario to set
	 */
	public void setSimScenario(String simScenario) {
		this.simScenario = simScenario;
	}
}
