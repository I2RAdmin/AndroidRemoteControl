package com.i2r.androidremotecontroller.connections;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * This interface models an active connection to another device.
 * This connection should be obtained via a Link object. This
 * interface extends Runnable so that any ongoing connections
 * can run in a worker thread - any reading operations from this
 * connection should be placed in the run method of all
 * concrete implementations.
 * @author Josh Noel
 */
public interface RemoteConnection extends Runnable {
	
	
	/**
	 * Sends the given bytes across the current connection.
	 */
	public void write(byte[] bytes);
	

	/**
	 * Query about this connection's state.
	 */
	public boolean isConnected();
	
	
	/**
	 * Disconnects the current connection. This object should
	 * be disposed of after this is called, as the connection
	 * should not be re-established. This method should only be
	 * called when all communication is complete.
	 */
	public void disconnect();
	
	
	/**
	 * Query for this connection's input stream
	 * @return this connection's current input stream,
	 * or null if there is no connection
	 */
	public InputStream getInputStream();
	
	
	/**
	 * Query for this connection's output stream
	 * @return this connection's current output stream,
	 * or null if there is no connection
	 */
	public OutputStream getOutputStream();
	
}
