package com.i2r.androidremotecontroller.sensors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import ARC.Constants;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.i2r.androidremotecontroller.CommandFilter;
import com.i2r.androidremotecontroller.ResponsePacket;
import com.i2r.androidremotecontroller.SupportedFeatures;
import com.i2r.androidremotecontroller.connections.RemoteConnection;

/**
 * This abstract class models the implementation needed for a piece of hardware
 * on this device to be controlled remotely. Here, the context and taskID are
 * defined so that the {@link CommandFilter} can control each concrete
 * implementation accordingly.
 * 
 * @author Josh Noel
 */
public abstract class GenericDeviceSensor {

	private static final String TAG = "GenericDeviceSensor";

	private int taskID;
	private LocalBroadcastManager manager;
	private Activity activity;
	private HashMap<String, String> properties;
	private RemoteConnection connection;
	
	
	// Constructor
	public GenericDeviceSensor(Activity activity) {
		this.activity = activity;
		this.manager = LocalBroadcastManager.getInstance(activity);
		this.properties = new HashMap<String, String>();
		this.connection = null;
		this.taskID = Constants.Args.ARG_NONE;
	}


	
	/**
	 * @return the properties data structure for this
	 * sensor. This structure stores all modification
	 * commands received by this sensor since its creation.
	 */
	protected HashMap<String, String> getProperties(){
		return properties;
	}
	

	/**
	 * Get an integer property defined by the given key
	 * from this sensor's map of properties
	 * @param key - the key defining the property to be
	 * retrieved.
	 * @return the value of the key given in its integer form,
	 * given that the key exists in this sensor's map of
	 * properties, and the value can be parsed to an integer.
	 * If the given key is not in this sensor's map of properties,
	 * this returns {@link ARG_NONE} as defined in {@link Constants#Args}
	 */
	protected int getIntProperty(String key){
		return getIntProperty(key, Constants.Args.ARG_NONE);
	}
	
	
	
	/**
	 * TODO: comment
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	protected int getIntProperty(String key, int defaultValue){
		int result = defaultValue;
		String temp = properties.get(key);
		if(temp != null){
			try{
				result = Integer.parseInt(properties.get(key));
			} catch (NumberFormatException e){
				
			}
		}
		return result;
	}
	
	
	/**
	 * Get a string property defined by the given key
	 * from this sensor's map of properties
	 * @param key - the key defining the property to be
	 * retrieved.
	 * @return the value of the key given, if it exists in
	 * this sensor's map of properties. If the given key is
	 * not in this sensor's map of properties, this returns
	 * {@link ARG_NONE} as defined in {@link Constants#Args}
	 */
	protected String getProperty(String key){
		return properties.get(key);
	}
	
	
	
	/**
	 * Query for this SensorCapture implementation's {@link Context}.
	 * @return the context given at the point of creation.
	 */
	public Activity getActivity() {
		return activity;
	}
	
	
	/**
	 * Query for the broadcast manager to send messages to the
	 * main activity with
	 * @return a LocalBroadcastManager relative to the main activity
	 */
	protected LocalBroadcastManager getBroadcastManager() {
		return manager;
	}

	
	/**
	 * Query for this sensor's connection to relay its data to
	 * @return the connection this sensor has, or null if there is
	 * no valid connection
	 */
	protected RemoteConnection getConnection(){
		return connection;
	}
	

	/**
	 * Query for this SensorCapture implementation's task ID given at the point
	 * of creation.
	 * @return the task ID given by the remote PC
	 */
	public int getTaskID() {
		return taskID;
	}

	
	/**
	 * Sets the ID for this capture object. Must be used only when this object's
	 * task has been completed, in order to start a new task.
	 * @param id - the task ID to give this object, to start a new task.
	 */
	protected void setTaskID(int id) {
		this.taskID = id;
	}

	
	/**
	 * Use char Constants in {@link Constants} for this to notify remote PC of state changes
	 */
	protected void sendTaskComplete() {
		ResponsePacket.sendNotification(taskID,
				Constants.Notifications.TASK_COMPLETE, connection);
	}
	
	
	protected void sendTaskErroredOut(String message){
		ResponsePacket.sendNotification(taskID, 
				Constants.Notifications.TASK_ERRORED_OUT,
				message, connection);
	}
	
	
	/**
	 * Use String Constants in {@link Constants} for this to notify remote PC of state changes
	 */
	protected void sendTaskErroredOut(){
		ResponsePacket.sendNotification(taskID,
				Constants.Notifications.TASK_ERRORED_OUT, connection);
	}
	
	
	/**
	 * Sets the connection for this sensor to relay data over
	 * @param connection - the connection to obtain a reference from
	 */
	public void setConnection(RemoteConnection connection){
		this.connection = connection;
	}
	
	
	
