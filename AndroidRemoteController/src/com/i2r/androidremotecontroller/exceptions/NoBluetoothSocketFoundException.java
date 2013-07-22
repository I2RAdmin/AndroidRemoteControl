package com.i2r.androidremotecontroller.exceptions;

public class NoBluetoothSocketFoundException extends Exception {

	private static final long serialVersionUID = 1L;


	public NoBluetoothSocketFoundException(){
		super();
	}
	
	
	public NoBluetoothSocketFoundException(String message){
		super(message);
	}
}
