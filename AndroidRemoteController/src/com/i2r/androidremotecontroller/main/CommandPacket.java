package com.i2r.androidremotecontroller.main;

import java.util.ArrayList;

import ARC.Constants;
import ARC.Constants.Args;
import android.util.Log;


/**
 * This class models a container for the commands parsed
 * from this application's {@link RemoteConnection} byte[] results.
 * A command cannot be created, but instead must be parsed with the
 * static method <br>{@link #parsePackets(String)} defined in this class.
 * @author Josh Noel
 */
public class CommandPacket {

	public static final int MAX_ALLOWED_SIZE = 10240;
	public static final String TAG = "CommandPacket";
	
	private int command, taskID;
	private String packetEnd;
	private String[] parameters;
	private int[] intParams;

	/**
	 * Constructor<br>
	 * Derives the given ArrayList of strings into meaningful
	 * information about this command.
	 * @param packet - the StringBuilder object to decode
	 * @throws NumberFormatException if a command int and task ID int could not
	 * be parsed from the given ArrayList
	 */
	private CommandPacket(ArrayList<String> packet) throws NumberFormatException {
		
		this.taskID = Integer.parseInt(packet.get(Constants.Commands.TASK_ID_INDEX));
		this.command = Integer.parseInt(packet.get(Constants.Commands.COMMAND_INDEX));
		this.packetEnd = packet.get(packet.size() - 1); // one over error
		this.parameters = null;

		int pointer = Constants.Commands.PARAM_START_INDEX;
		int paramLength = packet.size() - pointer - 1; // one over error
		
		if (paramLength > 0) {
			
			parameters = new String[paramLength];
			ArrayList<Integer> tempInts = new ArrayList<Integer>();
			
			for (int i = 0; i < paramLength; i++) {
				parameters[i] = packet.get(pointer);
				
				try{
					tempInts.add(Integer.parseInt(packet.get(pointer)));
				} catch (NumberFormatException e) {
					// not a number
				}
				
				pointer++;
			}
			
			if(!tempInts.isEmpty()){
				intParams = new int[tempInts.size()];
				for(int i = 0; i < intParams.length; i++){
					intParams[i] = tempInts.get(i);
				}
			}
		}
	}
	

	/**
	 * Query for the command packet's top level information, i.e. what
	 * its main objective is.
	 * @return the header of this command packet if it is a complete command,
	 * or {@link Args#ARG_NONE} if this is a partial packet.
	 * @see {@link Constants#Commands}
	 */
	public int getCommand() {
		return command;
	}

	
	/**
	 * Query for the command packet's task ID, given by the remote PC.
	 * @return the task ID of this command packet.
	 */
	public int getTaskID() {
		return taskID;
	}

	
	/**
	 * Retrieves the int value of the specified index
	 * from this packet's integer parameters, if it has any
	 * @param index - the index to return the value of
	 * @return the value at the specified index of this packet's
	 * integer parameters or {@link ARG_NONE} if there are no
	 * integer parameters or the index is out of range
	 * @see {@link Constants#Args}
	 */
	public int getInt(int index){
		int result = Constants.Args.ARG_NONE;
		if(intParams != null){
			try {
				result = intParams[index];
			} catch (ArrayIndexOutOfBoundsException e){
				Log.e(TAG, "index is not in range for params: " + index);
			}
		}
		return result;
	}
	
	
	/**
	 * Retrieves the String value of the specified index
	 * from this packet's String parameters, if it has any
	 * @param index - the index to return the value of
	 * @return the value at the specified index of this packet's
	 * String parameters or {@link ARG_STRING_NONE} if there are no
	 * String parameters or the index is out of range
	 * @see {@link Constants#Args}
	 */
	public String getString(int index){
		String result = Constants.Args.ARG_STRING_NONE;
		if(parameters != null){
			try {
				result = parameters[index];
			} catch (ArrayIndexOutOfBoundsException e){
				Log.e(TAG, "index is not in range for params: " + index);
			}
		}
		return result;
	}
	
	
	/**
	 * Query for the command packet's String parameter arguments.
	 * @return the String parameters of this command packet if it has any,
	 * or null if there are no String parameters or this is a partial packet.
	 */
	public String[] getStringParameters(){
		return parameters;
	}
	
	
	/**
	 * Query for the command packet's integer parameter arguments.
	 * @return the String parameters of this command packet if it has any,
	 * or null if there are no integer parameters or this is a partial packet.
	 */
	public int[] getIntParameters(){
		return intParams;
	}


