package com.i2r.androidremotecontroller.sensors;

import ARC.Constants;

/**
 * This is a helper class for duration settings
 * received by the controller. This essentially
 * is removing redundant code out of the concrete
 * {@link GenericDeviceSensor} implementations.
 * @author Josh Noel
 */
public class DurationHandler {
	
	private long startTime, max;
	private String id;
	private int index;
	
	/**
	 * Constructor<br>
	 * Creates a new DurationHandler with
	 * no start time and no max duration.
	 * {@link #maxReached()} will always
	 * return false in this state. The id and
	 * index of this handler are set to the "NONE"
	 * constants defined in {@link Constants#Args}
	 * @see {@link Constants#Args}
	 */
	public DurationHandler(){
		reset();
		this.id = Constants.Args.ARG_STRING_NONE;
		this.index = Constants.Args.ARG_NONE;
	}
	
	
	/**
	 * Constructor<br>
	 * Creates a new DurationHandler with
	 * no start time and no max duration.
	 * {@link #maxReached()} will always
	 * return false in this state.
	 * @param id - the ID that will be used
	 * to uniquely identify this handler.
	 * @param index - the index of this handler
	 * in a handler group. May be {@link Args#ARG_NONE}
	 * @see {@link Constants#Args}
	 */
	public DurationHandler(String id, int index){
		reset();
		this.id = id;
		this.index = index;
	}
	
	
	/**
	 * Starts this DurationHandler's timer.
	 * max duration must be set at least
	 * once before this or {@link #maxReached()}
	 * will never return true.
	 */
	public void start(){
		this.startTime = System.currentTimeMillis();
	}
	
	
	/**
	 * Query for the time passed in terms
	 * of the max duration currently set.
	 * @return true if the max duration has
	 * been set to a value greater than zero
	 * and the current time elapsed is greater
	 * than that duration, false otherwise.
	 */
	public boolean maxReached(){
		return max > 0 && System.currentTimeMillis() - startTime > max;
	}
	
	
	/**
	 * Resets the start time marker and the
	 * max duration marker. Once this is
	 * called, {@link #maxReached()} will
	 * return false until either {@link #setMax(int)}
	 * or {@link #setMax(long)} are called
	 */
	public void reset(){
		this.startTime = 0;
		this.max = 0;
	}
	
	
	/**
	 * Query for this DurationHandler's
	 * currently set max duration
	 * @return the currently set max duration
	 * for this  DurationHandler
	 */
	public long getMaxDuration(){
		return max;
	}
	
	
	/**
	 * Query for this handler's ID
	 * @return this handler's unique ID,
	 * or {@link Args#ARG_NONE} if the
	 * id parameter was not set at creation
	 */
	public String getID(){
		return id;
	}
	
	
	/**
	 * Query for this handler's index
	 * in a handler container.
	 * @return the handler's index in
	 * a given container, or {@link Args#ARG_NONE}
	 * if it was not given an index.
	 * @see {@link Constants#Args}
	 */
	public int getIndex(){
		return index;
	}
	
	
	/**
	 * Sets the max duration of this
	 * DurationHandler. Must be called at
	 * least once after {@link #reset()}
	 * has been called
	 * @param max - the max value to set
	 * this max equivalent to
	 */
	public DurationHandler setMax(int max){
		this.max = max;
		return this;
	}
	
	/**
	 * Sets the max duration of this
	 * DurationHandler. Must be called at
	 * least once after {@link #reset()}
	 * has been called
	 * @param max - the max value to set
	 * this max equivalent to
	 */
	public DurationHandler setMax(long max){
		this.max = max;
		return this;
	}
	
	
	@Override
	public boolean equals(Object other){
		boolean equal = false;
		
		if(other instanceof DurationHandler){
			equal = ((DurationHandler) other).id.equals(this.id);
		}
		
		return equal;
	}
	
}
