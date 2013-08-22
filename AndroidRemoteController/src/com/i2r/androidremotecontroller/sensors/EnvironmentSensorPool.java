package com.i2r.androidremotecontroller.sensors;

import java.util.List;

import ARC.Constants;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.i2r.androidremotecontroller.supported_features.EnvironmentFeatureSet;
import com.i2r.androidremotecontroller.supported_features.FormatExchanger;


/**
 * <p>This class models a generic sensor that, when
 * starting a new task, registers all sensors that
 * the remote controller specified and starts sending
 * the data from those sensors' callbacks as they arrive.
 * The data will be sent at the rate that the remote
 * controller specified for each sensor (approximately).</p>
 * 
 * <p>The task will only be stopped either when the duration
 * specified by the control has been reached, or the controller
 * manually terminates this task. If the controller did not specify
 * a duration, it is imperative that the controller stops the
 * task as soon as they have the data they need, or they will
 * risk running this android device's power down at an exceedingly
 * high rate.</p>
 * 
 * @author Josh Noel
 * @see {@link SensorEventListener}
 * @see {@link Sensor}
 * @see {@link SensorManager#registerListener(SensorEventListener, Sensor, int)}
 */
public class EnvironmentSensorPool extends
				GenericDeviceSensor implements SensorEventListener {

	private static final String TAG = "GenericEnvironmentSensor";
	
	private static final String[] ACCURACIES = {
			"accuracy-status-unreliable", "accuracy-status-low",
			"accuracy-status-medium", "accuracy-status-high"
	};
	
	
	private SensorManager manager;
	private List<Sensor> sensors;
	private boolean taskCompleted;

	
	/**
	 * Constructor<br>
	 * Creates a new Environment Sensor container for all available
	 * sensors that can be retrieved from this Activity's {@link SensorManager}
	 * @param activity - the activity which created this sensor container, and
	 * also the activity that this object's {@link SensorManager} will be
	 * obtained from.
	 */
	public EnvironmentSensorPool(Activity activity) {
		super(activity);
		this.manager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		this.sensors  = manager.getSensorList(Sensor.TYPE_ALL);
		this.taskCompleted = false;
		
		createNewDuration("duration");
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void releaseSensor() {
		// do nothing
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void killTask() {
		manager.unregisterListener(this);
		this.taskCompleted = true;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startNewTask(int taskID, int[] args) {
		setTaskID(taskID);
		taskCompleted = false;
		
		Log.d(TAG, "preparing sensors");
		
		// iterate through all available sensors
		for(Sensor sensor : sensors){
			
			int rate = getIntProperty(sensor.getName());
			
			// register any sensors that the controller
			// specifically asked for
			if(rate != Constants.Args.ARG_NONE){
				manager.registerListener(this, sensor, rate);
				Log.d(TAG, "registering " + sensor.getName() 
						+ " with a rate of " + rate);
			}
		}
		
		// start this sensor's data gathering timer
		if(args != null){
			getDuration(0).setMax(args[0]).start();
			waitForCompletion();
		} else {
			sendTaskErroredOut("duration for listening was not set");
			killTask();
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean taskCompleted() {
		return taskCompleted;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return TAG;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateSensorProperties(int taskID) {
		for(Sensor sensor : sensors){
			
			String sensorName = sensor.getName();
			int result = FormatExchanger.exchange(
					EnvironmentFeatureSet.UPDATE_SPEED, getProperty(sensorName));
			
			if(result != Constants.Args.ARG_NONE){
				Log.d(TAG, "modifying " + sensorName + "with value: " + result);
				modify(sensorName, result);
			}
		}
	}
	
	
	@Override
	public synchronized void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing for now
	}

	
	@Override
	public synchronized void onSensorChanged(SensorEvent event) {
		
		// general data encoding: name, timestamp, accuracy, values
		StringBuilder builder = new StringBuilder();
		builder.append(event.sensor.getName());
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(event.timestamp);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(ACCURACIES[event.accuracy]);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		
		for(int i = 0; i < event.values.length; i++){
			builder.append(event.values[i]);
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
		}
		
		sendData(Constants.DataTypes.ENVIRONMENT_DATA, builder.toString().getBytes());
	}
	

	/**
	 * Query about the ongoing state of this environment sensor collection
	 * @return true if this sensor collection has not timed out and
	 * the task has not been killed, false otherwise
	 */
	private boolean validToSave(){
		return !taskCompleted && !getDuration(0).maxReached();
	}
	
	
	/**
	 * Creates a wait thread that kills
	 * this task after the appropriate amount
	 * of time has passed.
	 */
	private synchronized void waitForCompletion(){
		
		Log.d(TAG, "waiting for environment sensing completion...");
		
		// wait in a separate thread to kill this task
		new Thread( new Runnable(){public void run(){
			
			// don't need to do anything here since
			// data transfer is controlled by callbacks
			while(validToSave()){
				try{
					Thread.sleep(1000);
				} catch(InterruptedException e){}
			}
			
			if(!taskCompleted){
				killTask();
			}
			
			sendTaskComplete();
			
		}}).start();
	}
	
}
