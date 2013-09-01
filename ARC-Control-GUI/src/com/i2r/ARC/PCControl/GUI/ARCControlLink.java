package com.i2r.ARC.PCControl.GUI;

import java.util.List;

import com.i2r.ARC.PCControl.Controller;
import com.i2r.ARC.PCControl.RemoteClient;
import com.i2r.ARC.PCControl.UnsupportedValueException;

public class ARCControlLink {

	private Controller controller;
	
	public ARCControlLink(){
//		this.controller = Controller.getInstance();
//		this.controller.initalize();
//		this.controller.establishUIStreams();
//		this.controller.establishConnections();
	}
	
	
	public void start(){
		
	}
	
	
	public List<RemoteClient> getClients(){
		return controller.getDevices();
	}
	
	
	public ARCControlDevice getDevice(RemoteClient client){
		ARCControlDevice device = null;
		try {
			// TODO: find actual device name
			device = new ARCControlDevice(client.getClass().getName(), client);
		} catch (UnsupportedValueException e) {
			e.printStackTrace();
		}
		
		return device;
	}
}
