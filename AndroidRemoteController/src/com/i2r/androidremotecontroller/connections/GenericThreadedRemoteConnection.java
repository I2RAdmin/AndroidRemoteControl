package com.i2r.androidremotecontroller.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.i2r.androidremotecontroller.main.RemoteControlActivity;
import com.i2r.androidremotecontroller.main.ResponsePacket;

/**
 * This class models a generic connection with a controller device.
 * The connection should be established prior to creating an instance
 * of this class, so that this class can handle the bulk of communication.
 * @author Josh Noel
 * @see {@link ThreadedRemoteConnection}
 */
public class GenericThreadedRemoteConnection extends ThreadedRemoteConnection {

	private static final String TAG = "GenericRemoteConnection";
	
	private static final int BUFFER_SIZE = 1024;
	
	private boolean connected;
	private int bytesRead;
	private byte[] buffer;
	
	/**
	 * Constructor<br>
	 * takes an {@link InputStream} and an {@link OutputStream}
	 * from a connection that was established prior to this
	 * object's creation.
	 * @param context - the context to send broadcasts to when
	 * data is transferred on either the input or output stream.
	 * @param input - the input stream to read incoming data with.
	 * @param output - the output stream to write to whenever
	 * result data for this application is available.
	 * @see {@link ResponsePacket}
	 * @see {@link RemoteControlActivity#ACTION_CONNECTION_READ}
	 * @see {@link RemoteControlActivity#ACTION_CONNECTOR_RESPONDED}
	 * @see {@link ConnectionManager}
	 */
	public GenericThreadedRemoteConnection(Context context, InputStream input, OutputStream output){
		super(context, input, output);
		connected = (input != null && output != null) ? true : false;
		this.bytesRead = 0;
		this.buffer = new byte[BUFFER_SIZE];
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		
		Log.i(TAG, "starting read thread");
		while(connected){
			try{
				bytesRead = in().read(buffer);
				if(bytesRead > 0){
					Log.d(TAG, "bytes read successfully - " + bytesRead);
					String result = new String(buffer).substring(0, bytesRead);
					Intent intent = new Intent(RemoteControlActivity.ACTION_CONNECTION_READ);
					intent.putExtra(RemoteControlActivity.EXTRA_COMMAND, result);
					getManager().sendBroadcast(intent);
				}
			} catch(IOException e){
				Log.d(TAG, "connection closed by remote device");
				returnToMain();
			}
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] bytes) {
		if(out() != null){
			try{
				out().write(bytes);
				Log.d(TAG, "successfully wrote bytes to stream - " + bytes.length);
			} catch (IOException e){
				Log.e(TAG, "error writing bytes to stream - " + bytes.length);
				returnToMain();
			}
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		Log.d(TAG, "disconnecting streams and closing connection");
		connected = false;
		
		if(in() != null){
		try{in().close();}
		catch(IOException e){Log.e(TAG, e.getMessage());}}
		
		if(out() != null){
		try{out().flush(); out().close();}
		catch(IOException e){Log.e(TAG, e.getMessage());}}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel(){
		disconnect();
	}
	

	
	/**
	 * Helper method for notifying the
	 * main activity that this connection
	 * is no longer valid.
	 */
	private void returnToMain(){
		Log.d(TAG, "connection closed by remote device");
		Intent intent = new Intent(RemoteControlActivity.ACTION_CONNECTOR_RESPONDED);
		intent.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE, 
				"connection closed by remote device, listening for connection...");
		this.connected = false;
		getManager().sendBroadcast(intent);
	}
	
} // end of GenericRemoteConnection class
