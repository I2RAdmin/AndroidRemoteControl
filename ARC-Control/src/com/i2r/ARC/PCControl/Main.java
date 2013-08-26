/**
 * 
 */
package com.i2r.ARC.PCControl;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The main class.  This is the class that contians the main method and the entry point into the program.
 * <p>
 * Starts the program by first initalizing the logger, then getting an instance of the {@link Controller} and initalizing it.  After that,
 * the {@link Controller}'s run method is called, and program control shifts to the Controller (the Controller runs on Thread-0)
 * <p>
 * If the run method returns or errors out, then the System is shut down, killing any running subthreads.
 * <p>
 * @author Johnathan Pagnutti
 *
 */
public class Main {

	//logger
	static final Logger logger = Logger.getLogger(Main.class);
	
	/**
	 * The entry point of the program.  As it stands, program configuration happens in a properties file, so any arguments passed to
	 * the program are ignored.
	 * <p>
	 * @param args arguments passed to the program
	 */
	public static void main(String[] args) {
		
		//initialize the logger
		PropertyConfigurator.configure("log4j.properties");
		
		logger.debug("\n\nPROGRAM START\n");
		//get a reference to the controller
		Controller cntrl = Controller.getInstance();
		
		//initialize it
		cntrl.initalize();
		
		//run the program
		cntrl.genericRun();
		
		System.exit(0); //shuts down any pending threads.
	}

}
