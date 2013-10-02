package com.i2r.androidremotecontroller.supportedfeatures;

import ARC.Constants;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.util.SparseArray;

import com.i2r.androidremotecontroller.connections.RemoteConnection;
import com.i2r.androidremotecontroller.main.ResponsePacket;


/**
 * Helper class for notifying controller of this
 * device's available features.
 * @author Josh Noel
 */
public class FullFeatureSet {

	
	private SparseArray<FeatureSet> sparse;
	private SensorManager sensorManager;
	private LocationManager locationManager;
	
	/**
	 * Constructor<br>
	 * Creates a full set of features for this device,
	 * based on the activity and camera parameters given.
	 * The parameters given are used to access this device's
	 * hardware sensors to see what they are capable of.
	 * @param activity - the activity to probe for sensor information.
	 * @param camera - the {@link Camera} object to probe for camera
	 * information - this needs to be passed as a parameter since
	 * {@link Camera#open()} apparently can't be called more than
	 * once without crashing this program.
	 */
	public FullFeatureSet(Activity activity, Camera camera){
		
		this.sparse = new SparseArray<FeatureSet>();
		
		this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		
		sparse.append(Constants.Sensors.CAMERA, new CameraFeatureSet(camera));
		sparse.append(Constants.Sensors.MICROPHONE, MicrophoneFeatureSet.getInstance());
		sparse.append(Constants.Sensors.ENVIRONMENT_SENSORS, new EnvironmentFeatureSet(sensorManager));
		sparse.append(Constants.Sensors.GPS, new LocationFeatureSet(locationManager));
	}
	
	
	
	/**
	 * Called if the given packet is a query for features of this device, so
	 * find out which feature descriptions the controller wants and send them.
	 * @param packet - the packet containing a request for this device's features
	 */
	public void sendFeatures(int[] features, int taskID, RemoteConnection connection){
			
			for(int i = 0; i < features.length; i++){
				
				FeatureSet set = sparse.get(features[i]);
				
				if(set != null){
					new ResponsePacket(taskID, features[i], set.encode()).send(connection);
				} else {
					ResponsePacket.getNotificationPacket(taskID, 
							Constants.Notifications.SENSOR_NOT_SUPPORTED,
							String.valueOf(features[i])).send(connection);
				}
			}
			
			ResponsePacket.getNotificationPacket(taskID,
					Constants.Notifications.TASK_COMPLETE).send(connection);

	}
	
	
	
	/**
	 * Query for a {@link FeatureSet} stored in
	 * this full set.
	 * @param index - the index mapped to the feature set.
	 * @return the feature set associated with the given index,
	 * or null if no feature set was found at the given index.
	 * @see {@link Constants#Sensors}
	 */
	public FeatureSet get(int index){
		return sparse.get(index);
	}
	
	
	/**
	 * Query for this full set's camera sub-set
	 * @return the {@link CameraFeatureSet} created by this
	 * full feature set upon its creation, or null if the sensor
	 * is not supported by this device.
	 */
	public CameraFeatureSet getCameraFeatureSet(){
		return (CameraFeatureSet) sparse.get(Constants.Sensors.CAMERA);
	}
	
	
	/**
	 * Query for this full set's microphone sub-set
	 * @return the {@link MicrophoneFeatureSet} created by this
	 * full feature set upon its creation, or null if the sensor
	 * is not supported by this device.
	 */
	public MicrophoneFeatureSet getMicrophoneFeatureSet(){
		return (MicrophoneFeatureSet) sparse.get(Constants.Sensors.MICROPHONE);
	}
	
	
	/**
	 * Query for this full set's environment sub-set
	 * @return the {@link EnvironmentFeatureSet} created by this
	 * full feature set upon its creation, or null if the sensor
	 * is not supported by this device.
	 */
	public EnvironmentFeatureSet getEnvironmentFeatureSet(){
		return (EnvironmentFeatureSet) sparse.get(Constants.Sensors.ENVIRONMENT_SENSORS);
	}
	
	
	/**
	 * Query for this full set's location sub-set
	 * @return the {@link LocationFeatureSet} created by this
	 * full feature set upon its creation, or null if the sensor
	 * is not supported by this device.
	 */
	public LocationFeatureSet getLocationFeatureSet(){
		return (LocationFeatureSet) sparse.get(Constants.Sensors.GPS);
	}
	
	
} // end of FullFeatureSet class
