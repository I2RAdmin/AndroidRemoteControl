package ARC;

/********************************************************************************|
 * This class models a pool of constants that both the android device being
 * controlled and the controlling PC can pull from, so that they can effectively
 * communicate.
 ********************************************************************************
 */
public final class Constants {

	
	/***************************************************************|
	 * Computer Command Structure:<br>
	 * TASK ID<br>
	 * COMMAND HEADER<br>
	 * [COMMAND ARGS]
	 * 
	 * NOTE: the header is only interpreted if the task ID
	 * given is unique among all currently running tasks,
	 * otherwise the header position will be treated as a parameter.
	 ***************************************************************
	 */
	public static final class Commands {

		public static final int TASK_ID_INDEX = 0;
		public static final int HEADER_INDEX = 1;
		public static final int PARAM_START_INDEX = 2;
		
		/**
		 * Kill command header - specifies that the following commands will
		 * involve stopping one or multiple procedures.
		 */
		public static final int KILL = 0;

		/**
		 * Kills any currently running applications on the android device,
		 * closes open sockets and terminates all android-2-PC communication.
		 */
		public static final int KILL_EVERYTHING = -1;

		/**
		 * Expected arguments following this command header:<br>
		 * int - maximum amount of pictures to be taken<br>
		 * long - frequency of captures in milliseconds (captures per second)
		 */
		public static final int PICTURE = 1;
		
		
		/**
		 * Modify a currently running task with new parameters
		 * Expected arguments:<br><br>
		 * taskID<br>
		 * MODIFY command<br>
		 * odd indexes after modify command are keys<br>
		 * even indexes after modify command are values
		 */
		public static final int MODIFY = 2;

	}// end Commands class


	
	/*********************************************************
	 * Argument constants used to decipher command packets.
	 ******************************************************** 
	 */
	public final static class Args {

		public static final int ARG_NO_CHANGE = -2;
		public static final int ARG_NONE = -1;
		public static final String SIZE_ZERO = "0";
		
		public static final int IMAGE_PARAMETER_SIZE = 7;
		public static final int IMAGE_CAMERA_PARAMETER_START_INDEX = 3;
		
		public static final int IMAGE_FREQUENCY_INDEX = 0;
		public static final int IMAGE_DURATION_INDEX = 1;
		public static final int IMAGE_MAX_COUNT_INDEX = 2;
		public static final int IMAGE_EXPOSURE_COMP_INDEX = 3;
		public static final int IMAGE_FORMAT_INDEX = 4;
		public static final int IMAGE_SIZE_WIDTH_INDEX = 5;
		public static final int IMAGE_SIZE_HEIGHT_INDEX = 6;
		

		public static final String CAMERA_SUPPORTED_FEATURE_LIST_FLASH = "camera_supported_feature_flash";
		public static final String CAMERA_SUPPORTED_FEATURE_LIST_FOCUS = "camera_supported_feature_focus";
		public static final String CAMERA_SUPPORTED_FEATURE_LIST_WHITE_BALANCE = "camera_supported_feature_white_balance";
		public static final String CAMERA_SUPPORTED_FEATURE_LIST_FORMAT = "camera_supported_feature_format";
		public static final String CAMERA_SUPPORTED_FEATURE_LIST_IMAGE_SIZE = "camera_supported_feature_size";
		public static final String CAMERA_SUPPORTED_FEATURE_LIST_SCENES = "camera_supported_feature_scene";
		
		// formats are given as "string type-integer constant to return"
		// when requesting the android device to set this parameter,
		// only the numeric value after "-" should be used
		public static final String CAMERA_IMAGE_FORMAT_JPEG = "jpeg-256";
		public static final String CAMERA_IMAGE_FORMAT_NV21 = "nv21-17";
		public static final String CAMERA_IMAGE_FORMAT_NV16 = "nv16-16";
		public static final String CAMERA_IMAGE_FORMAT_RGB_565 = "rgb_565-4";
		public static final String CAMERA_IMAGE_FORMAT_YUY2 = "yuy2-20";
		public static final String CAMERA_IMAGE_FORMAT_YV12 = "yv12-842094169";
		
