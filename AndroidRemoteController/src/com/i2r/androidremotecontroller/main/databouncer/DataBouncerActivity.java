package com.i2r.androidremotecontroller.main.databouncer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ARC.Constants;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.i2r.androidremotecontroller.R;

/**
 * This class models an activity where the user
 * can specify where an android device should
 * bounce data to.
 * @author Josh Noel
 */
public class DataBouncerActivity extends Activity
		implements ChannelListener, ActionListener, PeerListListener, OnItemClickListener {
	
	private static final String TAG = "DataBouncerActivity";
	private static final String UPDATE = "Update List";
	
	private ListView bouncerOptions;
	private ArrayAdapter<String> adapter;
	private DataBouncer bouncer;
	private WifiP2pManager manager;
	private Collection<WifiP2pDevice> devices;
	private Channel channel;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_data_bouncer);
		
		this.bouncer = DataBouncer.getInstance();
		this.bouncerOptions = (ListView) findViewById(R.id.peer_list);
		this.manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
		this.devices = null;
		
		this.bouncerOptions.setAdapter(adapter);
		this.bouncerOptions.setOnItemClickListener(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume(){
		super.onResume();
		this.channel = manager.initialize(this, getMainLooper(), this);
		this.manager.discoverPeers(channel, this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPause(){
		super.onPause();
		this.manager.cancelConnect(channel, null);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFailure(int reason) {
		Log.e(TAG, "failed to discover peers : error code - " + reason);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSuccess() {
		manager.requestPeers(channel, this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onChannelDisconnected() {
		Log.e(TAG, "main channel disconnected, attempting to re-establish...");
		this.channel = manager.initialize(this, getMainLooper(), this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		
		this.devices = peers.getDeviceList();
		ArrayList<String> temp = new ArrayList<String>();
		
		for(WifiP2pDevice device : peers.getDeviceList()){
			temp.add(device.deviceName);
		}
		
		adapter.clear();
		adapter.add(UPDATE);
		adapter.addAll(temp);
		inform("peer list updated");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		
		// if position is 0 in the list, user has selected "Update List"
		if(position == 0){
			this.manager.discoverPeers(channel, this);
			
			// else the user has selected a bouncer to add or remove
		} else {
			
			Iterator<WifiP2pDevice> iter = devices.iterator();
			
			// get to the device selected
			for(int i = 0; i < position && iter.hasNext(); i++){
				iter.next();
			}
			
			// create a new DataBouncerConnector with the obtained device
			WifiP2pDevice device = iter.next();
			DataBouncerConnector connector = new DataBouncerConnector(this, DataBouncerConnector.CLIENT,
					device.deviceAddress, Constants.Info.WIFI_PORT, device.deviceName);
			
			// if the DataBouncer already has this connector, remove it
			if(bouncer.contains(connector)){
				bouncer.remove(connector);
				inform(device.deviceName + " removed from data bouncer");
				
				// else add the connector to the bouncer array
			} else {
				bouncer.add(connector);
				inform(device.deviceName + " added to data bouncer");
			}
		}
	}
	
	
	/**
	 * Helper method for informing the
	 * user about this application's status.
	 * @param info - the information to display
	 * to the user.
	 */
	private void inform(String info){
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}
	
}
