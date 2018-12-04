/**
 * Link Class for modeling network links and latencies
 * @author jih0007
 * 
 */

package edu.auburn.pFogSim.netsim;

import edu.auburn.pFogSim.util.DataInterpreter;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import javafx.util.Pair;


public class Link {

	private Location leftCoord;
	private Location rightCoord;
	private double leftLatency;
	private double rightLatency;
	private double LATENCY_MULTIPLIER = 0.00000003;//data transfer at about 0.03 milliseconds per kilometer
	/**
	 * Constructor<br>
	 * Link cannot be circular, if left and right endpoints are the same throw IllegalArgumentException
	 * @param rLink
	 * @param lLink
	 * @param rLat
	 * @param lLat
	 */
	public Link(Location rLink, Location lLink, double rLat, double lLat) {
		rightCoord = rLink;
		leftCoord = lLink;
		//double dist = Math.sqrt(Math.pow(rLink.getXPos() - lLink.getXPos(), 2) + Math.pow(rLink.getYPos() - lLink.getYPos(), 2));
		double dist = DataInterpreter.measure(rLink.getYPos(), rLink.getXPos(), lLink.getYPos(), lLink.getXPos());
		rightLatency = dist * LATENCY_MULTIPLIER;
		leftLatency = dist * LATENCY_MULTIPLIER;
		/*rightLatency = rLat;
		leftLatency = lLat;*/
		if (!validateCoords()) {
			throw new IllegalArgumentException();
		}
	}
	public Link() {}
	/**
	 * get the latency as moving FROM the right node TO the left node
	 * @return the left latency
	 */
	public double getLeftLatency() {
		return leftLatency;
	}
	/**
	 * get the latency as moving FROM the left node TO the right node
	 * @return the right latency
	 */
	public double getRightLatency() {
		return rightLatency;
	}
	/**
	 * get the coords of the right node associated with this link
	 * @return right link
	 */
	public Location getRightLink() {
		return rightCoord;
	}
	/**
	 * get the coords of the left node associated with this link
	 * @return left link
	 */
	public Location getLeftLink() {
		return leftCoord;
	}
	/**
	 * Gives the opposite coordinate from the given coord on this link<br>
	 * if the input coord is not on this link throw an IllegalArgumentException
	 * @param in the src coord
	 * @return the destination coord
	 */
	public Location getOutgoingLink(Location in) {
		if (in.equals(leftCoord)) {
			return rightCoord;
		}
		else if (in.equals(rightCoord)) {
			return leftCoord;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	/**
	 * Gives the outgoing latency to traverse this link<br>
	 * if the input coord is not on this link throw an IllegalArgumentException
	 * @param in the src coord
	 * @return latency to the destination coord
	 */
	public double getOutgoingLat(Location in) {
		if (in.equals(leftCoord)) {
			return rightLatency;
		}
		else if (in.equals(rightCoord)) {
			return leftLatency;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	/**
	 * set the latency going TO the left node
	 * @param in
	 */
	public void setLeftLat(int in) {
		leftLatency = in;
	}
	/**
	 * set the latency going TO the right node
	 * @param in
	 */
	public void setRightLat(int in) {
		rightLatency = in;
	}
	/**
	 * set the coordinates of the left link
	 * @param xin
	 * @param yin
	 */
	public void setLeftLink(double xin, double yin) {
		leftCoord = new Location(xin, yin);
	}
	/**
	 * designate the left link coord
	 * @param in
	 */
	public void setLeftLink(Location in) {
		leftCoord = in;
	}
	/**
	 * set the coordinates of the right link
	 * @param xin
	 * @param yin
	 */
	public void setRightLink(double xin, double yin) {
		rightCoord = new Location(xin, yin);
	}
	/**
	 * designate the right link coord
	 * @param in
	 */
	public void setRightLink(Location in) {
		rightCoord = in;
	}
	/**
	 * compares test link to this link<br>
	 * returns true if the left links are the same, and the right links are the same, and the latencies are the same.
	 * 
	 * @param test
	 * @return the equality of the links
	 */
	public boolean equals(Link test) {
		//if the the left links are the same coords and the right links are the same coords
		if (this.getLeftLink().equals(test.getLeftLink()) && this.getRightLink().equals(test.getRightLink())) {
			//now make sure that the latencies match
			if (this.getLeftLatency() == test.getLeftLatency() && this.getRightLatency() == test.getRightLatency()) {
				return true;
			}
			else {
				return false;
			}
		}
		//if the link is backwards the values might still be equal
		else if (this.getLeftLink().equals(test.getRightLink()) && this.getRightLink().equals(test.getLeftLink())) {
			//make sure that if the coords are reversed that latencies are also reversed
			if (this.getLeftLatency() == test.getRightLatency() && this.getRightLatency() == test.getLeftLatency()) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
		/*the great monster, can be used in place of the current implementation
		 * return ((this.getLeftLink().equals(test.getLeftLink()) && this.getRightLink().equals(test.getRightLink())
				&& this.getLeftLatency() == test.getLeftLatency() && this.getRightLatency() == test.getRightLatency())
				||(this.getLeftLink().equals(test.getRightLink()) && this.getRightLink().equals(test.getLeftLink())
				&& this.getLeftLatency() == test.getRightLatency() && this.getRightLatency() == test.getLeftLatency()));*/
	}
	/**
	 * used to test if this link and the input link run between the same nodes without checking their latencies
	 * @param test
	 * @return equality of endpoints
	 */
	public boolean equalEndPoints(Link test) {
		//check to see if the left links and right links agree
		if (this.getLeftLink().equals(test.getLeftLink()) && this.getRightLink().equals(test.getRightLink())) {
			return true;
		}
		//or if the link is backwards
		else if (this.getLeftLink().equals(test.getRightLink()) && this.getRightLink().equals(test.getLeftLink())) {
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * Link should not be circular
	 * @return true if left and right coords are different<br>
	 * false if left and right coords are the same<br>
	 * false if either coord is null
	 */
	public boolean validateCoords() {
		try {
			//SimLogger.printLine(leftCoord.getXPos() + "\t" + rightCoord.getXPos());
			return !leftCoord.equals(rightCoord);
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	/**
	 * makes sure that latencies are positive
	 * @return
	 */
	public boolean validateLat() {
		return (rightLatency >= 0 && leftLatency >= 0);
	}
}
