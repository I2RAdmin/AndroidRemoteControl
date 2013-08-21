/**
 * 
 */
package com.i2r.ARC.PCControl;

import org.apache.log4j.Logger;

/**
 * This is a general purpose exception thrown whenever some value is incorrect, somewhere.
 * This exception probably does a bit too much right now, getting thrown when I want to convert an unchecked exception
 * to a checked one, when I get invalid data from the user, when I need to flag a strange case in running... etc.
 * 
 * TODO: Split this into several exceptions, so that we can have some more flexibility
 * @author Johnathan Pagnutti
 *
 *
 */
public class UnsupportedValueException extends Exception {

	private static final long serialVersionUID = 1L;
	
	//logger
	static final Logger logger = Logger.getLogger(UnsupportedValueException.class);
	
	/**
	 * Create a new UnsuportedValueException with no provided message
	 */
	public UnsupportedValueException() {
		super();
	}
	
	/**
	 * Create a new UnsupportedValueException with a provided message
	 * @param message the message to display along with the exception thrown
	 */
	public UnsupportedValueException(String message){
		super(message);
	}
}
