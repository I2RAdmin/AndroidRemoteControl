package com.i2r.androidremotecontroller.main.databouncer;

import java.util.ArrayList;
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
	
	private List<DataBouncerConnector> incoming, outgoing;
	
	/**
	 * Constructor<br>
	 * creates a container for incoming data as well as
	 * a container for outgoing data, so that it may
	 * be properly passed along.
	 */
	private DataBouncer(){
		this.incoming = new LinkedList<DataBouncerConnector>();
		this.outgoing = new LinkedList<DataBouncerConnector>();
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
	 * Bounces the given data to all outgoing
	 * {@link DataBouncerConnection}s. The incoming connections,
	 * in turn, use this when new data becomes available so
	 * that the data may be passed down the array of android devices.
	 * @param data - the data to be bounced to the next android device.
	 */
	public synchronized void bounce(byte[] data){
		if(data != null){
			for(DataBouncerConnector c : outgoing){
				c.getConnection().write(data);
			}
		} else {
			Log.e(TAG, "byte array is null, data bounce cancelled");
		}
	}
	
	
	/**
	 * <p>Adds the given {@link DataBouncerConnector} to this bouncer's
	 * pool of incoming connectors. The given connector will not be
	 * added if the outgoing pool contains the same connector.</p>
	 * 
	 * <p>Here, "same" implies that the connector has a mac-address
	 * that matches another connector in the opposite pool. This cannot
	 * be allowed to happen - if it did, then when the first device
	 * bounces data to this device, this device will simply bounce it back
	 * to the first. This will cause an infinite loop and severely drain
	 * both devices' batteries, as well as clogging other data streaming.</p>
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
	public synchronized void addIncoming(DataBouncerConnector connector){
		if(connector != null && connector.hasConnection()){
			if(!outgoing.contains(connector)){
				connector.getConnection().start();
				incoming.add(connector);
			} else {
				Log.e(TAG, "outgoing bouncer list contains given connector, add cancelled");
			}
		} else {
			Log.e(TAG, "connector has no valid conneciton, add cancelled");
		}
	}
	
	
	/**
	 * <p>Adds the given {@link DataBouncerConnector} to this bouncer's
	 * pool of outgoing connectors. The given connector will not be
	 * added if the outgoing pool contains the same connector.</p>
	 * 
	 * <p>Here, "same" implies that the connector has a mac-address
	 * that matches another connector in the opposite pool. This cannot
	 * be allowed to happen - if it did, then when the first device
	 * bounces data to this device, this device will simply bounce it back
	 * to the first. This will cause an infinite loop and severely drain
	 * both devices' batteries, as well as clogging other data streaming.</p>
	 * 
	 * <p>WARNING: this method attempts to start the given connector's
	 * connection's read thread before adding it to the pool. Do not start
	 * the given connector's connection's read thread before adding it
	 * to this pool</p>
	 * 
	 * @param connector - the connector to be added to this bouncer's pool
	 * of outgoing connections.
	 */
	public synchronized void addOutgoing(DataBouncerConnector connector){
		if(connector != null && connector.hasConnection()){
			if(!incoming.contains(connector)){
				connector.getConnection().start();
				outgoing.add(connector);
			} else {
				Log.e(TAG, "incoming bouncer list contains given connector, add cancelled");
			}
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
	public synchronized void removeIncoming(DataBouncerConnector connector){
		try{
			incoming.remove(connector);
		} catch(Exception e){
			Log.e(TAG, "failed to remove incoming connector : " + e.getMessage());
		}
	}
	
	
	/**
	 * Removes the given connector from this bouncer's pool of outgoing
	 * {@link DataBouncerConnector}s. This method does nothing if the
	 * given connector is not in its outgoing pool.
	 * @param connector - the connector to remove from this bouncer's pool.
	 */
	public synchronized void removeOutgoing(DataBouncerConnector connector){
		try{
			outgoing.remove(connector);
		} catch(Exception e){
			Log.e(TAG, "failed to remove outgoing connector : " + e.getMessage());
		}
	}
	
	
	/**
	 * Query for this {@link DataBouncer}s current list of
	 * incoming {@link DataBouncerConnector}s.
	 * @return a deep copy of this DataBouncer's incoming connectors.
	 */
	public synchronized List<DataBouncerConnector> getIncomingConnectors(){
		ArrayList<DataBouncerConnector> temp =
				new ArrayList<DataBouncerConnector>(incoming.size());
		temp.addAll(incoming);
		return temp;
	}
	
	
	/**
	 * Query for this {@link DataBouncer}s current list of
	 * outgoing {@link DataBouncerConnector}s.
	 * @return a deep copy of this DataBouncer's outgoing connectors.
	 */
	public synchronized List<DataBouncerConnector> getOutgoingConnectors(){
		ArrayList<DataBouncerConnector> temp =
				new ArrayList<DataBouncerConnector>(outgoing.size());
		temp.addAll(outgoing);
		return temp;
	}
	
	
	
	/**
	 * Clears all incoming and outgoing connections
	 * for this data bouncer. This bouncer will not
	 * pass data along until more connections are added
	 * after this is called.
	 */
	public synchronized void clearAll(){
		
		for(DataBouncerConnector c : incoming){
			if(c.getConnection().isConnected()){
				c.getConnection().disconnect();
			}
		}
		
		this.incoming.clear();
		
		for(DataBouncerConnector c : outgoing){
			if(c.getConnection().isConnected()){
				c.getConnection().disconnect();
			}
		}
		
		this.outgoing.clear();
	}
	
} // end of DataBouncer class
