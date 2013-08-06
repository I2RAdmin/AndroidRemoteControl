/**
 * 
 */
package com.i2r.ARC.PCControl.DataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.DataResponse;
import com.i2r.ARC.PCControl.RemoteClient;
import com.i2r.ARC.PCControl.ResponseAction;
import com.i2r.ARC.PCControl.link.RemoteLink;

/**
 * Class to parse elements from a {@link DataManager}.
 * This particular implementation is set to be used with an {@link ARCDataManager}, as a field.  
 * @see {@link DataParser} for contract details.
 * 
 * @author Johnathan Pagnutti
 *
 */
public class ARCDataParser implements DataParser<byte []> {

	//PACKET STRUCTURE:
	// ID
	// TYPE
	// ARG SIZE
	// ARG
	
	/**
	 * Constant to define that the parser expects to parse a new response segment
	 */
	public static final int NEW_RESPONSE = 0;
	
	/**
	 * Constant to define that the parser has parsed a new task ID
	 */
	public static final int READ_TASK_ID = 1;
	
	
	private static final int READ_ARGUMENT_TYPE = 2;
	
	/**
	 * Constant to define that the parser has parsed a new File Size
	 */
	public static final int READ_FILE_SIZE = 3;
	
	/**
	 * Constant to define the packet delimiter.
	 */
	public static final Byte RESPONSE_PACKET_DELIMITER = new Byte((byte)'\n');
	/**
	 * Constant to define the max size of the argument list
	 */
	public static final int ARGUMENT_LIST_SIZE = 1;
	
	/**
	 * Constant defines the integer value of ASCII 0
	 */
	public static final int ASCII_0 = 48;
	
	/**
	 * Constant defines the integer value of ASCII 9
	 */
	public static final int ASCII_9 = 57;
	
	/**
	 * Logger
	 */
	static final Logger logger = Logger.getLogger(ARCDataParser.class);
	
	//the current state of the buffer
	private int state;
	
	//the task ID assoiated with whatever the parser is parsing
	private int taskID = -1;
	
	//the file size associated with whatever the parser is parsing
	private int argumentSize = -1;
	
	private int argumentType = -1;
	
	//the amount of file bytes the parser has seen
	private int readCounter = 0;
	
	//an array to store the file bytes in as they're parsed
	private byte[] fileBytes = null;
	
	//the response arguments that the parser has seen
	private List<String> args;
	
	//synchronization and multithreading lock
	private final Object parseLock = new Object();
	
	//atomic double lock, to ensure that the subthread gets the parse lock before parse can be called again
	private AtomicBoolean lockAquired;
	
	
	//A list of the current data parsed.  Used for when we get incomplete segments of data and are waiting for the next block
	//to come in to finish them.
	private List<Byte> partialSection;

	public RemoteClient dev;
	
	/**
	 * Constructor
	 */
	public ARCDataParser(){
		//set the state of the parser to the default
		state = NEW_RESPONSE;
		
		//create the partial section list
		partialSection = new ArrayList<Byte>();
		
		//create the parsed argument list
		args = new ArrayList<String>();
		
		//set the lock as open
		lockAquired = new AtomicBoolean(false);
	}
	
	public ARCDataParser(RemoteClient dev){
		//set the state of the parser to the default
		state = NEW_RESPONSE;
		
		//create the partial section list
		partialSection = new ArrayList<Byte>();
		
		//create the parsed argument list
		args = new ArrayList<String>();
		
		//set the lock as open
		lockAquired = new AtomicBoolean(false);
		
		this.dev = dev;
	}
	
	/**
	 * Implemented from {@link DataParser}
	 * @see {@link DataParser} for contract information.
	 * 
	 * This method uses a double lock, the outer one is an atomic boolean, the inner one is a locking object
	 * Essentally, a calling method has to first set the lock aquired method, then get the locking object.  Another thread in
	 * another calling method now needs to wait for the thread to release the outer lock (set the lock to false from true) when it
	 * has aquired the inner lock (in a synchronized block with the inner locking object).
	 * 
	 * This ensures that the inner lock gets passed to the subthread before any logic in the parse method can execute.  This prevents
	 * a race condition, where a new block of data could start to be parsed before the parser's state reflected what was read in the 
	 * previous block of data
	 */
	@Override
	public void parseData(byte[] dataToParse) {
		//wait for the outer lock to open
		while(!lockAquired.compareAndSet(false, true)){}
		
		//aquired the right to aquire the lock
		//wait for the parselock
		synchronized(parseLock){
			//blocking any threads waiting on parse lock
			logger.debug("Parsing bytes: ");
			
			//log loop
			StringBuilder sb = new StringBuilder();
			for(byte b : dataToParse){
				sb.append(b);
				sb.append(" ");
			}
			logger.debug(sb.toString());
			
			//create a new thread object with the implementation as a parsing thread
			Thread t = new Thread(new ParseRunnable(dataToParse));
			
			//start it
			t.start();
		}
	}
	
