package com.i2r.androidremotecontroller.main.databouncer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import com.i2r.androidremotecontroller.connections.DataResponder;

/**
 * This class models a receiver for data bouncing.
 * In this application's current build, this is not used.
 * @author Josh Noel
 */
public class DataBouncerReceiver extends BroadcastReceiver {

	private IntentFilter filter;
	private DataResponder<WifiP2pInfo> p2p_info;
	private DataResponder<NetworkInfo> network;
	
	public DataBouncerReceiver(){
		this.p2p_info = null;
		this.network = null;
		this.filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		
		if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
			if(p2p_info != null){
				WifiP2pInfo info = (WifiP2pInfo)
						intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
				p2p_info.onDataReceived(info);
			}
			
			if(network != null){
				NetworkInfo net = (NetworkInfo)
						intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
				network.onDataReceived(net);
			}
			
		} else if(action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)){
			// TODO: respond to this action
			
		} else if(action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){
			// TODO: respond to this action
		}
	}
	
	public IntentFilter getFilter(){
		return filter;
	}
	
	public void registerWifiP2pInfoResponder(DataResponder<WifiP2pInfo> responder){
		this.p2p_info = responder;
	}
	
	public void registerNetworkInfoResponder(DataResponder<NetworkInfo> responder){
		this.network = responder;
	}

}
