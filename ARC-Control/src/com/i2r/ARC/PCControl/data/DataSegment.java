/**
 * 
 */
package com.i2r.ARC.PCControl.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Johnathan
 *
 */
public class DataSegment {

	static final Logger logger = Logger.getLogger(DataSegment.class);
	
	public String fileType;
	private List<byte[]> rawData;
	
	public DataSegment(String fileType, List<byte[]> data){
		this.fileType = fileType;
		this.rawData = data;
	}
	
	public DataSegment(){
		fileType = "";
		rawData = new ArrayList<byte[]>();
	}
	
	public DataSegment(String fileType){
		this.fileType = fileType;
		rawData = new ArrayList<byte[]>();
	}
	
	public void appendData(byte[] data){
		if(rawData == null){
			rawData = new ArrayList<byte[]>();
		}
		
		rawData.add(data);
	}
	
	public byte[] getData(){
		int dataSize = 0;
		for(byte[] chunk : rawData){
			dataSize += chunk.length;
		}
		
		byte[] rawArray = new byte[dataSize];
		
		int pos = 0;
		for(byte[] chunk : rawData){
			for(byte b : chunk){
				rawArray[pos] = b;
				pos++;
			}
		}
		
		logger.debug("data size: " + rawArray.length);
		return rawArray;
	}
	
	public void saveSegmentAsFile(String fileNameHeader){
		logger.debug("Starting save file thread for: " + fileNameHeader);
		Thread t = new Thread(new SaveFileRunnable(fileNameHeader, this));
		
		t.start();
	}
	
	
	/**********
	 * INNER CLASS
	 **********/
	 
	private class SaveFileRunnable implements Runnable{

		private DataSegment seg;
		private String fileNameHeader;
		
		public SaveFileRunnable(String fileNameHeader, DataSegment seg){
			this.seg = seg;
			this.fileNameHeader = fileNameHeader;
		}
		
		@Override
		public void run() {
			
			logger.debug("... in save file thread.");
			File file = new File(fileNameHeader + "." + fileType);
			try {
				OutputStream fileOut = new FileOutputStream(file);
				fileOut.write(seg.getData());
				fileOut.flush();
				fileOut.close();
			} catch (IOException e) {
				logger.error("Unable to save task data to file.");
			}
			
		}
		
	}
}
