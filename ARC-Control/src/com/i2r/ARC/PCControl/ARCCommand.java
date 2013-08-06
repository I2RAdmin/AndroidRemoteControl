/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * Class encapsulates an ARC Command.  ARC commands consist of an <code>int</code> header and a <code>List</code> of <code>String</code> 
 * elements as arguments.
 * 
 * The ARCCommand class also keeps track of various constants to use for defaults, the constant indexes of various argument parameters
 * and default argument strings for various commands.
 * 
 * @author Johnathan Pagnutti
 *
 */
public class ARCCommand {
	static final Logger logger = Logger.getLogger(ARCCommand.class);
	
	public static final int NO_COMMAND = -2;
	public static final int KILL = 0;
	public static final int TAKE_PICTURES = 1;
	public static final int MODIFY_SENSOR = 2;
	public static final int SUPPORTED_FEATURES = 3;
	
	public static final int TAKE_PICTURE_FREQUENCY_INDEX = 0;
	public static final int TAKE_PICTURE_TIMEFRAME_INDEX = 1;
	public static final int TAKE_PICTURE_AMMOUNT_INDEX = 2;

	public static final int CAMERA_FEATURE_INDEX = 0;
	
	public static final int TAKE_PICTURE_ARG_LIST_SIZE = 3;
	public static final int SUPPORTED_FEATURES_ARG_LIST_SIZE = 1;
	
	private static final int MINIMUM_TAKE_PICTURE_FREQUENCY_VALUE = 500;
	private static final int MINIMUM_TAKE_PICTURE_TIMEFRAME = MINIMUM_TAKE_PICTURE_FREQUENCY_VALUE;
	private static final int MINIMUM_TAKE_PICTURE_AMMOUNT = 1;
	
	private static final int CAMERA_ID = 4;
	
	private static final String PICTURE_FREQUENCY_DEFAULT = "3000";
	private static final String PICTURE_AMMOUNT_DEFAULT = "1";
	private static final String NO_ARGUMENT = "-1";
	private static final String PICTURE_TIMEFRAME_DEFAULT = "30";
	
	private static final String IMAGE_TYPE_JPEG = "jpeg";
	
	private static final String CAMERA_ID_DEFAULT = "4";
	
	private static final String[] DEFAULT_NO_COMMAND_ARGUMENTS = {};
	private static final String[] DEFAULT_KILL_COMMAND_ARGUMENTS = {NO_ARGUMENT};
	private static final String[] DEFAULT_TAKE_PICTURE_ARGUMENTS = {PICTURE_FREQUENCY_DEFAULT, NO_ARGUMENT, PICTURE_AMMOUNT_DEFAULT};
	private static final String[] DEFAULT_SUPPORTED_FEATURES = {NO_ARGUMENT};
	private static final String[] DEFAULT_MODIFY_SENSOR_ARGUMENTS = {NO_ARGUMENT};

	public static final int KILL_TASK_INDEX = 0;
	
	//the header to a command.  Usually something for the camera to do, but can also be a notifier about a sent task
	private int header;
	
	//the arguments to go with that command
	private List<String> arguments;
	
	//the remote device this command is paired with
	private RemoteClient dev;
	
	/**
	 * Default Constuctor
	 */
	public ARCCommand(RemoteClient dev){
		this.dev = dev;
		//set the header to no command
		this.header = NO_COMMAND;
		//set the arguments to the default values for the no command header
		try {
			arguments = defaultArguments(header);
		} catch (UnsupportedValueException e) {
			//this really never should happen.
			logger.error("Congrats! you've ended up in a circle of hell.");
		}
	}
	
	/**
	 * Default constructor for a particular header.  If the header given is invalid, then return the default ARCCommand
	 * @param header the header to use to create a new default command.
	 * @throws UnsupportedValueException 
	 */
	public ARCCommand(RemoteClient dev, int header) throws UnsupportedValueException{
		this.dev = dev;
		//for the supplied header
		switch(header){
		case NO_COMMAND:
		case KILL:
		case TAKE_PICTURES:
		case SUPPORTED_FEATURES:
		case MODIFY_SENSOR:
			//if the header was the no command header, the kill header, or the take pictures header
			//set the class header to the supplied header
			this.header = header;
			//set the arguments to the default arguments for that header
			arguments = defaultArguments(header);
			break;
		default:
			//if the header provided was not defined, set the default values for the class fields.
			throw new UnsupportedValueException("Supplied Command header " + header + " was invalid.");
		}
	}
	
