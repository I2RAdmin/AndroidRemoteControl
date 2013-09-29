package com.i2r.androidremotecontroller.main.databouncer;

import ARC.Constants;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.i2r.androidremotecontroller.R;
import com.i2r.androidremotecontroller.connections.DataResponder;

/**
 * This class models an activity where the user
 * can specify where an android device should
 * bounce data to.
 * @author Josh Noel
 */
public class DataBouncerActivity extends Activity implements DataResponder<byte[]> {
	
	
	private DataBouncerAdapter adapter;
	private ExpandableListView bouncerOptions;
	private NfcAdapter nfc;
	private DataBouncerReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_data_bouncer);
		
		this.adapter = new DataBouncerAdapter(this);
		this.bouncerOptions = (ExpandableListView) findViewById(R.id.bouncer_options);
		this.nfc = NfcAdapter.getDefaultAdapter(this);
		this.receiver = new DataBouncerReceiver(this);
		
		this.bouncerOptions.setAdapter(adapter);
		this.bouncerOptions.setOnChildClickListener(adapter);
	}
	
	
	@Override
	protected void onResume(){
		super.onResume();
		registerReceiver(receiver, receiver.getFilter());
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	
	public void askForDeviceTouch(){
		Toast.makeText(this, "Please touch devices together to add bouncer",
				Toast.LENGTH_SHORT).show();
	}
	
	
	
	public static NdefMessage getMessage(byte[] data){
		NdefRecord rec = null;
		NdefMessage msg = null;
		String domain = "com.i2r.androidremotecontroller.main.databouncer";
		String type = "socket-info-transfer";
		
		rec = NdefRecord.createExternal(domain, type, data);
		msg = new NdefMessage(new NdefRecord[]{rec});
		
		return msg;
	}
	
	
	public static String getMacAddress(Context context){
		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo connectionInfo = manager.getConnectionInfo();
		return connectionInfo.getMacAddress();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDataReceived(byte[] data) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void setupNfcPayload(int type){
		
		StringBuilder builder = new StringBuilder();
		builder.append(getMacAddress(this));
		builder.append(Constants.Delimiters.PACKET_DELIMITER);
		builder.append(type);
		
		NdefMessage message = getMessage(builder.toString().getBytes());
		this.nfc.setNdefPushMessage(message, this, (Activity[]) null);
	}
	
}
