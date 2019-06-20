package edu.boun.edgecloudsim.energy;
/**
 * Class for energy measurement
 * Written by Matthew Merck and Cameron Berry
 * 
 * @author mlm0175
 *
 */
public class EnergyModel {
	
	//All private values are values to be logged
	private static double totalEnergy;

	public static double getDownloadEnergy(long taskFileSize, double nJperBit) {
		return 0;
	}
	
	public static double getUploadEnergy() {
		return 0;
	}
	
	public static double calculateTotalIdleEnergy() {
		return 0;
	}
	
	public static double calculateDynamicEnergyConsumption() {
		return 0;
	}
}
