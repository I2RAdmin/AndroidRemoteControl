package com.i2r.androidremotecontroller.main;

import java.util.Iterator;
import java.util.LinkedList;

import ARC.Constants;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.i2r.androidremotecontroller.R;
import com.i2r.androidremotecontroller.connections.RemoteConnection;
import com.i2r.androidremotecontroller.exceptions.PacketStitchException;
import com.i2r.androidremotecontroller.sensors.CameraSensor;
import com.i2r.androidremotecontroller.sensors.EnvironmentSensorPool;
import com.i2r.androidremotecontroller.sensors.GenericDeviceSensor;
import com.i2r.androidremotecontroller.sensors.LocationSensor;
import com.i2r.androidremotecontroller.sensors.MicrophoneSensor;
import com.i2r.androidremotecontroller.supportedfeatures.FullFeatureSet;


/**
 * This class models a filtering system for {@link CommandPacket}
 * objects which are parsed from bytes received by the controlling
 * PC. These commands, once parsed, are either executed or put
 * in a queue based on the result of {@link CommandPacket#hasHighPriority()}.
 * If a command has high priority it is executed immediately, otherwise
 * it will be executed after all the commands ahead of it in this filtering
 * system's queue have been executed. Command execution will typically be
 * controlled by this application's {@link RemoteControlMaster}.
 * 
 * @author Josh Noel
 */
public class CommandFilter {
	
	private static final String TAG = "CommandFilter";
	private static final int MAX_COMMAND_CAPACITY = 10;
	
	private Activity activity;
	private LocalBroadcastManager manager;
	private RemoteConnection connection;
	private FullFeatureSet features;
	
	private Camera camera;
	private LinkedList<CommandPacket> commandQueue;
	private LinkedList<CommandPacket> stitchQueue;
	
	private SparseArray<GenericDeviceSensor> sensors;

	
	
	/**
	 * Constructor
	 * Takes all necessary sensor objects as its parameters, so that
	 * it can start and stop them independently of the main activity.
	 * @param activity - the activity that created this command filter
	 * @param camera - the camera from the main activity.
	 * @param holder - the surface holder from the view of the main activity
	 */
	public CommandFilter(Activity activity){
		
		this.activity = activity;
		this.camera = Camera.open();
		this.manager = LocalBroadcastManager.getInstance(activity);
		this.commandQueue = new LinkedList<CommandPacket>();
		this.stitchQueue = new LinkedList<CommandPacket>();
		this.features = new FullFeatureSet(activity, camera);
		this.connection = null;
		
		SurfaceHolder holder = ((SurfaceView) activity.findViewById(R.id.preview)).getHolder();
		
		this.sensors.append(Constants.Commands.TAKE_PICTURE, new CameraSensor(activity, camera, holder));
		this.sensors.append(Constants.Commands.RECORD_AUDIO, new MicrophoneSensor(activity));
		this.sensors.append(Constants.Commands.LISTEN_TO_ENVIRONMENT_SENSORS, new EnvironmentSensorPool(activity));
		this.sensors.append(Constants.Commands.GET_LOCATION, new LocationSensor(activity));
	}
	

	
	/**
	 * Decodes the given String buffer into a {@link CommandPacket}
	 * object array, and adds the contents of the array to the queue
	 * of commands, given that a command in the array is complete
	 * and not blank. If the command is considered high priority,
	 * it is executed immediately.
	 * @param buffer - the String to parse one or more CommandPacket
	 * objects from
	 * @see {@link CommandPacket#isCompleteCommand()}
	 * @see {@link CommandPacket#isCompleteCommand()}
	 */
	public void parseCommand(final String buffer){
		
		// get a new CommandPacket object from the string read by a RemoteConnection
		CommandPacket[] packets = CommandPacket.parsePackets(buffer);
		
		if(packets != null){
			
			// if the packet is legitimate, add it to the queue
			for(int i = 0; i < packets.length; i++){
				filterPacket(packets[i]);
			}
			
			// error parsing received data, revert back to main activity
		} else {
			
			Log.d(TAG, "resulting packet is null, returning to main");
			notifyMain("command(s) not parsed, listening for new commands");
		}
		
	}
	
	
	/**
	 * Helper method for {@link #parseCommand(String)}
	 * @param packet - the packet to probe for information
	 */
	private void filterPacket(CommandPacket packet){
		
		// packet is valid, continue with normal execution
		if(packet != null && packet.isCompleteCommand()){
			
			// high priority packets get executed immediately
			if(packet.hasHighPriority()){
				Log.d(TAG, "executing high priority command:\n" + packet.toString());
				execute(packet);
				
				// low priority packets get placed in queue
			} else {
				Log.d(TAG, "queueing command:\n" + packet.toString());
				commandQueue.add(packet);
			}
			
			// packet is incomplete command, see if it is blank
		} else if(packet != null && !packet.isCompleteCommand()){
			
			// packet is not blank, so it must be partial
			if(!packet.isBlankCommand()){
				Log.d(TAG, "partial command received");
				stitch(packet);
				
				// packet is blank, do nothing
			} else {
				Log.e(TAG, "blank command recieved, no action performed");
			}	
			
			// packet is null, do nothing
		} else {
			Log.e(TAG, "packet is null");
		}
	}
	
	
	/**
	 * Helper method for {@link #parseCommand(String)}
	 * @param packet - the partial packet to attempt
	 * to stitch a complete packet out of.
	 */
	private void stitch(CommandPacket packet){
		if(!stitchQueue.isEmpty()){
			try{
				CommandPacket result = 
						CommandPacket.stitch(stitchQueue.getFirst(), packet);
				commandQueue.add(result);
			} catch (PacketStitchException e){
				Log.e(TAG, "packet stitch failed, dumping packets");
			}
			stitchQueue.remove();
		} else {
			stitchQueue.add(packet);
		}
	}
	
	
	
