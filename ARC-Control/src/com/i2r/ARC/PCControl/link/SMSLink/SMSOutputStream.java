/**
 * 
 */
package com.i2r.ARC.PCControl.link.SMSLink;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.smslib.GatewayException;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.TimeoutException;

/**
 * @author Johnathan
 *
 */
public class SMSOutputStream extends OutputStream {
	static final Logger logger = Logger.getLogger(SMSOutputStream.class);
	
	String number;
	List<Byte> message;
	Service smsService;
	
	public SMSOutputStream(String number) {
		logger.debug("Sending texts to " + number);
		this.number = number;
		message = new ArrayList<Byte>();
		smsService = Service.getInstance();
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		message.add((byte)b);
		
		String txtMessage = new String(ArrayUtils.toPrimitive(message.toArray(new Byte[message.size()])));
		if(txtMessage.contains("PACKET_COMPLETE")){
			logger.debug("Found packet delimiter, sending text message...");
			send(txtMessage);
			message.clear();
		}
	}
	
	public void send(String txtMessage){
		logger.debug("Sending: " + txtMessage);
		OutboundMessage msg = new OutboundMessage(number, txtMessage);
		try {
			smsService.sendMessage(msg);
		} catch (TimeoutException | GatewayException | IOException
				| InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
