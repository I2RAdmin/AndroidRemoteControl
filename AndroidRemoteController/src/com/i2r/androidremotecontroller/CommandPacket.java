package com.i2r.androidremotecontroller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ARC.Constants;
import ARC.Constants.Args;
import android.util.Log;


/**
 * This class models a container for the commands parsed
 * from this application's {@link BluetotohSocket} byte[] results.
 * A command cannot be created, but must be parsed with the
 * static method defined in this class:<br>
 * {@link #parsePacket(String)}<br>
 * @author Josh Noel
 */
public class CommandPacket {

	public static final int MAX_ALLOWED_SIZE = 10240;
	public static final String TAG = "CommandPacket";
	
	private static final int FULL_PACKET_IDENTIFIER = 9001;
	
	private int header, taskID, packetEnd;
	private int[] parameters;
	private ArrayList<Integer> rawCommandPacket;

	/**
	 * Constructor #3
	 * Assumes the given StringBuilder is already
	 * a full command packet, and decodes it
	 * @param packet - the StringBuilder object to decode
	 */
	private CommandPacket(ArrayList<Integer> packet) {
		
		this.rawCommandPacket = packet;
		this.taskID = packet.get(Constants.Commands.TASK_ID_INDEX);
		this.header = packet.get(Constants.Commands.HEADER_INDEX);
		this.packetEnd = packet.get(packet.size() - 1); // one over error
		this.parameters = null;

		int pointer = Constants.Commands.PARAM_START_INDEX;
		int paramLength = packet.size() - pointer - 1; // one over error

		if (paramLength > 0) {
			this.parameters = new int[paramLength];
			for (int i = 0; i < paramLength; i++) {
				parameters[i] = packet.get(pointer++);
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
		return packetEnd == FULL_PACKET_IDENTIFIER;
	}
	
	
	public ArrayList<Integer> getRawPacket(){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.addAll(rawCommandPacket);
		return temp;
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
			ArrayList<ArrayList<Integer>> decodedPackets = decode(buffer, 
							Constants.PACKET_DELIMITER, Constants.PACKET_END);

			if(!decodedPackets.isEmpty()){
				packets = new CommandPacket[decodedPackets.size()];
				for(int i = 0; i < packets.length; i++){
					ArrayList<Integer> sub = decodedPackets.get(i);
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
	 * Stitches two packets together assuming that at least one of these packets
	 * has a completion identifier at its end.
	 * @param first - the first CommandPacket to stitch
	 * @param second - the second CommandPacket to stitch
	 * @return a new command packet which is the composite of the two given, with
	 * the order correlated to which one has the completion identifier being on the
	 * second latter half of the newly formed packet. As a result, the "stitch" of these
	 * two packets will be where the partial one ends and the complete one begins.
	 */
	public static CommandPacket stitchPackets(CommandPacket first, CommandPacket second){
		CommandPacket packet = null;
		ArrayList<Integer> temp = new ArrayList<Integer>();
			if(!first.isCompleteCommand()){
				temp.addAll(first.rawCommandPacket);
				temp.addAll(second.rawCommandPacket);
				packet = new CommandPacket(temp);
			} else if (!second.isCompleteCommand()){
				temp.addAll(second.rawCommandPacket);
				temp.addAll(first.rawCommandPacket);
				packet = new CommandPacket(temp);
			}
		return packet;
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
	public static ArrayList<ArrayList<Integer>> decode(String buffer, char delimiter,
								String fullPacketIdentifier) throws NumberFormatException {
		
		ArrayList<ArrayList<Integer>> packetList = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> subPacket = new ArrayList<Integer>();
		int subPacketPointer = 0;
		
		for (int i = 0; i < buffer.length(); i++) {
			
			if (buffer.charAt(i) == Constants.PACKET_DELIMITER) {
				
				String temp = buffer.substring(subPacketPointer, i);
				
				if(temp.equals(Constants.PACKET_END)){
					subPacket.add(Integer.valueOf(FULL_PACKET_IDENTIFIER));
					packetList.add(subPacket);
					subPacket = new ArrayList<Integer>();
				} else {
					subPacket.add(Integer.parseInt(temp));
				}
				
				subPacketPointer = i + 1;
			}

		}
		
		return packetList;
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
