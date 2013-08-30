/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.data.DataSegment;

/**
 * Object handles a {@link RemoteClientResponse} object and performs some action based on what fields in the {@link RemoteClientResponse} object have 
 * been filled.  {@link ResponseAction}s have references to the {@link RemoteClient} that started the task that returned a result we
 * need to respond to.
 * 
 * There are several actions we keep track of.  The first one is saving received data to some sort of file.  Saving a file is set to 
 * be its own thread created by the inner {@link SaveDataRunnable}, so that the system does not block on File I/O.
 * 
 * The other main response is to populate the {@link Capabilities} map.
 * 
 * The {@link RemoteClient} performs {@link Task} removal, and the process for that is covered there.
 * @author Johnathan Pagnutti
 *
 */
public class ResponseAction {
	
	static final Logger logger = Logger.getLogger(ResponseAction.class);
	
	/**
	 * The reference to the {@link RemoteClient} that started the {@link Task} that we are responding to
	 */
	RemoteClient dev;
	
	/**
	 * The {@link RemoteClientResponse} object that this action is going to use to attempt to do something
	 */
	RemoteClientResponse response;
	
	/**
	 * A {@link Task} that was referenced by the {@link RemoteClientResponse#taskID}.  Used here as a concrete reference to a particular task,
	 * even if, during processing, that task is removed from the {@link TaskStack}
	 */
	Task referencedTask;
	
	/**
	 * Constructor.
	 * 
	 * @param response the {@link RemoteClientResponse} object that contains the data to use to formulate a response
	 * @param dev the {@link RemoteClient} that started the task that this response is associated with
	 */	
	public ResponseAction(RemoteClientResponse dataResponse, RemoteClient dev) {
		this.response = dataResponse;
		this.dev = dev;
		
		logger.debug("Added: " + dataResponse.taskID + " to the pending task map.");
		
		//if this task is actually in the task stack (some notifications are not)...
		if(dev.deviceTasks.hasTask(dataResponse.taskID)){
			//then add it to the pending tasks map, as there might be some processing to perform
			dev.pendingTaskMap.put(dev.deviceTasks.getTask(dataResponse.taskID), dataResponse);
		}
	}

	/**
	 * Performs the action set in {@link ResponseAction#response}'s {@link RemoteClientResponse#action} with the data provided in the other fields of the 
	 * {@link ResponseAction#response}.
	 * 
	 * If the {@link RemoteClientResponse#action} is invalid, then don't do anything and log an error.
	 */
	public void performAction(){
		//get ready for the longest if/then/else statement of all time
		
		//if the response action is to save a file
		if(response.action == RemoteClientResponse.SAVE_FILE){
			//save the data associated with the response
			saveData();
			
		//if the response action is to stream data
		}else if (response.action == RemoteClientResponse.STREAM){
			//append data associated with a response to the current data associated with a task
			appendData();
			
		//if the response action is to remove a task
		}else if (response.action == RemoteClientResponse.REMOVE_TASK){
			StringBuilder sb = new StringBuilder();
			//check to see if the response has additional data
			
			//if the response's additional data is that the task has errored out...
			if(response.otherArgs.get(0).equals(RemoteClientResponse.TASK_ERRORED_ARGUMENT)){
				
				//tell the user that the task has errored
				sb.append(response.taskID + " has errored out.");
			
			//if the response's additional data is that the task referenced an unsupported sensor...	
			}else if(response.otherArgs.get(0).equals(RemoteClientResponse.UNSUPPORTED_SENSOR)){
				
				//tell the user that the sensor referenced was unsupported
				sb.append(response.taskID + " asked for an unsupported sensor.");
			
			//if the response's additional data is that the task referenced has been successfully compelted...
			}else if(response.otherArgs.get(0).equals(RemoteClientResponse.TASK_COMPLETE_ARGUMENT)){
				
				//tell the user that the task has been complete
				sb.append(response.taskID + " has been compeleted!");
			}
		
			//if there is any additional data...
			if(response.otherArgs.size() > 1){
				
				//for every data element with index > 1, index < size...
				for(int i = 1; i < response.otherArgs.size(); i++){
					//pass it to the UI
					sb.append(response.otherArgs.get(i));
					sb.append('\n');
				}
				
				
			}
			//send it off to the UI
			dev.report(sb.toString());
			
			//remove the task associated with the response
			removeTask();
			
		//if the response action is to notify the user
		//this action is also used for the connection assurance pings
		}else if (response.action == RemoteClientResponse.NOTIFY){
			
			//if the notification is to move to the next picture...
			if(response.otherArgs.get(0).equals(RemoteClientResponse.NEXT_PICTURE)){
				//if the task is in the task stack...
				if(dev.deviceTasks.hasTask(response.taskID)){
					//get a reference to the task
					Task t = dev.deviceTasks.getTask(response.taskID);
					
					// and save the data associated with it
					t.saveFile(t.pos);
					
					// increment the file position
					t.pos++;
				}
			}
			
			//if there is any additional information...
			if(response.otherArgs.size() > 1){
				StringBuilder sb = new StringBuilder();
				//for every data element with index > 1, index < size...
				for(int i = 1; i < response.otherArgs.size(); i++){
					//concatinate it into one message...
					sb.append(response.otherArgs.get(i));
					sb.append("\n");
				}
				

				//report the addititional info to the UI
				dev.report(sb.toString());
			}
			
		//if the response action is to set the camera arguments...
		}else if (response.action == RemoteClientResponse.CAMERA_ARGS){
			//set up the camera sensor with the data in response
			setSensorArguments(Sensor.CAMERA, response);
		
		//if the response action is to set the microphone arguments...
		}else if (response.action == RemoteClientResponse.MICROPHONE_ARGS){
			//set up the microphone sensor with the data in response
			setSensorArguments(Sensor.MICROPHONE, response);
		
		//if the response action is to set the environment arguments...	
		}else if (response.action == RemoteClientResponse.ENVIRONMENT_ARGS){
			//set up the environment sensor with the data in response
			setSensorArguments(Sensor.ENVIRONMENT, response);
		
		//if the response action is to set the location arguments...
		}else if (response.action == RemoteClientResponse.LOCATION_ARGS){
			//set up the location sensor with the data in response
			setSensorArguments(Sensor.LOCATION, response);
		
		//otherwise...
		}else{
			//log an error
			logger.error("The action " + response.action + " is invalid.");
		}
		
		//if the pending task map has a task with this response's ID...
		if(dev.pendingTaskMap.containsKey(response.taskID)){
			//remove it
			logger.debug("Removed " + response.taskID + " from the pending tasks map.");
			dev.pendingTaskMap.remove(response.taskID);
		}
	}
	