	/****************************
	 * INNER CLASSES
	 ****************************/
	
	/**
	 * Class that implements runnable to define the parse thread used by the outer {@link ARCDataParser}
	 * This thread attempts to parse a dynamically sized block of data received from some external source, such as a {@link RemoteLink}
	 * 
	 * @author Johnathan Pagnutti
	 *
	 */
	 private class ParseRunnable implements Runnable{


		/**
		  * A list created from the raw data passed in to parse
		  */
		 private List<Byte> rawData;
		 
		/**
		 * A boolean flag if there is data left in the provided data block to parse
		 */
		 private boolean hasData = false;
		 
		 public ParseRunnable(byte[] bytesToParse){
			 //convert the raw array passed in to a List of Bytes
			 //allows for list utility methods to be called on the parsed bytes.
			 this.rawData = new ArrayList<Byte>();
			rawData.addAll(Arrays.asList(ArrayUtils.toObject(bytesToParse)));
			 
			//blocking any threads waiting on parse lock
			logger.debug("Parsing bytes: ");
				
			//log loop
			StringBuilder sb = new StringBuilder();
			for(Byte b : rawData){
				sb.append(b);
				sb.append(" ");
			}
			logger.debug(sb.toString());
		 }
		 
		 /**
		  * Implemented from {@link Runnable#run()}
		  * @See {@link Runnable#run()} for generic contract information
		  * 
		  * This method is the core parsing method.  Based on the past data read in the link, this shifts the outer {@link ARCDataParser}
		  * state to reflect what data to expect next from a connection
		  */
		@Override
		public void run() {
			//wait for the parse lock
			synchronized(parseLock){
				//got it
				logger.debug("Aquired Inner Lock");
								
				//Probably a bit overkill, but if an empty byte array has been passed to the sub thread, then we don't have data to parse
				if(!rawData.isEmpty()){
					//so if the provided raw data isn't empty, set the flag to tell the parser that we have work to do
					hasData = true;
				}
				
				//while we have data to parse
				while(hasData){
					//if the parser is expecting a new response
					if(state == NEW_RESPONSE){
						logger.debug("Attempting to read a task ID...");
						//the first element we get from a response is the task ID that generated this response
						parseTaskID();

					//if the parser has just read a task ID
					}else if(state == READ_TASK_ID){
						logger.debug("Parsing Argument Type...");
						
						parseArgumentType();
						
					}else if(state == READ_ARGUMENT_TYPE){
						logger.debug("Parsing Argument Size...");
						
						//the second element is the size of the third element (argument)
						parseArgumentSize();
					//if the parser has just read the argument size	
					}else if(state == READ_FILE_SIZE){
						logger.debug("Parsing Argument Data...");
						
						//parse argument
						parseArgumentData();
						
					}
				}
			}
			
			//release the lock
			lockAquired.compareAndSet(true, false);
			logger.debug("Released Outer Lock");
		}

