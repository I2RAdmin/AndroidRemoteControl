/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.DataManager.ARCDataManager;
import com.i2r.ARC.PCControl.DataManager.DataManager;
import com.i2r.ARC.PCControl.UI.FileUI;
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
	
	private static Controller instance = new Controller();
	
	public final Object responseLock = new Object();
	DataResponse response;
	
	AtomicBoolean startLock;
	
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
		
		logger.debug("Created intial objects");
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
}
