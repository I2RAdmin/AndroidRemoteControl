package com.i2r.ARC.PCControl.GUI;

import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.i2r.ARC.PCControl.UnsupportedValueException;

/**
 * This class models the graphical representation of
 * an android device sensor.
 * @author Josh Noel
 */
public class GUISensor extends JPanel {

	public static final int NAME_INDEX = 0;
	
	private static final long serialVersionUID = -2936712671874995001L;
	
	private LinkedList<FeaturePanel> features;
	
	/**
	 * Constructor<br>
	 * Creates this sensor's {@link JPanel} representation
	 * for this GUI.
	 * @param rawFeatures - the features defining this sensor - the name
	 * of this sensor should always be first in the given array.
	 * @throws UnsupportedValueException if any of the values given do not
	 * comply with the rules for generating a sensor panel.
	 * @see {@link FeaturePanel}
	 */
	public GUISensor(String[] rawFeatures) throws UnsupportedValueException {
		
		this.setName(rawFeatures[NAME_INDEX]);
		this.features = new LinkedList<FeaturePanel>();
		this.setLayout(new GridLayout((rawFeatures.length - 1) * 2, 1));
		
		for(int i = NAME_INDEX + 1; i < rawFeatures.length; i++){
			features.add(new FeaturePanel(rawFeatures[i]));
		}
		
		for(FeaturePanel f : features){
			this.add(f);
			this.add(new JSeparator());
		}
	}
	
	/**
	 * Query for this sensor's manipulatable features.
	 * @return a {@link List} of this sensors
	 * available features.
	 * @see {@link FeaturePanel}
	 */
	public List<FeaturePanel> getFeatureList(){
		return features;
	}
	
}
