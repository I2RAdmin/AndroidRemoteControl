package com.i2r.ARC.PCControl.GUI;

import java.util.List;

import com.i2r.ARC.PCControl.Controller;
import com.i2r.ARC.PCControl.RemoteClient;

public class ARCControlLink {

	private Controller controller;
	
	public ARCControlLink(){
		this.controller = Controller.getInstance();
		this.controller.initalize();
		this.controller.establishUIStreams();
		this.controller.establishConnections();
	}
	
	
	public void start(){
		
	}
	
	
	public List<FeaturePanel> getFeatureSets(){
		return null;
	}
	
	
	public List<RemoteClient> getClients(){
		return controller.getDevices();
	}
}
