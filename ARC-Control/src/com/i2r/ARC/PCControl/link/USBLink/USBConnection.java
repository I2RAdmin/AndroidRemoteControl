package com.i2r.ARC.PCControl.link.USBLink;

import java.io.IOException;

import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.link.RemoteConnection;

public class USBConnection extends RemoteConnection<byte[]> {
	//you better believe its a logger
	static final Logger logger = Logger.getLogger(USBConnection.class);
		
	CommPort commPort;
	SerialPort serialPort;
	public USBConnection(CommPortIdentifier commPortIdentifier) throws PortInUseException, UnsupportedCommOperationException, IOException{
		logger.debug("Attempting to open stream from " + commPortIdentifier.getName());
		commPort = commPortIdentifier.open(this.getClass().getName(), 2000);
		serialPort = (SerialPort) commPort;
		
		logger.debug("Setting Magic Serial Port Parameters");
		serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		
		logger.debug("Getting input stream");
		dataIn = serialPort.getInputStream();
		
		logger.debug("Getting output stream");
		dataOut = serialPort.getOutputStream();
	}
	
	@Override
	public void close() {
		serialPort.close();
		commPort.close();
	}
}
