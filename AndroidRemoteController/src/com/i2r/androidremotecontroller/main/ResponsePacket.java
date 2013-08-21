package com.i2r.androidremotecontroller.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ARC.Constants;
import android.util.Log;

import com.i2r.androidremotecontroller.connections.RemoteConnection;

/**
 * This class models a response that the android device can build and send
 * across a {@link RemoteConnection} to a controlling PC.<br>
 * <br>
 * NOTE: this object is blind to its parameters, as they can vary greatly. Be
 * sure to use the {@link Constants} class when defining a new ResponsePacket.
 * 
 * @author Josh Noel
 * @see {@link #send(RemoteConnection)}
 * @see {@link #getNotificationPacket(int, String, String)}
 */
public class ResponsePacket {

	private static final String TAG = "ResponsePacket";

	public static boolean SHOW_DATA = true;

	private String header, footer;
	private int taskID, dataType;
	private byte[] data;

	
	/**
	 * Construct a new blank ResponsePacket.<br>
	 * NOTE: blank response packets are considered invalid. A response will only
	 * be sent across a connection when it is considered valid.
	 * 
	 * @see {@link ResponsePacket#isValid()}
	 */
	public ResponsePacket() {
		this.header = Constants.Args.ARG_STRING_NONE;
		this.footer = Constants.Args.ARG_STRING_NONE;
		this.taskID = Constants.Args.ARG_NONE;
		this.dataType = Constants.Args.ARG_NONE;
		this.data = null;
	}

	
	/**
	 * Construct a new valid ResponsePacket that can be written to a connection
	 * stream with
	 * {@link ResponsePacket#sendResponse(ResponsePacket, RemoteConnection)}.<br>
	 * NOTE: in order for this response packet to be valid, none of the
	 * following parameters can be null.
	 * 
	 * @param taskID
	 *            - the task id for the result data in this response. Task IDs
	 *            for data are supplied via the remote control PC.
	 * @param dataType
	 *            - the type of data being sent. This is to inform the remote
	 *            control PC what kind of data it is about to receive.
	 * @param data
	 *            - the actual data being sent
	 * @see {@link ResponsePacket#isValid()}
	 */
	public ResponsePacket(int taskID, int dataType, byte[] data) {
		this.header = Constants.Args.ARG_STRING_NONE;
		this.footer = Constants.Args.ARG_STRING_NONE;
		this.taskID = taskID;
		this.dataType = dataType;
		this.data = data;
	}

	
	/**
	 * Construct a new invalid ResponsePacket with a header, footer and task ID.
	 * This can be used when the data type and data to be sent are not readily
	 * known upon creation.<br>
	 * NOTE: setting the header and footer of this packet to anything other than
	 * {@link Args#ARG_CHAR_NONE} will cause that information to be sent when
	 * this response is sent. If you only want to send a bare valid packet,
	 * create an empty ResponsePacket and set the elements individually instead.
	 * 
	 * @param header
	 *            - the header identifier of this packet
	 * @param footer
	 *            - the footer identifier of this packet
	 * @param taskID
	 *            - the task ID of this packet. These are supplied by the remote
	 *            controlling device.
	 * @see {@link ResponsePacket#isValid()}
	 * @see {@link Constants#Args}
	 */
	public ResponsePacket(String header, String footer, int taskID) {
		this.header = header;
		this.footer = footer;
		this.taskID = taskID;
		this.dataType = Constants.Args.ARG_NONE;
		this.data = null;
	}

	
	/**
	 * Construct a new valid ResponsePacket that contains the full amount of
	 * information that a ResponsePacket can hold.<br>
	 * <br>
	 * NOTE: setting the header and footer of this packet to anything other than
	 * {@link Args#ARG_CHAR_NONE} will cause that information to be sent when
	 * this response is sent.<br>
	 * <br>
	 * SECONDARY NOTE: in order for this response packet to be valid, none of
	 * the following parameters can be null.
	 * 
	 * @param header
	 *            - the header identifier of this packet
	 * @param footer
	 *            - the footer identifier of this packet
	 * @param taskID
	 *            - the task id for the result data in this response. Task IDs
	 *            for data are supplied via the remote control PC.
	 * @param dataType
	 *            - the type of data being sent. This is to inform the remote
	 *            control PC what kind of data it is about to receive.
	 * @param data
	 *            - the actual data being sent
	 * @see {@link ResponsePacket#isValid()}
	 * @see {@link Constants#Args}
	 */
	public ResponsePacket(String header, String footer, int taskID, int dataType, byte[] data) {
		this.header = header;
		this.footer = footer;
		this.taskID = taskID;
		this.dataType = dataType;
		this.data = data;
	}

	
	// *********************|
	// GETTERS ------------|
	// *********************|

	
	/**
	 * Query for this response's header element
	 * 
	 * @return the header of this response if it has been set, or
	 *         {@link Args#ARG_CHAR_NONE} if it has not been set.
	 * @see {@link Constants#Args}
	 */
	public String getHeader() {
		return header;
	}

	
	/**
	 * Query for this response's footer element
	 * 
	 * @return the footer of this response if it has been set, or
	 *         {@link Args#ARG_CHAR_NONE} if it has not been set.
	 * @see {@link Constants#Args}
	 */
	public String getFooter() {
		return footer;
	}

	
	/**
	 * Query for this response's data type element. <br>
	 * NOTE: leaving this element of the ResponsePacket un-set will result in a
	 * failure to send the packet across a connection.
	 * 
	 * @return the data type of this response if it has been set, or
	 *         {@link Args#ARG_NONE} if it has not been set.
	 * @see {@link Constants#Args}
	 */
	public int getDataType() {
		return dataType;
	}

	
	/**
	 * Query for this response's task ID element. <br>
	 * NOTE: leaving this element of the ResponsePacket un-set will result in a
	 * failure to send the packet across a connection.
	 * 
	 * @return the task ID of this response if it has been set, or
	 *         {@link Args#ARG_NONE} if it has not been set.
	 * @see {@link Constants#Args}
	 */
	public int getTaskID() {
		return taskID;
	}

	
	/**
	 * Query for this response's byte array of data. <br>
	 * NOTE: leaving this element of the ResponsePacket null will result in a
	 * failure to send the packet across a connection.
	 * 
	 * @return the data byte array of this response if it has been set, or null
	 *         if it has not been set.
	 */
	public byte[] getData() {
		return data;
	}

	
	// ************************|
	// SETTERS ---------------|
	// ************************|

	
	/**
	 * Setter for the header element of this ResponsePacket.
	 * 
	 * @param header
	 *            - the header to set this ResponsePacket's header element to.
	 */
	public void setHeader(String header) {
		this.header = header;
	}
	

