/**
 * 
 */
package com.i2r.ARC.PCControl.data;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

/**
 * @author Johnathan
 *
 */
public class MultithreadStringBuffer{
	static final Logger logger = Logger.getLogger(MultithreadStringBuffer.class);
	
	List<Byte> charBuffer;
	private final ReentrantReadWriteLock lock;
	
    public MultithreadStringBuffer(){
    	charBuffer = new LinkedList<Byte>();
        lock = new ReentrantReadWriteLock();
    }
    
    public byte read(){
    	logger.debug("Attempting to read a byte from the buffer...");
         try{
             lock.readLock().lock();
             logger.debug("Reading: " + (char)charBuffer.get(0).byteValue());
             return charBuffer.get(0).byteValue();
         } finally{
        	 logger.debug("Removing: " + (char)charBuffer.get(0).byteValue());
        	 charBuffer.remove(0);
             lock.readLock().unlock();
         }
    }
    
    public void write(String message){
    	logger.debug("Attempting to write a message to the buffer...");
         try{
             lock.writeLock().lock();
             for(byte b: message.getBytes()){
            	 charBuffer.add(b);
             }
         } finally{
        	 logger.debug("Buffer state: ");
        	 StringBuilder sb = new StringBuilder();
        	 for(byte b : charBuffer){
        		 sb.append((char)b);
        	 }
        	 logger.debug(sb.toString());
             lock.writeLock().unlock();
         }
    }

	public boolean hasNext() {
		try{
			lock.readLock().lock();
			
			if(charBuffer.isEmpty()){
				return false;
			}else{
				return true;
			}
			
		} finally {
			lock.readLock().unlock();
		}
	}
}
