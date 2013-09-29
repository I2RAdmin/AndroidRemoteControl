package com.i2r.androidremotecontroller.connections;

/**
 * This interface models a simple responder
 * which processes data when it becomes
 * available.
 * @author Josh Noel
 */
public interface DataResponder<E> {

	/**
	 * Processes the given data. This
	 * should be used as a callback for
	 * multi-threading or blocking calls.
	 * @param data - the data to process
	 */
	public void onDataReceived(E data);
}
