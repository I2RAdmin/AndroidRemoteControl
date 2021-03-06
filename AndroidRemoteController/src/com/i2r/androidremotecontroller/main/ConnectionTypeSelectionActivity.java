package com.i2r.androidremotecontroller.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.i2r.androidremotecontroller.R;


/*********************************************************
 * -------- MAIN ENTRY POINT OF THIS APPLICATION ---------
 *********************************************************
 * This class models an activity where the user can
 * select what type of connection they would like to
 * use (i.e., bluetooth, usb, wifi, etc) to communicate
 * with the controlling PC for this application. Once
 * a connection type has been confirmed by the user,
 * control of this application is passed over to
 * the {@link RemoteControlActivity} class.
 * 
 * @author Josh Noel
 *********************************************************
 */
public class ConnectionTypeSelectionActivity extends Activity implements DialogInterface.OnClickListener {
	
	/**
	 * Used to specify in an Intent that the result
	 * of this extra will be one of the following:<br>
	 * {@link #EXTRA_BLUETOOTH}, {@link #EXTRA_USB},
	 * {@link #EXTRA_WIFI}
	 */
	public static final String EXTRA_CONNECTION_TYPE = "i2r_extra_connection_type";
	
	/**
	 * Used to pass as an argument for {@link #EXTRA_CONNECTION_TYPE}
	 * in the {@link Intent} that starts this application's
	 * {@link RemoteControlActivity}
	 */
	public static final String EXTRA_WIFI = "Wifi";
	
	/**
	 * Used to pass as an argument for {@link #EXTRA_CONNECTION_TYPE}
	 * in the {@link Intent} that starts this application's
	 * {@link RemoteControlActivity}
	 */
	public static final String EXTRA_BLUETOOTH = "Bluetooth";
	
	/**
	 * Used to pass as an argument for {@link #EXTRA_CONNECTION_TYPE}
	 * in the {@link Intent} that starts this application's
	 * {@link RemoteControlActivity}
	 */
	public static final String EXTRA_USB = "USB";
	
	
	public static final String ACTION_USER_TERMINATED_CONNECTION = "i2r_action_user_terminated_connection";
	
	
	private static final String TAG = "ConnectionActivity";
	private static final String EXIT = "Exit";
	
	/**
	 * A listing of all possible connection types for this
	 * application
	 * @see {@link #EXTRA_BLUETOOTH}
	 * @see {@link #EXTRA_USB}
	 * @see {@link #EXTRA_WIFI}
	 */
	public static final String[] CONNECTION_TYPES = {
		EXTRA_WIFI, EXTRA_BLUETOOTH, EXTRA_USB, EXIT
	};
	
	private static final int BLUETOOTH_CODE = 1;
	
	private static final String KEY_SELECTION = "connection-selection";
	private static final String NO_SELECTION = "[N/A]";