	/**
	 * Setter for the footer element of this ResponsePacket.
	 * 
	 * @param footer
	 *            - the header to set this ResponsePacket's footer element to.
	 */
	public void setFooter(String footer) {
		this.footer = footer;
	}

	
	/**
	 * Setter for the data type element of this ResponsePacket. <br>
	 * NOTE: failure to set this parameter for a ResponsePacket will result in a
	 * failure to send it across a connection.
	 * 
	 * @param dataType
	 *            - the data type to set this ResponsePacket's footer element
	 *            to.
	 * @see {@link ResponsePacket#isValid()}
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	
	/**
	 * Setter for the task ID element of this ResponsePacket. <br>
	 * NOTE: failure to set this parameter for a ResponsePacket will result in a
	 * failure to send it across a connection.
	 * 
	 * @param taskID
	 *            - the data type to set this ResponsePacket's task ID element
	 *            to.
	 * @see {@link ResponsePacket#isValid()}
	 */
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	
	/**
	 * Setter for the data element of this ResponsePacket. <br>
	 * NOTE: failure to set this parameter for a ResponsePacket will result in a
	 * failure to send it across a connection.
	 * 
	 * @param data
	 *            - the data type to set this ResponsePacket's data element to.
	 * @see {@link ResponsePacket#isValid()}
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	
	/**
	 * Tests to see if this response is well formed. Headers and Footers are
	 * seen as optional. This method is a composite of {@link #hasTaskID()},
	 * {@link #hasDataType()}, and {@link #hasData()}, and will only return true
	 * when all of these return true.
	 * 
	 * @return true if this response has a task ID, data type, and data to send,
	 *         false if any one of these has not been set.
	 */
	public boolean isValid() {
		return hasTaskID() && hasDataType() && hasData();
	}

	
	/**
	 * Query for the state of this ResponsePacket's header. This is used when
	 * determining if a header should be included in the response in
	 * {@link ResponsePacket#sendResponse(ResponsePacket, RemoteConnection)}
	 * 
	 * @return false if this packet's header is currently set to
	 *         {@link Args#ARG_CHAR_NONE}, true otherwise
	 * @see {@link Constants#Args}
	 */
	public boolean hasHeader() {
		return !header.equals(Constants.Args.ARG_STRING_NONE);
	}

	
	/**
	 * Query for the state of this ResponsePacket's footer. This is used when
	 * determining if a footer should be included in the response in
	 * {@link ResponsePacket#sendResponse(ResponsePacket, RemoteConnection)}
	 * 
	 * @return false if this packet's footer is currently set to
	 *         {@link Args#ARG_CHAR_NONE}, true otherwise
	 * @see {@link Constants#Args}
	 */
	public boolean hasFooter() {
		return !footer.equals(Constants.Args.ARG_STRING_NONE);
	}
	

