/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class abstracts the data structure for {@link Task}s.  It's not actually a stack.
 * The data strcutre is a map, which maps a {@link Integer} to a {@link Task}.  The <code>Integer</code> key is the task ID,
 * the unique identifier for that task.
 * <p>
 * The addition algorithm tries to use low numbers first.  Starting at 0, the code incramets by one until it finds a number not
 * currently be using as a task ID.  This becomes the task ID of the current task to add, and it is inserted into the map.
 * <p>
 * The obvious result from this is that there may be many tasks that have task ID 0 (or 1, or 345), however, there will never be
 * two task 0's at the same time.
 * <p>
 * In addition, the accessing methods to the task stack are synchronized so that multiple threads will not ruin the Task Stack
 * 
 * @author Johnathan Pagnutti
 *
 */
public class TaskStack {
	
	/**
	 * Logger
	 */
	static final Logger logger = Logger.getLogger(TaskStack.class);
	
	/**
	 * The {@link Map} of {@link Integer}s to {@link Task}s.  The <code>Integer</code>s are the task ID's for the {@link Task}s that
	 * are mapped to that key.  The elements in the {@link Map} are the currently pending tasks.
	 */
	private HashMap<Integer, Task> taskMap;
	
	/**
	 * Constructor
	 */
	public TaskStack(){
		//create a new HashMap for for the taskMap
		taskMap = new HashMap<Integer, Task>();
	}
	
	/**
	 * Creates a new task object and adds it to the task stack.  Also assigns an ID to that task.
	 * No messing with the task stack object while we're creating a new task
	 * 
	 * @param newCommand command to add
	 * @return the task created (which has a reference to the ID it got)
	 */
	public synchronized Task createTask(ARCCommand newCommand){
		//get a set of the current integers in the task map
		Set<Integer> taskIDSet = taskMap.keySet();
		//start the count at 0
		int newId = 0;
		
		//while we already have a task mapped to a particular id
		while(taskIDSet.contains(Integer.valueOf(newId))){
			//check the next number
			newId++;
		}
		
		//create a new task with the unique id
		logger.debug("Creating a new task with ID: " + newId);
		Task task = new Task(Integer.valueOf(newId), newCommand);
		
		//add it to the task map
		taskMap.put(Integer.valueOf(newId), task);
		
		//debug and print to output
		logger.debug(logStackState());
		
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
		
		//if the task has been holding on to data...
		if(taskMap.get(taskID).taskData != null){
			//attempt to save it
			logger.debug("Saving data held by the task...");
			saveTaskData(taskID);
		}
		
		//remove the task from the map
		taskMap.remove(Integer.valueOf(taskID));
		
		//debug and print to UI out
		logger.debug(logStackState());
	}

	/**
	 * Save the data held on to by a particular task
	 * 
	 * @param taskID the id of the task to save the data from
	 */
	private void saveTaskData(int taskID) {
		logger.debug("Saving data");
		//save the data as one or more files
		taskMap.get(taskID).pushDataToFile();
	}

	/**
	 * check to see if there are tasks remaining in the task stack
	 * @return true if there are tasks left, false otherwise
	 */
	public synchronized boolean tasksRemaining() {
		return !taskMap.isEmpty();
	}

	/**
	 * Get a task from the task stack given a task ID
	 * @param taskID the id of the task to get
	 * @return the task, or null if the task was not found
	 */
	public synchronized Task getTask(int taskID) {
		//log the state of the task stack
		logger.debug(logStackState());
		//return the task
		return taskMap.get(Integer.valueOf(taskID));
	}
	
	/**
	 * Get a string that represents the current state of the task stack
	 * 
	 * @return the state of the task stack as a string
	 */
	public synchronized String logStackState(){
		StringBuilder sb = new StringBuilder();
		sb.append("Task Stack State\n");
		//for each task in the stack
		for(Task t : taskMap.values()){
			//get the ID
			sb.append(t.getId());
			sb.append(" ");
			//get the string representation of the command header for this task
			sb.append(t.getCommand().getHeader().getAlias());
			sb.append(" ");
			//get any arguments passed along with the header
			for(String arg : t.getCommand().getArguments()){
				sb.append(arg);
				sb.append(" ");
			}
			sb.append("\n");
		}
		
		//return the task stack state.
		return sb.toString();
	}

	/**
	 * Check to see if a particular task exists in the task stack
	 * 
	 * @param taskID the ID of the task to check
	 * @return true if a task with that ID has been found, false if otherwise
	 */
	public boolean hasTask(Integer taskID) {
		if(taskMap.containsKey(taskID)){
			return true;
		}
		return false;
	}
}
