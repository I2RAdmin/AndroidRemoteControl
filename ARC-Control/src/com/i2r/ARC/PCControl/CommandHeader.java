/**
 * 
 */
package com.i2r.ARC.PCControl;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Johnathan
 *
 */
public enum CommandHeader {
	DO_NOTHING(-2, "ping"),
	KILL_ALL(-1, "nuke"),
	KILL_TASK(-3, "kill"),
	MODIFY_SENSOR(-4, "modify"),
	TAKE_PICTURE(0, "picture"),
	RECORD_AUDIO(1, "record"),
	LISTEN_ENVIRONMENT(2, "sense"),
	GET_LOCATION(3, "locate"),
	GET_SENSOR_FEATURES(-5, "features"),
	LIST_DEVICE_SENSORS(-10, "sensors"),
	LIST_DEVICES(-11, "devices"),
	PAUSE(-12, "pause"),
	CONNECT(-13, "connect"),
	FREEZE(-14, "freeze"),
	HELP(-15, "help");
	
	private static final Map<Integer, CommandHeader> headerType = new HashMap<Integer, CommandHeader>();
	private static final Map<String, CommandHeader> headerAlias = new HashMap<String, CommandHeader>();
	
	static{
		for(CommandHeader h : EnumSet.allOf(CommandHeader.class)){
			headerType.put(h.getType(), h);
			headerAlias.put(h.getAlias(), h);
		}
	}
	
	private Integer type;
	private String alias;
	
	private CommandHeader(Integer type, String alias){
		this.type = type;
		this.alias = alias;
	}
	
	public Integer getType(){
		return type;
	}
	
	public String getAlias(){
		return alias;
	}
	
	public static CommandHeader get(Integer type) throws UnsupportedValueException{
		if(!headerType.containsKey(type)){
			throw new UnsupportedValueException(type + " is not a valid command header.");
		}
		
		return headerType.get(type);
	}
	
	public static CommandHeader get(String alias) throws UnsupportedValueException{
		if(!headerAlias.containsKey(alias)){
			throw new UnsupportedValueException(alias + " is not a valid command header.");
		}
		
		return headerAlias.get(alias);
	}
}