	/**
	 * Query for the state of this ResponsePacket's task ID.<br>
	 * NOTE: in order for this packet to be sent across a connection
	 * successfully, this must return true.
	 * 
	 * @return false if this packet's header is currently set to
	 *         {@link Args#ARG_NONE}, true otherwise
	 * @see {@link Constants#Args}
	 */
	public boolean hasTaskID() {
		return taskID != Constants.Args.ARG_NONE;
	}
	

	/**
	 * Query for the state of this ResponsePacket's data type. In order for this
	 * response to be interpreted by the remote controller correctly, use one of
	 * the constants from {@link Constants#DataTypes}.<br>
	 * NOTE: in order for this packet to be sent across a connection
	 * successfully, this must return true.
	 * 
	 * @return false if this packet's data type is currently set to
	 *         {@link Args#ARG_NONE}, true otherwise
	 * @see {@link Constants#Args}
	 * @see {@link Constants#DataTypes}
	 */
	public boolean hasDataType() {
		return dataType != Constants.Args.ARG_NONE;
	}
	

	/**
	 * Query for the state of this ResponsePacket's data type. In order for this
	 * response to be interpreted by the remote controller correctly, use one of
	 * the constants from {@link Constants#DataTypes}.<br>
	 * NOTE: in order for this packet to be sent across a connection
	 * successfully, this must return true.
	 * 
	 * @return false if this packet's data type is currently set to
	 *         {@link Args#ARG_NONE}, true otherwise
	 * @see {@link Constants#Args}
	 * @see {@link Constants#DataTypes}
	 */
	public boolean hasData() {
		return data != null && data.length > 0;
	}
	
	
	/**
	 * Sends the current values of this ResponsePacket
	 * across the given connection.
	 * @param connection - the connection to write this
	 * encoded packet to.
	 * @return true if this response was sent across the
	 * given connection, false if this response failed
	 * to be sent. Failure of being sent can be due
	 * to this response being invalid, or the connection
	 * being null or invalid.
	 * @see {@link #isValid()}
	 * @see {@link RemoteConnection#isConnected()}
	 * @see {@link #sendResponse(ResponsePacket, RemoteConnection)}
	 */
	public boolean send(RemoteConnection connection){
		return sendResponse(this, connection);
	}
	

	/**
	 * Query for the string representation of this ResponsePacket with its
	 * underlying data excluded.
	 * 
	 * @return the string representation of this ResponsePacket with its
	 *         underlying data excluded from the result.
	 */
	@Override
	public String toString() {
		return toString(false);
	}
	

	/**
	 * Query for the string representation of this ResponsePacket with its
	 * underlying data included.
	 * 
	 * @return the string representation of this ResponsePacket with its
	 *         underlying data included in the result.
	 */
	public String toStringWithData() {
		return toString(SHOW_DATA);
	}
	

