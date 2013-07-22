/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.Arrays;
import java.util.List;

/**
 * Class encapsulates an ARC Command.  ARC commands consist of an <code>int</code> header and a <code>List</code> of <code>String</code> 
 * elements as arguments.
 * 
 * The ARCCommand class also keeps track of various constants to use for defaults, the constant indexes of various argument parameters
 * and default argument strings for various commands.
 * 
 * TODO: use Java Enumerations to cut some of this down to size and make it less scary.
 * @author Johnathan Pagnutti
 *
 */
public class ARCCommand {
	public static final int NO_COMMAND = -1;
	public static final int KILL = 0;
	public static final int TAKE_PICTURES = 1;
	
	public static final int TAKE_PICTURE_FREQUENCY_INDEX = 0;
	public static final int TAKE_PICTURE_TIMEFRAME_INDEX = 1;
	public static final int TAKE_PICTURE_AMMOUNT_INDEX = 2;
	public static final int PICTURE_FILETYPE_INDEX = 3;
	
	public static final int ARG_LIST_SIZE = 4;
	
	private static final int MINIMUM_TAKE_PICTURE_FREQUENCY_VALUE = 500;
	private static final int MINIMUM_TAKE_PICTURE_TIMEFRAME = MINIMUM_TAKE_PICTURE_FREQUENCY_VALUE;
	private static final int MINIMUM_TAKE_PICTURE_AMMOUNT = 1;
	
	private static final String PICTURE_FREQUENCY_DEFAULT = "3000";
	private static final String PICTURE_AMMOUNT_DEFAULT = "1";
	private static final String NO_ARGUMENT = "-1";
	private static final String PICTURE_TIMEFRAME_DEFAULT = "30";
	private static final String IMAGE_TYPE_JPEG = "jpeg";
	
	private static final String[] DEFAULT_NO_COMMAND_ARGUMENTS = {NO_ARGUMENT, NO_ARGUMENT, NO_ARGUMENT, NO_ARGUMENT};
	private static final String[] DEFAULT_KILL_COMMAND_ARGUMENTS = {NO_ARGUMENT, NO_ARGUMENT, NO_ARGUMENT, NO_ARGUMENT, NO_ARGUMENT};
	private static final String[] DEFAULT_TAKE_PICTURE_ARGUMENTS = {PICTURE_FREQUENCY_DEFAULT, NO_ARGUMENT, PICTURE_AMMOUNT_DEFAULT, IMAGE_TYPE_JPEG};
	
	//the header to a command.  Usually something for the camera to do, but can also be a notifier about a sent task
	private int header;
	//the arguments to go with that command
	private List<String> arguments;
	
	/**
	 * Default Constuctor
	 */
	public ARCCommand(){
		//set the header to no command
		this.header = NO_COMMAND;
		//set the arguments to the default values for the no command header
		arguments = defaultArguments(header);
	}
	
	/**
	 * Default constructor for a particular header.  If the header given is invalid, then return the default ARCCommand
	 * @param header the header to use to create a new default command.
	 */
	public ARCCommand(int header){
		//for the supplied header
		switch(header){
		case NO_COMMAND:
		case KILL:
		case TAKE_PICTURES:
			//if the header was the no command header, the kill header, or the take pictures header
			//set the class header to the supplied header
			this.header = header;
			//set the arguments to the default arguments for that header
			arguments = defaultArguments(header);
			break;
		default:
			//if the header provided was not defined, set the default values for the class fields.
			header = NO_COMMAND;
			arguments = defaultArguments(header);
		}
	}
	
