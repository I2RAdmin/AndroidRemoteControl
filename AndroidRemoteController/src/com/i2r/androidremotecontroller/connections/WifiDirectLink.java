package com.i2r.androidremotecontroller.connections;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ARC.Constants;
import android.app.Activity;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

import com.i2r.androidremotecontroller.exceptions.ServiceNotFoundException;

public class WifiDirectLink implements Link<WifiP2pDevice> {
	
	private WifiP2pManager manager;
	private Channel channel;
	private WifiChannelListener listener;
	private WifiPeerListListener peerListener;
	private GenericActionListener connectListener;
	private Activity activity;
	private boolean isServer, searchingForLinks;
	
	public WifiDirectLink(Activity activity, boolean isServer, WifiP2pManager manager) throws ServiceNotFoundException {
		
		if(manager == null){
			throw new ServiceNotFoundException("wifiP2pManager is null on WifiDirectLink constructor");
		}
		
		this.activity = activity;
		this.isServer = isServer;
		this.manager = manager;
		this.channel = manager.initialize(activity, activity.getMainLooper(), listener);
		
		this.searchingForLinks = false;
		this.listener = new WifiChannelListener();
		this.peerListener = new WifiPeerListListener();
		this.connectListener = new GenericActionListener(GenericActionListener.TYPE_CONNECTION);
		
	}

	
	@Override
	public RemoteConnection listenForRemoteConnection() {
		Socket socket = null;
		GenericRemoteConnection connection = null;
		try {
			ServerSocket listener = new ServerSocket(Constants.Info.WIFI_PORT);
			socket = listener.accept();
			if(socket != null){
				connection = new GenericRemoteConnection(activity, 
						socket.getInputStream(), socket.getOutputStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return connection;
	}

	
	@Override
	public RemoteConnection connectTo(WifiP2pDevice remote) {
		
		WifiP2pDevice device = (WifiP2pDevice) remote;
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		GenericRemoteConnection connection = null;
		
		try {
			
			Socket socket = new Socket(device.deviceName, Constants.Info.WIFI_PORT);
			
			if(socket != null){
				connection = new  GenericRemoteConnection(activity, 
						socket.getInputStream(), socket.getOutputStream());
				
				// TODO: figure out what to do with this
				this.manager.connect(channel, config, connectListener);
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connection;
	}

	
	@Override
	public void searchForLinks() {
		peerListener.devices.clear();
		searchingForLinks = true;
		if(manager != null && channel != null){
			manager.requestPeers(channel, peerListener);
		}
	}

	
	@Override
	public void haltConnectionDiscovery() {
		manager.stopPeerDiscovery(channel, null);
		manager.cancelConnect(channel, null);
	}

	
	@Override
	public List<WifiP2pDevice> getLinks() {
		return peerListener.devices;
	}

	
	@Override
	public boolean isServerLink() {
		return isServer;
	}

	
	@Override
	public boolean isClientLink() {
		return !isServer;
	}
	
	
	@Override
	public boolean isSearchingForLinks() {
		return searchingForLinks;
	}

	
	//********************************************|
	// IMPLEMENTED INTERFACES --------------------|
	//********************************************|
	
	
	/**
	 * Lightweight class for responding to a channel being disconnected.
	 * @author Josh Noel
	 */
	private class WifiChannelListener implements ChannelListener {
		
		public void onChannelDisconnected() {
			channel = null;
		}
	} // end of WifiChannelListener class
	
	
	/**
	 * Lightweight class for storing a new list of available peers
	 * whenever they become available.
	 * @author Josh Noel
	 */
	private class WifiPeerListListener implements PeerListListener {
		
		private ArrayList<WifiP2pDevice> devices;
		
		public WifiPeerListListener(){
			this.devices = new ArrayList<WifiP2pDevice>();
		}
		
		@Override
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			devices.addAll(peers.getDeviceList());
			searchingForLinks = false;
		}
		
	} // end of WifiPeerListListener class
	
	
	private class GenericActionListener implements ActionListener {

		private static final int TYPE_CONNECTION = 0;
		
		private int type;
		
		public GenericActionListener(int type) {
			this.type = type;
		}
		
		@Override
		public void onFailure(int reason) {
			switch (type) {
			case TYPE_CONNECTION:
				
				break;
			
			default:
				break;
			}
		}

		@Override
		public void onSuccess() {
			switch (type) {
			case TYPE_CONNECTION:
				
				break;
			
			default:
				break;
			}
		}
		
	}
	
	
} // end of WifiDirectLink class 