	/**
	 * Save the data associated with a {@link Task} to a file, and increment that {@link Task}'s {@link Task#pos} file counter.
	 */
	private void saveData() {
		//get a reference to the task
		Task ref = dev.deviceTasks.getTask(response.taskID);
		
		//if that reference is valid...
		if(ref != null){
			//save the data
			ref.saveFile(ref.pos);
			//increment the position
			ref.pos++;
		}
	}

	/**
	 * The action that removes a task from the stack.
	 * 
	 * This method is called when we get a {@link RemoteClientResponse} with a {@link RemoteClientResponse#action} of type {@link RemoteClientResponse#REMOVE_TASK}
	 * If a task is pending (ie, this side of the connection is currently doing something with the task), the remove waits for the task
	 * to no longer be pending, then removes it from the stack
	 */
	private void removeTask(){
		//tell the remote client remove it from the stack
		dev.removePendingTask(response.taskID);
	}
	
	/**
	 * The action that saves data from a task
	 * 
	 * This method is called when we get a {@link RemoteClientResponse} with a {@link RemoteClientResponse#action} of type {@link RemoteClientResponse#STREAM}.
	 * It is called to save data associated with the {@link RemoteClientResponse#taskID} of a {@link RemoteClientResponse}.  Starts the Save File thread, 
	 * and does not block.
	 */ 
	private void appendData(){
		Thread t = new Thread(new SaveDataRunnable(response));
		t.setName("Append-Data-Thread-" + t.getId());
		t.start();
	}

