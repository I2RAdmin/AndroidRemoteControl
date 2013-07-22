package com.i2r.androidremotecontroller.exceptions;

public class NoBluetoothAdapterFoundException extends Exception {


	private static final long serialVersionUID = 1L;


	public NoBluetoothAdapterFoundException(){
		super();
	}
	
	
	public NoBluetoothAdapterFoundException(String message){
		super(message);
	}
	
}
