package com.i2r.androidremotecontroller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ARC.Constants;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;

/**
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
 */
public final class SupportedFeatures {


	/**
	 * Key constants for the camera sensor
	 * @author Josh Noel
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
		
		public static final String SIZE_DELIMITER = ",";
		public static final String AREA_DELIMITER = "),(";

	}
	
	
	/**
	 * Key constants for the microphone sensor
	 * @author Josh Noel
	 */
	public static final class AudioKeys {
		
		public static final String ENCODER = "audio-encoding";
		public static final String SOURCE = "audio-source";
		public static final String CHANNEL = "audio-channel";
		public static final String OUTPUT_FORMAT = "audio-output-format";
		public static final String ENCODING_BIT_RATE = "audio-encoding-bit-rate";
		public static final String SAMPLING_RATE = "audio-sampling-rate";
		public static final String GPS_LONGITUDE = "audio-gps-longitude";
		public static final String GPS_LATTITUDE = "audio-gps-lattitude";
		public static final String MAX_DURATION = "audio-max-duration";
		public static final String MAX_FILE_SIZE = "audio-max-file-size";
		
		
		// ENCODING OPTIONS
		
		public static final int[] INTEGER_ENCODINGS = {
			AudioEncoder.AAC, AudioEncoder.AAC_ELD,
			AudioEncoder.AMR_NB, AudioEncoder.AMR_WB,
			AudioEncoder.HE_AAC, AudioEncoder.DEFAULT 
		};
		
		public static final String[] STRING_ENCODINGS = {
			"aac", "aac-eld", "amr-narrow-band",
			"amr-wide-band", "high-efficiency-aac", "default"
		};
		
		
		// RECORDING TYPE OPTIONS
		
		public static final int[] INTEGER_SOURCES = {
			AudioSource.CAMCORDER, AudioSource.MIC,
			AudioSource.VOICE_CALL, AudioSource.VOICE_COMMUNICATION,
			AudioSource.VOICE_DOWNLINK, AudioSource.VOICE_UPLINK,
			AudioSource.VOICE_RECOGNITION, AudioSource.DEFAULT
		};
		
		
		public static final String[] STRING_SOURCES = {
			"camcorder", "mic", "voice-call", "voice-communication",
			"voice-downlink", "voice-uplink", "voice-recognition", "default"
		};
		
		
		// OUTPUT FORMAT OPTIONS
		
		public static final int[] INTEGER_OUTPUT_FORMATS = {
			OutputFormat.AAC_ADTS, OutputFormat.AMR_NB,
			OutputFormat.AMR_WB, OutputFormat.MPEG_4,
			OutputFormat.THREE_GPP, OutputFormat.DEFAULT
		};
		
		
		public static final String[] STRING_OUTPUT_FORMATS = {
			"aac-adts", "amr-narrow-band", "amr-wide-band", 
			"mpeg-4", "3-gpp", "default"
		};
	}

	
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
				
				while(iter.hasNext()){
					stringFormats.add(exchangeFormat(iter.next().intValue(), 
							CameraKeys.INTEGER_IMAGE_FORMATS, 
							CameraKeys.STRING_IMAGE_FORMATS));
				}
				
