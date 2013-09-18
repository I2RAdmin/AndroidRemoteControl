package com.i2r.ARC.PCControl.GUI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.i2r.ARC.PCControl.Controller;
import com.i2r.ARC.PCControl.UI.StreamUI;

/**
 * This class models an intermediary between the I/O
 * streams of the ARC-Control system and the graphical
 * user interface of this application.
 * @author Josh Noel
 */
public class ARCControlLink {

	public static final String MODIFY = "modify";
	
	
	private Controller controller;
	private ARC_GUI_Controller guiCont;
	private PipedInputStream guiIn, controlIn;
	private PipedOutputStream guiOut, controlOut;
	private StreamUI<OutputStream, InputStream, String> ui;
	private ReadThread reader;
	
	/**
	 * Constructor<br>
	 * Obtains an instance of the ARC-Control system's
	 * {@link Controller} object and establishes
	 * a stream connection to it.
	 * @throws IOException if streams to the controller
	 * could not be properly established.
	 */
	public ARCControlLink(ARC_GUI_Controller guiCont, Controller controller) throws IOException {
		this.guiCont = guiCont;
		this.controller = controller;
		resetStreams();
	}
	
	
	/**
	 * Resets the streams to this control link's
	 * {@link Controller} reference.
	 * @throws IOException if streams to the controller
	 * could not be properly established.
	 */
	public synchronized void resetStreams() throws IOException {
		this.controlIn = new PipedInputStream();
		this.controlOut = new PipedOutputStream();
		
		this.guiIn = new PipedInputStream(controlOut);
		this.guiOut = new PipedOutputStream(controlIn);
		
		this.ui = new StreamUI<OutputStream, InputStream, String>
		                   (controlIn, controlOut, controller);
		
		// TODO: set newly created ui stream to controller
	}
	
	
	/**
	 * Starts the read loop for this link.
	 * All received messages will be handed
	 * to the {@link ARC_GUI_Controller}
	 */
	public synchronized void start(){
         reader = new ReadThread(guiIn);
         reader.start();
	}
	
	
	/**
	 * Stops all communication with this
	 * class's {@link Controller} object.
	 * In order to re-establish a connection,
	 * {@link #resetStreams()} and {@link #start()}
	 * must be called respectively.
	 */
	public synchronized void stop(){
		
		// close read thread
        if(reader != null){
       	 reader.cancel();
       	 reader = null;
        }
		
        // close ui
		if(ui != null){
			ui.close();
			ui = null;
		}
		
		// closing piped streams that link to ui
		if( controlIn != null){
			try {controlIn.close();}
			catch (Exception e) {}
			controlIn = null;
		}
		
		// closing piped streams that link to ui
		if(controlOut != null){
			try {controlOut.flush(); controlOut.close();}
			catch (Exception e) {}
			controlOut = null;
		}
		
		// closing piped streams that link to ui
		if(guiIn != null){
			try {guiIn.close();}
			catch (Exception e) {}
			guiIn = null;
		}
		
		// closing piped streams that link to ui
		if(guiOut != null){
			try {guiOut.flush(); guiOut.close();}
			catch (Exception e) {}
			guiOut = null;
		}
	}
	
	
	/**
	 * Sends a command across this link to this
	 * GUI's {@link Controller}
	 * @param command - the command to send to the controller
	 */
	public synchronized void sendCommand(String command){
		if(command != null){
			try{
				guiOut.write(command.getBytes());
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Sets this link's {@link Controller} reference to
	 * the one given. When this is called, {@link #resetStreams()}
	 * and {@link #start()} should be called immediately afterwards
	 * so that the streams may remain valid.
	 * @param controller - the controller to obtain a reference from
	 */
	public void setController(Controller controller){
		this.controller = controller;
	}
	
	
	/**
	 * Query for this link's controller reference
	 * @return the {@link Controller} this link
	 * currently has a reference to.
	 */
	public Controller getController(){
		return controller;
	}
	
	
	/**
	 * This class models a read thread for
	 * this link, and alerts the
	 * {@link ARC_GUI_Controller} when the
	 * {@link Controller} has new information
	 * to present to the user.
	 * @author Josh Noel
	 */
	private class ReadThread extends Thread {
		
		private InputStream input;
		private boolean running;
		
		public ReadThread(InputStream input){
			this.input = input;
			this.running = false;
		}
		
		@Override
		public void run(){
			running = true;
			byte[] buffer = new byte[1024];
			while(running){
				try {
					int numRead = input.read(buffer);
					if(numRead > 0){
						guiCont.onResponse(new String(buffer).substring(0, numRead));
					}
				} catch (IOException e) {
					e.printStackTrace();
					cancel();
				}
			}
		}
		
		public void cancel(){
			this.running = false;
		}
		
	}
	
}
