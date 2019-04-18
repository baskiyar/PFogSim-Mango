/*
 * Title:        EdgeCloudSim - Load Generator Model
 * 
 * Description: 
 * LoadGeneratorModel is an abstract class which is used for 
 * deciding task generation pattern via a task list. For those who
 * wants to add a custom Load Generator Model to EdgeCloudSim should
 * extend this class and provide a concreate instance via ScenarioFactory
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.task_generator;

import java.util.List;

import edu.boun.edgecloudsim.utils.EdgeTask;


/**
 * 
 * @author szs0117
 *
 */
public abstract class LoadGeneratorModel {
	protected List<EdgeTask> taskList;
	protected int numberOfMobileDevices;
	protected double simulationTime;
	protected String simScenario;
	
	
	/**
	 * 
	 * @param _numberOfMobileDevices
	 * @param _simulationTime
	 * @param _simScenario
	 */
	public LoadGeneratorModel(int _numberOfMobileDevices, double _simulationTime, String _simScenario){
		numberOfMobileDevices=_numberOfMobileDevices;
		simulationTime=_simulationTime;
		simScenario=_simScenario;
	};
	
	
	/**
	 * each task has a virtual start time
	 * it will be used while generating task
	 */
	public List<EdgeTask> getTaskList() {
		return taskList;
	}

	
	/**
	 * fill task list according to related task generation model
	 */
	public abstract void initializeModel();

	
	/**
	 * @return the numberOfMobileDevices
	 */
	public int getNumberOfMobileDevices() {
		return numberOfMobileDevices;
	}

	
	/**
	 * @param numberOfMobileDevices the numberOfMobileDevices to set
	 */
	public void setNumberOfMobileDevices(int numberOfMobileDevices) {
		this.numberOfMobileDevices = numberOfMobileDevices;
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

	
	/**
	 * @param taskList the taskList to set
	 */
	public void setTaskList(List<EdgeTask> taskList) {
		this.taskList = taskList;
	}
}
