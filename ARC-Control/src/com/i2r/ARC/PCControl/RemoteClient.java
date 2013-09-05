/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * <p>
 * Devices are created from a {@link RemoteLink} along with the connection URL.  Devices are now responsible for the actual connections.
 * Actually, {@link RemoteClient}'s are even bigger than that.  They manage their own {@link TaskStack}, handle their own {@link RemoteClientResponse}s
 * and perform their own {@link ResponseAction}s.  Essentally, a {@link RemoteClient} is an abstract of the entire control structure of
 * how to handle data from a device.  This allows the {@link Controller} to handle more than one device.
 * <p>
 * {@link RemoteClient}'s are also responsible for handling the connection healthiness.  The {@link RemoteClient} is specified to
 * ping the connection every so often to make sure the connection is still alive.  If it fails, the Remote client starts attempting
 * to reconnect.
 * <p>
 * {@link RemoteClient}'s also report out to the UI about various things going on the end user might want to be aware of.
 * 
 * @author Johnathan Pagnutti
 *
 */
public class RemoteClient {
	/**
	 * The amount of times to attempt to reconnect through a connection before it is declared dead
	 */
	private static final int RECONNECT_ATTEMPTS = 5;
	
	/**
	 * The amount of milliseconds to wait before sending another quality of service ping
	 */
	private static final int PING_INTERVAL = 60000;
	
	/**
	 * LOGGER SWARM.  OH GOD WHY.  WHY.
	 */
	static final Logger logger = Logger.getLogger(RemoteClient.class);
	
	/**
	 * The {@link RemoteLink} that will be used to create a {@link RemoteConnection} to this {@link RemoteClient}.  
	 */
	RemoteLink<byte[]> link;
	
	/**
	 * The {@link RemoteConnection} that will be used to actually connect to the remote device
	 */
	RemoteConnection<byte[]> conn;
	
	/**
	 * The {@link DataManager} that will handle writing and reading to the {@link RemoteConnection}.
	 * Reading, in this case, includes parsing and responding to parsed statements
	 */
	DataManager<Task, byte[]> dataManager;
	
	/**
	 * The string that specifies, exactly, what we need to connect to.  This is anything from an IP address, to a
	 * DNS HTTP URL, to a Serial-to-USB port number, it all depends on what the {@link RemoteLink#currentConnections()} returns.  
	 */
	String connString;
	
	/**
	 * This is the map of {@link Sensor}'s to {@link Capabilities}.  Elements are added to this map when a remote device responds
	 * to a {@link CommandHeader#GET_SENSOR_FEATURES} command for a particular {@link Sensor}.
	 * 
	 * If a device does not have a {@link Sensor} stored in this map, it can not use that sensor in any context, so the key set
	 * of this map doubles as the list of sensors the PC Client currently knows about on a remote device.
	 */
	Map<Sensor, Capabilities> supportedSensors;
	
	/**
	 * This is a map of {@link Sensor}s on a remote device, to a submap of parameters that sensor supports to the value currently
	 * set for that parameter.
	 * <p>
	 * (parameters may also be called features in other documentation).
	 * <p>
	 * This map is queried if a particular {@link CommandHeader} needs to use the currently known value of some parameter for
	 * some {@link Sensor}.  When the user asks for what the current value of some {@link Sensor} parameter is, a {@link CommandHeader#GET_SENSOR_FEATURES}
	 * command is sent to the remote client, and this map is updated, instead of this map being queried.
	 */
	Map<Sensor, Map<String, String>> currentSensorValues;
	
	/**
	 * This is a map of {@link Task}s to {@link RemoteClientResponse}'s associated with those tasks.  If some {@link RemoteClientResponse} needs
	 * to perform some sort of operation on this side of the connection, we want to make sure that it isn't removed from the
	 * {@link RemoteClient#deviceTasks} before we're done processing on this side.
	 * <p>
	 * So, the {@link RemoteClientResponse} is stored here while we're processing it, then released so it can be removed from the {@link TaskStack}
	 * safely.
	 */
	Map<Task, RemoteClientResponse> pendingTaskMap;
	
