package com.i2r.androidremotecontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.i2r.androidremotecontroller.sensors.SensorController;

/**
 * This class acts as a base starter for remote control
 * of an android device. It links remote control by
 * means of the start and stop buttons defined in the UI.
 * Once remote control has been started, all interaction
 * is sought through an outside connection to a PC.
 * This class acts as an interpreter for all incoming
 * commands from the PC serving said commands.
 */
public class RemoteControlActivity extends Activity {

	
	public static final String TAG = "RemoteControlActivity";
	
	//public static final String ACTION_EXECUTE_COMMAND = "i2r_action_execute_command";
	public static final String ACTION_UPDATE_MASTER = "i2r_bluetooth_respond_intent";
	public static final String ACTION_CONNECTOR_RESPONDED = "i2r_action_connection_created";
	public static final String ACTION_CONNECTION_READ = "i2r_action_connection_read";
	public static final String EXTRA_CONNECTION_STATUS = "i2r_extra_connection_status";
	public static final String EXTRA_TASK_ID = "i2r_extra_task_id";
	public static final String EXTRA_COMMAND = "i2r_extra_command";
	public static final String EXTRA_REMOTE_CONNECTION = "i2r_parcelable_extra_remote_connection";
	private static final int BT_REQUEST_CODE = 1;

	private BroadcastReceiver receiver;
	private RemoteControlMaster master;
	private SensorController sensorController;
	private TextView action;
	private Camera camera;
	private boolean started;
	
	
	/**
	 * Create a new {@link BluetoothConnectionManager} to handle
	 * bluetooth connection operations, get access to a
	 * TextView to update the UI of the application's
	 * progress, and create an ArrayList of {@link CommandPacket}s
	 * which are parsed from byte arrays received in the
	 * BluetoothConnectionManager. The CommandPAcket ArrayList
	 * will be used as a command Queue to execute command
	 * packets in the order that they come.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// booleans to query about the state of this activity during the
		// first steps of creation
		this.started = false;
		
		// creates a new master to control remote command flow, and a new responder
		// to mediate between the master and the device sensors
		
		this.action = (TextView) findViewById(R.id.current_action_text_view);
		
		// create a receiver for the BluetoothConnectionManager to send to
		this.receiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent){
				
				// if the source of the intent was a remote connection reading more commands
				if(intent.getAction().equals(ACTION_CONNECTION_READ)){
					Log.d(TAG, "update broadcast from connection recieved");
					action.setText("pasring command");
					master.updateByRemoteControl(intent.getStringExtra(EXTRA_COMMAND));
					
					
					// if the source of the intent was a new connection
				} else if(intent.getAction().equals(ACTION_CONNECTOR_RESPONDED)){
					Log.d(TAG, "initialization broadcast recieved");
					boolean connectionFound = intent.getBooleanExtra(EXTRA_CONNECTION_STATUS, false);
					String result = connectionFound ? "" : 
						"connection terminated by remote device, listening for reconnect...";
					action.setText(result);
					master.initializeConnection();
					
					// sensor completed a task, do something about it
				} else if(intent.getAction().equals(ACTION_UPDATE_MASTER)){
					Log.d(TAG,"master update broadcast recieved from sensor");
					action.setText("task completed: " + intent.getIntExtra(EXTRA_TASK_ID, -1));
				}
			}
		};
		
		
		// button to stop remote control
		Button stop = (Button) findViewById(R.id.stop_remote_control);
		stop.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				stopMaster();
			}
		});
	}
	
	
	
	// default
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	protected void onResume(){
		super.onResume();
		
		// add an IntentFilter to receive updates from bluetooth manager
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UPDATE_MASTER);
		filter.addAction(ACTION_CONNECTOR_RESPONDED);
		filter.addAction(ACTION_CONNECTION_READ);
		registerReceiver(receiver, filter);
		

		
		BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
		if(a != null && a.isEnabled()){
			startMain();
		} else if(a != null && !a.isEnabled()){
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_REQUEST_CODE);
		} else {
			Toast.makeText(this, "no bluetooth found on this device", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
		
		// stop remote control and free resources
		unregisterReceiver(receiver);
		sensorController.setSurfaceHolderForCamera(null);
		master.stop();
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == BT_REQUEST_CODE){
			if(resultCode == RESULT_OK){
				startMain();
			} else {
				Toast.makeText(this, "bluetooth must be enabled for this app to work correctly", 
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	private void startMain(){
		Log.d(TAG, "Setting current SurfaceHolder to SensorController");
		this.camera = Camera.open();
		SurfaceView view = (SurfaceView) findViewById(R.id.preview);
		this.sensorController = new SensorController(this, camera, view.getHolder());
		this.master = new RemoteControlMaster(sensorController);
		startMaster();
	}
	
	
	
	private void startMaster(){
		// set the 'start' button to start remote control
		if(!started){
			started = true;
			action.setText("remote control started, listening for connection...");
			Log.d(TAG, "remote control started");
			master.start();
		}
	}
	
	
	private void stopMaster(){
		// set the 'stop' button to stop remote control
		started = false;
		Log.d(TAG, "remote control stopped");
		master.stop();
		action.setText("remote control stopped, finishing...");
		finish();
	}
	
}// end of SelectorActivity class
