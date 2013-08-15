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
 * and the PC client, and the <code>String</code> name that an end user can read and type in to access that sensor
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
	ENVIRONMENT(12, "Environment");
	
	/**
	 * The internal map used to map the <code>integer</code> type to a particular sensor constant
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
			//add it to the type map
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
	 * @param type the integer code for a particular sensor
	 * @param alias the String readable name for a particular sensor
	 */
	private Sensor(Integer type, String alias){
		this.type = type;
		this.alias = alias;
	}
	
	/**
	 * Get the code that a particular sensor uses to communicate with a remote device
	 * @return the type of a Sensor
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
	
	
	public static Sensor get(Integer type) throws UnsupportedValueException{
		if(!sensorType.containsKey(type)){
			throw new UnsupportedValueException(type + " is not a valid sensor.");
		}
		return sensorType.get(type);
	}
	
	public static Sensor get(String alias) throws UnsupportedValueException{
		if(!sensorAlias.containsKey(alias)){
			throw new UnsupportedValueException(alias + " is not a valid sensor.");
		}
		return sensorAlias.get(alias);
	}
}
