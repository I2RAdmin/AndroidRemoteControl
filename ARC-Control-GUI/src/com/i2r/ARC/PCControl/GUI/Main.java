package com.i2r.ARC.PCControl.GUI;

/**
 * This class is the main entry point of this application.
 * @author Josh Noel
 */
public class Main {

	public static void main(String[] args){

		// obtain the singleton instance of this GUi controller
		ArcGuiController controller = ArcGuiController.getInstance();
		
		// if it initialized properly, show the GUI - otherwise shutdown
		if(controller.initialized()){
		controller.start();
		} else {
			System.err.println("failed to initialize controller, shutting down");
		}
	}
}
