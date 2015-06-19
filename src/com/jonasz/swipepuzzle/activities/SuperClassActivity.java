package com.jonasz.swipepuzzle.activities;

import com.jonasz.swipepuzzle.activities.SettingsActivity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

public class SuperClassActivity extends Activity {
	
	protected int screenWidth;
	protected int screenHeight;
	
	protected int buttonWidth;
	protected int buttonHeight;
	
	protected Toast toast;
	
	protected void getScreenSize() { 
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
	}
	
	protected void setButtonProperties() {
		buttonWidth = screenWidth / 2;
		buttonHeight = screenHeight / 7;
	}
	
	public boolean onKeyDown(int keycode, KeyEvent e) {
	    switch(keycode) {
	        case KeyEvent.KEYCODE_MENU:
	        	startSettingsActivity();
	            return true;
	    }
	    
	    return super.onKeyDown(keycode, e);
	}

	protected void startSettingsActivity() {
		Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);		
	    startActivity(intent);
	}
	
	protected void showToast(String toastMessage) {
		try {
			toast.getView().isShown();
			toast.setText(toastMessage);
		} catch (Exception e) {
			toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
		} finally {
			TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
			if (v != null)
				v.setGravity(Gravity.CENTER);
			
			toast.show();
		}
	}
}
