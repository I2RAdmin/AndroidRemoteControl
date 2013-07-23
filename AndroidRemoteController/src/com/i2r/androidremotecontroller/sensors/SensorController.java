package com.i2r.androidremotecontroller.sensors;

import java.util.concurrent.LinkedBlockingQueue;

import ARC.Constants;
import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.i2r.androidremotecontroller.CommandPacket;
import com.i2r.androidremotecontroller.connections.RemoteConnection;


/**
 * This class models a responder to any system calls for updating,
 * starting or ending sensor tasks.
 * @author Josh Noel
 */
public class SensorController {

	private static final String TAG = "SensorController";
	private static final int MAX_COMMAND_CAPACITY = 10;
	
	private Activity activity;
	private RemoteConnection connection;
	private CameraSensor imageCapture;
	private SurfaceHolder holder;
	private Camera camera;
	private LinkedBlockingQueue<CommandPacket> commandQueue;
	
	
	// Constructor
	public SensorController(Activity activity, Camera camera, SurfaceHolder holder){
		this.activity = activity;
		this.holder = holder;
		this.camera = camera;
		this.imageCapture = new CameraSensor(activity, camera, holder);
		this.commandQueue = new LinkedBlockingQueue<CommandPacket>();
		this.connection = null;
	}
	

	
	/**
	 * This method acts as a filter for command packets that are sent in via
	 * this application's open bluetooth socket. The {@link CommandPacket} parsed from
	 * the bytes read from the socket is sent here for interpretation.
	 * Commands are interpreted based on values in the {@link Constants} class.
	 * @param packet - the CommandPacket object to use for filtering
	 * @see {@link CommandPacket#parsePacket(byte[])}
	 */
	private void filterCommand(final CommandPacket packet){
		if(connection != null){
			Log.d(TAG, "filtering packet...");
			filter(packet);
		} else {
			Log.e(TAG, "no connection found, packet filtering aborted");
		}

	}
	
	
	// to be called only if this connection is not null
	private void filter(CommandPacket packet){
		
		// what kind of command are we getting?
		switch(packet.getCommand()){
		
		// command to take pictures
		case Constants.Commands.PICTURE:
			
			// create a new imageCapture task
			if(imageCapture == null){
				Log.d(TAG, "image capture is null, creating for task : " + packet.getTaskID());
				imageCapture = new CameraSensor(activity, camera, holder);
			}
			imageCapture.setConnection(connection);
			imageCapture.startNewTask(packet.getTaskID(), packet.getParameters());
			break;
			 
			
			// modify a currently running task
		case Constants.Commands.MODIFY:
			
			GenericDeviceSensor sensor = getSensor(packet.getTaskID());
			if(sensor != null){
				for(int i = 0; i < packet.getParameters().length; i += 2){
					imageCapture.modify(packet.getParameters()[i], packet.getParameters()[i + 1]);
				}
			}
			
			break;
			
		// command to kill a process by task ID
		case Constants.Commands.KILL:
			if(imageCapture != null && imageCapture.getTaskID() == packet.getTaskID()){
				imageCapture.killTask();
			}
			// add more sensor processes to stop by task ID here
			break;
			
			
		// command to stop all processes
		case Constants.Commands.KILL_EVERYTHING:
			if(imageCapture != null){ imageCapture.killTask(); imageCapture.releaseSensor(); imageCapture = null;}
			// add all other sensor processes to stop here...
			break;
			
		default:
			// TODO: make a default
			break;
		}
	}
	
	
	
	/**
	 * Cancel any currently running tasks
	 */
	public void cancel(){
		if(imageCapture != null){ imageCapture.killTask(); imageCapture.releaseSensor(); imageCapture = null;}
		commandQueue.clear();
	}
	
	
	
	/**
	 * Query for the availability of a given process.
	 * Since all hardware on the android device can
	 * only be used for one task at a time, the objects
	 * representing the hardware can be directly referenced
	 * in this class.
	 * @param taskID - the id to check against the currently
	 * running hardware processes.
	 * @return true if the task ID is unique among running processes or it has
	 * not yet been initialized, false if it matches the ID of an existing process.
	 */
	public boolean isAvailableService(int taskID){
		return sensorIsAvailable(imageCapture, taskID);
		// || or-ed with other sensors that will be added
	}
	
	
	// segmentation of available service query for the image sensor
	public static boolean sensorIsAvailable(GenericDeviceSensor capture, int taskID){
		return (capture == null || taskID != capture.getTaskID() ||
				(taskID == capture.getTaskID() && capture.taskCompleted()));
	}
	
	
	public GenericDeviceSensor getSensor(int taskID){
		GenericDeviceSensor sensor = null;
		if(imageCapture != null && imageCapture.getTaskID() == taskID){
			sensor = imageCapture;
		}
		return sensor;
	}
	
	
	/**
	 * Query for a reference to the activity which all the
	 * sensors in this application use.
	 * @return the relative activity (context of this application)
	 */
	public Activity getRelativeActivity(){
		return activity;
	}
	
	
	/**
	 * Sets the connection for this responder object
	 * @param connection - the connection to read and write with
	 */
	public void setConnection(RemoteConnection connection){
		this.connection = connection;
	}
	
	
	public void setSurfaceHolderForCamera(SurfaceHolder holder){
		this.holder = holder;
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
	
	
	
	public boolean sensorForNextCommandIsAvailable(){
		return isAvailableService(commandQueue.peek().getTaskID());
	}
	
	
	public boolean canExecuteNextCommand(){
		return hasNewCommands() && sensorForNextCommandIsAvailable();
	}
	

	
	/**
	 * Decodes the given byte array into a {@link CommandPacket}
	 * object, and adds it to the queue of commands.
	 */
	public void parseCommand(String read){
		
		// get a new CommandPacket object from the string read by bluetooth socket
		CommandPacket packet = CommandPacket.parsePacket(read);
		
		// if the packet is legitimate, add it to the queue
		if(packet != null && packet.isCompleteCommand()){
			commandQueue.add(packet);
			Log.d(TAG, "new command received:\n" + packet.toString());
		}
	}

	
	
	
	/**
	 * Executes the next command in sequence.
	 * If the command is for a process that is already running,
	 * 
	 */
	public void executeNextCommand(){
		
		// if there are commands left in the queue to execute
		if (!commandQueue.isEmpty()) {
			
			// check task ID to make sure that sensor isn't already being used
			int taskID = commandQueue.peek().getTaskID();
			if (isAvailableService(taskID)) {
				
				// notify UI and log that a new task is starting
				String result = "executing command - taskID:" + taskID;
				Log.d(TAG, result);
				
				// start the new task by filtering what kind of task it is
				filterCommand(commandQueue.poll());
				
			} else {
				Log.d(TAG, "service not available. TASK ID - " + taskID);
			}
		}
	}
	
}
