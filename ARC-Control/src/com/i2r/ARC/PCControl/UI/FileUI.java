/**
 * 
 */
package com.i2r.ARC.PCControl.UI;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.i2r.ARC.PCControl.ARCCommand;
import com.i2r.ARC.PCControl.Controller;

/**
 * This is the most basic form of GenericUI, takes ARC commands from a file and sends them to the phone.
 * ARCCommand files are text files that use whitespace in place of the typical ARC command delimiter, with a newline
 * delimiting a new command.
 * 
 * TODO: add a Java enumeration that allows for some strings to be typed out, rather than raw values
 * 
 * @author Johnathan
 * @depreciated
 */
public class FileUI {
	
	static final Logger logger = Logger.getLogger(FileUI.class);
	/**
	 * The file to read for command input
	 */
	File inputFile;
	
	/**
	 * Controller to send commands to
	 */
	Controller cntrl;
	
	/**
	 * Constructor
	 * @param filePath the local path or full system path to use to get the file
	 */
	public FileUI(String filePath){
		cntrl = Controller.getInstance();
		
		inputFile = new File(filePath);
	}
	
	/**
	 * Reads a command from the file and uses a reference to the controller to create a task base don it
	 * 
	 * uses a subthread to pass read commands to the controller
	 */
	public void readCommands(){
		Thread t = new Thread(new FileUIRunnable(inputFile));
		t.start();
	}
	
	/******************************
	 * INNER CLASS
	 ******************************/
	
	private class FileUIRunnable implements Runnable{

		private static final int READ_HEADER = 1;
		private static final int NEW_COMMAND = 0;
		
		private File fileToRead;
		private int state;
		
		public FileUIRunnable(File threadFile){
			fileToRead = threadFile;
			state = NEW_COMMAND;
		}
		
		@Override
		public void run() {
			
			try {
				Scanner fileScanner = new Scanner(fileToRead);
				
				while(fileScanner.hasNextLine()){
					String line = fileScanner.nextLine();
					
					state = NEW_COMMAND;
					int thisHeader = -1;
					List<String> thisArgs = new ArrayList<String>();
					
					Scanner lineScanner = new Scanner(line);
					while(lineScanner.hasNext()){
						if(lineScanner.hasNextInt() && state == NEW_COMMAND){
							thisHeader = lineScanner.nextInt();
							state = READ_HEADER;
						}else if(state == READ_HEADER){
							thisArgs.add(lineScanner.next());
						}
					}
					
					ARCCommand sendCommand;
					if(thisArgs.isEmpty()){
						sendCommand = new ARCCommand(thisHeader);
					}else{
						sendCommand = new ARCCommand(thisHeader, thisArgs);
					}
					
					cntrl.send(sendCommand);
				}
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
			
		}
	}
}
