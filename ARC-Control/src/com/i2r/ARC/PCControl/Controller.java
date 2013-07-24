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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.DataManager.ARCDataManager;
import com.i2r.ARC.PCControl.DataManager.DataManager;
import com.i2r.ARC.PCControl.UI.FileUI;
import com.i2r.ARC.PCControl.UI.StreamUI;
import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;
import com.i2r.ARC.PCControl.link.BluetoothLink.BluetoothConnection;
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
	StreamUI ui;
	
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
			RemoteOut = (prop.getProperty(REMOTE_OUT_PROPERTY) == null) ? UI_OUT_DEFAULT : prop.getProperty(REMOTE_OUT_PROPERTY);
			RemoteIn = (prop.getProperty(REMOTE_IN_PROPERTY) == null) ? UI_IN_DEFAULT : prop.getProperty(REMOTE_IN_PROPERTY);
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
	
	public void trialRun(){
		ARCCommand sample = new ARCCommand(ARCCommand.TAKE_PICTURES);
		Task sampleTask = tasks.createTask(sample);
		
		link = new CommandLineLink();
		
		connect("");
		
		dataManager = new ARCDataManager(conn);
		
		dataManager.write(sampleTask);
		dataManager.read();
		
		logger.debug("Started the read thread...");
		while(tasks.tasksRemaining()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
		//there are no more tasks to handle, close the stream
		try {
			dataManager.dataIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Trial Run Ended.");
	}

	public void runWithFileUI(String filePath){
		link = new BluetoothLink();
		List<String> connectionURLs;
		searchForConnections();
		boolean foundConnections = false;
		
		while(!foundConnections){
			connectionURLs = aquiredConnections();
			if(connectionURLs != null && !connectionURLs.isEmpty()){
				if(!connectionURLs.get(0).equals("STILL_SEARCHING")){
					logger.debug("Found " + connectionURLs.get(0));
					connect(connectionURLs.get(0));
					logger.debug("Connected to " + connectionURLs.get(0));
					foundConnections = true;
				}else{
					//logger.debug("Still searching for service");
				}
			}else if(connectionURLs == null){
				logger.debug("No valid connections could be found.");
				break;
			}
		}
		
		//if the connection object is null...
		if(conn == null){
			logger.error("A bluetooth connection could not be found... exiting");
			return;
		}
		
		dataManager = new ARCDataManager((BluetoothConnection) conn);
		dataManager.read();
		logger.debug("Started the read thread...");
		
		FileUI ui = new FileUI(filePath);
		ui.readCommands();
		
		while(startLock.compareAndSet(true, false)){
			//wait a second before asking again
			try {
				this.wait(1000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
		while(tasks.tasksRemaining()){
			try {
				if(tasks.tasksRemaining() && dataManager.dataIn.available() != -1){
					this.wait(1000);
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
	}
	
	public void send(ARCCommand sendCommand) {
		Task thisTask = tasks.createTask(sendCommand);
		dataManager.write(thisTask);
		
		startLock.compareAndSet(false, true);
	}
	
	public void runSendOnly(){
		link = new BluetoothLink();
		List<String> connectionURLs;
		searchForConnections();
		boolean foundConnections = false;
		
		System.out.println("Searching for Bluetooth connection...");
		while(!foundConnections){
			connectionURLs = aquiredConnections();
			if(connectionURLs != null && !connectionURLs.isEmpty()){
				if(!connectionURLs.get(0).equals("STILL_SEARCHING")){
					logger.debug("Found " + connectionURLs.get(0));
					connect(connectionURLs.get(0));
					logger.debug("Connected to " + connectionURLs.get(0));
					foundConnections = true;
				}else{
					//logger.debug("Still searching for service");
				}
			}else if(connectionURLs == null){
				logger.debug("No valid connections could be found.");
				break;
			}
		}
		
		//if the connection object is null...
		if(conn == null){
			logger.error("A bluetooth connection could not be found... exiting");
			return;
		}
		
		dataManager = new ARCDataManager((BluetoothConnection) conn);
		
		
		System.out.println("Press enter to take a picture or the word \"stop\" (no quotes) to end the madness");
		
		Scanner userInput = new Scanner(System.in);
		
		while(true){
			String in = userInput.nextLine();
			
			if(in != null && in.equals("stop")){
				//break out of the loop
				break;
			}else{
				ARCCommand comand = new ARCCommand(ARCCommand.TAKE_PICTURES);
				Task task = tasks.createTask(comand);
			
				dataManager.write(task);
			}
		}
		
		//close the stream
		try {
			dataManager.dataOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void runSendOnlyWithWifi(){
		link = new WifiLink();
		connect("AndroidRemoteControl");
	}
	
	public void genericRun(){
		//establish the requested link
		if(connType == TYPE_BLUETOOTH){
			//establish bluetooth connection
		}else if(connType == TYPE_LOCAL){
			//establish a local I/O stream connection
			link = new CommandLineLink();
			
			connect("");
			
			dataManager = new ARCDataManager(conn);
		}
		
		//establish the requested conn type
		if(RemoteIn == TYPE_OPEN){
			//only open the stream if this is set
			dataManager.read();
		}
		//TODO: ditto for standard out
		
		//establish the UI
		InputStream in = null;
		OutputStream out = null;
		
		if(UIIn == TYPE_STANDARD_IN){
			in = new BufferedInputStream(System.in);
		}else{
			File inFile = new File(UIIn);
			
			try {
				
				//TODO: test hack
				if(!inFile.exists()){
					inFile.createNewFile();
					FileWriter hackWriter = new FileWriter(inFile);
					hackWriter.write("1");
					hackWriter.flush();
					hackWriter.close();
				}
				
				in = new FileInputStream(UIIn);
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(UIOut == TYPE_STANDARD_OUT){
			out = new BufferedOutputStream(System.out);
		}else{
			File outFile = new File(UIOut);
			try {
				if(!outFile.exists()){
					outFile.createNewFile();
				}
				out = new FileOutputStream(UIOut);
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
		
		ui = new StreamUI<OutputStream, InputStream, byte[]>(in, out, this);
		ui.read();
		
		while(startLock.compareAndSet(true, false)){
			//wait a second before asking again
			try {
				this.wait(1000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
		try {
			while(tasks.tasksRemaining() && ui.source.available() != -1){
				try {
					if(tasks.tasksRemaining() && dataManager.dataIn.available() != -1){
						this.wait(1000);
					}
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					e.printStackTrace();
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
}
