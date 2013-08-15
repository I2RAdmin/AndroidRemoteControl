/**
 * 
 */
package com.i2r.ARC.PCControl;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Johnathan
 *
 */
public class Main {

	//logger
	static final Logger logger = Logger.getLogger(Main.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//initialize the logger
		PropertyConfigurator.configure("log4j.properties");
		
		logger.debug("\nPROGRAM START\n");
		Controller cntrl = Controller.getInstance();
		cntrl.initalize();
		
		cntrl.genericRun();
		
		System.exit(0); //shuts down any pending threads.
	}

}
