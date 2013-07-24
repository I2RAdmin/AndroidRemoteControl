package com.i2r.androidremotecontroller.sensors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ARC.Constants;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.i2r.androidremotecontroller.ResponsePacket;
import com.i2r.androidremotecontroller.connections.RemoteConnection;

/**
 * This abstract class models the implementation needed for a piece of hardware
 * on this device to be controlled remotely. Here, the context and taskID are
 * defined so that the {@link SensorController} can control each concrete
 * implementation accordingly.
 * 
 * @author Josh Noel
 */
public abstract class GenericDeviceSensor {

	private static final String TAG = "GenericDeviceSensor";

	private int taskID;
	private Context context;
	private RemoteConnection connection;
	
	
	// Constructor
	public GenericDeviceSensor(Context context, RemoteConnection connection, int taskID) {
		this.context = context;
		this.connection = connection;
		this.taskID = taskID;
	}


	
	/**
	 * Query for this SensorCapture implementation's {@link Context}.
	 * @return the context given at the point of creation.
	 */
	public Context getContext() {
		return context;
	}

	
	/**
	 * Query for this sensor's connection to relay its data to
	 * @return the connection this sensor has, or null if there is
	 * no valid connection
	 */
	public RemoteConnection getConnection(){
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
	 * Writes the result data that this sensor has accumulated to this
	 * manager's client socket, if it is open.
	 * @param data - the data to write to this RemoteConnection, if the
	 * connection is available
	 */
	protected void sendDataAcrossConnection(byte[] data, int dataType) { // TODO: make sequence task id, datatype, data size, data

		Log.d(TAG, "attempting to send data to across remote connection...");
		if (connection.isConnected()) {
			ResponsePacket response = new ResponsePacket(taskID, dataType, data);
			if(ResponsePacket.sendResponse(response, connection)) {
				Log.e(TAG, "response sent successfully:\n" + response.toString());
			} else {
				Log.e(TAG, "error sending response across connection:\n" + response.toString());
			}
			
			// connection given is not open, therefore no connection to write to
		} else {
			Log.e(TAG, Constants.ERROR_ON_DATA_TRANSFER + " - " + data.length
					+ " : task ID - " + taskID);
		}
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
	public static void saveDataToSD(byte[] data, String fileName, String extension, Context context){
		
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
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
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
	 * Use char Constants in {@link Constants} for this to notify remote PC of state changes
	 */
	protected void notifyRemoteDevice(char notification) {
		notifyRemoteDevice(notification + "");
	}
	
	
	/**
	 * Use String Constants in {@link Constants} for this to notify remote PC of state changes
	 */
	protected void notifyRemoteDevice(String notification){
		sendDataAcrossConnection(notification.getBytes(), Constants.DataTypes.NOTIFY);
	}
	
	
	/**
	 * Sets the connection for this sensor to relay data over
	 * @param connection - the connection to obtain a reference from
	 */
	public void setConnection(RemoteConnection connection){
		this.connection = connection;
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
	public abstract void startNewTask(int taskID, int[] params);
	
	
	/**
	 * Query about the state of this sensor's task
	 * @return true if the task has completed (successfully or unsuccessfully),
	 * false otherwise.
	 */
	public abstract boolean taskCompleted();
	
	
	/**
	 * Modifies the current sensor's parameters with the ones given. The int
	 * array given should be the length of all available parameters to change
	 * for this sensor, so that an ArrayIndexOutOfBoundsException won't occur.
	 * @requires this sensors parameters can be modified at any time, even
	 * when it is currently running a task.
	 * @ensures this sensor will respond to the given changes immediately, and
	 * the remote connection will be updated with the changes that occurred.
	 * @param params - the parameters to modify
	 */
	public abstract void modify(int key, int value);
	
	
	/**
	 * As of now, this returns {@link Constants#SUPPORTED_FEATURES_HEADER}
	 * followed by a Sensor descriptor tag, such as {@link Constants#CAMERA_SENSOR_TAG}
	 * followed by a well ordered list of supported features, then finally
	 * followed by the {@link Constants#SUPPORTED_FEATURES_FOOTER} tag. Any features
	 * that are a sub-list of this list of features should have their size given beforehand,
	 * so the controller PC knows how much data to expect.
	 * @return a String array of all the supported features
	 * valid for this application.
	 */
	public abstract byte[] getSupportedFeatures();

	
}
