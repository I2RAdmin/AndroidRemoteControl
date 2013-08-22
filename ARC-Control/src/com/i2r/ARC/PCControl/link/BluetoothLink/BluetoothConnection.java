/**
 * 
 */
package com.i2r.ARC.PCControl.link.BluetoothLink;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.link.RemoteConnection;

/**
 * Implements {@link RemoteConnection} of action <code>byte[]</code>
 * @see {@link RemoteConnection} for general contract information
 * 
 * A Bluetooth Connection for the PC Controller.  As per the general contract, and is the actual action returned from {@link Ble#c}
 * 
 * @author Johnathan Pagnutti
 *
 */
public class BluetoothConnection extends RemoteConnection<byte[]>{
	//you better believe its a logger
	static final Logger logger = Logger.getLogger(BluetoothConnection.class);
	
	StreamConnection conn;
	/**
	 * Implementation of the {@link RemoteConnection#RemoteConnection()}
	 * Constructor to create a new Bluetooth Connection
	 * 
	 * @param connURL the Bluetooth URL to connect to.
	 * @throws IOException 
	 */
	public BluetoothConnection(String connURL) throws IOException{
		logger.debug("Attempting to open stream from " + connURL);
		
		//attempt to get a connection...
		
		//open connection
		this.conn = (StreamConnection)Connector.open(connURL, Connector.READ_WRITE);
			
		//open the input stream
		dataIn = conn.openDataInputStream();
		logger.debug("Opened input stream.");
			
		//open the output stream
		dataOut = conn.openDataOutputStream();
		logger.debug("Opened output stream");
	}

	/**
	 * Closes the connection.  This directly closes the {@link StreamConnection} field {@link BluetoothConnection#conn}, so the underlying
	 * streams will also be closed.
	 * 
	 * Does not ensure that the streams are empty before it does this.
	 */
	@Override
	public void close() {
		try {
			dataOut.flush();
			dataOut.close();
			dataIn.close();
			conn.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
}

