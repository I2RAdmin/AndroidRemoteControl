package com.i2r.ARC.PCControl.link.SMSLink;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.Service;
import org.smslib.smpp.Address;
import org.smslib.smpp.BindAttributes;
import org.smslib.smpp.BindAttributes.BindType;
import org.smslib.smpp.jsmpp.JSMPPGateway;

import com.i2r.ARC.PCControl.link.RemoteConnection;
import com.i2r.ARC.PCControl.link.RemoteLink;

public class SMSLink implements RemoteLink<byte[]>{
	//you better believe its a logger
	static final Logger logger = Logger.getLogger(SMSLink.class);
		
	Service smsService;
	
	public SMSLink(){
		smsService = Service.getInstance();
	}
	
	@Override
	public void searchForConnections() {
		logger.debug("Going to send messages through SMPP");
		
		//gonna try to use the SMPP protocol for text messages
		JSMPPGateway gateway = new JSMPPGateway("smppcon", "smpp.activexperts-labs.com", 2775, 
				new BindAttributes("InternetGW", "password", "", BindType.TRANSCEIVER, 
						new Address(Address.TypeOfNumber.INTERNATIONAL, Address.NumberingPlanIndicator.ISDN)));
		
		
		
		try {
			smsService.addGateway(gateway);
		} catch (GatewayException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public List<String> currentConnections() {
		List<String> returnList = new ArrayList<String>();
		
		for(AGateway gateway : smsService.getGateways()){
			returnList.add(gateway.getGatewayId());
		}
		
		if(returnList.isEmpty()){
			return null;
		}else{
			return returnList;
		}
	}

	@Override
	public RemoteConnection<byte[]> connect(String gatewayID) {
		
		return new SMSConnection(gatewayID);
	}

}
