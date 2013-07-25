package com.i2r.androidremotecontroller.sensors;

import java.io.IOException;
import java.util.List;

import ARC.Constants;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;

import com.i2r.androidremotecontroller.RemoteControlActivity;

/**
 * This class models an android camera that responds to commands.
 * @author Josh Noel
 */
public class CameraSensor extends GenericDeviceSensor {

	private static final String TAG = "CameraSensor";
	private static final int PARAM_SIZE = Constants.Args.IMAGE_PARAMETER_SIZE + 3;
	private static final int START_TIME_INDEX = PARAM_SIZE - 3;
	private static final int MARKED_TIME_INDEX = PARAM_SIZE - 2;
	private static final int IMAGE_COUNT_INDEX = PARAM_SIZE - 1;
	private static final int MAX_QUALITY = 100;
	private static final String JPEG = "jpeg";
	
	@SuppressWarnings("unused")
	private static final boolean SAVE_TO_SD = true;
	private static final boolean SEND_TO_REMOTE = false;
	
	private SurfaceHolder holder;
	private Camera camera;
	private Surface surface;
	private GenericPictureCallback jpeg;
	private boolean started, waitingOnPicture, forceClose;
	private int[] parameters;
	
	public CameraSensor(Context context, Camera camera, SurfaceHolder holder) {
		super(context, null, Constants.Args.ARG_NONE);
		
		Log.d(TAG, "creating camera sensor");
		this.surface = new Surface();
		this.jpeg = new GenericPictureCallback(JPEG, SEND_TO_REMOTE);
		this.waitingOnPicture = forceClose = started = false;
		this.parameters = new int[PARAM_SIZE];
		this.parameters[START_TIME_INDEX] = 0;
		this.parameters[MARKED_TIME_INDEX] = 0;
		this.parameters[IMAGE_COUNT_INDEX] = 0;
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
		this.started = false;
	}

	
	@Override
	public void startNewTask(int taskID, int[] params) {
		Log.d(TAG, "starting new task : " + taskID);
		this.waitingOnPicture = forceClose = false;
		this.started = true;
		this.parameters[START_TIME_INDEX] = (int) System.currentTimeMillis();
		this.parameters[MARKED_TIME_INDEX] = (int) System.currentTimeMillis();
		this.parameters[IMAGE_COUNT_INDEX] = 0;
		setTaskID(taskID);
		for(int i = 0; i < parameters.length && i < params.length; i++){
			parameters[i] = params[i];
		}
		updateCamera();
		camera.startPreview();
		capture();
	}

	
	@Override
	public boolean taskCompleted() {
		return forceClose || done();
	}