	/**
	 * Modifies the current sensor's parameters with the one given.
	 * @param params - the parameter to modify
	 */
	public void modify(String key, String value){
		properties.put(key, value);
	}
	
	
	/**
	 * Modifies the current sensor's parameters with the one given.
	 * @param params - the parameter to modify
	 */
	public void modify(String key, int value){
		properties.put(key, String.valueOf(value));
	}
	
	
	/**
	 * Saves the given data to the native SD card, then alerts the system through the
	 * given Context to scan for new files on the SD card
	 * @param data - the data to save to the SD card
	 * @param fileName - the file name to save the data under
	 * @param extension - the type of data being saved
	 * @param context - the context in which to alert the system to scan for new files on
	 * the SD card.
	 */
	protected void saveDataToSD(byte[] data, String fileName, String extension){
		
		Log.d(TAG, "attempting to save data...");
		FileOutputStream stream = null;
		try {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS), fileName + extension);
			file.createNewFile();
			stream = new FileOutputStream(file);
			stream.write(data);
			stream.flush();
			stream.close();
			manager.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
					Uri.parse("file://"+ file)));
			Log.i(TAG, "data save to SD was successful");
		} catch (IOException e) {
			Log.e(TAG, "data save to SD failed (IOE) - " + e.getMessage());
			e.printStackTrace();
		} finally {
			if(stream != null){try{stream.flush(); stream.close();}catch(IOException e){}}
		}
	}
	
	
	/**
	 * Static method for defining what makes a sensor available for use
	 * @param sensor - the sensor to test for availability
	 * @param taskID - the task ID to ask it for
	 * @return true if the sensor is available and not running
	 */
	public static boolean isSensorAvailable(GenericDeviceSensor sensor, int taskID){
		return sensor != null && ((sensor.taskCompleted()
				&& sensor.getTaskID() == taskID) || sensor.getTaskID() != taskID);
	}
	
	
	
	/**
	 * Query for how any result data obtained from
	 * this sensor should be treated during a running task
	 * @return true if result data should be saved to the
	 * internal storage of the phone (possibly to free up bandwidth
	 * on the connection), false if the data should be sent to the
	 * remote device directly
	 */
	public boolean saveResultDataToFile(){
		String result = properties.get(SupportedFeatures.KEY_SAVE_TO_SD);
		return result != null && result.equals(SupportedFeatures.TRUE);
	}
	
	
	
	public boolean continueOnConnectionLost(){
		String result = properties.get(SupportedFeatures.KEY_CONTINUE_ON_CONNECTION_LOST);
		return result != null && result.equals(SupportedFeatures.TRUE);
	}
	
	
	
	/**
	 * Used when a device sensor's resources must be freed to use elsewhere.
	 * @requires all resources for this sensor will be released so that it may be
	 * used in other applications.
	 * @ensures this sensor will be available for use after this is called
	 */
	public abstract void releaseSensor();
	
	
	/**
	 * Kills the current task this sensor is executing, regardless of
	 * if the task was completed or not. This can be used to kill a task after
	 * it has completed successfully.
	 * @requires the current task ID and parameters become void and
	 * taskCompleted() returns true after this is called.
	 * @ensures a new task can be started for this sensor without error
	 */
	public abstract void killTask();
	
	
	/**
	 * Starts a new task for this sensor
	 * @param taskID - the ID assigned to the new task
	 * @param params - the parameters that the new task requires of this sensor
	 * @requires this sensor has no current;y running tasks and taskCompleted() returns true
	 * @ensures if this sensor has no current task, a new task with the given ID and parameters
	 * will be started.
	 * @see isSensorAvailable(GenericDeviceSensor, int)
	 */
	public abstract void startNewTask(int taskID, int[] args);
	
	
	/**
	 * Query about the state of this sensor's task
	 * @return true if the task has completed (successfully or unsuccessfully),
	 * false otherwise.
	 */
	public abstract boolean taskCompleted();
	
	
	
	/**
	 * Query for this sensor's name, or string id
	 * @return the name of this sensor, usually what
	 * this sensor is (i.e., CameraSensor, AccelerometerSensor, etc.)
	 * For ease of use, just return a TAG constant defined in any
	 * subclasses for logging.
	 */
	public abstract String getName();
	

	/**
	 * Updates this sensor's current properties list
	 * with the ones stored in the HashMap defined by
	 * {@link #getProperties()}. This is a heavy weight
	 * procedure, and should only be called when this
	 * object's HashMap is considered to be stable and
	 * rarely changing.
	 */
	public abstract void updateSensorProperties();
	
	
}
