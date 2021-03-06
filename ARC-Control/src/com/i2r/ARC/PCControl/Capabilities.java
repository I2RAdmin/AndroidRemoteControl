/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * The {@link Capabilities} class is a how we define what action of parameters we can change for a particular sensor
 * on the remote device.
 * 
 * {@link Capabilities} have three main components:
 * 
 * {@link Capabilities#featureDataTypes} This is a {@link Map} of the {@link String} name of the parameter to the {@link DataType}
 * 	of the values we can give to that parameter.  For example, if we had a sensor that took a {@link String} file action (jpeg), its
 * 	{@link DataType} would be {@link DataType#STRING}, and so the {@link Capabilities#featureDataTypes} would have an entry with a key of 
 * 	the {@link String} "filetype" and a value {@link DataType#STRING}
 * 
 * {@link Capabilities#featureLimiters} This is a {@link Map} of the {@link String} name of a parameter to the {@link Limiter} of
 * 	the values that we can give that parameter.  For example, if we had a sensor that took a {@link String} file action, and we knew it could
 * 	be one element of a {@link Set} of valid file types, then its {@link Limiter} is a {@link Limiter#SET}, so the 
 * {@link Capabilities#featureLimiters} would have an entry with the key as the {@link String} parameter name and the value of 
 * {@link Limiter#SET}
 * 
 *  {@link Capabilities#featureLimitArguments} This is a {@link Map} of the {@link String} name of a parameter to the {@link List<String>}
 *  of acceptable values for that parameter.  For example, if we had a sensor with the {@link DataType} of {@link DataType#STRING} with
 *  a {@link Limiter} of {@link Limiter#SET}, the values in this map would be the {@link String}s we could give this parameter.
 *  
 *  The class also contains several utility and error checking methods that prevent bad elements from being stored.  As the data communication
 *  between the Remote Device and the client are based on ASCII streams, all values are checked with ASCII codes, so a {@link DataType#INTEGER}
 *  is valid if the {@link char} that make up the {@link String} are all ASCII integers.
 *  
 * @author Johnathan Pagnutti
 *
 */
public class Capabilities {

	static final Logger logger = Logger.getLogger(Capabilities.class);
	
	/**
	 * Constant defines the integer value of ASCII 0
	 */
	public static final int ASCII_0 = 48;
	
	/**
	 * Constant defines the integer value of ASCII 9
	 */
	public static final int ASCII_9 = 57;
	
	/**
	 * Constant defines the integer value of the ASCII .
	 */
	public static final int ASCII_DOT = 46;
	
	/**
	 * Constant defines the integer value of the ASCII -
	 */
	public static final int ASCII_DASH = 45;
	/**
	 * Constant defines the interger value of ASCII null
	 */
	public static final int ASCII_NULL = 00;
	
	/**
	 * This is the {@link Map} between the name of a parameter of a sensor and the {@link DataType} of the arguments to change that
	 * parameter
	 */
	Map<String, DataType> featureDataTypes;
	
	/**
	 * This is the {@link Map} between the name of a parameter of a sensor and the {@link Limiter} on the arguments we can use to change
	 * that parameter
	 */
	Map<String, Limiter> featureLimiters;
	
	/**
	 * This is the {@link Map} between the name of a parameter of a sensor and the {@link List<String>} that defines the acceptable
	 * arguments for that parameter
	 */
	Map<String, List<String>> featureLimitArguments;
	
	/**
	 * Constructor.  Initalzies the three {@link Map}s that drive the {@link Capabilities} class
	 */
	public Capabilities() {
		featureDataTypes = new HashMap<String, DataType>();
		featureLimiters = new HashMap<String, Limiter>();
		featureLimitArguments = new HashMap<String, List<String>>();
	}

	/**
	 * Adds a new feature this {@link Capabilities}.  A feature is a parameter that we can change.  
	 * A base example is the flash on the android camera
	 * 
	 * @param featureName the name of the feature.  This is the key used in the {@link Capabilities} maps.
	 * @param action this is a {@link DataType} of the values we can pass to the feature
	 * @param limit this is the {@link Limiter} on the values we can pass to the feature
	 * @param args this is a {@link List<String>} of the acceptable values we can pass to a feature
	 */
	public void addFeature(String featureName, DataType type, Limiter limit, List<String> args){
		//LOGGING LOOOOOOOP
		logger.debug("Adding feature: ");
		logger.debug("	" + featureName);
		logger.debug("	" + type.getAlias());
		logger.debug("	" + limit.getType());
		for(String arg : args){
			logger.debug("	" + arg);
		}
		
		//add the feature name : DataType entry in the data types map
		featureDataTypes.put(featureName, type);
		
		//add the feature name : Limiter entry in the Limiters map
		featureLimiters.put(featureName, limit);
		
		//add the feature name : acceptable values entry in the acceptable values map
		featureLimitArguments.put(featureName, args);
	}
	
	/**
	 * Checks a supplied argument to see if can be safely passed to a feature.  The argument must be of the correct {@link DataType},
	 * and fall within the correct {@link List<String>} of acceptable arguments, set by the {@link Limiter} for this feature.
	 * <p>
	 * The strange return type comes in due to case insensitivity on the UI side.  According to the program, everything the user types is lower
	 * case, however, we do not want to put this restriction on the information from the remote side of the connection.  As such, if the phone
	 * requires some key or value to have a capital, we ensure that, in the transmission back to the phone, that requirement is ensured.
	 * <p>
	 * @param key the name of the feature to set a new value for
	 * @param value the value we want to tell the remote device to set for a feature
	 * 
	 * @return the key/value pair as it is saved in the Capabilities maps for use to transmit back
	 * 
	 * @throws UnsupportedValueException if the key is not a valid feature, or the value is incorrect for the supplied key
	 */
	public String[] checkArg(String key, String value) throws UnsupportedValueException{
		String[] returnTuple = new String[2];
		
		logger.debug("Feature keys: ");
		logger.debug(featureDataTypes.size());
		for(String str : featureDataTypes.keySet()){
			logger.debug("	" + str);
		}
		
		//check to see if the key provided is a valid feature name
		boolean foundFeature = false;
		
		//for each feature name
		for(String featureName : featureDataTypes.keySet()){
			//check to see if it matches the lowercase version of the feature name (for case insensitivity)
			if(key.equals(featureName.toLowerCase())){
				//if we get a match, set the flag to true
				foundFeature = true;
				returnTuple[0] = featureName;
				break;
			}
		}
		
		//if no match was ever found
		if(!foundFeature){
			throw new UnsupportedValueException(key + " not supported.");
		}
		
		/*
		if(!featureDataTypes.containsKey(key)){
			//it isn't.  Log and throw an error
			throw new UnsupportedValueException(key + " not supported.");
		}
		*/
		
		//the key does point to a valid feature!
		
		//Get the data action for this feature
		DataType type = featureDataTypes.get(returnTuple[0]);
		
		//check to see if the value could be interpreted as the data action for this feature
		if(!checkType(type, value)){
			//it can't, log and throw an error
			throw new UnsupportedValueException("Data for " + returnTuple[0] + " was of the incorrect action (needed to be: " + type.getAlias() + ")");
		}
		
		//it can!
		
		//get the limiter for this feature, and the acceptable arguments for this feature
		Limiter limit = featureLimiters.get(returnTuple[0]);
		List<String> limitVals = featureLimitArguments.get(returnTuple[0]);
		
		//check to see if the value falls under the acceptable arguments for this feature
		String temp = checkLimit(type, limit, limitVals, value);
	
		//if temp is null...
		if(temp == null){
			//it does not.  Log and throw the error.
			throw new UnsupportedValueException("Data for " + returnTuple[0] + " did not fall within the limit.");
		}
		
		//it does!  Return the value, as we now know it is safe and can be supplied as an argument for this feature
		returnTuple[1] = temp;
		return returnTuple;
	}

	/**
	 * This method checks a value based on a feature's {@link Limiter} and the {@link List<String>} of acceptable arguments for that
	 * feature.  If the value falls within the acceptable arguments, then we know the value can be used to set a particular
	 * feature.
	 * 
	 * @param action the {@link DataType} of a feature.  Used here so we can cast the value correctly for comparison
	 * @param limit the {@link Limiter} of a feature.  Used here so we can correctly interpet the limitVals
	 * @param limitVals the {@link List<String>} of acceptable arguments for a feature.  Used in conjunction with the limit so we
	 * 			can check to make sure the value falls within the acceptable bounds of a feature
	 * @param value The value we want to check for use with a feature
	 * 
	 * @return the string value how it is saved in the capabities maps if we can use the value passed in, null if otherwise
	 */
	private String checkLimit(DataType type, Limiter limit, List<String> limitVals, String value) {
		//check the limit
		switch(limit){
		//if the limit is ANY
		case ANY:
			//always return true.
			return value;
		//if the limit is a range
		case RANGE:
			//check the data action
			switch(type){
			//if the data action is an integer
			case INTEGER:
				//get the minimum value as an int
				int intMin = Integer.parseInt(limitVals.get(0));
				//get the maximum value as an int
				int intMax = Integer.parseInt(limitVals.get(1));
				//get the value as an integer
				int intVal = Integer.parseInt(value);
				
				//check to make sure the value falls between the minimum and the maximum (inclusive)
				if(intVal >= intMin && intVal <= intMax){
					//it do!  Return true
					return value;
				}else{
					//it don't! return false
					return null;
				}
			//if the data action is a double
			case DOUBLE:
				//get the minimum value as a double
				double doubleMin = Double.parseDouble(limitVals.get(0));
				//get the maximum value as a double
				double doubleMax = Double.parseDouble(limitVals.get(1));
				//get the value to check as a double
				double doubleVal = Double.parseDouble(value);
				
				//check to make sure the value falls between the minimum and the maximum (inclusive)
				if(doubleVal >= doubleMin && doubleVal <= doubleMax){
					//it do!  Return true
					return value;
				}else{
					//it don't! return false
					return value;
				}
			//if the data action is anything else
			default:
				//ranges don't make sense for that data action, just return false
				return null;
			}
		//if the limiter is a set
		case SET:
			//for each string in limit vals
			for(String setVal : limitVals){
				//if the value is in the list of limit values
				if(value.equals(setVal.toLowerCase())){
					//we can use it! return true
					return setVal;
				}
			}
			//the value was never found in the set of limit values, return false
			return null;
		//if the limiter is something else
		case CONST:
		default:
			//constants can't be changed, and if we see anything else, it isn't supported.
			return null;
		}
	}

	/**
	 * Checks to see if the value can be enterpeted as the supplied {@link DataType}
	 * 
	 * @param action the data action of this feature
	 * @param value the value to check
	 * @return true if the value can be interpeted as the {@link DataType} action, false if otherwise
	 */
	private boolean checkType(DataType type, String value) {
		//check the data action
		switch(type){
		//if the data action is an integer
		case INTEGER:
			//check to see if the value can be interpeted as an integer
			return checkInt(value);
		//if the data action is a double
		case DOUBLE:
			//check to see if the value can be interpeted as a double
			return checkDouble(value);
		//if the data action is a string
		case STRING:
			//check to see if the value can be interpeted as a string
			return checkString(value);
		//if the data action is a file or stream
		case FILE:
		case STREAM:
		case ANY:
			//return true.  These data types accept any value
			return true;
		//otherwise
		default:
			//return false, as we've landed in bad juju land
			return false;
		}
	}
	
	/**
	 * Checks to see if a value can be interpeted as a {@link DataType#STRING}
	 * 
	 * @param value the value to check
	 * @return true if the value can be interpeted as a {@link DataType#STRING}, false if otherwise
	 */
	private boolean checkString(String value) {
		String regex = "(?i)[\\x00-\\x7F&&[^\\x00-\\x1F\\x7F]]+";
		
		if(value.matches(regex)){
			return true;
		}else{
			return false;
		}
		
		/*
		//convert the value to a byte array
		byte[] dataToCheck = value.getBytes();
		
		//for each byte in the array
		for(byte b : dataToCheck){
			//get the raw ASCII value
			int rawValue = (char)b;
			//if the ASCII value is null
			if(rawValue == ASCII_NULL){
				//return false.  Strings will never have null characters
				return false;
			}
		}
		
		//otherwise, return true
		return true;*/
	}

	/**
	 * Checks to see if a value can be interpited as an {@link DataType#INTEGER}
	 * {@link DataType#INTEGER} are just ASCII numerals
	 * 
	 * @param value the value to check
	 * @return true if the value can be interpeted as an {@link DataType#INTEGER}, false if otherwise
	 */
	private boolean checkInt(String value) {
		String regex = "(?i)-?[0-9]+";
		
		if(value.matches(regex)){
			return true;
		}else{
			return false;
		}
		
		/*boolean dashFound = false;
		
		//convert the value to a byte array
		byte[] dataToCheck = value.getBytes();
		
		//for each byte in the byte array
		for(byte b : dataToCheck){
			//get the raw ASCII value
			int rawValue = (char)b;
			logger.debug(rawValue);
			
			//if the value falls between ASCII 0 and ASCII 9 (inclusive)
			if(rawValue >= ASCII_0 && rawValue <= ASCII_9){
				//great!  Move on to the next byte
				continue;
			//otherwise
			}else if (rawValue == ASCII_DASH && dashFound == false){
				dashFound = true;
				continue;
			}else{
				//return false, there is a no numeric in this value somwhere, and that means we can't interpet it as a number
				return false;
			}
		}
		
		//if we got through all the bytes without returning false, congrats!  We're a number of some sort
		return true;*/
	}

	/**
	 * Check to see if the value we have can be interpeted as a {@link DataType#DOUBLE}
	 * {@link DataType#DOUBLE} are ASCII numerals with one or fewer ASCII periods in them
	 * 
	 * @param value the value to check
	 * @return true if the value can be interpeted as a {@link DataType#DOUBLE}, false if otherwise
	 */
	private boolean checkDouble(String value) {
		String regex = "(i?)-?[0-9]+\\.*[0-9]*";
		
		if(value.matches(regex)){
			return true;
		}else{
			return false;
		}
		/*
		//boolean var to check to see if we have seen an ASCII dot yet
		boolean dotFound = false;
		boolean dashFound = false;
		
		//convert the value to a byte array
		byte[] dataToCheck = value.getBytes();
		
		//for each byte in the array
		for(byte b : dataToCheck){
			//get the raw ASCII value
			int rawValue = (char)b;
			//if the raw value is greater than ASCII 0 or less than ASCII 9 (inclusive)
			if(rawValue >= ASCII_0 && rawValue <= ASCII_9){
				//great, move on to the next byte
				continue;
			//else, if the raw value is the ASCII period and this is the first period we've seen
			}else if(rawValue == ASCII_DOT && dotFound == false){
				//set that we have found a period
				dotFound = true;
				//this will not evaluate more than once, and bump down to false
				//but for right now, great! move on to the next byte
				continue;
			//otherwise
			}else if(rawValue == ASCII_DASH && dashFound == false){
				dashFound = true;
				
				continue;
			}else{
				//we have some bad no numeric or double dot or something.  return false
				return false;
			}
		}
		
		//if we got through all the bytes and never returned false, great!  we can interpet this as a double
		return true;*/
	}	
}
