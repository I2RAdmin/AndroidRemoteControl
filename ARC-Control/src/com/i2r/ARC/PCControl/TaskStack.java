/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class instantiates the task stack
 * 
 * @author Johnathan
 *
 */
public class TaskStack {
	
	static final Logger logger = Logger.getLogger(TaskStack.class);
	
	private HashMap<Integer, Task> taskMap;
	private ArrayList<Task> taskList;
	
	public TaskStack(){
		taskMap = new HashMap<Integer, Task>();
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
		//Look at the current set of tasks in the map
		Set<Integer> taskIDSet = taskMap.keySet();
		int newId = 0;
		
		//while we already have a task mapped to a particular id
		while(taskIDSet.contains(Integer.valueOf(newId))){
			//check a new id
			newId++;
		}
		
		//create a new task with the unique id
		Task task = new Task(Integer.valueOf(newId), newCommand);
		
		//add it to the task map
		taskMap.put(Integer.valueOf(newId), task);
		
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
		taskMap.remove(Integer.valueOf(taskID));
		logStackState();
	}

	/**
	 * check to see if there are tasks remaining in the task stack
	 * 
	 * @return if there are tasks left.
	 */
	public synchronized boolean tasksRemaining() {
		return !taskMap.isEmpty();
	}

	/**
	 * Get a task from the task stack given a task ID
	 * @param taskID the task t get
	 * @return the task, or null if the task was not found
	 */
	public synchronized Task getTask(int taskID) {
		logStackState();
		return taskMap.get(Integer.valueOf(taskID));
	}
	
	//private internal method to log the current state of the task stack
	public synchronized String logStackState(){
		logger.debug("Task Stack State");
		StringBuilder sb = new StringBuilder();
		for(Task t : taskMap.values()){
			sb.append(t.getId());
			sb.append(" ");
			sb.append(t.getCommand().getHeader());
			sb.append(" ");
			for(String arg : t.getCommand().getArguments()){
				sb.append(arg);
				sb.append(" ");
			}
			
			sb.append("\n");
		}
		logger.debug(sb.toString());
		return sb.toString();
	}
}