	/**
	 * This is the data structure that holds on to any {@link Task} that the user has specified for this {@link RemoteClient}.
	 * {@link Task}s are removed when this {@link RemoteClient} gets a task complete message from the remote device, or when
	 * the user sends a kill command to a remote client, or if the {@link RemoteConnection} fails and can not be reestablished.
	 * <p>
	 * This data structure is used at several points.  Notably, under normal shutdown procedures, the program will not exit if
	 * any {@link Task}'s remain in this data structure for any remote client.
	 */
	TaskStack deviceTasks;
	
	/**
	 * Reference to the {@link Controller}, so this Remote Client can access the {@link Controller#ui} data streams to notify
	 * the user of various messages.
	 * <p>
	 * Religated to a class variable primeraly for speed concerns.  Rather than calling {@link Controller#getInstance()} every
	 * time we want to write, keeping the reference out here means we only need to perform a quick lookup before calling the
	 * write method
	 */
	Controller cntrl;
	
	/**
	 * Boolean flag to say if this Remote Client is marked for death.  If this is true, then when a connection is lost, the remote
	 * client doesn't bother trying to reconnect.
	 */
	boolean die;
	
	/**
	 * Constructor! 
	 * 
	 * @param link the remote link that will create a {@link RemoteConection} that allows for the PC client to communicate with
	 * the remote device
	 * @param connInfo the string of some sort of connection information needed to actually connect.  
	 * Comes from the {@link RemoteLink#currentConnections()} method.
	 */
	public RemoteClient(RemoteLink<byte[]> link, String connInfo){
		//don't make this client for death (we just created it!)
		die = false;
		
		pendingTaskMap = new ConcurrentHashMap<Task, RemoteClientResponse>();
		deviceTasks = new TaskStack();
		
		supportedSensors = new EnumMap<Sensor, Capabilities>(Sensor.class);
		currentSensorValues = new EnumMap<Sensor, Map<String, String>>(Sensor.class);
		
		this.link = link;
		connString = connInfo;
		
		cntrl = Controller.getInstance();
	}
	
	/**
	 * The method called to actually connect to this {@link RemoteClient}.  Creates a {@link RemoteConnection}, and then a new
	 * {@link ARCDataManager} to manage the I/O streams to the remote device.  The read thread of the {@link ARCDataManager} is
	 * started, and this {@link RemoteClient}'s ping thread is started.
	 * 
	 * @return true if we have valid connection objects, false if otherwise.
	 */
	public boolean connectToDevice(){
		logger.debug("Connecting to remote device at: " + connString);
		logger.debug("With a " + link.getClass().getSimpleName());
		//have the link get the remote connection object for this connection
		conn = link.connect(connString);
		
		//if we have a real remote connection
		if(conn != null){
			//assign a new data manager to the connection
			dataManager = new ARCDataManager(conn, this);
			//start the read thread
			dataManager.read();
			
			//create the ping thread
			Thread t = new Thread(new PingConnectionRunnable(this));
			t.setName("Ping-Thread");
			
			//start it
			t.start();
			
			//tell the user that this remote client has established a connection
			logger.debug("Established Connection");
			report("Established Connection");
			return true;
		}else{
			//otherwise, something has gone wrong.  return false
			return false;
		}
	}
	
