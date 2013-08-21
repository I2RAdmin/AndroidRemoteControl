/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Object handles a {@link DataResponse} object and performs some action based on what fields in the {@link DataResponse} object have 
 * been filled.  {@link ResponseAction}s have references to the {@link RemoteClient} that started the task that returned a result we
 * need to respond to, and the {@link Cotroller} so they can access the UI.
 * 
 * There are several actions we keep track of.  The first one is saving recieved data to some sort of file.  Saving a file is set to 
 * be its own thread created by the inner {@link SaveDataRunnable}, so that the system does not block on File I/O.
 * 
 * The other main response is to populate the {@link Capabilities} map
 * @author Johnathan Pagnutti
 *
 */
public class ResponseAction {
	
	static final Logger logger = Logger.getLogger(ResponseAction.class);
	
	/**
	 * The reference to the {@link Controller} so that this object can access the {@link TaskStack}
	 */
	Controller cntrl;
	
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
	 */	
	public ResponseAction(DataResponse dataResponse, RemoteClient dev) {
		this.response = dataResponse;
		cntrl = Controller.getInstance();
		this.dev = dev;
		
		logger.debug("Added: " + dataResponse.taskID + " to the pending task map.");
		dev.responseMap.put(dev.deviceTasks.getTask(dataResponse.taskID), dataResponse);
	}

	/**
	 * Performs the action set in {@link ResponseAction#response}'s {@link DataResponse#type} with the data provided in the other fields of the 
	 * {@link ResponseAction#response} through a private method call in this {@link ResponseAction}.
	 * If the {@link DataResponse#type} is invalid, then don't do anything and log an error.
	 */
	public void performAction(){
		//dev.deviceTasks.processingTaskLock.set(true);
		
		if(response.type == DataResponse.SAVE_FILE){
			saveData();
		}else if (response.type == DataResponse.STREAM){
			saveData();
		}else if (response.type == DataResponse.REMOVE_TASK){
			if(response.otherArgs.get(0).equals(DataResponse.TASK_ERRORED_ARGUMENT)){
				cntrl.ui.write(response.taskID + " has errored out.");
				
			}else if(response.otherArgs.get(0).equals(DataResponse.UNSUPPORTED_SENSOR)){
				cntrl.ui.write(response.taskID + " asked for an unsupported sensor.");
			}
		
			if(response.otherArgs.size() > 1){
				for(int i = 1; i < response.otherArgs.size(); i++){
					cntrl.ui.write(response.otherArgs.get(i));
				}
			}
			
			removeTask();
		}else if (response.type == DataResponse.NOTIFY){
			if(response.otherArgs.size() > 1){
				for(int i = 1; i < response.otherArgs.size(); i++){
					cntrl.ui.write(response.otherArgs.get(i));
				}
			}
		}else if (response.type == DataResponse.CAMERA_ARGS){
			setCameraArgs();
		}else if (response.type == DataResponse.MICROPHONE_ARGS){
			setMicArgs();
		}else if (response.type == DataResponse.ENVIRONMENT_ARGS){
			setEnvironmentArgs();
		}else if (response.type == DataResponse.LOCATION_ARGS){
			setLocationArgs();
		}else{
			logger.error("The type " + response.type + " is invalid.");
		}
		
		logger.debug("Removed " + response.taskID + " from the pending tasks map.");
		dev.responseMap.remove(response.taskID);
	}
	
	private void setEnvironmentArgs() {
		setSensorArguments(Sensor.ENVIRONMENT, response);
	}

	private void setMicArgs(){
		setSensorArguments(Sensor.MICROPHONE, response);
	}
	
	private void setCameraArgs() {
		setSensorArguments(Sensor.CAMERA, response);
	}

	private void setLocationArgs(){
		setSensorArguments(Sensor.LOCATION, response);
	}
	/**
	 * The action that removes a task from the stack.  This action should be called when {@link ResponseAction#response} follows the form
	 * of a {@link DataResponse} that would call for task removal.
	 */
	private void removeTask(){
		logger.debug("Removing a task with ID " + response.taskID + " from the stack.");
		dev.removePendingTask(response.taskID);
	}
	
	private void saveData(){
		Thread t = new Thread(new SaveDataRunnable(response));
		t.start();
	}

	private void setSensorArguments(Sensor sensor, DataResponse setArgumentResponse){
		logger.debug("Getting Sensor Args.");
		String sensorName;
		logger.debug("Device: " + dev.toString());
		
		switch(sensor){
		case CAMERA:
			sensorName = Sensor.CAMERA.getAlias();
			break;
		case MICROPHONE: 
			sensorName = Sensor.MICROPHONE.getAlias();
			break;
		case ENVIRONMENT:
			sensorName = Sensor.ENVIRONMENT.getAlias();
			break;
		case LOCATION:
			sensorName = Sensor.LOCATION.getAlias();
			break;
		default:
			cntrl.ui.write(sensor.getAlias() + " is not supported.");
			return;
		}
		
		cntrl.ui.write("Getting " + sensorName + " settings.");
		
		for(String line : setArgumentResponse.otherArgs){
			String[] lineElements = line.split("\n");
			
			StringBuilder sb2 =  new StringBuilder();
			for(String element : lineElements){
				sb2.append(element);
			}
			logger.debug(sb2.toString());
			
			String featureName = lineElements[0].replace(' ', '_');
			String currentValue = lineElements[1];
			
			if(lineElements.length > 4){
				DataType type = DataType.get(Integer.parseInt(lineElements[2]));
				Limiter limit = Limiter.get(Integer.parseInt(lineElements[3]));
				int size = Integer.parseInt(lineElements[4]);
			
				List<String> args = new ArrayList<String>(lineElements.length);
				logger.debug("Setting " + (lineElements.length - 4) + " args");
				int i = 0;
			
				while(i < size){
					args.add(lineElements[i + 5]);
					i++;
				}
			
				cntrl.ui.write(featureName);
				cntrl.ui.write("Current value: " + currentValue);
				cntrl.ui.write(type.getAlias());
				cntrl.ui.write(limit.getAlias());
			
				StringBuilder sb = new StringBuilder();
				for(String arg : args){
					sb.append(arg);
					sb.append(" ");
				}
				cntrl.ui.write(sb.toString());
				
				dev.setSensorParams(sensor, featureName, type, limit, args);
				dev.setCurrentValue(sensor, featureName, currentValue);
			}else{
				cntrl.ui.write("Currently: ");
				cntrl.ui.write(featureName + ": " + currentValue);
			}
		}
		
	}
	
	
	private class SaveDataRunnable implements Runnable{
		
		private DataResponse saveResponse;
		
		public SaveDataRunnable(DataResponse response){
			this.saveResponse = response;
		}
		
		@Override
		public void run() {
			logger.debug("Saving file...");
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
			
			if(response.type == DataResponse.SAVE_FILE){
				ref.pos++;
			}
		}
	}
}
