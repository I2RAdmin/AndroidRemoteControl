package com.i2r.ARC.PCControl.GUI;
import java.util.List;

import javax.swing.JPanel;

import ARC.Constants;

import com.i2r.ARC.PCControl.DataType;
import com.i2r.ARC.PCControl.Limiter;


public class FeaturePanel extends JPanel {

	private static final long serialVersionUID = -7795045743140743307L;

	private DataType type;
	private Limiter limiter;
	private List<String> args;
	
	public FeaturePanel(String featureName, DataType type, Limiter limiter, List<String> args){
		setName(featureName);
		this.type = type;
		this.limiter = limiter;
		this.args = args;
		
		init();
	}
	
	public void init(){
		
		switch(limiter.getType().intValue()){
		
		case Constants.DataTypes.SET:
			createSet();
			break;
			
		case Constants.DataTypes.RANGE:
			createRange();
			break;
			
		case Constants.DataTypes.ANY:
			createTypeAny();
			break;
			
			default:
				System.err.println("init went to default case");
				break;
		
		}
	}
	
	
	
	private void createSet(){
		// TODO: finish this panel type
	}
	
	
	
	private void createRange(){
		// TODO: finish this panel type
	}
	
	
	private void createTypeAny(){
		// TODO: finish this panel type
	}
	
	
	
} // end of FeaturePanel class
