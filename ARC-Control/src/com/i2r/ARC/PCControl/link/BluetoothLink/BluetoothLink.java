/**
 * 
 */
package com.i2r.ARC.PCControl.link.BluetoothLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;


/**
 * This class is the abstraction of a Bluetooth service searcher thingy.  It manages the Bluetooth SDP protocol 
 * to find the ARC service on a remote device and connect to a RFCOMM socket
 * 
 * @see {@link RemoteLink} for general contract information
 * 
 * @author Johnathan Pagnutti
 */

public class BluetoothLink implements Runnable, RemoteLink<byte[]>{
	//you better believe its a logger
	static final Logger logger = Logger.getLogger(BluetoothLink.class);
		
	//the number of attempts to make while searching for a service
	private static final int MAX_ATTEMPTS = 5;
	
	//The string representation of the uuid
	public static final String uuidString = "071299af103e4578b3cff2a386022a0d";
			
	//the local Bluetooth device we're using to connect
	private LocalDevice local;
	
	//The object used to locate other Bluetooth devices
	private DiscoveryAgent agent;
	
	//a list of devices discovered
	private ArrayList<RemoteDevice> devicesDiscovered;
	
	//a list of possible services to connect to on the discovered devices
	private ArrayList<String> servicesFound;
	
	//Objects used for synchronous timing
	//Used to notify when we're done looking for devices
	private BluetoothLinkEvent inquiryCompletedEvent = null;
	
	//Used to notify when we're done looking for services
	private BluetoothLinkEvent serviceSearchCompletedEvent = null;
	
	//UUID of the service, as a seperate object
	private UUID serviceUUID;
	
	//the UUID list of all the ARC service UUIDS
	//spoilers, its only one.  This exists to play nice with the API
	UUID[] uuidList;
	
	//The listener object for the device Inquiry
	private BluetoothSDPListener inquiryListener;
	
	//The listener object for the service search
	private BluetoothSDPListener serviceListener;
	
	//A boolean flag to check to see if the link has started searching for services
	public AtomicBoolean startedSearching;
	
	//A boolean flag to check to make sure the link has finished searching for services
	public AtomicBoolean completedSearching;

	/**
	 * Constructor
	 */
	public BluetoothLink(){
		//create a new atomic boolean for the started flag and set it to false
		startedSearching = new AtomicBoolean(false);
		
		//create a new atomic boolean for the finished searching flag and set it to false
		completedSearching = new AtomicBoolean(false);
		
		//create the lists to hold the discovered devices and discovered services 
		devicesDiscovered = new ArrayList<RemoteDevice>();
		servicesFound = new ArrayList<String>();
		
		//create the event objects
		inquiryCompletedEvent = new BluetoothLinkEvent();
		serviceSearchCompletedEvent = new BluetoothLinkEvent();
		
		//create the listener objects
		inquiryListener = new BluetoothSDPListener();
		serviceListener = new BluetoothSDPListener();
		
		//create a new UUID from the uuidString
		serviceUUID = new UUID(uuidString, false);
		
		//create a list of UUIDs of the ARC services
		uuidList = new UUID[]{serviceUUID};
	}
	
	/**
	 * Implementation of the {@link RemoteLink#searchForConnections()} method.
	 * @see {@link RemoteLink#searchForConnections()} for general contract information
	 * 
	 * This implementation is non-blocking, as it uses a subthread to handle the actual connection searches.
	 * The connection thread goes through the Bluetooth SDP protocol and searches for the ARC service on cached, and then nearby devices
	 */
	@Override
	public void searchForConnections(){
		logger.debug("Request to start the connection search thread...");
		//check to see if the search for valid connections has been started
		if(startedSearching.compareAndSet(false, true)){
			//if it has not, start the connection thread
			Thread t = new Thread(this);
			logger.debug("Starting the connection search thread...");
			t.start();
		}
	}
	
