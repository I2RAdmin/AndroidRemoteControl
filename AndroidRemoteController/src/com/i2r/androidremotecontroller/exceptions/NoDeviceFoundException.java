package com.i2r.androidremotecontroller.exceptions;

/**
 * This should be thrown whenever a null reference is
 * passed where a device object is supposed to be in
 * in this application. Device objects pertain to
 * objects that represent the remote PC that is controlling
 * this device. (examples are {@link UsbDevice}, {@link BluetoothDevice}
 * etc.)
 * @author Josh Noel
 *
 */
public class NoDeviceFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @see {@link NoDeviceFoundException}
	 */
	public NoDeviceFoundException(){
		super();
	}
	
	/**
	 * @see {@link NoDeviceFoundException}
	 */
	public NoDeviceFoundException(String message){
		super(message);
	}
}
