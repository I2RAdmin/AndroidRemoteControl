package com.i2r.androidremotecontroller.exceptions;

public class NoDeviceFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoDeviceFoundException(){
		super();
	}
	
	
	public NoDeviceFoundException(String message){
		super(message);
	}
}
