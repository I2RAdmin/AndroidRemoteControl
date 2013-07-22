package com.i2r.androidremotecontroller.sensors;

import java.io.IOException;

import ARC.Constants;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;

import com.i2r.androidremotecontroller.RemoteControlActivity;

public class CameraSensor extends GenericDeviceSensor {

	private static final String TAG = "CameraSensor";
	
	private SurfaceHolder holder;
	private Camera camera;
	private Surface surface;
	private GenericPictureCallback jpeg;
	private boolean waitingOnPicture, forceClose;
	private int pictureCount, startTime, markedTime, frequency, duration, maxPictureAmount;
	
	public CameraSensor(Context context, Camera camera, SurfaceHolder holder) {
		super(context, null, Constants.Args.ARG_NONE);
		
		Log.d(TAG, "creating camera sensor");
		this.surface = new Surface();
		this.jpeg = new GenericPictureCallback(GenericPictureCallback.JPEG, GenericPictureCallback.SAVE_TO_SD);
		this.waitingOnPicture = false;
		this.pictureCount = startTime = markedTime = 0;
		this.frequency = duration = maxPictureAmount = Constants.Args.ARG_NONE;
		this.camera = camera;
		
		// TODO: find display orientation before setting it
		this.camera.setDisplayOrientation(90);
		this.holder = holder;
		this.holder.addCallback(surface);
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
	public void startNewTask(int taskID, int[] params) {
		Log.d(TAG, "starting new task : " + taskID);
		this.waitingOnPicture = forceClose = false;
		this.pictureCount = 0;
		setTaskID(taskID);
		modify(params);
		capture();
	}

	@Override
	public boolean taskCompleted() {
		return forceClose || done();
	}

	@Override
	public void modify(int[] params) {
		Log.d(TAG, "modifying image capture paramters");
		this.frequency = params[Constants.Args.IMAGE_FREQUENCY_INDEX];
		this.duration = params[Constants.Args.IMAGE_DURATION_INDEX];
		this.maxPictureAmount = params[Constants.Args.IMAGE_COUNT_INDEX];
	}
	
	
	public boolean validToSave(){
		return validFrequency() && validDuration() && validPictureCount();
	}
	
	private boolean done(){
		return !validDuration() || !validPictureCount();
	}
	
	private boolean validFrequency(){
		return frequency == Constants.Args.ARG_NONE || System.currentTimeMillis() - markedTime > frequency;
	}
	
	private boolean validDuration(){
		return duration == Constants.Args.ARG_NONE || System.currentTimeMillis() - startTime < duration;
	}
	
	private boolean validPictureCount(){
		return maxPictureAmount == Constants.Args.ARG_NONE || pictureCount < maxPictureAmount;
	}
	
	
	
	private void capture(){
		if (!waitingOnPicture && validToSave()) {
			camera.takePicture(null, null, jpeg);
			pictureCount++;
			Log.d(TAG, "picture taken, waiting on callback");
		} 

		if (!waitingOnPicture && taskCompleted()) {
			Intent intent = new Intent(RemoteControlActivity.ACTION_UPDATE_MASTER);
			intent.putExtra(RemoteControlActivity.EXTRA_TASK_ID, getTaskID());
			killTask();
			getContext().sendBroadcast(intent);
			// notifyRemoteDevice later maybe
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
					//camera.setPreviewCallback(preview);
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
	
	
	
	
	private class GenericPictureCallback implements PictureCallback {

		private static final String JPEG = "jpeg";
		private static final boolean SAVE_TO_SD = true;
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
					saveDataToSD(data, Long.toString(System.currentTimeMillis()), ".jpg", getContext());
				} else {
					//sendDataAcrossConnection(data, Constants.DataTypes.JPEG);
				}
			}
			if(name.equals(JPEG)){
				Log.d(TAG, "retarting preview...");
				camera.startPreview();
				waitingOnPicture = false;
				capture();
			}
		}

	}// end of GenericPictureCallback class
	
}
