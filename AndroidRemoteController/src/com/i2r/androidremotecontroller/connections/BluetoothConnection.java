package com.i2r.androidremotecontroller.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.i2r.androidremotecontroller.RemoteControlActivity;
import com.i2r.androidremotecontroller.exceptions.NoBluetoothSocketFoundException;

/**
 * This class models a bluetooth implementation of a remote
 * connection. It takes in a BluetoothSocket and retrieves 
 * I/O streams from it.
 * @author Josh Noel
 */
public class BluetoothConnection implements RemoteConnection {

	private static final int BUFFER_SIZE = 1024;
	private static final String TAG = "BluetoothConnection";
	
	private final BluetoothSocket socket;
	private final InputStream input;
	private final OutputStream output;
	private Activity activity;
	private int bytesRead;
	private byte[] buffer;
	private boolean connected;
	
	/**
	 * Constructor
	 * attempts to get I/O streams from the given socket
	 * @param socket - the socket to get streams from for reading and writing
	 * @throws NoBluetoothSocketFoundException if socket is null
	 */
	public BluetoothConnection(BluetoothSocket socket, Activity activity) throws NoBluetoothSocketFoundException {
		
		if(socket == null){
			// no socket is found, so no connection can be made
			throw new NoBluetoothSocketFoundException();
		}
		
		// initialize this connection
		this.socket = socket;
		this.activity = activity;
		this.bytesRead = 0;
		this.buffer = new byte[BUFFER_SIZE];
		
		InputStream tempIn = null;
		OutputStream tempOut = null;
		
		try{
			// try to get I/O streams from the socket
			tempIn = socket.getInputStream();
			tempOut = socket.getOutputStream();
			this.connected = true;
			Log.d(TAG, "I/O streams retreived");
			
		} catch (IOException e){
			this.connected = false;
			Log.e(TAG, "I/O stream retreival failed");
		}
		
		input = tempIn;
		output = tempOut;
	}
	
	//**********************************************************************|
	// See {@link RemoteConnection} for documentation of these overrides ---|
	//**********************************************************************|
	


	@Override
	public boolean isConnected() {
		return connected;
	}

	
	
	@Override
	public void write(byte[] bytes) {
		try{
			output.write(bytes);
			Log.d(TAG, "bytes successfully written to socket - " + bytes.length);
		} catch(IOException e){
			Log.e(TAG, "error writing bytes to socket - " + bytes.length);
		}
	}
	
	

	@Override
	public void run() {
		
		Log.d(TAG, "running connection thread, socket status - " 
				+ Boolean.toString(connected));
		while(connected){
			try{
				
				bytesRead = input.read(buffer);
				
				if(bytesRead > 0){
					Log.d(TAG, "bytes read successfully - " + bytesRead);
					
					byte[] temp = new byte[bytesRead];
					
					for(int i = 0; i < bytesRead; i++){
						temp[i] = buffer[i];
					}
					
					Intent intent = new Intent(RemoteControlActivity.ACTION_CONNECTION_READ);
					intent.putExtra(RemoteControlActivity.EXTRA_COMMAND, new String(temp));
					activity.sendBroadcast(intent);
					
				} else {
					Log.e(TAG, "zero bytes read");
				}
				
			} catch(IOException e){
				connected = false;
				Log.d(TAG, "socket was closed by the remote device");
			}
		}
	}


	@Override
	public void disconnect() {
		connected = false;
		try{
			socket.close();
			Log.d(TAG, "socket closed successfully");
		} catch(IOException e){
			Log.e(TAG, "IOException while closing socket");
		}
	}

}
