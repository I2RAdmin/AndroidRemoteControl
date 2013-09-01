package com.i2r.ARC.PCControl.GUI;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class AndroidDeviceTab extends JPanel {

	private static final long serialVersionUID = 4857850415235003330L;

	private ARCControlDevice device;
	private JTabbedPane featuresPane;
	private JPanel newTasksPanel;
	private JPanel runningTasksPanel;
	private JPanel logPanel;
	
	public AndroidDeviceTab(ARCControlDevice device){
		this.device = device;
		this.featuresPane = createFeaturesPanel(device);
		this.newTasksPanel = createNewTaskPanel(device);
		this.runningTasksPanel = createRunningTasksPanel(device);
		this.logPanel = createLogPanel(device);
		
		
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
			
		
		// adding manipulatable features of sensors to left-most panel
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 2;
		gc.gridwidth = 1;
		this.add(featuresPane, gc);
		
		
		// adding panel to create a new task for this device in top center
		gc.gridx = 1;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		this.add(newTasksPanel, gc);
		
		
		// adding panel to show current tasks for this device in upper right corner
		gc.gridx = 2;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		this.add(runningTasksPanel, gc);
		
		
		// adding log info panel at bottom right
		gc.gridx = 1;
		gc.gridy = 1;
		gc.gridheight = 1;
		gc.gridwidth = 2;
		this.add(logPanel, gc);
	}
	
	
	
	public ARCControlDevice getDevice(){
		return device;
	}
	
	
	public void refresh(){
		featuresPane.repaint();
		newTasksPanel.repaint();
		runningTasksPanel.repaint();
		logPanel.repaint();
		repaint();
	}
	
	
	private static JTabbedPane createFeaturesPanel(ARCControlDevice device){
		JTabbedPane featuresPane = new JTabbedPane();
		for(Map.Entry<String, List<FeaturePanel>> entry : device.getFeaturePanels().entrySet()){
			
			String sensorName = entry.getKey();
			List<FeaturePanel> list = entry.getValue();
			
			JPanel features = new JPanel(new GridLayout(list.size(), 1));
			
			for(FeaturePanel p : list){
				features.add(p);
			}
			
			JScrollPane scrollableFeatures = new JScrollPane(features);
			featuresPane.addTab(sensorName, scrollableFeatures);
			
		}
		return featuresPane;
	}
	
	

	
	
	private static JPanel createNewTaskPanel(ARCControlDevice device){
		JPanel panel = new JPanel();
		
		
		
		return panel;
	}
	
	
	private static JPanel createRunningTasksPanel(ARCControlDevice device){
		JPanel panel = new JPanel();
		
		return panel;
	}
	
	
	private static JPanel createLogPanel(ARCControlDevice device){
		JPanel panel = new JPanel();
		
		return panel;
	}
	
	
	
	
}
