/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.DataManager.ARCDataManager;
import com.i2r.ARC.PCControl.DataManager.DataManager;
import com.i2r.ARC.PCControl.DataManager.DataParser;
import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;

/**
 * The capabilities object is part of the {@link RemoteClient} which is the {@link RemoteLink} that got the connection, the 
 * actual {@link RemoteConnection} that has the I/O streams to the device, the {@link DataManager} that is currently handling that connection,
 * along with its {@link DataParser}.
 * 
 * Devices are created from a {@link RemoteLink} along with the connection URL.  Devices are now responsible for the actual connections.
 * Actually, {@link RemoteClient}'s are even bigger than that.  They manage their own {@link TaskStack}, handle their own {@link DataResponse}s
 * and perform their own {@link ResponseAction}s.  Essentally, a {@link RemoteClient} is an abstract of the entire control structure of
 * how to handle data from a device.  This allows the {@link Controller} to handle more than one device.
 * 
 * @author Johnathan Pagnutti
 *
 */
public class RemoteClient {

	static final Logger logger = Logger.getLogger(RemoteClient.class);
	
	RemoteLink<byte[]> link;
	RemoteConnection<byte[]> conn;
	DataManager<Task, byte[]> dataManager;
	String connectionURL;
	Map<Sensor, Capabilities> supportedSensors;
	Map<Sensor, Map<String, String>> currentSensorValues;
	
	Map<Task, DataResponse> responseMap;
	TaskStack deviceTasks;
	Capabilities capabilities;
	
	AtomicBoolean retrievedCapabilities;
	
	public RemoteClient(RemoteLink<byte[]> link, String URL){
		responseMap = new HashMap<Task, DataResponse>();
		deviceTasks = new TaskStack();
		
		supportedSensors = new EnumMap<Sensor, Capabilities>(Sensor.class);
		currentSensorValues = new EnumMap<Sensor, Map<String, String>>(Sensor.class);
		
		retrievedCapabilities = new AtomicBoolean(false);
		
		this.link = link;
		connectionURL = URL;
	}
	
	public boolean connectToDevice(){
		logger.debug("Connecting to remote device at: " + connectionURL);
		logger.debug("With a " + link.getClass().getSimpleName());
		conn = link.connect(connectionURL);
		
		if(conn != null){
			dataManager = new ARCDataManager(conn, this);
			return true;
		}else{
			return false;
		}
	}
		
	public void sendTask(ARCCommand command) throws UnsupportedValueException{
		CommandHeader commandHeader = command.getHeader();
		
		Task newTask = deviceTasks.createTask(command);
		
		//if the task in question requires us to do something, do it here
		switch(commandHeader){
		case KILL_TASK:
			this.deviceTasks.removeTask(Integer.parseInt(command.getArguments().get(ARCCommand.KILL_TASK_INDEX)));
			break;
		case MODIFY_SENSOR:
			Sensor sensor = Sensor.get(Integer.parseInt(command.getArguments().get(0)));
			int i = 1;
			while(i < command.getArguments().size()){
				String featureName = command.getArguments().get(i);
				i++;
				String featureValue = command.getArguments().get(i);
				i++;
				
				this.currentSensorValues.get(sensor).put(featureName, featureValue);
			}
			break;
		case TAKE_PICTURE:
			if(!this.supportedSensors.containsKey(Sensor.CAMERA)){
				throw new UnsupportedValueException(Sensor.CAMERA.getAlias() + " is not a valid sensor for this device.");
			}
			break;
		case RECORD_AUDIO:
			if(!this.supportedSensors.containsKey(Sensor.MICROPHONE)){
				throw new UnsupportedValueException(Sensor.MICROPHONE.getAlias() + " is not a valid sensor for this device.");
			}
			break;
		default:
			break;
		}
		
		dataManager.write(newTask);
	}

	
	public void setSensorParams(Sensor sensor, String featureName, DataType type, Limiter limit, List<String> args) {
		Capabilities cap = supportedSensors.get(sensor);
		
		if(cap == null){
			cap = new Capabilities();
			supportedSensors.put(sensor, cap);
			currentSensorValues.put(sensor, new HashMap<String, String>());
		}
		
		cap.addFeature(featureName, type, limit, args);
	}

	
	public String checkSingleArg(Sensor sensor, String key, String string) throws UnsupportedValueException {
		 return supportedSensors.get(sensor).checkArg(key, string);
	}

	public void setCurrentValue(Sensor sensor, String featureName,
			String currentValue) {
		currentSensorValues.get(sensor).put(featureName, currentValue);
		
	}
}
