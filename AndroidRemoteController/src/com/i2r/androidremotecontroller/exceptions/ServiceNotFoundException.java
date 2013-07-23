package com.i2r.androidremotecontroller.exceptions;

public class ServiceNotFoundException extends Exception {


	private static final long serialVersionUID = 1L;

	private int type;

	public ServiceNotFoundException(){
		super();
		this.type = 0;
	}
	
	
	public ServiceNotFoundException(String message){
		super(message);
		this.type = 0;
	}
	
	
	public ServiceNotFoundException(int type){
		super();
		this.type = type;
	}
	
	
	public ServiceNotFoundException(String message, int type){
		super(message);
		this.type = type;
	}
	
	
	public int getType(){
		return type;
	}
	
}
