package com.i2r.androidremotecontroller.connections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import com.i2r.androidremotecontroller.exceptions.NoBluetoothAdapterFoundException;
import com.i2r.androidremotecontroller.exceptions.NoBluetoothSocketFoundException;

/**
 * This class models a Linker object to pair (but not necessarily connect)
 * this device with another bluetooth device 
 * @author Josh Noel
 */
public class BluetoothLink implements Link<BluetoothDevice> {

	private static final String TAG = "BluetoothLink";
	
	private UUID uuid;
	private BluetoothAdapter adapter;
	private Activity activity;
	private String name;
	private BluetoothServerSocket listener;
	
	// constructor
	public BluetoothLink(BluetoothAdapter adapter, UUID uuid, String name, Activity activity)
														throws NoBluetoothAdapterFoundException {
		
		if(adapter == null){
			// ABANDON SHIP!!!
			throw new NoBluetoothAdapterFoundException();
		}
		
		this.uuid = uuid;
		this.name = name;
		this.adapter = adapter;
		this.activity = activity;
		this.listener = null;
	}
	
	
	//**********************************************************************|
	// See {@link Link} for documentation of these overrides ---------------|
	//**********************************************************************|
	
	
	@Override
	public RemoteConnection connectTo(Object remote) {
		BluetoothDevice device = (BluetoothDevice) remote;
		BluetoothConnection connection;
		try{
			Log.d(TAG, "creating connection to device : " + device.getName());
			BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
			socket.connect();
			connection = new BluetoothConnection(socket, activity);
			Log.d(TAG, "successfully connected to  " + device.getName());
		} catch (NoBluetoothSocketFoundException e) {
			connection = null;
			e.printStackTrace();
		} catch (IOException e) {
			connection = null;
			e.printStackTrace();
		}
		return connection;
	}

	
	@Override
	public RemoteConnection listenForRemoteConnection() {
		BluetoothConnection connection = null;
		BluetoothSocket socket = null;
		
		try{
			listener = adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
			socket = listener.accept();
			
			if(socket != null){
				Log.d(TAG, "connection accepted");
				connection = new BluetoothConnection(socket, activity);
			} else {
				Log.e(TAG, "no connection found");
			}
			
		} catch(IOException e){
			e.printStackTrace();
		} catch (NoBluetoothSocketFoundException e) {
			e.printStackTrace();
		}
		
		return connection;
	}
	
	
	@Override
	public void searchForLinks() {
		if(!adapter.isEnabled()){
			activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
		}
	}

	
	
	@Override
	public List<BluetoothDevice> getLinks() {
		ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
		list.addAll(adapter.getBondedDevices());
		return list;
	}
	
	
	
	@Override
	public void haltConnectionDiscovery() {
		try {
			listener.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		listener = null;
		
		if(adapter.isDiscovering()){
			adapter.cancelDiscovery();
		}
	}


	@Override
	public boolean isListeningForConnections() {
		return listener != null;
	}


	@Override
	public boolean isSearchingForConnections(){
		return adapter.isDiscovering();
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
	public static BluetoothDevice findDeviceByUUID(UUID uuid) throws NoBluetoothAdapterFoundException {
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		// if adapter is null, bluetooth is not supported on this device
		// AKA - ABANDON SHIP!!!1!11!
		if(adapter == null){
			Log.e(TAG, "ERROR - bluetooth adapter is null");
			throw new NoBluetoothAdapterFoundException("bluetooth adapter is null");
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

		return device;
	}

}