	/**
	 * Send a task through the {@link RemoteClient#conn}.
	 * <p>
	 * This creates a new {@link Task}, puts it in the {@link TaskStack}, and performs any actions that this side of the client
	 * needs to do before sending the task (ie: removing {@link Task}s from the {@link TaskStack} before sending kill commands)
	 * <p>
	 * @param command the command to create a new {@link Task} for.
	 * @throws UnsupportedValueException thrown if the {@link ARCCommand} provided attempts to use any sensors that are not in
	 * this {@link RemoteClient}'s {@link Capabilities}
	 */
	public void sendTask(ARCCommand command) throws UnsupportedValueException{
		CommandHeader commandHeader = command.getHeader();
		
		//create a task to go with this command
		Task newTask;
		
		//if the task in question requires us to do something, do it here
		switch(commandHeader){
		case DO_NOTHING:
			//create the task
			newTask = deviceTasks.createTask(command);
			
			//the ping task never generates a response, and as such, needs to be removed from the stack
			//also, ping tasks never have pending data, so they can be removed without checking
			//the pending data map
			this.deviceTasks.removeTask(newTask.getId());
			break;
		case KILL_TASK:
			//create the task
			newTask = deviceTasks.createTask(command);
			
			//remove the task the kill task is stopping from the stack (waiting until it is no longer pending)
			removePendingTask(Integer.parseInt(command.getArguments().get(ARCCommand.KILL_TASK_INDEX)));
			break;
		case MODIFY_SENSOR:
			//one final check to make sure the sensor to modify is in the capabilities map for this remote client
			//the sensor in question
			Sensor sensor = Sensor.get(Integer.parseInt(command.getArguments().get(0)));
			
			//if the sensor listed in the command is not supported by this device...
			if(!this.supportedSensors.containsKey(sensor)){
				throw new UnsupportedValueException(sensor.getAlias() + " is not a valid sensor for this device.");
			}
			
			//create the task
			newTask = deviceTasks.createTask(command);
			break;
		case TAKE_PICTURE:
			//check to make sure the camera is supported for this remote client
			if(!this.supportedSensors.containsKey(Sensor.CAMERA)){
				//if it isn't, throw a new exception
				throw new UnsupportedValueException(Sensor.CAMERA.getAlias() + " is not a valid sensor for this device.");
			}
			
			//create the task
			newTask = deviceTasks.createTask(command);
			break;
		case RECORD_AUDIO:
			//check to make sure the microphone is supported for this remote client
			if(!this.supportedSensors.containsKey(Sensor.MICROPHONE)){
				//if it isn't supported, throw a new exception
				throw new UnsupportedValueException(Sensor.MICROPHONE.getAlias() + " is not a valid sensor for this device.");
			}
			
			//create the task
			newTask = deviceTasks.createTask(command);
			break;
		case GET_LOCATION:
			//check to make sure the location sensor is supported for this remote client
			if(!this.supportedSensors.containsKey(Sensor.LOCATION)){
				//if it isn't, throw a new exception
				throw new UnsupportedValueException(Sensor.LOCATION.getAlias() + " is not a valid sensor for this device.");
			}
			
			//create the task
			newTask = deviceTasks.createTask(command);
			break;
		case LISTEN_ENVIRONMENT:
			//check to make sure the environment sensor is supported for this remote client
			if(!this.supportedSensors.containsKey(Sensor.ENVIRONMENT)){
				//if it isn't, throw a new exception
				throw new UnsupportedValueException(Sensor.ENVIRONMENT.getAlias() + " is not a valid sensor for this device.");
			}
			
			//create the task
			newTask = deviceTasks.createTask(command);
			break;
		default:
			
			//create the task
			newTask = deviceTasks.createTask(command);
			break;
		}
		
		//if we have a valid data manager
		if(dataManager != null){
			//send the task off
			dataManager.write(newTask);
			
			//if the task is not a ping task...
			if(newTask.getCommand().getHeader() != CommandHeader.DO_NOTHING){
				//tell the user that we have created a new task
				report("Sent New Task: " + newTask.getId());
			}
		}else{
			//otherwise, report that the connection might be dead maybe
			report("Task " + newTask.getId() + " not sent, connection may be down.");
			logger.error("Task " + newTask.getId() + " not sent, Data Manager was null.");
			
			//remove the task from the task stack, as it never got sent
			this.deviceTasks.removeTask(newTask.getId());
		}
	}

