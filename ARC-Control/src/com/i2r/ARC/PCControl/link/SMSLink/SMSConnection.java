/**
 * 
 */
package com.i2r.ARC.PCControl.link.SMSLink;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.smslib.AGateway;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;
import org.smslib.OutboundMessage;
import org.smslib.SMSLibException;
import org.smslib.Service;

import com.i2r.ARC.PCControl.link.RemoteConnection;

/**
 * @author Johnathan
 *
 */
public class SMSConnection extends RemoteConnection<byte[]> {

	static final Logger logger = Logger.getLogger(SMSConnection.class);
	
	Service service;
	AGateway gateway;
	
	public SMSConnection(String gatewayID) {
		service = Service.getInstance();
		gateway = service.getGateway(gatewayID);
		
		dataIn = new SMSInputStream();
		dataOut = new SMSOutputStream("+9855026193");
		
		service.setInboundMessageNotification(new StreamInboundNotification(dataIn));
		service.setGatewayStatusNotification(new StreamGatewayStatusNotification());
		service.setOutboundMessageNotification(new StreamOutboundNotification());
		
		try {
			service.startService();
		} catch (SMSLibException | IOException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.i2r.ARC.PCControl.link.RemoteConnection#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	
	/******************
	 * INNER CLASSES
	 ******************/
	 
	private class StreamInboundNotification implements IInboundMessageNotification{

		SMSInputStream inboundStream;
		public StreamInboundNotification(InputStream dataIn) {
			inboundStream = (SMSInputStream)dataIn;
		}

		@Override
		public void process(AGateway gateway, MessageTypes msgType,
				InboundMessage msg) {
			
			if(msgType == MessageTypes.INBOUND){
				inboundStream.insert(msg.getText());
			}
			
			logger.debug("Inbound Message Recieved From " + msg.getOriginator());
			logger.debug("through gateway " + gateway.getGatewayId());
			logger.debug("message action: " + msgType.name());
			logger.debug("message: " + msg.getText());
		}
	}
	
	private class StreamGatewayStatusNotification implements IGatewayStatusNotification{

		public StreamGatewayStatusNotification() {}

		@Override
		public void process(AGateway gateway, GatewayStatuses msgType,
				GatewayStatuses msg) {
			logger.debug("Gateway Status Recieved Recieved");
			logger.debug("through gateway " + gateway.getGatewayId());
			logger.debug("message action: " + msgType.name());
			logger.debug("message: " + msg.name());
		}
	}
	
	private class StreamOutboundNotification implements IOutboundMessageNotification{

		public StreamOutboundNotification() {}

		@Override
		public void process(AGateway gateway, OutboundMessage msg) {
			logger.debug("Sending message.");
			logger.debug("through gateway " + gateway.getGatewayId());
			logger.debug("message: " + msg.getText());
		}
	}
}
