/*
 * Title:        EdgeCloudSim - Simulation Utils
 * 
 * Description:  Utility class providing helper functions
 * 
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.utils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import edu.boun.edgecloudsim.core.SimSettings;


/**
 * 
 * @author szs0117
 *
 */
public class SimUtils {

    public static final Random RNG = new Random(SimSettings.getInstance().getRandomSeed());
    
    
    /**
     * 
     * @param start
     * @param end
     * @return
     */
    public static int getRandomNumber(int start, int end) {
    	//return pd.sample();
		long range = (long)end - (long)start + 1;
		long fraction = (long)(range * RNG.nextDouble());
		return (int)(fraction + start); 
    }
    
    
    /**
     * 
     * @param start
     * @param end
     * @return
     */
    public static double getRandomDoubleNumber(double start, double end) {
    	//return pd.sample();
		double range = end - start;
		double fraction = (range * RNG.nextDouble());
		return (fraction + start); 
    }
    
    
    /**
     * 
     * @param start
     * @param end
     * @return
     */
    public static long getRandomLongNumber(int start, int end) {
    	//return pd.sample();
		long range = (long)end - (long)start + 1;
		long fraction = (long)(range * RNG.nextDouble());
		return (fraction + start); 
    }

    
    /**
     * 
     * @param outputFolder
     */
	public static void cleanOutputFolder(String outputFolder){
		//clean the folder where the result files will be saved
		File dir = new File(outputFolder);
		if(dir.exists() && dir.isDirectory())
		{
			for (File f: dir.listFiles())
			{
				if (f.exists() && f.isFile())
				{
					if(!f.delete())
					{
						SimLogger.printLine("file cannot be cleared: " + f.getAbsolutePath());
						System.exit(0);
					}
				}
			}
		}
		else {
			dir.mkdirs();
			SimLogger.printLine("Output folder created: " + outputFolder);
		}
	}
	
	/**
     * Create the folder where output files will be saved.
     * @param outputFolder
     * @return result output subfolder path
     */
	public static String createOutputFolder(String outputFolder){
		//Create the folder where the result files will be saved
		String subFolderName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		File dir = new File(outputFolder+"/"+subFolderName);
		if(dir.exists() && dir.isDirectory())
		{
//			for (File f: dir.listFiles())
//			{
//				if (f.exists() && f.isFile())
//				{
//					if(!f.delete())
//					{
//						SimLogger.printLine("file cannot be cleared: " + f.getAbsolutePath());
//						System.exit(0);
//					}
//				}
//			}
		}
		else {
			dir.mkdirs();
			SimLogger.printLine("Output folder created in " + outputFolder);
		}
		return dir.getAbsolutePath();
	}
	
	/**
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static String getTimeDifference(Date startDate, Date endDate){
		String result = "";
		long duration  = endDate.getTime() - startDate.getTime();

		long diffInMilli = TimeUnit.MILLISECONDS.toMillis(duration);
		long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
		long diffInDays = TimeUnit.MILLISECONDS.toDays(duration);
		
		if(diffInDays>0)
			result += diffInDays + ((diffInDays>1 == true) ? " Days " : " Day ");
		if(diffInHours>0)
			result += diffInHours % 24 + ((diffInHours>1 == true) ? " Hours " : " Hour ");
		if(diffInMinutes>0)
			result += diffInMinutes % 60 + ((diffInMinutes>1 == true) ? " Minutes " : " Minute ");
		if(diffInSeconds>0)
			result += diffInSeconds % 60 + ((diffInSeconds>1 == true) ? " Seconds" : " Second");
		if(diffInMilli>0 && result.isEmpty())
			result += diffInMilli + ((diffInMilli>1 == true) ? " Milli Seconds" : " Milli Second");
		
		return result;
	}
	
	
	/**
	 * 
	 * @param attractiveness
	 * @return
	 */
	public static SimSettings.PLACE_TYPES stringToPlace(String attractiveness){
		SimSettings.PLACE_TYPES placeType = null;
		if(attractiveness.equals("1"))
			placeType = SimSettings.PLACE_TYPES.ATTRACTIVENESS_L1;
		else if(attractiveness.equals("2"))
			placeType = SimSettings.PLACE_TYPES.ATTRACTIVENESS_L2;
		else if(attractiveness.equals("3"))
			placeType = SimSettings.PLACE_TYPES.ATTRACTIVENESS_L3;
		else{
			SimLogger.printLine("Unknown attractiveness level! Terminating simulation...");
	    	System.exit(0);
		}
		
		return placeType;
	}
	
	
	/**
	 * @return the rng
	 */
	public static Random getRng() {
		return RNG;
	}
}
