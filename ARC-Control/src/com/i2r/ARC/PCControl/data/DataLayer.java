/**
 * 
 */
package com.i2r.ARC.PCControl.data;

/**
 * Defines the methods to read and write to a data layer
 * 
 * It is important to note that the contract does not extend to the constants.  If the data layer either doesn't have enough data objects
 * to handle all the stacks, or chooses not to, it is up to the data layer to interpt how to handle IDs
 * @author Johnathan
 * @depreciated
 */
public interface DataLayer<T> {
	
	//codes for what data object to push/pop from
	static final int INPUT = 1;
	static final int OUTPUT = 2;
	
	/**
	 * Add a new element to this data layer
	 *  
	 * @param id the identifier of the data object to push the element to
	 * @param newElement
	 */
	public void push(int id, T newElement);
	
	/**
	 * Get the oldest element in this data layer
	 * 
	 * @param id the identifier of the data object you want to pop an element from
	 */
	public T pop(int id);
}