	/**
	 * Executes the next command in sequence if there are still
	 * commands queued. If the command is for a process that is
	 * already running or there are no commands left in the queue,
	 * this method does nothing.
	 */
	public void executeNextCommand(){
		
		// if there are commands left in the queue to execute
		if (!commandQueue.isEmpty()) {
			
			int taskID = commandQueue.get(0).getTaskID();
			
			// check to make sure command can be executed
			if (isAvailableService(commandQueue.get(0).getCommand())) {
				
				// notify UI and log that a new task is starting
				String result = "executing command - taskID: " + taskID;
				Log.d(TAG, result);
				
				// start the new task by filtering what kind of task it is
				execute(commandQueue.get(0));
				commandQueue.remove(0);
				
			} else {
				Log.d(TAG, "service not available. TASK ID - " + taskID);
			}
		} else {
			Log.d(TAG, "no more commands to execute in queue");
			notifyMain("listening for commands");
		}
	}
	
	
	
	/**
	 * Executes the given command packet only if
	 * the current connection to a controlling device
	 * is not null and the given command packet is not null.
	 */
	private void execute(CommandPacket packet){
		if(connection != null && packet != null){
			Log.d(TAG, "filtering packet...");
			filter(packet);
		} else {
			Log.e(TAG, "no connection found, packet filtering aborted");
		}

	}
	
	
	// helper method for the execute(CommandPacket) method above
	// to be called only if this connection is not null
	private void filter(CommandPacket packet){
		
		if(packet.isTaskStarter()){
			try{
				GenericDeviceSensor sensor = sensors.get(packet.getCommand());
				sensor.setConnection(connection);
				sensor.startNewTask(packet.getTaskID(),
					packet.getIntParameters());
			} catch(Exception e){
				Log.e(TAG, "command is undefined");
				ResponsePacket.getNotificationPacket(packet.getTaskID(),
						Constants.Notifications.TASK_ERRORED_OUT,
						"command is unknown").send(connection);
			}
		} else {
			filterSpecialCommand(packet);
		}	 

	}
	
	
	// FILTER HELPER METHODS ------------------------------|
	// the following methods are called based on the switch
	// statements defined in the filter method above
	
	
	/**
	 * Called if a special command was sent from the
	 * controlling device.
	 * @param packet - the packet containing a special command
	 * @see {@link Constants#Commands}
	 */
	private void filterSpecialCommand(CommandPacket packet){
		
		switch(packet.getCommand()){
		
		// modify a currently running task
		case Constants.Commands.MODIFY:
			modifyService(packet);
			break;
			
		// command to kill a process by task ID
		case Constants.Commands.KILL:
			killServicesByTaskID(packet);
			break;
			
			
		// command to stop all processes
		case Constants.Commands.KILL_EVERYTHING:
			killAllTasks(packet.getTaskID());
			break;
			
		// query for supported features on a given sensor
		case Constants.Commands.SUPPORTED_FEATURES:
			findSupportedFeatures(packet);
			break;
			
		// case is unknown, blow up in controller's face
		default:
			Log.e(TAG, "command is undefined");
			ResponsePacket.getNotificationPacket(packet.getTaskID(),
					Constants.Notifications.TASK_ERRORED_OUT,
					"command is unknown").send(connection);
			break;
		}
	}
	
	
	/**
	 * FILTER CASE MODIFY:
	 * Called if a command to modify a service was
	 * received from the controller PC
	 * @param packet - the CommandPacket containing which
	 * service to modify, and what parameters should be modified
	 * in that service
	 */
	private void modifyService(CommandPacket packet){
		
		if(packet.hasExtraStringParameters() && packet.hasExtraIntParameters()){
			GenericDeviceSensor sensor = sensors.get(packet.getInt(Constants.Args.CP_SENSOR_INDEX));
			
			if(sensor != null){
				
				Log.d(TAG, "modifying sensor: " + sensor.getName());
				String[] params = packet.getStringParameters();
				
				// key : value modifications
				for(int i = Constants.Args.KEY_VALUE_START_INDEX; i < params.length - 1; i += 2){
					sensor.modify(params[i], params[i+1]);
				}
				
				sensor.updateSensorProperties(packet.getTaskID());
				
				ResponsePacket.getNotificationPacket(packet.getTaskID(),
						Constants.Notifications.TASK_COMPLETE).send(connection);
				
			} else {
				ResponsePacket.getNotificationPacket(packet.getTaskID(),
						Constants.Notifications.TASK_ERRORED_OUT, 
						"sensor not found").send(connection);
			}
			
		} else {
			String result = "correct parameters not found in command";
			Log.e(TAG, result);
			ResponsePacket.getNotificationPacket(packet.getTaskID(),
					Constants.Notifications.TASK_ERRORED_OUT, result).send(connection);
		}
	}
	
	
	
