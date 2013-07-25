package com.i2r.androidremotecontroller;

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

public class ConnectionTypeSelectionActivity extends Activity {
	
	public static final String EXTRA_CONNECTION_TYPE = "i2r_extra_connection_type";
	public static final String EXTRA_WIFI = "Wifi";
	public static final String EXTRA_BLUETOOTH = "Bluetooth";
	public static final String EXTRA_USB = "USB";
	
	private static final String TAG = "ConnectionActivity";
	private static final String EXIT = "Exit";
	
	private static final String[] CONNECTION_TYPES = {
		EXTRA_WIFI, EXTRA_BLUETOOTH, EXTRA_USB, EXIT
	};
	
	private static final int BT_ENABLE_CODE = 1;

	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection_select);
		
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
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == BT_ENABLE_CODE){
			if(resultCode == RESULT_OK){
				setupBluetooth();
			} else {
				Toast.makeText(this, "bluetooth must be enabled for this app to work correctly", 
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	
	private void ensureSelection(final String selection){
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        		
		        	if(selection.equals(EXTRA_WIFI)){
		        		setupWifi();
		        	} else if(selection.equals(EXTRA_BLUETOOTH)){
		        		setupBluetooth();
		        	} else if(selection.equals(EXTRA_USB)){
		        		setupUsb();
		        	} else if(selection.equals(EXIT)){
		        		inform("exiting remote control app...");
		        		finish();
		        	}
		        	
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            // do nothing
		            break;
		        }
		    }
		};
		
		String message = selection.equals(EXIT) ? "Are you sure you want to exit the application?" :
			"Are you sure you would like to start using remote control with a "
			+ selection + " connection type?";
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setPositiveButton("OK", dialogClickListener)
		    .setNegativeButton("Cancel", dialogClickListener).show();
	}
	
	
	
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
            startActivity(intent);
			
		} else if(adapter != null && !adapter.isEnabled()){
			
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_ENABLE_CODE);
			
		} else {
			
			inform("no bluetooth found on this device");
			finish();
		}
	}
	
	
	private void setupUsb(){
		Log.i(TAG, "setup USB called");
		inform("USB connection is not currently supported for this app");
	}
	
	
	private void setupWifi(){
		Log.i(TAG, "setup wifi called");
		inform("wifi remote connection is not currently supported for this app");
	}
	
	
	private void inform(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