	/**
	 * Add one feature to the {@link Capabilities} of a {@link Sensor}.  If the {@link Capabilities} for a {@link Sensor}
	 * don't exist yet, then a new {@link Capabilities} is created and the {@link Sensor} is added to the {@link RemoteClient#supportedSensors}
	 * map.
	 * 
	 * @param sensor the sensor to add a parameter to
	 * @param featureName the name of the parameter to add to the sensor's capabilities
	 * @param type the data type of this parameter
	 * @param limit the limits on the values that this parameter can take
	 * @param args any additional information that is required to set the parameter
	 */
	public void setSensorParams(Sensor sensor, String featureName, DataType type, Limiter limit, List<String> args) {
		//get the capabilities of the provided sensor
		Capabilities cap = supportedSensors.get(sensor);
		
		//if there weren't any
		if(cap == null){
			//create a new capabilities object
			cap = new Capabilities();
			//add this sensor to the supported sensors map
			supportedSensors.put(sensor, cap);
			//add this sensor to the current value map
			currentSensorValues.put(sensor, new HashMap<String, String>());
		}
		
		//add a new feature to this sensor's capabilities
		cap.addFeature(featureName, type, limit, args);
	}

	
	/**
	 * Checks a single argument to a sensor.
	 * 
	 * This method is run when the end user requests to send a modify command, and checks to make sure the sensor can be
	 * changed how the end user wants to change it.
	 * 
	 * @param sensor the sensor to modify
	 * @param key the name of some sensor parameter to modify
	 * @param value the value to change the sensor parameter to
	 * 
	 * @return the key/value pair on how this argument needs to be represented to communicate with a remote device if acceptable, otherwise null
	 * 
	 * @throws UnsupportedValueException if the value fails any checks
	 */
	public String[] checkSingleArg(Sensor sensor, String key, String value) throws UnsupportedValueException {
		 return supportedSensors.get(sensor).checkArg(key, value);
	}

	/**
	 * Sets the current value of some sensor feature.
	 * <p>
	 * This method is usually called after a task complete packet is received from a remote client in response to a modify command,
	 * or when getting the initial configuration of a sensor.  This may also be called after a response is recieved when the end
	 * user asks about the current state of some parameter for the sensor.
	 * <p>
	 * In essence, this command is used to make sure that what the PC client thinks the current value is, and what it actually is on the
	 * phone, is up to date.
	 * <p>
	 * @param sensor the sensor that has a parameter we're updating the current value of
	 * @param featureName the parameter of that sensor that we're updating the current value of
	 * @param currentValue the value to set to the current value for sensor's featureName.
	 */
	public void setCurrentValue(Sensor sensor, String featureName,
			String currentValue) {
		currentSensorValues.get(sensor).put(featureName, currentValue);
		
	}

	/**
	 * Send a message to the end user.
	 * <p>
	 * This method is used to update the end user on this {@link RemoteClient}'s current state.  If there is anything a user should
	 * know about that has occured with this {@link RemoteClient}, then this method should be called.  Essentally, just adds a 
	 * header identifying that a message came from this client.
	 * <p>
	 * @param message the message to send to the user
	 */
	public void report(String message){
		//add a header to the message
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(this).append("]\n");
		sb.append(message);
		
		//send it to the UI
		cntrl.ui.write(sb.toString());
	}
	
	
	/**
	 * Removes a {@link Task} from this {@link RemoteClient}'s {@link TaskStack} at {@link RemoteClient#deviceTasks}.
	 * <p>
	 * If a {@link Task} is in this {@link RemoteClient}'s {@link RemoteClient#pendingTaskMap}, then we don't want to remove it from
	 * the {@link TaskStack} yet, and instead the client waits until the task is no longer pending to remove it.
	 * <p>
	 * To ensure that the wait doesn't cause the program to slow down, it's per
	 * <p>
	 * formed in a separate thread.
	 * <p>
	 * @param taskID the task that we want to remove from the task stack
	 */
	public void removePendingTask(int taskID){
		//get the task in question from the task stack
		Task referencedTask = deviceTasks.getTask(taskID);
		
		//if we have a valid task...
		if(referencedTask != null){
			//report to the user that the program intends to remove a task.
			report("Removing task " + taskID + " from " + this + " task stack.");
			
			//create and start the remove task thread
			Thread t = new Thread(new RemovePendingTaskRunnable(referencedTask));
			t.setName("Remove-Task-Thread");
			t.start();

		}else{
			//if we never got a valid task from the stack, we've attempted to remove a task that's already been removed.
			//log the error, but do not report it to the user, as it doesn't always indicate that the program has failed
			logger.error("Arrempted to remove a task with a reference to a task that was not on the stack.");
		}
	}
	
