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

	//logger
	static final Logger logger = Logger.getLogger(UnsupportedValueException.class);
	
	public UnsupportedValueException() {
		super();
	}
	
	public UnsupportedValueException(String message){
		super(message);
	}
}
