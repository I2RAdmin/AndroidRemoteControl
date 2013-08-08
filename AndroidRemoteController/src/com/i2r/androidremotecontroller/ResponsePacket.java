package com.i2r.androidremotecontroller;

import ARC.Constants;

import android.util.Log;

import com.i2r.androidremotecontroller.connections.RemoteConnection;

/**
 * This class models a response that the android device can build
 * and send across a {@link RemoteConnection} to a controlling PC.
 * NOTE: this object is blind to its parameters, as they can vary
 * greatly. Be sure to use the {@link Constants} class when defining
 * a new ResponsePacket.
 * @author Josh Noel
 *
 */
public class ResponsePacket {

	private static final String TAG = "ResponsePacket";
	
	public static boolean SHOW_DATA = true;
	
	private char header, footer;
	private int taskID, dataType;
	private byte[] data;
	
	
	public ResponsePacket(){
		this.header = Constants.Args.ARG_CHAR_NONE;
		this.footer = Constants.Args.ARG_CHAR_NONE;
		this.taskID = Constants.Args.ARG_NONE;
		this.dataType = Constants.Args.ARG_NONE;
		this.data = null;
	}
	
	
	public ResponsePacket(int taskID, int dataType, byte[] data){
		this.header = Constants.Args.ARG_CHAR_NONE;
		this.footer = Constants.Args.ARG_CHAR_NONE;
		this.taskID = taskID;
		this.dataType = dataType;
		this.data = data;
	}
	
	
	
	public ResponsePacket(char header, char footer, int taskID){
		this.header = header;
		this.footer = footer;
		this.taskID = taskID;
		this.dataType = Constants.Args.ARG_NONE;
		this.data = null;
	}
	
	
	public ResponsePacket(char header, char footer, int taskID, int dataType, byte[] data){
		this.header = header;
		this.footer = footer;
		this.taskID = taskID;
		this.dataType = dataType;
		this.data = data;
	}

	
	
	//*********************|
	// GETTERS ------------|
	//*********************|
	
	
	public int getHeader(){
		return header;
	}
	
	
	public int getFooter(){
		return footer;
	}
	
	
	public int getDataType(){
		return dataType;
	}
	
	
	public int getTaskID(){
		return taskID;
	}
	
	
	public byte[] getData(){
		return data;
	}
	
	
	//************************|
	// SETTERS ---------------|
	//************************|
	
	
	public void setHeader(char header){
		this.header = header;
	}
	
	
	public void setFooter(char footer){
		this.footer = footer;
	}
	
	
	public void setDataType(int dataType){
		this.dataType = dataType;
	}
	
	
	public void setTaskID(int taskID){
		this.taskID = taskID;
	}
	
	
	public void setData(byte[] data){
		this.data = data;
	}
	
	
	/**
	 * Tests to see if this response is well formed.
	 * Headers and Footers are seen as optional.
	 * @return true if this response has a task ID, data type, and data
	 * to send, false if any one of these has not been set.
	 */
	public boolean isValid(){
		return hasTaskID() && hasDataType() && hasData();
	}
	
	
	public boolean hasHeader(){
		return header != Constants.Args.ARG_NONE;
	}
	
	
	public boolean hasFooter(){
		return footer != Constants.Args.ARG_NONE;
	}
	
	
	public boolean hasTaskID(){
		return taskID != Constants.Args.ARG_NONE;
	}
	
	
	public boolean hasDataType(){
		return dataType != Constants.Args.ARG_NONE;
	}
	
	
	public boolean hasData(){
		return data != null && data.length > 0;
	}

	
	/**
	 * Uses the static method defined in this class to
	 * return an encoded version of this packet. Headers
	 * and footers are assumed to be unnecessary.
	 * @return the encoded version of this response packet, or null
	 * if encoding failed
	 * @see {@link #encodePacket(ResponsePacket, char, boolean)}
	 */
	private byte[] encode() {
		return encodePacket(this, Constants.Delimiters.PACKET_DELIMITER);
	}
	
	
	@Override
	public String toString(){
		return toString(false);
	}
	
	
	public String toString(boolean showData){
		StringBuilder builder = new StringBuilder();
		builder.append("header: ");
		builder.append(header);
		builder.append("\ntask ID: ");
		builder.append(taskID);
		builder.append("\ndata type: ");
		builder.append(dataType);
		builder.append("\ndata size: ");
		
		if(data != null){
			builder.append(data.length);
			if(showData){
				builder.append("\ndata:\n");
				builder.append(new String(data));
			}
		} else {
			builder.append(0);
		}
		
		builder.append("\nfooter: ");
		builder.append(footer);
		return builder.toString();
	}
	
	
	
	
	/**
	 * Notifies the remote device about this android device's state
	 * @param taskID - the taskID identifying the notification source
	 * @param notification - the notification that the remote device needs
	 * to be told about
	 * @param connection - the connection to send the notification over
	 * @return true if the notification was sent, false otherwise
	 * @see {@link Constants#Notifications}
	 * @see {@link #sendNotification(int, String, String, RemoteConnection)}
	 */
	public static synchronized boolean sendNotification(int taskID, 
								String notification, RemoteConnection connection){
		return sendNotification(taskID, notification, Constants.Args.ARG_NONE, connection);
	}
	
	
	/**
	 * Notifies the remote device about this android device's state
	 * @param taskID - the taskID identifying the notification source
	 * @param notification - the notification that the remote device needs
	 * to be told about
	 * @param connection - the connection to send the notification over
	 * @return true if the notification was sent, false otherwise
	 * @see {@link Constants#Notifications}<br>
	 * {@link #sendNotification(int, String, RemoteConnection)}
	 */
	public static synchronized boolean sendNotification(int taskID, 
							char notification, RemoteConnection connection){
		return sendNotification(taskID, Character.toString(notification), connection);
	}
	
	
	
