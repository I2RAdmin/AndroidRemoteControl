package com.i2r.androidremotecontroller.sensors;

import ARC.Constants;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.i2r.androidremotecontroller.ResponsePacket;
import com.i2r.androidremotecontroller.SupportedFeatures;
import com.i2r.androidremotecontroller.SupportedFeatures.AudioKeys;


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
	private long startTime;
	
	/**
	 * Constructor
	 * @param activity - the activity context in which this
	 * ARC microphone sensor object was created.
	 */
	public MicrophoneSensor(Activity activity) {
		super(activity);
		this.taskCompleted = recording = false;
		this.audio = null;
		this.recorder = null;
		this.startTime = 0;
	}


	
	@Override
	public void releaseSensor() {
		taskCompleted = true;
		recording = false;
		
		if(audio != null && audio.getState() == AudioRecord.STATE_INITIALIZED){
			if(audio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
				audio.stop();
			}
			audio.release();
			audio = null;
			sendTaskComplete();
		} else {
			sendTaskErroredOut();
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
		
		modify(AudioKeys.RECORD_DURATION, String.valueOf(args[0]));
		
		if(audio == null){
			createNewAudioRecorder();
		}
		
		if(audio != null){
			try {
				recorder = new RecordThread();
				
				new Thread(new Runnable() { public void run() {
				while(audio.getState() == AudioRecord.STATE_UNINITIALIZED){
					try{
						Thread.sleep(1000);
					} catch (InterruptedException e){
						// do nothing
					}
				}
				
				startRecording();
				
				}}).start();
				

			} catch (IllegalStateException e) {
				Log.e(TAG, "MediaRecorder illegal state: " + e.getMessage());
				killTask();
			}
		} else {
			Log.e(TAG, "audio recorder is null after update, aborting task " + taskID);
		}
	}
	
	
	
	private void startRecording(){
		if(!recording){
			startTime = System.currentTimeMillis();
			recording = true;
			audio.startRecording();
			recorder.start();
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
	public void updateSensorProperties() {
		
		String result = getProperty(AudioKeys.ENCODING);
				
		
		if(result != null){
			int temp = SupportedFeatures.exchangeAudioEncodingFormat(result);
			modify(AudioKeys.ENCODING, temp);
			
		} 
		
		result = getProperty(AudioKeys.CHANNEL);
		if(result != null){
			int temp = SupportedFeatures.exchangeAudioChannel(result);
			modify(AudioKeys.CHANNEL, temp);
			
		} 
		
		result = getProperty(AudioKeys.SOURCE);
		if(result != null){
			int temp = SupportedFeatures.exchangeAudioSourceFormat(result);
			modify(AudioKeys.SOURCE, temp);
			
		}
	}
	
	
	
	
	/**
	 * Copy pasta, the best kind of pasta.
	 */
	private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
	public AudioRecord findAudioRecord() {
	    for (int rate : mSampleRates) {
	        for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
	            for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
	                try {
	                    Log.d(TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
	                            + channelConfig);
	                    int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

	                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
	                        // check if we can instantiate and have a success
	                        AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

	                        if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
	                            return recorder;
	                    }
	                } catch (Exception e) {
	                    Log.e(TAG, rate + "Exception, keep trying.",e);
	                }
	            }
	        }
	    }
	    return null;
	}
	
	
	

	/**
	 * TODO: comment
	 */
	private void createNewAudioRecorder(){
		try{
			
			// 44100 supported by all devices
			int sampleRate = getIntProperty(AudioKeys.SAMPLING_RATE, 44100);
			int audioSource = getIntProperty(AudioKeys.SOURCE, AudioSource.DEFAULT);
			int channel = getIntProperty(AudioKeys.CHANNEL, AudioFormat.CHANNEL_IN_MONO);
			int audioFormat = getIntProperty(AudioKeys.ENCODING, AudioFormat.ENCODING_PCM_16BIT);
			int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat) * 2;
			audio = new AudioRecord(audioSource, sampleRate,
						channel, audioFormat, bufferSizeInBytes);
		} catch (IllegalArgumentException e){
			audio = null;
			Log.e(TAG, "failed to create audio object due to IllegalArgumentException: " + e.getMessage());
			sendTaskErroredOut(e.getMessage());
		}
	}
	
	
	private boolean validDuration(){
		int duration = getIntProperty(AudioKeys.RECORD_DURATION);
		return duration == Constants.Args.ARG_NONE || System.currentTimeMillis() - startTime < duration;
	}
	
	
	/**
	 * TODO: comment
	 * @author jnoel
	 */
	private class RecordThread extends Thread {
		
		private byte[] buffer;
		private static final int BUFFER_SIZE = 1024;
		
		public RecordThread(){
			this.buffer = new byte[BUFFER_SIZE];
		}
		
		
		public void run(){
			while(recording && validDuration()){
				int result = audio.read(buffer, 0, BUFFER_SIZE);
				if(result > 0){
					if(saveResultDataToFile()){
						saveDataToSD(buffer, Long.toString(System.currentTimeMillis()), ".wav");
					} else {
						if(getConnection().isConnected()){
							ResponsePacket packet = new ResponsePacket(getTaskID(),
									Constants.DataTypes.AUDIO, buffer);
							ResponsePacket.sendResponse(packet, getConnection());
						} else if(continueOnConnectionLost()){
							saveDataToSD(buffer, Long.toString(System.currentTimeMillis()), ".wav");
						} else {
							killTask();
						}
					}
				}
			}
			
			if(!validDuration()){
				killTask();
			}
		}
		
		
	}// end of RecordThread class

}
