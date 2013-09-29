package com.i2r.androidremotecontroller.main.databouncer;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;


/**
 * This class models a protocol for bouncing data between
 * android devices so that the data being bounced may get
 * to the end target by means of an array of these devices.
 * Bouncing is determined by user setup before launching
 * this application's remote control mode.
 * @author Josh Noel
 */
public class DataBouncer {

	private static final String TAG = "DataBouncer";
	private static DataBouncer instance = new DataBouncer();
	
	private List<DataBouncerConnector> bouncers;
	private boolean capture_point;
	
	/**
	 * Constructor<br>
	 * creates a container for incoming data as well as
	 * a container for outgoing data, so that it may
	 * be properly passed along.
	 */
	private DataBouncer(){
		this.bouncers = new LinkedList<DataBouncerConnector>();
		this.capture_point = false;
	}
	
	
	/**
	 * Query for this {@link DataBouncer}s singleton instance.
	 * This approach is used to bypass carrying an instance
	 * of this class down the chain to the sensor objects
	 * that will actually be accumulating data.
	 * @return the singleton instance of this class.
	 */
	public static DataBouncer getInstance(){
		return instance;
	}
	
	
	/**
	 * Query asking if this bouncer already has the
	 * given connector.
	 * @param connector - the connector to search for in
	 * this collection of {@link DataBouncerConnector}s.
	 * @return true if the given connector exists in this
	 * collection, false otherwise.
	 */
	public boolean contains(DataBouncerConnector connector){
		return bouncers.contains(connector);
	}
	
	
	/**
	 * Query for this device's status
	 * @return true if the user has specified
	 * this device to be a capture point in
	 * the device array, false otherwise
	 */
	public boolean isCapturePoint(){
		return capture_point;
	}
	
	
	/**
	 * Sets the status of this device as a
	 * capture point in the android device array.
	 * @param flag - the flag to set this device's
	 * capture point status to.
	 */
	public void setCapturePoint(boolean flag){
		Log.d(TAG, "capture point set to " + flag);
		this.capture_point = flag;
	}
	
	
	/**
	 * Bounces the given data to all outgoing
	 * {@link DataBouncerConnection}s. The incoming connections,
	 * in turn, use this when new data becomes available so
	 * that the data may be passed down the array of android devices.
	 * @param data - the data to be bounced to the next android device.
	 */
	public synchronized void bounce(byte[] data){
		if(data != null){
			for(DataBouncerConnector c : bouncers){
				if(!c.isOriginOfData(data)){
					c.getConnection().write(data);
				}
			}
		} else {
			Log.e(TAG, "byte array is null, data bounce cancelled");
		}
	}

	
	
	/**
	 * <p>Adds the given {@link DataBouncerConnector} to this bouncer's
	 * pool of incoming connectors.</p>
	 * 
	 * <p>WARNING: this method attempts to start the given connector's
	 * connection's read thread before adding it to the pool. Do not start
	 * the given connector's connection's read thread before adding it
	 * to this pool</p>
	 * 
	 * @param connector - the connector to be added to this bouncer's pool
	 * of incoming connections.
	 * @see {@link DataBouncerConnector#getConnection()}
	 */
	public synchronized void add(DataBouncerConnector connector){
		if(connector != null && connector.hasConnection()){
			connector.getConnection().start();
			bouncers.add(connector);
		} else {
			Log.e(TAG, "connector has no valid conneciton, add cancelled");
		}
	}
	
	
	/**
	 * Removes the given connector from this bouncer's pool of incoming
	 * {@link DataBouncerConnector}s. This method does nothing if the
	 * given connector is not in its incoming pool.
	 * @param connector - the connector to remove from this bouncer's pool.
	 */
	public synchronized void remove(DataBouncerConnector connector){
		try{
			bouncers.remove(connector);
		} catch(Exception e){
			Log.e(TAG, "failed to remove incoming connector : " + e.getMessage());
		}
	}
	
	
	/**
	 * Query for this {@link DataBouncer}s current list of
	 * incoming {@link DataBouncerConnector}s.
	 * @return a deep copy of this DataBouncer's incoming connectors.
	 */
	public synchronized List<DataBouncerConnector> getConnectors(){
		return bouncers;
	}
	
	
	/**
	 * Clears all incoming and outgoing connections
	 * for this data bouncer. This bouncer will not
	 * pass data along until more connections are added
	 * after this is called.
	 */
	public synchronized void clearAll(){
		
		for(DataBouncerConnector c : bouncers){
			if(c.getConnection().isConnected()){
				c.getConnection().disconnect();
			}
		}
		
		this.bouncers.clear();
	}
	
} // end of DataBouncer class
