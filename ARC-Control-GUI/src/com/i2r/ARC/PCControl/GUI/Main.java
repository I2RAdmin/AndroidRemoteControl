package com.i2r.ARC.PCControl.GUI;


public class Main {

	public static void main(String[] args){

		ArcGuiController controller = ArcGuiController.getInstance();
		if(controller.initialized()){
		controller.start();
		} else {
			System.err.println("failed to initialize controller, shutting down");
		}
	}
}
