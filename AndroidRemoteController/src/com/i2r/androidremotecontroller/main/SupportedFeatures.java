package com.i2r.androidremotecontroller.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ARC.Constants;
import ARC.Constants.Args;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioFormat;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

/******************************************************************************
 * This class defines a static way to obtain all the information that the
 * controller needs to know about this android device, in order to properly
 * manipulate its sensors. General rule for sending an individual feature for a
 * set of features:<br>
 * <ol>
 * <li>Name of feature(how controller should refer to it in communicating
 * back)</li>
 * <li>current value for this feature</li>
 * <li>type of data that this application expects from the controller when
 * referencing this feature</li>
 * <li>the limiting type of this feature - range or set;
 * switches and properties can be considered sets of 2 and 1 respectively</li>
 * <li>the size of the available values this feature can be set to</li>
 * <li>the actual feature values, delimited by the {@link PACKET_DELIMITER}
 * constant defined in {@link Constants#Delimiters}.</li>
 * <ol><br><br>
 * 
 * All features of a particular sensor will be "feature-delimited" with the
 * constant {@link PACKET_LIST_DELIMITER}, also defined in {@link Constants#Delimiters}.
 * The static methods which define sets of features for each particular sensor
 * in this class can be considered the "data" of a {@link ResponsePacket},
 * with the header being the task ID that came with the feature request,
 * and the data type being a feature defined in {@link Constants#DataTypes} 
 * (such as {@link CAMERA_SENSOR}, {@link MIC_SENSOR} etc...)
 * 
 * @author Josh Noel
 ******************************************************************************
 */
public final class SupportedFeatures {

	private static final String TAG = "SupportedFeatures";

	/***************************************
	 * Key constants for the camera sensor
	 * @author Josh Noel
	 ***************************************
	 */
	public static final class CameraKeys {
		
		
		// conversion arrays - these are used so the formats make sense
		// to the controller PC as well as the user
		public static final String[] STRING_IMAGE_FORMATS = {
			"jpeg", "nv21", "nv16", "rgb_565", "yuy2", "yv12"
		};
		
		
		public static final int[] INTEGER_IMAGE_FORMATS = {
			ImageFormat.JPEG, ImageFormat.NV21, ImageFormat.NV16, 
			ImageFormat.RGB_565, ImageFormat.YUY2, ImageFormat.YV12
		};
		
		
		public static final String[] ROTATIONS = {
			"0", "90", "180", "270"
		};
		
		// Camera HashMap KEY VALUES -------------------------|
		// these will be the keys that the controller uses
		// to manipulate sensors throughout the remote control
		// of this device
		
		// used when sending the supported features of the camera.
		// these constants will be used to set and get the resulting values
		// in the camera's parameter HashMap
		
		// sets
		public static final String FLASH = "flash-mode";
		public static final String FOCUS = "focus-mode";
		public static final String WHITE_BALANCE = "whitebalance";
		public static final String SCENES = "scene-mode";
		public static final String PICTURE_FORMAT = "picture-format";
		public static final String PICTURE_SIZE = "picture-size";
		
		public static final String FOCUS_AREAS = "focus-areas";
		public static final String METERING_AREAS = "metering-areas";
		
	    public static final String PREVIEW_SIZE = "preview-size";
	    public static final String PREVIEW_FORMAT = "preview-format";
	    public static final String PREVIEW_FRAME_RATE = "preview-frame-rate";
	    public static final String PREVIEW_FPS_RANGE = "preview-fps-range";
	    
	    public static final String JPEG_THUMBNAIL_SIZE = "jpeg-thumbnail-size";
	    public static final String JPEG_THUMBNAIL_QUALITY = "jpeg-thumbnail-quality";
	    
	    public static final String ROTATION = "rotation";

	    public static final String EFFECT = "effect";
	    public static final String ANTIBANDING = "antibanding";
	    
	    public static final String ZOOM = "zoom";
	    public static final String ZOOM_RATIOS = "zoom-ratios";
	    public static final String FOCUS_DISTANCES = "focus-distances";
	    public static final String VIDEO_SIZE = "video-size";
	   
	    public static final String MAX_NUM_DETECTED_FACES_HW = "max-num-detected-faces-hw";
	    public static final String MAX_NUM_DETECTED_FACES_SW = "max-num-detected-faces-sw";
	    public static final String RECORDING_HINT = "recording-hint";
	    public static final String VIDEO_STABILIZATION = "video-stabilization";
		
		
		// ranges
		public static final String EXPOSURE_COMPENSATION = "exposure-compensation";
		public static final String JPEG_QUALITY = "jpeg-quality";
		
		// switches
		public static final String AUTO_EXPOSURE_LOCK = "auto-exposure-lock";
		public static final String AUTO_WHITE_BALANCE_LOCK = "auto-white-balance-lock";
		
