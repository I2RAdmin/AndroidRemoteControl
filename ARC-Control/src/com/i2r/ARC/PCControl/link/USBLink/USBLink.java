package com.i2r.ARC.PCControl.link.USBLink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;

public class USBLink implements RemoteLink<byte[]>{
	    
	//you better believe its a logger
	static final Logger logger = Logger.getLogger(USBLink.class);
    
    Map<String, CommPortIdentifier> commPorts;
    AtomicBoolean completedSearching;
    
    public USBLink(){
    	commPorts = new HashMap<String, CommPortIdentifier>();
    	completedSearching = new AtomicBoolean(false);
    }
    
	@Override
	public void searchForConnections() {
		logger.debug("Searching for serial ports...");
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		
		while(portEnum.hasMoreElements()){
			CommPortIdentifier thisPort = portEnum.nextElement();
			logger.debug("Found: " + thisPort.getName() + ", " + thisPort.getPortType());
			
			if(thisPort.getPortType() == CommPortIdentifier.PORT_SERIAL){
				commPorts.put(thisPort.getName(), thisPort);
			}
		}
		
		completedSearching.compareAndSet(false, true);
	}

	@Override
	public List<String> currentConnections() {
		//check to make sure the initialization has completed
		if(completedSearching.compareAndSet(true, true)){
			//check to see if we have any connections in the services found list
			if(!commPorts.isEmpty()){
				//the list isn't empty, return the urls
				logger.debug("Returning service usb port names.");
				List<String> returnList = new ArrayList<String>();
				returnList.addAll(commPorts.keySet());
				return returnList;
			}else{
				//the list is empty, return null for no urls found
				logger.debug("No services have been found to get a usb port name.");
				return null;
			}
		}else{
			//still looking for devices, return the still searching constant
			return RemoteLink.STILL_SEARCHING;
		}
	}

	@Override
	public RemoteConnection<byte[]> connect(String connectionURL) {
		try {
			return new USBConnection(commPorts.get(connectionURL));
		} catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

}
