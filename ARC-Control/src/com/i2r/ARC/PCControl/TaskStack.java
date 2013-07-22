/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * This class instantiates the task stack
 * 
 * @author Johnathan
 *
 */
public class TaskStack {
	
	static final Logger logger = Logger.getLogger(TaskStack.class);
	
	private ArrayList<Task> taskList;
	
	public TaskStack(){
		taskList = new ArrayList<Task>();
	}
	
	/**
	 * Creates a new task object and adds it to the task stack
	 * No messing with the task stack object while we're creating a new task
	 * 
	 * @param newCommand command to add
	 * @return the task created
	 */
	public synchronized Task createTask(ARCCommand newCommand){
		//create a new task
		Task task = new Task(taskList.size(), newCommand);
		
		//add the new task to the task stack
		taskList.add(task);
		
		//debuuuuug
		logStackState();
		
		//return the newly created task
		return task;
	}
	
	/**
	 * Removes a task (given an ID) from the task stack
	 * no touching the stack while we're removing an element
	 * 
	 * @param taskID the id of the task to remove
	 */
	public synchronized void removeTask(int taskID){
		logger.debug("Attempting to remove task " + taskID);
		taskList.remove(taskID);
		logStackState();
	}

	/**
	 * check to see if there are tasks remaining in the task stack
	 * 
	 * @return if there are tasks left.
	 */
	public synchronized boolean tasksRemaining() {
		return !taskList.isEmpty();
	}

	/**
	 * Get a task from the task stack given a task ID
	 * @param taskID the task t get
	 * @return the task, or null if the task was not found
	 */
	public synchronized Task getTask(int taskID) {
		logStackState();
		if(taskID > taskList.size()){
			return null;
		}else{
			return taskList.get(taskID); 
		}
	}
	
	//private internal method to log the current state of the task stack
	private synchronized void logStackState(){
		logger.debug("Task Stack State");
		for(Task t : taskList){
			StringBuilder sb = new StringBuilder();
			sb.append(t.getId());
			sb.append(" ");
			sb.append(t.getCommand().getHeader());
			sb.append(" ");
			for(String arg : t.getCommand().getArguments()){
				sb.append(arg);
				sb.append(" ");
			}
			logger.debug(sb.toString());
		}
	}
}
