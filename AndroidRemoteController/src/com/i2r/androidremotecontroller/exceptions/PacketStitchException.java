package com.i2r.androidremotecontroller.exceptions;

/**
 * This class defines a type of {@link Exception} that
 * should be thrown when two {@link CommandPacket} objects
 * fail to stitch together successfully.
 * @author Josh Noel
 *
 */
public class PacketStitchException extends Exception {

	private static final long serialVersionUID = 1805970353365645954L;
	
	
	/**
	 * see {@link PacketStitchException} for details
	 */
	public PacketStitchException(){
		super();
	}
	
	
	/**
	 * see {@link PacketStitchException} for details
	 * @param message - the message to go with this exception.
	 */
	public PacketStitchException(String message){
		super(message);
	}

}
