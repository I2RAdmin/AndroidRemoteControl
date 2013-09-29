package com.i2r.androidremotecontroller.main.databouncer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;

import com.i2r.androidremotecontroller.connections.DataResponder;

public class DataBouncerReceiver extends BroadcastReceiver {

	private IntentFilter filter;
	private DataResponder<byte[]> responder;
	
	public DataBouncerReceiver(DataResponder<byte[]> responder){
		this.responder = responder;
		this.filter = new IntentFilter();
		this.filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
			NdefMessage[] msgs = (NdefMessage[])
					intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			responder.onDataReceived(msgs[0].getRecords()[0].getPayload());
		} 
	}
	
	public IntentFilter getFilter(){
		return filter;
	}

}
