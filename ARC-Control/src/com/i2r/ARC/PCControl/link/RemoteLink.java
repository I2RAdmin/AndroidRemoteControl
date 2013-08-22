/**
 * 
 */
package com.i2r.ARC.PCControl.link;

import java.util.Arrays;
import java.util.List;

/**
 * Interface that specifies what a RemoteLink should do.
 * RemoteLinks are objects that manage potential connections.  They handle the initial discovery of a 
 * potential connection, the storing of that connection, and generating the actual connection object to handle 
 * a potential connection.
 * 
 * Remote links are primerally used to create {@link RemoteConnection} objects to actually send data and recieve data
 * @author Johnathan Pagnutti
 * @param <T> the action of data the {@link RemoteConnection} this {@link RemoteLink} creates will deal with.
 */
public interface RemoteLink<T> {

	/**
	 * This list is a generic list to return when the remote link is still searching for potential connections.
	 */
	public static final List<String> STILL_SEARCHING = Arrays.asList("STILL_SEARCHING");
	
	/**
	 * Search for valid remote connections.  This method may block, but its not part the contract to do so.
	 */
	public void searchForConnections();
	
	/**
	 * Return any valid connection URLS that have currently been found.
	 * 
	 * @return a list of valid connection URLs, the list STILL_SEARCHING if link is still searching for valid connections, or null if no
	 * 			valid connections could be found
	 */
	public List<String> currentConnections();
	
	/**
	 * Establish a connection from a potential connection URL or a default.
	 * Parameterized to handle data of a specified action.
	 * 
	 * @param connectionURL the URL of the connection to create, or null to use a default
	 * @return a {@link RemoteConnection} object to use to get the input and output streams of the connection.
	 * 			Typed to the same action as the remote link.
	 */
	public RemoteConnection<T> connect(String connectionURL);
}
