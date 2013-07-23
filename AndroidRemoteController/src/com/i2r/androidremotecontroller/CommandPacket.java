package com.i2r.androidremotecontroller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ARC.Constants;
import ARC.Constants.Args;


/**
 * This class models a container for the commands parsed
 * from this application's {@link BluetotohSocket} byte[] results.
 * A command cannot be created, but must be parsed with the
 * static methods defined in this class:<br>
 * {@link #parsePacket(Integer[])}<br>
 * {@link #parsePacket(StringBuilder)}<br>
 * {@link #parsePartialPacket(byte[])}<br>
 * {@link #parseFullPacket(byte[])}<br>
 * @author Josh Noel
 */
public class CommandPacket {

	public static final int MAX_ALLOWED_SIZE = 10240;
	public static final String TAG = "CommandPacket";
	
	private static final int PARTIAL = 0;
	private static final int FULL = 1;
	
	private int header, taskID, packetStatus;
	private int[] parameters;
	private byte[] partialPacket;

	/**
	 * Constructor #3
	 * Assumes the given StringBuilder is already
	 * a full command packet, and decodes it
	 * @param packet - the StringBuilder object to decode
	 */
	private CommandPacket(String packet) {
		initialize(decode(packet));
	}

	
	/**
	 * Initializes all the attributes of this CommandPacket
	 * object, based on the information in the given Integer array.
	 * @param packet - the array to base this object's information on.
	 */
	private void initialize(int[] packet) {

		this.taskID = packet[Constants.Commands.TASK_ID_INDEX];
		this.header = packet[Constants.Commands.HEADER_INDEX];
		this.packetStatus = packet[packet.length - 1];
		this.parameters = null;

		int pointer = Constants.Commands.PARAM_START_INDEX;
		int paramLength = packet.length - pointer - 1;

		if (paramLength > 0) {
			this.parameters = new int[paramLength];
			for (int i = 0; i < paramLength; i++) {
				parameters[i] = packet[pointer++];
			}
		}
	}


	/**
	 * Query for the command packet's top level information, i.e. what
	 * its main objective is.
	 * @return the header of this command packet if it is a complete command,
	 * or {@link Args#ARG_NONE} if this is a partial packet.
	 */
	public int getCommand() {
		return header;
	}

	/**
	 * Query for the command packet's task ID, given by the remote PC.
	 * @return the task ID of this command packet if it is a complete command,
	 * or {@link Args#ARG_NONE} if this is a partial packet.
	 */
	public int getTaskID() {
		return taskID;
	}

	/**
	 * Query for the command packet's parameter arguments.
	 * @return the parameters of this command packet if it is a complete command,
	 * or null if this is a partial packet.
	 */
	public int[] getParameters() {
		return parameters;
	}

	/**
	 * Query for the parameters of this command packet
	 * @return true if the parameters are not null and greater than zero,
	 * false otherwise.
	 */
	public boolean hasExtraParameters() {
		return parameters != null && parameters.length > 0;
	}

	/**
	 * Query for this command packet's main motive
	 * @return true if the main motive is to kill by ID
	 * false otherwise
	 */
	public boolean isKillByID() {
		return header == Constants.Commands.KILL;
	}

	/**
	 * Query for this command packet's main motive
	 * @return true if the main motive is to kill all processes
	 * false otherwise
	 */
	public boolean isKillAll() {
		return header == Constants.Commands.KILL_EVERYTHING;
	}

	/**
	 * Query for this command packet's main motive
	 * @param id - the ID to compare to this command's kill motive
	 * @return true if the main motive is to kill the ID given
	 * false otherwise
	 */
	public boolean killThis(int id) {
		return isKillByID() && this.taskID == id;
	}

	/**
	 * Query for this command packet's type
	 * @return true if this is a complete command packet,
	 * false if this is a partial packet
	 */
	public boolean isCompleteCommand() {
		return packetStatus == FULL;
	}
	
	/**
	 * Query for the buffer overflow of this partial packet
	 * @return true if the packet has been stitched to a
	 * size that exceeds {@link #MAX_ALLOWED_SIZE}, false otherwise
	 */
	public boolean maxBufferSizeReached(){
		return partialPacket.length > MAX_ALLOWED_SIZE;
	}



	/**
	 * Parses a {@link CommandPacket} object from the given StringBuilder.
	 * This invocation assumes that the given StringBuilder contains
	 * a complete command packet. Giving a partial command packet will
	 * result in the returned object being garbage.
	 * @param buffer - the buffer to parse a complete CommandPacket from
	 * @return a new CommandPacket with the parameters found in the
	 * given StringBuilder
	 */
	public static CommandPacket parsePacket(String buffer) {
		return new CommandPacket(buffer);
	}



	/**
	 * This static method parses the commands given in a byte array that is
	 * retrieved from reading data on the open bluetooth socket for this
	 * application. The data is parsed based on the command
	 * {@link Constants#TERMINATOR}, which indicates a command's end point. The
	 * resulting commands are returned in a String array in the order that they
	 * were parsed.
	 * 
	 * @param buffer
	 *            - the byte array to parse commands from
	 * @return a string array of the successfully parsed commands
	 */
	public static int[] decode(String buffer) {
		
		ArrayList<Integer> commands = new ArrayList<Integer>();
		int[] result = null;
		int start = 0;
		
		for (int i = 0; i < buffer.length(); i++) {
			
			if (buffer.charAt(i) == Constants.PACKET_DELIMITER) {
				commands.add(Integer.parseInt(buffer.substring(start, i)));
				start = i + 1;
			}
		}
		
		
		result = new int[commands.size() + 1];
		for(int i = 0; i < commands.size(); i++){
			result[i] = commands.get(i).intValue();
		}
		
		// TODO: fix
		result[commands.size()] = Constants.PACKET_END; //result[commands.size() - 1] == Constants.PACKET_END ? FULL : PARTIAL;
		
		return result;
	}

	
	/**
	 * Encodes an integer array to a byte array
	 * @param commands - the commands to encode as bytes
	 * @return a byte array representing the encoded integer array
	 */
	public static byte[] encode(int[] commands) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < commands.length; i++) {
				builder.append(Integer.toString(commands[i]));
				builder.append(Constants.PACKET_DELIMITER);
			}
			stream.write(builder.toString().getBytes());
		} catch (IOException e) {
			stream = null;
		}
		return stream.toByteArray();
	}


	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("task ID: ");
		builder.append(taskID);
		builder.append('\n');
		builder.append("header: ");
		builder.append(header);
		builder.append('\n');
		builder.append("parameters: {");
		if(parameters != null){
			builder.append(parameters[0]);
			for(int i = 1; i < parameters.length; i++){
				builder.append(", ");
				builder.append(parameters[i]);
			}
		}
		builder.append("}\n");
		builder.append("complete (un-partial) task: ");
		builder.append(isCompleteCommand());
		return builder.toString();
	}
}
