package com.i2r.androidremotecontroller;

import java.io.File;
import java.util.UUID;

import ARC.Constants;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.util.Log;

import com.i2r.androidremotecontroller.connections.BluetoothLink;
import com.i2r.androidremotecontroller.connections.ConnectionManager;
import com.i2r.androidremotecontroller.connections.RemoteConnection;
import com.i2r.androidremotecontroller.connections.WifiDirectLink;
import com.i2r.androidremotecontroller.exceptions.ServiceNotFoundException;
import com.i2r.androidremotecontroller.sensors.SensorController;

/**
 * This class models the master to pivot all sub-operations of this
 * application on. Everything involving state change, creation and
 * destruction filters through an instance of this object.
 * @author Josh Noel
 */
public class RemoteControlMaster {
	
	private static final String TAG = "RemoteControlMaster";
	
	private ConnectionManager<?> connectionManager;
	private SensorController sensorController;
	private boolean started;
	
	
	/**
	 * Constructor
	 * creates a new master to control the flow of command reads and execution.
	 * @param activity - the activity to send broadcasts with so that this
	 * hierarchy will waterfall correctly.
	 * @param holder - a holder to give to the command manager for image capturing
	 */
	public RemoteControlMaster(SensorController sensors, String connectionType) throws ServiceNotFoundException {
		
		this.sensorController = sensors;
		this.started = false;
	
		// figure out what type of connection the user chose
		
		// BLUETOOTH
		if(connectionType.equals(ConnectionTypeSelectionActivity.EXTRA_BLUETOOTH)){
			
			createBluetoothRemoteConnection(BluetoothAdapter.getDefaultAdapter());
			
			// WIFI
		} else if(connectionType.equals(ConnectionTypeSelectionActivity.EXTRA_WIFI)){
			
			WifiP2pManager wifi = (WifiP2pManager) sensors.getRelativeActivity()
										.getSystemService(Activity.WIFI_P2P_SERVICE);
			
			createWifiDirectRemoteConnection(wifi);
			
			// USB
		} else if(connectionType.equals(ConnectionTypeSelectionActivity.EXTRA_USB)){
			
			UsbManager usb = (UsbManager) sensors.getRelativeActivity()
					.getSystemService(Activity.USB_SERVICE);
			
			createUsbRemoteConnection(usb);
			
		} else {
			throw new ServiceNotFoundException
				("given connection type is not defined in this application");
		}
	}
	
	
	/**
	 * Creates a remote connection using this device's Wifi P2P service if it is available.
	 * @param manager - the WifiP2pManager to create a connection with
	 * @return true if connection succeeded, false if wifi-direct is not available or
	 * connection to wifi-direct failed TODO: change to private
	 */
	private void createWifiDirectRemoteConnection(WifiP2pManager manager) throws ServiceNotFoundException {

			// create a WifiDirectLink to pass to this ConnectionManager
			WifiDirectLink linker = new WifiDirectLink(
						sensorController.getRelativeActivity(),
						ConnectionManager.CONNECTION_TYPE_SERVER, manager);
				
			// create a new ConnectionManager
			this.connectionManager = new ConnectionManager<WifiP2pDevice>(linker,
						ConnectionManager.CONNECTION_TYPE_SERVER, sensorController.getRelativeActivity());
				
			Log.d(TAG, "connection manager created");

	}
	
	
	
	/**
	 * Creates a new remote connection using this device's bluetooth service, if it is available.
	 * @param adapter - the adapter to use for creating a bluetooth connection
	 * @return true if connection creation succeeded, false if adapter is not available or
	 * connection to bluetooth failed
	 */
	private void createBluetoothRemoteConnection(BluetoothAdapter adapter) throws ServiceNotFoundException {
	
		// create a BluetoothLink to pass to this ConnectionManager
		BluetoothLink linker = new BluetoothLink(adapter,
				UUID.fromString(Constants.Info.UUID),
				Constants.Info.SERVICE_NAME, sensorController.getRelativeActivity());
			
		// create a new ConnectionManager
		this.connectionManager = new ConnectionManager<BluetoothDevice>(linker,
				ConnectionManager.CONNECTION_TYPE_SERVER, sensorController.getRelativeActivity());
			
		Log.d(TAG, "connection manager created");
	}
	
	
	/**
	 * Attempts to create a USB connection to a remote device.
	 * If no USB is connected, this master's connectionManager
	 * should block until either this application is stopped or
	 * a USB is connected.
	 */
	private void createUsbRemoteConnection(UsbManager manager){
		// TODO: implement USB
	}
	
	
	
