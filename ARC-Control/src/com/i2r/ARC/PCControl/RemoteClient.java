/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.DataManager.ARCDataManager;
import com.i2r.ARC.PCControl.DataManager.ARCDataParser;
import com.i2r.ARC.PCControl.DataManager.DataManager;
import com.i2r.ARC.PCControl.DataManager.DataParser;
import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;

/**
 * The capabilities object is part of the {@link RemoteClient} which is the {@link RemoteLink} that got the connection, the 
 * actual {@link RemoteConnection} that has the I/O streams to the device, the {@link DataManager} that is currently handling that connection,
 * along with its {@link DataParser}.
 * 
 * Devices are created from a {@link RemoteLink} along with the connection URL.  Devices are now responsible for the actual connections.
 * Actually, {@link RemoteClient}'s are even bigger than that.  They manage their own {@link TaskStack}, handle their own {@link DataResponse}s
 * and perform their own {@link ResponseAction}s.  Essentally, a {@link RemoteClient} is an abstract of the entire control structure of
 * how to handle data from a device.  This allows the {@link Controller} to handle more than one device.
 * 
 * @author Johnathan Pagnutti
 *
 */
public class RemoteClient {
	private static final int RECONNECT_ATTEMPTS = 5;
	static final Logger logger = Logger.getLogger(RemoteClient.class);
	
	RemoteLink<byte[]> link;
	RemoteConnection<byte[]> conn;
	DataManager<Task, byte[]> dataManager;
	
	String connectionURL;
	Map<Sensor, Capabilities> supportedSensors;
	Map<Sensor, Map<String, String>> currentSensorValues;
	
	Map<Task, DataResponse> responseMap;
	TaskStack deviceTasks;
	
	AtomicBoolean retrievedCapabilities;	
	Controller cntrl;
	
	boolean die;
	
	public RemoteClient(RemoteLink<byte[]> link, String URL){
		die = false;
		
		responseMap = new ConcurrentHashMap<Task, DataResponse>();
		deviceTasks = new TaskStack();
		
		supportedSensors = new EnumMap<Sensor, Capabilities>(Sensor.class);
		currentSensorValues = new EnumMap<Sensor, Map<String, String>>(Sensor.class);
		
		retrievedCapabilities = new AtomicBoolean(false);
		
		this.link = link;
		connectionURL = URL;
		
		cntrl = Controller.getInstance();
	}
	
	public boolean connectToDevice(){
		logger.debug("Connecting to remote device at: " + connectionURL);
		logger.debug("With a " + link.getClass().getSimpleName());
		conn = link.connect(connectionURL);
		
		if(conn != null){
			dataManager = new ARCDataManager(conn, this);
			dataManager.read();
			Thread t = new Thread(new PingConnectionRunnable(this));
			t.start();
			
			logger.debug("Established Connection");
			report("Established Connection");
			return true;
		}else{
			return false;
		}
	}
		
	public void sendTask(ARCCommand command) throws UnsupportedValueException{
		CommandHeader commandHeader = command.getHeader();
		
		Task newTask = deviceTasks.createTask(command);
		cntrl.ui.write("Task: " + newTask.getId());
		
		//if the task in question requires us to do something, do it here
		switch(commandHeader){
		case DO_NOTHING:
			//the ping task never generates a response, and as such, needs to be removed from the stack
			//also, ping tasks never have pending data, so they can be removed without checking
			this.deviceTasks.removeTask(newTask.getId());
			break;
		case KILL_TASK:
			removePendingTask(Integer.parseInt(command.getArguments().get(ARCCommand.KILL_TASK_INDEX)));
			break;
		case MODIFY_SENSOR:
			Sensor sensor = Sensor.get(Integer.parseInt(command.getArguments().get(0)));
			int i = 1;
			while(i < command.getArguments().size()){
				String featureName = command.getArguments().get(i);
				i++;
				String featureValue = command.getArguments().get(i);
				i++;
				
				this.currentSensorValues.get(sensor).put(featureName, featureValue);
			}
			break;
		case TAKE_PICTURE:
			if(!this.supportedSensors.containsKey(Sensor.CAMERA)){
				throw new UnsupportedValueException(Sensor.CAMERA.getAlias() + " is not a valid sensor for this device.");
			}
			break;
		case RECORD_AUDIO:
			if(!this.supportedSensors.containsKey(Sensor.MICROPHONE)){
				throw new UnsupportedValueException(Sensor.MICROPHONE.getAlias() + " is not a valid sensor for this device.");
			}
			break;
		case GET_LOCATION:
			if(!this.supportedSensors.containsKey(Sensor.LOCATION)){
				throw new UnsupportedValueException(Sensor.LOCATION.getAlias() + " is not a valid sensor for this device.");
			}
			break;
		case LISTEN_ENVIRONMENT:
			if(!this.supportedSensors.containsKey(Sensor.ENVIRONMENT)){
				throw new UnsupportedValueException(Sensor.ENVIRONMENT.getAlias() + " is not a valid sensor for this device.");
			}
			break;
		default:
			break;
		}
		
		if(dataManager != null){
			dataManager.write(newTask);
		}else{
			logger.error("Data Manager has not been found, huh?");
		}
	}

	
	public void setSensorParams(Sensor sensor, String featureName, DataType type, Limiter limit, List<String> args) {
		Capabilities cap = supportedSensors.get(sensor);
		
		if(cap == null){
			cap = new Capabilities();
			supportedSensors.put(sensor, cap);
			currentSensorValues.put(sensor, new HashMap<String, String>());
		}
		
		cap.addFeature(featureName, type, limit, args);
	}

	
	public String checkSingleArg(Sensor sensor, String key, String string) throws UnsupportedValueException {
		 return supportedSensors.get(sensor).checkArg(key, string);
	}

