/**
 * 
 */
package com.i2r.ARC.PCControl.link.wifiLink;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;

/**
 * Create a new Wifi Link that will handle data of type <code> byte[] </code>
 * 
 * Wifi is much slicker than Bluetooth, as we can pretty much create a connection outright from an IP address (that should be a constant).
 * 
 * The other option is to attempt to broadcast a request for IPs, and then see what replies.  But who does that.
 * @see {@link RemoteLink}
 * @author Johnathan Pagnutti
 */
public class WifiLink implements RemoteLink<byte[]> {

	static final Logger logger = Logger.getLogger(WifiLink.class);
	
	ServerSocket initalConn;
	Map<String, Socket> dataConnections;
	
	public AtomicBoolean completedSearching;
	
	public WifiLink(){
		completedSearching = new AtomicBoolean(false);
		dataConnections = new HashMap<String, Socket>();
		
		try {
			initalConn = new ServerSocket(9001);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void searchForConnections() {
		Thread t = new Thread(new SearchForWifiConnectionsRunnable());
		t.start();
	}

	@Override
	public List<String> currentConnections() {
		if(completedSearching.compareAndSet(true, true)){
			if(dataConnections.isEmpty()){
				return null;
			}else{
				List<String> returnList = new ArrayList<String>(dataConnections.keySet().size());
				returnList.addAll(dataConnections.keySet());
				return returnList;
			}
		}else{
			return RemoteLink.STILL_SEARCHING;
		}
	}

	@Override
	public RemoteConnection<byte[]> connect(String connectionURL) {
		try {
			if(dataConnections.keySet().contains(connectionURL)){
				return new WifiConnection(dataConnections.get(connectionURL));
			}else{
				return null;
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	
	/***************
	 * INNER CLASS
	 ***************/
	private class SearchForWifiConnectionsRunnable implements Runnable{

		public SearchForWifiConnectionsRunnable() {}

		@Override
		public void run() {
			try {
				Socket tmp = initalConn.accept();
				dataConnections.put(tmp.getInetAddress().toString(), tmp);
				completedSearching.compareAndSet(false, true);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}	
		}
	}
}
