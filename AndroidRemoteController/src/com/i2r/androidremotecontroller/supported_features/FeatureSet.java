package com.i2r.androidremotecontroller.supported_features;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ARC.Constants;

/**
 * This class models a container for a sensor's features, and is designed to be
 * extended for customization to a particular sensor.
 * 
 * TODO: make app start on phone start up and dim (or turn off) screen
 * also make phone wake back up on usb connected
 * 
 * @author Josh Noel
 * @see {@link Feature}
 */
public class FeatureSet {

	// **************************************************
	// |-- COMMON CONSTANTS TO ALL SUPPORTED FEATURES --|
	// **************************************************

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String[] SWITCH_VALUES = { TRUE, FALSE };

	public static final String KEY_SAVE_TO_SD = "save-to-sd";
	public static final String KEY_CONTINUE_ON_CONNECTION_LOST = "continue-on-connection-lost";

	private static final String SAVE_TO_SD = new Feature(KEY_SAVE_TO_SD, FALSE,
			SWITCH_VALUES, Constants.DataTypes.STRING, Constants.DataTypes.SET)
			.encode();

	private static final String CONTINUE_ON_CONNECTION_LOST = new Feature(
			KEY_CONTINUE_ON_CONNECTION_LOST, FALSE, SWITCH_VALUES,
			Constants.DataTypes.STRING, Constants.DataTypes.SET).encode();

	private LinkedList<Feature> features;

	/**
	 * Constructor<br>
	 * create a new feature set container for {@link Feature} objects
	 */
	public FeatureSet() {
		features = new LinkedList<Feature>();
	}

	/**
	 * Adds a switch feature to this feature set. This is a boolean for the
	 * controller to flip on or off.
	 * 
	 * @param name
	 *            - the name of this switch
	 * @param value
	 *            - the value this switch is currently set to
	 */
	public void addSwitch(String name, String value) {
		features.add(new Feature(name, value, SWITCH_VALUES,
				Constants.DataTypes.STRING, Constants.DataTypes.SET));
	}

	/**
	 * Adds a range feature to this feature set. This is a range with a max and
	 * a min that can accept all inclusive values.
	 * 
	 * @param name
	 *            - the name of this range
	 * @param value
	 *            - the value this range is currently set to
	 * @param dataType
	 *            - the data type of this range
	 * @param min
	 *            - the minimum value that this range can be set to
	 * @param max
	 *            - the maximum value that this range can be set to
	 */
	public void addRange(String name, String value, int dataType, String min,
			String max) {
		String[] range = new String[2];
		range[0] = min;
		range[1] = max;
		features.add(new Feature(name, value, range, dataType,
				Constants.DataTypes.RANGE));
	}

	/**
	 * Adds a property feature to this feature set. This is a property that
	 * cannot be changed, but is merely information that the controller PC
	 * should be aware of. This can be seen as an immutable set of 1.
	 * 
	 * @param name
	 *            - the name of this property
	 * @param value
	 *            - the value of this property
	 * @param dataType
	 *            - the data type of this property (so the controller knows how
	 *            to parse it)
	 */
	public void addProperty(String name, String value, int dataType) {
		features.add(new Feature(name, value, dataType, Constants.DataTypes.SET));
	}

	/**
	 * Adds a variant feature to this feature set. Unlike property features,
	 * variants can be set to anything the controller desires, provided it falls
	 * within the given data type. These are meant to give the controller more
	 * free range for customization when using sensors.
	 * 
	 * @param name
	 *            - the name of this variant
	 * @param value
	 *            - the value that this variant is currently set to
	 * @param dataType
	 *            - the data type this application expects back from the
	 *            controller when it references this variant
	 */
	public void addSingleVariant(String name, int dataType) {
		features.add(new Feature(name, 
				Constants.Args.ARG_STRING_NONE, dataType, 
				Constants.DataTypes.ANY));
	}

	/**
	 * Adds a feature with a set of possible values to this feature set. The
	 * given values define the only things values that this feature should ever
	 * be set to.
	 * 
	 * @param name
	 *            - the name of this feature
	 * @param value
	 *            - the value this feature is currently set to
	 * @param dataType
	 *            - the data type this application expects back from the
	 *            controller when referencing this feature.
	 * @param set
	 *            - the set of possible values that this feature can be set to
	 */
	public void addSet(String name, String value, int dataType, String[] set) {
		features.add(new Feature(name, value, set, dataType,
				Constants.DataTypes.SET));
	}

	/**
	 * Adds a feature with a collection of possible values to this feature set.
	 * The given values define the only things values that this feature should
	 * ever be set to.
	 * 
	 * @param name
	 *            - the name of this feature
	 * @param value
	 *            - the value this feature is currently set to
	 * @param dataType
	 *            - the data type this application expects back from the
	 *            controller when referencing this feature.
	 * @param set
	 *            - the set of possible values that this feature can be set to
	 */
	public void addSet(String name, String value, int dataType, List<?> set) {
		features.add(new Feature(name, value, set, dataType,
				Constants.DataTypes.SET));
	}

	/**
	 * Removes a Feature in this container by name, if the name is found in this
	 * container of {@link Feature}s
	 * 
	 * @param featureName
	 *            - the name to search for in this container, which indicates
	 *            the Feature to remove if it is found.
	 * @return true if the feature was found and removed, false otherwise.
	 */
	public boolean removeFeature(String featureName) {
		Iterator<Feature> iter = features.iterator();
		boolean found = false;

		while (iter.hasNext() && !found) {
			if (iter.next().getName().equals(featureName)) {
				iter.remove();
				found = true;
			}
		}

		return found;
	}

	/**
	 * Query for the encoded representation of this feature set.
	 * 
	 * @return the byte array encoded representation of this feature set to be
	 *         sent to the controller PC.
	 */
	public byte[] encode() {

		byte[] result = null;

		if (!features.isEmpty()) {
			StringBuilder builder = new StringBuilder();

			for (Feature feature : features) {
				String encoded = feature.encode();
				if(encoded != null){
					builder.append(encoded);
				}
			}

			builder.append(SAVE_TO_SD);
			builder.append(CONTINUE_ON_CONNECTION_LOST);

			result = builder.toString().getBytes();
		}

		return result;
	}
	
}
