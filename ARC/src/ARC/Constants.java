package ARC;

/********************************************************************************|
 * This class models a pool of constants that both the android device being
 * controlled and the controlling PC can pull from, so that they can effectively
 * communicate.
 ********************************************************************************
 */
public final class Constants {

	
	/***************************************************************
	 * Computer Command Structure:<br>
	 * TASK ID<br>
	 * COMMAND<br>
	 * [COMMAND ARGS]<br><br>
	 * 
	 * Note: only the computer client will send these constants
	 * across the connection.
	 ***************************************************************
	 */
	public static final class Commands {

		public static final int TASK_ID_INDEX = 0;
		public static final int HEADER_INDEX = 1;
		public static final int PARAM_START_INDEX = 2;
		
		/**
		 * Used by the controller PC to retrieve supported
		 * features of a particular sensor on the client android
		 * device - parameters should be the sensors to get features
		 * from
		 */
		public static final int SUPPORTED_FEATURES = -5;
		
		
		/**
		 * Modify a currently running task with new parameters
		 * Expected arguments:<br><br>
		 * taskID<br>
		 * MODIFY command<br>
		 * odd indexes after modify command are keys<br>
		 * even indexes after modify command are values
		 */
		public static final int MODIFY = -4;
		
		
		/**
		 * Kill command header - specifies that the following commands will
		 * involve stopping one or multiple procedures.
		 */
		public static final int KILL = -3;

		
		/**
		 * Sent by controller if there was an error while building
		 * command on the PC client. 
		 */
		public static final int NO_COMMAND = -2;
		
		
		/**
		 * Kills any currently running applications on the android device,
		 * closes open sockets and terminates all android-2-PC communication.
		 */
		public static final int KILL_EVERYTHING = -1;
		
			
		// TODO: tell jonathan that i did a stunt and he's gonna hate me
		
		/**
		 * Expected arguments following this command header:<br>
		 * int - maximum amount of pictures to be taken<br>
		 * long - frequency of captures in milliseconds (captures per second)
		 */
		public static final int PICTURE = 0;
		
		
		/**
		 * Command to start recording audio with
		 * the android device's microphone<br>
		 * WARNING: microphone features must be
		 * modified PRIOR to starting a task with
		 * this argument. Trying to modify the mic
		 * features while it is recording will do nothing.
		 */
		public static final int RECORD_AUDIO = 1;

	}// end Commands class


	
	/*********************************************
	 * Argument constants used to decipher byte
	 * streams received on either client.
	 *********************************************
	 */
	public final static class Args {

		
		// DEFAULT VALUES -----------------------------------|
		public static final int ARG_NO_CHANGE = -2;
		public static final int ARG_NONE = -1;
		
		public static final String ARE_STRING_NO_CHANGE = "-2";
		public static final String ARG_STRING_NONE = "-1";
		
		public static final char ARG_CHAR_NONE = '-';
		public static final char ARG_CHAR_NO_CHANGE = '_';
		
		public static final String SIZE_ZERO = "0";
		
		
		// SUPPORTED FEATURES CONSTANTS ----------------------|
		// these are used to inform the controller of
		// the features available on the client android device
		
		public static final int CP_SENSOR_INDEX = 0;
		public static final int KEY_VALUE_START_INDEX = 1;
		
		// CAMERA FEATURES -----------------------------|
		


		public static final int CAMERA_FREQUENCY_INDEX = 0;
		public static final int CAMERA_DURATION_INDEX = 1;
		public static final int CAMERA_PICTURE_AMOUNT_INDEX = 2;
		
		// defaults to give the camera (for testing)
		public static final long PICTURE_DEFAULT_FREQUENCY = 3000;
		public static final long PICTURE_DEFAULT_TIME_ELAPSE = 20000;
		public static final int PICTURE_DEFAULT_PICTURE_AMOUNT = 10;
		
		
		public static final int AUDIO_DURATION_INDEX = 0;
		// others...
		
		// TODO: make args for other sensors

	} // end Args class

	
	
	/*******************************************
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
	

	
	
	/***********************************************************
	 * Data types specified by either device, which represent
	 * what the data they are sending correlates to.
	 ***********************************************************
	 */
	public static final class DataTypes {
		