	private String selection;
	private AlertDialog.Builder builder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection_select);
		
		this.selection = NO_SELECTION;
		this.builder = new AlertDialog.Builder(this);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_expandable_list_item_1, CONNECTION_TYPES);
		
		ListView list = (ListView) findViewById(R.id.connection_list_view);
		
        TextView tv = new TextView(getApplicationContext());
        tv.setText("Select a connection type to start remote control:");

        list.addHeaderView(tv);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			  public void onItemClick(AdapterView<?> parent, View view,
			    int position, long id) {
				  if(position > 0){
					  ensureSelection(CONNECTION_TYPES[position - 1]);
				  }
			  }
			});
		
	}
	
	
	@Override
	protected void onResume(){
		super.onResume();
		if(selection != null && !selection.equals(NO_SELECTION)){
			executeSelection(selection);
		}
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putString(KEY_SELECTION, selection);
	}
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		selection = savedInstanceState.getString(KEY_SELECTION);
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
		case BLUETOOTH_CODE:
			
			if(resultCode == RESULT_OK){
				setupBluetooth();
			} else {
				inform("bluetooth must be enabled for this app to work correctly");
			}
			
			break;
		}
		
		if(data != null && data.getAction().equals(ACTION_USER_TERMINATED_CONNECTION)){
			selection = NO_SELECTION;
		}
	}
	
	
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
        case DialogInterface.BUTTON_POSITIVE:
        	executeSelection(selection);
            break;

        case DialogInterface.BUTTON_NEGATIVE:
            // do nothing
            break;
        }
    }
    
    
    // restoration helper method
    private void executeSelection(String selection){
    	if(selection.equals(EXTRA_WIFI)){
    		setupWifi();
    	} else if(selection.equals(EXTRA_BLUETOOTH)){
    		setupBluetooth();
    	} else if(selection.equals(EXTRA_USB)){
    		setupUsb();
    	} else if(selection.equals(EXIT)){
    		finish();
    	}
    }
	
	
	/**
	 * Ensures that the option the user selected from
	 * the given list is the connection type that they
	 * would like to start the application with, using
	 * a Dialog Box with "OK" and "Cancel" options.
	 * @param selection - the connection type selection
	 * to confirm for use.
	 * @see {@link #EXTRA_BLUETOOTH}
	 * @see {@link #EXTRA_USB}
	 * @see {@link #EXTRA_WIFI}
	 * @see {@link #EXTRA_CONNECTION_TYPE}
	 */
	private void ensureSelection(String selection){
		
		this.selection = selection;
		
		String message = selection.equals(EXIT) ? 
			"Are you sure you want to exit the application?" :
			"Are you sure you would like to start using remote control with a "
			+ selection + " connection type?";
		
		builder.setMessage(message).setPositiveButton("OK", this)
		    .setNegativeButton("Cancel", this).show();
	}
	
	
	/**
	 * Prepares bluetooth utilities for this application's use.
	 * Called when the user selects the "bluetooth" option for this
	 * activity, and confirms that they would like to start remote
	 * control with this connection type.
	 */
	private void setupBluetooth(){
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if(adapter != null && adapter.isEnabled()){
			
			Log.d(TAG, "bluetooth enabled, starting discovery");
			if(adapter.isDiscovering()){
				adapter.cancelDiscovery();
			}
			
			adapter.startDiscovery();

	        Intent intent = new Intent(this, RemoteControlActivity.class);
	        intent.putExtra(EXTRA_CONNECTION_TYPE, EXTRA_BLUETOOTH);
	        startActivityForResult(intent, 0);
			
		} else if(adapter != null && !adapter.isEnabled()){
			
			startActivityForResult(new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE),BLUETOOTH_CODE);
			
		} else {
			inform("no bluetooth found on this device");
		}
	}
	
	
	/**
	 * Prepares USB utilities for this application's use.
	 * Called when the user selects the "USB" option for this
	 * activity, and confirms that they would like to start remote
	 * control with this connection type.
	 */
	private void setupUsb(){
//		Log.d(TAG, "starting remote control with usb");
//	    Intent intent = new Intent(this, RemoteControlActivity.class);
//	    intent.putExtra(EXTRA_CONNECTION_TYPE, EXTRA_USB);
//	    startActivity(intent);
		
		inform("USB control currently not supported for this app");
	}
	
	
	/**
	 * Prepares wifi utilities for this application's use.
	 * Called when the user selects the "wifi" option for this
	 * activity, and confirms that they would like to start remote
	 * control with this connection type.
	 */
	private void setupWifi(){
		Log.d(TAG, "starting remote control with wifi");
	    Intent intent = new Intent(this, RemoteControlActivity.class);
	    intent.putExtra(EXTRA_CONNECTION_TYPE, EXTRA_WIFI);
	    startActivityForResult(intent, 0);
	}
	
	
	
	/**
	 * Helper method for informing the user about the
	 * state of this application.
	 * @param message - the message to give to the user
	 */
	private void inform(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	
}
