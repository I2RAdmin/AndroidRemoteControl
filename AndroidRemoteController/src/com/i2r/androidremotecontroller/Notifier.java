package com.i2r.androidremotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * <b>Summary</b><br><br>
 * This class models a basic notifier that can be queried
 * about the status of received broadcasts from the android
 * system. This class will be primarily used to block until
 * the android system responds, and should block in a separate
 * thread until the main thread has returned all expected callbacks.
 * @author Josh Noel
 */
public class Notifier {

	private static final String[] WIFI_DIRECT_ACTIONS = {
		WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION,
		WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION,
		WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION,
		WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION, 
		WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
	};
	
	
	private static final String[] BLUETOOTH_ACTIONS = {
		BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED,
		BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
		BluetoothAdapter.ACTION_DISCOVERY_STARTED,
		BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED,
		BluetoothAdapter.ACTION_SCAN_MODE_CHANGED,
		BluetoothAdapter.ACTION_STATE_CHANGED
	};
	
	
	private static final String[] USB_ACTIONS = {
		UsbManager.ACTION_USB_ACCESSORY_ATTACHED,
		UsbManager.ACTION_USB_ACCESSORY_DETACHED,
		UsbManager.ACTION_USB_DEVICE_ATTACHED,
		UsbManager.ACTION_USB_DEVICE_DETACHED
	};
	
	
	private static final int NO_SUCH_ACTION = -1;
	private static final int DEFAULT_INT = NO_SUCH_ACTION;
	
	
	private IntentFilter filter;
	private BroadcastReceiver receiver;
	private Context context;
	private String[] filters;
	private Intent[] results;
	private boolean registered;
	
	
	/**
	 * Default Constructor
	 * @param context - the context to register all given filters with
	 * @param filters - the filters to register in a BroadcastReceiver
	 */
	public Notifier(Context context, String[] filters){
		
		this.context = context;
		this.filters = filters;
		this.registered = false;
		this.results = new Intent[filters.length];
		this.resetResults();
		
		this.filter = new IntentFilter();
		for(int i = 0; i < filters.length; i++){
			filter.addAction(filters[i]);
		}
		
		this.receiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent){
				int i = indexOf(intent.getAction());
				if(i != NO_SUCH_ACTION){
					results[i] = intent;
				}
			}
		};
		
		register();
	}
	
	
	/**
	 * Registers the {@link BroadcastReceiver} that this
	 * Notifier uses to update its result intents.
	 */
	public void register(){
		if(!registered){
			registered = true;
			context.registerReceiver(receiver, filter);
		}
	}
	
	
	/**
	 * Unregisters the {@link BroadcastReceiver} that this
	 * Notifier uses to update its result intents.
	 */
	public void unregister(){
		if(registered){
			registered = false;
			context.unregisterReceiver(receiver);
		}
	}
	
	
	/**
	 * Resets all result {@link Intent} objects to null
	 * for each action filter defined in this Notifier.
	 */
	public void resetResults(){
		for(int i = 0; i < results.length; i++){
			results[i] = null;
		}
	}
	
	
	/**
	 * Resets the resulting intent at the given index to null.<br><br>
	 * <b>WARNING:</b><br>
	 * be sure you know what action you are resetting for,
	 * or risk permanently losing corresponding intent objects to
	 * actions at any indexes mistakenly called
	 * @param index - the index corresponding to the action to be reset
	 * @see {@link #indexOf(String)}
	 */
	public void resetResult(int index){
		if(0 <= index && index < results.length){
			results[index] = null;
		}
	}
	
	
	/**
	 * Resets the resulting Intent of the given action to null.
	 * @param action - the action filter to reset for this notifier
	 */
	public void resetResult(String action){
		int i = indexOf(action);
		if(i != NO_SUCH_ACTION){
			results[i] = null;
		}
	}


	/**
	 * @return true if this notifier has registered its
	 * filter with the Context given at creation, false if
	 * it has not registered or unregistered its filter. As
	 * of now, this Notifier definition registers its filter
	 * upon creation, so this query can serve as a check to
	 * make sure the filter gets unregistered to prevent
	 * memory leaks.
	 */
	public boolean isRegistered(){
		return registered;
	}
	
	

	/**
	 * @param index - the index to retrieve a result {@link Intent} from
	 * @return the intent sent with the most recent broadcast corresponding
	 * to the given action, or null if no broadcast has been received yet or
	 * the given index is out of range for the list of filters in this notifier.
	 * @see {@link #indexOf(String)}
	 */
	public Intent getIntent(int index){
		return (0 <= index && index < results.length) ? results[index] : null;
	}
	
	
	
	/**
	 * Should not be used for blocking calls as this uses a linear search,
	 * instead use {@link #indexOf(String)} with {@link #getIntent(int)}.
	 * @param action - the action to get the corresponding {@link Intent} for
	 * @return The Intent sent with the most recent broadcast for the given action,
	 * or null if no broadcast was received for this action or the action is not
	 * defined in this notifier.
	 */
	public Intent getIntent(String action){
		int i = indexOf(action);
		return (i != NO_SUCH_ACTION) ? results[i] : null;
	}
	
	
	
	/**
	 * @param index - the index to check the state of in this notifier.
	 * NOTE: should use {@link #actionOccurred(String)} if indexes of
	 * registered filters is unknown
	 * @return true if the state of the filter at the given index is true,
	 * false if the index is false or is not within the range of this
	 * notifier's filter count (0 to filters.length)
	 * @see {@link #filterCount()}
	 */
	public boolean actionOccurred(int index){
		return (0 <= index && index < results.length) ? results[index] != null : false;
	}
	
	
	/**
	 * Should not be used for blocking calls as this uses a linear search,
	 * instead use {@link #indexOf(String)} with {@link #actionOccurred(int)}.
	 * @param action - the action to check the state of in this notifier
	 * @return true if a broadcast for that action has occurred, false if
	 * the action did not occur or it does not exist in this notifier
	 */
	public boolean actionOccurred(String action){
		int i = indexOf(action);
		return (i == NO_SUCH_ACTION) ? false : results[i] != null;
	}
	
	
	/**
	 * @return The number of filters that this notifier is listening with,
	 * or -1 if there are no filters
	 */
	public int filterCount(){
		return (filters == null) ? DEFAULT_INT : filters.length;
	}
	
	
	/**
	 * Returns the index of the given action in this Notifier's
	 * collection of filters, or -1 if
	 * @param action - the action to search through this list
	 * of filters for an index
	 * @return the index of the given action in this Notifier's
	 * collection of filters, or -1 if it does not exist in this
	 * notifier's collection of fliters
	 */
	public int indexOf(String action){
		int i = 0;
		while(i < filters.length && !filters[i].equals(action)){i++;}
		return (i < filters.length) ? i : NO_SUCH_ACTION;
	}
	
	
	
	/**
	 * Static Wifi android system notifier
	 * @param context - the context to register a filter for
	 * broadcasts with
	 * @return a notifier that is registered  to all
	 * {@link WifiP2pManager} ACTION broadcasts which are defined as its
	 * string constants.
	 */
	public static Notifier getWifiDirectNotifier(Context context){
		return new Notifier(context, WIFI_DIRECT_ACTIONS);
	}
	
	
	/**
	 * Static Bluetooth android system notifier
	 * @param context - the context to register a filter for
	 * broadcasts with
	 * @return a notifier that is registered  to all
	 * {@link BluetoothAdapter} ACTION broadcasts which are defined as its
	 * string constants.
	 */
	public static Notifier getBluetoothNotifier(Context context){
		return new Notifier(context, BLUETOOTH_ACTIONS);
	}
	
	
	/**
	 * Static Usb android system notifier
	 * @param context - the context to register a filter for
	 * broadcasts with
	 * @return a notifier that is registered  to all
	 * {@link UsbManager} ACTION broadcasts which are defined as its
	 * string constants.
	 */
	public static Notifier getUSBNotifier(Context context){
		return new Notifier(context, USB_ACTIONS);
	}
	
	
	/**
	 * This class models a generic stall thread that waits
	 * for a notifier to indicate the android system has issued
	 * a particular broadcast. This thread's sole purpose is to
	 * block until the android system has caught up with the
	 * application running this thread. This is merely to
	 * compensate for the callback structure of the android OS.
	 * The Runnable given in the constructor of this object is NOT
	 * the one that will be run on the calling of start(), instead
	 * the given runnable should continue execution in the main thread
	 * via method call. The calling of start for this object begins a
	 * block, which does not return until the system responds or {@link #cancel()}
	 * is called. Note that if cancel is called on this thread, the given Runnable will
	 * not be executed.
	 * @author Josh Noel
	 */
	public class Staller extends Thread {
		
		private Notifier notifier;
		private String listenFor;
		private Runnable runner;
		private boolean running, started;
		
		/**
		 * Default Constructor
		 * @param notifier - the notifier to query for broadcast changes in the android system
		 * @param listenFor - the string to query the notifier for an update with - this should
		 * be predefined as a constant and given to the notifier upon its creation. If this string
		 * does not exist in the notifier, the given runnable is executed automatically without any
		 * blocking, which will render this object useless
		 * @param runner - the Runnable to execute after blocking - should continue the flow of the
		 * main thread
		 */
		public Staller(Notifier notifier, String listenFor, Runnable runner){
			this.notifier = notifier;
			this.listenFor = listenFor;
			this.runner = runner;
			this.running = true;
			this.started = false;
		}
		
		public void run(){
			
			int index = notifier.indexOf(listenFor);
			started = true;
			
			if(index != NO_SUCH_ACTION){
				
				// wait for action occurrence
				while(running && !notifier.actionOccurred(index)){
					try{
						Thread.sleep(1000);
					} catch(InterruptedException e){
						e.printStackTrace();
					}
				} 
			}
			
			if(running){
				runner.run();
			}
			
		}
		
		/**
		 * Cancels this thread's execution. If this thread was not yet
		 * started, this method does nothing. The Runnable given at creation
		 * will NOT be executed if this is called.
		 */
		public void cancel(){
			if(started){
				running = false;
			}
		}
	}
}
