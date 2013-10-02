package com.i2r.androidremotecontroller.main.databouncer;

import com.i2r.androidremotecontroller.connections.AndroidThreadedRemoteConnection;

/**
 * This interface models a generic connector
 * that constructs an {@link AndroidThreadedRemoteConnection}
 * to be used by this application's {@link DataBouncer}.
 * Currently only one class implements this interface:
 * {@link WifiDirectConnector} - but this data bouncer package
 * is designed to have multiple types of connections (not just
 * wifi-direct) so that it will have maximum flexibility in
 * the future.
 * @author Josh Noel
 */
public interface Connector {

	/**
	 * Query for this connector's resulting connection.
	 * @return the connection resulting from this
	 * connector, or null if this connector failed to
	 * establish a connection.
	 */
	public AndroidThreadedRemoteConnection getConnection();
	
	
	/**
	 * Query for this connector's current state
	 * @return true if this connector currently
	 * has a valid and open connection, false otherwise.
	 */
	public boolean hasConnection();
	
}