		// properties
		public static final String FOCAL_LENGTH = "focal-length";
		public static final String PREFERRED_PREVIEW_SIZE_FOR_VIDEO = "preferred-preview-size-for-video";
		public static final String EXPOSURE_COMPENSATION_STEP = "exposure-compensation-step";
	    public static final String HORIZONTAL_VIEW_ANGLE = "horizontal-view-angle";
	    public static final String VERTICAL_VIEW_ANGLE = "vertical-view-angle";
		
	    
	    // flexible singles
	    public static final String GPS_LATITUDE = "gps-latitude";
	    public static final String GPS_LONGITUDE = "gps-longitude";
	    public static final String GPS_ALTITUDE = "gps-altitude";
	    public static final String GPS_TIMESTAMP = "gps-timestamp";
	    public static final String GPS_PROCESSING_METHOD = "gps-processing-method";
	    
		// custom for this application
		public static final String PICTURE_AMOUNT = "picture-max-count";
		public static final String FREQUENCY = "frequency-interval";
		public static final String DURATION = "duration-interval";
		
		
		public static final String MAX_QUALITY = "100";
		public static final String MIN_QUALITY = "0";
		public static final String MIN_ZOOM = "0";
		
		public static final String SUPPORTED_VALUES_SUFFIX = "-values";
		
		public static final String EMPTY_AREA = "(0,0,0,0,0)";
		public static final String EMPTY_SIZE = "(0,0)";

	}
	
	
	/**********************************************
	 * Key constants for the microphone sensor
	 * @author Josh Noel
	 **********************************************
	 */
	public static final class AudioKeys {
		
		public static final String ENCODING = "audio-encoding";
		public static final String SOURCE = "audio-source";
		public static final String CHANNEL = "audio-channel";
		public static final String SAMPLING_RATE = "audio-sampling-rate";
		public static final String RECORD_DURATION = "recording-duration";
		
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
		 * 	"camcorder", "mic", "voice-call", "voice-communication",
			"voice-downlink", "voice-uplink", "voice-recognition", "default"
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
		
	}
	
	
	/*********************************************
	 * Key constants for the Environment Sensors
	 * @author Josh Noel
	 *********************************************
	 */
	public static final class EnvironmentKeys {
		
		public static final String DURATION = "recording-duration";
		public static final String UPDATE_SPEED = "update-speed";
		
		public static final int[] INTEGER_UPDATE_RATES = {
			SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_NORMAL,
			SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI
		};
		
		public static final String[] STRING_UPDATE_RATES = {
			"update-fastest", "update-normal", "update-slow", "update-slowest"
		};
	}
	
	
	/**************************************
	 * Key constants for location sensors
	 * @author Josh Noel
	 **************************************
	 */
	public static final class LocationKeys {
		
		public static final String PROXIMITY_ALERT = "proximity-alert";
		public static final String PROXIMITY_ALERT_LATITUDE = "proximity-alert-latitude";
		public static final String PROXIMITY_ALERT_LONGITUDE = "proximity-alert-longitude";
		public static final String PROXIMITY_ALERT_RADIUS = "proximity-alert-radius";
		public static final String PROXIMITY_ALERT_EXPIRATION = "proximity-alert-expiration";
		
		
	}
	
	
	
	
	/******************************************************
	 * Static mapping of int arrays to string arrays with
	 * their name referral being the key.<br>
	 * (Totally stole this idea from Johnathan Pagnutti)<br><br>
	 * 
	 * Current available keys to get exhangers with:<br>
	 * {@link CameraKeys#PICTURE_FORMAT}
	 * {@link AudioKeys#ENCODING}
	 * {@link AudioKeys#SOURCE}
	 * {@link AudioKeys#CHANNEL}
	 * {@link EnvironmentKeys#UPDATE_SPEED}
	 * @author Josh Noel
	 * @see {@link FormatExchanger#getExchanger(String)}
	 ******************************************************
	 */
	public enum FormatExchanger {
		
		// ENUM SET
		
		// CAMERA SENSOR
		CM_IMAGE(CameraKeys.PICTURE_FORMAT,
				CameraKeys.INTEGER_IMAGE_FORMATS,
				CameraKeys.STRING_IMAGE_FORMATS),
		
		// AUDIO SENSOR
		AU_ENCODING(AudioKeys.ENCODING, 
				AudioKeys.INTEGER_ENCODINGS,
				AudioKeys.STRING_ENCODINGS),
				
		AU_SOURCE(AudioKeys.SOURCE,
				AudioKeys.INTEGER_SOURCES,
				AudioKeys.STRING_SOURCES),
				
		AU_CHANNEL(AudioKeys.CHANNEL,
				AudioKeys.INTEGER_CHANNELS,
				AudioKeys.STRING_CHANNELS),
		
