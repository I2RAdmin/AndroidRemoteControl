package com.i2r.ARC.PCControl.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ARC_GUI_Listener implements ActionListener {

	
	private ARCControlDevice device;
	
	public ARC_GUI_Listener(ARCControlDevice device){
		this.device = device;
	}
	
	
	public ARCControlDevice getDevice(){
		return device;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(ARCControlDevice.CHANGE_FEATURE)){
			
			
		} else if(e.getActionCommand().equals(ARCControlDevice.SEND_COMMAND)){
			
		}
	}

}
