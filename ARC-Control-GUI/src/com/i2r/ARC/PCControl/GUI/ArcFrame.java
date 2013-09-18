package com.i2r.ARC.PCControl.GUI;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * This class models the graphical interface for
 * this application. This interface will be updated
 * by the {@link ArcGuiController} as new information
 * from the {@Controller} becomes available.
 * @author Josh Noel
 */
public class ArcFrame extends JFrame {

	private static final long serialVersionUID = 2982094766360421621L;

	private JTabbedPane androidDevicePane;
	private ArrayList<ArcDeviceTab> devices;
	private ArcMenuBar menuBar;
	
	/**
	 * Constructor<br>
	 * creates a new standard {@link JFrame}
	 * which will be populated with android devices
	 * as the user establishes connections to them.
	 * @param controller - the {@link ArcGuiController}
	 * which this application will use to pass information
	 * to the {@link Controller} when the user wishes to
	 * send a new command or establish a new connection.
	 */
	public ArcFrame(){
		this.androidDevicePane = new JTabbedPane();
		this.devices = new ArrayList<ArcDeviceTab>();
		this.menuBar = new ArcMenuBar();
		this.setLayout(new BorderLayout());
		this.add(androidDevicePane, BorderLayout.CENTER);
		this.add(menuBar, BorderLayout.NORTH);
		this.setTitle("Android Remote Control");
		this.setLocationRelativeTo(null);
		this.setSize(800, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
	/**
	 * Used by this class's {@link ArcGuiController}
	 * to allow this GUI to respond to new information
	 * presented by the {@link Controller}
	 * @param response - the response to parse and
	 * update this GUI with.
	 */
	public void updateByResponse(String response){
		
		// TODO: integrate response
		
		// case 1: create new device
		
		// case 2: remove task that was deemed complete
		
		// case 3: add log statement

	}
	
	
	/**
	 * Repaints all {@link Component}s of
	 * this GUI.
	 */
	public void repaintAll(){
		androidDevicePane.repaint();
		menuBar.repaint();
		repaint();
	}
	
}
