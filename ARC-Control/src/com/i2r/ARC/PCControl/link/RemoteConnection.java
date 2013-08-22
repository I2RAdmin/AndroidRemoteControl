package com.i2r.ARC.PCControl.link;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstract class for a remote connection, which is what is used to read and write data to a remote device.
 * Remote connections are created by a {@link RemoteLink} and are typed to kind of data that will be written 
 * and received from the data streams to and from the socket
 * 
 * The streams generated are {@link DataInputStream} for input and {@link DataOuputStream} for output
 * 
 * @author Johnathan Pagnutti
 * @param <T> the action of data that the {@link RemoteConnection} is going to deal with
 */
public abstract class RemoteConnection<T> {

	public InputStream dataIn;
	public OutputStream dataOut;
	
	/**
	 * Close this connection, which closes both tied input and output streams
	 * Note- the actual details of connection closing are left to the extenders of this class, and due to the amount
	 * of possibilities for what a connection is, there is no higher general reference to one.
	 * 
	 * So, if you implement this method, close the connection.  Don't be that guy.  No one likes him.
	 */
	public abstract void close();
}