	/**
	 * Get the default argument list for a supplied header
	 * 
	 * @param header the header to get the default argument list for
	 * 
	 * @return the default list, or null if the passed header was not defined
	 * @throws UnsupportedValueException 
	 */
	private List<String> defaultArguments(int header) throws UnsupportedValueException {
		//for the value of the provided header...
		switch(header){
		//if the header was no command
		case NO_COMMAND:
			//return the default no command argument list
			return Arrays.asList(DEFAULT_NO_COMMAND_ARGUMENTS);
		//if the header was kill
		case KILL:
			//return the default kill argument list
			return Arrays.asList(DEFAULT_KILL_COMMAND_ARGUMENTS);
		//if the header was take pictures
		case TAKE_PICTURES:
			//return the default take pictures list
			return Arrays.asList(DEFAULT_TAKE_PICTURE_ARGUMENTS);
		case SUPPORTED_FEATURES:
			return Arrays.asList(DEFAULT_SUPPORTED_FEATURES);
		case MODIFY_SENSOR:
			return Arrays.asList(DEFAULT_MODIFY_SENSOR_ARGUMENTS);
		default:
			throw new UnsupportedValueException("Supplied command header " + header + " was invalid.");
		}
	}

	/**
	 * TODO: comment this
	 * @param dev
	 */
	public void setRemoteDevice(RemoteClient dev){
		this.dev = dev;
	}
	
	/**
	 * @return the header
	 */
	public int getHeader() {
		return header;
	}

	/**
	 * @return the arguments
	 */
	public List<String> getArguments() {
		return arguments;
	}

	/**
	 * Versitile Constructor.  Allows to set the header, along with the arguments to that header
	 * If the arguments supplied are invalid, defaults to the default argument list for the header given.
	 * 
	 * If the header passed is undefined, then defaults to the default ARCCommand.
	 * 
	 * @param header the header for the ARCCommand
	 * @param arguments the list of arguments to use for a specified header
	 * @throws UnsupportedValueException if an argument in arguments is invalid for the given header
	 */
	public ARCCommand(RemoteClient dev, int header, List<String> arguments) throws UnsupportedValueException{
		this.dev = dev;
		
		//for the defined headers...
		switch(header){
		case NO_COMMAND:
		case KILL:
		case TAKE_PICTURES:
		case SUPPORTED_FEATURES:
		case MODIFY_SENSOR:
			//set the header to the provided header
			this.header = header;
			this.arguments = checkAgainstDevice(header, arguments);
			
			break;
		default:
			throw new UnsupportedValueException("Supplied command header " + header + " was invalid.");
		}
		
		
	}
	
	/**
	 * Checks a list of arguments, along with the header they go to, to see if they are valid.  If they are, they are returned.
	 * If they are not, the default argument list for that particular header is returned.
	 * 
	 * If the header is undefined, then null is returned.
	 * 
	 * @param header the header to check the argument list again.
	 * @param arguments the argument list to check
	 * 
	 * @return the provided argument list if it checked out, the default if the given arguments were bad or null if the given header
	 * 			was undefined
	 */
	private List<String> checkAgainstDevice(int header, List<String> arguments) throws UnsupportedValueException {
		switch (header){
		case NO_COMMAND:
			return checkNoCommandArgs(arguments);
		case KILL:
			return checkKillCommandArgs(arguments);
		case SUPPORTED_FEATURES:
			return checkSupportedFeaturesCommandArgs(arguments);
		case TAKE_PICTURES:
			return checkTakePicturesCommandArgs(arguments);
		case MODIFY_SENSOR:
			return checkDeviceModifySensorParams(arguments);
		default:
			throw new UnsupportedValueException("The supplied header " + header + " was invalid.");
		}
	}

	
	private List<String> checkDeviceModifySensorParams(List<String> arguments) throws UnsupportedValueException {
		logger.debug("Checking against device: " + dev.toString());
		
		String sensor = arguments.get(0);
		List<String> subArgs = arguments.subList(1, arguments.size());
		
		switch(sensor){
		case (CAMERA_ID_DEFAULT):
			int i = 0;
			while(i < subArgs.size()){
				String key = subArgs.get(i);
				i++;
				String value = subArgs.get(i);
				i++;
				
				dev.checkSingleArg(Sensor.CAMERA, key, value);
			}
		}
		
		return arguments;
	}

	private List<String> checkSupportedFeaturesCommandArgs(List<String> arguments) throws UnsupportedValueException {
		
		if(arguments.size() < SUPPORTED_FEATURES_ARG_LIST_SIZE){
			return defaultArguments(SUPPORTED_FEATURES);
		}
		
		for(int i = 0; i < arguments.size(); i++){
			int num = Integer.parseInt(arguments.get(i));
			
			switch(num){
			case CAMERA_ID:
				break;
			default:
				throw new UnsupportedValueException("Unsupported Sensor " + num);
			}
		}
		
		return arguments;
	}

