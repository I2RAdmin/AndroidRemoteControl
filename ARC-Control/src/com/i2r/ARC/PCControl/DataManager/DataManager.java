/**
 * 
 */
package com.i2r.ARC.PCControl.DataManager;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.i2r.ARC.PCControl.link.RemoteConnection;

/**
 * The generic high level abstract class that defines how a data manager.
 * 
 * Data Managers take the connection streams gotten from some a passed in {@link RemoteConnection}, and reading and writing
 * to the streams from the connection.  The connection streams are of type {@link DataInputStream} to
 * receive data and {@link DataOuputStream} to send data.
 * 
 * Data Managers also have a {@link DataParser} of the same type as the data manager, to parse the data received from
 * the input stream
 *
 * @author Johnathan Pagnutti
 * @param <T> The type of data this data manager use for writing to the {@link RemoteConnection}.  It is important to note that the 
 * 			{@link DataManager} must obey the {@link RemoteConnection}'s parameterized type when sending data, so for most cases, 
 * 			{@link DataManager#write(Object)} must perform a conversion
 * @param <U> The type of data this data manager will read from the {@link RemoteConnection}
 *
 */
public abstract class DataManager<T, U> {

	/**
	 * {@link InputStream} stream to read data in
	 */
	public InputStream dataIn;

	/**
	 * {@link OutputStream} stream to write data out
	 */
	public OutputStream dataOut;

	/**
	 * {@link DataParser} parser to interpret data
	 */
	public DataParser<U> parser;
	
	/**
	 * Constructor.
	 * 
	 * @param source the remote connection to get the input and output streams from
	 *   
	 */
	public DataManager (RemoteConnection<U> source){
		this.dataIn = source.dataIn;
		this.dataOut = source.dataOut;
		
	}
	
	/**
	 * Read method.
	 * Under many implementations, this method will block, although it is not specified by the contract
	 * to do so.  This is the generic attempt to read data in and do something with that data.
	 * 
	 * It is not under contract, but the idea is that a {@link DataParser#parse(Object)} method will be called to parse
	 * objects received from the stream and then tell some other object to do something about it
	 * 
	 * @requires that there is a valid input stream to read from (the dataIn class variable != null)
	 * @ensures nothing
	 */
	public abstract void read();
	
	/**
	 * Write method.
	 * Under some implementations, this method may block, although it is not specified by the contract
	 * to do so.
	 * 
	 * @param dataElement the element to write to the data output stream
	 * @requires that there is a valid output stream to write to (the dataOut class variable != null)
	 * @ensures nothing.  Not even that the data got written.  Nope.  #DealWithIt
	 */
	public abstract void write(T dataElement);
	
}
