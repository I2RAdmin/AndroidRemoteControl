package com.i2r.ARC.PCControl.GUI;

import java.io.IOException;

import com.i2r.ARC.PCControl.Controller;

/**
 * This class acts as both an instantiator and
 * a middle-man to pass information between the
 * GUI and the {@link Controller} using a
 * {@link ArcControlLink}. Any classes that
 * extend this should also override the {@link #sendCommand(String)}
 * and {@link #onResponse(String)} methods, so as to
 * correctly transfer data.
 * @author Josh Noel
 */
public class ArcGuiController {
	
	
	private static ArcGuiController instance = new ArcGuiController();
	
	private ArcFrame frame;
	private ArcControlLink link;
	private boolean initialized;
	
	/**
	 * Constructor<br>
	 * creates a new controller with a GUI frame
	 * and a link for data interpretation. Be sure
	 * to check {@link #initialized()} before using
	 * this controller.
	 * @see {@link ArcFrame}
	 * @see {@link ArcControlLink}
	 */
	private ArcGuiController() {
		init();
	}
	
	/**
	 * Initializes this controller's {@link ArcControlLink}
	 * and {@link ArcFrame}
	 */
	public synchronized boolean init(){
		
		boolean initialized;
		
		try{
			this.link = new ArcControlLink(Controller.getInstance());
			this.frame = new ArcFrame();
			initialized = true;
		} catch (IOException e){
			e.printStackTrace();
			initialized = false;
		}
		
		this.initialized = initialized;
		
		return initialized;
	}
	
	
	/**
	 * Query for this controller's singleton instance
	 * @return the singleton instance of this controller.
	 */
	public static ArcGuiController getInstance(){
		return instance;
	}
	
	
	/**
	 * Query for this controller's state
	 * @return true if this controller was properly initialized,
	 * false otherwise. If this returns false, this controller
	 * should not be used for android communication.
	 */
	public boolean initialized(){
		return initialized;
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
	 * across this {@link ArcControlLink} to
	 * be executed.
	 */
	public void sendCommand(String command){
		link.sendCommand(command);
	}
	
	
	/**
	 * Used by this controller's {@link ArcControlLink}
	 * to update this GUI
	 * @param response - the response for the GUI to interpret
	 */
	public void onResponse(String response){
		frame.updateByResponse(response);
	}
}
