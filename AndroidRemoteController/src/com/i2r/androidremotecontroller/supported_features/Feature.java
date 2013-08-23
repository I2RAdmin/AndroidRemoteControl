package com.i2r.androidremotecontroller.supported_features;

import java.util.Collection;
import java.util.Iterator;

import ARC.Constants;

import com.i2r.androidremotecontroller.main.ResponsePacket;


/******************************************************************************
 * <p>This class defines a way to obtain all the information that the
 * controller needs to know about this android device, in order to properly
 * manipulate its sensors. General rule for sending an individual feature for a
 * set of features:</p>
 * <ol>
 * <li>Name of feature(how controller should refer to it in communicating
 * back)</li>
 * <li>current value for this feature</li>
 * <li>type of data that this application expects from the controller when
 * referencing this feature</li>
 * <li>the limiting type of this feature - range or set;
 * switches and properties can be considered sets of 2 and 1 respectively</li>
 * <li>the size of the available values this feature can be set to</li>
 * <li>the actual feature values, delimited by the {@link PACKET_DELIMITER}
 * constant defined in {@link Constants#Delimiters}.</li>
 * </ol><br>
 * 
 * <p>All features of a particular sensor will be "feature-delimited" with the
 * constant {@link PACKET_LIST_DELIMITER}, also defined in {@link Constants#Delimiters}.
 * The static methods which define sets of features for each particular sensor
 * in this class can be considered the "data" of a {@link ResponsePacket},
 * with the header being the task ID that came with the feature request,
 * and the data type being a feature defined in {@link Constants#DataTypes} 
 * (such as {@link CAMERA_SENSOR}, {@link MIC_SENSOR} etc...)</p>
 * 
 * @author Josh Noel
 ******************************************************************************
 */
public class Feature {

	
	private String name, value;
	private String[] possibleValues;
	private int dataType, limitingType;
	
	
	/**
	 * Single variant Constructor.<br>
	 * Creates a feature with a possible value set of
	 * one (a set of one with the value given).
	 * @param name - the name of this feature
	 * @param value - the value that this feature is currently set to
	 * @param dataType - the data type this application expects from
	 * the controller when referencing this feature
	 * @param limitingType - the containment type of this feature
	 * (set, range, property etc.)
	 */
	public Feature(String name, String value, int dataType, int limitingType){
		this.name = name;
		this.value = value;
		this.possibleValues = null;
		this.limitingType = limitingType;
		this.dataType = dataType;
	}
	
	
	/**
	 * Array set Constructor.<br>
	 * Creates a feature with a set of possible values equal
	 * to all the values found in the possibleValues parameter.
	 * @param name - the name of this feature
	 * @param value - the value that this feature is currently set to
	 * @param possibleValues - all the possible values that this feature's
	 * "value" parameter can be set to.
	 * @param dataType - the data type this application expects from
	 * the controller when referencing this feature
	 * @param limitingType - the containment type of this feature
	 */
	public Feature(String name, String value,
			String[] possibleValues, int dataType, int limitingType){
		this.name = name;
		this.value = value;
		this.possibleValues = possibleValues;
		this.dataType = dataType;
		this.limitingType = limitingType;
	}
	
	
	/**
	 * Collection Constructor.<br>
	 * Creates a feature with a set of possible values equal
	 * to all the values found in the possibleValues parameter.
	 * @param name - the name of this feature
	 * @param value - the value that this feature is currently set to
	 * @param possibleValues - all the possible values that this feature's
	 * "value" parameter can be set to. The objects in this collection
	 * are presented to the controller in their toString() form.
	 * @param dataType - the data type this application expects from
	 * the controller when referencing this feature
	 * @param limitingType - the containment type of this feature
	 */
	public Feature(String name, String value,
			Collection<?> possibleValues, int dataType, int limitingType){
		this.name = name;
		this.value = value;
		this.dataType = dataType;
		this.limitingType = limitingType;
		
		this.possibleValues = new String[possibleValues.size()];
		Iterator<?> iter = possibleValues.iterator();
		for(int i = 0; i < possibleValues.size(); i++){
			this.possibleValues[i] = String.valueOf(iter.next());
		}
	}

	
	/**
	 * Query for this feature's name
	 * @return the name given to this feature at creation.
	 */
	public String getName(){
		return name;
	}
	
	
	/**
	 * Query for this feature's current value
	 * @return the value given to this feature at creation
	 */
	public String getValue(){
		return value;
	}
	
	
	/**
	 * Query for this feature's expected data type
	 * @return the data type given to this feature at creation
	 * @see {@link Constants#DataTypes}
	 */
	public int getDataType(){
		return dataType;
	}
	
	
	/**
	 * Query for the limiting type of this feature
	 * @return the limiting type given to this feature at creation
	 * @see {@link Constants#DataTypes}
	 */
	public int getLimitingType(){
		return limitingType;
	}
	
	
	/**
	 * Query for this features set of possible values.
	 * @return a copy of this feature's possible values,
	 * or null if the {@link #Feature(String, String, int, int)}
	 * constructor was used to create this feature.
	 */
	public String[] getPossibleValues(){
		String[] temp = null;
		
		if(possibleValues != null){
			temp = new String[possibleValues.length];
			for(int i = 0; i < possibleValues.length; i++){
				temp[i] = possibleValues[i];
			}
		}
		return temp;
	}
	
	
	/**
	 * Query for the state of this feature's validity
	 * to be sent to the controller PC.
	 * @return true if the name, value, data type and
	 * limiting type have been set; false otherwise.
	 * It is implied here that a feature does not have
	 * to have a set of possible values, (i.e., when a
	 * feature is a property of this android device) but
	 * if a feature should have a set of values, they must
	 * be given at creation.
	 */
	public boolean isValid(){
		return name != null && value != null &&
				dataType != Constants.Args.ARG_NONE &&
				limitingType != Constants.Args.ARG_NONE;
	}
	
	

