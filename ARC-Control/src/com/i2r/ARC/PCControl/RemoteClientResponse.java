package com.i2r.ARC.PCControl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This object packages a response from some remote device.
 * 
 * Data comes on from the parser as a task ID, response type and an array of bytes.
 * The {@link RemoteClientResponse} object parses the byte array, and sets the appropriate fields based on the response type.
 * 
 * <b> NOTE </b> It is important to note that a {@link RemoteClientResponse} does not actually perform any of the actions that the response
 * encapsulates.  Think about them as the required data to perform an action, however, the actual doing of the action might be at a later time.
 * 
 * @author Johnathan Pagnutti
 */
public class RemoteClientResponse {
	
	//TODO:
	//I actually don't want to comment these constants right now.  They may get folded into the dataType enumeration
	public static final int DATA_TYPE_NOTIFY = 0;
	
	public static final int DATA_TYPE_CAMERA_ARGS = 1;
	public static final int DATA_TYPE_MICROPHONE_ARGS = 10;
	public static final int DATA_TYPE_ENVIRONMENT_ARGS = 12;
	public static final int DATA_TYPE_LOCATION_ARGS = 15;
	
	public static final int DATA_TYPE_IMAGE = 2;
	public static final int DATA_TYPE_AUDIO = 11;
	public static final int DATA_TYPE_ENVIRONMENT = 13;
	public static final int DATA_TYPE_LOCATION = 14;
	
	/**
	 * This is the notify code that a task has been successfully completed.
	 */
	public static final String TASK_COMPLETE_ARGUMENT = "#";
	
	/**
	 * This is the notify code that a task has errored out.
	 */
	public static final String TASK_ERRORED_ARGUMENT = "!";
	
	/**
	 * This is the notify code that a requested sensor is unsupported
	 */
	public static final String UNSUPPORTED_SENSOR = "@";
	
	/**
	 * This is the notify code that the device has gotten within/no longer within some proximity
	 */
	public static final String PROXIMITY_ALERT = "P";
	
	/**
	 * This is the notify code to move on to the next picture
	 */
	public static final String NEXT_PICTURE = "$";
	
	/**
	 * This is the action code to remove a task from the task stack
	 */
	public static int REMOVE_TASK = 0;
	
	/**
	 * This is the action code to save some task data to a file
	 */
	public static int SAVE_FILE = 1;
	
	/**
	 * This is the action code to stream data
	 * TODO: not really implemented yet
	 */
	public static int STREAM = 2;
	
	/**
	 * This is the action code to notify the user of something on the remote device
	 */
	public static int NOTIFY = 3;
	
	/**
	 * This is the action code to set the {@link Sensor#CAMERA} in a {@link RemoteClient}'s {@link Capabilities} map.
	 */
	public static int CAMERA_ARGS = 4;
	
	/**
	 * This is the action code to set the {@link Sensor#MICROPHONE} in a {@link RemoteClient}'s {@link Capabilities} map.
	 */
	public static int MICROPHONE_ARGS = 5;
	
	/**
	 * This is the action code to set the {@link Sensor#ENVIRONMENT} in a {@link RemoteClient}
	 */
	public static int ENVIRONMENT_ARGS = 6;
	
	/**
	 * This is the action code to set the {@link Sensor#LOCATION} in a {@link RemoteClient}
	 */
	public static int LOCATION_ARGS = 7;
	
	/**
	 * The taskID.  This marks which task the response should be associated with.
	 * This value should never be -1 when we actually want to process a response
	 */
	int taskID = -1;
	
	/**
	 * Size of the data block.  When data that we want to stream or save exists in a response, this is the amount
	 * of bytes in the block.
	 */
	int dataBlockSize = -1;
	
	/**
	 * The data type of the arguments passed along in this response.
	 * Going to be one of the constants outlined at the top of the class
	 */
	int argType = -1;
	
	/**
	 * Action Type.  This sets the action of this response, which is how methods that actually act on the response figure out
	 * what action the response is abstracting.
	 * 
	 * -1 is the initial set action, which is an error action.
	 */
	int action = -1;
	
	/**
	 * An array of file data to stream or save to a file.  When this field has data, we want to stream or save it.  Otherwise,
	 * it remains null
	 */
	byte[] dataBlock = null;
	
