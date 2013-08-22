package com.i2r.androidremotecontroller.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class models a simple boot on load
 * implementation for this application.
 * @author Josh Noel
 *
 */
public class StartOnBoot extends BroadcastReceiver {

	private static final String BOOT_UP = "android.intent.action.BOOT_COMPLETED";
	public static final String EXTRA_BOOT_UP = "com.i2r.androidremotecontroller.main.extra_boot_up";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(BOOT_UP)){
			Intent i = new Intent(context, ConnectionTypeSelectionActivity.class);
			intent.putExtra(EXTRA_BOOT_UP, true);
			context.startActivity(i);
		}
	}

}