	public void setCurrentValue(Sensor sensor, String featureName,
			String currentValue) {
		currentSensorValues.get(sensor).put(featureName, currentValue);
		
	}

	public void report(String message){
		StringBuilder sb = new StringBuilder();
		sb.append("From ").append(this).append("\n");
		sb.append(message);
		
		cntrl.ui.write(sb.toString());
	}
	
	public void removePendingTask(int taskID){
		Task referencedTask = deviceTasks.getTask(taskID);
		
		if(referencedTask != null){
			report("Removing task " + taskID + " from " + this + " task stack.");
			
			Thread t = new Thread(new RemovePendingTaskRunnable(referencedTask));
			t.start();

		}else{
			logger.error("Arrempted to remove a task with a reference to a task that was not on the stack.");
		}
	}
	
	public boolean reconnect() {
		//if we weren't supposed to die...
		if(!die){
			//start attempting to reconnect
			for(int i = 0; i < RECONNECT_ATTEMPTS; i++){
				if(this.connectToDevice()){
					report("connection reestablished");
					return true;
				}else{
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		
		return false;
	}
	
	 //Going down hard
	public void shutdown() {
		logger.debug(this + " is shuting down hard.");
		report("Going Down... NOW");
		report("All pending data may or may not be lost.");
			
		die = true;
			
		for(Task t : responseMap.keySet()){
			responseMap.remove(t);
		}
				
		deviceTasks.clear();
			
		cntrl.devices.remove(this);
	}

	//the associated ARCParser has failed
	public void respondParserFailure(int state, int taskID) {
		switch(state){
			case ARCDataParser.READ_TASK_ID:
			case ARCDataParser.READ_FILE_SIZE:
			case ARCDataParser.READ_ARGUMENT_TYPE:
				clearTaskData(taskID);
				resendTask(taskID);
				break;
			case ARCDataParser.NEW_RESPONSE:
			default:
				break;
		}
		logger.debug("Parser failed in state: " + state);
			
	}

	private void resendTask(int taskID) {
		Task t = deviceTasks.getTask(taskID);
				
		if(t != null){
			ARCCommand command = t.getCommand();
			removePendingTask(t.getId());
			try {
				sendTask(command);
			} catch (UnsupportedValueException e) {
				report("Unable to resend task " + taskID + " after parsing error.");
			}
		}
				
	}

	private void clearTaskData(int taskID) {
		Task t = deviceTasks.getTask(taskID);
			
		if(t != null){
			t.clearData();
		}			
	}
			
	/**********************
	 * INNER CLASS
	 **********************/
	private class RemovePendingTaskRunnable implements Runnable{
		Task referencedTask;
		
		public RemovePendingTaskRunnable(Task referencedTask) {
			this.referencedTask = referencedTask;
		}

		@Override
		public void run() {
			while(true){
				if(responseMap.containsKey(referencedTask.getId())){
					try{
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
					continue;
				}else{
					deviceTasks.removeTask(referencedTask.getId());
					report("Sent tasks:");
			
					if(deviceTasks.tasksRemaining()){
						report(deviceTasks.logStackState());
					}else{
						report("none");
					}
					break;
				}
			}
		}
	}
	
	
	
	/**********************
	 * INNER CLASS
	 **********************/ 
	 private class PingConnectionRunnable implements Runnable{
		 
		 RemoteClient dev;
		 
		 public PingConnectionRunnable(RemoteClient dev){
			 this.dev = dev;
		 }
		 
		@Override
		public void run() {
			while(!die){
				
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
				
				if(!die){
					try {
						dev.sendTask(ARCCommand.fromString(dev, "ping"));
					} catch (UnsupportedValueException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	 }
}
