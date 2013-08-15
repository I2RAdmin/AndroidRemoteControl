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
public enum Limiter {
	ANY(8, "any"),
	SET(7, "set"),
	SIZE(12, "size"),
	RANGE(6, "range"),
	CONST(9, "constant");
	
	private static final Map<Integer, Limiter> limiterType = new HashMap<Integer, Limiter>();
	private static final Map<String, Limiter> limiterAlias = new HashMap<String, Limiter>();
	static{
		for(Limiter l : EnumSet.allOf(Limiter.class)){
			limiterType.put(l.getType(), l);
		}
		
		for(Limiter l : EnumSet.allOf(Limiter.class)){
			limiterAlias.put(l.getAlias(), l);
		}
	}
	
	private Integer type;
	private String alias;
	
	private Limiter(Integer type, String alias){
		this.type = type;
		this.alias = alias;
	}
	
	public Integer getType(){
		return type;
	}
	
	public String getAlias(){
		return alias;
	}
	
	public static Limiter get(Integer type){
		return limiterType.get(type);
	}
	
	public static Limiter get(String alias){
		return limiterAlias.get(alias);
	}
}
