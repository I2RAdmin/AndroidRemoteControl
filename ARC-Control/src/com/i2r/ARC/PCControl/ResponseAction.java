/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.data.DataSegment;

/**
 * Object handles a {@link DataResponse} object and performs some action based on what fields in the {@link DataResponse} object have 
 * been filled.  {@link ResponseAction}s have references to the {@link RemoteClient} that started the task that returned a result we
 * need to respond to, and the {@link Controller} so they can access the UI.
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
	 * The reference to the {@link Controller} so that this object can access the {@link TaskStack}
	 */
	Controller cntrl;
	
	/**
	 * The reference to the {@link RemoteClient} that started the {@link Task} that we are responding to
	 */
	RemoteClient dev;
	
	/**
	 * The {@link DataResponse} object that this action is going to use to attempt to do something
	 */
	DataResponse response;
	
	/**
	 * A {@link Task} that was referenced by the {@link DataResponse#taskID}.  Used here as a concrete reference to a particular task,
	 * even if, during processing, that task is removed from the {@link TaskStack}
	 */
	Task referencedTask;
	
	/**
	 * Constructor.  In addition to assigning the passed in value to the {@link ResponseAction#response} field, gets a reference to the
	 * {@link Controller} to use.
	 * 
	 * @param response the {@link DataResponse} object that contains the data to use to formulate a response
	 * @param dev the {@link RemoteClient} that started the task that this response is associated with
	 */	
	public ResponseAction(DataResponse dataResponse, RemoteClient dev) {
		this.response = dataResponse;
		cntrl = Controller.getInstance();
		this.dev = dev;
		
		logger.debug("Added: " + dataResponse.taskID + " to the pending task map.");
		
		//if this task is actually in the task stack (some notifications are not)...
		if(dev.deviceTasks.hasTask(dataResponse.taskID)){
			//then add it to the pending tasks map, as there might be some processing to perform
			dev.responseMap.put(dev.deviceTasks.getTask(dataResponse.taskID), dataResponse);
		}
	}

	/**
	 * Performs the action set in {@link ResponseAction#response}'s {@link DataResponse#action} with the data provided in the other fields of the 
	 * {@link ResponseAction#response}.
	 * 
	 * If the {@link DataResponse#action} is invalid, then don't do anything and log an error.
	 */
	public void performAction(){
		//get ready for the longest if/then/else statement of all time
		
		//if the response action is to save a file
		if(response.action == DataResponse.SAVE_FILE){
			//save the data associated with the response
			saveData();
		//if the response action is to stream data
		}else if (response.action == DataResponse.STREAM){
			//TODO: actually stream the data to a thing
			//save the data associated with the response
			saveData();
		//if the response action is to remove a task
		}else if (response.action == DataResponse.REMOVE_TASK){
			//check to see if the response has additional data
			
			//if the response's additional data is that the task has errored out...
			if(response.otherArgs.get(0).equals(DataResponse.TASK_ERRORED_ARGUMENT)){
				
				//tell the user that the task has errored
				cntrl.ui.write(response.taskID + " has errored out.");
			
			//if the response's additional data is that the task referenced an unsupported sensor...	
			}else if(response.otherArgs.get(0).equals(DataResponse.UNSUPPORTED_SENSOR)){
				
				//tell the user that the sensor referenced was unsupported
				cntrl.ui.write(response.taskID + " asked for an unsupported sensor.");
			}
		
			//if there is any additional data...
			if(response.otherArgs.size() > 1){
				//for every data element with index > 1, index < size...
				for(int i = 1; i < response.otherArgs.size(); i++){
					//pass it to the UI
					cntrl.ui.write(response.otherArgs.get(i));
				}
			}
			
			//remove the task associated with the response
			removeTask();
			
		//if the response action is to notify the user
		//this action is also used for the connection assurance pings
		}else if (response.action == DataResponse.NOTIFY){
			
			//if the notification is to move to the next picture...
			if(response.otherArgs.get(0).equals(DataResponse.NEXT_PICTURE)){
				//if the task is in the task stack...
				if(dev.deviceTasks.hasTask(response.taskID)){
					//get a reference to the task
					Task t = dev.deviceTasks.getTask(response.taskID);
					
					// and save the data associated with it
					t.pushAllData();
					
					// increment the file position
					t.pos++;
				}
			}
			
			//if there is any additional information...
			if(response.otherArgs.size() > 1){
				//for every data element with index > 1, index < size...
				for(int i = 1; i < response.otherArgs.size(); i++){
					//pass it to the UI
					cntrl.ui.write(response.otherArgs.get(i));
				}
			}
		
		//if the response action is to set the camera arguments...
		}else if (response.action == DataResponse.CAMERA_ARGS){
			//set up the camera sensor with the data in response
			setSensorArguments(Sensor.CAMERA, response);
		
		//if the response action is to set the microphone arguments...
		}else if (response.action == DataResponse.MICROPHONE_ARGS){
			//set up the microphone sensor with the data in response
			setSensorArguments(Sensor.MICROPHONE, response);
		
		//if the response action is to set the environment arguments...	
		}else if (response.action == DataResponse.ENVIRONMENT_ARGS){
			//set up the environment sensor with the data in response
			setSensorArguments(Sensor.ENVIRONMENT, response);
		
		//if the response action is to set the location arguments...
		}else if (response.action == DataResponse.LOCATION_ARGS){
			//set up the location sensor with the data in response
			setSensorArguments(Sensor.LOCATION, response);
		
		//otherwise...
		}else{
			//log an error
			logger.error("The action " + response.action + " is invalid.");
		}
		
		//if the pending task map has a task with this response's ID...
		if(dev.responseMap.containsKey(response.taskID)){
			//remove it
			logger.debug("Removed " + response.taskID + " from the pending tasks map.");
			dev.responseMap.remove(response.taskID);
		}
	}
	
	/**
	 * The action that removes a task from the stack.
	 * 
	 * This method is called when we get a {@link DataResponse} with a {@link DataResponse#action} of type {@link DataResponse#REMOVE_TASK}
	 * If a task is pending (ie, this side of the connection is currently doing something with the task), the remove waits for the task
	 * to no longer be pending, then removes it from the stack
	 */
	private void removeTask(){
		//tell the remote client remove it from the stack
		//FIXME: not removing tasks right now
		//dev.removePendingTask(response.taskID);
	}
	
	/**
	 * The action that saves data from a task
	 * 
	 * This method is called when we get a {@link DataResponse} with a {@link DataResponse#action} of type {@link DataResponse#SAVE_FILE}
	 * FIXME: or {@link DataResponse#STREAM}.
	 * It is called to save data associated with the {@link DataResponse#taskID} of a {@link DataResponse}.  Starts the Save File thread, 
	 * and does not block.
	 */ 
	private void saveData(){
		Thread t = new Thread(new SaveDataRunnable(response));
		t.start();
	}

	/**
	 * The action that sets up a sensor to use.
	 * 
	 * This method is called when we get a {@link DataResponse} with a {@link DataResponse#action} of any one of the following types:
	 * {@link DataResponse#CAMERA_ARGS}
	 * {@link DataResponse#MICROPHONE_ARGS}
	 * {@link DataResponse#ENVIRONMENT_ARGS}
	 * {@link DataResponse#LOCATION_ARGS}
	 * 
	 * Informs this side of the link that there is a {@link Sensor} of the {@link DataResponse#argType} type, and provides the data 
	 * needed to create a {@link Capabilities} object for that {@link Sensor}, and stores all that data in the {@link RemoteClient#supportedSensors}
	 * so that future commands for that {@link RemoteClient} can check against it.
	 * 
	 * This method needs to run for a particular {@link Sensor} before that {@link Sensor} can be used in any context.
	 * 
	 * @param sensor the sensor we want to set up
	 * @param setArgumentResponse the data response with the data to use to set up a particular sensor
	 */
	private void setSensorArguments(Sensor sensor, DataResponse setArgumentResponse){
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
	 * The save data thread takes a section of data in the {@link DataResponse#fileData} and appends it to a {@link Task}'s {@link DataSegment}
	 * so it can be saved when there is time to do so
	 **************/
	private class SaveDataRunnable implements Runnable{
		
		/**
		 * The {@link DataResponse} that spawned this thread
		 */
		private DataResponse saveResponse;
		
		/**
		 * Constructor!
		 * 
		 * @param response the data response that spawned this thread
		 */
		public SaveDataRunnable(DataResponse response){
			this.saveResponse = response;
		}
		
		/**
		 * Run method!  
		 * This 
		 * 
		 * @see {@link Runnable#run()}
		 */
		@Override
		public void run() {
			logger.debug("Saving data...");
			Task ref = dev.deviceTasks.getTask(saveResponse.taskID);
		
			
			if(ref == null){
				//bad news bears
				logger.error("Arrempted to save data with a reference to a task that was not on the stack.");
				return;
			}
			
			cntrl.ui.write("Saving data recived from the remote connection.");
			
			String fileType = "raw";
			
			
			logger.debug("Response Type: " + response.argType);
			
			switch(response.argType){
			case (DataResponse.DATA_TYPE_IMAGE):
				if(dev.currentSensorValues.containsKey(Sensor.CAMERA)){
					if(dev.currentSensorValues.get(Sensor.CAMERA).containsKey("picture-format")){
						fileType = dev.currentSensorValues.get(Sensor.CAMERA).get("picture-format");
					}else{
						logger.debug("Picture format feature not found in " + Sensor.CAMERA.getAlias());
						logger.debug("Using jpeg");
						fileType = "jpeg";
					}
				}else{
					logger.error(Sensor.CAMERA.getAlias() + " has not been found for this device.");
					//TODO: send some kind of error packet?
				}
				break;
			case (DataResponse.DATA_TYPE_AUDIO):
				if(dev.currentSensorValues.containsKey(Sensor.MICROPHONE)){
					if(dev.currentSensorValues.get(Sensor.MICROPHONE).containsKey("audio-output-format")){
						fileType = dev.currentSensorValues.get(Sensor.CAMERA).get("audio-output-format");
					}else{
						logger.debug("Audio format feature not found in " + Sensor.MICROPHONE.getAlias());
						logger.debug("Using wav");
						fileType = "wav";
					}
				}else{
					logger.error(Sensor.MICROPHONE.getAlias() + " has not been found for this device.");
					//TODO: send some kind of error packet?
				}
				break;
			case (DataResponse.DATA_TYPE_ENVIRONMENT):
				if(dev.currentSensorValues.containsKey(Sensor.ENVIRONMENT)){
					if(dev.currentSensorValues.get(Sensor.ENVIRONMENT).containsKey("")){
						fileType = dev.currentSensorValues.get(Sensor.ENVIRONMENT).get("");
					}else{
						logger.debug("Environment format feature not found in " + Sensor.ENVIRONMENT.getAlias());
						logger.debug("Using txt");
						fileType = "txt";
					}
				}
				break;
			default:
				fileType = "raw"; 
				break;
			}
			
			cntrl.ui.write("Data Info: ");
			cntrl.ui.write("	From task ID: " + ref.getId());
			cntrl.ui.write("	#: " + ref.pos);
			cntrl.ui.write("	As a: " + fileType);
			
			ref.saveChunk(fileType, saveResponse.fileData);
		}
	}
}
