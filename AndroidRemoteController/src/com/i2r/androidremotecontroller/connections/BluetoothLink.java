package com.i2r.androidremotecontroller.connections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import ARC.Constants;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import com.i2r.androidremotecontroller.exceptions.ServiceNotFoundException;

/**
 * This class models a Linker object to pair (but not necessarily connect)
 * this device with a controlling bluetooth device (PC client)
 * @author Josh Noel
 */
public class BluetoothLink implements Link<BluetoothDevice> {

	private static final String TAG = "BluetoothLink";
	
	private UUID uuid;
	private BluetoothAdapter adapter;
	private Activity activity;
	private String name;
	private BluetoothServerSocket listener;
	private BluetoothSocket socket;
	private boolean isServer;
	
	/**
	 * Constructor<br>
	 * creates a new BluetoothLink that accepts connections only with other
	 * devices that have the UUID given.
	 * @param activity - a reference to the activity that made this link.
	 * @param isServer - flag to tell this link to act as a server or a client link
	 * @throws ServiceNotFoundException if the given {@link BluetoothAdapter} is null.
	 * @see {@link ConnectionManager#CONNECTION_TYPE_CLIENT}
	 * @see {@link ConnectionManager#CONNECTION_TYPE_SERVER}
	 */
	public BluetoothLink(Activity activity, boolean isServer) throws ServiceNotFoundException {
		
		this.adapter = BluetoothAdapter.getDefaultAdapter();
		
		if(adapter == null){
			// ABANDON SHIP!!!
			throw new ServiceNotFoundException("bluetooth adapter is null");
		}
		
		this.uuid = UUID.fromString(Constants.Info.UUID);
		this.name = Constants.Info.SERVICE_NAME;
		this.activity = activity;
		this.isServer = isServer;
		this.listener = null;
	}
	
	
	//**********************************************************************|
	// See the Link interface for documentation of these overrides ---------|
	//**********************************************************************|
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ThreadedRemoteConnection listenForRemoteConnection() {
		GenericThreadedRemoteConnection connection = null;
		socket = null;
		
		try{
			listener = adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
			socket = listener.accept();
			
			if(socket != null){
				Log.d(TAG, "connection accepted");
				connection = new GenericThreadedRemoteConnection(activity, 
						socket.getInputStream(), socket.getOutputStream());
			} else {
				Log.e(TAG, "no connection found");
			}
			
		} catch(IOException e){
			Log.e(TAG, "exception while listening for bluetooth link");
			connection = null;
		}
		
		return connection;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ThreadedRemoteConnection connectTo(BluetoothDevice remote) {
		GenericThreadedRemoteConnection connection = null;
		try{
			Log.d(TAG, "creating connection to device : " + remote.getName());
			socket = remote.createInsecureRfcommSocketToServiceRecord(uuid);
			socket.connect();
			connection = new GenericThreadedRemoteConnection(activity, 
					socket.getInputStream(), socket.getOutputStream());
			Log.d(TAG, "successfully connected to  " + remote.getName());
		} catch (IOException e) {
			Log.e(TAG, "error while searching for bluetooth link");
			connection = null;
		}
		return connection;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void searchForLinks() {
		Log.d(TAG, "searching for bluetooth links");
		if(!adapter.isEnabled()){
			activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
		} else {
			adapter.startDiscovery();
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BluetoothDevice> getLinks() {
		ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
		list.addAll(adapter.getBondedDevices());
		return list;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void haltConnectionDiscovery() {
		
		Log.d(TAG, "halting connection discovery for bluetooth");
		
		if(listener != null){
			try {
				listener.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			
			listener = null;
		}
		
		
		if(socket != null){
			try{
				socket.close();
			} catch(IOException e){
				Log.e(TAG, e.getMessage());
			}
			
			socket = null;
		}
		
		
		if(adapter.isDiscovering()){
			adapter.cancelDiscovery();
		}
		
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isServerLink() {
		return isServer;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSearchingForLinks() {
		return adapter.isDiscovering();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Context getContext() {
		return activity;
	}
	
	
	/**
	 * Iterates through all the bluetooth devices currently bonded to this android device,
	 * and searches each one for the UUID given. Once this method finds a matching UUID,
	 * it returns the device that the given UUID was found in.<br>
	 * NOTE: This method assumes that the given parameters are not null.
	 * passing null will result in a NullPointerException.
	 * @param notifier - the {@link Notifier} object being used by the findController(Activity) method
	 * @param iter - the iterator over the set of bluetooth devices currently bonded to this device
	 * @param uuid - the UUID to use for comaprison when finding the device
	 * @return the device that contains the matching UUID of the one given, or null if no match was found
	 */
	public static BluetoothDevice findDeviceByUUID(UUID uuid) throws ServiceNotFoundException {
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		// if adapter is null, bluetooth is not supported on this device
		// AKA - ABANDON SHIP!!!1!11!
		if(adapter == null){
			Log.e(TAG, "ERROR - bluetooth adapter is null");
			throw new ServiceNotFoundException("bluetooth adapter is null");
		}
		
		// get the iterator for all the devices paired with this device
		Iterator<BluetoothDevice> iter = adapter.getBondedDevices().iterator();
		
		BluetoothDevice device = null;
		boolean controllerFound = false;
		
		Log.d(TAG, "searching for device with UUID: " + uuid.toString());
		
		// search for the controller through all the devices paired with this device
		while(iter.hasNext() && !controllerFound){
			
			device = iter.next();
				
			// get ParcelUuids from notifier after uuid action occurs
			ParcelUuid[] uuids = device.getUuids();

			// do any UUIDs match?
			for (int i = 0; i < uuids.length && !controllerFound; i++) {
				if (uuids[i].getUuid().compareTo(uuid) == 0) {
					controllerFound = true;
				}
			}
		}
		
		
		if(!controllerFound){
			device = null;
		}

		return device;
	}


} // end of BluetoothLink class
