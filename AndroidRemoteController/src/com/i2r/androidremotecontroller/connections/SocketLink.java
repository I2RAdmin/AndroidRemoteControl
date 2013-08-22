package com.i2r.androidremotecontroller.connections;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ARC.Constants;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * This class models a socket connection finder that
 * implements the {@link Link} interface. Socket connections
 * in this case mean connections to the remote device by
 * means of using wifi to find an IP Address and a port
 * on the machine that the IP Address leads to.
 * @author Josh Noel
 * @see {@link ServerSocket}
 * @see {@link Socket}
 */
public class SocketLink implements Link<Object> {

	private static final String TAG = "SocketLink";
	
	private ServerSocket listener;
	private Socket socket;
	private Activity activity;
	private boolean isServer;
	
	/**
	 * Constructor<br>
	 * Creates a new SocketLink object with the given
	 * activity as the context in which it was created,
	 * and a boolean flag specifying whether it should
	 * act as a server or a client connector.
	 * @param activity - the activity in which this link was created.
	 * @param isServer - the flag specifying how this link should act.
	 * @see {@link ConnectionManager#CONNECTION_TYPE_CLIENT}
	 * @see {@link ConnectionManager#CONNECTION_TYPE_SERVER}
	 */
	public SocketLink(Activity activity, boolean isServer){
		this.activity = activity;
		this.isServer = isServer;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ThreadedRemoteConnection listenForRemoteConnection() {
		GenericThreadedRemoteConnection connection = null;
		try {
			listener = new ServerSocket(Constants.Info.WIFI_PORT);
			 socket = listener.accept();

			if (socket != null) {
				connection = new GenericThreadedRemoteConnection(activity,
						socket.getInputStream(), socket.getOutputStream());
			}
			
			listener.close();
			
		} catch (IOException e) {
			Log.e(TAG, "error creating connection from port");
			e.printStackTrace();
		}
		return connection;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ThreadedRemoteConnection connectTo(Object remote) {
		GenericThreadedRemoteConnection connection = null;
		try {
			socket = new Socket(Constants.Info.CONTROLLER_IP_ADDRESS,
					Constants.Info.WIFI_PORT);
			connection = new GenericThreadedRemoteConnection(activity, 
					socket.getInputStream(), socket.getOutputStream());
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return connection;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void searchForLinks() {
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSearchingForLinks() {
		return false;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void haltConnectionDiscovery() {
		if(listener != null){
			try{
				listener.close();
			} catch (IOException e){}
			listener = null;
		}
		
		if(socket != null){
			try{
				socket.close();
			} catch (IOException e){}
			socket = null;
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> getLinks() {
		ArrayList<Object> temp = new ArrayList<Object>(1);
		temp.add(new Object());
		return temp;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isServerLink() {
		return isServer;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Context getContext() {
		return activity;
	}
	
} // end of SocketLink class
