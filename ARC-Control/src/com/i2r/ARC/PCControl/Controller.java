/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.UI.StreamUI;
import com.i2r.ARC.PCControl.link.RemoteLink;
import com.i2r.ARC.PCControl.link.BluetoothLink.BluetoothLink;
import com.i2r.ARC.PCControl.link.SMSLink.SMSLink;
import com.i2r.ARC.PCControl.link.USBLink.USBLink;
import com.i2r.ARC.PCControl.link.lineLink.CommandLineLink;
import com.i2r.ARC.PCControl.link.wifiLink.WifiLink;

/**
 * The brains of the program, runs all the things
 * 
 * @author Johnathan
 *
 */
public class Controller{
	RemoteLink<byte[]> link;
	
	StreamUI<OutputStream, InputStream, String> ui;
	List<RemoteClient> devices;
	
	private static Controller instance = new Controller();
	
	public final Object responseLock = new Object();
	List<AtomicBoolean> stillSearchLocks;
	
	RemoteClientResponse response;
	
	AtomicBoolean startLock;
	
	Properties prop;
	
	String UIOut;
	String remoteOut;
	String UIIn;
	String remoteIn;
	String connList;
	List<String> connTypes;
	
	private static final String TYPE_BLUETOOTH = "BLUETOOTH";
	private static final String TYPE_LOCAL = "LOCAL";
	private static final String TYPE_USB = "USB";
	private static final String TYPE_SMS = "SMS";
	private static final String TYPE_WIFI = "WIFI";
	
	private static final String TYPE_STANDARD_IN = "STANDARD_IN";
	private static final String TYPE_STANDARD_OUT = "STANDARD_OUT";
	
	private static final String TYPE_CLOSED = "CLOSED";
	private static final String TYPE_OPEN = "OPEN";
	
	private static final String UI_OUT_PROPERTY = "UI_OUT";
	private static final String UI_IN_PROPERTY = "UI_IN";
	private static final String REMOTE_OUT_PROPERTY = "REMOTE_OUT";
	private static final String REMOTE_IN_PROPERTY = "REMOTE_IN";
	private static final String CONN_TYPE_PROPERTY = "CONN_TYPE";
	
	
	private static final String UI_OUT_DEFAULT = TYPE_STANDARD_OUT;
	private static final String UI_IN_DEFAULT = TYPE_STANDARD_IN;
	
	private static final String REMOTE_OUT_DEFAULT = TYPE_OPEN;
	private static final String REMOTE_IN_DEFAULT = TYPE_OPEN;
	
	private static final String CONN_TYPE_DEFAULT = TYPE_BLUETOOTH;
	
	static final Logger logger = Logger.getLogger(Controller.class);
	
	private Controller(){
	}
	
	public static Controller getInstance(){
		return instance;
	}
	
