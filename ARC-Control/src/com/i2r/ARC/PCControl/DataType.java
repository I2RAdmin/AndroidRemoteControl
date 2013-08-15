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
public enum DataType {
	INTEGER(3, "integer"), 
	DOUBLE(4, "double"),  
	FILE(10, "file"),
	STRING(5, "string"),
	STREAM(11, "stream"),
	ANY(8, "any");
	
	private static final Map<Integer, DataType> dataType = new HashMap<Integer, DataType>();
	private static final Map<String, DataType> dataAlias = new HashMap<String, DataType>();
	
	static{
		for(DataType t : EnumSet.allOf(DataType.class)){
			dataType.put(t.getType(), t);
		}
		
		for(DataType t : EnumSet.allOf(DataType.class)){
			dataAlias.put(t.getAlias(), t);
		}
	}
	
	private Integer type;
	private String alias;
	
	private DataType(Integer type, String alias){
		this.type = type;
		this.alias = alias;
	}
	
	public Integer getType(){
		return type;
	}
	
	public String getAlias(){
		return alias;
	}
	
	public static DataType get(Integer type){
		return dataType.get(type);
	}
	
	public static DataType get(String alias){
		return dataAlias.get(alias);
	}
	
}