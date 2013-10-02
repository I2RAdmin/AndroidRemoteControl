package com.i2r.androidremotecontroller.main;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.i2r.androidremotecontroller.connections.BluetoothLink;
import com.i2r.androidremotecontroller.connections.ConnectionManager;
import com.i2r.androidremotecontroller.connections.RemoteConnection;
import com.i2r.androidremotecontroller.connections.SocketLink;
import com.i2r.androidremotecontroller.exceptions.ServiceNotFoundException;
import com.i2r.androidremotecontroller.main.databouncer.DataBouncer;


/**
 * This class models a {@link BroadcastReceiver} that responds
 * to commands from a remote machine when the commands are broadcast
 * to it from a {@link RemoteConnection} via {@link #ACTION_CONNECTOR_RESPONDED}.
 * @author Josh Noel
 */
public class RemoteControlReceiver extends BroadcastReceiver {

	
	private static final String TAG = "RemoteControlReceiver";
	private static final String DEFAULT_MESSAGE = "inactive";
	
	
	/**
	 * Used for filtering {@link Intent}s in this application. This constant is
	 * called whenever a command filter has finished a short term task.
	 */
	public static final String ACTION_TASK_COMPLETE = "i2r_action_sensor_completed_task";

	/**
	 * Used for filtering {@link Intent}s in this application. This constant is
	 * called whenever a connection manager has established or lost a connection
	 * to the controller.
	 */
	public static final String ACTION_CONNECTOR_RESPONDED = "i2r_action_connection_created";

	/**
	 * Used for filtering {@link Intent}s in this application. This constant is
	 * called a connection manager has received byte data on an open connection
	 * with the controller.
	 */
	public static final String ACTION_CONNECTION_READ = "i2r_action_connection_read";
	
	/**
	 * Extra for {@link #onReceive(Context, Intent)}
	 */
	public static final String EXTRA_TASK_ID = "i2r_extra_task_id";
	
	/**
	 * Extra for {@link #onReceive(Context, Intent)}
	 */
	public static final String EXTRA_COMMAND = "i2r_extra_command";
	
	/**
	 * Extra for {@link #onReceive(Context, Intent)}
	 */
	public static final String EXTRA_RESULT_DATA = "i2r_extra_result_data";
	
	/**
	 * Extra for {@link #onReceive(Context, Intent)}
	 */
	public static final String EXTRA_DATA_TYPE = "i2r_extra_data_type";
	
