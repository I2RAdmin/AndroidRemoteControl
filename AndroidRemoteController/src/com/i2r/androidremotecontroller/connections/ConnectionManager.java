package com.i2r.androidremotecontroller.connections;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.i2r.androidremotecontroller.RemoteControlActivity;

/**
 * This class models a manager for a connection type
 * which implements the {@link Link} interface. This class implements
 * Thread, and when it is run, the given link object is used to attempt
 * to create a {@link RemoteConnection} object, which will then be stored
 * so that this object can be queried for it later.
 * @author Josh Noel
 */
public class ConnectionManager<E> {

	public static final boolean CONNECTION_TYPE_SERVER = true;
	public static final boolean CONNECTION_TYPE_CLIENT = false;
	
	private static final String TAG = "ConnectionManager";
	
	private Link<E> linker;
	private boolean isServer;
	private LocalBroadcastManager manager;
	private RemoteConnection connection;
	private Thread runningConnection;
	private ConnectionFinder finder;
	
	/**
	 * Constructor<br>
	 * creates a new Connection manager which will use the given {@link Link}
	 * interface to search for and create new connections with a remote controlling device.
	 * @param linker - the linker to use when handling connections for this application's
	 * {@link RemoteControlMaster}
	 * @param isServer - the flag to assert what kind of connections this
	 * manager should look for.
	 * @param activity - a reference to the activity that created this connection manager.
	 * @see {@link Link}
	 */
	public ConnectionManager(Link<E> linker, boolean isServer, Activity activity){
		this.linker = linker;
		this.isServer = isServer;
		this.manager = LocalBroadcastManager.getInstance(activity);
		this.runningConnection = null;
		this.finder = null;
	}
	
	
	/**
	 * Starts a side thread for device searching
	 * and inquiry. The thread uses the {@link Link}
	 * interface given upon this manager's creation
	 * to find connections.
	 */
	public void findConnection(){
		cancel();
		Log.d(TAG, "starting connection search thread");
		finder = new ConnectionFinder();
		finder.start();
	}

	
	/**
	 * Starts a new thread with the current
	 * RemoteConnection stored by this manager.
	 * If this manager's RemoteConnection instance is
	 * null, this method does nothing.
	 */
	public void startDataTransfer(){
		
		if(linker.isServerLink()){
			Log.d(TAG, "stopping connection discovery");
			linker.haltConnectionDiscovery();
			finder = null;
		}
		
		if(connection != null){
			Log.d(TAG, "starting data transfer on connection thread");
			runningConnection = new Thread(connection, "runnningConnectionThread");
			runningConnection.start();
		}
	}
	
	
	
	/**
	 * Cancels any ongoing connection, if there is one,
	 * and stops all this manager's side processes if
	 * it currently has any running.
	 */
	public void cancel(){
		
		Log.d(TAG, "all connections canceled");
		if(linker.isServerLink()){
			linker.haltConnectionDiscovery();
		}
		
		if(connection != null){
			connection.disconnect();
			connection = null;
		}
		
		runningConnection = null;
		finder = null;
	}
	
	
	/**
	 * Query for the type of connection this manager is making.
	 * @return true if this manager is set
	 * to listen for other devices, false otherwise.
	 */
	public boolean isServerConnection(){
		return isServer;
	}
	
	/**
	 * Query for the type of connection this manager is making.
	 * @return true if this manager is set
	 * to seek out other devices, false otherwise.
	 */
	public boolean isClientConnection(){
		return !isServer;
	}
	
	/**
	 * Query for the Link this manager is using to make a connection.
	 * @return a reference to this manager's current {@link Link} object.
	 */
	public Link<?> getLink(){
		return  linker;
	}
	
	/**
	 * Query for the connection of this manager.
	 * @return a reference to this manager's current connection,
	 * or null if this manager does not currently have an
	 * established connection.
	 * @see {@link RemoteConnection}
	 */
	public RemoteConnection getConnection(){
		return connection;
	}
	
	/**
	 * Query for the state of this manager's connection.
	 * @return true if this manager currently has an
	 * active and valid connection with a remote controlling
	 * device, false otherwise.
	 */
	public boolean hasConnection(){
		return connection != null && connection.isConnected();
	}
	
	
	
	/**
	 * Side thread for this connection manager to look for
	 * connections with. This is used so that waiting
	 * for a connection does not block the main thread.
	 * @author Josh Noel
	 */
	private class ConnectionFinder extends Thread {
		
		@Override
		public void run() {
			connection = null;
			if (isServer) {
				
				Log.d(TAG, "listening for a connection...");
				connection = linker.listenForRemoteConnection();
				
			} else {
				
				Log.d(TAG, "attempting to connect...");
				linker.searchForLinks();
				
				// wait for linker to find a fresh list of peers
				while(linker.isSearchingForLinks()){}
				
				// TODO: make this a list to choose from, if needed
				connection = linker.connectTo(linker.getLinks().get(0));
			}
			
			if(connection != null){
				runningConnection = new Thread(connection);
				Log.d(TAG, "connection thread created");
			}
			
			// notify main Activity that connection search has finished
			Intent intent = new Intent(RemoteControlActivity.ACTION_CONNECTOR_RESPONDED);
			String message = connection != null ? "connection found" : "no connection found";
			intent.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE, message);
			manager.sendBroadcast(intent);
		}
	}
	
}
