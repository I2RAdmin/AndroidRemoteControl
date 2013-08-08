package com.i2r.androidremotecontroller.sensors;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ARC.Constants;
import android.app.Activity;
import android.media.MediaRecorder;
import android.util.Log;

import com.i2r.androidremotecontroller.ResponsePacket;
import com.i2r.androidremotecontroller.SupportedFeatures.AudioKeys;


/**
 * This class models an android microphone sensor that
 * can be controlled remotely via controller PC.<br>
 * NOTE: once a task has been started with this sensor,
 * its parameters cannot be changed until the task is either
 * finished or killed.
 * @author Josh Noel
 */
public class MicrophoneSensor extends GenericDeviceSensor implements MediaRecorder.OnInfoListener {

	private static final String TAG = "MicrophoneSensor";
	
	private boolean taskCompleted, started;
	private MediaRecorder recorder;
	private File tempFile;
	private boolean saveToFile;
	
	/**
	 * Constructor
	 * @param activity - the activity context in which this
	 * ARC microphone sensor object was created.
	 */
	public MicrophoneSensor(Activity activity) {
		super(activity);
		
		this.taskCompleted = started = false;
		this.recorder = new MediaRecorder();
		try {
			this.tempFile = File.createTempFile("temp-audio-file", null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.saveToFile = false;
	}


	
	@Override
	public void releaseSensor() {
		taskCompleted = true;
		
		if(started){
			recorder.stop();
			started = false;
			sendResultAudio();
		}
		
		recorder.release();
	}

	
	@Override
	public void killTask() {
		taskCompleted = true;
		
		if(started){
			recorder.stop();
			sendResultAudio();
			started = false;
		}
	}

	
	@Override
	public void startNewTask(int taskID, int[] args) {
		setTaskID(taskID);
		taskCompleted = false;
		recorder.reset();
		
		updateSensorProperties();
		
		try {
			recorder.prepare();
			recorder.start();
			started = true;
		} catch (IllegalStateException e) {
			Log.e(TAG, "MediaRecorder illegal state: " + e.getMessage());
			started = false;
			taskCompleted = true;
		} catch (IOException e) {
			Log.e(TAG, "MediaRecorder IOException: " + e.getMessage());
			started = false;
			taskCompleted = true;
		}	
	}

	
	@Override
	public boolean taskCompleted() {
		return taskCompleted;
	}

	
	@Override
	public boolean saveResultDataToFile(){
		return saveToFile;
	}
	
	
	@Override
	public String getName() {
		return TAG;
	}

	
	@Override
	public void updateSensorProperties() {
		
		// parameters must be set in the following order
		if(recorder != null && !started){
			
			// TODO: set string formatted properties to
			// string value of their ints where needed
			// (exhangeFormat in SupportedFeatures)
			
			
			recorder.setAudioChannels(getIntProperty
					(AudioKeys.CHANNEL)); // 1 or 2
			recorder.setAudioSource(getIntProperty
					(AudioKeys.SOURCE));
			recorder.setOutputFormat(getIntProperty
					(AudioKeys.OUTPUT_FORMAT));
			recorder.setAudioEncoder(getIntProperty
					(AudioKeys.ENCODER));
			recorder.setAudioEncodingBitRate(getIntProperty
					(AudioKeys.ENCODING_BIT_RATE));
			recorder.setAudioSamplingRate(getIntProperty
					(AudioKeys.SAMPLING_RATE));
			recorder.setLocation(getIntProperty
					(AudioKeys.GPS_LATTITUDE), 
								 getIntProperty
					(AudioKeys.GPS_LONGITUDE));
			recorder.setMaxDuration(getIntProperty
					(AudioKeys.MAX_DURATION));
			recorder.setMaxFileSize(getIntProperty
					(AudioKeys.MAX_FILE_SIZE));
			
			setupDataTransfer();
			recorder.setOnInfoListener(this);
		}
	}


	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
		   what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
			if(saveToFile){
				sendResultAudio();
				sendTaskComplete();
				killTask();
			}
		}
	}
	
	
	/**
	 * helper method for setting audio properties.
	 */
	private void setupDataTransfer(){
		FileDescriptor descriptor = null;
		if(getConnection().isConnected()){
			FileOutputStream stream = (FileOutputStream) getConnection().getOutputStream();
			try {
				descriptor = stream.getFD();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(descriptor != null && descriptor.valid()){
			recorder.setOutputFile(descriptor);
			saveToFile = false;
		} else {
			recorder.setOutputFile(tempFile.getAbsolutePath());
			saveToFile = true;
		}
	}
	
	
	/**
	 * helper method for sending audio result data
	 */
	private void sendResultAudio(){
		if(tempFile != null && saveToFile){
			if(getConnection().isConnected()){
				ResponsePacket packet = new ResponsePacket(getTaskID(),
						Constants.DataTypes.MICROPHONE, getBytes(tempFile));
				ResponsePacket.sendResponse(packet, getConnection());
			} else {
				// possibly save to SD later
				//saveDataToSD(getBytes(tempFile), Long.toString(System.currentTimeMillis()), ".tmp");
			}
		}
	}
	
	
	/**
	 * Converts the given file into its direct byte representation.
	 * @param file - the file to convert to a byte array
	 * @return the byte array representation of the given file,
	 * or null on error
	 */
	public static byte[] getBytes(File file){
		byte[] bytes = null;
		
		try{
			int length = (int) file.length();
			 if(length > 0){
				 bytes = new byte[length];
				 new FileInputStream(file).read(bytes, 0, length);
			 }
		} catch(IOException e){
			bytes = null;
		}
		
		return bytes;
	}

}
