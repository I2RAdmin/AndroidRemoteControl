package com.i2r.androidremotecontroller.main;

import com.i2r.androidremotecontroller.connections.ConnectionManager;
import com.i2r.androidremotecontroller.sensors.SensorDurationHandler;

/**
 * Used to periodically test the connection for
 * validity, since there are some cases in
 * which the connection does not close properly.
 */
public class Pinger extends Thread {
	
	public static final int DEFAULT_PING_FREQUENCY = 30000;
	
	private SensorDurationHandler handler;
	private ConnectionManager<?> manager;
	private boolean started;
	
	public Pinger(int frequency, ConnectionManager<?> manager){
		this.handler = new SensorDurationHandler(frequency);
		this.manager = manager;
		this.started = true;
		handler.start();
	}
	
	
	@Override
	public void run(){
		while(started){
			if(handler.maxReached()){
				
				// see if the connection is valid after
				// time interval has been reached
				boolean connected = ResponsePacket
						.sendPing(manager.getConnection());
				
				// if connection isn't valid anymore,
				// try to reconnect
				if(!connected){
					manager.findConnection();
					
				} 
				
				handler.start();
				
			} else {
				try{
					Thread.sleep(1000);
				} catch (InterruptedException e){}
			}
		}
	}
	
	
	public void reset(){
		handler.start();
	}
	
	
	public void cancel(){
		this.started = false;
	}
	
} // end of Pinger class
