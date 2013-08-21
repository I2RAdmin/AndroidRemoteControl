package com.i2r.androidremotecontroller.supported_features;

import java.util.List;

import ARC.Constants;
import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * This class models a feature set for all the environment
 * sensors that the controller PC can remotely activate on
 * this android device.
 * @author Josh Noel
 * @see {@link FeatureSet}
 * @see {@link Feature}
 */
public class EnvironmentFeatureSet extends FeatureSet {

	
	public static final String DURATION = "recording-duration";
	public static final String UPDATE_SPEED = "update-speed";
	
	public static final int[] INTEGER_UPDATE_RATES = {
		SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_NORMAL,
		SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI
	};
	
	public static final String[] STRING_UPDATE_RATES = {
		"update-fastest", "update-normal", "update-slow", "update-slowest"
	};
	
	
	/**
	 * Constructor<br>
	 * @param manager - the manager to obtain supported
	 * features information from.
	 * @see {@link EnvironmentFeatureSet}
	 */
	public EnvironmentFeatureSet(SensorManager manager){
		
		if(manager != null){
			List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
			
			if(sensors != null){
				for(Sensor sensor : sensors){
					addSet(sensor.getName(),
							Constants.Args.ARG_STRING_NONE, 
							Constants.DataTypes.STRING,
							EnvironmentFeatureSet.STRING_UPDATE_RATES);
				}
			}
		}
	}
	
	
} // end of EnvironmentFeatureSet class
