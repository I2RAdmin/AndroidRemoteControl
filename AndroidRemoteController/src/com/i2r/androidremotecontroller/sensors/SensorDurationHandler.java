package com.i2r.androidremotecontroller.sensors;

import ARC.Constants;

/**
 * <p>
 * This is a helper class for duration settings received by the controller. This
 * essentially is removing redundant code out of the concrete
 * {@link GenericDeviceSensor} implementations, since most if not all
 * GenericDeviceSensor implementations require one or more timers for durations
 * set by the remote controller.
 * </p>
 * 
 * @author Josh Noel
 * @see {@link #setMax(int)}
 * @see {@link #setMax(long)}
 * @see {@link #start()}
 */
public class SensorDurationHandler {

	private long startTime, max;
	private String id;
	private int index;

	/**
	 * Constructor<br>
	 * Creates a new DurationHandler with no start time and no max duration.
	 * {@link #maxReached()} will always return false in this state. The id and
	 * index of this handler are set to the "NONE" constants defined in
	 * {@link Constants#Args}
	 * 
	 * @see {@link Constants#Args}
	 */
	public SensorDurationHandler() {
		reset();
		this.id = Constants.Args.ARG_STRING_NONE;
		this.index = Constants.Args.ARG_NONE;
	}

	/**
	 * Constructor<br>
	 * Creates a new DurationHandler with no start time and no max duration.
	 * {@link #maxReached()} will always return false in this state.
	 * 
	 * @param id
	 *            - the ID that will be used to uniquely identify this handler.
	 * @param index
	 *            - the index of this handler in a handler group. May be
	 *            {@link Args#ARG_NONE}
	 * @see {@link Constants#Args}
	 */
	public SensorDurationHandler(String id, int index) {
		reset();
		this.id = id;
		this.index = index;
	}

	/**
	 * Starts this DurationHandler's timer. max duration must be set at least
	 * once before this or {@link #maxReached()} will never return true. This
	 * method can be called multiple times without calling {@link #reset()} or
	 * setting a new max value. Calling this method again essentially sets this
	 * duration's timer back to zero.
	 */
	public void start() {
		this.startTime = System.currentTimeMillis();
	}

	/**
	 * Query for the time passed in terms of the max duration currently set.
	 * 
	 * @return true if the current time elapsed is greater than that duration,
	 *         false otherwise. This will return true if the max has not
	 *         been set.
	 */
	public boolean maxReached() {
		return  System.currentTimeMillis() - startTime > max;
	}
	
	
	/**
	 * Query about the state of this duration's "max" field
	 * @return true if the field is set to a positive value greater
	 * than zero, false otherwise
	 */
	public boolean hasMax(){
		return max > 0;
	}
	

	/**
	 * Resets the start time marker and the max duration marker. Once this is
	 * called, {@link #maxReached()} will return false until either
	 * {@link #setMax(int)} or {@link #setMax(long)} are called
	 */
	public void reset() {
		this.startTime = Constants.Args.ARG_NONE;
		this.max = Constants.Args.ARG_NONE;
	}

	/**
	 * Query for this DurationHandler's currently set max duration
	 * 
	 * @return the currently set max duration for this DurationHandler
	 */
	public long getMaxDuration() {
		return max;
	}

	/**
	 * Query for this handler's ID
	 * 
	 * @return this handler's unique ID, or {@link Args#ARG_NONE} if the id
	 *         parameter was not set at creation
	 */
	public String getID() {
		return id;
	}

	/**
	 * Query for this handler's index in a handler container.
	 * 
	 * @return the handler's index in a given container, or
	 *         {@link Args#ARG_NONE} if it was not given an index.
	 * @see {@link Constants#Args}
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets the max duration of this DurationHandler. Must be called at least
	 * once after {@link #reset()} has been called
	 * 
	 * @param max
	 *            - the max value to set this max equivalent to
	 * @return a reference to this handler as a convenience for starting and
	 *         setting the max in one line.
	 */
	public SensorDurationHandler setMax(int max) {
		this.max = max;
		return this;
	}

	/**
	 * Sets the max duration of this DurationHandler. Must be called at least
	 * once after {@link #reset()} has been called
	 * 
	 * @param max
	 *            - the max value to set this max equivalent to
	 * @return a reference to this handler as a convenience for starting and
	 *         setting the max in one line.
	 */
	public SensorDurationHandler setMax(long max) {
		this.max = max;
		return this;
	}

	/**
	 * compares the String id and int index of this handler
	 * the the object other, assuming that other can be
	 * casted to a {@link SensorDurationHandler}
	 */
	@Override
	public boolean equals(Object other) {
		boolean equal = false;

		if (other instanceof SensorDurationHandler) {
			SensorDurationHandler otherHandler = 
						(SensorDurationHandler) other;
			equal = otherHandler.id.equals(this.id) &&
					otherHandler.index == this.index;
		}

		return equal;
	}
	
}
