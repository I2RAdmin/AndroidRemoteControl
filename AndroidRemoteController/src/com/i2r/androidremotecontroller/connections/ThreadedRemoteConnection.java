package com.i2r.androidremotecontroller.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

/**
 * This class models the abstract definition of a
 * {@link RemoteConnection} where the connection now
 * acts as a self contained process in its own thread.
 * @author Josh Noel
 */
public abstract class ThreadedRemoteConnection
		extends Thread implements RemoteConnection, DataResponder<byte[]> {
	
	private static final String TAG = "ThreadedRemoteConnection";
	
	public static final int DEFAULT_BUFFER_SIZE = 1024;

	private InputStream in;
	private OutputStream out;
	private String disconnectMessage;
	
	private boolean connected;
	private int bytesRead;
	private byte[] buffer;
	
	/**
	 * Constructor<br>
	 * Creates a new {@link RemoteConnection} that extends
	 * {@link Thread}, so it can be treated as a contained
	 * process.
	 * @param context - the context in which this connection
	 * was created 
	 * @param in - the input stream of this connection
	 * @param out - the output stream of this connection
	 */
	public ThreadedRemoteConnection(InputStream in, OutputStream out){
		this.in = in;
		this.out = out;
		this.connected = (in != null && out != null) ? true : false;
		this.bytesRead = 0;
		this.buffer = new byte[DEFAULT_BUFFER_SIZE];
		this.disconnectMessage = null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void run() {
		
		Log.i(TAG, "starting read thread");
		while(connected){
			try{
				bytesRead = in.read(buffer);
				if(bytesRead > 0){
					Log.d(TAG, "bytes read successfully - " + bytesRead);
					byte[] trimmed = new byte[bytesRead];
					for(int i = 0; i < bytesRead; i++){
						trimmed[i] = buffer[i];
					}
					onDataReceived(trimmed);
				}
			} catch(IOException e){
				Log.d(TAG, "connection closed by remote device");
				this.connected = false;
				this.disconnectMessage = e.getMessage();
				onDisconnected();
			}
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized final void write(byte[] bytes) {
		if(out != null){
			try{
				out.write(bytes);
				Log.d(TAG, "successfully wrote bytes to stream - "
						+ bytes.length);
			} catch (IOException e){
				Log.e(TAG, "error writing bytes to stream - "
						+ bytes.length);
				this.connected = false;
				this.disconnectMessage = e.getMessage();
				onDisconnected();
			}
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void disconnect() {
		Log.d(TAG, "disconnecting streams and closing connection");
		connected = false;
		
		if(in != null){
		try{in.close();}
		catch(IOException e){Log.e(TAG, e.getMessage());}}
		
		if(out != null){
		try{out.flush(); out.close();}
		catch(IOException e){Log.e(TAG, e.getMessage());}}
		
		onDisconnected();
	}
	
	
	/**
	 * Callback for when this connection has been
	 * terminated, regardless of whether the termination
	 * was local or by the remote device.
	 */
	public abstract void onDisconnected();
	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isConnected() {
		return connected;
	}
	
	
	/**
	 * Query for the message received when
	 * this connection has been terminated.
	 * @return the message received when this
	 * connection has been terminated, or
	 * null if this connection is still alive.
	 */
	public final String getDisconnectMessage(){
		return disconnectMessage;
	}

} // end of ThreadedRemoteConnection class
