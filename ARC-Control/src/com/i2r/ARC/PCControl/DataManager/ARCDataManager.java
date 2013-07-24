/**
 * 
 */
package com.i2r.ARC.PCControl.DataManager;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.Task;
import com.i2r.ARC.PCControl.link.RemoteConnection;

/**
 * This is the ARC implementation of a {@link DataManager}
 * Generic to allow for any {@link RemoteConnection} of type <code>byte[]</code> to be used in the constructor.
 * @see {@link DataManager} for general contract details and notes regarding data hiding
 * 
 * @author Johnathan Pagnutti
 *
 */
public class ARCDataManager extends DataManager<Task, byte[]>{

	/**
	 * The character that delimits the elements in a packet
	 */
	public static char SEND_PACKET_DELIMITER = '\n';
	
	static final Logger logger = Logger.getLogger(ARCDataManager.class);
	
	/**
	 * Constructor.
	 * A new {@link ARCDataParser} object is also created and assigned to the {@link DataManager#parser} field
	 * 
	 * @param connection the Bluetooth connection to use to get the I/O streams for the read and write methods
	 * 
	 * @see {@link DataManager} for contract details
	 */
	public ARCDataManager(RemoteConnection<byte[]> conn){
		super(conn);
		
		parser = new ARCDataParser();
	}

	/**
	 * The implementation of the read() method
	 * @see {@link DataManager#read()} for contract details
	 * 
	 * This implementation of the read method does not block by having a thread handle the actual read loop
	 */
	@Override
	public void read() {
		//create a new thread and start it
		Thread t = new Thread(new ARCDataManagerRunnable(dataIn, parser));
		t.start();
	}

	/**
	 * The implementation of the write() method
	 * @see {@link DataManager#write(Object)} for contract details
	 * 
	 * @param dataElement the data element to write to the connection
	 */
	@Override
	public void write(Task dataElement) {
		byte[] dataByes = toSendBytes(dataElement);
		
		try {
			//log loop
			logger.debug("Writing: ");
			StringBuilder sb = new StringBuilder();
			for(byte b : dataByes){
				sb.append(b);
				sb.append(" ");
			}
			logger.debug(sb.toString());
			
			//write data out to the remote connection
			dataOut.write(dataByes);
		} catch (IOException e) {
			//something bad as happened, socket could have been closed (EOF Exception)
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	private byte[] toSendBytes(Task dataElement) {
		StringBuilder sb = new StringBuilder();
		sb.append(dataElement.getId());
		sb.append(SEND_PACKET_DELIMITER);
		sb.append(dataElement.getCommand().getHeader());
		sb.append(SEND_PACKET_DELIMITER);
		for(String arg : dataElement.getCommand().getArguments()){
			//for testing purposes, don't send the last argument
			if(arg.equals("jpeg")){
				continue;
			}
			sb.append(arg);
			sb.append(SEND_PACKET_DELIMITER);
		}
		
		return sb.toString().getBytes();
	}
	
	/*************************
	 * INNER CLASS
	 *************************/

	/**
	 * Implements the thread that the BluetoothDataManager uses to read from the socket.
	 * This thread blocks so that the main program does not halt when we want to read from the socket.
	 *  
	 * @author Johnathan Pagnutti
	 *
	 */
	private class ARCDataManagerRunnable implements Runnable{
		
		private InputStream threadIn;
		private DataParser<byte[]> threadParser;
		
		/**
		 * Creates a new BluetoothDataManagerThread Runnable, which is passed to a Thread object as an argument to use
		 * this object's {@link this#run()} method when {@link Thread#start()} is called
		 * 
		 * This is the thread that handles the read loop, so it uses an input stream to read from and a parser to interpet the data read in
		 * @param dataIn the data input stream to read from
		 * @param parser the parser to use interpet the data read in
		 */
		public ARCDataManagerRunnable(InputStream dataIn, DataParser<byte[]> parser){
			this.threadIn = dataIn;
			this.threadParser = parser;
		}
		
		/**
		 * Implemented from {@link Thread}
		 * 
		 * Starts the read loop, which reads data from the {@link RemoteBluetoothConnection} until the connection is closed.  The idea is that a
		 * {@link DataParser} object that was provided to this thread will parse the data read in.
		 * 
		 * @see {@link DataManager#read()} for generic contract information on the calling method to this thread
		 * @see {@link ARCDataManager} for details about the class that uses this thread
		 */
		@Override
		public void run(){
			try {
				//buffer to read data into
				byte[] readBuffer = new byte[1024];
				
				logger.debug("Waiting to read bytes...");
				//this is the read data loop.  Read until we hit a socket exception (the connection is dead)
				while(true){
					
					//read data into the buffer
					int bytesRead = threadIn.read(readBuffer);
						
					//if we have read at least one byte...
					if(bytesRead > 0){
						//trim the array down to the number of bytes read
						byte[] cleanArray = new byte[bytesRead];
						System.arraycopy(readBuffer, 0, cleanArray, 0, cleanArray.length);
					
						logger.debug("Read " + cleanArray.length + " bytes from the connection.");
					
						//pass the trimmed to the parser to parse it
						threadParser.parse(cleanArray);
					}else if(bytesRead == -1){
						threadIn.close();
						break;
					}
				}
			} catch (IOException e) {
				//some error has occured.  Hopefully, its an EOF exception (socket closed)
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
			
		}
	}

}