	@Override
	public void modify(int key, int value) {
		if(0 <= key && key < parameters.length){
			Log.i(TAG, "modifying image capture paramters");
			parameters[key] = value;
			if(key >= Constants.Args.IMAGE_CAMERA_PARAMETER_START_INDEX){
				updateCamera();
				if(started){
					Log.i(TAG, "restarting preview");
					camera.startPreview();
				}
			}
		}
	}
	
	
	private void updateCamera(){
		if(camera != null){
			Log.i(TAG, "updating camera parameters");
			Camera.Parameters p = camera.getParameters();
			p.setExposureCompensation(parameters[Constants.Args.IMAGE_EXPOSURE_COMP_INDEX]);
			p.setPictureFormat(parameters[Constants.Args.IMAGE_FORMAT_INDEX]);
			p.setPictureSize(parameters[Constants.Args.IMAGE_SIZE_WIDTH_INDEX], 
							 parameters[Constants.Args.IMAGE_SIZE_HEIGHT_INDEX]);
			p.setJpegQuality(MAX_QUALITY);
			
			camera.setParameters(p);
		} else {
			Log.e(TAG, "error setting camera parameters, camera is null");
		}
	}

	
	// TODO: may delete this
	@Override
	public byte[] getSupportedFeatures() {

		StringBuilder builder = new StringBuilder(1000);

		if (camera != null) {

			Camera.Parameters params = camera.getParameters();
			List<String> flash = params.getSupportedFlashModes();
			List<String> focus = params.getSupportedFocusModes();
			List<String> whiteBalance = params.getSupportedWhiteBalance();
			List<Integer> formats = params.getSupportedPictureFormats();
			List<Size> sizes = params.getSupportedPictureSizes();

			
			char n = Constants.PACKET_DELIMITER;
			
			builder.append(Constants.SUPPORTED_FEATURES_HEADER);
			builder.append(n);
			builder.append(Constants.CAMERA_SENSOR_TAG);
			builder.append(n);
			builder.append(Integer.toString(params.getMinExposureCompensation()));
			builder.append(n);
			builder.append(Integer.toString(params.getMaxExposureCompensation()));
			builder.append(n);

			
			
			if (flash != null) {
				
				builder.append(Constants.PACKET_LIST_DELIMITER);
				builder.append(n);
				builder.append(Constants.Args.CAMERA_SUPPORTED_FEATURE_LIST_FLASH);
				builder.append(n);
				builder.append(Integer.toString(flash.size()));
				builder.append(n);
				
				for (int i = 0; i < flash.size(); i++) {
					builder.append(flash.get(i));
					builder.append(n);
				}
			} 
			

			if (focus != null) {
				
				builder.append(Constants.PACKET_LIST_DELIMITER);
				builder.append(n);
				builder.append(Constants.Args.CAMERA_SUPPORTED_FEATURE_LIST_FOCUS);
				builder.append(n);
				builder.append(Integer.toString(focus.size()));
				builder.append(n);
				
				for (int i = 0; i < focus.size(); i++) {
					builder.append(focus.get(i));
					builder.append(n);
				}
			} 

			
			// TODO: turn off white balance and delete this
			if (whiteBalance != null) {
				
				builder.append(Constants.PACKET_LIST_DELIMITER);
				builder.append(n);
				builder.append(Integer.toString(whiteBalance.size()));
				builder.append(n);
				
				for (int i = 0; i < whiteBalance.size(); i++) {
					builder.append(whiteBalance.get(i));
					builder.append(n);
				}
			} 
			

			
			if (formats != null) {
				
				builder.append(Constants.PACKET_LIST_DELIMITER);
				builder.append(n);
				builder.append(Constants.Args.CAMERA_SUPPORTED_FEATURE_LIST_FORMAT);
				builder.append(n);
				builder.append(Integer.toString(formats.size()));
				builder.append(n);
				
				for (int i = 0; i < formats.size(); i++) {

					String result;
					switch (formats.get(i).intValue()) {
					case ImageFormat.JPEG:
						result = Constants.Args.CAMERA_IMAGE_FORMAT_JPEG;
						break;

					case ImageFormat.NV21:
						result = Constants.Args.CAMERA_IMAGE_FORMAT_NV21;
						break;

					case ImageFormat.NV16:
						result = Constants.Args.CAMERA_IMAGE_FORMAT_NV16;
						break;

					case ImageFormat.RGB_565:
						result = Constants.Args.CAMERA_IMAGE_FORMAT_RGB_565;
						break;

					case ImageFormat.YUY2:
						result = Constants.Args.CAMERA_IMAGE_FORMAT_YUY2;
						break;

					case ImageFormat.YV12:
						result = Constants.Args.CAMERA_IMAGE_FORMAT_YV12;
						break;

					default:
						result = "unknown";
						break;
					}

					builder.append(result);
					builder.append(n);
				}
			}

			if (sizes != null) {
				
				builder.append(Constants.PACKET_LIST_DELIMITER);
				builder.append(n);
				builder.append(Constants.Args.CAMERA_SUPPORTED_FEATURE_LIST_IMAGE_SIZE);
				builder.append(n);
				builder.append(Integer.toString(sizes.size()));
				builder.append(n);
				
				for (int i = 0; i < sizes.size(); i++) {
					Size size = sizes.get(i);
					builder.append(size.width);
					builder.append(n);
					builder.append(size.height);
					builder.append(n);
				}
			}
			
			builder.append(Constants.SUPPORTED_FEATURES_FOOTER);
			builder.append(n);
			
		}
		
		return builder.toString().getBytes();
	}
	

	
	
	//******************************|
	// QUIRIES FOR CAPTURING -------|
	//******************************|
	
	
	private boolean validToSave(){
		return validFrequency() && validDuration() && validPictureCount();
	}
	
	private boolean done(){
		return !validDuration() || !validPictureCount();
	}
	
	private boolean validFrequency(){
		return parameters[Constants.Args.IMAGE_FREQUENCY_INDEX] == Constants.Args.ARG_NONE 
				|| System.currentTimeMillis() - parameters[MARKED_TIME_INDEX] 
					> parameters[Constants.Args.IMAGE_FREQUENCY_INDEX];
	}
	
	private boolean validDuration(){
		return parameters[Constants.Args.IMAGE_DURATION_INDEX] == Constants.Args.ARG_NONE
				|| System.currentTimeMillis() - parameters[START_TIME_INDEX]
					< parameters[Constants.Args.IMAGE_DURATION_INDEX];
	}
	
	private boolean validPictureCount(){
		return parameters[Constants.Args.IMAGE_MAX_COUNT_INDEX] == Constants.Args.ARG_NONE
				|| parameters[IMAGE_COUNT_INDEX] < parameters[Constants.Args.IMAGE_MAX_COUNT_INDEX];
	}
	
	
	
	private void capture(){
		if (!waitingOnPicture && validToSave()) {
			camera.takePicture(null, null, jpeg);
			parameters[IMAGE_COUNT_INDEX] = parameters[IMAGE_COUNT_INDEX] + 1;
			this.parameters[MARKED_TIME_INDEX] = (int) System.currentTimeMillis();
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
					saveDataToSD(data, Long.toString(System.currentTimeMillis()), ".jpg", getContext());
				} else {
					sendDataAcrossConnection(data, Constants.DataTypes.JPEG);
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