	/**
	 * This field encapsulates any other arguments we would want to send along with a response. It's default is a <code>List</code> of
	 * action <code>String</code> with the single element of a blank string.
	 */
	
	List<String> otherArgs;
	
	/**
	 * Object constructor.  Takes some block of data along with a taskID, and tries to interpet the block. It sets the action argument
	 * to the action of response this is, based on how it can interpet the provided byte array
	 *  
	 * @param taskID the taskID to set
	 * @param data the actual data to set
	 * 
	 * @requires dataBlockSize == data.length
	 */
	static final Logger logger = Logger.getLogger(RemoteClientResponse.class);
	
	public RemoteClientResponse(int taskID, int dataType, byte[] data){
		logger.debug("Creating a Response for task " + taskID);
		
		//taskID will always be set.
		this.taskID = taskID;
		this.argType = dataType;
		
		//figure out what was in the passed block
		interpet(data);
	}

	/**
	 * Private method to interpet the data block sent to the response object.  Sets the appropriate fields as well.
	 * @param data
	 */
	private void interpet(byte[] data) {
		
		switch(argType){
		
		case(DATA_TYPE_NOTIFY):
			//create a string from the data
			String fullData = new String(data);
			String controlString;
			String message = null;
			if(fullData.contains("\n")){
				controlString = fullData.substring(0, fullData.indexOf("\n"));
				message = fullData.substring(fullData.indexOf("\n"));
			}else{
				controlString = fullData;
			}
			
			if(controlString.equals(TASK_COMPLETE_ARGUMENT) || controlString.equals(TASK_ERRORED_ARGUMENT) || 
					controlString.equals(UNSUPPORTED_SENSOR)){
				//remove the task from the task stack
				logger.debug("Remove Task Response created.");
				this.action = REMOVE_TASK;
				
				otherArgs = new ArrayList<String>(1);
				
				this.otherArgs.add(controlString);
				
				if(message != null){
					this.otherArgs.add(message);
				}
				
			}else if (controlString.equals(NEXT_PICTURE)){
				//save the current picture data and move on to the next chunk
				logger.debug("Save File Task has been created.");
				this.action = SAVE_FILE;
				
				otherArgs = new ArrayList<String>(1);
				this.otherArgs.add(controlString);
				
			}else if(controlString.equals(PROXIMITY_ALERT)){
				//alert the user to the fact that we have gotten close or moved away from some point
				logger.debug("Notify Response created.");
				this.action = NOTIFY;
				
				otherArgs = new ArrayList<String>(1);
				this.otherArgs.add(controlString);
				
				if(message != null){
					this.otherArgs.add(message);
				}
				
			}else{
				logger.debug("Could not interpet response packet: ");
				logger.debug(controlString);
			}
			break;
			
		case(DATA_TYPE_MICROPHONE_ARGS):
		case(DATA_TYPE_CAMERA_ARGS):
		case (DATA_TYPE_ENVIRONMENT_ARGS):
		case (DATA_TYPE_LOCATION_ARGS):
			if(argType == DATA_TYPE_MICROPHONE_ARGS){
				this.action = MICROPHONE_ARGS;
			}else if (argType == DATA_TYPE_CAMERA_ARGS){
				this.action = CAMERA_ARGS;
			}else if (argType == DATA_TYPE_ENVIRONMENT_ARGS){
				this.action = ENVIRONMENT_ARGS;
			}else if (argType == DATA_TYPE_LOCATION_ARGS){
				this.action = LOCATION_ARGS;
			}
		
			String[] micArgs = new String(data).split("&");
		
			logger.debug("Seting " + micArgs.length + " features.");
			otherArgs = new ArrayList<String>(micArgs.length);
		
			for(String argLine : micArgs){
				otherArgs.add(argLine);
			}
			break;
		
		case(DATA_TYPE_AUDIO):
		case(DATA_TYPE_ENVIRONMENT):
		case(DATA_TYPE_LOCATION):
		case (DATA_TYPE_IMAGE):
			//send the audio to an audio buffer
			logger.debug("Append Data task created.");
			this.action = STREAM;
			this.dataBlockSize = data.length;
			this.dataBlock = data;
			break;
		default:
			logger.error("This argument action is not supported: " + argType);
		}
	}
}
