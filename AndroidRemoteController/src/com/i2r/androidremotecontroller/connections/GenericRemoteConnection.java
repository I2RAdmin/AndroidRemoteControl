package com.i2r.androidremotecontroller.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.i2r.androidremotecontroller.RemoteControlActivity;

public class GenericRemoteConnection implements RemoteConnection {

	private static final int BUFFER_SIZE = 1024;
	private static final String TAG = "GenericRemoteConnection";
	
	private LocalBroadcastManager manager;
	private Context context;
	private InputStream input;
	private OutputStream output;
	private boolean connected;
	private int bytesRead;
	private byte[] buffer;
	
	public GenericRemoteConnection(Context context, InputStream input, OutputStream output){
		this.context = context;
		this.manager = LocalBroadcastManager.getInstance(context);
		this.input = input;
		this.output = output;
		connected = (input != null && output != null) ? true : false;
		this.bytesRead = 0;
		this.buffer = new byte[BUFFER_SIZE];
	}
	
	@Override
	public void run() {
		
		Log.i(TAG, "starting read thread");
		while(connected){
			try{
				bytesRead = input.read(buffer);
				if(bytesRead > 0){
					Log.d(TAG, "bytes read successfully - " + bytesRead);
					String result = new String(buffer).substring(0, bytesRead);
					Intent intent = new Intent(RemoteControlActivity.ACTION_CONNECTION_READ);
					intent.putExtra(RemoteControlActivity.EXTRA_COMMAND, result);
					manager.sendBroadcast(intent);
				}
			} catch(IOException e){
				connected = false;
				Log.d(TAG, "connection closed by remote device");
				Intent intent = new Intent(RemoteControlActivity.ACTION_CONNECTOR_RESPONDED);
				intent.putExtra(RemoteControlActivity.EXTRA_CONNECTION_STATUS, false);
				manager.sendBroadcast(intent);
			}
		}
	}

	@Override
	public void write(byte[] bytes) {
		if(output != null){
			try{
				output.write(bytes);
				Log.d(TAG, "successfully wrote bytes to stream - " + bytes.length);
			} catch (IOException e){
				Log.e(TAG, "error writing bytes to stream - " + bytes.length);
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void disconnect() {
		Log.d(TAG, "disconnecting streams and closing connection");
		connected = false;
		if(input != null){try{input.close();}catch(IOException e){}}
		if(output != null){try{output.flush(); output.close();}catch(IOException e){}}
	}

	
	public Context getContext(){
		return context;
	}
	
}
