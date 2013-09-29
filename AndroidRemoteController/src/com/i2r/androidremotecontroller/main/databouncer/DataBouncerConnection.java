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
	
	/**
	 * {@inheritDoc}
	 */
	public DataBouncerConnection(InputStream in, OutputStream out) {
		super(in, out);
		this.bouncer = DataBouncer.getInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDataReceived(byte[] data) {
		bouncer.bounce(data);
	}

}
