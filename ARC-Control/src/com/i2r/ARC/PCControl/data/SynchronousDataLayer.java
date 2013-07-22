/**
 * 
 */
package com.i2r.ARC.PCControl.data;

import java.util.ArrayDeque;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

/**
 * Implementation of a Data Layer that uses sychronized methods to ensure that its input and output stacks are thread safe
 * It also contains an input and output stack
 * @author Johnathan
 *
 */
public class SynchronousDataLayer<T> extends Observable implements DataLayer<T>{
	static final Logger logger = Logger.getLogger(SynchronousDataLayer.class);
	
	//Synchronization lock for the data in stack
	final Object dataInLock = new Object();
	
	//Synchronization lock for the data out stack
	final Object dataOutLock = new Object();
	
	//The stack that handles data coming in from a remote device
	private ArrayDeque<T> dataInStack;
	
	//The stack taht handes data going out to a remote device
	private ArrayDeque<T> dataOutStack;
	
	/**
	 * Constructor
	 */
	public SynchronousDataLayer(){
		//create a new data in stack
		this.dataInStack = new ArrayDeque<T>();
		
		//create a new data out stack
		this.dataOutStack = new ArrayDeque<T>();
	}

	public void registerObserver(Observer o){
		addObserver(o);
	}
	
	public void removeObserver(Observer o){
		removeObserver(o);
	}
	
	/**
	 * Push a new data element to the data object identified by id
	 * 
	 * @note is thread safe
	 */
	@Override
	public void push(int id, T newElement) {
		logger.debug("Adding " + newElement.toString() + " to the " + id + " stack");
		//for the supplied id...
		switch(id){
		//if the id is equal to DataLayer.INPUT
		case DataLayer.INPUT:
			//block for the input lock
			synchronized(dataInLock){
				//push to the input stack
				dataInStack.push(newElement);
			}
			setChanged();
			break;
		//if the id is equal to DataLayer.OUTPUT
		case DataLayer.OUTPUT:
			//block for the output lock
			synchronized(dataOutLock){
				//push to the output stack
				dataOutStack.push(newElement);
			}
			setChanged();
			break;
		//if the id is none of the above
		default:
			//don't do anything, pushing and poping to an unsupported stack is ignored by contract
			break;
		}
		notifyObservers();
	}

	/**
	 * pop the bottom data element from the stack selected by id 
	 * 
	 * @param id the stack to use
	 * 
	 * @return the element poped off the stack
	 */
	@Override
	public T pop(int id) {
		logger.debug("Removing the last element from the " + id + " stack");
		T element;
		//for the supplied id...
		switch(id){
		//if the id is equal to DataLayer.INPUT
		case DataLayer.INPUT:
			//block for the input lock
			synchronized(dataInLock){
				//pop the oldest element in the input stack
				element = dataInStack.pollLast();
			}
			setChanged();
			break;
		//if the id is equal to DataLayer.OUTPUT
		case DataLayer.OUTPUT:
			//block for the output lock
			synchronized(dataOutLock){
				//pop the oldest element in the output stack
				element = dataOutStack.pollLast();
			}
			setChanged();
			break;
		default:
			//set element to null.
			element = null;
		}
		notifyObservers(element);
		return element;
	}

}
