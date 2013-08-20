package com.i2r.androidremotecontroller.connections;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.i2r.androidremotecontroller.main.RemoteControlActivity;


/**
 * This class models a {@link Link} implementation with USB connections.
 * Since USBs are immediately known to be connected and not connected,
 * this class only uses 
 * @author jnoel
 *
 */
public class UsbLink extends BroadcastReceiver implements Link<UsbDevice> {

	private static final String TAG = "UsbLink";
	private static final String USB_DEVICE_REQUEST = "i2r_usb_device_request";
	private static final String USB_ACCESSORY_REQUEST = "i2r_usb_accessory_request";
	
	private Context context;
	private LocalBroadcastManager broadcastManager;
	private UsbManager usbManager;
	private UsbAccessory accessory;
	private UsbDevice device;
	private IntentFilter usbFilter;
	private boolean isServer, listeningForConnection;
	private int[] endpointAddresses, interfaceIds;
	
	/**
	 * Create a UsbLink where this device is the UsbAccessory.<br>
	 * NOTE: This requires that a usb device is already connected to this
	 * device when an instance of this class is created.<br>
	 * Currently used by this application.
	 * @param context - the context in which this link was created.
	 */
	public UsbLink(Context context, boolean isServer) {
		
		this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		this.broadcastManager = LocalBroadcastManager.getInstance(context);
		this.isServer = isServer;
		this.endpointAddresses = null;
		this.interfaceIds = null;
		this.context = context;
		this.usbFilter = new IntentFilter();
		usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		usbFilter.addAction(USB_DEVICE_REQUEST);
		usbFilter.addAction(USB_ACCESSORY_REQUEST);
		context.registerReceiver(this, usbFilter);
		this.listeningForConnection = true;
	}
	
	
	/**
	 * Create a UsbLink where this device is the UsbDevice (host).
	 * This UsbLink creation type is not currently used by this application,
	 * and is generally considered less efficient since a usb host is
	 * defined as the device powering the bus and creating the connection.
	 * This will put a heavy load on both this device's processing and power supply.
	 * @param context - the context in which this UsbLink was created
	 * @param endpointAddresses - the enpoint addresses to search for while
	 * connecting to a remote device via USB
	 * @param interfaceIds - the interface IDs to search for while connecting
	 * to a remote device via USB
	 * @see {@link UsbInterface}
	 * @see {@link UsbEndpoint}
	 */
	public UsbLink(Context context, int[] endpointAddresses, int[] interfaceIds) throws IllegalArgumentException {
		if(endpointAddresses.length > 2){
			throw new IllegalArgumentException("only a USB link with two endpoint addresses (in/out) can be created");
		}
		
		this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		this.isServer = false;
		this.endpointAddresses = endpointAddresses;
		this.interfaceIds = interfaceIds;
		this.context = context;
		this.usbFilter = new IntentFilter();
		usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		usbFilter.addAction(USB_DEVICE_REQUEST);
		usbFilter.addAction(USB_ACCESSORY_REQUEST);
		context.registerReceiver(this, usbFilter);
		this.listeningForConnection = false;
	}
	
	
	
