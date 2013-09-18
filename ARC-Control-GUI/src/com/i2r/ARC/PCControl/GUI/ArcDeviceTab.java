package com.i2r.ARC.PCControl.GUI;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class ArcDeviceTab extends JPanel {

	private static final long serialVersionUID = 4857850415235003330L;
	
	private static final String SENSOR_LABEL = "Sensors";

	private ArcControlDevice device;
	private JPanel sensorsPanel;
	private JPanel newTasksPanel;
	private JPanel runningTasksPanel;
	private JPanel logPanel;
	
	public ArcDeviceTab(ArcControlDevice device){
		this.device = device;
        this.setName(device.getName());
		
		GridBagLayout gbl_devicePanel = new GridBagLayout();
		gbl_devicePanel.columnWidths = new int[]{150, 150, 150, 0};
		gbl_devicePanel.rowHeights = new int[]{100, 100, 0};
		gbl_devicePanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_devicePanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		
		this.setLayout(gbl_devicePanel);
		this.sensorsPanel = createSensorsPanel(device);
		this.newTasksPanel = createNewTaskPanel(device);
		this.runningTasksPanel = createRunningTasksPanel(device);
		this.logPanel = createLogPanel(device);
		
		this.add(sensorsPanel, getSensorsPanelConstraints());
		this.add(newTasksPanel, getNewTaskPanelConstraints());
		this.add(runningTasksPanel, getRunningTasksPanelConstraints());
		this.add(logPanel, getLogPanelConstraints());
	}
	
	
	
	public ArcControlDevice getDevice(){
		return device;
	}
	
	public void addLogStatement(String statement){
		// TODO: dump statement to log panel
	}
	
	
	public void addRunningTask(String runningTask){
		// TODO: add task info to listing in GUI
	}
	
	public void removRunningeTask(String runningTask){
		// TODO: remove task from listing in GUI
	}
	
	public void refresh(){
		sensorsPanel.repaint();
		newTasksPanel.repaint();
		runningTasksPanel.repaint();
		logPanel.repaint();
		repaint();
	}
	
	
	public static JPanel createSensorsPanel(ArcControlDevice device){
		JPanel sensorsPanel = new JPanel();
		JLabel sensorsLabel = new JLabel(SENSOR_LABEL);
		JTabbedPane sensorTabs = new JTabbedPane(JTabbedPane.TOP);
		JButton updateSensorsButton = new JButton("Update Sensors");
		
		updateSensorsButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
                 				
			}
		});
		
		sensorsPanel.setLayout(new BorderLayout(0, 0));
		
		for(GuiSensor sensor : device.getSensorList()){
		     sensorTabs.addTab(sensor.getName(), new JScrollPane(sensor));
		}
		
		sensorsPanel.add(sensorsLabel, BorderLayout.NORTH);
		sensorsPanel.add(sensorTabs, BorderLayout.CENTER);
		sensorsPanel.add(updateSensorsButton, BorderLayout.SOUTH);
		
		return sensorsPanel;
	}
	
	
	public static GridBagConstraints getSensorsPanelConstraints(){
		GridBagConstraints gbc_sensorsPanel = new GridBagConstraints();
		gbc_sensorsPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_sensorsPanel.fill = GridBagConstraints.BOTH;
		gbc_sensorsPanel.insets = new Insets(0, 0, 0, 5);
		gbc_sensorsPanel.gridheight = 2;
		gbc_sensorsPanel.gridx = 0;
		gbc_sensorsPanel.gridy = 0;
		return gbc_sensorsPanel;
	}
	
	
	
	public static JPanel createNewTaskPanel(ArcControlDevice device){
		
		JPanel newTaskPanel = new JPanel();
		newTaskPanel.setLayout(new BorderLayout(0, 0));
		
		// TODO: make a drop down on left and options on right
		// JSplitPanel and something for drop down...
		
		return newTaskPanel;
	}
	
	
	
	public static GridBagConstraints getNewTaskPanelConstraints(){
		GridBagConstraints gbc_newTaskPanel = new GridBagConstraints();
		gbc_newTaskPanel.fill = GridBagConstraints.BOTH;
		gbc_newTaskPanel.insets = new Insets(0, 0, 5, 5);
		gbc_newTaskPanel.gridx = 1;
		gbc_newTaskPanel.gridy = 0;
		return gbc_newTaskPanel;
	}
	
	
	
	public static JPanel createRunningTasksPanel(ArcControlDevice device){
		JPanel runningTasksPanel = new JPanel();
		
		//TODO: implement running tasks panel
		
		return runningTasksPanel;
	}
	
	
	
	public static GridBagConstraints getRunningTasksPanelConstraints(){
		GridBagConstraints gbc_runningTasksPanel = new GridBagConstraints();
		gbc_runningTasksPanel.anchor = GridBagConstraints.NORTHEAST;
		gbc_runningTasksPanel.fill = GridBagConstraints.BOTH;
		gbc_runningTasksPanel.insets = new Insets(0, 0, 5, 0);
		gbc_runningTasksPanel.gridx = 2;
		gbc_runningTasksPanel.gridy = 0;
		return gbc_runningTasksPanel;
	}
	
	
	
	public static JPanel createLogPanel(ArcControlDevice device){
		JPanel logPanel = new JPanel();
		
		//TODO: implement logger here
		
		return logPanel;
	}
	
	
	public static GridBagConstraints getLogPanelConstraints(){
		GridBagConstraints gbc_logPanel = new GridBagConstraints();
		gbc_logPanel.anchor = GridBagConstraints.SOUTHEAST;
		gbc_logPanel.fill = GridBagConstraints.BOTH;
		gbc_logPanel.gridwidth = 2;
		gbc_logPanel.gridx = 1;
		gbc_logPanel.gridy = 1;
		return gbc_logPanel;
	}
	
	
	// TODO: make update button send command
	@SuppressWarnings("unused")
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			StringBuilder builder = new StringBuilder();
			builder.append(device.getIndex()).append(" ");
			builder.append(ArcControlLink.MODIFY).append(" ");
		}
	}
	
}