	/**
	 * Attempts to reconnect to a remote device if we've lost the connection.
	 * <p>
	 * Tries {@link RemoteClient#RECONNECT_ATTEMPTS} times, waiting five seconds between each attempt.  If a valid connection can not
	 * be established in that time, the connection is considered dead and this {@link RemoteClient} is removed as a valid client to
	 * connect and send commands to.
	 * <p>
	 * @return true if the connection could be reestablished, false if otherwise
	 */
	public boolean reconnect() {
		//if we weren't supposed to die...
		if(!die){
			//start attempting to reconnect
			for(int i = 0; i < RECONNECT_ATTEMPTS; i++){
				//if we could reconnect...
				if(this.connectToDevice()){
					//tell the user that we've reconnected
					report("connection reestablished");
					//tell the program that we've reconnected
					return true;
				//otherwise...
				}else{
					//wait 5 seconds before trying again.
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		
		//if we never reconnected, tell the program that we were unable to reconnect.
		return false;
	}
	
	/**
	 * This method brings a {@link RemoteClient} down.  Under most cases, if this method is called, its because the connection to a 
	 * {@link RemoteClient} has been lost and can't be reestablished.
	 * <p>
	 * This method removes the {@link RemoteClient} from the {@link Controller#devices} map, so the remote device can no longer be
	 * contacted by the end user.  All pending tasks from this {@link RemoteClient} are ended, making an attempt to save any data
	 * that those tasks happen to be holding on to.
	 * <p>
	 * Marks the client for death, so that no attempts will be made to reconnect with this device
	 */
	public void shutdown() {
		//tell the user that this client is shutting down
		logger.debug(this + " is shuting down hard.");
		report("Going Down... NOW");
		report("All pending data may or may not be lost.");
		
		//mark this client for death, so QoS pings stop and no attempts are made to reconnect with this device
		die = true;
		
		//remove any pending tasks from the task map, without waiting for them to finish
		for(Task t : pendingTaskMap.keySet()){
			pendingTaskMap.remove(t);
		}
		
		//clear the task stack, making an attempt to save data in the tasks.
		deviceTasks.clear();
		
		//close the remote connection
		conn.close();
		
		//remove this device from the controller's devices list
		cntrl.devices.remove(this);
	}

	/**
	 * This method is called when the {@link DataParser} that is associated with this {@link RemoteClient}'s {@link DataManager} at
	 * {@link RemoteClient#dataManager} fails.
	 * <p>
	 * Under the current implementation, we must have read a task ID before we can recover from a parser failure.  In that case,
	 * all data associated with that {@link Task} is cleared, and the {@link Task} is removed from the task stack.  The task is then
	 * sent back over the connection to try again.
	 * <p>
	 * @param state the state of the parser when this method is called
	 * @param taskID the taskID read by the parser, or null if no task ID was read
	 */
	public void respondParserFailure(int state, int taskID) {
		//depending on the parser sate...
		switch(state){
			//if the parser has read a taskID before it failed...
			case ARCDataParser.READ_TASK_ID:
			case ARCDataParser.READ_FILE_SIZE:
			case ARCDataParser.READ_ARGUMENT_TYPE:
				//clear out any data associated with the task
				clearTaskData(taskID);
				//resend it
				resendTask(taskID);
				break;
			//otherwise
			case ARCDataParser.NEW_RESPONSE:
			default:
				//let the failure stand
				break;
		}
		//log that the parser has failed, but don't tell the user as it may recover
		logger.debug("Parser failed in state: " + state);
			
	}

	/**
	 * This method is called when we want to resend a {@link Task} to the Remote Device.
	 * <p>
	 * Behind the scenes, the {@link ARCCommand} that this task was created for is saved, the old {@link Task} is removed
	 * and a new {@link Task} is created.
	 * <p>
	 * There is no guarantee that the new {@link Task} will perform any better than the old one.  Several commands that can
	 * be sent to the remote device require other commands to be sent beforehand (i.e.: modify before sense), and those commands
	 * are not resent along with the new task.
	 * <p>
	 * This method shouldn't be used after receiving a task errored notification from the remote device, as those notifications imply
	 * a larger problem with the task sent.  Either some error has occurred on the remote side, or commands were sent out of order,
	 * or something even bigger.  At any rate, resending the task will almost never be a good plan.
	 * <p>
	 * Currently, this method is used to resend tasks after a parser failure, as letting the parser reset and clear the old stream
	 * might be enough to get a task through.
	 * <p>
	 * @param taskID the ID of the task to resend
	 */
	private void resendTask(int taskID) {
		//get a reference to the task we want to resend
		Task t = deviceTasks.getTask(taskID);
				
		//if the reference isn't null...
		if(t != null){
			//get the ARC Command 
			ARCCommand command = t.getCommand();
			//remove the task from the task stack
			removePendingTask(t.getId());
			try {
				//send the command over the link again
				sendTask(command);
			} catch (UnsupportedValueException e) {
				report("Unable to resend task " + taskID + " after parsing error.");
			}
		}
	}

	/**
	 * This method clears out data associated with a task, making no attempt to save it.  Usually used in context of some error
	 * Occurring.
	 * <p>
	 * @param taskID the task to clear data from.
	 */
	private void clearTaskData(int taskID) {
		//get a reference to the task that we want to clear data from...
		Task t = deviceTasks.getTask(taskID);
			
		//if not null...
		if(t != null){
			//removes all the data from that task
			t.clearData();
		}			
	}
	
	
	public Map<Sensor, Capabilities> getSupportedSensors(){
		return supportedSensors;
	}
	
	
			
	/**********************
	 * INNER CLASS
	 **********************/
	/**
	 * This runnable defines how a remove task thread should run.
	 * <p>
	 * The task that we want to remove is passed to the thread in this runnable's constructor.  The {@link Runnable#run()} method
	 * defines what to do when {@link Thread#start()} is called.
	 * 
	 * @author Johnathan Pagnutti
	 */
	private class RemovePendingTaskRunnable implements Runnable{
		/**
		 * This is the {@link Task} that we want to remove from the outer remote client's {@link TaskStack}
		 */
		Task referencedTask;
		
		/**
		 * Constructor!
		 * 
		 * @param referencedTask the task that this thread is going to remove
		 */
		public RemovePendingTaskRunnable(Task referencedTask) {
			this.referencedTask = referencedTask;
		}

		/**
		 * This method defines how a task is removed from the stack.
		 * <p>
		 * The pending task map is checked to see if it contains the taskID of the task we want to remove.  If it does, the thread
		 * waits for 5 seconds before trying again.  If it doesn't, the task is removed from the task stack.
		 */
		@Override
		public void run() {
			//keep trying, using break statements to escape
			while(true){
				//if the pending task map contains a reference to the task...
				if(pendingTaskMap.containsKey(referencedTask.getId())){
					try{
						//wait five seconds before checking again
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
					continue;
				//otherwise...
				}else{
					//remove the task from the task stack
					deviceTasks.removeTask(referencedTask.getId());
					
					//inform the user what tasks are left on the task stack
					StringBuilder sb = new StringBuilder();
					sb.append("Sent Tasks: \n");
			
					if(deviceTasks.tasksRemaining()){
						sb.append(deviceTasks.logStackState());
					}else{
						sb.append("none");
					}
					
					report(sb.toString());
					break;
				}
			}
		}
	}
	
	
	
	/**********************
	 * INNER CLASS
	 **********************/
	/**
	 * This runnable defines how the ping connection thread runs.
	 * <p>
	 * The {@link RemoteClient} we want to ping is passed in the constructor, and the {@link Runnable#run()} defines how the
	 * {@link Thread@start()} method should be run.
	 * <p>
	 * @author Johnathan Pagnutti
	 *
	 */
	 private class PingConnectionRunnable implements Runnable{
		 
		 /**
		  * The {@link RemoteClient} we want to ping
		  */
		 RemoteClient dev;
		 
		 /**
		  * Constructor!
		  * 
		  * @param dev the remote device we want to ping
		  */
		 public PingConnectionRunnable(RemoteClient dev){
			 this.dev = dev;
		 }
		 
		 /**
		  * This method defines how a {@link RemoteClient} is pinged.
		  * <p>
		  * Every {@link RemoteClient#PING_INTERVAL}, a ping is sent over the connection to ensure that it is still active and alive.
		  * The pings take the form of the {@link CommandHeader#DO_NOTHING} header, with no argument data passed.
		  */
		@Override
		public void run() {
			//as long as a remote client has not been marked for death...
			while(!die){
				//wait the ping interval
				try {
					Thread.sleep(PING_INTERVAL);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
				
				//if the remote client still isn't marked for death...
				if(!die){
					try {
						//send a ping
						dev.sendTask(ARCCommand.fromString(dev, "ping"));
					} catch (UnsupportedValueException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	 }
}