	/**
	 * FILTER CASE KILL BY TASK ID:
	 * Called if a command to kill specific services was recieved
	 * from the controller PC. 
	 * @param packet - the CommandPacket containing the services to kill
	 */
	private void killServicesByTaskID(CommandPacket packet){
		
		if(packet.hasExtraIntParameters()){
			
			int[] tasksToKill = packet.getIntParameters();
			
			// search currently running for task to kill
			for(int i = 0; i < sensors.size(); i++){
				GenericDeviceSensor sensor = sensors.valueAt(i);
				if (sensor != null && arrayContainsValue(tasksToKill,
						sensor.getTaskID())) {
					Log.d(TAG, "killing current task: " + sensor.getTaskID());
					sensor.killTask();
				}
			}
 		
			
			// search queue for task to kill
			boolean found = false;
			Iterator<CommandPacket> iter = commandQueue.iterator();

			while (iter.hasNext() && !found) {

				CommandPacket temp = iter.next();

				// iterating through the parameter array multiple times
				// rather than the LinkedList, since iteration through the
				// LinkedList is more expensive
				if (arrayContainsValue(tasksToKill, temp.getTaskID())) {
					Log.d(TAG, "removing task from queue: " + temp.getTaskID());
					iter.remove();
					found = true;
				}
			}
			
			ResponsePacket.getNotificationPacket(packet.getTaskID(),
					Constants.Notifications.TASK_COMPLETE).send(connection);
			
		} else {
			
			Log.e(TAG, "no int parameters found in command");
			ResponsePacket.getNotificationPacket(packet.getTaskID(),
					Constants.Notifications.TASK_ERRORED_OUT).send(connection);
		}

	}
	
	
	
	/**
	 * FILTER CASE KILL EVERYTHING:
	 * Called if command to kill all processes is received from
	 * the remote device.
	 * @param taskID - the task ID of the kill all processes command,
	 * which will be used to inform the remote device that this
	 * kill command completed successfully
	 */
	private void killAllTasks(int taskID){
		cancel();
		ResponsePacket.getNotificationPacket(taskID,
				Constants.Notifications.TASK_COMPLETE).send(connection);
	}
	
	
	

