package com.i2r.ARC.PCControl.GUI;

import java.io.IOException;

public class Main {

	public static void main(String[] args){
		try {
			ARC_GUI_Controller controller = new ARC_GUI_Controller();
			controller.start();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("controller errored on creation");
		}
	}
}