	/**
	 * UsbAccessory = client, does not power bus (waiter)
	 * Host and Client are switched in this link for
	 * convenience purposes with the ConnectionManager
	 */
	@Override
	public RemoteConnection listenForRemoteConnection() {
		RemoteConnection connection = null;
		
		UsbAccessory[] a = usbManager.getAccessoryList();
		
		if(a != null){
			
			accessory = a[0];

			if(usbManager.hasPermission(accessory)){
				connection = getConnectionFromAccessory(accessory);
				
			} else {
				PendingIntent intent = PendingIntent.getBroadcast(context, 0,
						new Intent(USB_ACCESSORY_REQUEST), 0);
				usbManager.requestPermission(accessory, intent);
			}
			
		} else {
			Intent intent = new Intent(RemoteControlActivity.ACTION_TASK_COMPLETE);
			intent.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE,
					"no USB device detected, please connect a USB device...");
			broadcastManager.sendBroadcast(intent);
			
			while(accessory == null){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return connection;
	}

	
	/**
	 * Helper method to listen for PC connection via usb 
	 * @param accessory - the accessory to open a connection with
	 * @return a {@link RemoteConnection} with input and output streams
	 * that were obtained from the given accessory, or null if no connection
	 * with the given accessory could be created.
	 */
	private RemoteConnection getConnectionFromAccessory(UsbAccessory accessory){
		RemoteConnection connection = null;
		ParcelFileDescriptor d = usbManager.openAccessory(accessory);
		
		if(d != null){
			Log.d(TAG, "creating streams from accessory descriptor");
			FileInputStream input = new FileInputStream(d.getFileDescriptor());
			FileOutputStream output = new FileOutputStream(d.getFileDescriptor());
			connection = new GenericRemoteConnection(context, input, output);
		} else {
			Log.e(TAG, "error opening file descriptor");
		}
		return connection;
	}

	
	/**
	 * UsbDevice = host, powers bus (seeker)<br>
	 * WARNING: this application is set be default
	 * to treat this android device as an accessory,
	 * treating it as a host will most likely not be
	 * as stable and will drain the batter much faster,
	 * as the host device is the one powering the bus
	 */
	@Override
	public RemoteConnection connectTo(UsbDevice remote) {
		RemoteConnection connection = null;
		if(isCorrectDevice(remote)){
			if(usbManager.hasPermission(remote)){
				
				UsbInterface[] interfaces = getInterfaces(remote, interfaceIds);
				UsbEndpoint[] points = getEndpoints(interfaces, endpointAddresses);
				UsbDeviceConnection usbConnection = usbManager.openDevice(remote);
				// TODO: make this stuff streams
				//connection = new UsbHostConnection(context, interfaces[0], 
				//		usbConnection, points[0], points[0]);
				
			} else {
				usbManager.requestPermission(remote, 
						PendingIntent.getBroadcast(
								context, 0, new Intent(USB_DEVICE_REQUEST), 0));
			}
		}
		return connection;
	}
	

	@Override
	public void searchForLinks() {
		if(!listeningForConnection){
			Log.d(TAG, "starting usb connection receiver");
			context.registerReceiver(this, usbFilter);
			listeningForConnection = true;
		} else {
		 Log.e(TAG, "usb connection receiver already started");	
		}
	}
	

	@Override
	public boolean isSearchingForLinks() {
		return false;
	}

	
	@Override
	public void haltConnectionDiscovery() {
		if(listeningForConnection){
			Log.d(TAG, "canceling UsbLink services");
			context.unregisterReceiver(this);
			listeningForConnection = false;
		}
	}
	

	@Override
	public List<UsbDevice> getLinks() {
		List<UsbDevice> links = null;
		if(!usbManager.getDeviceList().isEmpty()){
			links = new ArrayList<UsbDevice>
				(usbManager.getDeviceList().values());
			
		}	
		return links;
	}
	

	@Override
	public boolean isServerLink() {
		return isServer;
	}


	
	/**
	 * Helper method for {@link #connectTo(UsbDevice)}
	 * @param device - the device to test for
	 * the correct parameters
	 * @return true if this device holds true for
	 * all the required parameters, false otherwise
	 */
	public boolean isCorrectDevice(UsbDevice device){
		// TODO: add device inquiry here
		return true;
	}
	
	
	/**
	 * Helper method for {@link #listenForRemoteConnection()}
	 * @param accessory - the accessory to test for
	 * the correct parameters
	 * @return true if the accessory holds true for
	 * all the reuqired parameters, false otherwise
	 */
	public boolean isCorrectAccessory(UsbAccessory accessory){
		// TODO: add accessory inquiry here
		return true;
	}
	
	
	// TODO: (MUY IMPORTANTE' ==>) look into ServerSocket and InetAddress
	
	

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		
		if(action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)){
			
			Log.d(TAG, "accessory attatched");
			Intent ui = new Intent(RemoteControlActivity.ACTION_TASK_COMPLETE);
			ui.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE, "accessory attached");
			broadcastManager.sendBroadcast(ui);
			
