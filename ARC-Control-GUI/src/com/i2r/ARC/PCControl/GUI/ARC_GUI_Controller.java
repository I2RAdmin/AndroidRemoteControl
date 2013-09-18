package com.i2r.ARC.PCControl.GUI;

import java.io.IOException;

import com.i2r.ARC.PCControl.Controller;

/**
 * This class acts as both an instantiator and
 * a middle-man to pass information between the
 * GUI and the {@link Controller} using a
 * {@link ARCControlLink}. Any classes that
 * extend this should also override the {@link #sendCommand(String)}
 * and {@link #onResponse(String)} methods, so as to
 * correctly transfer data.
 * @author Josh Noel
 */
public class ARC_GUI_Controller {
	
	private ARCFrame frame;
	private ARCControlLink link;
	
	/**
	 * Constructor<br>
	 * creates a new controller with a GUI frame
	 * and a link for data interpretation.
	 * @throws IOException if the link streams
	 * could not be properly established.
	 * @see {@link ARCFrame}
	 * @see {@link ARCControlLink}
	 */
	public ARC_GUI_Controller() throws IOException {
		this.link = new ARCControlLink(this, Controller.getInstance());
		this.frame = new ARCFrame(this);
	}
	
	/**
	 * Starts the link to this application's
	 * {@link Controller} and presents the
	 * user with this GUI. This can be
	 * considered the main entry point of
	 * this program.
	 */
	public void start(){
		link.start();
		frame.setVisible(true);
	}
	
	
	/**
	 * Used by this GUI to send commands to
	 * the {@link Controller} for execution.
	 * @param command - the command to send
	 * across this {@link ARCControlLink} to
	 * be executed.
	 */
	public void sendCommand(String command){
		link.sendCommand(command);
	}
	
	
	/**
	 * Used by this controller's {@link ARCControlLink}
	 * to update this GUI
	 * @param response - the response for the GUI to interpret
	 */
	public void onResponse(String response){
		frame.updateByResponse(response);
	}
}