				builder.append(encodeCollection(CameraKeys.PICTURE_FORMAT,
						exchangeImageFormat(params.getPictureFormat()),
						Constants.DataTypes.STRING, stringFormats));
			}
			
			
			List<Integer> previewFormats = params.getSupportedPictureFormats();
			if (previewFormats != null) {
				ArrayList<String> stringFormats = new ArrayList<String>();
				Iterator<Integer> iter = previewFormats.iterator();
				
				while(iter.hasNext()){
					stringFormats.add(exchangeImageFormat(iter.next()));
				}
				
				builder.append(encodeCollection(CameraKeys.PREVIEW_FORMAT,
						exchangeImageFormat(params.getPreviewFormat()),
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
			
			
			
			if(params.isZoomSupported()){
				List<Integer> zooms = params.getZoomRatios();
				builder.append(encodeCollection(CameraKeys.ZOOM,
						String.valueOf(params.getZoom()), 
						Constants.DataTypes.INTEGER, zooms));
			}
			
			
			ArrayList<Integer> rotations = new ArrayList<Integer>(4);
			rotations.add(0);
			rotations.add(90);
			rotations.add(180);
			rotations.add(270);
			builder.append(encodeCollection(CameraKeys.ROTATION,
					params.get(CameraKeys.ROTATION), 
					Constants.DataTypes.INTEGER, rotations));
			
			
			
			List<Size> sizes = params.getSupportedPictureSizes();
			builder.append(encodeCameraSpecialObject(params, 
					CameraKeys.PICTURE_SIZE, sizes.size(), 
					CameraKeys.SIZE_DELIMITER));

			
			
			List<Size> thumbnail = params.getSupportedJpegThumbnailSizes();
			builder.append(encodeCameraSpecialObject(params, 
					CameraKeys.JPEG_THUMBNAIL_SIZE, thumbnail.size(),
					CameraKeys.SIZE_DELIMITER));
			
			
			List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
			builder.append(encodeCameraSpecialObject(params, 
					CameraKeys.PREVIEW_FPS_RANGE, fpsRanges.size(), 
					CameraKeys.AREA_DELIMITER));
			
			
			
			List<Size> previewSizes = params.getSupportedPreviewSizes();
			builder.append(encodeCameraSpecialObject(params, 
					CameraKeys.PREVIEW_SIZE, previewSizes.size(), 
					CameraKeys.SIZE_DELIMITER));
			
			
			
			params.getSupportedVideoSizes(); // set
			
			
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
			
			builder.append(encodeProperty(CameraKeys.FOCAL_LENGTH,
					String.valueOf(params.getFocalLength())));
			
			builder.append(encodeProperty(CameraKeys.HORIZONTAL_VIEW_ANGLE,
					String.valueOf(params.getHorizontalViewAngle())));
			
			builder.append(encodeProperty(CameraKeys.VERTICAL_VIEW_ANGLE,
					String.valueOf(params.getVerticalViewAngle())));
			
			builder.append(encodeProperty(CameraKeys.EXPOSURE_COMPENSATION_STEP,
					String.valueOf(params.getExposureCompensationStep())));
			
			builder.append(encodeProperty(CameraKeys.PREFERRED_PREVIEW_SIZE_FOR_VIDEO,
					String.valueOf(params.getPreferredPreviewSizeForVideo())));
			
			builder.append(encodeProperty(CameraKeys.FOCUS_DISTANCES,
					params.get(CameraKeys.FOCUS_DISTANCES)));
			
			if(params.getMaxNumMeteringAreas() > 0){
				builder.append(encodeProperty(CameraKeys.METERING_AREAS, 
						params.get(CameraKeys.METERING_AREAS)));
			}
			
			if(params.getMaxNumFocusAreas() > 0){
				builder.append(encodeProperty(CameraKeys.FOCUS_AREAS, 
						params.get(CameraKeys.FOCUS_AREAS)));
			}
			
			
			// ******************************************|
			// ----------- SINGLE VARIANTS --------------|
			// ******************************************|
			
			
			builder.append(encodeSingle(CameraKeys.GPS_ALTITUDE, 
					params.get(CameraKeys.GPS_ALTITUDE), Constants.DataTypes.DOUBLE));
			builder.append(encodeSingle(CameraKeys.GPS_LATITUDE, 
					params.get(CameraKeys.GPS_LATITUDE), Constants.DataTypes.DOUBLE));
			builder.append(encodeSingle(CameraKeys.GPS_LONGITUDE, 
					params.get(CameraKeys.GPS_LONGITUDE), Constants.DataTypes.DOUBLE));
			builder.append(encodeSingle(CameraKeys.GPS_PROCESSING_METHOD, 
					params.get(CameraKeys.GPS_ALTITUDE), Constants.DataTypes.STRING));
			builder.append(encodeSingle(CameraKeys.GPS_TIMESTAMP, 
					params.get(CameraKeys.GPS_TIMESTAMP), Constants.DataTypes.INTEGER));
			
			
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
		
		// SETS
		
		builder.append(encodeSize2Set(AudioKeys.CHANNEL, Constants.Args.ARG_STRING_NONE, 
				Constants.DataTypes.INTEGER, Constants.DataTypes.SET, 
				String.valueOf(1), String.valueOf(2)));
		
		builder.append(encodeCollection(AudioKeys.ENCODER, Constants.Args.ARG_STRING_NONE, 
				Constants.DataTypes.STRING, AudioKeys.STRING_ENCODINGS.length,
				encodeCollection(AudioKeys.STRING_ENCODINGS)));
		
		builder.append(encodeCollection(AudioKeys.SOURCE, Constants.Args.ARG_STRING_NONE, 
				Constants.DataTypes.STRING, AudioKeys.STRING_SOURCES.length,
				encodeCollection(AudioKeys.STRING_SOURCES)));
		
		builder.append(encodeCollection(AudioKeys.OUTPUT_FORMAT, Constants.Args.ARG_STRING_NONE, 
				Constants.DataTypes.STRING, AudioKeys.STRING_OUTPUT_FORMATS.length,
				encodeCollection(AudioKeys.STRING_OUTPUT_FORMATS)));
		
		
		// SINGLE INT VARIANTS
		
		builder.append(encodeSingle(AudioKeys.ENCODING_BIT_RATE, Constants.Args.ARG_STRING_NONE,
				Constants.DataTypes.INTEGER));
		
		builder.append(encodeSingle(AudioKeys.GPS_LATTITUDE, Constants.Args.ARG_STRING_NONE,
				Constants.DataTypes.INTEGER));
		
		builder.append(encodeSingle(AudioKeys.GPS_LONGITUDE, Constants.Args.ARG_STRING_NONE,
				Constants.DataTypes.INTEGER));
		
		
		builder.append(encodeSingle(AudioKeys.MAX_DURATION, Constants.Args.ARG_STRING_NONE,
				Constants.DataTypes.INTEGER));
		
		
		builder.append(encodeSingle(AudioKeys.MAX_FILE_SIZE, Constants.Args.ARG_STRING_NONE,
				Constants.DataTypes.INTEGER));
		
		
		builder.append(encodeSingle(AudioKeys.SAMPLING_RATE, Constants.Args.ARG_STRING_NONE,
				Constants.DataTypes.INTEGER));
		
		
		return builder.toString().getBytes();
	}
	
	
	
	
	// TODO: add other supported feature calls here
	
	//|********************************************************************|
	//|********************************************************************|
	//
	//|------------ STATIC HELPER METHODS FOR FEATURE ENCODING ------------|
	//
	//|********************************************************************|
	//|********************************************************************|
	
	/**
	 * Encodes the given property with the given value to a string which
	 * will be parsable by the controller PC. This is only used to inform
	 * the controller of fixed parameters, and the given property cannot
	 * be changed.
	 * @return the String representation of the given property and value
	 * to be sent to the controller PC
	 */
	public static String encodeProperty(String propertyName, String value){
		return encodeSingle(propertyName, value, Constants.DataTypes.CONST);
	}
	
	
	
	public static String encodeSingularVariant(String property, String value){
		return encodeSingle(property, value, Constants.DataTypes.ANY);
	}
	
	
	
	public static String encodeSingle(String name, String value, int dataType){
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
	 */
	public static String encodeRange(String rangeName, String currentValue, int dataType, String min, String max){
		return encodeSize2Set(rangeName, currentValue, dataType, Constants.DataTypes.RANGE, min, max);
	}
	
	
	
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
	 * it of a list (or range) of available choices.
	 * Collections sent to the controller PC have the following format:<br>
	 * 
	 * <ol>
	 * <li>Name of collection 
	 * (how controller should refer to it in communicating back)</li>
	 * <li>type of data that this application expects from the controller
	 * when referencing this collection</li>
	 * <li>the limiting type of this collection - range or set of values</li>
	 * <li>the size of the collection (element count)</li>
	 * <li>the actual collection elements, in the order that they are definied
	 * in the collection</li>
	 * <ol><br>
	 * 
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
	 * to be sent to the remote PC
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
	
	
	
	
	private static String encodeCollection(String[] collection){
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < collection.length; i++){
			builder.append(String.valueOf(collection[i]));
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
		}
		
		return builder.toString();
	}
	
	
	
	//|-------------------- EXCHANGE HELPER METHODS -----------------------------|
	
	
	public static int exchangeAudioOutputFormat(String format){
		return exchangeFormat(format, AudioKeys.STRING_OUTPUT_FORMATS, AudioKeys.INTEGER_OUTPUT_FORMATS);
	}
	
	public static String exchangeAudioOutputFormat(int format){
		return exchangeFormat(format, AudioKeys.INTEGER_OUTPUT_FORMATS, AudioKeys.STRING_OUTPUT_FORMATS);
	}
	
	public static int exchangeAudioSourceFormat(String format){
		return exchangeFormat(format, AudioKeys.STRING_SOURCES, AudioKeys.INTEGER_SOURCES);
	}
	
	public static String exchangeAudioSourceFormat(int format){
		return exchangeFormat(format, AudioKeys.INTEGER_SOURCES, AudioKeys.STRING_SOURCES);
	}
	
	public static int exchangeAudioEncodingFormat(String encoding){
		return exchangeFormat(encoding, AudioKeys.STRING_ENCODINGS, AudioKeys.INTEGER_ENCODINGS);
	}
	
	public static String exchangeAudioEncodingFormat(int encoding){
		return exchangeFormat(encoding, AudioKeys.INTEGER_ENCODINGS, AudioKeys.STRING_ENCODINGS);
	}
	
	public static int echangeImageFormat(String format){
		return exchangeFormat(format, CameraKeys.STRING_IMAGE_FORMATS, CameraKeys.INTEGER_IMAGE_FORMATS);
	}
	
	public static String exchangeImageFormat(int format){
		return exchangeFormat(format, CameraKeys.INTEGER_IMAGE_FORMATS, CameraKeys.STRING_IMAGE_FORMATS);
	}
	
	
	
	/**
	 * Finds the integer representation of the string image format given
	 * @param format - the format to get an integer representation of
	 * @param stringFormats - the string representation of the intFormats
	 * @param intFormats - the int array to search for the given format in.
	 * Once the index of the string format is found in the string array,
	 * it can be directly mapped to the int array
	 * @return the integer representation of the given string format,
	 * or {@link ARG_NONE} if the format was not found.
	 * @see {@link ImageFormat}
	 * @see {@link Constants#Args}
	 */
	public static int exchangeFormat(String format, String[] stringFormats, int[] intFormats){
		int result = Constants.Args.ARG_NONE;
		if (stringFormats != null && intFormats != null
				&& stringFormats.length == intFormats.length) {
			for (int i = 0; i < stringFormats.length; i++) {
				if (stringFormats[i].equals(format)) {
					result = intFormats[i];
					break;
				}
			}
		}
		return result;
	}
	
	
	
	/**
	 * Finds the string representation of the int image format given
	 * @param format - the format to get a string representation of
	 * @param intFormats - the int array to search for the given format in
	 * @param stringFormats - the string representation of the intFormats. Once
	 * the index of the int format is found in the int array, it can be directly
	 * mapped to the string array
	 * @return the string representation of the given int format,
	 * or {@link ARG_STRING_NONE} if the format was not found.
	 * @see {@link ImageFormat}
	 * @see {@link Constants#Args}
	 */
	public static String exchangeFormat(int format, int[] intFormats, String[] stringFormats){
		String result = Constants.Args.ARG_STRING_NONE;
		if (stringFormats != null && intFormats != null
				&& stringFormats.length == intFormats.length) {
			for (int i = 0; i < intFormats.length; i++) {
				if (intFormats[i] == format) {
					result = stringFormats[i];
					break;
				}
			}
		}
		return result;
	}
	
	
	/**
	 * Specialty method for the camera supported features
	 * @param p - the parameters to obtain info from
	 * @param key - the key of the info in the parameters to deal with
	 * @param size - the size of the collection given by the key
	 * @param delimiterToReplace - the delimiter to replace in the returned
	 * value to make the result string match this application's remote
	 * communication specifications.
	 * @return An encoded string representing the collection obtained from
	 * the given parameters with the given key
	 */
	private static String encodeCameraSpecialObject(Camera.Parameters p, String key, int size, String delimiterToReplace){
		String collection = null;
		String temp = p.get(key + CameraKeys.SUPPORTED_VALUES_SUFFIX)
				.replaceAll(delimiterToReplace, STRING_DELIMITER);
		
		if(temp.charAt(0) == '(' && temp.charAt(temp.length() - 1) == ')'){
			collection = temp.substring(1, temp.length() - 1);
		} else {
			collection = temp;
		}
		
		return encodeCollection(key, p.get(key), Constants.DataTypes.STRING, size, collection);
	}
	
	
	
}