		/**
		 * Notifier type - tells controller that
		 * following data is purely informative
		 * of the android device's state
		 */
		public static final int NOTIFY = 0;
		

		
		/**
		 * Image type - informs the controller that it's
		 * receiving a picture
		 */
		public static final int IMAGE = 2;
		
		/**
		 * Int type - informs the controller that
		 * a certain part of this application
		 * expects integer values as input
		 */
		public static final int INTEGER = 3;
		
		/**
		 * double type - informs the controller that
		 * a certain part of this application
		 * expects doubles values as input
		 */
		public static final int DOUBLE = 4;
		
		/**
		 * String type - informs the controller that
		 * a certain part of this application
		 * expects String values as input
		 */
		public static final int STRING = 5;
		
		/**
		 * Range type - informs the controller that
		 * the data its about to receive is in the
		 * form of a range of numbers, and that
		 * any input for that range is expected to
		 * fall within that range
		 */
		public static final int RANGE = 6;
		
		/**
		 * Set type - informs the controller that
		 * it's about to receive a set of elements that
		 * it can choose from as input parameters
		 */
		public static final int SET = 7;
		
		/**
		 * data type any - input is not specific
		 * to a particular data type
		 */
		public static final int ANY = 8;
		
		
		/**
		 * data type const - describes any
		 * properties that are immutable and
		 * cannot be set to a different value
		 */
		public static final int CONST = 9;
		
		
		/**
		 * Feature type - used when the controller is querying the android
		 * device for the camera's features
		 */
		public static final int CAMERA = 1;

		
		/**
		 * Audio data type - used when a request is received by the android
		 * device for an audio task, or when the android device is sending audio
		 * back to the controller
		 */
		public static final int MICROPHONE = 10;

		/**
		 * Temperature data type - used when a request is recieved by the
		 * android device for the phone's current temperature reading
		 * of its surrounding environment
		 */
		public static final int TEMPERATURE = 6;

		/**
		 * Pressure data type - used when a request is recieved by the
		 * android device for the phone's current atmospheric pressure reading
		 * of its surrounding environment
		 */
		public static final int PRESSURE = 7;

		
		
	} // end of DataTypes class
	
	

	
	
	
	/*****************************************************
	 * All delimiters used to encode information to byte
	 * streams so that when they are received, they can
	 * be parsed back into meaningful information.
	 *****************************************************
	 */
	public static final class Delimiters {
		
		/**
		 * Command separator for a packet of commands to be interpreted by the phone
		 * app. WARNING: ALL commands must end with this terminator or they will not
		 * be interpreted correctly. (especially the last command in the packet)
		 */
		public static final char PACKET_DELIMITER = '\n';
		
		/**
		 * Used to define the end of a list of elements being sent
		 * to the controller PC. The controller can use this list,
		 * along with its reference name, to change the parameter
		 * that the list name represents to any of the values
		 * defined in the resulting list.
		 */
		public static final char PACKET_LIST_DELIMITER = '&';
		
		
		/**
		 * Defines a response packet's starting point.
		 * Not currently used.
		 */
		public static final String PACKET_START = "PACKET_START";
		
		/**
		 * Defines a response packet's ending point.
		 * Used to split packets if they are received in
		 * a single bundled string.
		 */
		public static final String PACKET_END = "PACKET_COMPLETE";
		
		
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
		
	} // end of Delimiters class
	
	

	/*******************************************************
	 * Notifications are short byte packets sent from the
	 * android device to quickly tell the pc client about
	 * a certain task or query that the pc gave to the
	 * android device.
	 *******************************************************
	 */
	public static final class Notifications {
		
		
		/**
		 * Used with Notify DataTypes to alert the controller
		 * that a task has been completed on the android side
		 */
		public static final char TASK_COMPLETE = '#';
		
		
		/**
		 * Used with Notify DataTypes to alert the controller
		 * that a task has failed to complete on the android side
		 */
		public static final char TASK_ERRORED_OUT = '!';
		
		
		public static final char SENSOR_NOT_SUPPORTED = '@';
		
		
	}

	

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
