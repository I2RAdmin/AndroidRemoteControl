package com.i2r.androidremotecontroller.sensors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import ARC.Constants;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;

import com.i2r.androidremotecontroller.main.ResponsePacket;
import com.i2r.androidremotecontroller.main.SupportedFeatures;
import com.i2r.androidremotecontroller.main.SupportedFeatures.CameraKeys;

/**
 * This class models a camera sensor that responds to commands
 * sent by a remote controller.
 * @author Josh Noel
 */
public class CameraSensor extends GenericDeviceSensor {

	private static final String TAG = "CameraSensor";
	private static final String JPEG = "jpeg";
	
	private SurfaceHolder holder;
	private Camera camera;
	private Surface surface;
	private GenericPictureCallback pictureCallback;
	private boolean waitingOnPicture, forceClose, started;
	private long pictureCount;
	
	
	/**
	 * Constructor<br>
	 * creates a new CameraSensor object with the given camera to use
	 * for taking pictures, and the given surface holder to use as a
	 * preview display (required by the API for taking pictures)
	 * @param activity - the activity in which this sensor was created
	 * @param camera - the camera passed from the main activity
	 * @param holder - the surface holder passed from the main activity
	 */
	public CameraSensor(Activity activity, Camera camera, SurfaceHolder holder) {
		super(activity);
		
		Log.d(TAG, "creating camera sensor");
		this.surface = new Surface();
		this.pictureCallback = new GenericPictureCallback(JPEG);
		this.waitingOnPicture = forceClose = started = false;
		this.pictureCount = 0;
		this.camera = camera;
		
		// TODO: find display orientation before setting it
		this.camera.setDisplayOrientation(90);
		this.holder = holder;
		this.holder.addCallback(surface);
		
		createNewDuration("frequency");
		createNewDuration("duration");
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
		
		setTaskID(taskID);
		
		if(params != null){
			modify(SupportedFeatures.CameraKeys.PICTURE_AMOUNT, 
					params[Constants.Args.AMOUNT_INDEX]);
			
			getDuration(Constants.Args.FREQUENCY_INDEX)
				.setMax(params[Constants.Args.FREQUENCY_INDEX]).start();
			
			getDuration(Constants.Args.DURATION_INDEX)
				.setMax(params[Constants.Args.DURATION_INDEX]).start();
			
			capture();
		} else {
			sendTaskErroredOut("invalid parameters for camera");
			killTask();
		}
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
	public void updateSensorProperties(int taskID){
		
		int result = SupportedFeatures.exchange(
				CameraKeys.PICTURE_FORMAT, getProperty(CameraKeys.PICTURE_FORMAT));
		
		if(result != Constants.Args.ARG_NONE){
			modify(CameraKeys.PICTURE_FORMAT, result);
		}
		
		result = SupportedFeatures.exchange(
				CameraKeys.PREVIEW_FORMAT, getProperty(CameraKeys.PREVIEW_FORMAT));
		
		if(result != Constants.Args.ARG_NONE){
			modify(CameraKeys.PREVIEW_FORMAT, result);
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
			
			try{
				camera.setParameters(params);
			} catch (RuntimeException e){
				ResponsePacket.getNotificationPacket(
						taskID, Constants.Notifications.TASK_ERRORED_OUT,
						e.getMessage()).send(getConnection());
			}
			
			
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
	
	
	/**
	 * Query for this sensor's ability to save data at
	 * the current moment
	 * @return true if all conditions set by the controller
	 * are met at the current moment, false otherwise
	 */
	private boolean validToSave(){
		return validFrequency() && validDuration() 
				&& validPictureCount() && !forceClose && !waitingOnPicture;
	}
	
	
	/**
	 * Query for the progress of this task.
	 * @return true if the task has successfully
	 * completed, false otherwise
	 */
	private boolean done(){
		return !validDuration() || !validPictureCount();
	}
	
	
	/**
	 * Query for the frequency state of this task.
	 * with regard to the frequency set by the controller.
	 * @param time - the time marker to compare to
	 * @return true if the amount of time that has passed
	 * is greater than or equal to the frequency set by the
	 * controller; also returns true if the frequency was never set.
	 * If the controller did set the frequency and the current amount
	 * of time passed is less than the set frequency, returns false.
	 */
	private boolean validFrequency(){
		return !getDuration(Constants.Args.FREQUENCY_INDEX).hasMax() ||
				getDuration(Constants.Args.FREQUENCY_INDEX).maxReached();
	}
	
	
	/**
	 * Query for the duration state of this task.
	 * @param time - the amount of time elapsed
	 * @return true if the current time subtracted
	 * from the start time is less than the duration
	 * set by the controller; also returns true if
	 * the duration was not set by the controller.
	 * If the duration was set and the current time
	 * passed is greater than the set duration,
	 * returns false.
	 */
	private boolean validDuration(){
		return !getDuration(Constants.Args.DURATION_INDEX).hasMax() 
				|| !getDuration(Constants.Args.DURATION_INDEX).maxReached();
	}
	
	
	/**
	 * Query about the current amount of
	 * pictures taken since this task was started.
	 * @return true if the current picture count
	 * is less than the max picture count set by
	 * the controller; also returns true if the
	 * controller did not set the max picture count.
	 * If the max picture count was set by the controller
	 * and the current picture count is greater than that
	 * value, this returns false.
	 */
	private boolean validPictureCount(){
		int count = getIntProperty(SupportedFeatures.CameraKeys.PICTURE_AMOUNT);
		return count == Constants.Args.ARG_NONE || pictureCount < count;
	}
	
	
	/**
	 * Main looping method for taking pictures.
	 * This gets called by {@link #startNewTask(int, int[])}
	 * and then subsequently by this class's
	 * {@link GenericPictureCallback} until
	 * {@link #done()} returns true.
	 */
	private void capture(){
		
		// wait for specified frequency
		Log.d(TAG, "waiting for appropriate frequency...");
		while(!validFrequency()){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// do nothing
			}
		} 
		
		if (validToSave()) {
			
			camera.takePicture(null, null, pictureCallback);
			pictureCount++;
			getDuration(Constants.Args.FREQUENCY_INDEX).start();
			waitingOnPicture = true;
			Log.d(TAG, "picture taken, waiting on callback");
			
		}  else {
			
			StringBuilder status = new StringBuilder();
			status.append("picture not taken\nwaiting on picture: ");
			status.append(waitingOnPicture);
			status.append("\nvalid picture count: ");
			status.append(validPictureCount());
			status.append("\nvalid duration: ");
			status.append(validDuration());
			Log.d(TAG, status.toString());
		}

		
		if (taskCompleted()) {
			sendTaskComplete();
			killTask();
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
		
		
		/**
		 * This figures out what to do with result data based on
		 * {@link GenericDeviceSensor#saveResultDataToFile()}
		 * and {@link GenericDeviceSensor#continueOnConnectionLost()}
		 * @param data - the data to store/send across the current connection
		 * @param saveToSD - the flag for saving to the SD card - only
		 * saves to the SD card if this flag is true.
		 */
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
					sendData(Constants.DataTypes.IMAGE, data);
				}
				
			}}, builder.toString()).start();
		}

	} // end of GenericPictureCallback class
	
}
