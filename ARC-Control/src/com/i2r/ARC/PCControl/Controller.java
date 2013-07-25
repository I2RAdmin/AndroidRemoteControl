/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.DataManager.ARCDataManager;
import com.i2r.ARC.PCControl.DataManager.DataManager;
import com.i2r.ARC.PCControl.UI.StreamUI;
import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;
import com.i2r.ARC.PCControl.link.BluetoothLink.BluetoothLink;
import com.i2r.ARC.PCControl.link.lineLink.CommandLineLink;
import com.i2r.ARC.PCControl.link.wifiLink.WifiLink;

/**
 * The brains of the program, runs all the things
 * 
 * @author Johnathan
 *
 */
public class Controller{
	TaskStack tasks;
	RemoteConnection<byte[]> conn;
	RemoteLink<byte[]> link;
	DataManager<Task, byte[]> dataManager;
	
	StreamUI<OutputStream, InputStream, String> ui;
	
	private static Controller instance = new Controller();
	
	public final Object responseLock = new Object();
	DataResponse response;
	
	AtomicBoolean startLock;
	
	Properties prop;
	
	String UIOut;
	String RemoteOut;
	String UIIn;
	String RemoteIn;
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
		tasks = new TaskStack();
		startLock = new AtomicBoolean(false);
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
			RemoteOut = (prop.getProperty(REMOTE_OUT_PROPERTY) == null) ? REMOTE_OUT_DEFAULT : prop.getProperty(REMOTE_OUT_PROPERTY);
			RemoteIn = (prop.getProperty(REMOTE_IN_PROPERTY) == null) ? REMOTE_IN_DEFAULT : prop.getProperty(REMOTE_IN_PROPERTY);
			connType = (prop.getProperty(CONN_TYPE_PROPERTY) == null) ? CONN_TYPE_DEFAULT : prop.getProperty(CONN_TYPE_PROPERTY);
			
		}else{
			logger.debug("Configuration file not found, loading defaults.");
			UIOut = UI_OUT_DEFAULT;
			RemoteOut = REMOTE_OUT_DEFAULT;
			UIIn = UI_IN_DEFAULT;
			RemoteIn = REMOTE_IN_DEFAULT;
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
	
	public void connect(String URL){
		logger.debug("Attempting to connect to " + URL);
		conn = link.connect(URL);
		
		logger.debug("Connected with a " + conn.getClass());
	}

	
	public void send(ARCCommand sendCommand) {
		Task thisTask = tasks.createTask(sendCommand);
		dataManager.write(thisTask);
		
		startLock.compareAndSet(false, true);
	}
	
	/**
	 * @deprecated
	 */
	public void runSendOnlyWithWifi(){
		link = new WifiLink();
		connect("AndroidRemoteControl");
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
			logger.error("could not set in/out ui streams");
			
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
						
						connect(connectionURLs.get(0));
						ui.write("Connected to " + connectionURLs.get(0));
						logger.debug("Connected to " + connectionURLs.get(0));
						foundConnections = true;
					}else{
					}
				}else if(connectionURLs == null){
					ui.write("No Valid Connections were found.");
					logger.debug("No valid connections could be found.");
					break;
				}
			}
			
			//if the connection object is null...
			if(conn == null){
				ui.write("Could not create a valid bluetooth connection.  Shutting down.");
				logger.error("A bluetooth connection could not be found... exiting");
				ui.close();
				return;
			}
			
			dataManager = new ARCDataManager(conn);
			
		}else if(connType.equals(TYPE_LOCAL)){
			ui.write("Creating a Local Connection");
			ui.write("This is a debuging configuration, if you see this message in prod, close the program and check the config file");
			logger.debug("creating local connection");
			//establish a local I/O stream connection
			link = new CommandLineLink();
			
			connect("");
			
			dataManager = new ARCDataManager(conn);
		}
		
		//establish the requested conn type
		//if we want to not open the side for reading, then the RemoteIn parameter is set to closed.
		//this pretty much just never calls the dataManager.read() method.
		if(RemoteIn.equals(TYPE_OPEN)){
			//only open the stream if this is set
			dataManager.read();
			logger.debug("Started data reading...");
		}
		
		//establish the in side of the UI (from user)
		//we don't need to use the generic streams anymore, I have them this way for convience, really.
		ui.read();
		
		
		
		ui.write("Enter Commands: ");
		while(!startLock.compareAndSet(true, false));
		
		while(!ui.inClosed);
		
		ui.write("Shutting Down.  PEACE");
		//close down resources.  we're done with them.
		conn.close();
	}
}
