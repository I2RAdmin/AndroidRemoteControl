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
import com.i2r.androidremotecontroller.SupportedFeatures.CameraKeys;

/**
 * This class models an android camera that responds to commands.
 * @author Josh Noel
 */
public class CameraSensor extends GenericDeviceSensor {

	private static final String TAG = "CameraSensor";
	
	@SuppressWarnings("unused")
	private static final int MAX_QUALITY = 100;
	private static final String JPEG = "jpeg";
	
	private SurfaceHolder holder;
	private Camera camera;
	private Surface surface;
	private GenericPictureCallback pictureCallback;
	private boolean waitingOnPicture, forceClose, started;
	private long startTime, markedTime, pictureCount;
	
	public CameraSensor(Activity activity, Camera camera, SurfaceHolder holder) {
		super(activity);
		
		Log.d(TAG, "creating camera sensor");
		this.surface = new Surface();
		this.pictureCallback = new GenericPictureCallback(JPEG);
		this.waitingOnPicture = forceClose = started = false;
		this.startTime = markedTime = pictureCount = 0;
		this.camera = camera;
		
		// TODO: find display orientation before setting it
		this.camera.setDisplayOrientation(90);
		this.holder = holder;
		this.holder.addCallback(surface);
		
		modify(SupportedFeatures.CameraKeys.FREQUENCY, Constants.Args.ARG_STRING_NONE);
		modify(SupportedFeatures.CameraKeys.DURATION, Constants.Args.ARG_STRING_NONE);
		modify(SupportedFeatures.CameraKeys.PICTURE_AMOUNT, Constants.Args.ARG_STRING_NONE);
	}

	
	@Override
	public void releaseSensor() {
		
		Log.d(TAG, "releasing camera sensor");
		
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
		this.started = waitingOnPicture = false;
	}

	
	@Override
	public void startNewTask(int taskID, int[] params) {
		Log.d(TAG, "starting new task : " + taskID);
		this.waitingOnPicture = forceClose = false;
		this.started = true;
		this.pictureCount = 0;
		this.startTime = markedTime = System.currentTimeMillis();
		setTaskID(taskID);
		modify(SupportedFeatures.CameraKeys.FREQUENCY, params[Constants.Args.FREQUENCY_INDEX]);
		modify(SupportedFeatures.CameraKeys.DURATION, params[Constants.Args.DURATION_INDEX]);
		modify(SupportedFeatures.CameraKeys.PICTURE_AMOUNT, params[Constants.Args.AMOUNT_INDEX]);
		updateSensorProperties();
		capture();
	}

	
	@Override
	public boolean taskCompleted() {
		return !waitingOnPicture && (forceClose || done());
	}

	
	@Override
	public String getName(){
		return TAG;
	}
	
	
	@Override
	public void updateSensorProperties(){
		
		String sTemp = getProperty(CameraKeys.PICTURE_FORMAT);
		if(sTemp != null){
			int iTemp = SupportedFeatures.exhangeImageFormat(sTemp);
			modify(CameraKeys.PICTURE_FORMAT, iTemp);
		}
		
		if(camera != null){
			
			Log.d(TAG, "updating camera parameters");
			Camera.Parameters params = camera.getParameters();
			Iterator<HashMap.Entry<String, String>> entries = 
					getProperties().entrySet().iterator();
			
			while(entries.hasNext()){
				HashMap.Entry<String, String> next = entries.next();
				params.set(next.getKey(), next.getValue());
			}
			
			camera.setParameters(params);
			
			if(started){
				camera.startPreview();
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
		return validFrequency(time) && validDuration(time) && validPictureCount() && !forceClose && !waitingOnPicture;
	}
	
	private boolean done(){
		long time = System.currentTimeMillis();
		return !validDuration(time) || !validPictureCount();
	}
	
	private boolean validFrequency(long time){
		int frequency = getIntProperty(SupportedFeatures.CameraKeys.FREQUENCY);
		return frequency == Constants.Args.ARG_NONE || time - markedTime > frequency;
	}
	
	private boolean validDuration(long time){
		int duration = getIntProperty(SupportedFeatures.CameraKeys.DURATION);
		return duration == Constants.Args.ARG_NONE || time - startTime < duration;
	}
	
	
	private boolean validPictureCount(){
		int count = getIntProperty(SupportedFeatures.CameraKeys.PICTURE_AMOUNT);
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
		
		if (validToSave()) {
			
			camera.takePicture(null, null, pictureCallback);
			pictureCount++;
			markedTime = System.currentTimeMillis();
			waitingOnPicture = true;
			Log.d(TAG, "picture taken, waiting on callback");
			
		}  else {
			
			StringBuilder status = new StringBuilder();
			status.append("picture not taken\nwaiting on picture: ");
			status.append(waitingOnPicture);
			status.append("\nvalid picture count: ");
			status.append(validPictureCount());
			status.append("\nvalid duration: ");
			status.append(validDuration(System.currentTimeMillis()));
			Log.d(TAG, status.toString());
		}

		
		if (taskCompleted()) {
			Intent intent = new Intent(RemoteControlActivity.ACTION_TASK_COMPLETE);
			intent.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE, "task complete: " + getTaskID());
			sendTaskComplete();
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
			// do nothing
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
		
		public GenericPictureCallback(String name){
			this.name = name;
		}
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if(data == null){
				Log.e(name, "byte array is null");
			} else {
				Log.d(name, "picture taken : byte size - " + data.length);
				saveData(data, saveResultDataToFile());
			}
			
			if(name.equals(JPEG)){
				Log.d(TAG, "retarting preview...");
				camera.startPreview();
				waitingOnPicture = false;
				capture();
			}
		}
		
		
		private void saveData(final byte[] data, final boolean saveToSD){
			StringBuilder builder = new StringBuilder();
			builder.append("write-picture-");
			builder.append(getTaskID());
			builder.append("-");
			builder.append(pictureCount);
			new Thread(new Runnable() { public void run(){
				if(saveToSD){
					saveDataToSD(data, Long.toString(System.currentTimeMillis()), ".jpg");
				} else {
					ResponsePacket packet = new ResponsePacket(getTaskID(), Constants.DataTypes.IMAGE, data);
					ResponsePacket.sendResponse(packet, getConnection());
				}
			}}, builder.toString()).start();
		}

	} // end of GenericPictureCallback class
	
}