	/**
	 * Convenience method. This is equivalent to using
	 * {@link #encode(Feature, String, String)} with 
	 * {@link Delimiters#PACKET_DELIMITER} as the first string, and
	 * {@link Delimiters#PACKET_LIST_DELIMITER} as the second string.
	 * @return the encoded string representation of this feature to
	 * be sent to the controller PC.
	 * @see {@link Constants#Delimiters}
	 */
	public String encode(){
		return encode(this, String.valueOf(Constants.Delimiters.PACKET_DELIMITER), 
				String.valueOf(Constants.Delimiters.PACKET_LIST_DELIMITER));
	}
	
	
	/**
	 * Encodes a feature of a sensor to be sent to the controller PC.
	 * See {@link Feature} for details on how this method works.
	 * @param feature - the feature to encode and send to a controller PC.
	 * @param element_delimiter - the delimiter for each of the individual
	 * elements in the given Feature object.
	 * @param end_delimiter - a delimiter to seal this encoding with.
	 * @return an encoded String representation of the given Feature
	 */
	public static String encode(Feature feature,
			String element_delimiter, String end_delimiter){
		
		String result = null;
		
		if(feature.isValid()){
			
			StringBuilder builder = new StringBuilder();
			
			builder.append(feature.name);
			builder.append(element_delimiter);
			builder.append(feature.value);
			builder.append(element_delimiter);
			builder.append(feature.dataType);
			builder.append(element_delimiter);
			builder.append(feature.limitingType);
			builder.append(element_delimiter);
			
			if(feature.possibleValues != null){
				
				builder.append(feature.possibleValues.length);
				builder.append(element_delimiter);
				
				for(int i = 0; i < feature.possibleValues.length; i++){
					builder.append(feature.possibleValues[i]);
					builder.append(element_delimiter);
				}
				
			} else {
				
				builder.append(1);
				builder.append(element_delimiter);
				builder.append(feature.value);
				builder.append(element_delimiter);
				
			}
			
			builder.append(end_delimiter);
			
			result = builder.toString();
		}
		
		return result;
	}
	
}
