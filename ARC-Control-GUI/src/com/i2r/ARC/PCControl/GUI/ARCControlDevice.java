package com.i2r.ARC.PCControl.GUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ARC.Constants;

import com.i2r.ARC.PCControl.ARCCommand;
import com.i2r.ARC.PCControl.Capabilities;
import com.i2r.ARC.PCControl.DataType;
import com.i2r.ARC.PCControl.Limiter;
import com.i2r.ARC.PCControl.RemoteClient;
import com.i2r.ARC.PCControl.Sensor;
import com.i2r.ARC.PCControl.UnsupportedValueException;

public class ARCControlDevice {
	
	
	public static final String CHANGE_FEATURE = "change_feature";
	public static final String SEND_COMMAND = "send_command";
	
	private static final String GET_ALL_FEATURES = "features " + Constants.Sensors.CAMERA + " "
			+ Constants.Sensors.MICROPHONE + " " + Constants.Sensors.ENVIRONMENT_SENSORS + " "
			+ Constants.Sensors.GPS;
	
	private String name;
	private RemoteClient client;
	private Map<String, List<FeaturePanel>> features;
	
	public ARCControlDevice(String name, RemoteClient client) throws UnsupportedValueException {
		this.name = name;
		this.client = client;
		this.features = new HashMap<String, List<FeaturePanel>>();
		
		if(!client.connectToDevice()){
			throw new UnsupportedValueException("failed to connect to device");
		}
		
		ARCCommand getFeatures = ARCCommand.fromString(GET_ALL_FEATURES);
		client.sendTask(getFeatures);
		
		this.features = getFeatures(client);
	}
	
	
	
	public String getDeviceName(){
		return name;
	}
	
	
	public RemoteClient getClient(){
		return client;
	}
	
	
	public Map<String, List<FeaturePanel>> getFeaturePanels(){
		return features;
	}
	
	
	
	
	public static Map<String, List<FeaturePanel>> getFeatures(RemoteClient client){
		
		HashMap<String, List<FeaturePanel>> map = null;
		
		if(client != null && !client.getSupportedSensors().isEmpty()){
			
			map = new HashMap<String, List<FeaturePanel>>();
			
			for(Map.Entry<Sensor, Capabilities> entry : client.getSupportedSensors().entrySet()){
				
				Sensor sensor = entry.getKey();
				Capabilities capabilities = entry.getValue();
				ArrayList<FeaturePanel> featureList = new ArrayList<FeaturePanel>();
				
				for(Map.Entry<String, DataType> feature : capabilities.featureDataTypes().entrySet()){
					
					String name = feature.getKey();
					DataType type = feature.getValue();
					Limiter limiter = capabilities.featureLimiters().get(name);
					List<String> args = capabilities.featureLimitArguments().get(name);
					int currentValueIndex = 0; //args.indexOf(capabilities.featureCurrentValues().get(name));
			
					featureList.add(new FeaturePanel(name, type, limiter, args, currentValueIndex));
				}
				
				map.put(sensor.getAlias(), featureList);
			}
		}
		
		return map;
	}
	
}