		// ENVIRONMENT SENSORS
		ENV_UPDATE_SPEED(EnvironmentKeys.UPDATE_SPEED,
				EnvironmentKeys.INTEGER_UPDATE_RATES,
				EnvironmentKeys.STRING_UPDATE_RATES);

		
		   // the map to store all these enums in
	       private static final HashMap<String, FormatExchanger>
	       		arrayMappings = new HashMap<String, FormatExchanger>();
	       
	       // create map at compile time so it can be statically accessed
	       static{
	           for(FormatExchanger fe : EnumSet.allOf(FormatExchanger.class)){
	        	   arrayMappings.put(fe.getName(), fe);
	           }
	       }
	       
	       private String key;
	       private int[] intValues;
	       private String[] stringValues;
	       
	       
	    /**
		 * Constructor<br>
		 * creates a new version of this enum that is gets mapped by its key
		 * parameter into this enum definition's HashMap of enums.
		 * 
		 * The map is used to obtain this enum, and the {@link #get(int)} and
		 * {@link #get(String)} methods are used to exchange values.
		 * 
		 * @param key - the key to map this enum by
		 * @param intValues - the values defining the given key in integer format
		 * @param stringValues - the values defining the given key in string format
		 * @throws IllegalArgumentException if any pair of arrays do not map correctly
		 */
	       private FormatExchanger(String key, int[] intValues, String[] stringValues)
	       													throws IllegalArgumentException {
	    	   
	    	   if(intValues.length != stringValues.length){
	    		   StringBuilder b = new StringBuilder();
	    		   b.append("ERROR - lengths do not match: ");
	    		   b.append("key: ");
	    		   b.append(key);
	    		   b.append(" int array length: ");
	    		   b.append(intValues.length);
	    		   b.append(" string array length: ");
	    		   b.append(stringValues.length);
	    		   
	    		   Log.e(TAG, b.toString());
	    		   throw new IllegalArgumentException(
	    		"int array length and string array length must be equal to map values correctly");
	    	   }
	    	   
	    	   this.key = key;
	    	   this.intValues = intValues;
	    	   this.stringValues = stringValues;

	       }
	       
	       
	       /**
	        * Query for the int representation of the given
	        * string key
	        * @param key - the string key to echange with its
	        * int counterpart.
	        * @return the int representation of the string key
	        * given if it exists in this enum, or {@link Args#ARG_NONE}
	        * if the key was not found.
	        * @see {@link Constants#Args}
	        */
	       public int get(String key){
	    	   int index = getIndex(key);
	    	   int result = index == Constants.Args.ARG_NONE ? 
	    			   Constants.Args.ARG_NONE : intValues[index];
	               return result;
	       }
	       
	       
	       /**
	        * Query for the string representation of the
	        * given int key
	        * @param key - the int key to echange with
	        * its string counterpart.
	        * @return the string representation of the given
	        * int key if it exists in this enum, or null
	        * if the key was not found.
	        */
	       public String get(int key){
	    	   int index = getIndex(key);
	    	   String result = index == Constants.Args.ARG_NONE ? 
	    			   null : stringValues[index];
	               return result;
	       }
	       
	       
	       /**
	        * Query for this enum's key identifier
	        * @return this enum's string name that
	        * was given to it upon creation.
	        */
	       public String getName(){
	    	   return key;
	       }
	       
	       
	       /**
	        * Query for an enum echanger that deals with an android
	        * device's supported features
	        * @param key - the name of the enum exchanger
	        * @return the enum found at the specified key, or null
	        * if the key was not found in the enum mapping.
	        */
	       public static FormatExchanger getExchanger(String key){
	               return arrayMappings.get(key);
	       }
	       
	       
	       // finding indices from the int array
	       private int getIndex(int key){
	    	   int i;
	    	   for(i = 0; i < intValues.length && intValues[i] != key; i++);
	    	   if(i >= intValues.length){
	    		   i = Constants.Args.ARG_NONE;
	    	   }
	    	   return i;
	       }
	       
	       
	       // finding indices from the string array
	       private int getIndex(String key){
	    	   int i = Constants.Args.ARG_NONE;
	    	   if(key != null){
		    	   for(i = 0; i < stringValues.length 
		    			   && !stringValues[i].equals(key); i++);
		    	   if(i >= stringValues.length){
		    		   i = Constants.Args.ARG_NONE;
		    	   }
	    	   }
	    	   return i;
	       }
	       
	} // end of FormatExchanges enum
	
	
	
	// *********************************************************************************
	// |---------------- COMMON CONSTANTS TO ALL SUPPORTED FEATURES -------------------|
	// *********************************************************************************
	
	public static final String KEY_SAVE_TO_SD = "save-to-sd";
	public static final String KEY_CONTINUE_ON_CONNECTION_LOST = "continue-on-connection-lost";
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";

	public static final String STRING_DELIMITER = 
			Character.toString(Constants.Delimiters.PACKET_DELIMITER);

	
	
