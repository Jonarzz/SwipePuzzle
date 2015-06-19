package com.jonasz.swipepuzzle.activities;

import com.jonasz.swipepuzzle.R;
import com.jonasz.swipepuzzle.shapes.*;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Vibrator;

public class StartActivity extends SuperClassActivity {
	
	static final int VIBRATE_LENGTH = 25;
	
	private Button newGameButton;
	private Button continueButton;
	
	Vibrator vibe;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				
		getScreenSize();
		setButtonProperties();
		createMainMenu();
	}
	
	private void createMainMenu() {
		newGameButton = (Button)findViewById(R.id.newGameButton);
		continueButton = (Button)findViewById(R.id.continueButton);

		int iterator = 1;
		createButton(newGameButton, iterator++);
		createButton(continueButton, iterator);
	}
	
	private void createButton(Button button, int iterator) {
		addDefaultButtonImage(button);
		
		button.setWidth(buttonWidth);
		button.setHeight(buttonHeight);
		
		button.setOnClickListener(new AddButtonClickListener(button));
		button.setOnTouchListener(new AddButtonTouchListener(button));
		
	}
	
	@SuppressWarnings({"deprecation"})
	@SuppressLint("NewApi")
	private void addDefaultButtonImage(Button button) {
		
		Bitmap originalBitmap = null;
		
		if (button.getId() == newGameButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.new_game_button);
		if (button.getId() == continueButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.continue_button);
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, buttonWidth, buttonHeight, true);
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
			button.setBackground(new BitmapDrawable(getResources(), scaledBitmap));
		else
			button.setBackgroundDrawable(new BitmapDrawable(scaledBitmap));
	}
	
	@SuppressWarnings({"deprecation"})
	@SuppressLint("NewApi")
	public void addClickedButtonImage(Button button) {
		Bitmap originalBitmap = null;
		
		if (button.getId() == newGameButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.new_game_button_clicked);
		if (button.getId() == continueButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.continue_button_clicked);
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, buttonWidth, buttonHeight, true);
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
			button.setBackground(new BitmapDrawable(getResources(), scaledBitmap));
		else
			button.setBackgroundDrawable(new BitmapDrawable(scaledBitmap));
	}

	private class AddButtonClickListener implements OnClickListener {
		private Button button;
		
		public AddButtonClickListener(Button button) {
			this.button = button;
		}

		public void onClick(View v) {
			if (button.getId() == newGameButton.getId())
				addNewGameButtonListener();
			if (button.getId() == continueButton.getId())
				addContinueButtonListener();
		}

		private void addNewGameButtonListener() {
			Intent intent = new Intent(StartActivity.this, NewGameActivity.class);
			
			Bundle bundle = new Bundle();
			bundle.putBoolean("startNewGame", true);
			bundle.putString("imageName", getImageNameFromSettings());
			intent.putExtras(bundle);

		    startActivity(intent);
		}

		private void addContinueButtonListener() {
			Intent intent = new Intent(StartActivity.this, NewGameActivity.class);
			
			Bundle bundle = new Bundle();
			bundle.putBoolean("startNewGame", false);
			bundle.putString("imageName", getImageNameFromSettings());
			intent.putExtras(bundle);
			
		    startActivity(intent);
		}
		
	}
	
	private String getImageNameFromSettings() {
		SharedPreferences settings = getSharedPreferences("Settings", 0);
		return settings.getString("imageName", "girl");
	}
	
	private class AddButtonTouchListener implements OnTouchListener {
		private Button button;
		
		public AddButtonTouchListener(Button button) {
			this.button = button;
		}
		
		public boolean onTouch(View view, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				addClickedButtonImage(button);
				vibe.vibrate(VIBRATE_LENGTH);
			}
            if(event.getAction() == MotionEvent.ACTION_UP) {
            	addDefaultButtonImage(button);
            	if(touchingInsideButton(event))
            		view.performClick();
            }
            
			return false;
		}
		
		private boolean touchingInsideButton(MotionEvent event) {
			Point touchingPoint = new Point((int)event.getX(), (int)event.getY());
			Rectangle buttonArea = new Rectangle((int)button.getX(), (int)button.getY(), buttonWidth, buttonHeight);
			
			if (buttonArea.contains(touchingPoint))
				return true;
			else
				return false;
		}

	}
}
