package com.i2r.androidremotecontroller.main.databouncer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * This class models a data bouncing service for
 * this application, in order to send data across
 * an array of devices in the background of this
 * application.
 * @author Josh Noel
 */
public class DataBouncerService extends Service {

	
	private LocalBinder binder;
	private DataBouncer bouncer;
	
	
	@Override
	public void onCreate(){
		super.onCreate();
		this.binder = new LocalBinder();
		this.bouncer = DataBouncer.getInstance();
		this.bouncer.setContext(this);
	}
	

	@Override
	public boolean stopService(Intent intent){
		boolean result = super.stopService(intent);
		bouncer.clearAll();
		bouncer.setContext(null);
		return result;
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	
	/**
	 * Helper class to give this service's
	 * binder a reference to the service.
	 * The query for this service object is
	 * not currently used, but may be used
	 * in the future.
	 * @author Josh Noel
	 */
	private class LocalBinder extends Binder {
		
		/**
		 * Query for this binder's parent
		 * service object.
		 * @return This binder's parent
		 * {@link DataBouncerService}
		 */
		@SuppressWarnings("unused") // may be used later
		public DataBouncerService getService(){
			return DataBouncerService.this;
		}
	}

}
