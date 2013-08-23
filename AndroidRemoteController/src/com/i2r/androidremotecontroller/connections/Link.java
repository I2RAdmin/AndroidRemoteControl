package com.i2r.androidremotecontroller.connections;

import java.util.List;

import android.content.Context;

/**
 * This interface models a generic link between two remote
 * devices, which have similar means for communicating (i.e.,
 * Bluetooth, WifiDirect, USB, etc...). This should be used
 * to define how two devices can find and establish a connection
 * to each other. The generics should be an object representation
 * of a remote device to connect to, i.e. WifiP2pDevice,
 * BluetoothDevice, etc.
 * @author Josh Noel
 */
public interface Link<E> {
	
	
	/**
	 * Used for server side connecting. This method
	 * will typically block in concrete implementations,
	 * and should be called in a separate worker thread.
	 * @return a {@link ThreadedRemoteConnection} to a controller
	 * device if one was found, else returns null.
	 */
	public ThreadedRemoteConnection listenForRemoteConnection();
	
	
	/**
	 * Used for client side connecting. This method
	 * sends a request to a remote object to establish
	 * a new connection. Unlike {@link #listenForRemoteConnection()},
	 * this method will return immediately.
	 * @param remote - the remote device to connect to.
	 * @return a {@link ThreadedRemoteConnection} to the remote
	 * device if the device accepted this request, else
	 * returns null.
	 */
	public ThreadedRemoteConnection connectTo(E remote);

	
	/**
	 * Perform a radial search for other devices in the immediate
	 * area, to seek out all possible connections. This method
	 * requires that this device is discoverable to other devices.
	 * This method will return immediately, so {@link #isSearchingForLinks()}
	 * should be used to wait for the search to be complete.
	 * Afterwards, {@link #getLinks()} can be used to obtain a
	 * list of devices to connect with.
	 */
	public void searchForLinks();
	
	
	/**
	 * Query for this link's searching state
	 * @return true if this link is currently
	 * in the discovery process, false otherwise
	 */
	public boolean isSearchingForLinks();
	
	
	/**
	 * Stops all connection searching processes
	 * currently active in this Link.
	 */
	public void haltConnectionDiscovery();
	
	
	/**
	 * The resulting list of available potential connections after
	 * {@link #searchForLinks()} has been called. This method may
	 * return an empty list if {@link #searchForLinks()} was not
	 * called prior to its invocation.
	 * @return a new list of all possible connections for this device.
	 */
	public List<E> getLinks();
	
	
	/**
	 * Query about the listening state of this Link
	 * @return true if this link is set to listen
	 * for connections, false otherwise
	 */
	public boolean isServerLink();

	
	
	/**
	 * Query for this link's context. This is
	 * purely a convenience query for any objects
	 * using a concrete implementation of this interface.
	 * @return the {@link Context} in which
	 * this Link was created.
	 */
	public Context getContext();
	
}
