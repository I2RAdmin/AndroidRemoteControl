/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The data type enumeration.
 * This enum defines a set of constants that have been agreed upon to represent the type of data that a sensor's feature accepts.  Essentally, its used to make sure
 * that the end user is passing an argument that makes sense for a particular {@link Sensor}'s feature.
 * <p>
 * Each element has two components: the <code>int</code> internal code used to represent a data type for communication between the device and
 * the PC client, and the <code>String</code> name that an end user can read and type in to access that sensor.
 * @author Johnathan Pagnutti
 */
public enum DataType {
	/**
	 * The constant the represents an integer data type.  Fixed point, counting number data.
	 */
	INTEGER(3, "integer"), 
	/**
	 * The constant that represents a floating point data type. 
	 */
	DOUBLE(4, "double"),  
	/**
	 * The constant that represents file data.  UNUSED.
	 */
	FILE(10, "file"),
	/**
	 * The constant that represents some set of ASCII character data.
	 */
	STRING(5, "string"),
	/**
	 * The constant that represents some data from some stream.  UNUSED.
	 */
	STREAM(11, "stream"),
	/**
	 * The constant that represents any data.  Essentally, represents untyped data.
	 */
	ANY(8, "any");
	
	/**
	 * The internal map used to map the <code>integer</code> action to a particular data type constant
	 */
	private static final Map<Integer, DataType> dataType = new HashMap<Integer, DataType>();
	
	/**
	 * The internal map used to map the <code>String</code> readable name of a data type to a particular data type constant
	 */
	private static final Map<String, DataType> dataAlias = new HashMap<String, DataType>();
	
	//TODO: this is a weird block of code, and I need to touch up the documentation here.  Maybe even write a bit about it
	//in the header
	//Static initialization block for the Enumeration 
	static{
		//For each data type listed in the definition of the data type class
		for(DataType t : EnumSet.allOf(DataType.class)){
			//add it to the type map
			dataType.put(t.getType(), t);
			//add it to the readable string map
			dataAlias.put(t.getAlias(), t);
		}
	}
	
	/**
	 * Private internal {@link Integer} used for assigning data type constants codes to communicate with the remote device
	 */
	private Integer type;
	
	/**
	 * Private internal {@link String} used for assigning data type constants human readable Strings for the UI
	 */
	private String alias;
	
	/**
	 * Constructor.  Private to the class.
	 * @param type the Integer code for a particular data type
	 * @param alias the String readable name for a particular data type
	 */
	private DataType(Integer type, String alias){
		this.type = type;
		this.alias = alias;
	}
	
	/**
	 * Get the code that a particular data type uses to communicate with a remote device
	 * @return the code of a data type
	 */
	public Integer getType(){
		return type;
	}
	
	/**
	 * Get a human-readable name of a data type
	 * @return the human readable name of a data type
	 */
	public String getAlias(){
		return alias;
	}
	
	/**
	 * Get the data type constant from a {@link Integer} code
	 * @param type the code to use to get a data type Constant
	 * @return the data type constant
	 * @throws UnsupportedValueException if the code provided is not valid
	 */
	public static DataType get(Integer type) throws UnsupportedValueException{
		//if the provided data type code is not a key in the data type code map
		if(!dataType.containsKey(type)){
			//throw an exception
			throw new UnsupportedValueException(type + " is not a valid data type.");
		}
		//return the data type constant mapped to the passed in Integer
		return dataType.get(type);
	}
	
	/**
	 * Get the data type constant from a <code>String</code> human readable name
	 * 
	 * @param alias the human readable name to use to get a data type constant
	 * @return the data type constant associated with this human readable name
	 * @throws UnsupportedValueException if the human readable name provided was not valid
	 */
	public static DataType get(String alias) throws UnsupportedValueException{
		//if the provided data type name is not a key in the sensor name map
		if(!dataAlias.containsKey(alias)){
			//throw an exception
			throw new UnsupportedValueException(alias + " is not a valid data type.");
		}
		//return the data type constant mapped to the passed in data type name
		return dataAlias.get(alias);
	}
	
}