			this.accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
			
			
			
		} else if(action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
			
			Log.d(TAG, "device attached");
			Intent ui = new Intent(RemoteControlActivity.ACTION_TASK_COMPLETE);
			ui.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE, "device attached");
			broadcastManager.sendBroadcast(ui);
			
			device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			
			if(device != null){
				if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
					connectTo(device);
				} else {
					Log.d(TAG, "permission to connect was denied: " + device.getDeviceName());
				}
			} else {
				Log.d(TAG, "received USB device is null, no action performed");
			}
			
			
			
		} else if(action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)){
		
			Intent ui = new Intent(RemoteControlActivity.ACTION_TASK_COMPLETE);
			ui.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE, "accessory detached");
			broadcastManager.sendBroadcast(ui);
		
			
			
		} else if(action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
			
			Intent ui = new Intent(RemoteControlActivity.ACTION_TASK_COMPLETE);
			ui.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE, "device detached");
			broadcastManager.sendBroadcast(ui);
		
			
			
		} else if(action.equals(USB_DEVICE_REQUEST)){
			
			boolean success = intent.getBooleanExtra (UsbManager.EXTRA_PERMISSION_GRANTED, false);
			Log.d(TAG, "custom request received. result: " + Boolean.toString(success));
			
			if(success){
				// TODO: do things with accessory
			} else {
				// TODO: alert user that app cannot run without connection
			}
			
			
			
		} else if(action.equals(USB_ACCESSORY_REQUEST)){
			
		}
	}

	
	
	
	
	// |---------------------- STATIC HELPER METHODS -------------------------------|
	
	
	/**
	 * Retrieves all available endpoints from the given device
	 * @param device - the device to get endpoints from
	 * @return all endpoints that the given device has available
	 */
	public static UsbEndpoint[] getEndpoints(UsbDevice device){
		return getEndpoints(getInterfaces(device, null), null);
	}
	
	
	/**
	 * Retrieves all endpoints from the device that match the given requirements
	 * @param device - the device to retrieve endpoints from
	 * @param addresses - the addresses to match endpoint addresses against to find
	 * specific endpoints. This can be null, if the client wants all available endpoints
	 * from the given device.
	 * @return
	 */
	public static UsbEndpoint[] getEndpoints(UsbInterface[] interfaces, int[] addresses){
		
		UsbEndpoint[] points = null;
		ArrayList<UsbEndpoint> temp = new ArrayList<UsbEndpoint>();
			
		
		// search through all available interfaces
		for(int i = 0; i < interfaces.length; i++){
			
			UsbInterface next = interfaces[i];
			int endpointCount = next.getEndpointCount();
			
			// check all endpoints for every interface
			for(int j = 0; j < endpointCount; j++){
				
				UsbEndpoint point = next.getEndpoint(j);
				
				if(addresses != null){
					
					// check the current endpoint to see if its address
					// matches any of the addresses given. If so, add it
					// to the result array
					for(int k = 0; k < addresses.length; k++){
						if(addresses[k] == point.getAddress()){
							temp.add(point);
							break;
						}
					} // end address for-loop
					
					// dont check for addresses, just get all available endpoints
				} else {
					temp.add(point);
				}
				
			} // end enpoint for-loop 
			
		} // end interface for-loop
		
		
		// create the result array from the temp ArrayList if
		// temp is not empty
		if(!temp.isEmpty()){
			points = new UsbEndpoint[temp.size()];
			for(int i = 0; i < points.length; i++){
				points[i] = temp.get(i);
			}
		}
		
		return points;
	}
	
	
	
	/**
	 * Gets all UsbInterfaces that meet the specified requirements in the
	 * parameters.
	 * @param device - the device to get UsbInterfaces from.
	 * @param ids - UsbInterface IDs to match against when adding an
	 * interface to the list.
	 * @return If the IDs parameter is null, returns all interfaces available on
	 * the given device, else this only returns interfaces with a matching
	 * ID to one of the IDs in the given int array.
	 */
	public static UsbInterface[] getInterfaces(UsbDevice device, int[] ids){
		
		UsbInterface[] interfaces = null;
		ArrayList<UsbInterface> temp = new ArrayList<UsbInterface>();
		
		int interfaceCount = device.getInterfaceCount();
		
		
		for(int i = 0; i < interfaceCount; i++){
			
			UsbInterface next = device.getInterface(i);
			
			if(ids != null){
				
				for(int j = 0; j < ids.length; j++){
					if(ids[j] == next.getId()){
						temp.add(next);
						break;
					}
				} // end address for-loop
				
			} else {
				temp.add(next);
			}
			
		} // end interface for-loop
		
		return interfaces;
	}


	@Override
	public Context getContext() {
		return context;
	}
	
}
