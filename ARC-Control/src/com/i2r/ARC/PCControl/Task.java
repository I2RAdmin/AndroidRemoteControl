/**
 * 
 */
package com.i2r.ARC.PCControl;


/**
 * This class is a representation of a task, which is an ARC command with a task id
 * 
 * @author Johnathan
 *
 */
public class Task {
	
	private int id;
	int pos;
	private ARCCommand command;
	
	/**
	 * Constructor
	 * 
	 * @param newID a new task ID
	 * @param command a new command
	 */
	public Task(int newID, ARCCommand command){
		this.id = newID;
		this.command = command;
		this.pos = 0;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the command
	 */
	public ARCCommand getCommand() {
		return command;
	}

}
