package com.i2r.ARC.PCControl.GUI.Device;

import java.awt.GridBagConstraints;

/**
 * 
 * @author Administrator
 *
 */
public class NewTaskPanel extends AbstractDeviceTabPanel {


	private static final long serialVersionUID = 4334647831641416912L;
	
	
	/**
	 * Constructor<br>
	 * @param device
	 */
	public NewTaskPanel(ArcControlDevice device, GridBagConstraints constraints) {
		super(device, constraints);
		
		//TODO: create dropdown list for sensor selection and
		// panels that fit the parameter requirements for each sensor.
		// Might use JSplitPane **
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInfo(String info) {
		// TODO Auto-generated method stub
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeInfo(String info) {
		// TODO Auto-generated method stub
	}

}
