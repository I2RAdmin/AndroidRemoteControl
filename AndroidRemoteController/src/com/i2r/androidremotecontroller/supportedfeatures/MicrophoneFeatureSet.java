package com.i2r.androidremotecontroller.supportedfeatures;


import ARC.Constants;
import android.media.AudioFormat;
import android.media.MediaRecorder.AudioSource;


/**
 * This class models a feature set for this android
 * device's microphone. As of now, microphone features
 * are constant, so only one instance of this class
 * is needed.
 * @author Josh Noel
 * @see {@link FeatureSet}
 * @see {@link Feature}
 * @see {@link #getInstance()}
 * @see {@link #FEATURES}
 */
public class MicrophoneFeatureSet extends FeatureSet {

	
	public static final String ENCODING = "audio-encoding";
	public static final String SOURCE = "audio-source";
	public static final String CHANNEL = "audio-channel";
	public static final String SAMPLING_RATE = "audio-sampling-rate";
	public static final String RECORD_DURATION = "audio-recording-duration";
	
	// ENCODING OPTIONS
	
	
	/**
	 * Constants obtained from {@link AudioFormat}
	 */
	public static final int[] INTEGER_ENCODINGS = {
		AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT
	};
	
	
	/**
	 * Constant values:<br>
	 * "encoding-pcm-16-bit", "encoding-pcm-8-bit"
	 */
	public static final String[] STRING_ENCODINGS = {
		"encoding-pcm-16-bit", "encoding-pcm-8-bit"
	};
	
	// RECORDING TYPE OPTIONS
	
	/**
	 * constants obtained from {@link AudioSource}
	 */
	public static final int[] INTEGER_SOURCES = {
		AudioSource.CAMCORDER, AudioSource.MIC,
		AudioSource.VOICE_CALL, AudioSource.VOICE_COMMUNICATION,
		AudioSource.VOICE_DOWNLINK, AudioSource.VOICE_UPLINK,
		AudioSource.VOICE_RECOGNITION, AudioSource.DEFAULT
	};
	
	/**
	 * Constant values:<br>
	 * "camcorder", "mic", "voice-call", "voice-communication",
	 * "voice-downlink", "voice-uplink", "voice-recognition", "default"
	 */
	public static final String[] STRING_SOURCES = {
		"camcorder", "mic", "voice-call", "voice-communication",
		"voice-downlink", "voice-uplink", "voice-recognition", "default"
	};
	
	
	/**
	 * Constants obtained from {@link AudioFormat}
	 */
	public static final int[] INTEGER_CHANNELS = {
		AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO
	};
	
	/**
	 * Constant values:<br>
	 * "mono", "stereo"
	 */
	public static final String[] STRING_CHANNELS = {
		"mono", "stereo"
	};
	
	/**
	 * Constant values:<br>
	 * "44100", "22050", "16000", "11025"
	 */
	public static final String[] BIT_RATE_SAMPLES = {
		"44100", "22050", "16000", "11025"
	};
	
	
	private static final MicrophoneFeatureSet set = new MicrophoneFeatureSet();
	
	/**
	 * Static footprint of the current microphone features.
	 * If this object is only requested for encoded features,
	 * use this. Otherwise use {@link #getInstance}
	 */
	public static final byte[] FEATURES = set.encode();
	
	/**
	 * Constructor<br>
	 * @see {@link MicrophoneFeatureSet}
	 */
	private MicrophoneFeatureSet(){
		
		addSet(ENCODING, Constants.Args.ARG_STRING_NONE, 
				Constants.DataTypes.STRING, STRING_ENCODINGS);
			
		
		addSet(SOURCE, Constants.Args.ARG_STRING_NONE, 
				Constants.DataTypes.STRING, STRING_SOURCES);
		
		
		addSet(CHANNEL, MicrophoneFeatureSet.STRING_CHANNELS[0], 
				Constants.DataTypes.STRING, STRING_CHANNELS);
		
		
		addSet(SAMPLING_RATE, MicrophoneFeatureSet.BIT_RATE_SAMPLES[0],
				Constants.DataTypes.INTEGER, BIT_RATE_SAMPLES);
		
		
		addRange(RECORD_DURATION, Constants.Args.ARG_STRING_NONE,
				Constants.DataTypes.INTEGER, String.valueOf(0), String.valueOf(Integer.MAX_VALUE));
	}
	
	
	/**
	 * Query for the singleton instance of this class.
	 * @return a {@link MicrophoneFeatureSet} singleton instance
	 */
	public static MicrophoneFeatureSet getInstance(){
		return set;
	}
	
} // end of MicrophoneFeatureSet class
