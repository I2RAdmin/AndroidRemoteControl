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
 * TODO: comment
 * @author Josh Noel
 */
public class SocketLink implements Link<Object> {

	private static final String TAG = "SocketLink";
	
	private Activity activity;
	private boolean isServer;
	
	public SocketLink(Activity activity, boolean isServer){
		this.activity = activity;
		this.isServer = isServer;
	}
	
	
	@Override
	public RemoteConnection listenForRemoteConnection() {
		RemoteConnection connection = null;
		try {
			ServerSocket socket = new ServerSocket(Constants.Info.WIFI_PORT);
			Socket result = socket.accept();

			if (result != null) {
				connection = new GenericRemoteConnection(activity,
						result.getInputStream(), result.getOutputStream());
			}
			
		} catch (IOException e) {
			Log.e(TAG, "error creating connection from port");
			e.printStackTrace();
		}
		return connection;
	}

	
	@Override
	public RemoteConnection connectTo(Object remote) {
		RemoteConnection connection = null;
		try {
			Socket socket = new Socket(Constants.Info.CONTROLLER_IP_ADDRESS,
					Constants.Info.WIFI_PORT);
			connection = new GenericRemoteConnection(activity, 
					socket.getInputStream(), socket.getOutputStream());
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return connection;
	}

	@Override
	public void searchForLinks() {
		
	}

	@Override
	public boolean isSearchingForLinks() {
		return false;
	}

	@Override
	public void haltConnectionDiscovery() {
		
	}

	@Override
	public List<Object> getLinks() {
		ArrayList<Object> temp = new ArrayList<Object>(1);
		temp.add(new Object());
		return temp;
	}

	@Override
	public boolean isServerLink() {
		return isServer;
	}


	@Override
	public Context getContext() {
		return activity;
	}
	

}