	public void initalize(){
		logger.debug("creating data objects");
		devices = new ArrayList<RemoteClient>();
		
		startLock = new AtomicBoolean(true);
		stillSearchLocks = new ArrayList<AtomicBoolean>();
		
		prop = new Properties();
		connTypes = new ArrayList<String>();
		logger.debug("Created intial objects");
		logger.debug("loading properties");
		File propertyFile = new File("config.properties");
		
		if(propertyFile.exists()){
			try {
				prop.load(new FileInputStream(propertyFile));
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
			
			logger.debug("Loaded configuration, setting streams & connection");
			UIOut = (prop.getProperty(UI_OUT_PROPERTY) == null) ? UI_OUT_DEFAULT : prop.getProperty(UI_OUT_PROPERTY);
			UIIn = (prop.getProperty(UI_IN_PROPERTY) == null) ? UI_IN_DEFAULT : prop.getProperty(UI_IN_PROPERTY);
			remoteOut = (prop.getProperty(REMOTE_OUT_PROPERTY) == null) ? REMOTE_OUT_DEFAULT : prop.getProperty(REMOTE_OUT_PROPERTY);
			remoteIn = (prop.getProperty(REMOTE_IN_PROPERTY) == null) ? REMOTE_IN_DEFAULT : prop.getProperty(REMOTE_IN_PROPERTY);
			connList = (prop.getProperty(CONN_TYPE_PROPERTY) == null) ? CONN_TYPE_DEFAULT : prop.getProperty(CONN_TYPE_PROPERTY);
			
		}else{
			logger.debug("Configuration file not found, loading defaults.");
			UIOut = UI_OUT_DEFAULT;
			remoteOut = REMOTE_OUT_DEFAULT;
			UIIn = UI_IN_DEFAULT;
			remoteIn = REMOTE_IN_DEFAULT;
			connList = CONN_TYPE_DEFAULT;
		}
		
		for(String type : connList.split(",")){
			if(!type.equals("")){
				connTypes.add(type);
			}
		}
	}
	
	public List<String> aquiredConnections(RemoteLink<byte[]> searchLink){
		List<String> validConns = searchLink.currentConnections();
		
		if(validConns == null){
			logger.debug("No valid Connections found.");
			return validConns;
		}else if(validConns.get(0).equals("STILL_SEARCHING")){
			return validConns;
		}else{
			logger.debug("Found " + validConns.size() + " valid connections.");
			for(String portName : validConns){
				logger.debug(portName);
			}
			
			return validConns;
		}
	}
	
	
	public void genericRun(){
		//establish the UI
		OutputStream out = null;
		InputStream in = null;
		
		if(UIIn.equals(TYPE_STANDARD_IN)){
			in = System.in;
		}else{
			File inFile = new File(UIIn);
			
			try {
				in = new FileInputStream(inFile);
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
		if(UIOut.equals(TYPE_STANDARD_OUT)){
			out = System.out;
		}else{
			File outFile = new File(UIOut);
			try {
				if(!outFile.exists()){
					outFile.createNewFile();
				}
				out = new FileOutputStream(outFile);
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
		if(in == null || out == null){
			logger.error("Could not set in/out ui streams");
			//TODO: throw an error
		}
		
		
		
		ui = new StreamUI<OutputStream, InputStream, String>(in, out, this);
		ui.write("Starting the Android Remote Controller!");
		
		//establish links to remote devices
		
		for (String connType : connTypes) {
			AtomicBoolean threadLock = new AtomicBoolean(true);
			stillSearchLocks.add(threadLock);
			if (connType.equals(TYPE_BLUETOOTH)) {
				Thread t = new Thread(new EstablishConnectionsRunnable(new BluetoothLink(), "Bluetooth URL", stillSearchLocks.size() - 1));
				t.setName("Bluetooth-Search-Thread");
				t.start();
			} else if (connType.equals(TYPE_USB)) {
				Thread t = new Thread(new EstablishConnectionsRunnable(new USBLink(), "USB port", stillSearchLocks.size() - 1));
				t.setName("USB-Search-Thread");
				t.start();
			} else if (connType.equals(TYPE_SMS)) {
				Thread t = new Thread(new EstablishConnectionsRunnable(new SMSLink(), "SMS gateway", stillSearchLocks.size() - 1));
				t.setName("SMS-Search-Thread");
				t.start();
			} else if (connType.equals(TYPE_WIFI)) {
				Thread t = new Thread(new EstablishConnectionsRunnable(new WifiLink(), "Wifi IP", stillSearchLocks.size() - 1));
				t.setName("WIFI-Search-Thread");
				t.start();
			} else if (connType.equals(TYPE_LOCAL)) {
				ui.write("Creating a Local Connection");
				ui.write("This is a debuging configuration, if you see this message in prod, close the program and check the config file");
				logger.debug("creating local connection");
				// establish a local I/O stream connection
				link = new CommandLineLink();

				devices.add(new RemoteClient(link, ""));
			}
		}
		
		boolean searching = true;
		
		while(searching){
			for(AtomicBoolean lock : stillSearchLocks){
				if(lock.compareAndSet(false, false)){
					searching = false;
				}
			}
		}
		
		if(devices.isEmpty()){
			logger.error("No remote devices have been found.");
			ui.write("No remote devices were found.");
			//TODO: throw an error
			
			ui.close();
			return;
		}
		
		ui.write("For local commands, use index -1");
		
		//establish the in side of the UI (from user)
		ui.read();
		
		ui.write("Enter Commands: ");
		while(!startLock.compareAndSet(true, false));
		logger.debug("Starting....");
		
		while(!ui.inClosed.compareAndSet(true, true));
		ui.write("Shut down read side of UI... there may tasks still pending...");
		logger.debug("UI has stopped reading.");
		
		for(RemoteClient dev : devices){
			dev.die = true;
		}
		
		boolean allTasksComplete = false;
		while(!allTasksComplete){
			for(RemoteClient dev : devices){
				if(dev.deviceTasks.tasksRemaining()){
					allTasksComplete = false;
					break;
				}else{
					allTasksComplete = true;
				}
			}
		}
		
		logger.debug("All tasks have been finished.");
		
		ui.write("Shutting Down.  PEACE.");
		//close down resources.  we're done with them.
		
		for(RemoteClient dev : devices){
			dev.die = true;
			if(dev.conn != null){
				dev.conn.close();
			}
		}
		
		ui.close();
	}

	public void send(RemoteClient dev, ARCCommand newCommand) throws UnsupportedValueException {
		
		switch(newCommand.getHeader()){
		case LIST_DEVICES:
		case LIST_DEVICE_SENSORS:
		case PAUSE:
		case HELP:
			throw new UnsupportedValueException(newCommand.getHeader() + " is not a valid remote command.");
		default:
			if(devices.contains(dev)){
				dev.sendTask(newCommand);
			}else{
				throw new UnsupportedValueException(dev + " was not found in the master list.");
			}
			break;
		}
	}

	public RemoteClient getDevice(Integer deviceIndex) throws UnsupportedValueException {
		if(deviceIndex == null){
			throw new UnsupportedValueException("Device index null!");
		}
		if(deviceIndex > devices.size()){
			throw new UnsupportedValueException("Device at " + deviceIndex + " not found.");
		}
		logger.debug("Getting device at index " + deviceIndex.intValue());
		return devices.get(deviceIndex);
	}

	public void performLocal(ARCCommand arcCommand) throws UnsupportedValueException{
		//do a local command
		switch(arcCommand.getHeader()){
		case LIST_DEVICES:
			ui.write("Current Remote Devices: ");
			for(int i = 0; i < devices.size(); i++){
				ui.write(String.valueOf(i));
			}
			ui.write("-1");
			break;
		case LIST_DEVICE_SENSORS:
			ui.write("Current Sensors on device " + arcCommand.getArguments().get(0));
			RemoteClient dev = this.devices.get(Integer.parseInt(arcCommand.getArguments().get(0)));
			for(Sensor sensor : dev.supportedSensors.keySet()){
				ui.write(sensor.getAlias());
			}
			break;
		case HELP:
			ui.write("Help document is currently being written.");
			break;
		case PAUSE:
			RemoteClient devToPause = getDevice(Integer.parseInt(arcCommand.getArguments().get(0)));
			
			if(arcCommand.getArguments().size() > 1){
				ui.write("Pausing while task " + arcCommand.getArguments().get(1) + " is running on " + devToPause);
				while(devToPause.deviceTasks.hasTask(Integer.parseInt(arcCommand.getArguments().get(1)))){
					//WHEEEEEEEEEEEEEEEEEEEEEEEEEE
				}
			}else{
				ui.write("Pausing while " + devToPause + " has tasks.");
				while(devToPause.deviceTasks.tasksRemaining()){
					//WHEEEEEEEEEEEEEEEEEEEEEEEEEE
				}
				
				ui.write("Task Stack for " + devToPause);
				ui.write(devToPause.deviceTasks.logStackState());
			}
			break;
		case CONNECT:
			RemoteClient connDev = this.devices.get(Integer.parseInt(arcCommand.getArguments().get(0)));
			if(connDev.connectToDevice()){
				ui.write("Successfully Connected to the remote device!");
			}
			break;
		default:
			throw new UnsupportedValueException(arcCommand.getHeader().getAlias() + " is not a valid local command.");
		}
	}
	
	
	/*****************
	 * INNER CLASSES
	 *****************/
	
	private class EstablishConnectionsRunnable implements Runnable {

		private RemoteLink<byte[]> link;
		private String message;
		private int lockIndex;
		public EstablishConnectionsRunnable(RemoteLink<byte[]> link, String message, int lockIndex){
			this.link = link;
			this.message =  message;
			this.lockIndex = lockIndex;
		}
		
		@Override
		public void run() {
			List<String> connStrings;
			logger.debug("starting connection search...");
			link.searchForConnections();
			boolean foundConnections = false;

			ui.write("Searching for " + message + "s...");
			while (!foundConnections) {
				connStrings = aquiredConnections(link);
				if (connStrings != null && !connStrings.isEmpty()) {
					if (!connStrings.get(0).equals("STILL_SEARCHING")) {
						ui.write("Found " + connStrings.size());
						logger.debug("Found " + connStrings.size());

						for (String connStr : connStrings) {
							devices.add(new RemoteClient(link, connStr));
							ui.write("Found " + message + connStr);
							ui.write("Use index " + (devices.size() - 1)
									+ " to access this device.");
							logger.debug("Found a valid connection " + connStr);
						}

						foundConnections = true;
					}
				} else if (connStrings == null) {
					ui.write("No valid " + message + "s were found.");
					logger.debug("No valid " + message + "s could be found.");
					break;
				}
			}
			
			if(!stillSearchLocks.get(lockIndex).compareAndSet(true, false)){
				logger.error("Unable to unlock lock.");
			}
		}
	}
}

