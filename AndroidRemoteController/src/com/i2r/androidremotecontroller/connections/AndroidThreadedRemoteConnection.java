package com.i2r.androidremotecontroller.connections;

import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.i2r.androidremotecontroller.main.RemoteControlActivity;
import com.i2r.androidremotecontroller.main.RemoteControlReceiver;
import com.i2r.androidremotecontroller.main.ResponsePacket;
import com.i2r.androidremotecontroller.main.databouncer.DataBouncer;

/**
 * This class models a generic connection with a controller device.
 * The connection should be established prior to creating an instance
 * of this class, so that this class can handle the bulk of communication.
 * @author Josh Noel
 * @see {@link ThreadedRemoteConnection}
 */
public class AndroidThreadedRemoteConnection extends ThreadedRemoteConnection {

	private static final String TAG = "AndroidThreadedRemoteConnection";
	
	private LocalBroadcastManager manager;
	private Context context;
	private DataBouncer bouncer;
	
	private byte[] lastReceived;
	private boolean shouldReconnect;
	
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
	public AndroidThreadedRemoteConnection(Context context,
					InputStream input, OutputStream output){
		super(input, output);
		this.context = context;
		this.manager = LocalBroadcastManager.getInstance(context);
		this.lastReceived = null;
		this.bouncer = DataBouncer.getInstance();
		this.shouldReconnect = true;
	}
	
	
	
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
	public AndroidThreadedRemoteConnection(Context context,
					InputStream input, OutputStream output, boolean shouldReconnect){
		super(input, output);
		this.context = context;
		this.manager = LocalBroadcastManager.getInstance(context);
		this.lastReceived = null;
		this.bouncer = DataBouncer.getInstance();
		this.shouldReconnect = shouldReconnect;
	}
	
	
	
	
	/**
	 * Query for this connection's {@link Context}
	 * @return the context in which this connection
	 * was created
	 */
	public final Context getContext(){
		return context;
	}
	
	
	/**
	 * TODO: comment
	 * @return
	 */
	public final LocalBroadcastManager getLocalBroadcastManager(){
		return manager;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDataReceived(byte[] data){
		if(!DataBouncer.dataIsEqual(lastReceived, data)){
			if(bouncer.isEmpty() || bouncer.isCapturePoint()){
				String result = new String(data);
				Intent intent = new Intent(RemoteControlReceiver.ACTION_CONNECTION_READ);
				intent.putExtra(RemoteControlReceiver.EXTRA_COMMAND, result);
				context.sendBroadcast(intent);
			}
			lastReceived = data;
			bouncer.bounce(data);
		} else {
			Log.e(TAG, "duplicate data received, no action performed");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisconnected(){
		this.disconnect();
		if(shouldReconnect){
			Log.d(TAG, "connection closed by remote device");
			Intent intent = new Intent
					(RemoteControlReceiver.ACTION_CONNECTOR_RESPONDED);
			intent.putExtra(RemoteControlReceiver.EXTRA_INFO_MESSAGE, 
					"connection closed by remote device, listening for connection...");
			manager.sendBroadcast(intent);
		}
	}
	
	
	/**
	 * Query for the last chunk read on
	 * this connection's input stream.
	 * @return the last byte array chunk
	 * read on the input stream, or null
	 * if no data has been read yet.
	 */
	public byte[] getLastPacketReceived(){
		return lastReceived;
	}
	
	
} // end of GenericRemoteConnection class
