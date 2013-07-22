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
	public final class Commands {

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

	}// end Commands class


	
	/*********************************************************
	 * Argument constants used to decipher command packets.
	 ******************************************************** 
	 */
	public final class Args {

		public static final int ARG_NO_CHANGE = -2;
		public static final int ARG_NONE = -1;
		
		public static final int IMAGE_PARAMETER_COUNT = 3;
		public static final int IMAGE_FREQUENCY_INDEX = 0;
		public static final int IMAGE_DURATION_INDEX = 1;
		public static final int IMAGE_COUNT_INDEX = 2;

		public static final long ARG_PICTURE_DEFAULT_FREQUENCY = 3000;
		public static final long ARG_PICTURE_DEFAULT_TIME_ELAPSE = 20000;
		public static final int ARG_PICTURE_DEFAULT_PICTURE_AMOUNT = 10;

		// TODO: make args for other sensors

	} // end Args class

	
	/*******************************************|
	 * Info about this application
	 *******************************************
	 */
	public final class Info {
		
		/**
		 * UUID of the ARC service
		 * @see Sorcery
		 */
		public static final String OLD_UUID = "4280720EB1E14C09AE55C2A4D70CEEEE";
		
		public static final String UUID = "071299af-103e-4578-b3cf-f2a386022a0d";
		
		/**
		 * name of the ARC service. spoilers: its ARC.
		 */
		public static final String SERVICE_NAME = "Android Remote Controller";
		
	} // end Info class
	

	
	public final class DataTypes {
		
		public static final int NOTIFY = 0;
		
		public static final int JPEG = 1;
	}
	
	

	/**
	 * Command separator for a packet of commands to be interpreted by the phone
	 * app. WARNING: ALL commands must end with this terminator or they will not
	 * be interpreted correctly. (especially the last command in the packet)
	 */
	public static final char PACKET_DELIMITER = '\n';
	
	public static final char TASK_COMPLETE = '#';
	
	public static final String TASK_ERRORED_OUT = "FFFFFFFFFFUUUUUUUUUUUUUUUUU";
	
	
	public static final String ERROR_ON_DATA_TRANSFER = "error - bytes could not be written because manager's client socket is not connected";


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
