package com.i2r.androidremotecontroller.connections;

import com.i2r.androidremotecontroller.main.RemoteControlActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * This class models a USB connection to another device
 * where this device acts as the host and powers the bus.
 * @author Josh Noel
 */
public class UsbHostConnection implements RemoteConnection {

	private static final String TAG = "UsbHostConnection";
	private static final int BUFFER_SIZE = 1024;
	private static final int TIME_OUT = 5000;
	
	private UsbDeviceConnection connection;
	private UsbInterface usbInterface;
	private UsbEndpoint in, out;
	private LocalBroadcastManager manager;
	private byte[] buffer;
	private boolean running;
	
	/**
	 * Constructor<br>
	 * creates a new Usb connection to a remote device by
	 * claiming one of its {@link UsbInterface}s and two
	 * {@link UsbEndpoint}s (one facing in and one facing out)
	 * that are from that interface. These endpoints are what
	 * will be used to transfer data.
	 * @param context - the context in which this connection was created
	 * @param usbInterface - the interface to claim for data transfer
	 * @param connection - a connection object used to send data on endpoints
	 * @param in - the inward facing endpoint used to receive data with; must
	 * belong to the given interface
	 * @param out - the outward facing endpoint used to send data with; must
	 * belong to the given interface
	 */
	public UsbHostConnection(Context context, UsbInterface usbInterface,
							UsbDeviceConnection connection, UsbEndpoint in, UsbEndpoint out){
		this.manager = LocalBroadcastManager.getInstance(context);
		this.usbInterface = usbInterface;
		this.connection = connection;
		this.in = in;
		this.out = out;
		this.buffer = new byte[BUFFER_SIZE];
	}
	
	
	
	@Override
	public void run() {
		this.running = true;
		while(running){
			int result = connection.bulkTransfer(in, buffer, BUFFER_SIZE, TIME_OUT);
			if(result > 0){
				String command = new String(buffer).substring(0, result);
				Intent intent = new Intent(RemoteControlActivity.ACTION_CONNECTION_READ);
				intent.putExtra(RemoteControlActivity.EXTRA_COMMAND, command);
				manager.sendBroadcast(intent);
			} else {
				Log.e(TAG, "error reading from bus");
			}
		}
	}

	
	@Override
	public void write(byte[] bytes) {
		int result = connection.bulkTransfer(out, bytes, bytes.length, TIME_OUT);
		Log.d(TAG, result > 0 ? "bytes successfully written to bus: " + result 
				: "bytes unable to be written to bus: " + result);
	}

	
	@Override
	public boolean isConnected() {
		return running;
	}

	
	@Override
	public void disconnect() {
		running = false;
		Log.d(TAG, "disconnecting USB host");
		connection.releaseInterface(usbInterface);
		connection.close();
	}
	
}
