package com.i2r.androidremotecontroller.connections;

import java.util.List;

/**
 * This interface models a generic link between two remote
 * devices, which have similar means for communicating (i.e.,
 * Bluetooth, WifiDirect, USB, etc...). This should be used
 * to define how two devices can find and establish a connection
 * to each other.
 * @author Josh Noel
 */
public interface Link<E> {
	
	
	/**
	 * Used for server side connecting. 
	 * @requires this device supports server sockets for
	 * connecting to.
	 * @ensures nothing
	 * @return a ConnectorThread object to use for listening
	 */
	public RemoteConnection listenForRemoteConnection();
	
	
	/**
	 * Used for client side connecting.
	 * @param remote - the remote device to connect to
	 * @requires the given remote object can be connected to
	 * @ensures nothing
	 * @return a RemoteConnection object if the connection
	 * attempt was successful.
	 */
	public RemoteConnection connectTo(Object remote);

	
	/**
	 * Perform a radial search for other devices in the immediate
	 * area, to seek out all possible connections.
	 * @requires this device can discover other devices and is discoverable
	 * @ensures all available connections to this device will be found
	 */
	public void searchForLinks();
	
	
	/**
	 * Stops all connection searching processes currently active in this Link.
	 */
	public void haltConnectionDiscovery();
	
	
	/**
	 * The resulting list of available potential connections after
	 * {@link #searchForLinks()} has been called.
	 * @return a new list of all possible connections for this device.
	 * @requires searchForLinks() has been called at least once prior to invocation
	 * @ensures a list of all the devices paired with this device will be returned
	 */
	public List<E> getLinks();
	
	
	/**
	 * Query about the listening state of this Link
	 */
	public boolean isListeningForConnections();
	
	
	
	/**
	 * Query about the searching state of this Link
	 */
	public boolean isSearchingForConnections();
	
	
}
