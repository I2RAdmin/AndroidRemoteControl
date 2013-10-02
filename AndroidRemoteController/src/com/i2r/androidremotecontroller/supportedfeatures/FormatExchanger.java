package com.i2r.androidremotecontroller.supportedfeatures;

import java.util.EnumSet;
import java.util.HashMap;

import ARC.Constants;
import ARC.Constants.Args;
import android.util.Log;

/******************************************************
 * Static mapping of int arrays to string arrays with their name referral being
 * the key.<br>
 * (Totally stole this idea from Johnathan Pagnutti)<br>
 * <br>
 * 
 * Current available keys to get exhangers with:<br>
 * {@link CameraFeatureSet#PICTURE_FORMAT} {@link MicrophoneFeatureSet#ENCODING}
 * {@link MicrophoneFeatureSet#SOURCE} {@link MicrophoneFeatureSet#CHANNEL}
 * {@link EnvironmentFeatureSet#UPDATE_SPEED}
 * 
 * @author Josh Noel
 * @see {@link FormatExchanger#getExchanger(String)}
 ****************************************************** 
 */
public enum FormatExchanger {

	// ENUM SET

	// CAMERA SENSOR
	CM_IMAGE(CameraFeatureSet.PICTURE_FORMAT, CameraFeatureSet.INTEGER_IMAGE_FORMATS,
			CameraFeatureSet.STRING_IMAGE_FORMATS),

	// AUDIO SENSOR
	AU_ENCODING(MicrophoneFeatureSet.ENCODING, MicrophoneFeatureSet.INTEGER_ENCODINGS,
			MicrophoneFeatureSet.STRING_ENCODINGS),

	AU_SOURCE(MicrophoneFeatureSet.SOURCE, MicrophoneFeatureSet.INTEGER_SOURCES,
			MicrophoneFeatureSet.STRING_SOURCES),

	AU_CHANNEL(MicrophoneFeatureSet.CHANNEL, MicrophoneFeatureSet.INTEGER_CHANNELS,
			MicrophoneFeatureSet.STRING_CHANNELS),

	// ENVIRONMENT SENSORS
	ENV_UPDATE_SPEED(EnvironmentFeatureSet.UPDATE_SPEED,
			EnvironmentFeatureSet.INTEGER_UPDATE_RATES,
			EnvironmentFeatureSet.STRING_UPDATE_RATES);

	// the map to store all these enums in
	private static final HashMap<String, FormatExchanger> arrayMappings = new HashMap<String, FormatExchanger>();

	// create map at compile time so it can be statically accessed
	static {
		for (FormatExchanger fe : EnumSet.allOf(FormatExchanger.class)) {
			arrayMappings.put(fe.getName(), fe);
		}
	}

	private static final String TAG = "FormatExchanger";

	private String key;
	private int[] intValues;
	private String[] stringValues;

	/**
	 * Constructor<br>
	 * creates a new version of this enum that is gets mapped by its key
	 * parameter into this enum definition's HashMap of enums.
	 * 
	 * The map is used to obtain this enum, and the {@link #get(int)} and
	 * {@link #get(String)} methods are used to exchange values.
	 * 
	 * @param key
	 *            - the key to map this enum by
	 * @param intValues
	 *            - the values defining the given key in integer format
	 * @param stringValues
	 *            - the values defining the given key in string format
	 * @throws IllegalArgumentException
	 *             if any pair of arrays do not map correctly
	 */
	private FormatExchanger(String key, int[] intValues, String[] stringValues)
			throws IllegalArgumentException {

		if (intValues.length != stringValues.length) {
			StringBuilder b = new StringBuilder();
			b.append("ERROR - lengths do not match: ");
			b.append("key: ");
			b.append(key);
			b.append(" int array length: ");
			b.append(intValues.length);
			b.append(" string array length: ");
			b.append(stringValues.length);

			Log.e(TAG, b.toString());
			throw new IllegalArgumentException(
					"int array length and string array length must be equal to map values correctly");
		}

		this.key = key;
		this.intValues = intValues;
		this.stringValues = stringValues;

	}

	/**
	 * Query for the int representation of the given string key
	 * 
	 * @param key
	 *            - the string key to echange with its int counterpart.
	 * @return the int representation of the string key given if it exists in
	 *         this enum, or {@link Args#ARG_NONE} if the key was not found.
	 * @see {@link Constants#Args}
	 */
	public int get(String key) {
		int index = getIndex(key);
		int result = index == Constants.Args.ARG_NONE ? Constants.Args.ARG_NONE
				: intValues[index];
		return result;
	}

	/**
	 * Query for the string representation of the given int key
	 * 
	 * @param key
	 *            - the int key to echange with its string counterpart.
	 * @return the string representation of the given int key if it exists in
	 *         this enum, or null if the key was not found.
	 */
	public String get(int key) {
		int index = getIndex(key);
		String result = index == Constants.Args.ARG_NONE ? null
				: stringValues[index];
		return result;
	}

	/**
	 * Query for this enum's key identifier
	 * 
	 * @return this enum's string name that was given to it upon creation.
	 */
	public String getName() {
		return key;
	}



	// finding indices from the int array
	private int getIndex(int key) {
		int i;
		for (i = 0; i < intValues.length && intValues[i] != key; i++)
			;
		if (i >= intValues.length) {
			i = Constants.Args.ARG_NONE;
		}
		return i;
	}

	// finding indices from the string array
	private int getIndex(String key) {
		int i = Constants.Args.ARG_NONE;
		if (key != null) {
			for (i = 0; i < stringValues.length && !stringValues[i].equals(key); i++)
				;
			if (i >= stringValues.length) {
				i = Constants.Args.ARG_NONE;
			}
		}
		return i;
	}
	
	
	
	/**
	 * Query for an enum echanger that deals with an android device's supported
	 * features
	 * 
	 * @param key
	 *            - the name of the enum exchanger
	 * @return the enum found at the specified key, or null if the key was not
	 *         found in the enum mapping.
	 */
	public static FormatExchanger getExchanger(String key) {
		return arrayMappings.get(key);
	}
	
	
	/**
	 * Exchanges a string value for its integer representation from a specified
	 * map.<br>
	 * This is a convenience method that is short-hand for getting a
	 * {@link FormatExchanger} via {@link FormatExchanger#getExchanger(String)}
	 * and then calling {@link FormatExchanger#get(String)} on the
	 * FormatExchanger obtained.
	 * 
	 * @param mapKey - the mapkey to use in {@link FormatExchanger#getExchanger(String)}
	 * @param value - the value to get an int version of by calling 
	 * 		  {@link FormatExchanger#get(String)}
	 * @return the int representation of the string value given from the map
	 *         given, assuming the map contains the string value. If either
	 *         argument is null or the given map does not contain the given
	 *         value, this returns {@link ARG_NONE}.
	 * @see {@link FormatExchanger}
	 * @see {@link Constants#Args}
	 * @see Sorcery
	 */
	public static int exchange(String mapKey, String value){
		int result = Constants.Args.ARG_NONE;
		if(mapKey != null && value != null){
			FormatExchanger e = FormatExchanger.getExchanger(mapKey);
			if(e != null){
				result = e.get(value);
			} 
		} 
		return result;
	}

} // end of FormatExchanges enum
