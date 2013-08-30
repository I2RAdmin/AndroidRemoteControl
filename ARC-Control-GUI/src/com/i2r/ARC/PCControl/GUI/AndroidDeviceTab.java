package com.i2r.ARC.PCControl.GUI;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class AndroidDeviceTab extends JPanel {

	private static final long serialVersionUID = 4857850415235003330L;

	private ARCControlLink link;
	private JTabbedPane featuresPane;
	private JPanel newTasksPanel;
	private JPanel runningTasksPanel;
	private JPanel logPanel;
	
	public AndroidDeviceTab(String name, ARCControlLink link){
		this.setName(name);
		this.link = link;
		this.featuresPane = createFeaturesPane(link);
		this.newTasksPanel = createNewTaskPanel(link);
		this.runningTasksPanel = createRunningTasksPanel(link);
		this.logPanel = createLogPanel(link);
		
		
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
			
		//TODO: make this look like sketch
		
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 2;
		gc.gridwidth = 1;
		this.add(featuresPane, gc);
		
		
		gc.gridx = 1;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		this.add(newTasksPanel, gc);
		
		
		gc.gridx = 2;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.gridwidth = 1;
		this.add(runningTasksPanel, gc);
		
		
		gc.gridx = 1;
		gc.gridy = 1;
		gc.gridheight = 1;
		gc.gridwidth = 2;
		this.add(logPanel, gc);
	}
	
	
	
	public ARCControlLink getLink(){
		return link;
	}
	
	
	public void refresh(){
		featuresPane.repaint();
		newTasksPanel.repaint();
		runningTasksPanel.repaint();
		logPanel.repaint();
		repaint();
	}
	
	
	private static JTabbedPane createFeaturesPane(ARCControlLink link){
		JTabbedPane featuresPane = new JTabbedPane();
		
		for(FeaturePanel set : link.getFeatureSets()){
			
		}
		
		return featuresPane;
	}
	
	

	
	
	private static JPanel createNewTaskPanel(ARCControlLink link){
		JPanel panel = new JPanel();
		
		return panel;
	}
	
	
	private static JPanel createRunningTasksPanel(ARCControlLink link){
		JPanel panel = new JPanel();
		
		return panel;
	}
	
	
	private static JPanel createLogPanel(ARCControlLink link){
		JPanel panel = new JPanel();
		
		return panel;
	}
	
	
	
	
}
