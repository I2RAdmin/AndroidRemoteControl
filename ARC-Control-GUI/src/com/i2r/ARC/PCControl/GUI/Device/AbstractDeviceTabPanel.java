package com.i2r.ARC.PCControl.GUI.Device;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;


public abstract class AbstractDeviceTabPanel extends JPanel {

	private static final long serialVersionUID = -2526380226664493899L;

	private ArcControlDevice device;
	
	public AbstractDeviceTabPanel(ArcControlDevice device){
		this.device = device;
	}
	
	
	public ArcControlDevice getDevice(){
		return device;
	}
	
	
	public abstract GridBagConstraints getConstraints();
	
	public abstract void addInfo(String info);
	
	public abstract void removeInfo(String info);
	
}