	/**
	 * Checks the argument list when the header supplied is to take pictures.
	 * 
	 * If any element of the list falls outside specified bounds or is otherwise invalid, it is set to the default.
	 * 
	 * @param arguments the list of arguments to check
	 * @return a valid list of arguments, that may or may not have defaults.
	 * @throws UnsupportedValueException 
	 */
	private List<String> checkTakePicturesCommandArgs(List<String> arguments) throws UnsupportedValueException {
		//counting variable
		int i;
		//value holder
		int num;
		
		//make sure the list is of the right size
		if(arguments.size() != TAKE_PICTURE_ARG_LIST_SIZE){
			return defaultArguments(TAKE_PICTURES);
		}
		
		//for each arg in the argument list...
		for(i = 0; i < arguments.size(); i++){
			//get the value of the argument
			String value = arguments.get(i);
			
			//for the argument in position i...
			switch(i){
			//if i is the take picture frequency index
			case TAKE_PICTURE_FREQUENCY_INDEX:
				//parse the string value as an integer
				num = Integer.parseInt(value);
				//if that integer is shorter than the minimum value set
				if(num < 0 && num != -1){
					throw new UnsupportedValueException("Supplied picture frequency is not valid.");
				}
				break;
			//if i is the time to take pictures in index
			case TAKE_PICTURE_TIMEFRAME_INDEX:
				//parse the string value as an integer
				num = Integer.parseInt(value);
				//if that integer is smaller than the minimum value for time
				if(num < 0 && num != -1){
					throw new UnsupportedValueException("Supplied picture duration is invalid");
				}
				break;
			//if i is the amount of pictures to take
			case TAKE_PICTURE_AMMOUNT_INDEX:
				//parse the string value as an integer
				num = Integer.parseInt(value);
				//if the integer value is smaller than the minimum allowed number of pictures to take
				if(num < 0 && num != -1){
					//set it to the minimum value
					throw new UnsupportedValueException("Supplied picture ammount is invalid");
				}
				break;
			default:
				throw new UnsupportedValueException("The argument at position " + i + " is out of bounds.");
			}
		}
		
		//now, either the picture time frame or the picture amount must be set to -1.
		int takePictureTimeNum =  Integer.parseInt(arguments.get(TAKE_PICTURE_TIMEFRAME_INDEX));
		int takePictureAmountNum = Integer.parseInt(arguments.get(TAKE_PICTURE_AMMOUNT_INDEX));
		
		//if they're both not -1
		if(takePictureTimeNum != -1 && takePictureAmountNum != -1){
			throw new UnsupportedValueException("Both the take picture duration and the take picture amount are set.");
		}
		
		return arguments;
	}

	/**
	 * Checks and sets the arguments provided for the kill command
	 * 
	 * The kill command only has one set of valid arguments- the defauts.  So, set them.
	 * @param arguments the arguments to check for the kill command
	 * @return the correct list of arguments for the kill command
	 * @throws UnsupportedValueException 
	 */
	private List<String> checkKillCommandArgs(List<String> arguments) throws UnsupportedValueException {
		
		//kill just has one argument
		if(arguments.isEmpty()){
			throw new UnsupportedValueException("Invalid number of arguments for the kill command.");
		}else{
			int taskId = Integer.parseInt(arguments.get(KILL_TASK_INDEX));
			if(taskId < 0){
				throw new UnsupportedValueException("Task ID must be greater than 0.");
			}else if(dev.deviceTasks.getTask(taskId) == null){
				throw new UnsupportedValueException("Task ID not found in " + dev + " task stack");
			}else{
				return arguments;
			}
		}
	}

	/**
	 * Checks and sets the arguments provided by the no command command
	 * 
	 * The no command command has only one set of valid arguments, the defaults.  So, just set them rather than checking.
	 * 
	 * @param arguments the arguments that we want to check for the no command 
	 * @return the correct list of arguments for the no command
	 * @throws UnsupportedValueException 
	 */
	private List<String> checkNoCommandArgs(List<String> arguments) throws UnsupportedValueException {
		return defaultArguments(NO_COMMAND);
	}

	/**
	 * Return a new ARC command given a string
	 * @param line the string to create a new ARCCommand out of
	 * @return
	 * @throws UnsupportedValueException if the line is invalid
	 */
	public static ARCCommand fromString(RemoteClient device, String line) throws UnsupportedValueException {
		
		logger.debug("Line: " + line);
		Scanner lineScan = new Scanner(line);
		int header;
		
		if(lineScan.hasNextInt()){
			header = lineScan.nextInt();
			logger.debug("header: " + header);
		}else{
			throw new UnsupportedValueException("Could not parse header from supplied line.");
		}
		
		if(header != NO_COMMAND && header != KILL && header != TAKE_PICTURES && header != SUPPORTED_FEATURES && header != MODIFY_SENSOR){
			logger.debug("Header not valid, using default.");
			return new ARCCommand(device);
		}
		
		if(lineScan.hasNext()){
			List<String> lineArgs = new ArrayList<String>();
			while(lineScan.hasNext()){
				lineArgs.add(lineScan.next());
			}
			return new ARCCommand(device, header, lineArgs);
		}else{
			return new ARCCommand(device, header);
		}
	}
}