	/**
	 * Get the default argument list for a supplied header
	 * 
	 * @param header the header to get the default argument list for
	 * 
	 * @return the default list, or null if the passed header was not defined
	 */
	private List<String> defaultArguments(int header) {
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
		default:
			//the passed header was undefined, return null
			return null;
		}
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
	 */
	public ARCCommand(int header, List<String> arguments){
		//for the defined headers...
		switch(header){
		case NO_COMMAND:
		case KILL:
		case TAKE_PICTURES:
			//set the header to the provided header
			this.header = header;
			//check the provided argument list
			arguments = checkArguments(header, arguments);
			break;
		default:
			//otherwise, return the default ARCCommand
			this.header = NO_COMMAND;
			arguments = defaultArguments(header);
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
	private List<String> checkArguments(int header, List<String> arguments) {
		switch(header){
		case NO_COMMAND:
			return checkNoCommandArgs(arguments);
		case KILL:
			return checkKillCommandArgs(arguments);
		case TAKE_PICTURES:
			return checkTakePicturesCommandArgs(arguments);
		default:
			//in theory, this is unreachable
			return null;
		}
	}

	/**
	 * Checks the argument list when the header supplied is to take pictures.
	 * 
	 * If any element of the list falls outside specified bounds or is otherwise invalid, it is set to the default.
	 * @param arguments the list of arguments to check
	 * @return a valid list of arguments, that may or may not have defaults.
	 */
	private List<String> checkTakePicturesCommandArgs(List<String> arguments) {
		//counting variable
		int i;
		//value holder
		int num;
		//for each arg in the argument list...
		for(i = 0; i < ARG_LIST_SIZE; i++){
			//get the value of the argument
			String value = arguments.get(i);
			
			//for the argument in position i...
			switch(i){
			//if i is the take picture frequency index
			case TAKE_PICTURE_FREQUENCY_INDEX:
				//parse the string value as an integer
				num = Integer.parseInt(value);
				//if that integer is shorter than the minimum value set
				if(num < MINIMUM_TAKE_PICTURE_FREQUENCY_VALUE){
					//set it at the minimum value
					arguments.set(TAKE_PICTURE_FREQUENCY_INDEX, String.valueOf(MINIMUM_TAKE_PICTURE_FREQUENCY_VALUE));
				}
				break;
			//if i is the time to take pictures in index
			case TAKE_PICTURE_TIMEFRAME_INDEX:
				//parse the string value as an integer
				num = Integer.parseInt(value);
				//if that integer is smaller than the minimum value for time
				if(num < MINIMUM_TAKE_PICTURE_TIMEFRAME && num != -1){
					//set it to the minimum value
					arguments.set(TAKE_PICTURE_TIMEFRAME_INDEX, String.valueOf(MINIMUM_TAKE_PICTURE_TIMEFRAME));
				}
				break;
			//if i is the amount of pictures to take
			case TAKE_PICTURE_AMMOUNT_INDEX:
				//parse the string value as an integer
				num = Integer.parseInt(value);
				//if the integer value is smaller than the minimum allowed number of pictures to take
				if(num < MINIMUM_TAKE_PICTURE_AMMOUNT && num != -1){
					//set it to the minimum value
					arguments.set(TAKE_PICTURE_AMMOUNT_INDEX, String.valueOf(MINIMUM_TAKE_PICTURE_AMMOUNT));
				}
				break;
			//if i is the filetype
			case PICTURE_FILETYPE_INDEX:
				//TODO: check to make sure the filetype is a part of the allowed files innumeration
				//set it as a jpeg
				arguments.set(PICTURE_FILETYPE_INDEX, IMAGE_TYPE_JPEG);
				break;
			default:
				//in theory, this segment is unreachable
				break;
			}
		}
		
		//now, either the picture time frame or the picture amount must be set to -1.
		int takePictureTimeNum =  Integer.parseInt(arguments.get(TAKE_PICTURE_TIMEFRAME_INDEX));
		int takePictureAmountNum = Integer.parseInt(arguments.get(TAKE_PICTURE_AMMOUNT_INDEX));
		
		//if they're both -1
		if(takePictureTimeNum == -1 && takePictureAmountNum == -1){
			//set the amount to the default
			arguments.set(TAKE_PICTURE_AMMOUNT_INDEX, PICTURE_AMMOUNT_DEFAULT);
		}
		
		//if they're both not -1
		if(takePictureTimeNum != -1 && takePictureAmountNum != -1){
			//set the picture amount
			arguments.set(TAKE_PICTURE_TIMEFRAME_INDEX, NO_ARGUMENT);
		}
		return arguments;
	}

	/**
	 * Checks and sets the arguments provided for the kill command
	 * 
	 * The kill command only has one set of valid arguments- the defauts.  So, set them.
	 * @param arguments the arguments to check for the kill command
	 * @return the correct list of arguments for the kill command
	 */
	private List<String> checkKillCommandArgs(List<String> arguments) {
		return defaultArguments(KILL);
	}

	/**
	 * Checks and sets the arguments provided by the no command command
	 * 
	 * The no command command has only one set of valid arguments, the defaults.  So, just set them rather than checking.
	 * 
	 * @param arguments the arguments that we want to check for the no command 
	 * @return the correct list of arguments for the no command
	 */
	private List<String> checkNoCommandArgs(List<String> arguments) {
		return defaultArguments(NO_COMMAND);
	}

}