		private void parseArgumentType() {
			//see if the packet delimiter is in the provided raw data
			if(!rawData.contains(RESPONSE_PACKET_DELIMITER)){
				logger.debug("A delimiter could not be found, adding data block to the partial data list.");
				//we did not see a delimiter, appending the data from this block to the partial list to use
				//append to the next block we scan.
				
				//check to make sure the data we're going to append could interpeted as a number, so that the
				//partial data block does not get corrupted.
				int partialCheck = interpetAsInt(rawData);
				
				if(partialCheck != -1){
					//if it could be considered as an ASCII number, append it to the patial list
					partialSection.addAll(rawData);
					logger.debug("Partial data list has " + partialSection.size() + " bytes.");	
				}
				
				//clear the block
				rawData.clear();
				hasData = false;
			}else{
				//we do have the packet delimiter, so we can parse a task ID from the partial data we have received and the
				//raw data block provided
				
				//get the index of the delimiter
				int rawPacketDelimiterIndex = rawData.indexOf(RESPONSE_PACKET_DELIMITER);
				
				List<Byte> argumentTypeBytes = new ArrayList<Byte>();
				argumentTypeBytes.addAll(partialSection);
				
				argumentTypeBytes.addAll(rawData.subList(0, rawPacketDelimiterIndex));
				
				logger.debug("Parsing " + argumentTypeBytes.size() + " bytes.");
				
				//clear the partial section data
				partialSection.clear();
				
				//logging loop.  I'm MAYHUD
				logger.debug("Argument Type bytes: ");
				//log loop
				StringBuilder sb = new StringBuilder();
				for(Byte b : argumentTypeBytes){
					sb.append(b);
					sb.append(" ");
				}
				logger.debug(sb.toString());
				
				//intepet the chunk as an integer
				argumentType = interpetAsInt(argumentTypeBytes);
				
				// if the task ID is still its default value...
				if(argumentType == -1){
					logger.error("Could not enterpet this segment as a argument type.");
					logger.error("Clearing current data to parse and resetting parser.");
					//wipe the current block
					argumentTypeBytes.clear();
					rawData.clear();
					hasData = false;
					
					//TODO: tell the remote device that the stream is dead
					//TODO: set the parser to wait for the stream clear packet
					
					//reset the parser
					parserReset();
				}else{
					//change the state of the parser to say that we have parsed a task ID
					state = READ_ARGUMENT_TYPE;
					logger.debug("Argument Type " + argumentType);
			
					//remove this sublist from the buffer, as we don't need it any more
					logger.debug("Pulling " + (rawPacketDelimiterIndex + 1) + " bytes as already read.");
					
					int i;
					for(i = 0; i < rawPacketDelimiterIndex; i++){
						//removal is performed byte by byte
						rawData.remove(0);
					}
					
					//remove the actual delimiter
					rawData.remove(0);					
					logger.debug("Amount in raw data after removal of " + (i + 1) + " bytes: " + rawData.size());

					//if there is nothing left in the data list, flag that we no longer have data to parse
					if(rawData.isEmpty()){
						logger.debug("Setting has data to false");
						hasData = false;
					}
				}
			}
			
		}

		private void parserReset() {
			logger.debug("Reset Parser");
			
			//reset the taskID
			taskID = -1;
			
			argumentType = -1;
			
			//reset the file size
			argumentSize = -1;
			
			//reset the file byte array
			fileBytes = null;
			
			//reset the number of bytes read
			readCounter = 0;
			
			//waiting for a new response
			state = NEW_RESPONSE;
		}

		private void respondWithParsedData() {
			new ResponseAction(new DataResponse(taskID, argumentType, fileBytes), dev).performAction();
		}

		private void parseArgumentData() {
			//if we haven't been to this part of the parser yet, the array to hold the file will be null
			//allocate space to hold the bytes
			if(fileBytes == null){
				fileBytes = new byte[argumentSize];
				logger.debug("Allocated " + argumentSize + " bytes to store an argument.");
			}
		
			//internal read counter
			int internalCounter = 0;
			
			logger.debug("This chunk has " + rawData.size() + " file bytes");
			logger.debug(readCounter + " bytes have been read thus far");
			//for each byte left in the raw byte read buffer
			for(Byte b : rawData){
				//if we have not read the correct number of bytes yet
				if(readCounter < fileBytes.length){
					//add that byte to the file array
					fileBytes[readCounter] = b.byteValue();
					//increment the bytes read counter
					readCounter++;
					internalCounter++;
				}
			}
			logger.debug("Read " + internalCounter + " on this pass");
			logger.debug("Total after this pass: " + readCounter);
			
			//if we've read all the bytes for the file
			if(readCounter == fileBytes.length){
				logger.debug("Read all of the argument bytes");
				
				//create a new ResponseAction and perform it
				respondWithParsedData();
				
				//reset the parser
				parserReset();
				
			}
			
			//remove read bytes from the raw list
			logger.debug("Pulling " + internalCounter + " bytes as already read.");
			logger.debug("Raw data: " + rawData.size());
			int i;
			for(i = 0; i < internalCounter; i++){
				rawData.remove(0);
			}
			
			//if there is nothing left in the data list, flag that we no longer have data to parse
			if(rawData.isEmpty()){
				logger.debug("No more data to parse for this chunk.");
				hasData = false;
			}
			
		}

