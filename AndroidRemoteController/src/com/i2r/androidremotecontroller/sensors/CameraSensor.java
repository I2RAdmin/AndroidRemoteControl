package com.i2r.androidremotecontroller.sensors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import ARC.Constants;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;

import com.i2r.androidremotecontroller.SupportedFeatures;
import com.i2r.androidremotecontroller.RemoteControlActivity;
import com.i2r.androidremotecontroller.ResponsePacket;

/**
 * This class models an android camera that responds to commands.
 * @author Josh Noel
 */
public class CameraSensor extends GenericDeviceSensor {

	private static final String TAG = "CameraSensor";
	
	@SuppressWarnings("unused")
	private static final int MAX_QUALITY = 100;
	private static final String JPEG = "jpeg";
	
	@SuppressWarnings("unused")
	private static final boolean SAVE_TO_SD = true;
	private static final boolean SEND_TO_REMOTE = false;
	
	private SurfaceHolder holder;
	private Camera camera;
	private Surface surface;
	private GenericPictureCallback jpeg;
	private boolean waitingOnPicture, forceClose;
	private long startTime, markedTime, pictureCount;
	
	public CameraSensor(Activity activity, Camera camera, SurfaceHolder holder) {
		super(activity, null, Constants.Args.ARG_NONE);
		
		Log.d(TAG, "creating camera sensor");
		this.surface = new Surface();
		this.jpeg = new GenericPictureCallback(JPEG, SEND_TO_REMOTE);
		this.waitingOnPicture = forceClose = false;
		this.startTime = markedTime = pictureCount = 0;
		this.camera = camera;
		
		// TODO: find display orientation before setting it
		this.camera.setDisplayOrientation(90);
		this.holder = holder;
		this.holder.addCallback(surface);
		
		modify(SupportedFeatures.KEY_CAMERA_FREQUENCY, Constants.Args.ARG_STRING_NONE);
		modify(SupportedFeatures.KEY_CAMERA_DURATION, Constants.Args.ARG_STRING_NONE);
		modify(SupportedFeatures.KEY_CAMERA_PICTURE_AMOUNT, Constants.Args.ARG_STRING_NONE);
	}

	
	@Override
	public void releaseSensor() {
		
		Log.d(TAG, "releasing camera sensor");
		
		killTask();
		
		if(camera != null){
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		
		this.holder = null;
	}

	
	@Override
	public void killTask() {
		Log.d(TAG, "killing task : " + getTaskID());
		this.forceClose = true;
	}

	
	@Override
	public void startNewTask(int taskID, String[] params) {
		Log.d(TAG, "starting new task : " + taskID);
		this.waitingOnPicture = forceClose = false;
		setTaskID(taskID);
		modify(SupportedFeatures.KEY_CAMERA_FREQUENCY, params[Constants.Args.CAMERA_FREQUENCY_INDEX]);
		modify(SupportedFeatures.KEY_CAMERA_DURATION, params[Constants.Args.CAMERA_DURATION_INDEX]);
		modify(SupportedFeatures.KEY_CAMERA_PICTURE_AMOUNT, params[Constants.Args.CAMERA_PICTURE_AMOUNT_INDEX]);
		updateCamera();
		capture();
	}

	
	@Override
	public boolean taskCompleted() {
		return !waitingOnPicture && (forceClose || done());
	}

	
	
	private void updateCamera(){
		if(camera != null){
			
			Log.d(TAG, "updating camera parameters");
			Camera.Parameters params = camera.getParameters();
			Iterator<HashMap.Entry<String, PropertyValue>> entries = 
					getProperties().entrySet().iterator();
			
			while(entries.hasNext()){
				HashMap.Entry<String, PropertyValue> next = entries.next();
				
				if(next.getValue().isNumber()){
					params.set(next.getKey(), next.getValue().getIntValue());
				} else {
					params.set(next.getKey(), next.getValue().getStringValue());
				}

			}
			
		} else {
			Log.e(TAG, "error setting camera parameters, camera is null");
		}
	}



	
	
	//******************************|
	// QUIRIES FOR CAPTURING -------|
	//******************************|
	
	
	private boolean validToSave(){
		long time = System.currentTimeMillis();
		return validFrequency(time) && validDuration(time) && validPictureCount();
	}
	
	private boolean done(){
		long time = System.currentTimeMillis();
		return !validDuration(time) || !validPictureCount();
	}
	
	private boolean validFrequency(long time){
		int frequency = getProperties().get(SupportedFeatures.KEY_CAMERA_FREQUENCY).getIntValue();
		return frequency == Constants.Args.ARG_NONE || time - markedTime > frequency;
	}
	
	private boolean validDuration(long time){
		int duration = getProperties().get(SupportedFeatures.KEY_CAMERA_DURATION).getIntValue();
		return duration == Constants.Args.ARG_NONE || time - startTime < duration;
	}
	
	
	private boolean validPictureCount(){
		int count = getProperties().get(SupportedFeatures.KEY_CAMERA_PICTURE_AMOUNT).getIntValue();
		return count == Constants.Args.ARG_NONE || pictureCount < count;
	}
	
	
	
	private void capture(){
		
		// wait for specified frequency
		Log.d(TAG, "waiting for appropriate frequency...");
		while(!validFrequency(System.currentTimeMillis())){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 
		
		if (!waitingOnPicture && validToSave()) {
			camera.takePicture(null, null, jpeg);
			pictureCount++;
			markedTime = System.currentTimeMillis();
			waitingOnPicture = true;
			Log.d(TAG, "picture taken, waiting on callback");
		}  else {
			Log.d(TAG, "picture not taken, wait status: " + Boolean.toString(waitingOnPicture));
		}

		
		if (!waitingOnPicture && taskCompleted()) {
			Intent intent = new Intent(RemoteControlActivity.ACTION_TASK_COMPLETE);
			intent.putExtra(RemoteControlActivity.EXTRA_TASK_ID, getTaskID());
			notifyRemoteDevice(Constants.TASK_COMPLETE);
			killTask();
			getBroadcastManager().sendBroadcast(intent);
		}
	}
	

	
	/**
	 * Callback implementation for the SurfaceHolder object
	 * obtained in the onCreate method. Coordinates the
	 * preview surface with the camera object obtained 
	 * in this top level class
	 * @author Josh Noel
	 */
	private class Surface implements SurfaceHolder.Callback {
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// do nothing
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// set up camera as soon as surface is available
			if(camera != null){ 
				try{
					camera.setPreviewDisplay(holder);
					camera.startPreview();
					Log.d(TAG, "set display success");
				} catch (IOException e){
					Log.d(TAG, "set preview display failed");
				} 
			} else {
				Log.d(TAG, "camera is null at set display point");
			}
		}
		
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// no point in having a sensor that can't relay data
			releaseSensor();
		}
	}
	
	
	
	/**
	 * A Generic callback for the camera when takePicture() is called.
	 * The type given at creation determines which parameter position
	 * this callback was given to the takePicture() method at.
	 * @author Josh Noel
	 */
	private class GenericPictureCallback implements PictureCallback {


		private String name;
		private boolean saveToSD;
		
		public GenericPictureCallback(String name, boolean saveToSD){
			this.name = name;
			this.saveToSD = saveToSD;
		}
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if(data == null){
				Log.e(name, "byte array is null");
			} else {
				Log.d(name, "picture taken : byte size - " + data.length);
				if(saveToSD){
					saveDataToSD(data, Long.toString(System.currentTimeMillis()), ".jpg", getActivity());
				} else {
					ResponsePacket packet = new ResponsePacket(getTaskID(), Constants.DataTypes.JPEG, data);
					ResponsePacket.sendResponse(packet, getConnection());
				}
			}
			
			if(name.equals(JPEG)){
				Log.d(TAG, "retarting preview...");
				camera.startPreview();
				waitingOnPicture = false;
				capture();
			}
		}

	} // end of GenericPictureCallback class
	
}
