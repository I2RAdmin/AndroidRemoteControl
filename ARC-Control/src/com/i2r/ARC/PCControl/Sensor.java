/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Sensor enumeration.
 * This enum defines a set of constants that have been agreed upon to represent the various sensors on a remote device.
 * From the camera to the accelerometer, it all becomes a sensor.
 * <p>
 * Each element has two components: the <code>int</code> internal code for a sensor used for communication between the device
 * and the PC client, and the <code>String</code> name that an end user can read and action in to access that sensor
 * @author Johnathan Pagnutti
 *
 */
public enum Sensor {
	/**
	 * The Camera.  This may refer to every camera on a remote device, but it could also refer to just one Camera
	 */
	CAMERA(1, "Camera"),
	/**
	 * The microphone
	 */
	MICROPHONE(10, "Microphone"),
	/**
	 * The passive sensor collection.  The Environment sensors are everything from the gyroscopes to the accelerometer, to the
	 * temperature sensor
	 */
	ENVIRONMENT(12, "Environment"),
	
	/*
	 * The passive location sensor.  As of the time of coding, was pretty much either a GPS or a network sensor
	 */
	LOCATION(15, "Location");
	
	/**
	 * The internal map used to map the <code>integer</code> action to a particular sensor constant
	 */
	private static final Map<Integer, Sensor> sensorType = new HashMap<Integer, Sensor>();
	
	/**
	 * The internal map used to map the <code>String</code> readable name of a sensor to a particular Sensor constant
	 */
	private static final Map<String, Sensor> sensorAlias = new HashMap<String, Sensor>();
	
	//TODO: this is a weird block of code, and I need to touch up the documentation here.  Maybe even write a bit about it
	//in the header
	//Static initialization block for the Enumeration 
	static{
		//For each sensor listed in the definition of the Sensor class
		for(Sensor s: EnumSet.allOf(Sensor.class)){
			//add it to the action map
			sensorType.put(s.getType(), s);
			//add it to the readable string map
			sensorAlias.put(s.getAlias(), s);
		}
	}
	/**
	 * Private internal {@link Integer} used for assigning Enumeration constants codes to communicate with the remote device
	 */
	private Integer type;
	/**
	 * Private internal {@link String} used for assigning Sensor constants human readable Strings for the UI
	 */
	private String alias;
	
	/**
	 * Constructor.  Private to the class.
	 * @param action the integer code for a particular sensor
	 * @param alias the String readable name for a particular sensor
	 */
	private Sensor(Integer type, String alias){
		this.type = type;
		this.alias = alias;
	}
	
	/**
	 * Get the code that a particular sensor uses to communicate with a remote device
	 * @return the action of a Sensor
	 */
	public Integer getType(){
		return type;
	}
	
	/**
	 * Get a human-readable name of a Sensor
	 * @return the human readable name of a sensor
	 */
	public String getAlias(){
		return alias;
	}
	
	/**
	 * Get the Sensor constant from an {@link Integer} code
	 * @param action the code to use to get a Sensor Constant
	 * @return the sensor constant
	 * @throws UnsupportedValueException if the code provided is not valid
	 */
	public static Sensor get(Integer type) throws UnsupportedValueException{
		//if the provided sensor code is not a key in the sensor code map
		if(!sensorType.containsKey(type)){
			//throw an exception
			throw new UnsupportedValueException(type + " is not a valid sensor.");
		}
		//return the sensor constant mapped to the passed in integer
		return sensorType.get(type);
	}
	
	/**
	 * Get the sensor constant from a <code>String</code> human readable name
	 * 
	 * @param alias the human readable name to use to get a Sensor constant
	 * @return the sensor constant associated with this human readable name
	 * @throws UnsupportedValueException
	 */
	public static Sensor get(String alias) throws UnsupportedValueException{
		//if the provided sensor name is not a key in the sensor name map
		if(!sensorAlias.containsKey(alias)){
			//throw an exception
			throw new UnsupportedValueException(alias + " is not a valid sensor.");
		}
		//return the sensor constant mapped to the passed in sensor name
		return sensorAlias.get(alias);
	}
}