	/**
	 * Initializer used by the main activity
	 */
	public void start(){
		this.started = true;
		if(!connectionManager.hasConnection()){
			Log.d(TAG, "finding connections with connection manager");
			connectionManager.findConnection();
		}
	}
	
	
	/**
	 * Used by the main activity to stop this application
	 */
	public void stop(){
		Log.d(TAG, "master stopped");
		this.started = false;
	
		if(connectionManager != null){
			connectionManager.cancel();
		}
		
		if(sensorController != null){
			sensorController.cancel();
			sensorController.setConnection(null);
		}
	}

	
	
	/**
	 *  if the command manager has not been created due to no available
	 *  RemoteConnection, then create a new CommandManager
	 */
	public void initializeConnection(){
		
		// if there is a connection established and the command manager is null,
		// create a new CommandManager
		if(connectionManager.hasConnection() && started){
			
			// check the remote connection for any new incoming data
			connectionManager.startDataTransfer();
			
			Log.d(TAG, "connection found, passing reference to responder");
			this.sensorController.setConnection(connectionManager.getConnection());
			
			// if there is no connection established and the connection manager
			// isn't running, try again to get a remote connection
		} else if (!connectionManager.hasConnection() && started){
			
			Log.d(TAG, "no connection found, restarting connection manager");
			connectionManager.findConnection();
		}
	}
	
	
	
	
	/**
	 * Any sensor updates or new command reads are thrown to the
	 * main activity which then waterfalls down to this method to
	 * update the command manager. 
	 */
	public synchronized void updateByRemoteControl(String command){	

		Log.d(TAG, "starting update by remote control sequence");
		// if a remote connection has been established, continue with update
		if (connectionManager.hasConnection() && started) {
			
			sensorController.parseCommand(command);
			sensorController.executeNextCommand();
			
			// cannot update commands because there is no connection established
		} else if(!connectionManager.hasConnection() && started){
			Log.d(TAG, "connection lost, restarting connection manager.");
			connectionManager.findConnection();
			
			// connection has been stopped locally, perform cleanup
		} else {
			
			// alert the System to scan for new files are on the SD card
			File file = new File(Environment.getExternalStoragePublicDirectory(
				    Environment.DIRECTORY_PICTURES).getAbsolutePath());
				sensorController.getRelativeActivity()
				.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
				    Uri.parse("file://"+ file)));
		}
	}
	
	
	/**
	 * Updates this master's {@link SensorController} by trying
	 * to execute the next command in its command queue. This
	 * method is meant to be called when a sensor sends a broadcast
	 * back to main to notify that it has completed its task.
	 */
	public synchronized void updateSensors(){
		
		if(connectionManager.hasConnection() && sensorController.hasNewCommands()){
			
			// execute next command after previous task was completed
			sensorController.executeNextCommand();
			
			// start saving data to SD if connection has been lost and
			// there are still commands to execute
		} else {
			// TODO: switch to SD writing here
		}
	}
	
	
	/**
	 * @return the connection currently held by this master's
	 * {@link ConnectionManager}, or null if either the manager is
	 * null or the manager does not currently have an active connection.
	 */
	public RemoteConnection getConnection(){
		RemoteConnection connection = null;
		if(connectionManager != null){
			connection = connectionManager.getConnection();
		}
		return connection;
	}
	
	
} // end on RemoteControlMaster class
