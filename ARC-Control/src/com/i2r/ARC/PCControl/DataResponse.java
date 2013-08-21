package com.i2r.ARC.PCControl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This object packages a response from some remote device.
 * Due to contract knowledge, we can assume that the responses will come as a taskID plus some other values.
 * 
 * Data comes on from the parser as an array of bytes and a taskID
 * TODO: this will be expanded.
 * The {@link DataResponse} object parses the byte array, and sets the appropriate fields based on how it can interpret the byte values.
 * 
 * <b> NOTE </b> It is important to note that a {@link DataResponse} does not actually perform any of the actions that its various constructors
 * encapsulate.  Think about them as the required data to perform an action, however, the actual doing of the action might be at a later time.
 * 
 * @author Johnathan Pagnutti
 */
public class DataResponse {
	public static final int DATA_TYPE_NOTIFY = 0;
	
	public static final int DATA_TYPE_CAMERA_ARGS = 1;
	public static final int DATA_TYPE_MICROPHONE_ARGS = 10;
	public static final int DATA_TYPE_ENVIRONMENT_ARGS = 12;
	public static final int DATA_TYPE_LOCATION_ARGS = 15;
	
	public static final int DATA_TYPE_IMAGE = 2;
	public static final int DATA_TYPE_AUDIO = 11;
	public static final int DATA_TYPE_ENVIRONMENT = 13;
	public static final int DATA_TYPE_LOCATION = 14;
	
	public static final String REMOVE_TASK_ARGUMENT = "#";
	public static final String TASK_ERRORED_ARGUMENT = "!";
	public static final String UNSUPPORTED_SENSOR = "@";
	public static final String PROXIMITY_ALERT = "P";
	
	public static int REMOVE_TASK = 0;
	public static int SAVE_FILE = 1;
	public static int STREAM = 2;
	public static int NOTIFY = 3;
	public static int CAMERA_ARGS = 4;
	public static int MICROPHONE_ARGS = 5;
	public static int ENVIRONMENT_ARGS = 6;
	public static int LOCATION_ARGS = 7;
	
	/**
	 * The taskID.  This marks which task the response should be associated with.
	 * This value should never be -1 when we actually want to process a response
	 */
	int taskID = -1;
	
	/**
	 * File Size.  For when a file size exists in a response, this field is set with the file size
	 * otherwise it remains -1
	 */
	int fileSize = -1;
	
	int argType = -1;
	
	/**
	 * Response Type.  This sets the type of this response, which is how methods that actually act on the response figure out
	 * what action the response is abstracting.
	 * 
	 * -1 is the initial set type, which is an error type.
	 */
	int type = -1;
	
	/**
	 * An array of file data to save to a file.  This field is set when it makes sense to do so (we want to save a file)
	 * Otherwise, this field is null
	 */
	byte[] fileData = null;
	
	/**
	 * This field encapsulates any other arguments we would want to send along with a response. It's default is a <code>List</code> of
	 * type <code>String</code> with the single element of a blank string.
	 */
	
	List<String> otherArgs;
	
	/**
	 * Object constructor.  Takes some block of data along with a taskID, and tries to interpet the block. It sets the type argument
	 * to the type of response this is, based on how it can interpet the provided byte array
	 *  
	 * @param taskID the taskID to set
	 * @param data the actual data to set
	 * 
	 * @requires fileSize == data.length
	 */
	static final Logger logger = Logger.getLogger(DataResponse.class);
	
	public DataResponse(int taskID, int dataType, byte[] data){
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
			
			if(controlString.equals(REMOVE_TASK_ARGUMENT) || controlString.equals(TASK_ERRORED_ARGUMENT) || 
					controlString.equals(UNSUPPORTED_SENSOR)){
				//remove the task from the task stack
				logger.debug("Remove Task Response created.");
				this.type = REMOVE_TASK;
				
				otherArgs = new ArrayList<String>(1);
				this.otherArgs.add(controlString);
				
				if(message != null){
					this.otherArgs.add(message);
				}
				
			}else if(controlString.equals(PROXIMITY_ALERT)){
				//alert the user to the fact that we have gotten close or moved away from some point
				logger.debug("Notify Response created.");
				this.type = NOTIFY;
				
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
				this.type = MICROPHONE_ARGS;
			}else if (argType == DATA_TYPE_CAMERA_ARGS){
				this.type = CAMERA_ARGS;
			}else if (argType == DATA_TYPE_ENVIRONMENT_ARGS){
				this.type = ENVIRONMENT_ARGS;
			}else if (argType == DATA_TYPE_LOCATION_ARGS){
				this.type = LOCATION_ARGS;
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
			//send the audio to an audio buffer
			logger.debug("Stream task created.");
			this.type = STREAM;
			this.fileSize = data.length;
			this.fileData = data;
			break;
		case (DATA_TYPE_IMAGE):
			//Save the image as a file
			logger.debug("Save file task created.");
			this.type = SAVE_FILE;
			this.fileSize = data.length;
			this.fileData = data;
			break;
		default:
			logger.error("This argument type is not supported: " + argType);
		}
	}
}
