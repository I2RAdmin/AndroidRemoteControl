package com.i2r.androidremotecontroller.main;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.i2r.androidremotecontroller.R;
import com.i2r.androidremotecontroller.exceptions.ServiceNotFoundException;

/**
 * This class acts as a base starter for remote control of an android device.
 * This class starts the {@link RemoteControlMaster} for this application,
 * giving it the connection type chosen by the user in the
 * {@link ConnectionTypeSelectionActivity} to start receiving commands with.
 * This activity is started with an Intent that is sent from
 * {@link ConnectionTypeSelectionActivity}, and that intent will hold the string
 * connection type to start the master with.
 * 
 * @see {@link ConnectionTypeSelectionActivity#EXTRA_BLUETOOTH}
 * @see {@link ConnectionTypeSelectionActivity#EXTRA_WIFI}
 * @see {@link ConnectionTypeSelectionActivity#EXTRA_USB}
 */
public class RemoteControlActivity extends Activity implements OnClickListener {

	private static final String TAG = "RemoteControlActivity";
	
	private RemoteControlReceiver receiver;
	private IntentFilter filter;

	/**
	 * Create a new {@link BluetoothConnectionManager} to handle
	 * connection operations, get access to a TextView to update the UI of the
	 * application's progress, and create an ArrayList of {@link CommandPacket}s
	 * which are parsed from byte arrays received in the
	 * BluetoothConnectionManager. The CommandPAcket ArrayList will be used as a
	 * command Queue to execute command packets in the order that they come.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote_control);

		// button to stop remote control
		Button stop = (Button) findViewById(R.id.stop_remote_control);
		stop.setOnClickListener(this);
		
		// receiver cannot be created until user specifies connection type
		this.receiver = null;
		
		// add an IntentFilter to receive updates from manager
		this.filter = new IntentFilter();
		this.filter.addAction(RemoteControlReceiver.ACTION_TASK_COMPLETE);
		this.filter.addAction(RemoteControlReceiver.ACTION_CONNECTOR_RESPONDED);
		this.filter.addAction(RemoteControlReceiver.ACTION_CONNECTION_READ);

	}
	

	@Override
	protected void onResume() {
		super.onResume();
		
		String connectionType = getIntent().getStringExtra(
				ConnectionTypeSelectionActivity.EXTRA_CONNECTION_TYPE);
		
		try{
			receiver = new RemoteControlReceiver(this, connectionType);
			registerReceiver(receiver, filter);
			receiver.start();
		} catch(ServiceNotFoundException e){
			Log.e(TAG, e.getMessage());
			this.receiver = null;
			finish();
		}
		
	}
	

	@Override
	protected void onPause() {
		super.onPause();
		if(receiver.isStarted()){
			receiver.stop();
		}
		unregisterReceiver(receiver);
	}


	@Override
	public void onClick(View view) {
		Intent result = new Intent(ConnectionTypeSelectionActivity
				.ACTION_USER_TERMINATED_CONNECTION);
		setResult(RESULT_OK, result);
		receiver.stop();
		finish();
	}
	

} // end of SelectorActivity class
