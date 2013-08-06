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
import com.i2r.ARC.PCControl.link.lineLink.CommandLineLink;

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
	DataResponse response;
	
	AtomicBoolean startLock;
	
	Properties prop;
	
	String UIOut;
	String remoteOut;
	String UIIn;
	String remoteIn;
	String connType;
	
	private static final String TYPE_BLUETOOTH = "BLUETOOTH";
	private static final String TYPE_LOCAL = "LOCAL";
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
		prop = new Properties();
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
			connType = (prop.getProperty(CONN_TYPE_PROPERTY) == null) ? CONN_TYPE_DEFAULT : prop.getProperty(CONN_TYPE_PROPERTY);
			
		}else{
			logger.debug("Configuration file not found, loading defaults.");
			UIOut = UI_OUT_DEFAULT;
			remoteOut = REMOTE_OUT_DEFAULT;
			UIIn = UI_IN_DEFAULT;
			remoteIn = REMOTE_IN_DEFAULT;
			connType = CONN_TYPE_DEFAULT;
		}
	}
	
	public void searchForConnections(){
		logger.debug("starting connection search..");
		link.searchForConnections();
	}
	
	public List<String> aquiredConnections(){
		//logger.debug("requesting info on connections");
		List<String> validConns = link.currentConnections();
		
		if(validConns == null){
			logger.debug("No valid Connections found-dizzle");
			return validConns;
		}else if(validConns.get(0).equals("STILL_SEARCHING")){
			//logger.debug("Still searching");
			return validConns;
		}else{
			logger.debug("Found " + validConns.size() + " valid connections.");
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
		
		//establish the requested link
		if(connType.equals(TYPE_BLUETOOTH)){
			link = new BluetoothLink();
			List<String> connectionURLs;
			searchForConnections();
			boolean foundConnections = false;
			
			ui.write("Searching for Bluetooth connection...");
			while(!foundConnections){
				connectionURLs = aquiredConnections();
				if(connectionURLs != null && !connectionURLs.isEmpty()){
					if(!connectionURLs.get(0).equals("STILL_SEARCHING")){
						ui.write("Found " + connectionURLs.get(0));
						logger.debug("Found and using " + connectionURLs.get(0));
						
						devices.add(new RemoteClient(link, connectionURLs.get(0)));
						ui.write("Found a valid URL " + connectionURLs.get(0));
						ui.write("Use index " + (devices.size() - 1) + " to access this device.");
						logger.debug("Found a valid connection " + connectionURLs.get(0));
						foundConnections = true;
					}
				}else if(connectionURLs == null){
					ui.write("No Valid bluetooth connections were found.");
					logger.debug("No valid bluetooth connections could be found.");
					break;
				}
			}
		}else if(connType.equals(TYPE_LOCAL)){
			ui.write("Creating a Local Connection");
			ui.write("This is a debuging configuration, if you see this message in prod, close the program and check the config file");
			logger.debug("creating local connection");
			//establish a local I/O stream connection
			link = new CommandLineLink();
			
			devices.add(new RemoteClient(link, ""));
		}
		
		if(devices.isEmpty()){
			logger.error("No remote devices have been found.");
			ui.write("No remote devices were found.");
			//TODO: throw an error
			
			ui.close();
			return;
		}
		
		RemoteClient dev = devices.get(0);
		logger.debug("Using device " + dev);
		
		//connect to the first device in the list
		dev.connectToDevice();
		
		//establish the requested conn type
		//if we want to not open the side for reading, then the remoteIn parameter is set to closed, so we never call the read in method
		if(remoteIn.equals(TYPE_OPEN)){
			dev.dataManager.read();
			logger.debug("Started data reading...");
		}
		
		//establish the in side of the UI (from user)
		//we don't need to use the generic streams anymore, I have them this way for convience, really.
		ui.read();
		
		ui.write("Enter Commands: ");
		while(!startLock.compareAndSet(true, false));
		logger.debug("Starting....");
		
		while(!ui.inClosed.compareAndSet(true, true));
		ui.write("Shut down read side of UI... there may tasks still pending...");
		logger.debug("UI has stopped reading.");
		
		while(dev.deviceTasks.tasksRemaining());
		logger.debug("All tasks have been finished.");
		
		ui.write("Shutting Down.  PEACE.");
		//close down resources.  we're done with them.
		dev.conn.close();
		ui.close();
	}

	public void send(RemoteClient dev, ARCCommand newCommand) {
		if(devices.contains(dev)){
			dev.sendTask(newCommand);
		}else{
			logger.error("Device not found.");
			//TODO: THROW ERROR
		}
	}

	public RemoteClient getDevice(Integer deviceIndex) {
		return devices.get(deviceIndex);
	}
}
