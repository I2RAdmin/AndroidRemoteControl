package ARC;

/********************************************************************************|
 * This class models a pool of constants that both the android device being
 * controlled and the controlling PC can pull from, so that they can effectively
 * communicate.
 * 
 * Sub class constant containers:<br>
 * {@link Commands}, {@link Args}, {@link #Info}, {@link DataTypes},<br>
 * {@link Delimiters}, {@link Notifications}, {@link Sensors}
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
	 * across the connection.<br><br>
	 * 
	 * Constants:<br>
	 * {@link #TASK_ID_INDEX}, {@link #COMMAND_INDEX}, {@link #PARAM_START_INDEX},
	 * {@link #SUPPORTED_FEATURES},<br> {@link #MODIFY}, {@link #KILL},
	 * {@link #NO_COMMAND}, {@link #KILL_EVERYTHING},<br>
	 * {@link #TAKE_PICTURE}, {@link #RECORD_AUDIO}
	 ***************************************************************
	 */
	public static final class Commands {

		/**
		 * Index defining the position of the task id for
		 * a command packet.
		 * @see {@link CommandPacket}
		 */
		public static final int TASK_ID_INDEX = 0;
		
		/**
		 * Index defining the position of the command for
		 * a command packet.
		 * @see {@link CommandPacket}
		 */
		public static final int COMMAND_INDEX = 1;
		
		/**
		 * Index defining the position of the first
		 * parameter for a command packet.
		 * @see {@link CommandPacket}
		 */
		public static final int PARAM_START_INDEX = 2;
		
		/**
		 * Used by the controller PC to retrieve supported
		 * features of a particular sensor on the client android
		 * device - parameters should be the sensors to get features
		 * from. If the current device does not support the requested
		 * sensor, a {@link Notifications#SENSOR_NOT_SUPPORTED}
		 * notification will be returned.
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
		public static final int TAKE_PICTURE = 0;
		
		
		/**
		 * Command to start recording audio with
		 * the android device's microphone<br>
		 * WARNING: microphone features must be
		 * modified PRIOR to starting a task with
		 * this argument. Trying to modify the mic
		 * features while it is recording will do nothing.
		 */
		public static final int RECORD_AUDIO = 1;
		
		/**
		 * Command to start sending data from the environment
		 * sensors. If the duration for the sensors to send
		 * back data has not been set prior to this call,
		 * it is imperative that the task be manually killed
		 * by the controller as soon as possible, or risk
		 * draining the android device's power at a very high rate.
		 */
		public static final int LISTEN_TO_ENVIRONMENT_SENSORS = 2;
		
		
		/**
		 * Command to start sending GPS data back to the controller PC.
		 * Depending on the parameters received with this command,
		 * either a single update will be sent, or updates will continue
		 * to be sent for a specified duration.
		 */
		public static final int GET_LOCATION = 3;

	}// end Commands class


	
	/*********************************************
	 * Argument constants used to decipher byte
	 * streams received on either client.<br><br>
	 * 
	 * Constants:<br>
	 * {@link #ARG_NO_CHANGE}, {@link #ARG_NONE}, {@link #ARG_STRING_NO_CHANGE},
	 * {@link #ARG_STRING_NONE},<br>{@link #ARG_CHAR_NO_CHANGE}, {@link #ARG_CHAR_NONE},
	 * {@link #CP_SENSOR_INDEX}, {@link #KEY_VALUE_START_INDEX},<br>
	 * {@link #PICTURE_DEFAULT_FREQUENCY}, {@link #PICTURE_DEFAULT_TIME_ELAPSE},
	 * {@link #PICTURE_DEFAULT_PICTURE_AMOUNT}, {@link #FREQUENCY_INDEX},
	 * {@link #DURATION_INDEX}, {@link #AMOUNT_INDEX}
	 * 
	 *********************************************
	 */
	public final static class Args {

		
		// DEFAULT VALUES -----------------------------------|
		
		/**
		 * argument to keep the specified parameter at
		 * its current value.
		 */
		public static final int ARG_NO_CHANGE = -2;
		
		/**
		 * argument specifying that a parameter has no value
		 */
		public static final int ARG_NONE = -1;
		
		
		/**
		 * argument to keep the specified parameter at
		 * its current value.
		 */
		public static final double ARG_DOUBLE_NO_CHANGE = -2D;
		
		
		/**
		 * argument specifying that a parameter has no value
		 */
		public static final double ARG_DOUBLE_NONE = -1D;
		
		
		/**
		 * argument to keep the specified parameter at
		 * its current value.
		 */
		public static final String ARE_STRING_NO_CHANGE = "-2";
		
		/**
		 * argument specifying that a parameter has no value
		 */
		public static final String ARG_STRING_NONE = "-1";
		
		/**
		 * argument to keep the specified parameter at
		 * its current value.
		 */
		public static final char ARG_CHAR_NO_CHANGE = '_';
		
		/**
		 * argument specifying that a parameter has no value
		 */
		public static final char ARG_CHAR_NONE = '-';
		
		
		// SUPPORTED FEATURES CONSTANTS ----------------------|
		// these are used to inform the controller of
		// the features available on the client android device
		
		/**
		 * Index at which a command packet contains the sensor
		 * ID which will be used to kill or modify tasks. This
		 * can only be used if a command packet is a modify
		 * or kill packet.
		 * @see {@link CommandPacket}
		 */
		public static final int CP_SENSOR_INDEX = 0;
		
		/**
		 * Index at which a command packet contains the sensor
		 * ID which will be used to modify tasks. This
		 * can only be used if a command packet is a modify packet.
		 * @see {@link CommandPacket}
		 */
		public static final int KEY_VALUE_START_INDEX = 1;
		
		// CAMERA FEATURES -----------------------------|
		
		
		// defaults to give the camera (for testing)
		
		/**
		 * Index for the camera frequency parameter
		 * when starting a new camera task
		 */
		public static final int FREQUENCY_INDEX = 0;
		
		/**
		 * Index for the camera duration parameter
		 * when starting a new camera task
		 */
		public static final int DURATION_INDEX = 1;
		
		/**
		 * Index for the camera picture amount parameter
		 * when starting a new camera task
		 */
		public static final int AMOUNT_INDEX = 2;
		
		
		/**
		 * Default value, primarily for testing
		 */
		public static final long PICTURE_DEFAULT_FREQUENCY = 3000;
		
		/**
		 * Default value, primarily for testing
		 */
		public static final long PICTURE_DEFAULT_TIME_ELAPSE = 20000;
		
		/**
		 * Default value, primarily for testing
		 */
		public static final int PICTURE_DEFAULT_PICTURE_AMOUNT = 10;
	

	} // end Args class

	
	
	/*******************************************
	 * Info about this application<br><br>
	 * 
	 * Constants:<br>
	 * {@link #UUID}, {@link #SERVICE_NAME},
	 * {@link #WIFI_PORT}
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
		public static final int WIFI_PORT = 9001;
		
		
		public static final String CONTROLLER_IP_ADDRESS = "74.243.32.87";
		
	} // end Info class
	

	
	
	/***********************************************************
	 * Data types specified by either device, which represent
	 * what the data they are sending correlates to.<br><br>
	 * 
	 * Constants:<br>
	 * {@link #NOTIFY}, {@link #INTEGER}, {@link #DOUBLE},<br>
	 * {@link #STRING}, {@link #RANGE}, {@link #SET},
	 * {@link #ANY},<br>
	 * {@link #ENVIRONMENT_DATA}, {@link #LOCATION},
	 * {@link #AUDIO}, {@link #IMAGE}
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
		 * Image type - informs the controller that it's
		 * receiving a picture
		 */
		public static final int IMAGE = 2;
		
		
		/**
		 * Audio data type - used when sending audio data from the
		 * microphone sensor back to the controller
		 */
		public static final int AUDIO = 11;
		

		/**
		 * Environment data type - used when sending data
		 * from some generic environment sensor. In order
		 * to distinguish environment sensors when multiple
		 * are being used to gather data, the name of the
		 * sensor will be included in the sent packet along
		 * with this data type. 
		 */
		public static final int ENVIRONMENT_DATA = 13;
		
		
		/**
		 * Location data type - sent whenever location data
		 * is being sent back to the controller device.
		 */
		public static final int LOCATION = 14;
		
		
	} // end of DataTypes class
	
	
	
	/**********************************************
	 * Used when specifying a sensor to the remote
	 * controller.<br><br>
	 * 
	 * Constants:<br>
	 * {@link #CAMERA}, {@link #MICROPHONE},
	 * {@link #ENVIRONMENT_SENSORS}, {@link #GPS}
	 **********************************************
	 */
	public static final class Sensors {
		
		
		/**
		 * Camera sensor - used when the controller is querying the android
		 * device for the camera's features
		 */
		public static final int CAMERA = 1;
		
		
		/**
		 * Microphone sensor - used when a request is received by the android
		 * device for an audio task, or when the android device is sending audio
		 * back to the controller
		 */
		public static final int MICROPHONE = 10;
		
		/**
		 * Environment sensors - used when informing
		 * the controller about available environment sensors.
		 * @see {@link Sensor} for types
		 */
		public static final int ENVIRONMENT_SENSORS = 12;
		
		
		/**
		 * GPS Sensor - used when informing the controller
		 * about an android device's GPS capabilities
		 */
		public static final int GPS = 15;
		
		
	} // end of Sensors class
	
	

	/*****************************************************
	 * All delimiters used to encode information to byte
	 * streams so that when they are received, they can
	 * be parsed back into meaningful information.<br><br>
	 * 
	 * Constants:<br>
	 * {@link #PACKET_DELIMITER},
	 * {@link #PACKET_LIST_DELIMITER},
	 * {@link #PACKET_START}, {@link #PACKET_END},<br>
	 * {@link #SUPPORTED_FEATURES_HEADER},
	 * {@link #SUPPORTED_FEATURES_FOOTER}
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
		 * their size will be given in advance.<br>
		 * Currently not used.
		 */
		public static final char SUPPORTED_FEATURES_HEADER = 'H';
		
		/**
		 * Header for android device's response to the controller PC's
		 * request for a list of supported features. Order will be
		 * as follows:<br>
		 * {@link #SUPPORTED_FEATURES_HEADER}<br>
		 * sensor tag (such as {@link #CAMERA_SENSOR_TAG})<br> 
		 * list of features<br>
		 * SUPPORTED_FEATURES_FOOTER<br>
		 * if the list of features contains sub-lists for feature types,
		 * their size will be given in advance.<br>
		 * Currently not used.
		 */
		public static final char SUPPORTED_FEATURES_FOOTER = 'F';
		
	} // end of Delimiters class
	
	

	/*******************************************************
	 * Notifications are short byte packets sent from the
	 * android device to quickly tell the pc client about
	 * a certain task or query that the pc gave to the
	 * android device.<br><br>
	 * 
	 * Constants:<br>
	 * {@link #TASK_COMPLETE}, {@link #TASK_ERRORED_OUT},
	 * {@link #SENSOR_NOT_SUPPORTED}
	 *******************************************************
	 */
	public static final class Notifications {
		
		
		/**
		 * Used to alert the controller
		 * that a task has been completed on the android side
		 */
		public static final char TASK_COMPLETE = '#';
		
		
		/**
		 * Used to alert the controller
		 * that a task has failed to complete on the android side
		 */
		public static final char TASK_ERRORED_OUT = '!';
		
		/**
		 * Used to alert controller
		 * that a requested sensor is not supported on this
		 * device. Sensor requests will typically come from
		 * a query for the current device's supported features
		 * for that sensor.
		 * @see {@link Commands#SUPPORTED_FEATURES}
		 */
		public static final char SENSOR_NOT_SUPPORTED = '@';
		
		
		/**
		 * Used to notify the remote controller when this
		 * android device is entering or exiting a proximity
		 * specified by the controller (via GPS sensors)
		 */
		public static final char PROXIMITY_UPDATE = 'P';
		
		
	} // end of Notifications class

} // end of Constants class
