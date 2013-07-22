package com.i2r.ARC.PCControl.link.lineLink;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.i2r.ARC.PCControl.link.RemoteConnection;

//extends the RemoteConnection for a dummy connection on the command line
public class CommandLineConnection extends RemoteConnection<byte[]> {
	
	public CommandLineConnection(){
		dataIn = new DataInputStream(System.in);
		dataOut = new DataOutputStream(System.out);
		
	}
}
