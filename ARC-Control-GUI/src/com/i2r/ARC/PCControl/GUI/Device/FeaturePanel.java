package com.i2r.ARC.PCControl.GUI.Device;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ARC.Constants;

import com.i2r.ARC.PCControl.DataType;
import com.i2r.ARC.PCControl.Limiter;
import com.i2r.ARC.PCControl.UnsupportedValueException;
import com.i2r.ARC.PCControl.GUI.ArcGuiController;

/**
 * This class models a single feature in a given Sensor's list
 * of available features. A list of these panels will be displayed
 * for every sensor tab in the features panel of the
 * corresponding {@link ArcDeviceTab}.
 * @author Josh Noel
 *
 */
public class FeaturePanel extends JPanel implements ActionListener, ChangeListener {

	private static final long serialVersionUID = -7795045743140743307L;

	public static final int PARAMETER_LENGTH = 5;
	
	private static final int NAME_INDEX = 0;
	private static final int TYPE_INDEX = 1;
	private static final int LIMITER_INDEX = 2;
	private static final int PARAM_START_INDEX = 3;
	
	private DataType type;
	private Limiter limiter;
	private List<String> args;
	private JTextField text;
	private int currentValueIndex, sliderValue;
	
	
	/**
	 * Constructor<br>
	 * @param featureName - the name to represent this feature
	 * on the {@link ArcDeviceTab}
	 * @param type - the type of data this feature
	 * contains (see {@link Limiter}s)
	 * @param limiter - the listing type of this feature
	 * (see {@link DataType}s)
	 * @param args - the argument list for this feature; this
	 * can be a set to choose from,<br> or a range defining min
	 * and max values
	 */
	public FeaturePanel(String featureName, DataType type,
			Limiter limiter, List<String> args) throws UnsupportedValueException {
		
		if(featureName == null || type == null || limiter == null || args == null){
			throw new UnsupportedValueException("no parameters in FeaturePanel constructor can be null");
		}
		
		this.setName(featureName);
		this.type = type;
		this.limiter = limiter;
		this.args = args;
		this.currentValueIndex = Constants.Args.ARG_NONE;
		this.sliderValue = Constants.Args.ARG_NONE;
		this.text = null;
	
		initializePanel();
	}
	
	
	
	public FeaturePanel(String details) throws UnsupportedValueException {
		
		String[] info = details.split("\n");
		setName(info[NAME_INDEX]);
		this.type = DataType.get(info[TYPE_INDEX]);
		this.limiter = Limiter.get(info[LIMITER_INDEX]);
		
		this.args = new ArrayList<String>(info.length - PARAM_START_INDEX);
		for(int i = PARAM_START_INDEX; i < info.length; i++){
			args.add(info[i]);
		}
		
		initializePanel();
	}
	
	
	
	public void initializePanel(){
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
				this.setName(getName() + " errored on creation");
				break;
		
		}
	}
	
	
	
	public void setCurrentValue(String value){
		int index = args.indexOf(value);
		if(index >= 0){
			this.currentValueIndex = index;
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
			button.addActionListener(this);
			add(button);
		}
	}
	
	
	
	private void createRange(){
		setLayout(new GridLayout(2,1));
		add(new JLabel(getName()));
		int min = Integer.parseInt(args.get(0));
		int max = Integer.parseInt(args.get(1));
		JSlider range = new JSlider(JSlider.HORIZONTAL, min, max, (min + max) / 2);
		range.addChangeListener(this);
		add(range);
	}
	
	
	private void createTypeAny(){
		setLayout(new GridLayout(2,1));
		text = new JTextField();
		add(new JLabel(getName()));
		add(text);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		ArcGuiController.getInstance().sendCommand(e.getActionCommand());
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		sliderValue = source.getValue();
	}
	
	
	
} // end of FeaturePanel class
