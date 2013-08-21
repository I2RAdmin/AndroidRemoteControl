/**
 * 
 */
package com.i2r.ARC.PCControl.link.SMSLink;

import java.io.IOException;
import java.io.InputStream;

import com.i2r.ARC.PCControl.data.MultithreadStringBuffer;

/**
 * @author Johnathan
 *
 */
public class SMSInputStream extends InputStream {

	MultithreadStringBuffer messageBuffer;
	
	public SMSInputStream(){
		messageBuffer = new MultithreadStringBuffer();
	}
	
	
	public void insert(String message){
		messageBuffer.write(message);
	}
	
	/**
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		try{
			
			while(!messageBuffer.hasNext()){}
			
			return messageBuffer.read();
		}catch(IndexOutOfBoundsException e){
			return -1;
		}
	}
}
