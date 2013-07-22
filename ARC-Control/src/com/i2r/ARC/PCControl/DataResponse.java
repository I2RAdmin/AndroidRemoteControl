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
	
	public static final String REMOVE_TASK_ARGUMENT = "#";
	public static int REMOVE_TASK = 0;
	public static int SAVE_FILE = 1;
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
	public DataResponse(int taskID, byte[] data){
		//taskID will always be set.
		this.taskID = taskID;
		
		//default set for the arg list
		otherArgs = new ArrayList<String>();
		otherArgs.add(" ");
		
		//figure out what was in the passed block
		interpet(data);
	}

	/**
	 * Private method to interpet the data block sent to the response object.  Sets the appropriate fields as well.
	 * @param data
	 */
	private void interpet(byte[] data) {
		//create a string from the data
		String tempDataString = new String(data);
		
		//TODO: talk with Josh about having arguments be all broken up.
		
		if(tempDataString.equals(REMOVE_TASK_ARGUMENT)){
			//remove the task from the task stack
			this.type = REMOVE_TASK;
			this.otherArgs.add(tempDataString);
		}else{
			//Save it as a file
			this.type = SAVE_FILE;
			this.fileSize = data.length;
			this.fileData = data;
		}
	}
}
