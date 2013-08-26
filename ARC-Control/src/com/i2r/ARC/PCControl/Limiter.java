/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Limiter enumeration.
 * <p>
 * This enum defins a set of constants that have been agreed upon to represent the various limits on values that can be sent to 
 * modify parameters of a {@link Sensor}.  These are sets of values, ranges of values, constants, or anything (no limit).
 * <p>
 * Each element has two components: the <code>int</code> internal code for a limit used for communication between the device and the PC client, 
 * and the <code>String</code> name that an end user can read to understand the limit set on a {@link Sensor} parameter.
 * @author Johnathan Pagnutti
 *
 */
public enum Limiter {
	/**
	 * Any.  This is the limit used when we don't want a limit
	 */
	ANY(8, "any"),
	/**
	 * Set.  Values for a parameter must be one of the elements sent over.  Sets of 1 are possible.
	 */
	SET(7, "set"),
	/**
	 * Size.  Values must be smaller than a size sent over.  Currently no used.
	 */
	SIZE(12, "size"),
	/**
	 * Range.  Values must be in between a range sent over, inclusive.
	 */
	RANGE(6, "range"),
	/**
	 * Constant.  This sensor parameter can not be changed.
	 */
	CONST(9, "constant");
	
	/**
	 * The internal map used to map the <code>Integer</code> limit to a particular limit constant
	 */
	private static final Map<Integer, Limiter> limiterType = new HashMap<Integer, Limiter>();
	/**
	 * The internal map used to map the <code>String</code> readable name of a limit to a particular limit constant
	 */
	private static final Map<String, Limiter> limiterAlias = new HashMap<String, Limiter>();
	
	//TODO: this is a weird block of code, and I need to touch up the documentation here.  Maybe even write a bit about it
	//in the header
	//Static initialization block for the Enumeration 
	static{
		//for each sensor listed in the definition of the Limiter class
		for(Limiter l : EnumSet.allOf(Limiter.class)){
			//add it to the action map
			limiterType.put(l.getType(), l);
			//add it to the readable string map
			limiterAlias.put(l.getAlias(), l);
		}
	}
	
	/**
	 * Private internal {@link Integer} used for assigning Limiter constants codes to communicate with the remote device
	 */
	private Integer type;
	
	/**
	 * Private internal {@link String} used for assigning Limiter constants human readable String for the UI
	 */
	private String alias;
	
	/**
	 * Constructor.  Private to the class.
	 * 
	 * @param type the Integer code for a particular limiter
	 * @param alias the String readable name for a particular limiter
	 */
	private Limiter(Integer type, String alias){
		this.type = type;
		this.alias = alias;
	}
	
	/**
	 * Get the code that a particular limiter uses to communicate with the remote device
	 * @return the code of a limiter
	 */
	public Integer getType(){
		return type;
	}
	
	/**
	 * Get a human-readable name of a limiter
	 * @return the human-readable name of a limiter
	 */
	public String getAlias(){
		return alias;
	}
	
	/**
	 * Get the Limiter constant from a {@link Integer} code
	 * @param type the code to get a Limiter constant from
	 * @return the limiter constant
	 * @throws UnsupportedValueException if the code provided is not valid
	 */
	public static Limiter get(Integer type) throws UnsupportedValueException{
		//if the provided limiter code was not a key in the limiter code map
		if(!limiterType.containsKey(type)){
			//throw an exception
			throw new UnsupportedValueException(type + " is not a valid limiter.");
		}
		
		//return the limiter constant mapped to the passed in Integer
		return limiterType.get(type);
	}
	
	/**
	 * Get the limiter constant from a <code>String</code> human readable name
	 * 
	 * @param alias the human readale name to use to get a Limiter constant
	 * @return the limiter constant associated with this human readable name
	 * @throws UnsupportedValueException if the human readable name provided was not valid
	 */
	public static Limiter get(String alias) throws UnsupportedValueException{
		//if the provded limiter name is not a key in the limiter name map
		if(!limiterAlias.containsKey(alias)){
			//throw an exception
			throw new UnsupportedValueException(alias + " is not a valid limiter");
		}
		
		//return the limiter constant mapped to the passed in limiter name
		return limiterAlias.get(alias);
	}
}
