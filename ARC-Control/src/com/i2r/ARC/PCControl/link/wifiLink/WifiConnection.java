/**
 * 
 */
package com.i2r.ARC.PCControl.link.wifiLink;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.link.RemoteConnection;

/**
 * Creates a wifi connection to some URL.  Because all of the actual connecting will happen via the OS and wifi Direct (which, if I understand
 * correctly, I can treat like a wireless AP), the only actual work to do is to connect directly to the agreed upon IP address.
 * 
 * @author Johnathan Pagnutti
 */
public class WifiConnection extends RemoteConnection<byte[]> {
	Socket TCPSocket;
	
	static final Logger logger = Logger.getLogger(WifiConnection.class);
	
	/**
	 * Ok, smalls, check it.  The connString in this case is the string Inet address of the phone.  How am I gonna get that?
	 * By being champion.  The gateway of the network connection should be the phone's IP, so the {@link WifiLink} class can pass that to me.
	 * Boom.
	 * 
	 * @param connString 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public WifiConnection(String connectionURL) throws UnknownHostException, IOException {
		TCPSocket = new Socket(connectionURL, 9001);
		
		dataIn = new DataInputStream(TCPSocket.getInputStream());
		dataOut = new DataOutputStream(TCPSocket.getOutputStream());	
	}

	public WifiConnection(Socket socket) throws IOException {
		logger.debug("Connecting through passed socket.");
		TCPSocket = socket;
		
		dataIn = new DataInputStream( new BufferedInputStream(TCPSocket.getInputStream()));
		dataOut = new DataOutputStream( new BufferedOutputStream(TCPSocket.getOutputStream()));
	}

	@Override
	public void close() {
		try {
			dataOut.flush();
			dataOut.close();
			dataIn.close();
			TCPSocket.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
