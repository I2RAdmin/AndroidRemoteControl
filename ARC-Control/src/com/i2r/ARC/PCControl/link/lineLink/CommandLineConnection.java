package com.i2r.ARC.PCControl.link.lineLink;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.i2r.ARC.PCControl.link.RemoteConnection;

//extends the RemoteConnection for a dummy connection on the command line
public class CommandLineConnection extends RemoteConnection<byte[]> {
	
	public CommandLineConnection(){
		dataIn = new DataInputStream(System.in);
		dataOut = new DataOutputStream(System.out);
		
	}

	@Override
	public void close() {
		//just closes the streams, as we're pulling data from system in and out
		try {
			dataIn.close();
			dataOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