	/**
	 * Implementation of the Runnable interface
	 * @see {@link Runnable#run()} for generic contract information
	 *
	 * Establishes a Bluetooth connection to the ARC service on a remote device
	 * Goes through the SDP protocol to search for devices, then searchs for the ARC service in each device found
	 * Adds each new valid ARC link to the class list of found links
	 */
	@Override
	public void run() {
		logger.debug("Searching for ARC service URLs");
				
		//get the local Bluetooth device, set the device as discoverable and then use that local device to get the local discovery agent
		try {
			local = LocalDevice.getLocalDevice();
		
			local.setDiscoverable(DiscoveryAgent.GIAC);	
			agent = local.getDiscoveryAgent();
		
			//check to see if the local cache has any Bluetooth devices
			RemoteDevice[] cachedDevices = agent.retrieveDevices(DiscoveryAgent.CACHED);
			logger.debug("Retrieved " + cachedDevices.length + " device from the cache.");
			
			//if devices have been found...
			if(cachedDevices != null){
				//for each device found in the cache
				for(RemoteDevice remote : cachedDevices){
					//add that device to the remote devices list
					addNewDevice(remote);
				}
			
				//search for the ARC service on the current devices in the device list
				//this call will block until the search is done
				searchForARCService(uuidList);
			}
		
			//if we have not found any instances of the ARC service...
			if(servicesFound.isEmpty()){
				logger.debug("Attempting to find remote devices out in the ather...");
				//create a collection of the current devices in the list
				//these are the devices we found in the cache that do not have the ARC service on them
				Collection<RemoteDevice> foundDevices = devicesDiscovered.subList(0, devicesDiscovered.size());
				//start a search for nearby bluetooth devices
				//this call will block until the device search is done
				searchForBluetoothDevices();
				
				//remove the cached devices from the device list
				devicesDiscovered.removeAll(foundDevices);
				
				//resize the list
				devicesDiscovered.trimToSize();
				
				//search for the ARC service on the devices in the device discovered list
				searchForARCService(uuidList);
			}
			
			//final check to see if we have found any valid services
			if(servicesFound.isEmpty()){
				logger.debug("Unable to find any ARC service URLs");
				//TODO throw some sort of error
			}
		} catch (BluetoothStateException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		
		//Set the completed searching flag
		if(!completedSearching.compareAndSet(false, true)){
			logger.debug("Somehow, a thread got around the lock, and we've initalized Bluetooth more than once.  And it didn't crash.");
			//TODO we have a problem
		}
	}

	/**
	 * Starts the search for nearby Bluetooth devices
	 * If any are found, it adds the devices to the discovered devices list
	 * 
	 * @throws BluetoothStateException if the local bluetooth device does not allow an inquiry to be started due to other operations being performed on the device
	 * @throws InterruptedException if any thread interrupts the current thread while waiting for the Device Inquiry to finish
	 */
	private void searchForBluetoothDevices() throws BluetoothStateException, InterruptedException {
		logger.debug("Blocking for Device Inquiry...");
		//block for the device search
		synchronized(inquiryCompletedEvent){
			//look for nearby devices...
			boolean started = agent.startInquiry(DiscoveryAgent.GIAC, inquiryListener);
			
			//if the search has started
			if(started){
				logger.debug("Stopping this thread until Device Inquiry completes...");
				//block until the search is done
				inquiryCompletedEvent.wait();
			}
		}
		
		//handle the return code of the event
		switch(inquiryCompletedEvent.getCode()){
		//if OK...
		case DiscoveryListener.INQUIRY_COMPLETED:
			logger.debug("OK code recieved from device inquiry");
			break;
			//if terminated...
		case DiscoveryListener.INQUIRY_TERMINATED:
			logger.debug("Terminated code recieved from device inquiry");
			//throw exception
			break;
			//if exited with errors...
		case DiscoveryListener.INQUIRY_ERROR:
			logger.debug("Error code recieved from device inquiry");
			//throw exception
			break;
		//none of the above...
		default:
			//throw exception, return code is invalid
			logger.debug("Invalid code recieved from device inquiry");
			break;
		}
	}

	/**
	 * Start the search for the ARC service on the devices in the device list
	 * The search is a blocking call, however, the block is not indefinite
	 * 
	 * MAX_ATTEMPTS attempts are made to find the service on the device
	 * 
	 * @param uuidList the list of UUIDS for the ARC service
	 * 
	 * @throws BluetoothStateException if the local bluetooth device does not allow a service search to be started due to other operations being performed on the device 
	 * @throws InterruptedException if any thread interrupts the current thread while waiting for the Service Search to finish
	 */
	private void searchForARCService(UUID[] uuidList) throws BluetoothStateException, InterruptedException {
		//debug loop
		logger.debug("Searching for service(s) with UUID(s): ");
		for(UUID id : uuidList){
			logger.debug(id.toString());
		}

		//for each device we have discovered...
		for(RemoteDevice device : devicesDiscovered){
			//start at 0 attempts made to find the service on this device
			int attempts = 0;
			
			//the result code from a service search
			int resultCode = -1;
			
			//while we haven't gotten the OK code and we haven't tried MAX_ATTEMPTS...
			while(resultCode != DiscoveryListener.SERVICE_SEARCH_COMPLETED && attempts < MAX_ATTEMPTS){
				//block while we're searching for services
				logger.debug("Blocking For the Service Search for device " + device.getBluetoothAddress());
				logger.debug("Try # " + attempts);
				synchronized(serviceSearchCompletedEvent){
					//search for services with any of the IDs in the uuidList
					agent.searchServices(null, uuidList, device, serviceListener);
					
					//block until the search is done
					logger.debug("Stopping this thread until Service Search completes...");
					serviceSearchCompletedEvent.wait();
				}
				
				//handle the return code from the service search
				switch(serviceSearchCompletedEvent.getCode()){
				//if ok...
				case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
					//get happy
					//set result code
					resultCode = DiscoveryListener.SERVICE_SEARCH_COMPLETED;
					logger.debug("OK code recieved from service search (#" + serviceSearchCompletedEvent.getTransID() + ")");
					break;
				//if terminated...
				case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
					//set result code
					resultCode = DiscoveryListener.SERVICE_SEARCH_TERMINATED;
					//throw exception
					logger.debug("Terminated code recieved from service search (#" + serviceSearchCompletedEvent.getTransID() + ")");
					break;
				//if error...
				case DiscoveryListener.SERVICE_SEARCH_ERROR:
					//set result code
					resultCode = DiscoveryListener.SERVICE_SEARCH_ERROR;
					//try the search again
					logger.debug("Error code recieved from service search (#" + serviceSearchCompletedEvent.getTransID() + ")");
					break;
				//if no service records were found...
				case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
					//set result code
					resultCode = DiscoveryListener.SERVICE_SEARCH_NO_RECORDS;
					//try again
					logger.debug("No correct service record code recieved from service search (#" + serviceSearchCompletedEvent.getTransID() + ")");
					break;
				//if the device could not be reached...
				case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
					//set result code
					resultCode = DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE;
					//try again
					logger.debug("Device not reachable code recieved from service search (#" + serviceSearchCompletedEvent.getTransID() + ")");
					break;
				//else the return code is invalid
				default:
					//throw exception
					logger.debug("Invalid code recieved from service search (#" + serviceSearchCompletedEvent.getTransID() + ")");
					break;
				}
				
				//used an attempt
				attempts++;
			}
		}
	}

	/**
	 * Add a device to the remote device list.  If the device is already in the list, it is not added.
	 * 
	 * @param btDevice device to add 
	 */
	private void addNewDevice(RemoteDevice btDevice) {
		//if the device is not already in the device list
		if(!devicesDiscovered.contains(btDevice)){
			//add it to the list
			devicesDiscovered.add(btDevice);
			logger.debug("Device " + btDevice.getBluetoothAddress() + " added.");
		}
	}
	
	/**
	 * Adds one or more new service URLs to the services found list
	 * 
	 * @param servRecord the new services found to add to the list
	 */
	private void addNewServices(ServiceRecord[] servRecord) {
		//for each service in the service record...
		for(ServiceRecord record : servRecord){
			//get a no authenticate, no encryption connection url to the service
			String url = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			
			//if the URL is null...
			if(url == null){
				//move on to the next service
				continue;
			}
			
			//add the URL to the services found list
			servicesFound.add(url);
			
			logger.debug("Service " + url + " added.");
		}
	}
	
	/**
	 * Implementation of the {@link RemoteLink#currentConnections()} method.
	 * @see {@link RemoteLink#currentConnections()} for general contract information
	 * 
	 * Returns the list of valid ARC service URLs to attempt a connection too.
	 * 
	 * @return the ARC service link list or:
	 * 			1) {@link RemoteLink#STILL_SEARCHING} if the link has not finished searching for services
	 * 			2) null the service list was empty
	 */
	@Override
	public List<String> currentConnections() {
		//check to make sure the initialization has completed
		if(completedSearching.compareAndSet(true, true)){
			//check to see if we have any connections in the services found list
			if(!servicesFound.isEmpty()){
				//the list isn't empty, return the urls
				logger.debug("Returning service urls.");
				return servicesFound;
			}else{
				//the list is empty, return null for no urls found
				logger.debug("No services have been found to get a URL.");
				return null;
			}
		}else{
			//still looking for devices, return the still searching constant
			return RemoteLink.STILL_SEARCHING;
		}
	}
	
	/**
	 * Implementation of the {@link RemoteLink#connect(String)} method.
	 * @see {@link RemoteLink#connect(String)} for general contract information
	 * 
	 * @return A new remote connection from either a supplied URL or if null, the first URL in the connection list
	 */
	@Override
	public RemoteConnection<byte[]> connect(String connectionURL) {
		//use the supplied connection url
		String conn = connectionURL;
		if(conn == null){
			//just use the first in the list if none supplied
			conn = servicesFound.get(0);
		}
		
		logger.debug("Getting a remote connection to " + conn);
		return new BluetoothConnection(conn);
	}
	
	/********************
	 * INTERNAL CLASS
	 ********************/
	
	/**
	 * This private (lol,jk) internal class handles the callback methods that the Bluetooth API calls during the SDP protocol
	 * These callbacks deal with discovering new bluetooth devices, and discovering new services on those devices
	 * 
	 * @author Johnathan
	 *
	 */
	private class BluetoothSDPListener implements DiscoveryListener{

		/**
		 * Implements {@link DiscoveryListener}
		 * @see {@link DiscoveryListener} for generic contract information.
		 * 		lol,jk the BlueCove documentation is garbage.
		 * Method called when a new device has been discovered.  Adds the new device to the device list
		 * 
		 * @param btDevice the remote bluetooth device discovered
		 * @param the class of the remote bluetooth device discovered
		 */
		@Override
		public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
			logger.debug("Discovered: " + btDevice.getBluetoothAddress());
			addNewDevice(btDevice);
		}

		/**
		 * Implements {@link DiscoveryListener#inquiryCompleted(int)}
		 * @see {@link DiscoveryListener#inquiryCompleted(int)} for generic contract information.
		 * 
		 * Method called when the device inquiry is finished (done searching for remote devices)
		 * Sets the code of the inquiryCompletedEvent and notifies all thread waiting on the Inquiry Completed Event that it is finished
		 * 
		 * @param discType how the inquiry was completed
		 */
		@Override
		public void inquiryCompleted(int discType) {
			//set the code of the event to the discType
			//determines if the inquiry went ok, was inturrpted or errored for any reason.
			inquiryCompletedEvent.setCode(discType);
			
			//wait for the inquiry to be done
			synchronized(inquiryCompletedEvent){
				//notify any sleeping threads on this event that we're done with it
				inquiryCompletedEvent.notifyAll();
				logger.debug("Wake threads waiting for device inquiry to finish");
			}
		}

		/**
		 * Implements {@link DiscoveryListener#serviceSearchCompleted(int, int)}
		 * @see {@link DiscoveryListener#serviceSearchCompleted(int, int)} for generic contract information.
		 * 
		 * This method is called when the service search is complete
		 * Sets the transaction ID and response code of the Service Search Completed Event
		 * Notifies any threads waiting on the service search completed event that it has been finished
		 * 
		 * @param transID the transaction ID identifying the request which initiated the service search
		 * @param respCode the response code that indicates how the search ended
		 */
		@Override
		public void serviceSearchCompleted(int transID, int respCode) {
			//set the transID and code of the event to the transID and response code of the service search complete event
			serviceSearchCompletedEvent.setTransID(transID);
			serviceSearchCompletedEvent.setCode(respCode);
			
			//alert any blocking threads on this event that the event is finished
			synchronized(serviceSearchCompletedEvent){
				//notify any sleeping threads that we're done with the service search
				serviceSearchCompletedEvent.notifyAll();
				logger.debug("Wake threads waiting on service search to finish");
			}
			
		}

		/**		 
		 * Implements {@link DiscoveryListener#servicesDiscovered(int, ServiceRecord[])}
		 * @see {@link DiscoveryListener#servicesDiscovered(int, ServiceRecord[])} for generic contract information.
		 * 
		 * Method is called when one or more ARC services have been found on a remote device.  Adds the service URL to the list
		 * of valid service URLs that we can connect to.
		 * 
		 * @param transID the transaction ID of the service search that is posting the result
		 * @param servRecord a list of all ARC services found during the search request
		 */
		@Override
		public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
			logger.debug("Attempting to add " + servRecord.length + " new service links.");
			addNewServices(servRecord);
		}
	}
}