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
	ANY(8),
	SET(7),
	SIZE(11),
	RANGE(6);
	
	private static final Map<Integer, Limiter> lookup = new HashMap<Integer, Limiter>();
	
	static{
		for(Limiter l : EnumSet.allOf(Limiter.class)){
			lookup.put(l.getType(), l);
		}
	}
	
	private Integer type;
	
	private Limiter(Integer type){
		this.type = type;
	}
	
	public Integer getType(){
		return type;
	}
	
	public static Limiter get(Integer status){
		return lookup.get(status);
	}
}