	/**
	 * Extra for {@link #onReceive(Context, Intent)}
	 */
	public static final String EXTRA_INFO_MESSAGE = "i2r_extra_info_message";
	
	
	private ConnectionManager<?> manager;
	private CommandFilter filter;
	private Pinger pinger;
	private String message;
	private boolean started;
	
	
	/**
	 * TODO: possibly make an array of managers
	 * 
	 * Constructor<br>
	 * @param activity
	 * @param type
	 * @throws ServiceNotFoundException
	 */
	public RemoteControlReceiver(Activity activity,
			String type) throws ServiceNotFoundException {
		
		this.manager = getManager(activity, type);
		this.pinger = new Pinger(Pinger.DEFAULT_PING_FREQUENCY, manager);
		this.filter = new CommandFilter(activity);
		this.message = DEFAULT_MESSAGE;
		this.started = false;
		
		DataBouncer.getInstance().add(manager);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onReceive(Context context, Intent intent) {
		
		// if the source of the intent was a remote connection reading
		// more commands
		if (intent.getAction().equals(ACTION_CONNECTION_READ)) {

			Log.d(TAG, "update broadcast from connection recieved");
			message = "pasring command";
			pinger.reset();
			update(intent.getStringExtra(EXTRA_COMMAND));

			// if the source of the intent was a new connection
		} else if (intent.getAction().equals(ACTION_CONNECTOR_RESPONDED)) {

			Log.d(TAG, "initialization broadcast recieved");
			String message = intent.getStringExtra(EXTRA_INFO_MESSAGE);
			this.message = message;
			initializeConnection();

			// if a sensor is done with a task, ask the master to
			// start the next one
		} else if (intent.getAction().equals(ACTION_TASK_COMPLETE)) {
			
			String result = intent.getStringExtra(EXTRA_INFO_MESSAGE);
			Log.d(TAG, result);
			this.message = result;
			updateSensors();
			
		}
		
	}
	
	
	/**
	 * Initializer used by {@link RemoteControlActivity}
	 */
	public void start() {
		this.started = true;
		if (!manager.hasConnection()) {
			Log.d(TAG, "finding connections with connection manager");
			manager.findConnection();
		}
	}
	
	
	
	/**
	 * Used by {@link RemoteControlActivity} to halt all
	 * master processes for this application.
	 */
	public void stop() {
		Log.d(TAG, "master stopped");
		this.started = false;

		if (manager != null) {
			manager.cancel();
			manager = null;
		}

		if (filter != null) {
			filter.cancel();
			filter = null;
		}
	}
	
	
	/**
	 * Query for this receiver's current message
	 * @return the message representing this receiver's
	 * currently running process.
	 */
	public String getMessage(){
		return message;
	}
	
	
	/**
	 * Query for this receiver's state
	 * @return true if this receiver has
	 * been started, false otherwise.
	 */
	public boolean isStarted(){
		return started;
	}
	
	
	/**
	 * Query for this receiver's manager.
	 * @return the connection currently held by this master's
	 *         {@link ConnectionManager}, or null if either the manager is null
	 *         or the manager does not currently have an active connection.
	 */
	public ConnectionManager<?> getConnectionManager() {
		return manager;
	}
	
	
	/**
	 * This is called whenever a {@link RemoteConnection} successfully reads
	 * data from the connection stream, and sends it to this application's
	 * {@link RemoteControlActivity} via broadcast. The activity then calls this
	 * method with the command string that was given to it by the connection
	 * which did the initial reading.
	 * @see {@link RemoteControlActivity#ACTION_CONNECTION_READ}
	 */
	public void update(String command){
		
		Log.d(TAG, "starting update by remote control sequence");
		
		// if a remote connection has been established, continue with update
		if (manager.hasConnection() && started) {

			filter.parseCommand(command);
			filter.executeNextCommand();

			// cannot update commands because there is no connection established
		} else if (!manager.hasConnection() && started) {
			Log.d(TAG, "connection lost, restarting connection manager.");
			manager.findConnection();

			// connection has been stopped locally, perform cleanup
		} else {

//			// alert the System to scan for new files are on the SD card
//			File file = new File(Environment.getExternalStoragePublicDirectory(
//					Environment.DIRECTORY_PICTURES).getAbsolutePath());
//			filter.getActivity().sendBroadcast(
//					new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
//							+ file)));
		}
	}
	
	
	/**
	 * Used by {@link #onReceive(Context, Intent)} to update
	 * this receiver's {@link CommandFilter}.
	 */
	public void initializeConnection(){
		
		// if there is a connection established and the command manager is null,
		// create a new CommandManager
		if (manager.hasConnection() && started) {

			// check the remote connection for any new incoming data
			manager.startDataTransfer();

			Log.d(TAG, "connection found, passing reference to responder");
			this.filter.setConnection(manager.getConnection());

			// if there is no connection established and the connection manager
			// isn't running, try again to get a remote connection
		} else if (!manager.hasConnection() && started) {

			Log.d(TAG, "no connection found, restarting connection manager");
			manager.findConnection();
		}
	}
	
	
	
	
	/**
	 * Updates this receiver's {@link CommandFilter} by trying to execute the next
	 * command in its command queue. This method is meant to be called when a
	 * {@link #ACTION_TASK_COMPLETE} broadcast is received
	 * in the main activity.
	 */
	public synchronized void updateSensors() {

		if (manager.hasConnection() && filter.canExecuteNextCommand()) {
			// execute next command after previous task was completed
			filter.executeNextCommand();
		}
	}
	
	
	
	/**
	 * Query for a manager suitable to the given String type.
	 * 
	 * @param activity - the activity in which to relate this manager.
	 * @param type - the type of this manager - can be any of the types listed
	 * in {@link ConnectionTypeSelectionActivity}.
	 * 
	 * @return a {@link ConnectionManager} designed for the specified connection type,
	 * or null if the type did not correlate to any of the types found
	 * in {@link ConnectionTypeSelectionActivity}.
	 * 
	 * @throws ServiceNotFoundException if the type given was not suitable for creating
	 * a connection manager.
	 */
	public static ConnectionManager<?> getManager(Activity activity,
			String type) throws ServiceNotFoundException {
		ConnectionManager<?> manager = null;
		
		// BLUETOOTH
		if (type.equals(ConnectionTypeSelectionActivity.EXTRA_BLUETOOTH)) {
			manager = new ConnectionManager<BluetoothDevice>
				(new BluetoothLink(activity, ConnectionManager.CONNECTION_TYPE_SERVER));

			// WIFI
		} else if (type.equals(ConnectionTypeSelectionActivity.EXTRA_WIFI)) {
			manager = new ConnectionManager<Object>(new SocketLink(activity,
					ConnectionManager.CONNECTION_TYPE_CLIENT));

			// USB
		} else if (type.equals(ConnectionTypeSelectionActivity.EXTRA_USB)) {
			manager = null; // USB currently not supported

		} else {
			throw new ServiceNotFoundException(
					"given connection type is not defined in this application");
		}
		
		return manager;
	}

} // end of RemoteControlReceiver class