		// parameters are sent as strings defined below, but one of these ints
		// should be returned by the PC upon request to change this camera parameter
		public static final int CAMERA_FLASH_OFF = 0;
		public static final int CAMERA_FLASH_ON = 1;
		public static final int CAMERA_FLASH_RED_EYE = 2;
		public static final int CAMERA_FLASH_TORCH = 3;
		
		public static final String[] IMAGE_FLASH_MODES = {
			"off", "on", "red-eye", "torch"
		};
		
		// parameters are sent as strings defined below, but one of these ints
		// should be returned by the PC upon request to change this camera parameter
		public static final int CAMERA_FOCUS_AUTO = 0;
		public static final int CAMERA_FOCUS_CONTINUOUS_PICTURE = 1;
		public static final int CAMERA_FOCUS_CONTINUOUS_VIDEO = 2;
		public static final int CAMERA_FOCUS_EDOF = 3;
		public static final int CAMERA_FOCUS_FIXED = 4;
		public static final int CAMERA_FOCUS_INFINITY = 5;
		public static final int CAMERA_FOCUS_MACRO = 6;
		
		public static final String[] IMAGE_FOCUS_MODES = {
			"auto", "continuous-picture", "continuous-video",
			"edof", "fixed", "infinity", "macro"
		};
		
		
		// defaults to give the camera (for testing)
		public static final long ARG_PICTURE_DEFAULT_FREQUENCY = 3000;
		public static final long ARG_PICTURE_DEFAULT_TIME_ELAPSE = 20000;
		public static final int ARG_PICTURE_DEFAULT_PICTURE_AMOUNT = 10;

		// TODO: make args for other sensors

	} // end Args class

	
	/*******************************************|
	 * Info about this application
	 *******************************************
	 */
	public static final class Info {
		
		/**
		 * UUID of the ARC service
		 * @see Sorcery
		 * @deprecated
		 */
		public static final String OLD_UUID = "4280720EB1E14C09AE55C2A4D70CEEEE";
		
		/**
		 * UUID to connect PC and android device with, using bluetooth
		 */
		public static final String UUID = "071299af-103e-4578-b3cf-f2a386022a0d";
		
		/**
		 * name of the ARC service. spoilers: its ARC.
		 */
		public static final String SERVICE_NAME = "AndroidRemoteController";
		
		/**
		 * Wifi port to connect to android device through
		 */
		public static final int WIFI_PORT = 9999;
		
	} // end Info class
	

	
	/**
	 * Data types specified by either device, specifying
	 * what type of data they are sending across the connection.
	 */
	public static final class DataTypes {
		
		public static final int NOTIFY = 0;
		
		public static final int JPEG = 1;
	}
	
	

	/**
	 * Command separator for a packet of commands to be interpreted by the phone
	 * app. WARNING: ALL commands must end with this terminator or they will not
	 * be interpreted correctly. (especially the last command in the packet)
	 */
	public static final char PACKET_DELIMITER = '\n';
	
	public static final char PACKET_LIST_DELIMITER = '&';
	
	public static final char PACKET_START = 'S';
	
	public static final String PACKET_END = "PACKET_COMPLETE";
	
	public static final char TASK_COMPLETE = '#';
	
	public static final char TASK_ERRORED_OUT = '!';
	

	/**
	 * Header for android device's response to the controller PC's
	 * request for a list of supported features. Order will be
	 * as follows:<br>
	 * SUPPORTED_FEATURES_HEADER<br>
	 * sensor tag (such as {@link #CAMERA_SENSOR_TAG})<br> 
	 * list of features<br>
	 * {@link #SUPPORTED_FEATURES_FOOTER}<br>
	 * if the list of features contains sub-lists for feature types,
	 * their size will be given in advance
	 */
	public static final char SUPPORTED_FEATURES_HEADER = 'H';
	
	public static final char SUPPORTED_FEATURES_FOOTER = 'F';
	
	
	public static final char CAMERA_SENSOR_TAG = 'C';

	/**
	 * Returns from phone:<br>
	 * Task id<br>
	 * data (probably byte array)
	 */
	public static final int HEADER_ELEMENTS = 2;

	/**
	 * Jenny, I got your number<br>
	 * I need to make you mine<br>
	 * Jenny, don't change your number
	 * 
	 * @deprecated I totally tried to call this and it said the number wasn't in
	 *             service...
	 */
	public static final int GENERIC_ID = 8675309;


}