	/**
	 * See {@link #sendNotification(int, String, String, RemoteConnection)} 
	 * for details on this method.
	 */
	public static synchronized boolean sendNotification(int taskID, char notification, 
												int extraData, RemoteConnection connection){
		return sendNotification(taskID, Character.toString(notification), extraData, connection);
	}
	
	
	
	/**
	 * Notify the remote device about this android device's state
	 * @param taskID - the taskID identifying the notification source
	 * @param notification - the notification that the remote device needs
	 * to be told about
	 * @param extraData - any extra data to further inform the controller
	 * about the state of this notification. This parameter will not be
	 * sent if it is equal to {@link ARG_STRING_NONE} as defined in 
	 * {@link Constants#Args}
	 * @param connection - the connection to send the notification over
	 * @return true if the notification was sent, false otherwise
	 * @see {@link Constants#Notifications}
	 */
	public static synchronized boolean sendNotification(int taskID, String notification, 
											int extraType, RemoteConnection connection){
		ResponsePacket packet;
		if(extraType != Constants.Args.ARG_NONE){
			StringBuilder builder = new StringBuilder();
			builder.append(notification);
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
			builder.append(extraType);
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
			packet = new ResponsePacket(taskID, Constants.DataTypes.NOTIFY, 
					builder.toString().getBytes());
		} else {
			packet = new ResponsePacket(taskID, 
					Constants.DataTypes.NOTIFY, notification.getBytes());
		}
		return sendResponse(packet, connection);
	}
	
	
	
	/**
	 * Sends the byte array encoded version of the
	 * response packet across the given connection, providing
	 * the packet and connection are valid and not null.
	 * @param packet - the packet to encode and send across the given connection
	 * @param connection - the connection to send the packet over
	 * @return true if the packet was successfully sent, false otherwise
	 */
	public static synchronized boolean sendResponse(ResponsePacket packet, RemoteConnection connection) {
		byte[] result = null;
		if(packet != null && packet.isValid() && connection != null && connection.isConnected()){
			result = packet.encode();
			if(result != null){
				connection.write(result);
			} else {
				Log.e(TAG, "could not send response because encodedPacket returned null");
			} 
		} else {
			
			StringBuilder builder = new StringBuilder();
			builder.append("response was not sent:\n");
			builder.append((packet == null) ? "packet is null\n" : 
				(!packet.isValid()) ? "packet is not valid\n" : "");
			builder.append((connection == null) ? "connection is null" : 
				(!connection.isConnected()) ? "connection is not valid" : "");
			
			Log.e(TAG,  builder.toString());
		}
		
		return result != null;
	}

	
	
	/**
	 * Encodes the given ResponsePacket to a byte array that can be
	 * sent across an ARC connection.
	 * @param packet - the packet to write to a byte array
	 * @param delimiter - the delimiter to use inbetween this response's data
	 * @param includeWrappers - option to include header and footer wrappers for
	 * this response. If this response was not built with a header or footer,
	 * false should be put here.
	 * @return a byte array representing the encoded version of a response packet
	 * to send across an ARC connection to the controlling PC, or null if writing
	 * to a temp ByteArrayOutputStream failed.
	 */
	private static byte[] encodePacket(ResponsePacket packet, char delimiter) {
		
		byte[] result = null;
		
		if(packet.isValid()){
			
			StringBuilder buffer = new StringBuilder();
				
			if(packet.hasHeader()){
				buffer.append(packet.header);
				buffer.append(delimiter);
			}
			
			buffer.append(packet.taskID);
			buffer.append(delimiter);
			buffer.append(packet.dataType);
			buffer.append(delimiter);
			buffer.append(packet.data.length);
			buffer.append(delimiter);
			buffer.append(packet.data);
			
			
			if(packet.hasFooter()){
				buffer.append(packet.footer);
				buffer.append(delimiter);
			}
			
			result = buffer.toString().getBytes();

		} else {
			Log.e(TAG, "error - response is not well formed and could not be transposed to a byte array result");
		}
		
		return result;
	}
}
