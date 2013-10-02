package com.i2r.androidremotecontroller.main.databouncer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.i2r.androidremotecontroller.R;

/**
 * This class models an activity where the user
 * can specify where an android device should
 * bounce data to.
 * @author Josh Noel
 */
public class DataBouncerActivity extends Activity
		implements ChannelListener, ActionListener,
				   PeerListListener, OnItemClickListener,
				   OnCheckedChangeListener {
	
	private static final String TAG = "DataBouncerActivity";
	private static final String UPDATE = "Update List";
	
	private ListView bouncerOptions;
	private Switch capturePointSwitch;
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
		this.capturePointSwitch = (Switch) findViewById(R.id.capture_point);
		this.manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
		
		this.devices = null;
		
		this.adapter.add(UPDATE);
		this.bouncerOptions.setAdapter(adapter);
		this.bouncerOptions.setOnItemClickListener(this);
		this.capturePointSwitch.setOnCheckedChangeListener(this);
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
		this.channel = null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSuccess() {
		Log.d(TAG, "channel created, getting peer list");
		this.manager.requestPeers(channel, this);
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFailure(int reason) {
		
		String result = "failed to discover peers";
		
		switch(reason){
		case WifiP2pManager.P2P_UNSUPPORTED:
			result = "this device does not support wifi direct";
			break;
			
		case WifiP2pManager.BUSY:
			result = result + "p2p manager is in busy state";
			break;
			
		case WifiP2pManager.NO_SERVICE_REQUESTS:
			result = result + " : no service requests set";
			break;
			
		case WifiP2pManager.ERROR:
			result = result + " : internal error";
			
			default:
				result = result + " : unknown error";
				break;
		}
		
		inform(result);
		Log.e(TAG, result);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onChannelDisconnected() {
		String result = "main channel disconnected, attempting to re-establish...";
		Log.e(TAG, result);
		inform(result);
		this.channel = manager.initialize(this, getMainLooper(), this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCheckedChanged(CompoundButton button, boolean flag) {
		bouncer.setCapturePoint(flag);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onPeersAvailable(WifiP2pDeviceList peers) {
		Log.d(TAG, "new peer list available, updating current list");
		
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
		if(manager != null && devices != null && channel != null){
			respondToClick(position);
		}
	}
	
	
	/**
	 * Helper method for {@link #onItemClick(AdapterView, View, int, long)}.
	 * Creates a new {@link WifiDirectConnector} if the selected item has
	 * not already been added, otherwise removes the item at the given position.
	 * 
	 * @param position - the position of the {@link WifiP2pDevice} to add or
	 * remove from this {@link DataBouncer}
	 */
	private void respondToClick(int position){
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
			WifiDirectConnector connector = new WifiDirectConnector(manager, channel, device);
			
			// if the DataBouncer already has this connector, remove it
			if(bouncer.contains(connector)){
				bouncer.remove(connector);
				inform(device.deviceName + " removed from data bouncer");
                this.adapter.insert(this.adapter.getItem(position) + " : disconnected", position);
				
				// else add the connector to the bouncer array
			} else {
				bouncer.add(connector);
				inform(device.deviceName + " added to data bouncer");
				this.adapter.insert(this.adapter.getItem(position) + " : connected", position);
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
	
} // end of DataBouncerActivity class
