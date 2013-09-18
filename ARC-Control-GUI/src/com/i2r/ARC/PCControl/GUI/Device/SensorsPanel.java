package com.i2r.ARC.PCControl.GUI.Device;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * This class models a sensors panel which will be part
 * of the interface defining the {@link ArcControlDevice}
 * given to this object upon creation.
 * @author Josh Noel
 */
public class SensorsPanel extends AbstractDeviceTabPanel {

	private static final long serialVersionUID = 1L;
	private static final String SENSOR_LABEL = "Sensors";
	
	/**
	 * {@inheritDoc}
	 * @param device - the {@link ArcControlDevice} to
	 * base this GUI tab off of.
	 */
	public SensorsPanel(ArcControlDevice device, GridBagConstraints constraints) {
		super(device, constraints);
		
		JLabel sensorsLabel = new JLabel(SENSOR_LABEL);
		JTabbedPane sensorTabs = new JTabbedPane(JTabbedPane.TOP);
		
		this.setLayout(new BorderLayout(0, 0));
		
		for(GuiSensor sensor : device.getSensorList()){
		     sensorTabs.addTab(sensor.getName(), new JScrollPane(sensor));
		}
		
		this.add(sensorsLabel, BorderLayout.NORTH);
		this.add(sensorTabs, BorderLayout.CENTER);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInfo(String info) {
        // sensors panel is immutable, so this does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeInfo(String info) {
        // sensors panel is immutable, so this does nothing
	}

}
