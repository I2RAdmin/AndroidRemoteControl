package com.i2r.androidremotecontroller.main.databouncer;

import java.io.InputStream;
import java.io.OutputStream;

import com.i2r.androidremotecontroller.connections.ThreadedRemoteConnection;


/**
 * This class models a connection to another android device, purely
 * for bouncing data along a chain so that it may reach its
 * proper destination.
 * @author Josh Noel
 *
 */
public class DataBouncerConnection extends ThreadedRemoteConnection {

	private DataBouncer bouncer;
	private byte[] lastReceived;
	
	/**
	 * {@inheritDoc}
	 */
	public DataBouncerConnection(InputStream in, OutputStream out) {
		super(in, out);
		this.bouncer = DataBouncer.getInstance();
		this.lastReceived = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDataReceived(byte[] data) {
		lastReceived = data;
		bouncer.bounce(data);
	}
	
	
	/**
	 * Query for the last chunk read on
	 * this connection's input stream.
	 * @return the last byte array chunk
	 * read on the input stream, or null
	 * if no data has been read yet.
	 */
	public byte[] getLastPacketReceived(){
		return lastReceived;
	}

}
