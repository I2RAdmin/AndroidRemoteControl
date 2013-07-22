/**
 * 
 */
package com.i2r.ARC.PCControl.data;

import java.util.Observable;
import java.util.Observer;

/**
 * Class that handles new things being in the I/O queues
 * @author Johnathan
 *
 */
public class DataReader<T> implements Observer{
	DataLayer<T> ioBuffer;
	
	public DataReader (DataLayer<T> layer){
		ioBuffer = layer;
	}

	/**
	 * Updates the classes observing this data reader when the stacks change in the data layer
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
	
	
}
