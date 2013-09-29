package com.i2r.androidremotecontroller.main.databouncer;

import android.app.Activity;
import android.os.Bundle;

/**
 * This class models an activity where the user
 * can specify where an android device should
 * bounce data to.
 * @author Josh Noel
 */
public class DataBouncerActivity extends Activity {

	private DataBouncer bouncer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		this.bouncer = DataBouncer.getInstance();
		
		
		// TODO: make updatable list view for adding nodes
		// to data bouncing array
	}
	
	
	@Override
	protected void onResume(){
		super.onResume();
		
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
		
	}
	
}
