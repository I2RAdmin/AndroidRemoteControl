package com.i2r.ARC.PCControl.GUI;

import com.i2r.ARC.PCControl.Controller;
import com.i2r.ARC.PCControl.RemoteClient;
import com.i2r.ARC.PCControl.UnsupportedValueException;

public class ARCControlLink {

	private Controller controller;
	
	public ARCControlLink(){
		this.controller = Controller.getInstance();
	}
	
	
	public void start(){
		this.controller.initalize();
		this.controller.establishUIStreams();
		this.controller.establishConnections();
	}
	
	
	public Controller getController(){
		return controller;
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
