package com.i2r.ARC.PCControl.GUI;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import ARC.Constants;

import com.i2r.ARC.PCControl.DataType;
import com.i2r.ARC.PCControl.Limiter;


public class FeaturePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -7795045743140743307L;

	private DataType type;
	private Limiter limiter;
	private List<String> args;
	private JTextField text;
	private int currentValueIndex;
	private double sliderValue;
	
	
	public FeaturePanel(String featureName, DataType type,
			Limiter limiter, List<String> args, int currentValueIndex){
		
		this.setName(featureName);
		this.type = type;
		this.limiter = limiter;
		this.args = args;
		this.currentValueIndex = currentValueIndex;
		this.text = null;
		this.sliderValue = Constants.Args.ARG_DOUBLE_NONE;
		
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
				this.setName(featureName + " errored on creation");
				break;
		
		}
		
	}
	
	
	public DataType getDataType(){
		return type;
	}
	
	
	public Limiter getLimiter(){
		return limiter;
	}
	
	
	public String getCurrentValue(){
		String set = getCurrentSetValue();
		return set != null ? set :
				sliderValue != Constants.Args.ARG_DOUBLE_NONE
				? Double.toString(sliderValue) :
				getEnteredText();
	}
	
	
	public String getEnteredText(){
		return text != null ? text.getText() : null;
	}
	
	
	public String getCurrentSetValue(){
		return limiter.getType().intValue() == Constants.DataTypes.SET
				? args.get(currentValueIndex) : null;
	}
	
	
	public double getCurrentRangeValue(){
		return sliderValue;
	}
	
	
	private void createSet(){
		setLayout(new GridLayout(args.size() + 1, 1));
		add(new JLabel(getName()));
		
		for(String arg : args){
			JRadioButton button = new JRadioButton(arg);
			button.setActionCommand(arg);
			add(button);
		}
	}
	
	
	
	private void createRange(){
		setLayout(new GridLayout(2,1));
		add(new JLabel(getName()));
		
	}
	
	
	private void createTypeAny(){
		setLayout(new GridLayout(2,1));
		text = new JTextField();
		add(new JLabel(getName()));
		add(text);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		
		try{
			sliderValue = Double.parseDouble(e.getActionCommand());
		} catch (NumberFormatException exeption){
			sliderValue = Constants.Args.ARG_DOUBLE_NONE;
			int value = args.indexOf(e.getActionCommand());
			if(value > -1){
				currentValueIndex = value;
			}
		}
	}
	
	
} // end of FeaturePanel class
