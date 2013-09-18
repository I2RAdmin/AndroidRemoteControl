package com.i2r.ARC.PCControl.GUI;

import java.util.LinkedList;
import java.util.List;

import com.i2r.ARC.PCControl.UnsupportedValueException;

/**
 * This class models a data structure for information
 * obtained from the control stream about a certain device.
 * @author Josh Noel
 */
public class ArcControlDevice {
	
	
	private String name;
	private int index;
	private List<GuiSensor> features;

	
	/**
	 * Constructor<br>
	 * creates a new Android device structure to
	 * query for information when making its
	 * graphical counterpart for this GUI.
	 * @param name - the name of this android device
	 * @param index - the index of this android device in the {@link Controller}
	 * @param sensorList - the sensor list for this device - each sub-array is
	 * a unique sensor with its name at index zero, followed by all of its manipulatable features.
	 * @throws UnsupportedValueException if the given sensor list cannot be properly parsed
	 */
	public ArcControlDevice(String name, int index, String[][] sensorList) throws UnsupportedValueException {
		this.name = name;
		this.features = new LinkedList<GuiSensor>();
		
		for(int i = 0; i < sensorList.length; i++){
			String[] featureList = sensorList[i];
			for(int j = 0; j < featureList.length; j++){
				features.add(new GuiSensor(featureList));
			}
		}
	}
	
	/**
	 * Query for this device's name
	 * @return the name given to this android device object
	 * upon its creation
	 */
	public String getName(){
		return name;
	}
	
	
	/**
	 * Query for this device's index - to be used when
	 * sending remote commands to this device
	 * @return this device's index in this GUI's {@link Controller}
	 */
	public int getIndex(){
		return index;
	}
	
	/**
	 * Query for this device's sensor information.
	 * @return a {@link List} of all this device's
	 * manipulatable sensors
	 * @see {@link GuiSensor}
	 */
	public List<GuiSensor> getSensorList(){
		return features;
	}
	
}
