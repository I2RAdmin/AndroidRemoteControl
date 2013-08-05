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
import com.i2r.ARC.PCControl.Loadable.LoadableDataManager;
import com.i2r.ARC.PCControl.Loadable.LoadablePacket;
import com.i2r.ARC.PCControl.Loadable.LoadableTask;
import com.i2r.ARC.PCControl.Loadable.LoadableTaskStack;
import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;

/**
 * The capabilities object is part of the {@link RemoteDevice} which is the {@link RemoteLink} that got the connection, the 
 * actual {@link RemoteConnection} that has the I/O streams to the device, the {@link DataManager} that is currently handling that connection,
 * along with its {@link DataParser}.
 * 
 * Devices are created from a {@link RemoteLink} along with the connection URL.  Devices are now responsible for the actual connections.
 * Actually, {@link RemoteDevice}'s are even bigger than that.  They manage their own {@link TaskStack}, handle their own {@link DataResponse}s
 * and perform their own {@link ResponseAction}s.  Essentally, a {@link RemoteDevice} is an abstract of the entire control structure of
 * how to handle data from a device.  This allows the {@link Controller} to handle more than one device.
 * 
 * @author Johnathan Pagnutti
 *
 */
public class RemoteDevice {

	static final Logger logger = Logger.getLogger(RemoteDevice.class);
	
	RemoteLink<byte[]> link;
	RemoteConnection<byte[]> conn;
	DataManager<Task, byte[]> dataManager;
	String connectionURL;
	Map<Sensor, Capabilities> supportedSensors;
	
	
	Map<Task, DataResponse> responseMap;
	TaskStack deviceTasks;
	Capabilities capabilities;
	
	AtomicBoolean retrievedCapabilities;
	
	public RemoteDevice(RemoteLink<byte[]> link, String URL){
		responseMap = new HashMap<Task, DataResponse>();
		deviceTasks = new TaskStack();
		
		supportedSensors = new EnumMap<Sensor, Capabilities>(Sensor.class);
		
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
		
	public void sendTask(ARCCommand command){
		Task newTask = deviceTasks.createTask(command);
		dataManager.write(newTask);
	}

	
	public void setSensorParams(Sensor sensor, String featureName, DataType type, Limiter limit, List<String> args) {
		Capabilities cap = new Capabilities();
		cap.addFeature(featureName, type, limit, args);
		
		supportedSensors.put(sensor, cap);
	}

	
	public String checkSingleArg(Sensor sensor, String key, String string) throws UnsupportedValueException {
		 return supportedSensors.get(sensor).checkArg(key, string);
	}
}
