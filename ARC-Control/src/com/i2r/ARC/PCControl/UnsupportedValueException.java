/**
 * 
 */
package com.i2r.ARC.PCControl;

/**
 * This error is thrown when an value can not be set to a feature because it is not correct according to that feature's 
 * sensor's {@link Capabilities} class.
 * 
 * Or, maybe more susiccntly: whenever an argument fails a check against a {@link Capabilities}
 * 
 * @author Johnathan Pagnutti
 *
 */
public class UnsupportedValueException extends Exception {

	public UnsupportedValueException() {
		// TODO Auto-generated constructor stub
	}

}
