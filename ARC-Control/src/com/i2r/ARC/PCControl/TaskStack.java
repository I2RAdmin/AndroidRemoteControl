/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * This class abstracts the data structure for {@link Task}s.  It's not actually a stack.
 * The data structure is a map, which maps a {@link Integer} to a {@link Task}.  The <code>Integer</code> key is the task ID,
 * the unique identifier for that task.
 * <p>
 *.Task ID's are randomly generated from {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}.
 * <p>
 * The obvious result from this is that there may be many tasks that have task ID 0 (it's rare, but possible), however, there 
 * will never be two task 0's at the same time.
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
	private Map<Integer, Task> taskMap;
	
	Random rand;
	
	/**
	 * Constructor
	 */
	public TaskStack(){
		//create a new HashMap for for the taskMap
		taskMap = new ConcurrentHashMap<Integer, Task>();
		
		rand = new Random();
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
		
		//get a random number for the task ID
		int newId = rand.nextInt(Integer.MAX_VALUE);
		
		//while we already have a task mapped to a particular id
		while(taskIDSet.contains(Integer.valueOf(newId))){
			//generate a new random number
			newId = rand.nextInt(Integer.MAX_VALUE);
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
		
		//while(!processingTaskLock.compareAndSet(false, false));
		
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
		//if the task map has a task id that matches the one provided
		if(taskMap.containsKey(taskID)){
			//return true
			return true;
		}
		//otherwise, return false
		return false;
	}
}
