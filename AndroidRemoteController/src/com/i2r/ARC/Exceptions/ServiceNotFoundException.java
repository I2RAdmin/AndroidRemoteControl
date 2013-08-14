package com.i2r.ARC.Exceptions;


/**
 * This should be thrown whenever a requested service
 * cannot be obtained. Cases may include requesting for
 * a sensor or sensor info that does not exist on the device,
 * or if this program attempts to access a sensor that is
 * already being used.
 * @author Josh Noel
 *
 */
public class ServiceNotFoundException extends Exception {


	private static final long serialVersionUID = 1L;

	private int type;

	/**
	 * @see {@link ServiceNotFoundException}
	 */
	public ServiceNotFoundException(){
		super();
		this.type = 0;
	}
	
	/**
	 * @see {@link ServiceNotFoundException}
	 */
	public ServiceNotFoundException(String message){
		super(message);
		this.type = 0;
	}
	
	/**
	 * @see {@link ServiceNotFoundException}
	 */
	public ServiceNotFoundException(int type){
		super();
		this.type = type;
	}
	
	/**
	 * @see {@link ServiceNotFoundException}
	 */
	public ServiceNotFoundException(String message, int type){
		super(message);
		this.type = type;
	}
	
	
	/**
	 * gets the type of this exception
	 * @return 0 if type was not set, else
	 * returns the set type. Types can be
	 * set for custom responses.
	 */
	public int getType(){
		return type;
	}
	
}
