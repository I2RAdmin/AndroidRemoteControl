package com.i2r.ARC.PCControl.GUI;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.i2r.ARC.PCControl.Controller;

public class DefaultTabPanel extends JPanel {

	private static final long serialVersionUID = -4941091941398752345L;
	
	public static final String BLUETOOTH = "Bluetooth";
	public static final String WIFI = "Wifi";
	public static final String USB = "USB";
	
	private static final String[] CONNECTION_TYPES = {
		BLUETOOTH, WIFI
	};
	
	private static final int CONNECTION_TYPE_COUNT = CONNECTION_TYPES.length;
	private static final int DEFAULT_COLUMN_COUNT = 1;
	
	private LinkedList<SearchGroup> groups;
	
	public DefaultTabPanel() {
		super();
		
		this.groups = new LinkedList<SearchGroup>();
		
		setLayout(new GridLayout(CONNECTION_TYPE_COUNT, DEFAULT_COLUMN_COUNT));
		
		for(String type : CONNECTION_TYPES){
			SearchGroup group = new SearchGroup(type);
			groups.add(group);
			this.add(group);
		}
	}
	
	
	public void repaintAll(){
		for(SearchGroup group : groups){
			group.repaintAll();
		}
	}
	
	
	public void updateSearcher(String update){
		// TODO: parse what kind of connection and device names
		// then add them to the appropriate search group.
		// also update JLabel for search group
	}
	
	
	public static String[][] getDetails(String deviceName){
		String[][] result = null;
		
		// TODO: send appropriate command to connect to device
		ArcGuiController.getInstance().sendCommand("");
		
		return result;
	}
	
	

	private class SearchGroup extends JPanel implements ActionListener {

		private static final long serialVersionUID = 3965250266917336887L;

		private static final String DEFAULT_STATE = "inactive";
		
		private JButton searchButton;
		private JLabel currentState;
		private JComboBox<String> resultDevices;
		
		public SearchGroup(String connectionType){
			super();
			
			this.searchButton = new JButton(connectionType);
			this.currentState = new JLabel(DEFAULT_STATE);
			this.resultDevices = new JComboBox<String>();
			
			this.searchButton.addActionListener(this);
			this.resultDevices.addActionListener(this);
			
			this.setName(connectionType);
			this.setLayout(new GridLayout(1,4));
			this.add(searchButton);
			this.add(currentState);
			this.add(resultDevices);
		}
		
		
		public void repaintAll(){
			searchButton.repaint();
			currentState.repaint();
			resultDevices.repaint();
			repaint();
		}

		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if(source instanceof JComboBox){
				JComboBox box = (JComboBox) source;
				box.getSelectedIndex(); // TODO: use this to acquire device details
			}
		}
			
	}


}
