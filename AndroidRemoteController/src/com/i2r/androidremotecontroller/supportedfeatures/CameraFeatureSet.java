package com.i2r.androidremotecontroller.supportedfeatures;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ARC.Constants;
import android.graphics.ImageFormat;
import android.hardware.Camera;

/**
 * This class models an android {@link Camera}
 * object's feature set that will be presented
 * to the controller PC.
 * 
 * @author Josh Noel
 * @see {@link FeatureSet}
 * @see {@link Feature}
 */
public class CameraFeatureSet extends FeatureSet {

	
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
	
	
	/**
	 * Constructor<br>
	 * @param camera - the camera object to get
	 * supported features from.
	 * @see {@link CameraFeatureSet}
	 */
	public CameraFeatureSet(Camera camera){
		if(camera != null){
			
			
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
			
			
			int min = params.getMinExposureCompensation();
			int max = params.getMaxExposureCompensation();
			if(min != max){
				addRange(EXPOSURE_COMPENSATION,
						String.valueOf(params.getExposureCompensation()), 
						Constants.DataTypes.INTEGER, 
						String.valueOf(min), String.valueOf(max));
			}
			
			
			addRange(JPEG_QUALITY, String.valueOf(params.getJpegQuality()), 
					Constants.DataTypes.INTEGER, MIN_QUALITY, MAX_QUALITY);
			
			
			// ***********************************|
			// ------------- SETS ----------------|
			// ***********************************|
			
			// FLASH MODES
			List<String> flash = params.getSupportedFlashModes();
			if (flash != null) {
				addSet(FLASH, params.getFlashMode(),
						Constants.DataTypes.STRING, flash);
			} 
		
			
			// FOCUS MODES
			List<String> focus = params.getSupportedFocusModes();
			if (focus != null) {
				addSet(FOCUS, params.getFocusMode(),
						Constants.DataTypes.STRING, focus);
			} 
			
			
			// WHITE BALANCES
			List<String> whiteBalance = params.getSupportedWhiteBalance();
			if (whiteBalance != null) {
				addSet(WHITE_BALANCE, params.getWhiteBalance(),
						Constants.DataTypes.STRING,whiteBalance);
			} 
			
			
			// SCENE MODES	
			List<String> scenes = params.getSupportedSceneModes();
			if(scenes != null){
				addSet(SCENES, params.getSceneMode(),
						Constants.DataTypes.STRING,scenes);
			}
		
			
			// IMAGE FORMATS
			List<Integer> pictureFormats = params.getSupportedPictureFormats();
			if (pictureFormats != null) {
				ArrayList<String> stringFormats = new ArrayList<String>();
				Iterator<Integer> iter = pictureFormats.iterator();
				FormatExchanger e = FormatExchanger.getExchanger(CameraFeatureSet.PICTURE_FORMAT);
				
				while(iter.hasNext()){
					stringFormats.add(e.get(iter.next().intValue()));
				}
				
				addSet(PICTURE_FORMAT, e.get(params.getPictureFormat()),
						Constants.DataTypes.STRING, stringFormats);
			}
			
			
			List<Integer> previewFormats = params.getSupportedPictureFormats();
			if (previewFormats != null) {
				ArrayList<String> stringFormats = new ArrayList<String>();
				Iterator<Integer> iter = previewFormats.iterator();
				FormatExchanger e = FormatExchanger.getExchanger(CameraFeatureSet.PICTURE_FORMAT);
				
				while(iter.hasNext()){
					stringFormats.add(e.get(iter.next().intValue()));
				}
				
				addSet(PREVIEW_FORMAT, e.get(params.getPreviewFormat()),
						Constants.DataTypes.STRING, stringFormats);
			}
			
			
			
			List<String> antibanding = params.getSupportedAntibanding();
			if(antibanding != null){
				addSet(ANTIBANDING, params.getAntibanding(),
						Constants.DataTypes.STRING, antibanding);
			}
			
			
			
			List<String> colors = params.getSupportedColorEffects();
			if(colors != null){
				addSet(EFFECT, params.getColorEffect(),
						Constants.DataTypes.STRING, colors);
			}
			
			
			if(params.isZoomSupported()){
				List<Integer> zooms = params.getZoomRatios();
				addSet(CameraFeatureSet.ZOOM,
						String.valueOf(params.getZoom()), 
						Constants.DataTypes.INTEGER, zooms);
			}
			
			
			addSet(ROTATION, params.get(CameraFeatureSet.ROTATION), 
					Constants.DataTypes.INTEGER, CameraFeatureSet.ROTATIONS);
			
			
			
			List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
			addSet(CameraFeatureSet.PICTURE_SIZE, params.get(CameraFeatureSet.PICTURE_SIZE),
					Constants.DataTypes.STRING, encodeSizes(pictureSizes));

			
			
			List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
			addSet(PREVIEW_SIZE, params.get(CameraFeatureSet.PREVIEW_SIZE),
					Constants.DataTypes.STRING, encodeSizes(previewSizes));
		
			
			
			List<Camera.Size> thumbnailSizes = params.getSupportedJpegThumbnailSizes();
			addSet(JPEG_THUMBNAIL_SIZE, params.get(CameraFeatureSet.JPEG_THUMBNAIL_SIZE),
					Constants.DataTypes.STRING, encodeSizes(thumbnailSizes));

			
			
			List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
			addSet(PREVIEW_FPS_RANGE, params.get(CameraFeatureSet.PREVIEW_FPS_RANGE),
					Constants.DataTypes.STRING, encodeFpsRanges(fpsRanges));
			

			
			
			// ***********************************|
			// ----------- SWITCHES --------------|
			// ***********************************|
			
			
			// AUTO EXPOSURE LOCK
			if(params.isAutoExposureLockSupported()){
				addSwitch(CameraFeatureSet.AUTO_EXPOSURE_LOCK, 
						String.valueOf(params.getAutoExposureLock()));
			}
			
			
			// AUTO WHITE BALANCE LOCK
			if(params.isAutoWhiteBalanceLockSupported()){
				addSwitch(CameraFeatureSet.AUTO_WHITE_BALANCE_LOCK,
						String.valueOf(params.getAutoWhiteBalanceLock()));
			}
			
			
			// ********************************|
			// --------- PROPERTIES -----------|
			// ********************************|
			
			addProperty(FOCAL_LENGTH, 
					String.valueOf(params.getFocalLength()),
					Constants.DataTypes.DOUBLE);
			
			
			addProperty(HORIZONTAL_VIEW_ANGLE, 
					String.valueOf(params.getHorizontalViewAngle()),
					Constants.DataTypes.DOUBLE);
			
			
			addProperty(VERTICAL_VIEW_ANGLE, 
					String.valueOf(params.getVerticalViewAngle()),
					Constants.DataTypes.DOUBLE);
			
			
			addProperty(EXPOSURE_COMPENSATION_STEP, 
					String.valueOf(params.getExposureCompensationStep()),
					Constants.DataTypes.DOUBLE);
			
			
			addProperty(PREFERRED_PREVIEW_SIZE_FOR_VIDEO, 
					String.valueOf(params.getPreferredPreviewSizeForVideo()),
					Constants.DataTypes.STRING);
			
			
			addProperty(FOCUS_DISTANCES, params.get(FOCUS_DISTANCES),
					Constants.DataTypes.STRING);
			
			
			if(params.getMaxNumMeteringAreas() > 0){
				addProperty(METERING_AREAS, params.get(METERING_AREAS),
						Constants.DataTypes.STRING);
			}
			
			
			if(params.getMaxNumFocusAreas() > 0){
				addProperty(FOCUS_AREAS, params.get(FOCUS_AREAS),
						Constants.DataTypes.STRING);
			}
			
			
			
			// ******************************************|
			// ----------- SINGLE VARIANTS --------------|
			// ******************************************|
			
			
			addSingleVariant(GPS_ALTITUDE, Constants.DataTypes.DOUBLE);
			
			
			addSingleVariant(GPS_LATITUDE, Constants.DataTypes.DOUBLE);
			
			
			addSingleVariant(GPS_LONGITUDE, Constants.DataTypes.DOUBLE);
			
			
			addSingleVariant(GPS_PROCESSING_METHOD, Constants.DataTypes.STRING);
			
			
			addSingleVariant(GPS_TIMESTAMP, Constants.DataTypes.INTEGER);
			
			
		}
	}
	
	
	
	
	/**
	 * Helper method for camera parameters.
	 * @param sizes - the sizes to encode
	 * @return the encoded string representation of the
	 * given list of sizes.
	 */
	private static String[] encodeSizes(List<Camera.Size> sizes){
		String[] result = new String[sizes.size()];
		int i = 0;
		for(Camera.Size size : sizes){
			result[i++] = size.width + "x" + size.height;
		}
		return result;
	}
	
	
	/**
	 * Helper method for camera parameters.
	 * @param ranges - the ranges to encode
	 * @return a string encoded representation of the given
	 * list of FPS ranges.
	 */
	private static String[] encodeFpsRanges(List<int[]> ranges){
		String[] result = new String[ranges.size()];
		int i = 0;
		for(int[] range : ranges){
			result[i++] = range[0] + "," + range[1];
		}
		return result;
	}
	
	
} // end of CameraFeatureSet class