	/**
	 * Query for the state of the String parameters of this command
	 * @return true if it has string parameters, false otherwise
	 */
	public boolean hasExtraStringParameters(){
		return parameters != null;
	}

	
	/**
	 * Query for the state of the integer parameters of this command
	 * @return true if it has integer parameters, false otherwise
	 */
	public boolean hasExtraIntParameters(){
		return intParams != null;
	}

	
	/**
	 * Query for this command packet's type
	 * @return true if this is a complete command packet,
	 * false if this is a partial packet
	 */
	public boolean isCompleteCommand() {
		return packetEnd.equals(Constants.Delimiters.PACKET_END) 
				&& command != Constants.Commands.NO_COMMAND;
	}
	
	
	/**
	 * Query for the state of this packet
	 * @return true if this is a blank command
	 * that should not be executed, false if this
	 * command is not blank
	 */
	public boolean isBlankCommand(){
		return command == Constants.Commands.NO_COMMAND;
	}
	
	
	/**
	 * Query for the priority state of this command
	 * @return true if this command is considered to
	 * be a high priority command, as defined in 
	 * {@link Constants#Commands}, false otherwise.
	 */
	public boolean hasHighPriority(){
		return command < 0;
	}

	

	/**
	 * Parses an array of {@link CommandPacket} objects from the given
	 * String buffer. This invocation assumes that the given String
	 * contains at least one complete command packet.
	 * @param buffer - the buffer to parse at least one complete CommandPacket from
	 * @return all complete commands found (if any) in the form of a CommandPacket array,
	 * with a partial as the last packet if no ending identifier is found in it.
	 * If no completed commands are found returns either an array of size 1 which
	 * contains 1 partial command, or null if no command could be parsed from the given
	 * buffer, due to a NumberFormatException thrown by {@link #decode(String)}
	 */
	public static CommandPacket[] parsePackets(String buffer) {
		CommandPacket[] packets = null;
		try{
			ArrayList<ArrayList<String>> decodedPackets = decode(buffer, 
							Constants.Delimiters.PACKET_DELIMITER, Constants.Delimiters.PACKET_END);

			if(!decodedPackets.isEmpty()){
				packets = new CommandPacket[decodedPackets.size()];
				for(int i = 0; i < packets.length; i++){
					ArrayList<String> sub = decodedPackets.get(i);
					if(!sub.isEmpty()){
						try{
							packets[i] = new CommandPacket(sub);
						} catch(NumberFormatException e){
							Log.e(TAG, "sub packet not formatted correctly");
						}
					} else {
						Log.e(TAG, "sub packet is empty");
					}
				}
			} else {
				Log.e(TAG, "packet list is empty");
			}
			
		} catch(NumberFormatException e){
			Log.e(TAG, "packets could not be parsed due to incorrect formatting:\n" + buffer);
		}
		return packets;
	}
	


	
	
	/**
	 * This static method parses the commands given in a byte array that is
	 * retrieved from reading data on a {@link RemoteConnection} for this
	 * application. The data is parsed based on the command
	 * {@link PACKET_DELIMITER}, which indicates a command's end point.
	 * If at any point a {@link PACKET_DELIMITER} is found, the command packet
	 * is deemed complete. This process is repeated until the buffer's end is reached.
	 * 
	 * @param buffer - the String to parse commands from
	 * @param delimiter - the delimiter to look for while parsing out sections of the string
	 * @param fullPacketIdentifier - the string to look for which indicates a packet's end.
	 * It implied that if the buffer's end has not been reached here, another packet's beginning
	 * immediately follows the previous one's ending. NOTE: these identifiers also need to be
	 * delimited by the given delimiter (the delimiter needs to immediately follow this) or
	 * they will not be interpreted correctly and will be skipped - this will most likely cause
	 * parsing errors.
	 * @return an ArrayList of ArrayLists of Strings. The latter ArrayList can be seen
	 * as an individual CommandPacket, while the former ArrayList is a container of packets.
	 * @see {@link Constants#Delimiters}
	 */
	private static ArrayList<ArrayList<String>> decode(String buffer,
							char delimiter, String fullPacketIdentifier) {
		
		ArrayList<ArrayList<String>> packetList = new ArrayList<ArrayList<String>>();
		ArrayList<String> subPacket = new ArrayList<String>();
		int subPacketPointer = 0;
		
		for (int i = 0; i < buffer.length(); i++) {
			
			if (buffer.charAt(i) == Constants.Delimiters.PACKET_DELIMITER) {
				
				String temp = buffer.substring(subPacketPointer, i);
				
				subPacket.add(temp);
				
				if(temp.equals(Constants.Delimiters.PACKET_END)){
					packetList.add(subPacket);
					subPacket = new ArrayList<String>();
				} 
				
				subPacketPointer = i + 1;
			}

		}
		
		return packetList;
	}
	


	/**
	 * Returns all the values of this
	 * CommandPacket, in the form "key: value\n"
	 */
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("task ID: ");
		builder.append(taskID);
		builder.append('\n');
		builder.append("command: ");
		builder.append(command);
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