	/**
	 * No constructor needed since features can be obtained
	 * independently from the android device's state
	 */
	private SupportedFeatures(){}
	
	
	
	
	/**
	 * Retrieves all the necessary Camera information that the
	 * controller needs to know from the given camera in order
	 * to manipulate its parameters correctly and effectively. 
	 * @param camera - the camera to retrieve parameters from
	 * @return the encoded representation of the given camera's
	 * parameters which can be interpreted by the controller, or null
	 * if there was an error while getting these features
	 */
	public static byte[] getCameraFeatures(Camera camera){
		
		byte[] result = null;
		
		// no camera = no parameters, so check camera's validity first
		if (camera != null) {
			
			StringBuilder builder = new StringBuilder(1000);
			
			// get all relevant parameters which can be manipulated by
			// the controller PC
			Camera.Parameters params = camera.getParameters();
			
			
			// all of the following are null checks for the queried
			// parameters just obtained. If their value is null,
			// that parameter is not supported for this camera and
			// can be ignored when sending these features to the controller.
			
			
			// ********************************|
			// ----------- RANGES -------------|
			// ********************************|
			
			// EXPOSURE COMPENSATIONS
			int min = params.getMinExposureCompensation();
			int max = params.getMaxExposureCompensation();
			if(min != max){
				builder.append(encodeRange(CameraKeys.EXPOSURE_COMPENSATION,
						String.valueOf(params.getExposureCompensation()), 
						Constants.DataTypes.INTEGER, 
						String.valueOf(min), String.valueOf(max)));
			}
			
			
			builder.append(encodeRange(CameraKeys.JPEG_QUALITY, 
					String.valueOf(params.getJpegQuality()), 
					Constants.DataTypes.INTEGER,
					CameraKeys.MIN_QUALITY, CameraKeys.MAX_QUALITY));
			
			
			// ***********************************|
			// ------------- SETS ----------------|
			// ***********************************|
			
			// FLASH MODES
			List<String> flash = params.getSupportedFlashModes();
			if (flash != null) {
				builder.append(encodeCollection(CameraKeys.FLASH,
						params.getFlashMode(),
						Constants.DataTypes.STRING,flash));
			} 
		
			
			// FOCUS MODES
			List<String> focus = params.getSupportedFocusModes();
			if (focus != null) {
				builder.append(encodeCollection(CameraKeys.FOCUS,
						params.getFocusMode(),
						Constants.DataTypes.STRING, focus));
			} 
			
			
			// WHITE BALANCES
			List<String> whiteBalance = params.getSupportedWhiteBalance();
			if (whiteBalance != null) {
				builder.append(encodeCollection(CameraKeys.WHITE_BALANCE,
						params.getWhiteBalance(),
						Constants.DataTypes.STRING,whiteBalance));
			} 
			
			
			// SCENE MODES	
			List<String> scenes = params.getSupportedSceneModes();
			if(scenes != null){
				builder.append(encodeCollection(CameraKeys.SCENES,
						params.getSceneMode(),
						Constants.DataTypes.STRING,scenes));
			}
		
			
			// IMAGE FORMATS
			List<Integer> pictureFormats = params.getSupportedPictureFormats();
			if (pictureFormats != null) {
				ArrayList<String> stringFormats = new ArrayList<String>();
				Iterator<Integer> iter = pictureFormats.iterator();
				FormatExchanger e = FormatExchanger.getExchanger(CameraKeys.PICTURE_FORMAT);
				
				while(iter.hasNext()){
					stringFormats.add(e.get(iter.next().intValue()));
				}
				
				builder.append(encodeCollection(CameraKeys.PICTURE_FORMAT,
						e.get(params.getPictureFormat()),
						Constants.DataTypes.STRING, stringFormats));
			}
			
			
			List<Integer> previewFormats = params.getSupportedPictureFormats();
			if (previewFormats != null) {
				ArrayList<String> stringFormats = new ArrayList<String>();
				Iterator<Integer> iter = previewFormats.iterator();
				FormatExchanger e = FormatExchanger.getExchanger(CameraKeys.PICTURE_FORMAT);
				
				while(iter.hasNext()){
					stringFormats.add(e.get(iter.next().intValue()));
				}
				
				builder.append(encodeCollection(CameraKeys.PREVIEW_FORMAT,
						e.get(params.getPreviewFormat()),
						Constants.DataTypes.STRING, stringFormats));
			}
			
			
			
			List<String> antibanding = params.getSupportedAntibanding();
			if(antibanding != null){
				builder.append(encodeCollection(CameraKeys.ANTIBANDING,
						params.getAntibanding(),
						Constants.DataTypes.STRING, antibanding));
			}
			
			
			
			List<String> colors = params.getSupportedColorEffects();
			if(colors != null){
				builder.append(encodeCollection(CameraKeys.EFFECT,
						params.getColorEffect(),
						Constants.DataTypes.STRING, colors));
			}
			
			
			// TODO: check parameters object for how it stores areas
			
			
			if(params.isZoomSupported()){
				List<Integer> zooms = params.getZoomRatios();
				builder.append(encodeCollection(CameraKeys.ZOOM,
						String.valueOf(params.getZoom()), 
						Constants.DataTypes.INTEGER, zooms));
			}
			
			
			builder.append(encodeCollection(CameraKeys.ROTATION,
					params.get(CameraKeys.ROTATION), 
					Constants.DataTypes.INTEGER, CameraKeys.ROTATIONS.length, 
					encodeStringArray(CameraKeys.ROTATIONS)));
			
			
			
			List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
			builder.append(encodeCollection(CameraKeys.PICTURE_SIZE,
					params.get(CameraKeys.PICTURE_SIZE), Constants.DataTypes.STRING,
					pictureSizes.size(), encodeSizes(pictureSizes)));

			
			
			List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
			builder.append(encodeCollection(CameraKeys.PREVIEW_SIZE,
					params.get(CameraKeys.PREVIEW_SIZE), Constants.DataTypes.STRING,
					previewSizes.size(), encodeSizes(previewSizes)));
		
			
			
			List<Camera.Size> thumbnailSizes = params.getSupportedJpegThumbnailSizes();
			builder.append(encodeCollection(CameraKeys.JPEG_THUMBNAIL_SIZE,
					params.get(CameraKeys.JPEG_THUMBNAIL_SIZE), Constants.DataTypes.STRING,
					thumbnailSizes.size(), encodeSizes(thumbnailSizes)));

			
			
			List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
			builder.append(encodeCollection(CameraKeys.PREVIEW_FPS_RANGE,
					params.get(CameraKeys.PREVIEW_FPS_RANGE), Constants.DataTypes.STRING,
					fpsRanges.size(), encodeFpsRanges(fpsRanges)));
			

			
			
			// ***********************************|
			// ----------- SWITCHES --------------|
			// ***********************************|
			
			
			// AUTO EXPOSURE LOCK
			if(params.isAutoExposureLockSupported()){
				builder.append(encodeSwitch(CameraKeys.AUTO_EXPOSURE_LOCK, 
						String.valueOf(params.getAutoExposureLock())));
			}
			
			
			// AUTO WHITE BALANCE LOCK
			if(params.isAutoWhiteBalanceLockSupported()){
				builder.append(encodeSwitch(CameraKeys.AUTO_WHITE_BALANCE_LOCK,
						String.valueOf(params.getAutoWhiteBalanceLock())));
			}
			
			
			// ********************************|
			// --------- PROPERTIES -----------|
			// ********************************|
			
			builder.append(encodeSingle(CameraKeys.FOCAL_LENGTH,
					String.valueOf(params.getFocalLength()),
					Constants.DataTypes.DOUBLE));
			
			
			builder.append(encodeSingle(CameraKeys.HORIZONTAL_VIEW_ANGLE,
					String.valueOf(params.getHorizontalViewAngle()),
					Constants.DataTypes.DOUBLE));
			
			
			builder.append(encodeSingle(CameraKeys.VERTICAL_VIEW_ANGLE,
					String.valueOf(params.getVerticalViewAngle()),
					Constants.DataTypes.DOUBLE));
			
			
			builder.append(encodeSingle(CameraKeys.EXPOSURE_COMPENSATION_STEP,
					String.valueOf(params.getExposureCompensationStep()),
					Constants.DataTypes.DOUBLE));
			
			
			builder.append(encodeSingle(CameraKeys.PREFERRED_PREVIEW_SIZE_FOR_VIDEO,
					String.valueOf(params.getPreferredPreviewSizeForVideo()),
					Constants.DataTypes.STRING));
			
			
			builder.append(encodeSingle(CameraKeys.FOCUS_DISTANCES,
					params.get(CameraKeys.FOCUS_DISTANCES), Constants.DataTypes.STRING));
			
			
			if(params.getMaxNumMeteringAreas() > 0){
				builder.append(encodeSingle(CameraKeys.METERING_AREAS, 
						params.get(CameraKeys.METERING_AREAS), Constants.DataTypes.STRING));
			}
			
			
			if(params.getMaxNumFocusAreas() > 0){
				builder.append(encodeSingle(CameraKeys.FOCUS_AREAS, 
						params.get(CameraKeys.FOCUS_AREAS), Constants.DataTypes.STRING));
			}
			
			
			
			// ******************************************|
			// ----------- SINGLE VARIANTS --------------|
			// ******************************************|
			
			
			builder.append(encodeSingle(CameraKeys.GPS_ALTITUDE, 
					params.get(CameraKeys.GPS_ALTITUDE), Constants.DataTypes.ANY,
					Constants.DataTypes.DOUBLE));
			
			
			builder.append(encodeSingle(CameraKeys.GPS_LATITUDE, 
					params.get(CameraKeys.GPS_LATITUDE), Constants.DataTypes.ANY,
					Constants.DataTypes.DOUBLE));
			
			
			builder.append(encodeSingle(CameraKeys.GPS_LONGITUDE, 
					params.get(CameraKeys.GPS_LONGITUDE), Constants.DataTypes.ANY,
					Constants.DataTypes.DOUBLE));
			
			
			builder.append(encodeSingle(CameraKeys.GPS_PROCESSING_METHOD, 
					params.get(CameraKeys.GPS_ALTITUDE), Constants.DataTypes.ANY,
					Constants.DataTypes.STRING));
			
			
			builder.append(encodeSingle(CameraKeys.GPS_TIMESTAMP, 
					params.get(CameraKeys.GPS_TIMESTAMP), Constants.DataTypes.ANY,
					Constants.DataTypes.INTEGER));
			
			
			builder.append(sdOptions());
			
			
			result = builder.toString().getBytes();
			
		}
		
		return result;
	}
	
	
	
	
	
