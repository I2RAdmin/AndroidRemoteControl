package com.i2r.androidremotecontroller.sensors;

import ARC.Constants;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.i2r.androidremotecontroller.main.ResponsePacket;
import com.i2r.androidremotecontroller.supportedfeatures.LocationFeatureSet;

/**
 * This class models a GPS sensor that can be queried for
 * position data by the controlling device.
 * @author Josh Noel
 * @see {@link LocationManager}
 */
public class LocationSensor extends GenericDeviceSensor implements LocationListener {

	public static final String ACTION_PROXIMITY_ALERT = "i2r_action_proximity_alert";
	
	private static final String TAG = "LocationSensor";
	
	private boolean taskCompleted;
	private LocationManager manager;
	private BroadcastReceiver receiver;
	
	/**
	 * Constructor<br>
	 * Creates a broadcast receiver for proximity updates,
	 * and stores the given Activity.
	 * @param activity - the activity in which this sensor
	 * was created.
	 */
	public LocationSensor(Activity activity) {
		super(activity);
		this.taskCompleted = false;
		this.manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		createNewDuration("response-duration");
		
		this.receiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent){
				if(intent.getAction().equals(ACTION_PROXIMITY_ALERT)){
					
					boolean entering = intent.getBooleanExtra(
							LocationManager.KEY_PROXIMITY_ENTERING, false);
					String result = entering ? "entering proximity": "exiting proximity";
					
					ResponsePacket.getNotificationPacket(getTaskID(),
							Constants.Notifications.PROXIMITY_UPDATE, result)
							.send(getConnection());
				}
			}
		};
		
		activity.registerReceiver(receiver, new IntentFilter(ACTION_PROXIMITY_ALERT));
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void releaseSensor() {
		getActivity().unregisterReceiver(receiver);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void killTask() {
		taskCompleted = true;
		try{
			manager.removeUpdates(this);
		} catch(Exception e){} // do nothing
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startNewTask(int taskID, int[] args) {
		
		setTaskID(taskID);
		this.taskCompleted = false;
		
		if(args != null){
			
			getDuration(0).setMax(args[0]).start();
			String provider = getProperty(LocationFeatureSet.PROVIDER);
			
			try{
				if(getDuration(0).hasMax()){
					manager.requestLocationUpdates(provider, args[1], args[2], this);
					Log.d(TAG, "starting location responder with duration");
				} else {
					manager.requestSingleUpdate(provider, this, getActivity().getMainLooper());
					Log.d(TAG, "startinglocation responder with single response");
				}
			} catch (Exception e){
				sendTaskErroredOut(e.getMessage());
				killTask();
			}
			
		} else {
			sendTaskErroredOut("invalid gps parameters");
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

		setTaskID(taskID);
		
		if(getBooleanProperty(LocationFeatureSet.PROXIMITY_ALERT)){
			
			double latitude = getDoubleProperty(
					LocationFeatureSet.PROXIMITY_ALERT_LATITUDE);
			double longitude = getDoubleProperty(
					LocationFeatureSet.PROXIMITY_ALERT_LONGITUDE);
			float radius = (float) getDoubleProperty(
					LocationFeatureSet.PROXIMITY_ALERT_RADIUS);
			int expiration = getIntProperty(
					LocationFeatureSet.PROXIMITY_ALERT_EXPIRATION);
			
			if(latitude != Constants.Args.ARG_DOUBLE_NONE 
					&& longitude != Constants.Args.ARG_DOUBLE_NONE
					&& radius != Constants.Args.ARG_DOUBLE_NONE){
				
				PendingIntent intent = PendingIntent.getBroadcast(getActivity(), 0, 
						new Intent(ACTION_PROXIMITY_ALERT), 0);
				
				manager.addProximityAlert(latitude, longitude,
						radius, expiration, intent);
			} else {
				sendTaskErroredOut(
						"latitude, longitude and radius must be set when proximity alert is on");
			}
			
		}
		
	}


	@Override
	public void onLocationChanged(Location location) {
		if(!taskCompleted){
			if(!getDuration(0).hasMax() || !getDuration(0).maxReached()){
				StringBuilder builder = new StringBuilder();
				location.dump(new StringBuilderPrinter(builder), "");
				sendData(Constants.DataTypes.LOCATION, builder.toString().getBytes());
			} 
			
			if(getDuration(0).maxReached()){
				sendTaskComplete();
				killTask();
			}
		}
	}


	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "provider disabled: " + provider);
	}


	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "provider enabled: " + provider);
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		StringBuilder b = new StringBuilder();
		b.append("status changed: ");
		b.append(provider);
		b.append(" - status: ");
		b.append(status);
		Log.d(TAG, b.toString());
	}

}
