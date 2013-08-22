package com.i2r.ARC.PCControl.DataManager;

import com.i2r.ARC.PCControl.DataResponse;

/**
 * Class to parse the data Recieved from some some external source
 * 
 * Usually used by a {@link DataManager} to parse data from the {@link DataManager}'s input stream,
 * however, this functionality is not required by the contract.  {@link DataManager}s have a {@link DataManager#parser} field
 * for this typical contract case, that is also required by constructor.
 * 
 * Extends runnable to allow for the parsing step to run on its own thread and not eat processing time.
 * 
 * @author Johnathan Pagnutti
 *
 */
public interface DataParser<T>{	
	
	/**
	 * Parse method.  Takes the data received and attempts to make sense of it.
	 * Data is parsed by blocks, so for each block of data passed to the parser (via this method), an attempt is made to fit
	 * that data into whatever the parser expects to be seeing next.  Actual parser behavior can vary wildly from implementation to
	 * implementation.
	 * 
	 * After enough data has been seen that some action can be taken, a {@link DataResponse} object is created.  It is not required that
	 * the parser act, or tell another part of the program to act, on the created {@link DataResponse}.
	 * 
	 * @param dataToParse a data element from some data source.  The action of the element is determined at runtime
	 *
	 * @requires that some sort of data source be attached to the parser
	 * @ensures very little.  An attempt is made to create a valid response object (such as {@link DataResponse}), 
	 * 			however, as response objects are not defined by the contract, they can vary wildly and are 
	 * 			usually part of another class.  In addition, there is no requirement that the parser must act on a created {@link DataResponse}
	 *
	 * 			In short, expect a velociraptor to come attack you whenever this method is called.
	 */
	public void parseData(T dataToParse);
	
}
