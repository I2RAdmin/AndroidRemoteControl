package com.i2r.androidremotecontroller.main.databouncer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import ARC.Constants;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.util.Log;


/**
 * This class models a middle man that attempts to establish a connection
 * to a remote device based on the parameters given to it at creation.
 * The parameters given at creation will be used throughout this application's
 * life-cycle so that the user can add and remove bouncers dynamically. 
 * @author Josh Noel
 */
public class DataBouncerConnector implements ActionListener, ChannelListener {

	public static final int RECEIVING = 0;
	public static final int SENDING = 1;
	
	private static final String TAG = "DataBouncerConnector";
	
	private InetSocketAddress address;
	private DataBouncerConnection connection;
	private Socket socket;
	private String alias;
	private int type;
	
	/**
	 * Constructor<br>
	 * Attempts to create a new {@link DataBouncerConnection} with
	 * the parameters given.
	 * 
	 * @param activity - the activity to retrieve a {@link WifiP2pManager}
	 * from, in order to establish this connection.
	 * @param type - the relationship this connection has with the remote
	 * device - can be {@link #SENDING} or {@link #RECEIVING}
	 * @param address - the address to the remote device
	 * @param port - the port to communicate with the remote device on
	 * @param alias - the remote device's human readable name
	 * 
	 * @see {@link #hasConnection()}
	 * @see {@link #getConnection()}
	 */
	public DataBouncerConnector(Activity activity,
			int type, String address, int port, String alias){
		
		this.type = type;
		this.alias = alias;
		this.address = new InetSocketAddress(address, port);
		this.connection = null;
		
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = address;
		config.wps.setup = WpsInfo.PBC;
		
		WifiP2pManager manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
		Channel channel = manager.initialize(activity, activity.getMainLooper(), this);
		manager.connect(channel, config, this);
	}
	
	
	/**
	 * Query for this connector's connection state
	 * @return true if this connector currently has
	 * a valid {@link DataBouncerConnection}, false
	 * otherwise.
	 */
	public boolean hasConnection(){
		return connection != null && connection.isConnected();
	}
	
	
	/**
	 * Query for this connector's {@link DataBouncerConnection}
	 * @return null if the connection could not be established,
	 * else returns this connector's resulting connection upon
	 * creation.
	 */
	public DataBouncerConnection getConnection(){
		return connection;
	}
	
	
	/**
	 * Query for this connector's destination address.
	 * @return the {@link InetSocketAddress} created
	 * using the address and port given at creation.
	 */
	public InetSocketAddress getAddress(){
		return address;
	}
	
	
	/**
	 * Query for the alias (human readable name) of the
	 * device that this connector is attempting to
	 * communicate with.
	 * @return the remote device's human readable name
	 */
	public String getAlias(){
		return alias;
	}
	
	/**
	 * Query for this connector's creation type.
	 * @return the type of connection this connector
	 * will try to make.
	 * @see #RECEIVING
	 * @see #SENDING
	 */
	public int getType(){
		return type;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onChannelDisconnected() {
		
		Log.d(TAG, "channel disconnected, closing streams for "
				+ address.getHostName());
		
		if(connection != null && connection.isConnected()){
			connection.disconnect();
			connection = null;
		}
		
		if(socket != null){
			try{
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (Exception e){
				Log.e(TAG, "error shutting down connection : "
						+ e.getMessage());
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFailure(int reason) {
		Log.e(TAG, "failed to create connection : error number - " + reason);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSuccess() {
		
		try{
			
			if(type == SENDING){
				ServerSocket srv = new ServerSocket(Constants.Info.WIFI_PORT);
				this.socket = srv.accept();
				
			} else {
				this.socket = new Socket();
				this.socket.connect(address);
			}
			
			if(socket != null){
				this.connection = new DataBouncerConnection
						(socket.getInputStream(), socket.getOutputStream());
			}
			
		} catch (IOException e){
			Log.e(TAG, "failed to create connection : " + e.getMessage());
			this.connection = null;
			this.socket = null;
		}
	}
	
	
	/**
	 * Compares two {@link DataBouncerConnector}s for
	 * equality by the mac-address given at creation.
	 * @see {@link InetSocketAddress}
	 */
	@Override
	public boolean equals(Object other){
		boolean equal = false;
		if(other instanceof DataBouncerConnector){
			try{
				DataBouncerConnector temp = (DataBouncerConnector) other;
				equal = temp.address.getHostName().equals(address.getHostName());
			} catch (ClassCastException e){}
		}
		
		return equal;
	}
	
	
	/**
	 * Query for the string representation of this connector.
	 * @return the device's alias that this connector is
	 * connected (or attempting to connect) to, as well as
	 * its mac-address. Both of these attributes are given
	 * upon creation of a new instance of this class, and
	 * cannot be altered.
	 */
	@Override
	public String toString(){
		return new StringBuilder().append(alias)
				.append(" - ").append(address.getHostName()).toString();
	}
	
	
} // end of DataBouncerConnector class
