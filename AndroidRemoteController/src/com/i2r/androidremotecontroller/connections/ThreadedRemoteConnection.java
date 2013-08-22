package com.i2r.androidremotecontroller.connections;

import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

/**
 * This class models the abstract definition of a
 * {@link RemoteConnection} where the connection now
 * acts as a self contained process in its own thread.
 * @author Josh Noel
 */
public abstract class ThreadedRemoteConnection extends Thread
										implements RemoteConnection {
	

	private LocalBroadcastManager manager;
	private Context context;
	private InputStream in;
	private OutputStream out;
	
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
	public ThreadedRemoteConnection(Context context, InputStream in,
													OutputStream out){
		this.manager = LocalBroadcastManager.getInstance(context);
		this.in = in;
		this.out = out;
		this.context = context;
	}
	
	
	/**
	 * Query for this connection's {@link InputStream}
	 * @return the input stream given at creation.
	 */
	protected InputStream in(){
		return in;
	}
	
	
	/**
	 * Query for this connection's {@link OutputStream}
	 * @return the output stream given at creation
	 */
	protected OutputStream out(){
		return out;
	}
	
	
	/**
	 * Query for this connection's {@link LocalBroadcastManager}
	 * @return the local broadcast manager retrieved from
	 * the {@link Context} given at creation
	 */
	protected LocalBroadcastManager getManager(){
		return manager;
	}
	
	
	/**
	 * Query for this connection's {@link Context}
	 * @return the context in which this connection
	 * was created
	 */
	protected Context getContext(){
		return context;
	}
	
	
	/**
	 * Cancels any looping processes defined in
	 * this connection's {@link #run()} method
	 */
	public abstract void cancel();

}
