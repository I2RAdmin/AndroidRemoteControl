package com.i2r.ARC.PCControl.GUI.Device;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

/**
 * This class models a graphical representation of a module
 * regarding an android device. Example modules (sub-classes) which will
 * appear in the GUI are a logging panel, a panel for starting
 * new tasks, a panel to display current running tasks, and
 * displaying manipulatable sensors which are available on the device.
 * @author Josh Noel
 */
public abstract class AbstractDeviceTabPanel extends JPanel {

	private static final long serialVersionUID = -2526380226664493899L;

	private ArcControlDevice device;
	private GridBagConstraints constraints;
	
	/**
	 * Constructor<br>
	 * Creates this panel tab in accordance with the available
	 * information in the given {@link ArcControlDevice}
	 * @param device - the android device to make a graphical
	 * representation of for the user to interact with.
	 */
	public AbstractDeviceTabPanel(ArcControlDevice device, GridBagConstraints constraints){
		this.device = device;
		this.constraints = constraints;
	}
	
	
	/**
	 * Query for this tab's root device
	 * @return the {@link ArcControlDevice} which
	 * was used to create this tab panel.
	 */
	public ArcControlDevice getDevice(){
		return device;
	}
	
	
	/**
	 * Query for this panel's {@link GridBagConstraints}
	 * @return the constraints currently set to this tab panel.
	 */
	public GridBagConstraints getConstraints(){
		return constraints;
	}
	
	
	/**
	 * Adds information received from the out stream of this application
	 * to this tab panel.
	 * @param info - the info to add to this GUI tab panel
	 */
	public abstract void addInfo(String info);
	
	
	/**
	 * Removes information received from the out stream of this application
	 * from this tab panel.
	 * @param info - the info to remove from this GUI tab panel
	 */
	public abstract void removeInfo(String info);
	
}