	/**
	 * Query for the available features of the microphone
	 * for an android device. This method basically dumps
	 * the microphone API onto the connection stream.
	 * @return All the available parameters of an android
	 * microphone that can be changed through the android APIs,
	 * or null on error getting features
	 */
	public static byte[] getMicrophoneFeatures(){
		
		StringBuilder builder = new StringBuilder(1000);
	
	
		builder.append(encodeCollection(AudioKeys.ENCODING, Constants.Args.ARG_STRING_NONE, 
				Constants.DataTypes.STRING, AudioKeys.STRING_ENCODINGS.length,
				encodeStringArray(AudioKeys.STRING_ENCODINGS)));
			
		
		builder.append(encodeCollection(AudioKeys.SOURCE, Constants.Args.ARG_STRING_NONE, 
				Constants.DataTypes.STRING, AudioKeys.STRING_SOURCES.length,
				encodeStringArray(AudioKeys.STRING_SOURCES)));
		
		
		builder.append(encodeCollection(AudioKeys.CHANNEL, AudioKeys.STRING_CHANNELS[0], 
				Constants.DataTypes.STRING, AudioKeys.STRING_CHANNELS.length,
				encodeStringArray(AudioKeys.STRING_CHANNELS)));
		
		
		builder.append(encodeCollection(AudioKeys.SAMPLING_RATE, AudioKeys.BIT_RATE_SAMPLES[0],
				Constants.DataTypes.INTEGER, AudioKeys.BIT_RATE_SAMPLES.length,
				encodeStringArray(AudioKeys.BIT_RATE_SAMPLES)));
		
		
		builder.append(encodeRange(AudioKeys.RECORD_DURATION, Constants.Args.ARG_STRING_NONE,
				Constants.DataTypes.INTEGER, String.valueOf(0), String.valueOf(Integer.MAX_VALUE)));
		
		builder.append(sdOptions());
		
		
		return builder.toString().getBytes();
	}
	
	
	
