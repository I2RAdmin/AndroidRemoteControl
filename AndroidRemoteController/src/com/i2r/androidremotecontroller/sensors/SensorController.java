package com.i2r.androidremotecontroller.sensors;

import java.util.Iterator;
import java.util.LinkedList;

import ARC.Constants;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;

import com.i2r.androidremotecontroller.CommandPacket;
import com.i2r.androidremotecontroller.RemoteControlActivity;
import com.i2r.androidremotecontroller.ResponsePacket;
import com.i2r.androidremotecontroller.SupportedFeatures;
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
	private LocalBroadcastManager manager;
	private RemoteConnection connection;
	private CameraSensor imageCapture;
	private SurfaceHolder holder;
	private Camera camera;
	private LinkedList<CommandPacket> commandQueue;
	
	
	// Constructor
	public SensorController(Activity activity, Camera camera, SurfaceHolder holder){
		this.activity = activity;
		this.holder = holder;
		this.camera = camera;
		this.manager = LocalBroadcastManager.getInstance(activity);
		this.imageCapture = new CameraSensor(activity, camera, holder);
		this.commandQueue = new LinkedList<CommandPacket>();
		this.connection = null;
	}
	

	
	/**
	 * This method acts as a filter for command packets that are sent in via
	 * this application's open bluetooth socket. The {@link CommandPacket} parsed from
	 * the bytes read from the socket is sent here for interpretation.
	 * Commands are interpreted based on values in the {@link Constants} class.
	 * @param packet - the CommandPacket object to use for filtering
	 * @see {@link CommandPacket#parsePackets(String)}
	 */
	private void execute(final CommandPacket packet){
		if(connection != null && packet != null){
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
			startPictureService(packet);
			break;
			 
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
			killAllServices();
			break;
			
		case Constants.Commands.SUPPORTED_FEATURES:
			findSupportedFeatures(packet);
			break;
			
		default:
			// TODO: make a default
			break;
		}
	}
	
	
	// FILTER HELPER METHODS --------------------------|
	
	private void startPictureService(CommandPacket packet){
		if(imageCapture == null){
			Log.d(TAG, "image capture is null, creating for task : " + packet.getTaskID());
			imageCapture = new CameraSensor(activity, camera, holder);
		}
		imageCapture.setConnection(connection);
		imageCapture.startNewTask(packet.getTaskID(), packet.getStringParameters());
	}
	
	
	
	private void modifyService(CommandPacket packet){
		
		if(packet.hasExtraStringParameters()){
			GenericDeviceSensor sensor = getSensor(packet.getTaskID());
			
			if(sensor != null){
				Log.d(TAG, "modifying task: " + packet.getTaskID());
				String[] params = packet.getStringParameters();
				// key : value modifications
				for(int i = 0; i < params.length - 1; i += 2){
					imageCapture.modify(params[i], params[i+1]);
				}
			}
		} else {
			Log.e(TAG, "no string parameters found in command");
		}
	}
	
	
	
	private void killServicesByTaskID(CommandPacket packet){
		
		if(packet.hasExtraIntParameters()){
			int[] tasksToKill = packet.getIntParameters();
			
			boolean isCamera = arrayContainsValue(tasksToKill,
					imageCapture.getTaskID());
			
			if (imageCapture != null && isCamera) {
				Log.d(TAG, "killing current camera task");
				imageCapture.killTask();

				// TODO: add more sensor processes to stop by task ID here
			}

			if (!isCamera || tasksToKill.length > 1) {
				
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
			}
		} else {
			Log.e(TAG, "no int parameters found in command");
		}
	}
	
	
	public static boolean arrayContainsValue(int[] array, int value){
		
		boolean hasValue = false;
		for(int i = 0; i < array.length && !hasValue; i++){
			hasValue = array[i] == value;
		}
		return hasValue;
	}
	
	
	private void killAllServices(){
		
		if(imageCapture != null){ 
			imageCapture.killTask();
			imageCapture.releaseSensor(); 
			imageCapture = null;
		}
		
		// add all other sensor processes to stop here...
	}
	
	
	
	private synchronized void findSupportedFeatures(CommandPacket packet){
		
		if(packet.hasExtraIntParameters()){
			int[] features = packet.getIntParameters();
			
			for(int i = 0; i < features.length; i++){
				
				switch(features[i]){
				
				case Constants.Args.CAMERA_FEATURES:
					
					Log.d(TAG, "sending supported camera features to controller PC");
					byte[] cameraFeatures = SupportedFeatures.getCameraFeatures(camera);
					ResponsePacket rp = new ResponsePacket(packet.getTaskID(),
							Constants.DataTypes.CAMERA_FEATURES, cameraFeatures);
					ResponsePacket.sendResponse(rp, connection);
					
					break;
					
					// TODO: add more sensors here
					
				default:
					Log.e(TAG, "supported features went to default case");
					break;
				}
			}
		} else {
			Log.e(TAG, "no int parameters found in command");
		}
	}
	
	
	
	
	/**
	 * Cancel any currently running tasks
	 */
	public void cancel(){
		if(imageCapture != null){ 
			imageCapture.killTask();
			imageCapture.releaseSensor(); 
			imageCapture = null;
		}
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
	public boolean isAvailableService(int service){
		boolean result;
		if(service == Constants.Commands.PICTURE){
			result = isServiceAvailable(imageCapture);
		} else {
			result = true;
		}
		return result;
		
	}

	
	
	private static boolean isServiceAvailable(GenericDeviceSensor sensor){
		return sensor != null && (sensor.taskCompleted() || 
				sensor.getTaskID() == Constants.Args.ARG_NONE);
	}
	
	
	
	/**
	 * @param taskID - the task ID to check against all this controllers
	 * current sensors.
	 * @return the sensor which has a matching task ID to the one
	 * given, or null if no sensor with the given task ID was found
	 */
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
	
	
	/**
	 * Sets this sensorController's SurfaceHolder to give to
	 * a camera sensor
	 * @param holder - the holder to obtain a reference of
	 */
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
	
	
	/**
	 * Query for the availability of the sensor that the
	 * next command in line intends to use
	 * @return true if the next command in line's required
	 * sensor is available, false otherwise.
	 */
	public boolean sensorForNextCommandIsAvailable(){
		return isAvailableService(commandQueue.get(0).getCommand());
	}
	
	
	/**
	 * @return a composite of {@link #hasNewCommands()}
	 * and {@link #sensorForNextCommandIsAvailable()}. Returns true
	 * only if both of these return true.
	 */
	public boolean canExecuteNextCommand(){
		return hasNewCommands() && sensorForNextCommandIsAvailable();
	}
	

	
	/**
	 * Decodes the given byte array into a {@link CommandPacket}
	 * object, and adds it to the queue of commands.
	 */
	public void parseCommand(String buffer){
		
		// get a new CommandPacket object from the string read by a RemoteConnection
		CommandPacket[] packets = CommandPacket.parsePackets(buffer);
		
		if(packets != null){
			
			// if the packet is legitimate, add it to the queue
			for(int i = 0; i < packets.length; i++){
				if(packets[i] != null && packets[i].isCompleteCommand()){
					
					if(packets[i].hasHighPriority()){
						Log.d(TAG, "executing high priority command:\n" + packets[i].toString());
						execute(packets[i]);
					} else {
						Log.d(TAG, "queueing command:\n" + packets[i].toString());
						commandQueue.add(packets[i]);
					}
					
					
				} else if(packets[i] != null && !packets[i].isCompleteCommand()){
					
					Log.d(TAG, "partial command received");
					// TODO: hold and wait to stitch
					
				} else {
					Log.e(TAG, "command " + i + " could not be parsed");
					notifyMain(Constants.Args.ARG_NONE);
				}
			}
		}
	}

	
	
	
	/**
	 * Executes the next command in sequence.
	 * If the command is for a process that is already running,
	 */
	public void executeNextCommand(){
		
		// if there are commands left in the queue to execute
		if (!commandQueue.isEmpty()) {
			
			int taskID = commandQueue.get(0).getTaskID();
			
			// check to make sure command can be executed
			if (isAvailableService(commandQueue.get(0).getCommand())) {
				
				// notify UI and log that a new task is starting - TODO: delete info updates
				
				String result = "executing command - taskID:" + taskID;
				Log.d(TAG, result);
				
				// start the new task by filtering what kind of task it is
				execute(commandQueue.get(0));
				commandQueue.remove(0);
				
			} else {
				Log.d(TAG, "service not available. TASK ID - " + taskID);
			}
		} else {
			Log.d(TAG, "no more commands to execute in queue");
			notifyMain(Constants.Args.ARG_NONE);
		}
	}
	
	
	private void notifyMain(int notifyType){
		Intent intent = new Intent(RemoteControlActivity.ACTION_TASK_COMPLETE);
		intent.putExtra(RemoteControlActivity.EXTRA_TASK_ID, notifyType);
		manager.sendBroadcast(intent);
	}
	
}
