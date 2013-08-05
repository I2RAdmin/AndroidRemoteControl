package com.i2r.androidremotecontroller;

import java.util.ArrayList;

import ARC.Constants;
import ARC.Constants.Args;
import android.util.Log;


/**
 * This class models a container for the commands parsed
 * from this application's {@link RemoteConnection} byte[] results.
 * A command cannot be created, but must be parsed with the
 * static method defined in this class:<br>
 * {@link #parsePackets(String)}<br>
 * @author Josh Noel
 */
public class CommandPacket {

	public static final int MAX_ALLOWED_SIZE = 10240;
	public static final String TAG = "CommandPacket";
	
	private static final String FULL_PACKET_IDENTIFIER = "FULL_PACKET";
	
	private int command, taskID;
	private String packetEnd;
	private int[] intParams;
	private String[] stringParams;

	/**
	 * Constructor #3
	 * Assumes the given StringBuilder is already
	 * a full command packet, and decodes it
	 * @param packet - the StringBuilder object to decode
	 */
	private CommandPacket(ArrayList<String> packet) {
		
		this.taskID = Integer.parseInt(packet.get(Constants.Commands.TASK_ID_INDEX));
		this.command = Integer.parseInt(packet.get(Constants.Commands.HEADER_INDEX));
		this.packetEnd = packet.get(packet.size() - 1); // one over error
		this.intParams = null;
		this.stringParams = null;

		int pointer = Constants.Commands.PARAM_START_INDEX;
		int paramLength = packet.size() - pointer - 1; // one over error
		ArrayList<Integer> tempInts = new ArrayList<Integer>();
		
		if (paramLength > 0) {
			this.stringParams = new String[paramLength];
			for (int i = 0; i < paramLength; i++) {
				
				stringParams[i] = packet.get(pointer);
				
				try{
					tempInts.add(Integer.parseInt(packet.get(pointer)));
				} catch(NumberFormatException e){
					Log.d(TAG, e.getMessage());
				}
				
				pointer++;
			}
			
			if(!tempInts.isEmpty()){
				intParams = new int[tempInts.size()];
				for(int i = 0; i < intParams.length; i++){
					intParams[i] = tempInts.get(i).intValue();
				}
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
		return command;
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
	 * Query for the command packet's int parameter arguments.
	 * @return the int parameters of this command packet if it has any,
	 * or null if there are no int parameters or this is a partial packet.
	 */
	public int[] getIntParameters() {
		return intParams;
	}
	
	
	/**
	 * Query for the command packet's String parameter arguments.
	 * @return the String parameters of this command packet if it has any,
	 * or null if there are no String parameters or this is a partial packet.
	 */
	public String[] getStringParameters(){
		return stringParams;
	}

	
	/**
	 * Query for the state of the int parameters of this command packet
	 * @return true if it has int parameters, false otherwise
	 */
	public boolean hasExtraIntParameters() {
		return intParams != null;
	}
	
	
	/**
	 * Query for the state of the String parameters of this command
	 * @return true if it has string parameters, false otherwise
	 */
	public boolean hasExtraStringParameters(){
		return stringParams != null;
	}

	
	/**
	 * Query for this command packet's main motive
	 * @return true if the main motive is to kill by ID
	 * false otherwise
	 */
	public boolean isKillByID() {
		return command == Constants.Commands.KILL;
	}

	
	/**
	 * Query for this command packet's main motive
	 * @return true if the main motive is to kill all processes
	 * false otherwise
	 */
	public boolean isKillAll() {
		return command == Constants.Commands.KILL_EVERYTHING;
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
		return packetEnd.equals(FULL_PACKET_IDENTIFIER);
	}
	
	
	/**
	 * Query for the priority state of this command
	 * @return true if this command is considered to
	 * be a high priority command, as defined in 
	 * {@link Constants#Commands}, false otherwise.
	 */
	public boolean hasHighPriority(){
		return command == Constants.Commands.KILL ||
				command == Constants.Commands.MODIFY ||
				command == Constants.Commands.KILL_EVERYTHING ||
				command == Constants.Commands.SUPPORTED_FEATURES;
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
							Constants.PACKET_DELIMITER, Constants.PACKET_END);

			if(!decodedPackets.isEmpty()){
				packets = new CommandPacket[decodedPackets.size()];
				for(int i = 0; i < packets.length; i++){
					ArrayList<String> sub = decodedPackets.get(i);
					if(!sub.isEmpty()){
						packets[i] = new CommandPacket(sub);
					} else {
						Log.e(TAG, "sub packet is empty");
					}
				}
			} else {
				Log.e(TAG, "packet list is empty");
			}
			
		} catch(NumberFormatException e){
			Log.d(TAG, "packets could not be parsed due to incorrect argument : " + buffer);
		}
		return packets;
	}
	


	
	
	/**
	 * This static method parses the commands given in a byte array that is
	 * retrieved from reading data on a {@link RemoteConnection} for this
	 * application. The data is parsed based on the command
	 * {@link Constants#TERMINATOR}, which indicates a command's end point.
	 * If at any point a {@link Constants#PACKET_END} is found, the command packet
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
	 * @return an ArrayList of ArrayLists of Integers. The latter ArrayList can be seen
	 * as an individual CommandPacket, while the former ArrayList is a container of packets.
	 */
	public static ArrayList<ArrayList<String>> decode(String buffer, char delimiter, String fullPacketIdentifier) {
		
		ArrayList<ArrayList<String>> packetList = new ArrayList<ArrayList<String>>();
		ArrayList<String> subPacket = new ArrayList<String>();
		int subPacketPointer = 0;
		
		for (int i = 0; i < buffer.length(); i++) {
			
			if (buffer.charAt(i) == Constants.PACKET_DELIMITER) {
				
				String temp = buffer.substring(subPacketPointer, i);
				
				if(temp.equals(Constants.PACKET_END)){
					subPacket.add(FULL_PACKET_IDENTIFIER);
					packetList.add(subPacket);
					subPacket = new ArrayList<String>();
				} else {
					subPacket.add(temp);
				}
				
				subPacketPointer = i + 1;
			}

		}
		
		return packetList;
	}


	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("task ID: ");
		builder.append(taskID);
		builder.append('\n');
		builder.append("command: ");
		builder.append(command);
		builder.append('\n');
		
		builder.append("int parameters: {");
		if(intParams != null){
			builder.append(intParams[0]);
			for(int i = 1; i < intParams.length; i++){
				builder.append(", ");
				builder.append(intParams[i]);
			}
		}
		builder.append("}\n");
		
		builder.append("string parameters: {");
		if(intParams != null){
			builder.append(stringParams[0]);
			for(int i = 1; i < stringParams.length; i++){
				builder.append(", ");
				builder.append(stringParams[i]);
			}
		}
		builder.append("}\n");
		
		builder.append("complete (un-partial) task: ");
		builder.append(isCompleteCommand());
		return builder.toString();
	}
}
