package com.i2r.androidremotecontroller.sensors;

import ARC.Constants;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.i2r.androidremotecontroller.supported_features.FormatExchanger;
import com.i2r.androidremotecontroller.supported_features.MicrophoneFeatureSet;


/**
 * This class models an android microphone sensor that
 * can be controlled remotely via controller PC.<br>
 * NOTE: once a task has been started with this sensor,
 * its parameters cannot be changed until the task is either
 * finished or killed.
 * @author Josh Noel
 */
public class MicrophoneSensor extends GenericDeviceSensor {

	private static final String TAG = "MicrophoneSensor";
	
	private AudioRecord audio;
	private RecordThread recorder;
	private boolean taskCompleted, recording;
	
	/**
	 * Constructor<br>
	 * creates a new blank audio sensor. The properties of this sensor can be changed
	 * multiple times before its task is started, but once its task is started
	 * its parameters must not be changed.
	 * @param activity - the activity context in which this ARC microphone
	 * sensor object was created.
	 */
	public MicrophoneSensor(Activity activity) {
		super(activity);
		this.taskCompleted = recording = false;
		this.audio = null;
		this.recorder = null;
		createNewDuration("duration");
	}


	
	@Override
	public void releaseSensor() {
		taskCompleted = true;
		recording = false;
		
		if(audio != null && audio.getState() 
				== AudioRecord.STATE_INITIALIZED){
			
			if(audio.getRecordingState() 
					== AudioRecord.RECORDSTATE_RECORDING){
				audio.stop();
			}
			
			audio.release();
			audio = null;
		} 
	}

	
	@Override
	public void killTask() {
		releaseSensor();
	}

	
	@Override
	public void startNewTask(int taskID, int[] args) {
		setTaskID(taskID);
		taskCompleted = false;
		createNewAudioRecorder();
		
		if(audio != null && audio.getState() 
				== AudioRecord.STATE_INITIALIZED){
			
			try {
				
				if(args != null){
					recorder = new RecordThread();
					recording = true;
					audio.startRecording();
					recorder.start();
					getDuration(0).setMax(args[0]).start();
				} else {
					sendTaskErroredOut("invalid microphone parameters");
					killTask();
				}

			} catch (IllegalStateException e) {
				Log.e(TAG, "MediaRecorder illegal state: " + e.getMessage());
				killTask();
			}
		} else {
			Log.e(TAG, "audio recorder is null after update, aborting task " + taskID);
			killTask();
		}
	}
	

	
	@Override
	public boolean taskCompleted() {
		return taskCompleted;
	}

	
	@Override
	public String getName() {
		return TAG;
	}

	
	@Override
	public void updateSensorProperties(int taskID) {
		
		int result = FormatExchanger.exchange(
				MicrophoneFeatureSet.ENCODING, getProperty(MicrophoneFeatureSet.ENCODING));
				
		if(result != Constants.Args.ARG_NONE){
			Log.d(TAG, "modifying encoding");
			modify(MicrophoneFeatureSet.ENCODING, result);
			
		} 
		
		result = FormatExchanger.exchange(
				MicrophoneFeatureSet.CHANNEL, getProperty(MicrophoneFeatureSet.CHANNEL));
		
		if(result != Constants.Args.ARG_NONE){
			Log.d(TAG, "modifying channel");
			modify(MicrophoneFeatureSet.CHANNEL, result);
			
		} 
		
		result = FormatExchanger.exchange(
				MicrophoneFeatureSet.SOURCE, getProperty(MicrophoneFeatureSet.SOURCE));
		
		if(result != Constants.Args.ARG_NONE){
			Log.d(TAG, "modifying source");
			modify(MicrophoneFeatureSet.SOURCE, result);
		}
	}

	

