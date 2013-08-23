package com.i2r.ARC.PCControl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.data.DataSegment;


/**
 * The {@link Task} class is the basic abstraction of some action for the program to take.  {@link Task}s are almost always various commands
 * sent to a {@link RemoteClient}, which may or may not return with data. In the event that there is data associated with performing this 
 * {@link Task}, it is stored as {@link DataSegment}
 * 
 * Tasks are usually not created for actions that we perform locally, to try and optimize the amount of memory the program uses.
 * 
 * Tasks have three important fields:
 * 	{@link Task#id} is the <code>int</code> task id.  This is a unique identifier for this task.
 * 	{@link Task#command} is the {@link ARCCommand} command.  This is the command that goes along with this {@link Task}.  This is the actual
 * 						job to be done.
 * 	{@link Task#taskData} is a {@link Map} of {@link Integer}s to {@link DataSegment}s that holds on to data associated with this {@link Task}
 * 						Under the typical case, this data is saved to a file when the {@link Task} is completed
 * 
 * @author Johnathan Pagnutti
 *
 */
public class Task {
	
	/**
	 * Logger reference
	 */
	static final Logger logger = Logger.getLogger(Task.class);
	
	/**
	 * The unique <code>int</code> ID that goes with this {@link Task}
	 */
	private int id;
	
	/**
	 * The <code>int</code> position counter.  Keeps track of unique {@link DataSegments} associated with this class, so that
	 * unique {@link DataSegments} will be different values in the {@link Task#taskData} {@link Map}
	 */
	int pos;
	
	/**
	 * The {@link ARCCommand} that abstracts the command associated with this {@link Task}.  The {@link Task} can be considered as an active
	 * performance of this {@link ARCCommand}
	 */
	private ARCCommand command;
	
	/**
	 * The {@link Map} of {@link Integer} positions to {@link DataSegment}s.  This allows a {@link Task} to deal with multipule {@link DataSegment}s
	 * that may or may not be related to eachother.  Seperate key/value pairs in this {@link Map} may refer to data that needs to be saved to
	 * different files, or streams that have different sources, etc.
	 */
	Map<Integer, DataSegment> taskData;
	
	/**
	 * Constructor
	 * 
	 * @param newID a new task ID
	 * @param command a new command
	 */
	public Task(int newID, ARCCommand command){
		//initialize the class fields to the provided values
		this.id = newID;
		this.command = command;
		
		//initialize pos to 0
		this.pos = 0;
		
		//if the command has a header that will return persistent data...
		switch(command.getHeader()){
		case TAKE_PICTURE:
		case RECORD_AUDIO:
		case LISTEN_ENVIRONMENT:
			//initialize the data map
			taskData = new HashMap<Integer, DataSegment>();
			break;
		default:
			//otherwise, don't bother wasting the space
			break;
		}
	}

	/**
	 * Get the task ID
	 * @return the unique ID for this task
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the {@link ARCCommand} associated with this task
	 * @return the command
	 */
	public ARCCommand getCommand() {
		return command;
	}
	
	/**
	 * Save a section of data that has been associated with this task, along with the filetype of that data
	 * 
	 * @param fileType the string that will be appended to the filename dealing with this data
	 * @param data the data to save
	 */
	public void saveChunk(String fileType, byte[] data){
		//if this task has a task data field
		if(taskData != null){
			//attempt to get the data segment at pos
			DataSegment section = taskData.get(pos);
			
			//if that data segment does not exist
			if(section == null){
				//create it
				section = new DataSegment();
				//set the file action
				section.fileType = fileType;
				//append passed in data to the segment
				section.appendData(data);
			}else{
				//append passed in data to the segment
				section.appendData(data);			
			}
			
			//add the new and improved data segment to the map
			taskData.put(pos, section);
		}
	}

	/**
	 * Save a single file from this {@link Task}'s {@link Task#taskData}.  Dumps one {@link DataSegment} to a file.
	 * 
	 * @param dataPos the position of the data segment to save to a file.
	 */
	public void saveFile(int dataPos){
		//get the file data
		DataSegment file = taskData.get(dataPos);
		
		//if we found file data...
		if(file != null){
			//create a new file header string, ala string builder
			StringBuilder sb = new StringBuilder();
			sb.append(id);
			sb.append("_");
			sb.append(dataPos);
			//save the data
			file.saveSegmentAsFile(sb.toString());
		}
	}
	
	/**
	 * Push the data in this task's {@link Task#taskData} to a set of files.
	 * The data path will be the main directory of the eventual code.
	 */
	public void pushAllData() {
		//for each position in the taskData map
		for(Integer dataPos : taskData.keySet()){
			//safe the file
			saveFile(dataPos);
		}
	}

	/**
	 * Clear out this tasks's {@link Task#taskData}.
	 * Does not attempt to save the data, just destroys it.
	 * 
	 * Use with care.
	 */
	public void clearData() {
		//if this task has data...
		if(taskData != null){
			//clear it
			taskData.clear();
		}
		
	}
}