	/**
	 * The action that sets up a sensor to use.
	 * 
	 * This method is called when we get a {@link RemoteClientResponse} with a {@link RemoteClientResponse#action} of any one of the following types:
	 * {@link RemoteClientResponse#CAMERA_ARGS}
	 * {@link RemoteClientResponse#MICROPHONE_ARGS}
	 * {@link RemoteClientResponse#ENVIRONMENT_ARGS}
	 * {@link RemoteClientResponse#LOCATION_ARGS}
	 * 
	 * Informs this side of the link that there is a {@link Sensor} of the {@link RemoteClientResponse#argType} type, and provides the data 
	 * needed to create a {@link Capabilities} object for that {@link Sensor}, and stores all that data in the {@link RemoteClient#supportedSensors}
	 * so that future commands for that {@link RemoteClient} can check against it.
	 * 
	 * This method needs to run for a particular {@link Sensor} before that {@link Sensor} can be used in any context.
	 * 
	 * @param sensor the sensor we want to set up
	 * @param setArgumentResponse the data response with the data to use to set up a particular sensor
	 */
	private void setSensorArguments(Sensor sensor, RemoteClientResponse setArgumentResponse){
		logger.debug("Getting Sensor Args.");
		logger.debug("Device: " + dev.toString());
		String sensorName;
		
		//get the human readable name of this sensor
		sensorName = sensor.getAlias();

		//if the human readable name is null...
		if(sensorName == null){
			//then this side doesn't support that sort of sensor.
			//tell the user about it, then return from this method
			dev.report((sensor.getAlias() + " is not supported."));
			return;
		}
		
		//inform the user that we're getting setting for the provided sensor
		dev.report("Getting " + sensorName + " settings.");
		
		//for each string in the other argument section of the response...
		for(String line : setArgumentResponse.otherArgs){
			 //break up the string by the packet delimiter
			String[] lineElements = line.split("\n");
			
			//logging loop.
			StringBuilder sb2 =  new StringBuilder();
			sb2.append("Line:\n");
			for(String element : lineElements){
				sb2.append(element);
				sb2.append(" ");
			}
			logger.debug(sb2.toString());
			
			//attempt to assign each element of the line to the correct variable.
			try{
				//get the feature name, replacing any spaces with underscores (for UI sanity)
				String featureName = lineElements[0].replace(' ', '_');
				//get the current value of the sensor
				String currentValue = lineElements[1];
			
				//if we have more than 5 total elements (including the previous two), then we have enough data to support the feature
				if(lineElements.length > 5){
					//get the data type of the parameters of this feature
					DataType type = DataType.get(Integer.parseInt(lineElements[2]));
					//get the limit on the valid parameters of this feature
					Limiter limit = Limiter.get(Integer.parseInt(lineElements[3]));
					//get the size of any additional elements that go along with this feature
					int size = Integer.parseInt(lineElements[4]);
			
					List<String> args = new ArrayList<String>(lineElements.length);
					logger.debug("Setting " + (lineElements.length - 4) + " args");
					int i = 0;
			
					//while we have more additional elements to get
					while(i < size){
						//add them to the argument list
						args.add(lineElements[i + 5]);
						i++;
					}
			
					//use a string builder to create the message to send back to the UI
					StringBuilder sb = new StringBuilder();
					
					//append each section of the feature to the message
					sb.append((featureName)).append('\n');
					sb.append("Current value: " + currentValue).append('\n');
					sb.append(type.getAlias()).append('\n');
					sb.append(limit.getAlias()).append('\n');
			
					
					for(String arg : args){
						sb.append(arg);
						sb.append(" ");
					}
					
					//set the parameters for this feature in the remote client's capabilities map
					dev.setSensorParams(sensor, featureName, type, limit, args);
					
					//set the current value of this feature in the device's current value map
					dev.setCurrentValue(sensor, featureName, currentValue);
					
					//report to the user that a new feature for a sensor has been created, and what type of arguments
					//that feature supports.
					dev.report(sb.toString());
				}else{
					//report to the user what the current value is
					dev.report("Currently: " + featureName + ": " + currentValue);
					
					//set the current value of this feature in the device's current value map
					dev.setCurrentValue(sensor, featureName, currentValue);
				}
			}catch(Exception e){
				//something done goofed, so just don't load this feature for this sensor and hope to all higher powers that we still have 
				//enough data to actually use the sensor
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**************
	 * INNER CLASS
	 * @author Johnathan Pagnutti
	 *
	 *
	 * This is the {@link Runnable} implementation that defines how the save data thread is run.  Data that the thread needs is passed 
	 * through this constructor and the {@link Runnable#run()} method defines what the thread does when {@link Thread#start()} is called.
	 * <p>
	 * The save data thread takes a section of data in the {@link RemoteClientResponse#dataBlock} and appends it to a {@link Task}'s {@link DataSegment}
	 * so it can be saved when there is time to do so.
	 **************/
	private class SaveDataRunnable implements Runnable{
		
		/**
		 * The {@link RemoteClientResponse} that spawned this thread
		 */
		private RemoteClientResponse saveResponse;
		
		/**
		 * Constructor!
		 * 
		 * @param response the data response that spawned this thread.  Contains the data we want to save.
		 */
		public SaveDataRunnable(RemoteClientResponse response){
			this.saveResponse = response;
		}
		
		/**
		 * Private internal helper method to get a file type associated with a sensor, through the {@link RemoteClient}'s 
		 * {@link RemoteClient#currentSensorValues} for that sensor.
		 * @param sensor the sensor to check the {@link RemoteClient#currentSensorValues} of
		 * @param fileTypeKey the key to look for in the {@link RemoteClient#currentSensorValues}
		 * @return either the value stored at fileTypeKey of the {@link RemoteClient#currentSensorValues},
		 * 			the <code>String</code> "default",
		 * 			or null if the sensor could not be found
		 */
		private String getFileType(Sensor sensor, String fileTypeKey){
			//if the current value map has values for the data type...
			if(dev.currentSensorValues.containsKey(sensor)){
				//if the camera sensor in the current values map has a value for "picture-format"...
				if(dev.currentSensorValues.get(sensor).containsKey(fileTypeKey)){
					//use the file type specified in the camera's current values map
					return dev.currentSensorValues.get(sensor).get(fileTypeKey);
				}else{
					//otherwise use a default
					logger.debug(fileTypeKey + " not found in " + sensor.getAlias());
					logger.debug("Using default");
					return "default";
				}
			}else{
				//otherwise, log an error, as we don't have a sensor registered, how are we getting data from that sensor?
				logger.error(sensor.getAlias() + " has not been found for this device.");
				//TODO: send some kind of error packet?
				return null;
			}	
		}
		
		
		/**
		 * Run method!  
		 * 
		 * This method handles the grunt work of appending data to a {@link Task}'s {@link Task#taskData}.  This is done by first
		 * checking against the{@link RemoteClient}'s {@link Capabilities} map to see if a particular filetype has been set
		 * for data from a sensor, and if not, defaulting.  Then, the data is appended to the current element in the 
		 * {@link Task#taskData}.
		 * 
		 * @see {@link Runnable#run()}
		 */
		@Override
		public void run() {
			logger.debug("Saving data...");
			//get the task we're adding data too...
			Task ref = dev.deviceTasks.getTask(saveResponse.taskID);
		
			//if we could not get a task....
			if(ref == null){
				//bad news bears
				logger.error("Arrempted to save data with a reference to a task that was not on the stack.");
				//exit out, an error has occurred somewhere
				return;
			}
			
			dev.report("Saving data recived from the remote connection.");
			
			//inital filetype.  This is, essentally, what to use if we couldn't glean a file type from the capabilities object
			// and didn't have a default for that sensor
			String fileType = "raw";
			
			
			logger.debug("Response Type: " + response.argType);
			
			//depending on the argument type...
			switch(response.argType){
			//if the argument type was image data...
			case (RemoteClientResponse.DATA_TYPE_IMAGE):
				//attempt to get a file type associated with the camera...
				fileType = getFileType(Sensor.CAMERA, "picture-format");
				
				//if none was found...
				if(fileType.equals("default")){
					//use a jpeg
					fileType = "jpg";
				}
				break;
			//if the argument type was audio data...
			case (RemoteClientResponse.DATA_TYPE_AUDIO):
				//attempt to get a file type associated with the microphone...
				fileType = getFileType(Sensor.MICROPHONE, "audio-output-format");
				//if none was found...
				if(fileType.equals("default")){
					//use a wav
					fileType = "wav";
				}
				break;
			//if the argument type was environment data...
			case (RemoteClientResponse.DATA_TYPE_ENVIRONMENT):
				//attempt to get a file type associated with the environment...
				fileType = getFileType(Sensor.ENVIRONMENT, "");
				//if none was found...
				if(fileType.equals("default")){
					//use a txt
					fileType = "txt";
				}
				break;
			//if the argument type was location data...
			case (RemoteClientResponse.DATA_TYPE_LOCATION):
				//attempt to get a file type associated with the location...
				fileType = getFileType(Sensor.LOCATION, "");
				//if none was found...
				if(fileType.equals("default")){
					//use a txt
					fileType = "txt";
				}
				break;
			//otherwise
			default:
				//just use "raw" and let and end user figure it out
				fileType = "raw"; 
				break;
			}
			
			//if we encountered some sort of error geting the file type...
			if(fileType == null){
				//TODO: man, I don't know what to do here.  Throw an error?  QQ?
				fileType = "raw";
			}
			
			//save the segment of data we got
			ref.saveChunk(fileType, saveResponse.dataBlock);
			
			StringBuilder sb = new StringBuilder();
			
			//TODO: this clutters up the UI, so either find a better way to report this or just don't.
			sb.append("Data Info: \n");
			sb.append("	From task ID: " + ref.getId() + " \n");
			sb.append("	#: " + ref.pos + " \n");
			sb.append("	As a: " + fileType + " \n");
			
			//report that we saved a segment
			dev.report(sb.toString());
		}
	}
}