	/**
	 * Returns the string representation of this ResponsePacket, with an option
	 * to show its uderlying data.
	 * 
	 * @param showData
	 *            - the flag for showing data
	 * @return the string representation of this ResponsePacket - data is shown
	 *         only if the showData flag is set to true.
	 */
	private String toString(boolean showData) {
		StringBuilder builder = new StringBuilder();
		builder.append("header: ");
		builder.append(header);
		builder.append("\ntask ID: ");
		builder.append(taskID);
		builder.append("\ndata type: ");
		builder.append(dataType);
		builder.append("\ndata size: ");

		if (data != null) {
			builder.append(data.length);
			if (showData) {
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
	 * Query for a ResponsePacket object that notifies the
	 * remote device about this android device's state.
	 * 
	 * @param taskID
	 *            - the taskID identifying the notification source
	 * @param notification
	 *            - the notification that the remote device needs to be told
	 *            about
	 * @return a ResponsePacket with a notification data type and the following
	 * parameters as its other types.
	 * @see {@link Constants#Notifications}<br>
	 *      {@link #getNotificationPacket(int, String)}
	 */
	public static ResponsePacket getNotificationPacket(int taskID, char notification) {
		return getNotificationPacket(taskID, Character.toString(notification));
	}
	

	/**
	 * Query for a ResponsePacket object that notifies the
	 * remote device about this android device's state.
	 * 
	 * @param taskID
	 *            - the taskID identifying the notification source
	 * @param notification
	 *            - the notification that the remote device needs to be told
	 *            about
	 * @return a ResponsePacket with a notification data type and the following
	 * parameters as its other types.
	 * @see {@link Constants#Notifications}
	 * @see {@link #getNotificationPacket(int, String, String)}
	 */
	public static ResponsePacket getNotificationPacket(int taskID, String notification) {
		return getNotificationPacket(taskID, notification, Constants.Args.ARG_STRING_NONE);
	}
	
	
	/**
	 * See {@link #getNotificationPacket(int, String, String)} for
	 * details on this method.
	 */
	public static ResponsePacket getNotificationPacket(int taskID, char notification, String message) {
		return getNotificationPacket(taskID, Character.toString(notification), message);
	}
	

	/**
	 * Query for a ResponsePacket object that notifies the
	 * remote device about this android device's state.
	 * 
	 * @param taskID
	 *            - the taskID identifying the notification source
	 * @param notification
	 *            - the notification that the remote device needs to be told
	 *            about
	 * @param message
	 *            - a message to further inform the controller about the
	 *            state of this notification. This parameter will not be sent if
	 *            it is equal to {@link ARG_STRING_NONE} as defined in
	 *            {@link Constants#Args}
	 * @return a ResponsePacket with a notification data type and the following
	 * parameters as its other types.
	 * @see {@link Constants#Notifications}
	 */
	public static ResponsePacket getNotificationPacket(int taskID, String notification, String message) {
		ResponsePacket packet;
		if (!message.equals(Constants.Args.ARG_STRING_NONE)) {
			StringBuilder builder = new StringBuilder();
			builder.append(notification);
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
			builder.append(message);
			builder.append(Constants.Delimiters.PACKET_DELIMITER);
			packet = new ResponsePacket(taskID, Constants.DataTypes.NOTIFY,
					builder.toString().getBytes());
		} else {
			packet = new ResponsePacket(taskID, Constants.DataTypes.NOTIFY,
					notification.getBytes());
		}
		return packet;
	}
	

	/**
	 * Sends the byte array encoded version of the response packet across the
	 * given connection, providing the packet and connection are valid and not
	 * null.
	 * 
	 * @param packet
	 *            - the packet to encode and send across the given connection
	 * @param connection
	 *            - the connection to send the packet over
	 * @return true if the packet was successfully sent, false otherwise
	 */
	public static synchronized boolean sendResponse(ResponsePacket packet, RemoteConnection connection) {
		byte[] result = null;
		if (packet != null && packet.isValid() && connection != null && connection.isConnected()) {
			result = encodePacket(packet, Constants.Delimiters.PACKET_DELIMITER);
			if (result != null) {
				//Log.d(TAG, "response:\n" + packet.toStringWithData());
				connection.write(result);
			} else {
				Log.e(TAG, "could not send response because encodedPacket returned null");
			}
		} else {

			StringBuilder builder = new StringBuilder();
			builder.append("response was not sent:\n");
			builder.append((packet == null) ? "packet is null\n" : (!packet
					.isValid()) ? "packet is not valid\n" : "");
			builder.append((connection == null) ? "connection is null"
					: (!connection.isConnected()) ? "connection is not valid"
							: "");

			Log.e(TAG, builder.toString());
		}

		return result != null;
	}

	
	/**
	 * Encodes the given ResponsePacket to a byte array that can be sent across
	 * an ARC connection. This is the bare bones ordering of how a ResponsePacket
	 * should be sent to the remote controller.
	 * 
	 * @param packet
	 *            - the packet to write to a byte array
	 * @param delimiter
	 *            - the delimiter to use inbetween this response's data
	 * @param includeWrappers
	 *            - option to include header and footer wrappers for this
	 *            response. If this response was not built with a header or
	 *            footer, false should be put here.
	 * @return a byte array representing the encoded version of a response
	 *         packet to send across an ARC connection to the controlling PC, or
	 *         null if writing to a temp ByteArrayOutputStream failed.
	 */
	private static byte[] encodePacket(ResponsePacket packet, char delimiter) {

		byte[] result = null;

		// make sure data is valid before it is sent
		if (packet.isValid()) {

			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				// include a header only if it has been added
				if (packet.hasHeader()) {
					stream.write(packet.header.getBytes());
					stream.write(delimiter);
				}

				// As of now, the ordering is: task ID, data type, data size, data.
				// The ints are converted to their ascii characters and
				// then the bytes of those characters are stored in the
				// result stream.
				stream.write(String.valueOf(packet.taskID).getBytes());
				stream.write(delimiter);
				stream.write(String.valueOf(packet.dataType).getBytes());
				stream.write(delimiter);
				stream.write(String.valueOf(packet.data.length).getBytes());
				stream.write(delimiter);
				stream.write(packet.data);

				// include a footer only if it has been added
				if (packet.hasFooter()) {
					stream.write(packet.footer.getBytes());
					stream.write(delimiter);
				}

				result = stream.toByteArray();

			} catch (IOException e) {
				Log.e(TAG,
						"error while creating byte encoded packet: "
								+ e.getMessage());
			}

		} else {
			Log.e(TAG,
					"error - response is not well formed and could not be transposed to a byte array result");
		}

		return result;
	}
}