	/**
	 * Create a new AudioRecorder based on
	 * the parameters in {@link #getProperties()}
	 * This should only be called once per task.
	 */
	private void createNewAudioRecorder(){
		try{
			
			// 44100 supported by all devices
			int sampleRate = getIntProperty(
					MicrophoneFeatureSet.SAMPLING_RATE, 44100);
			int audioSource = getIntProperty(
					MicrophoneFeatureSet.SOURCE, AudioSource.DEFAULT);
			int channel = getIntProperty(
					MicrophoneFeatureSet.CHANNEL, AudioFormat.CHANNEL_IN_MONO);
			int audioFormat = getIntProperty(
					MicrophoneFeatureSet.ENCODING, AudioFormat.ENCODING_PCM_16BIT);
			
			int bufferSizeInBytes = AudioRecord.getMinBufferSize(
					sampleRate, channel, audioFormat) * 3;
			
			audio = new AudioRecord(audioSource, sampleRate,
						channel, audioFormat, bufferSizeInBytes);
			 
		} catch (IllegalArgumentException e){
			
			audio = null;
			Log.e(TAG, 
					"failed to create audio object due to IllegalArgumentException: "
							+ e.getMessage());
			
			sendTaskErroredOut(e.getMessage());
		}
	}
	
	
	
	/**
	 * Copy pasta, the best kind of pasta.
	 * Iterates through all the possible combinations
	 * to find one that will work on the current device.
	 */
	public static AudioRecord findAudioRecord() {
		
		int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
		short[] formats = new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT};
		short[] channels = new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};
		
	    for (int rate : mSampleRates) {
	        for (short audioFormat : formats) {
	            for (short channelConfig : channels) {
	            	
	                try {
	                	
	                    Log.d(TAG, "Attempting rate " + rate + "Hz, bits: "
	                    		+ audioFormat + ", channel: " + channelConfig);
	                    int bufferSize = AudioRecord
	                    	.getMinBufferSize(rate, channelConfig, audioFormat);

	                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
	                    	
	                        // check if we can instantiate and have a success
	                        AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT,
	                        		rate, channelConfig, audioFormat, bufferSize);

	                        if (recorder.getState() == AudioRecord.STATE_INITIALIZED){
	                            return recorder;
	                        }
	                    }
	                    
	                } catch (Exception e) {
	                    Log.e(TAG, rate + "Exception, keep trying.", e);
	                }
	            }
	        }
	    }
	    return null;
	}
	
	
	
	
	/**
	 * Query for the duration state of this microphone sensor
	 * @return true if the duration property was not set by the controller or if
	 * it was set and the current passed time is less than the set duration; false
	 * if the current passed time is greater than the set duration.
	 */
	private boolean validDuration(){
		return !getDuration(0).maxReached();
	}
	
	
	/**
	 * This class models a worker thread that
	 * reads data from this class's {@link AudioRecord}
	 * object and sends the results wherever
	 * the controller has specified them to be sent.
	 * @author Josh Noel
	 */
	private class RecordThread extends Thread {
		
		private byte[] buffer;
		private static final int BUFFER_SIZE = 1024;
		
		public RecordThread(){
			this.buffer = new byte[BUFFER_SIZE];
		}
		
		@Override
		public void run(){
			while(!taskCompleted && recording && validDuration()){
				
				// read more data from audio recorder
				int result = audio.read(buffer, 0, BUFFER_SIZE);
				
				// if there is still data to read
				if(result > 0){
					
					// if controller specified to save this
					// data to internal storage
					if(saveResultDataToFile()){
						saveDataToSD(buffer, 
								Long.toString(System.currentTimeMillis()), ".wav");
						
						// the controller did not specify to save
						// to internal storage
					} else {
						
						// if this sensor currently has a valid connection
						if(getConnection().isConnected()){
							
							sendData(Constants.DataTypes.AUDIO, buffer);
							
							// if there is no valid connection,
							// but the controller specified to keep recording
						} else if(continueOnConnectionLost()){
							saveDataToSD(buffer, 
									Long.toString(System.currentTimeMillis()), ".wav");
						} else {
							killTask();
						}
					}
				}
			}
			
			// if loop ended because duration limit
			// was reached, shutdown task and send task complete
			if(!validDuration()){
				sendTaskComplete();
				killTask();
			}
		}
		
		
	}// end of RecordThread class

}
