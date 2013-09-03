package com.i2r.ARC.PCControl.GUI;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class ARCFrame extends JFrame {

	private static final long serialVersionUID = 2982094766360421621L;

	private JTabbedPane androidDevicePane;
	private ARCMenuBar menuBar;
	
	public ARCFrame(ARCControlLink link){
		this.androidDevicePane = new JTabbedPane();
		this.menuBar = new ARCMenuBar();
		this.setLayout(new BorderLayout());
		this.add(androidDevicePane, BorderLayout.CENTER);
		this.add(menuBar, BorderLayout.NORTH);
		this.setTitle("Android Remote Control");
		this.setLocationRelativeTo(null);
		this.setSize(800, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
	
	public void addDevice(AndroidDeviceTab tab){
		androidDevicePane.addTab(tab.getName(), tab);
	}
	
	
	public void update(){
		androidDevicePane.repaint();
		menuBar.repaint();
		repaint();
	}
	
}
