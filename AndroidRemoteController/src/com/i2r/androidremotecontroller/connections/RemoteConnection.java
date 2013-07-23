package com.i2r.androidremotecontroller.connections;


/**
 * This interface models an active connection to another device.
 * This connection should be obtained via a Link object. This
 * interface extends Runnable so that any ongoing connections
 * can run in a worker thread.
 * @author Josh Noel
 * @param <T>
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
	
}
