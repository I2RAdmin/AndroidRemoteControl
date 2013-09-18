package com.i2r.ARC.PCControl.GUI.Device;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class SensorsPanel extends AbstractDeviceTabPanel {

	private static final long serialVersionUID = 1L;
	private static final String SENSOR_LABEL = "Sensors";

	private GridBagConstraints constraints;
	
	public SensorsPanel(ArcControlDevice device) {
		super(device);
		this.constraints = getSensorsPanelConstraints();
		
		JLabel sensorsLabel = new JLabel(SENSOR_LABEL);
		JTabbedPane sensorTabs = new JTabbedPane(JTabbedPane.TOP);
		
		this.setLayout(new BorderLayout(0, 0));
		
		for(GuiSensor sensor : device.getSensorList()){
		     sensorTabs.addTab(sensor.getName(), new JScrollPane(sensor));
		}
		
		this.add(sensorsLabel, BorderLayout.NORTH);
		this.add(sensorTabs, BorderLayout.CENTER);
	}

	@Override
	public GridBagConstraints getConstraints() {
		return constraints;
	}

	@Override
	public void addInfo(String info) {
        // sensors panel is immutable, so this does nothing
	}

	@Override
	public void removeInfo(String info) {
        // sensors panel is immutable, so this does nothing
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

}
