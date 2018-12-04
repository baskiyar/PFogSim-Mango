package edu.auburn.pFogSim.Exceptions;
/**
 * custom exception for attempting to setup invalid parent-child relationships for puddles
 * @author Jacob I Hall jih0007@auburn.edu
 *
 */
public class BadPuddleParentageException extends RuntimeException {

	public BadPuddleParentageException(String string) {
		super(string);
	}

}
