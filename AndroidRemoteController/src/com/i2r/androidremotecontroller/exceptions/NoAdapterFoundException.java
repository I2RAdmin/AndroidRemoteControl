package com.i2r.androidremotecontroller.exceptions;

public class NoAdapterFoundException extends Exception {


	private static final long serialVersionUID = 1L;

	private int type;

	public NoAdapterFoundException(){
		super();
		this.type = 0;
	}
	
	
	public NoAdapterFoundException(String message){
		super(message);
		this.type = 0;
	}
	
	
	public NoAdapterFoundException(int type){
		super();
		this.type = type;
	}
	
	
	public NoAdapterFoundException(String message, int type){
		super(message);
		this.type = type;
	}
	
	
	public int getType(){
		return type;
	}
	
}
