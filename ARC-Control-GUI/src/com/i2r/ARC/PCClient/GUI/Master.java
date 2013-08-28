package com.i2r.ARC.PCClient.GUI;

public class Master {

	
	private static Master instance = new Master();
	
	
	private Master(){}
	
	
	public static Master getInstance(){
		return instance;
	}
	
	
	public void start(){
		// TODO: create frame and logic references.
	}
}