	/**
	 * @param manager - the {@link SensorManager} to obtain a list of
	 * all this device's sensors from.
	 * @return an encoded byte array of all the supported environment
	 * sensors that this android device has to offer
	 * @see {@link Sensor}
	 */
	public static byte[] getEnvironmentSensorFeatures(SensorManager manager){
		byte[] result = null;
		
		if(manager != null){
			StringBuilder builder = new StringBuilder();
			List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
			
			if(sensors != null){
				for(int i = 0; i < sensors.size(); i++){
					builder.append(encodeCollection(sensors.get(i).getName(), 
							Constants.Args.ARG_STRING_NONE, Constants.DataTypes.STRING,
							EnvironmentKeys.STRING_UPDATE_RATES.length,
							encodeStringArray(EnvironmentKeys.STRING_UPDATE_RATES)));
				}
				
				
				result = builder.toString().getBytes();
			}
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Query for the supported features found in the given {@link LocationManager}
	 * @param manager - the manager to obtain supported features information from
	 * @return a listing of all LocationProviders (and their properties) that
	 * can be obtained from the given LocationManager.
	 * 
	 * TODO: finish this
	 */
	public static byte[] getLocationSupportedFeatures(LocationManager manager){
		byte[] result = null;
		
		if(manager != null){
			
			StringBuilder builder = new StringBuilder();
			
			
			List<String> providers = manager.getAllProviders();
			
			Iterator<String> iter = providers.iterator();
			
			while(iter.hasNext()){
				LocationProvider p = manager.getProvider(iter.next());
				p.getAccuracy();
				p.getName();
				p.getPowerRequirement();
				p.hasMonetaryCost();
			}
			
			
			builder.append(sdOptions());
			
			result = builder.toString().getBytes();
		}
		
		return result;
	}
	
	
	
	
	//|********************************************************************|
	//|********************************************************************|
	//
	//|------------ STATIC HELPER METHODS FOR FEATURE ENCODING ------------|
	//
	//|********************************************************************|
	//|********************************************************************|
	
	
	
	/**
	 * Returns available options for manipulating how a sensor stores or
	 * sends its data.
	 * @return a string encoded representation of how the controller
	 * can manipulate the way in which a sensor handles its data.
	 */
	public static String sdOptions(){
		StringBuilder builder = new StringBuilder();
		builder.append(encodeSwitch(KEY_SAVE_TO_SD, FALSE));
		builder.append(encodeSwitch(KEY_CONTINUE_ON_CONNECTION_LOST, FALSE));
		return builder.toString();
	}
	
	
	/**
	 * Convenience method, see {@link #encodeSingle(String, String, int, int)}
	 * for details.
	 * @param name - the name that the controller should use when referencing
	 * this set of 1.
	 * @param value - the value that this set of 1 is currently set to.
	 * dataType - the data type that this set of 1 expects back from
	 * the controller.
	 * @return a string encoded representation of a set of size 1 with the
	 * following parameters that follows the SupportedFeatures ordering rules.
	 */
	public static String encodeSingle(String name, String value, int dataType){
		return encodeSingle(name, value, Constants.DataTypes.SET, dataType);
	}
	
	
	
	/**
	 * Constructs the given parameters into a set of size 1, which follows the
	 * general ordering defined for this class.
	 * @param name - the name that the controller should use when referencing
	 * this set of 1.
	 * @param value - the value that this set of 1 is currently set to.
	 * @param limiter - the limiting type of this collection
	 * @param dataType - the data type that this set of 1 expects back from
	 * the controller.
	 * @return a string encoded representation of a set of size 1 with the
	 * following parameters that follows the SupportedFeatures ordering rules.
	 * @see {@link SupportedFeatures}
	 * @see {@link Constants#DataTypes}
	 */
	public static String encodeSingle(String name, String value, int limiter, int dataType){
		StringBuilder builder = new StringBuilder();
		
		builder.append(name);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(value);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(dataType);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(Constants.DataTypes.SET);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(1);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(value);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(Constants.Delimiters.PACKET_LIST_DELIMITER);
		
		return builder.toString();
	}
	
	
	
	/**
	 * Encodes a switch to be sent to the controller PC.
	 * Since a switch (or boolean parameter) is really just
	 * a list of size 2, it is treated as such when being sent
	 * to the controller PC. 
	 * @param switchName - the name for the controller PC to reference
	 * this parameter by when sending modification commands
	 * @return the encoded string representation of a switch
	 * with the given string ID
	 */
	public static String encodeSwitch(String switchName, String currentValue){
		return encodeSize2Set(switchName, currentValue, Constants.DataTypes.STRING,
				Constants.DataTypes.SET, TRUE, FALSE);
	}
	
	
	/**
	 * Encodes a value range to be sent to the controller PC.
	 * @param rangeName - the name for the controller to refer to
	 * when referencing this range.
	 * @param dataType - the data type that this application expects
	 * when the controller references this range by the given name
	 * @param min - the minimum value of this range (inclusive)
	 * @param max - the maximum value of this range (inclusive)
	 * @return an encoded String representation of this range to
	 * be sent to the controller for interpretation.
	 * @see {@link Constants#DataTypes}
	 */
	public static String encodeRange(String rangeName, String currentValue, int dataType, String min, String max){
		return encodeSize2Set(rangeName, currentValue, dataType, Constants.DataTypes.RANGE, min, max);
	}
	
	
	
	/**
	 * Encodes a set of size 2 to be sent to a controlling device.
	 * This encoding method follows the {@link SupportedFeatures}
	 * encoding rules.
	 * @param name - the name for the controller to use when referencing this
	 * set of size 2.
	 * @param currentValue - the current value that this parameter is set to.
	 * @param dataType - the data type that this application expects back from
	 * @param limiter - the limiting type of this collection (set, range, switch,
	 * property, etc..)
	 * @param first - the first element of this size 2 set
	 * @param second - the second element of this size 2 set
	 * @return an encoded string representation of a size 2 set of values to send
	 * to the remote controller.
	 * @see {@link Constants#DataTypes}
	 */
	private static String encodeSize2Set(String name, String currentValue, int dataType, 
								int limiter, String first, String second){
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(name);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(currentValue);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(String.valueOf(dataType));
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(String.valueOf(limiter));
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(2);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(first);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(second);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(Constants.Delimiters.PACKET_LIST_DELIMITER);
		
		return builder.toString();
	}
	
	
	
	
	/**
	 * Encodes a collection to be sent to the controller PC, to inform
	 * it of a list (or range) of available choices. This method follows
	 * the {@link SupportedFeatures} encoding rules.
	 * @param elements - the collection of elements to send to the controller PC
	 * @param delimiter - the delimiter to use for separating all information sent
	 * @param collectionName - the String id of the collection being sent
	 * @param dataType - the data type expected to come back when the controller
	 * makes a selection from this collection
	 * @param limiter - the type of collection (range or set)
	 * @return an encoded String representation of the given collection to send
	 * to the remote PC.
	 * @see {@link Constants#DataTypes}
	 * @see {@link Constants#PACKET_DELIMITER}
	 * @see {@link #encodeCollection(Collection)}
	 */
	public static String encodeCollection(String collectionName, 
				String currentValue, int dataType, Collection<?> collection){
		return encodeCollection(collectionName, currentValue, dataType, 
				collection.size(), encodeCollection(collection));
	}
	

	/**
	 * This defines the basic encoding algorithm for feature lists
	 * that will be sent to the remote controller to be parsed.
	 * the basic format is the same order and definition as the
	 * following parameters
	 * @param collectionName - the name by which the remote controller should
	 * refer to this collection when changing its parameters. (should be human readable
	 * and relevant to the wrapping sensor)
	 * @param currentValue - the current value that this parameter is set to
	 * @param dataType - the data type that this application expects back from the
	 * remote controller when changing this parameter
	 * @param size - the number of elements in this collection's feature set
	 * @param encodedItems - the already encoded feature set
	 * @return the encoded representation of the given collection, following the
	 * specified parameters that define it.
	 * @see {@link Constants#DataTypes}
	 */
	private static String encodeCollection(String collectionName, String currentValue,
												int dataType, int size, String encodedItems){
		StringBuilder builder = new StringBuilder();
		
		// name, current value, type, limiter, size, args
		builder.append(collectionName);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(currentValue);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(dataType);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(Constants.DataTypes.SET);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(size);
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(encodedItems);
		builder.append(Constants.Delimiters.PACKET_LIST_DELIMITER);
		
		return builder.toString();
	}
	
	
	/**
	 * only to be used for objects who's toString() method has
	 * been correctly implemented to represent that object
	 * @param collection - the collection of objects to encode
	 * to be sent to the remote controller
	 * @return the encoded String representation of this collection
	 */
	private static String encodeCollection(Collection<?> collection){
		StringBuilder builder = new StringBuilder();
		
		Iterator<?> iter = collection.iterator();
		
		while(iter.hasNext()){
			builder.append(String.valueOf(iter.next()));
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
		}
		
		return builder.toString();
	}
		
	
	
	/**
	 * Used to encode String array constants defined at
	 * the beginning of this class definition.
	 * @param collection - the collection to encode for
	 * sending to the remote controller
	 * @return the encoded String representation of the given
	 * string array which the remote controller can parse into
	 * meaningful information
	 */
	public static String encodeStringArray(String[] collection){
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < collection.length; i++){
			builder.append(String.valueOf(collection[i]));
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
		}
		
		return builder.toString();
	}
	
	
	
	/**
	 * Exchanges a string value for its integer representation from a specified
	 * map.<br>
	 * This is a convenience method that is short-hand for getting a
	 * {@link FormatExchanger} via {@link FormatExchanger#getExchanger(String)}
	 * and then calling {@link FormatExchanger#get(String)} on the
	 * FormatExchanger obtained.
	 * 
	 * @param mapKey - the mapkey to use in {@link FormatExchanger#getExchanger(String)}
	 * @param value - the value to get an int version of by calling 
	 * 		  {@link FormatExchanger#get(String)}
	 * @return the int representation of the string value given from the map
	 *         given, assuming the map contains the string value. If either
	 *         argument is null or the given map does not contain the given
	 *         value, this returns {@link ARG_NONE}.
	 * @see {@link FormatExchanger}
	 * @see {@link Constants#Args}
	 * @see Sorcery
	 */
	public static int exchange(String mapKey, String value){
		int result = Constants.Args.ARG_NONE;
		if(mapKey != null && value != null){
			FormatExchanger e = FormatExchanger.getExchanger(mapKey);
			if(e != null){
				result = e.get(value);
			} 
		} 
		return result;
	}
	
	
	
	/**
	 * Helper method for camera parameters.
	 * @param sizes - the sizes to encode
	 * @return the encoded string representation of the
	 * given list of sizes.
	 */
	private static String encodeSizes(List<Camera.Size> sizes){
		StringBuilder builder = new StringBuilder();
		for(Camera.Size s : sizes){
			builder.append(s.width);
			builder.append('x');
			builder.append(s.height);
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
		}
		return builder.toString();
	}
	
	
	/**
	 * Helper method for camera parameters.
	 * @param ranges - the ranges to encode
	 * @return a string encoded representation of the given
	 * list of FPS ranges.
	 */
	private static String encodeFpsRanges(List<int[]> ranges){
		StringBuilder builder = new StringBuilder();
		for(int[] range : ranges){
			builder.append(range[0]);
			builder.append(',');
			builder.append(range[1]);
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
		}
		return builder.toString();
	}
	
	
}