		private int interpetAsInt(List<Byte> byteList) {
			int potentialFileSize = -1;
			byte[] temp = new byte[byteList.size()];
			boolean validNum = false;
			
			logger.debug("allocated " + temp.length + " bytes.");
			logger.debug("Converting: ");
			//debug loop
			StringBuilder sb = new StringBuilder();
			for(Byte b : byteList){
				sb.append(b.byteValue());
				sb.append(" ");
			}
			logger.debug(sb.toString());
			
			int i = 0;
			//for each byte in the arg sublist...
			for(Byte b : byteList){
				
				//see if the raw byte value falls between the ASCII integer values for 0 - 9
				//for those of us who know Java, chars are two bytes wide.  How do I not loose information?
				//I'm taking advantage of Java's byte widening system-- characters in the standard ASCII set have Unicode values of
				// /u00XX, as the upper unicode byte is not used.
				//When Java converts from a byte to a char, it first widens the byte to an int by sign-extending it.  This means that the
				//highest order bit from the byte is extended to fill the space on the int.
				//Now, by contract, I know that I will get ASCII characters, which do not use the high order sign bit
				// (ASCII ranges from 0000 0000 - 0111 1111), therefore this widening conversion will always fill with 0's.
				//Then, Java truncates the int down to a char, by removing the pair of high order bytes, leaving me with 0x00XX
				//Therefore, the resultant unicode character will always have 0x00 in its high byte, and the ASCII value in its low byte,
				//which will convert to the expected integer value.
				//
				//In conclusion, Dr. Nino, I KNOW EXACTLY WHAT I'M DOING.
				int rawValue = (char)b.byteValue();
				
				if(rawValue >= ASCII_0 && rawValue <= ASCII_9){
					temp[i] = b.byteValue();
					i++;
					validNum = true;
				}else{
					validNum = false;
					break;
				}
			}
			
			if(validNum){
				potentialFileSize = Integer.parseInt(new String(temp));
			}
			
			return potentialFileSize;
		}

		/**
		 * Private helper method to parse the task ID from the provided blocks of data.
		 */
		private void parseTaskID() {
			//see if the packet delimiter is in the provided raw data
			if(!rawData.contains(RESPONSE_PACKET_DELIMITER)){
				logger.debug("A delimiter could not be found, adding data block to the partial data list.");
				//we did not see a delimiter, appending the data from this block to the partial list to use
				//append to the next block we scan.
				
				//check to make sure the data we're going to append could interpeted as a number, so that the
				//partial data block does not get corrupted.
				int partialCheck = interpetAsInt(rawData);
				
				if(partialCheck != -1){
					//if it could be considered as an ASCII number, append it to the patial list
					partialSection.addAll(rawData);
					logger.debug("Partial data list has " + partialSection.size() + " bytes.");	
				}
				
				//clear the block
				rawData.clear();
				hasData = false;
			}else{
				//we do have the packet delimiter, so we can parse a task ID from the partial data we have received and the
				//raw data block provided
				
				//get the index of the delimiter
				int rawPacketDelimiterIndex = rawData.indexOf(RESPONSE_PACKET_DELIMITER);
				
				List<Byte> taskIdBytes = new ArrayList<Byte>();
				taskIdBytes.addAll(partialSection);
				
				taskIdBytes.addAll(rawData.subList(0, rawPacketDelimiterIndex));
				
				logger.debug("Parsing " + taskIdBytes.size() + " bytes.");
				
				//clear the partial section data
				partialSection.clear();
				
				//logging loop.  I'm MAYHUD
				logger.debug("task ID bytes: ");
				//log loop
				StringBuilder sb = new StringBuilder();
				for(Byte b : taskIdBytes){
					sb.append(b);
					sb.append(" ");
				}
				logger.debug(sb.toString());
				
				//intepet the chunk as an integer
				taskID = interpetAsInt(taskIdBytes);
				
				// if the task ID is still its default value...
				if(taskID == -1){
					logger.error("Could not enterpet this segment as a task ID.");
					logger.error("Clearing current data to parse and resetting parser");
					//wipe the current block
					taskIdBytes.clear();
					rawData.clear();
					hasData = false;
					
					//TODO: tell the remote device that the stream is dead
					//TODO: set the parser to wait for the stream clear packet
					
					//reset the parser
					parserReset();
				}else{
					//change the state of the parser to say that we have parsed a task ID
					state = READ_TASK_ID;
					logger.debug("task ID " + taskID);
			
					//remove this sublist from the buffer, as we don't need it any more
					logger.debug("Pulling " + (rawPacketDelimiterIndex + 1) + " bytes as already read.");
					
					int i;
					for(i = 0; i < rawPacketDelimiterIndex; i++){
						//removal is performed byte by byte
						rawData.remove(0);
					}
					
					//remove the actual delimiter
					rawData.remove(0);					
					logger.debug("Amount in raw data after removal of " + (i + 1) + " bytes: " + rawData.size());

					//if there is nothing left in the data list, flag that we no longer have data to parse
					if(rawData.isEmpty()){
						logger.debug("Setting has data to false");
						hasData = false;
					}
				}
			}
		}
		
