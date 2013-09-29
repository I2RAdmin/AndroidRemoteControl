package com.i2r.androidremotecontroller.connections;

import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.i2r.androidremotecontroller.main.RemoteControlActivity;
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

	private static final String TAG = "GenericRemoteConnection";
	
	private LocalBroadcastManager manager;
	private Context context;
	
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
	}
	
	
	/**
	 * Query for this connection's {@link Context}
	 * @return the context in which this connection
	 * was created
	 */
	protected final Context getContext(){
		return context;
	}
	
	
	/**
	 * TODO: comment
	 * @return
	 */
	protected final LocalBroadcastManager getLocalBroadcastManager(){
		return manager;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDataReceived(byte[] data){
		DataBouncer db = DataBouncer.getInstance();
		if(db.isCapturePoint()){
			String result = new String(data);
			Intent intent = new Intent(RemoteControlActivity.ACTION_CONNECTION_READ);
			intent.putExtra(RemoteControlActivity.EXTRA_COMMAND, result);
			manager.sendBroadcast(intent);
		}
		db.bounce(data);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void onDisconnected(String message){
		super.onDisconnected(message);
		Log.d(TAG, "connection closed by remote device");
		Intent intent = new Intent
				(RemoteControlActivity.ACTION_CONNECTOR_RESPONDED);
		intent.putExtra(RemoteControlActivity.EXTRA_INFO_MESSAGE, 
				"connection closed by remote device, listening for connection...");
		manager.sendBroadcast(intent);
	}
	
	
} // end of GenericRemoteConnection class
