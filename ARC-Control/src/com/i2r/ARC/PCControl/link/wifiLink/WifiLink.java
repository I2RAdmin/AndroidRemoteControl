/**
 * 
 */
package com.i2r.ARC.PCControl.link.wifiLink;

import java.util.List;

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
 * @deprecated
 */
public class WifiLink implements RemoteLink<byte[]> {

	static final Logger logger = Logger.getLogger(WifiLink.class);
	
	
	public WifiLink(){}
	
	/**
	 * Unused.
	 */
	@Override
	public void searchForConnections() {}

	@Override
	public List<String> currentConnections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RemoteConnection<byte[]> connect(String connectionURL) {
		return new WifiConnection(connectionURL);
	}

	
}
