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
	INTEGER(3), 
	DOUBLE(4),  
	FILE(9),
	STRING(5),
	STREAM(10);
	
	private static final Map<Integer, DataType> lookup = new HashMap<Integer, DataType>();
	private static final Map<DataType, String> readableName = new HashMap<DataType, String>();
	
	static{
		for(DataType t : EnumSet.allOf(DataType.class)){
			lookup.put(t.getType(), t);
		}
	}
	
	private Integer type;
	
	private DataType(Integer type){
		this.type = type;
	}
	
	public Integer getType(){
		return type;
	}
	
	public static DataType get(Integer status){
		return lookup.get(status);
	}
	
}