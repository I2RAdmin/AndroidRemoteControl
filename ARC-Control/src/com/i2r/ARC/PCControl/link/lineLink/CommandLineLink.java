package com.i2r.ARC.PCControl.link.lineLink;

import java.util.List;

import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;

/**
 * A Command Line "link" so I can test the parser.
 * 
 * @author Johnathan Pagnutti
 *
 */
public class CommandLineLink implements RemoteLink<byte[]>{

	@Override
	public void searchForConnections() {
		//in this implmentation, we don't need to go looking for connections.  This is esentally null.
		
	}

	@Override
	public List<String> currentConnections() {
		//there are no URL's to ever connect to, return null
		return null;
	}

	@Override
	public RemoteConnection<byte[]> connect(String connectionURL) {
		//Return a CommandLine Remote Connection, URL is ignored
		return new CommandLineConnection();
	}
	
	
}