		private void parseArgumentSize(){
			//see if the packet delimiter is in the provided raw data
			if(!rawData.contains(RESPONSE_PACKET_DELIMITER)){
				logger.debug("A delimiter could not be found, adding data block to the partial data list.");
				//we did not see a delimiter, appending the data from this block to the partial list to use
				//append to the next block we scan.
				
				//check to make sure the data we're going to append could interpeted as a number, so that the
				//partial data block does not get corrupted.
				int partialCheck = interpetAsInt(rawData);
				
				if(partialCheck != -1){
					//if it could be considered as an ASCII number, append it to the patial list
					partialSection.addAll(rawData);
					logger.debug("Partial data list has " + partialSection.size() + " bytes.");	
				}
				
				//clear the block
				rawData.clear();
				hasData = false;
			}else{
				//get the index of the delimiter
				int rawPacketDelimiterIndex = rawData.indexOf(RESPONSE_PACKET_DELIMITER);
				
				//get the sublist of the data read in from the first element to the index of the first delimiter
				List<Byte> argSublist = new ArrayList<Byte>();
				argSublist.addAll(partialSection);
				argSublist.addAll(rawData.subList(0, rawPacketDelimiterIndex));
				
				//clear the partial section data
				partialSection.clear();
				
				//logging loop
				logger.debug("Argument size bytes: ");
				//log loop
				StringBuilder sb = new StringBuilder();
				for(Byte b : argSublist){
					sb.append(b);
					sb.append(" ");
				}
				logger.debug(sb.toString());
				
				//see if the new argument can be interpeted as a filesize
				argumentSize = interpetAsInt(argSublist);
				
				//if the argument size could not be determined by this chunk...
				if(argumentSize == -1){
					logger.error("Could not enterpet this segment as an argument size.");
					logger.error("Clearing current data to parse and resetting parser");
					//wipe the current block
					argSublist.clear();
					rawData.clear();
					hasData = false;
					
					//TODO: tell the remote device that the stream is dead
					//TODO: set the parser to wait for the stream clear packet
					
					//reset the parser
					parserReset();
				//now that we have an argument size...
				}else{
					logger.debug("Argument size: " + argumentSize);
					logger.debug("State of parser set to read in an argument");
					//set the parser to start read a variable size argument
					state = READ_FILE_SIZE;
					
					//remove this sublist from the buffer, as we don't need it any more
					logger.debug("Pulling " + (rawPacketDelimiterIndex + 1) + " bytes as already read.");
					int i;
					for(i = 0; i < rawPacketDelimiterIndex; i++){
						//removal is performed byte by byte
						rawData.remove(0);
					}
					//remove the actual delimiter
					rawData.remove(0);
					
					logger.debug("Amount in raw data after removal of " + (i + 1) + " bytes: " + rawData.size());
					
					//if there is nothing left in the data list, flag that we no longer have data to parse
					if(rawData.isEmpty()){
						hasData = false;
					}
				}
			}
		}
	 }
}
