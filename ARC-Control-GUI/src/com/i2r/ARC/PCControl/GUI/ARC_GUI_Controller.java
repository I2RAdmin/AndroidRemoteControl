package com.i2r.ARC.PCControl.GUI;

public class ARC_GUI_Controller {

	
	private static ARC_GUI_Controller instance = new ARC_GUI_Controller();
	
	private ARCFrame frame;
	private ARCControlLink link;
	
	private ARC_GUI_Controller(){
		this.link = new ARCControlLink();
		this.frame = new ARCFrame(link);
	}
	
	
	public static ARC_GUI_Controller getInstance(){
		return instance;
	}
	
	
	public void start(){
		link.start();
		frame.setVisible(true);
	}
}