	/**
	 * Called if the given packet is a query for features of this device, so
	 * find out which feature descriptions the controller wants and send them.
	 * @param packet - the packet containing a request for this device's features
	 */
	private void findSupportedFeatures(CommandPacket packet){
		if(packet.hasExtraIntParameters()){
			features.sendFeatures(packet.getIntParameters(),
					packet.getTaskID(), connection);
		} else {
			Log.e(TAG, "no int parameters found in command");
			ResponsePacket.getNotificationPacket(packet.getTaskID(),
				Constants.Notifications.TASK_ERRORED_OUT).send(connection);
		}
	}
	
	
	
	
	/**
	 * Cancels any currently running tasks, clears
	 * any pending tasks from the command queue,
	 * and pauses the state of this controller
	 */
	public void cancel(){
		for(int i = 0; i < sensors.size(); i++){
			GenericDeviceSensor sensor = sensors.valueAt(i);
			if(sensor != null){ 
				sensor.killTask();
				sensor.releaseSensor(); 
			}
		}
		commandQueue.clear();
	}
	
	
	
	/**
	 * Notification helper method - notifies the main activity.
	 * This is used when both the sensors and the master cannot
	 * update main and the responsibility for updating is left
	 * to this class. This is simply for updating the info that
	 * the UI displays about this application's progress with
	 * command execution.
	 * @param notifyType - the type of notification to send back to main
	 */
	private void notifyMain(String message){
		Intent intent = new Intent(RemoteControlReceiver.ACTION_TASK_COMPLETE);
		intent.putExtra(RemoteControlReceiver.EXTRA_INFO_MESSAGE, message);
		manager.sendBroadcast(intent);
	}
	
	
	
	/**
	 * Sets the connection for this responder object.<br>
	 * NOTE: if this is null and there are pending commands
	 * in this filter's queue, the commands will fail to
	 * execute, and will be disposed of.
	 * @param connection - the connection to read and write with
	 */
	public void setConnection(RemoteConnection connection){
		this.connection = connection;
	}
	
	
	
	
	//|*****************************************************|
	//|--------------------- QUERIES -----------------------|
	//|*****************************************************|
	
	
	
	/**
	 * Helper method for finding int values in an array. This
	 * method is more efficient than going through the commandQueue
	 * multiple times with an iterator, as that would require
	 * multiple object instances of creation on the iterator's part.
	 * @param array - the array to search through for the given value
	 * @param value - the value to search for in the given array
	 * @return true if the value given was found in the given array,
	 * false otherwise.
	 */
	public static boolean arrayContainsValue(int[] array, int value){
		boolean hasValue = false;
		for(int i = 0; i < array.length && !hasValue; i++){
			hasValue = array[i] == value;
		}
		return hasValue;
	}
	
	
	/**
	 * Query for the availability of a given process.
	 * @param service - the service to check against the currently
	 * running hardware processes for availability
	 * @return true if the service is available, false otherwise.
	 * @see {@link Constants#Commands}
	 */
	public boolean isAvailableService(int service){
		return service >= 0 || isAvailableService(sensors.get(service));
		
	}

	
	/**
	 * Query for the state of a given sensor
	 * @param sensor - the sensor to test for availability
	 * @return true if the sensor is not null and either its current
	 * task is complete or it has not started any tasks yet, false otherwise.
	 */
	public static boolean isAvailableService(GenericDeviceSensor sensor){
		return sensor != null && (sensor.taskCompleted() || 
				sensor.getTaskID() == Constants.Args.ARG_NONE);
	}
	
	
	/**
	 * Query for a reference to the activity which all the
	 * sensors in this application use.
	 * @return the relative activity (context of this application)
	 */
	public Activity getActivity(){
		return activity;
	}
	
	
	/**
	 * Query asking if this command manager can parse any new commands
	 * at its current state.
	 * @return true if command slots are open in the queue, false otherwise
	 */
	public boolean commandCapacityReached(){
		return commandQueue.size() >= MAX_COMMAND_CAPACITY;
	}
	
	
	/**
	 * Query for the state of this command queue
	 * @return true if command queue has elements, false otherwise
	 */
	public boolean hasNewCommands(){
		return !commandQueue.isEmpty();
	}
	
	
	/**
	 * Query for the availability of the sensor that the
	 * next command in line intends to use
	 * @return true if the next command in line's required
	 * sensor is available, false otherwise.
	 * @see {@link #isAvailableService(int)}
	 */
	public boolean sensorForNextCommandIsAvailable(){
		return isAvailableService(commandQueue.get(0).getCommand());
	}
	
	
	/**
	 * @return a composite boolean result of {@link #hasNewCommands()}
	 * and {@link #sensorForNextCommandIsAvailable()}. Returns true
	 * only if both of these return true.
	 */
	public boolean canExecuteNextCommand(){
		return hasNewCommands() && sensorForNextCommandIsAvailable();
	}

	
}
