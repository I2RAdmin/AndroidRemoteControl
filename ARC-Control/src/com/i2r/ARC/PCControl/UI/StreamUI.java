/**
 * 
 */
package com.i2r.ARC.PCControl.UI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.ARCCommand;
import com.i2r.ARC.PCControl.Controller;
import com.i2r.ARC.PCControl.RemoteClient;
import com.i2r.ARC.PCControl.UnsupportedValueException;

/**
 * More Generic GenericUI setup.  Reads in things from a {@link InputStream} source, writes out things to a {@link OutputStream} destination.  The
 * concept is to combine this with some sort of operating system piping, to allow for GenericUI to come from anywhere that isn't a GUI.
 * 
 * Fuck GUIs.
 * @author Johnathan
 * @param <U> A subtype of {@link OutputStream} that will be writing data out to the user. 
 * @param <T> A subtype of {@link InputStream} that will read data in from the user.
 * @param <V> The type of data that will be written out to the {@link StreamUI#dest} stream.  This allows for us to pass objects (and utalize
 * their <code>toString()</code> methods).
 */
public class StreamUI<U extends OutputStream, T extends InputStream, V> {

	private InputStream source;
	private OutputStream dest;
	public AtomicBoolean inClosed;
	
	Controller cntrl;
	
	private Thread readThread;
	
	static final Logger logger = Logger.getLogger(StreamUI.class);
	
	public static final String END_READING_FLAG = "stop";
	
	public StreamUI(T source, U dest, Controller creator){
		inClosed = new AtomicBoolean(false);
		
		this.source = source;
		this.dest = dest;
		
		cntrl = creator;
		
		readThread = new Thread(new StreamUIReadRunnable(source));
	}
	
	
	public void close() {
		try {
			source.close();
			dest.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	/**
	 * Read a line of data off the input stream, through the use of an input parser of some sort, to prevent bad lines.
	 * 
	 */
	public void read(){
		readThread.start();
	}
	
	/**
	 * Write to the output stream that a thing happened.
	 * 
	 */
	public void write(V dataElement){
		try {
			dest.write((dataElement.toString() + "\n").getBytes());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	
	/**************************
	 * Private Inner Class
	 *************************/
	private class StreamUIReadRunnable implements Runnable{
		
		private Scanner readScan;
		
		private StreamUIReadRunnable(T inStream){
			readScan = new Scanner(inStream);
			readScan.useDelimiter(" ");
		}
		
		@Override
		public void run() {
			//implementation of the read method
			while(true){
				while(!readScan.hasNextLine());
				String line = readScan.nextLine();
				logger.debug("Read in: " + line);

				if(line.equals(END_READING_FLAG)){
					inClosed.set(true);
					break;
				}

				int remoteDeviceIndex = -2;
				try{
					
					remoteDeviceIndex = Integer.valueOf(line.substring(0, line.indexOf(' ')));
				
				}catch(NumberFormatException e){
					try {
						dest.write("Malformed Command, could not get a Remote Device or local reference.".getBytes());
						logger.error(e.getMessage(), e);
						continue;
					} catch (IOException e1) {
						logger.error(e1.getMessage(), e1);
						continue;
					}
				}
				
				try {
					if(remoteDeviceIndex > -1){
						RemoteClient dev = cntrl.getDevice(remoteDeviceIndex);
						cntrl.send(dev, ARCCommand.fromString(dev, line.substring(line.indexOf(' '))));
					}else if (remoteDeviceIndex == -1){
						logger.debug("Command: ");
						logger.debug(line.substring(line.indexOf(' ')));
						cntrl.performLocal(ARCCommand.fromString(line.substring(line.indexOf(' '))));
					}else{
						throw new UnsupportedValueException("Invalid Remote Device Specified (given: " + remoteDeviceIndex + ").");
					}
				} catch (UnsupportedValueException e) {
					logger.error(e.getMessage(), e);
					String uiMessage = "Invalid Command Arguments.\n";
					
					try {
						dest.write(uiMessage.getBytes());
						dest.write(e.getMessage().getBytes());
					} catch (IOException e1) {
						logger.error(e1.getMessage(), e1);
						continue;
					}
				}
			}
			
			logger.debug("Stopping UI Read Thread.");
		}
	}
}
