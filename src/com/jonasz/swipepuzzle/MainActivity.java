package com.jonasz.swipepuzzle;

import com.jonasz.swipepuzzle.activities.*;
import com.jonasz.swipepuzzle.shapes.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
// TODO AppCompat niedojebany
public class MainActivity extends SuperClassActivity {
	
	static final int VIBRATE_LENGTH = 25;
	
	private Button startButton;
	private Button scoresButton;
	private Button settingsButton;
	private Button exitButton;
	
	Vibrator vibe;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		getScreenSize();
		setButtonProperties();
		createMainMenu();
	}
	
	private void createMainMenu() {
		startButton = (Button)findViewById(R.id.startButton);
		scoresButton = (Button)findViewById(R.id.scoresButton);
		settingsButton = (Button)findViewById(R.id.settingsButton);
		exitButton = (Button)findViewById(R.id.exitButton);

		int iterator = 1;
		
		createButton(startButton, iterator++);
		createButton(scoresButton, iterator++);
		createButton(settingsButton, iterator++);
		createButton(exitButton, iterator);
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
		
		if (button.getId() == startButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.start_button);
		if (button.getId() == scoresButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scores_button);
		if (button.getId() == settingsButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.settings_button);
		if (button.getId() == exitButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exit_button);
		
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
		
		if (button.getId() == startButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.start_button_clicked);
		if (button.getId() == scoresButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scores_button_clicked);
		if (button.getId() == settingsButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.settings_button_clicked);
		if (button.getId() == exitButton.getId())
			originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exit_button_clicked);
		
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
			if (button.getId() == startButton.getId())
				addStartButtonListener();
			if (button.getId() == scoresButton.getId())
				addScoresButtonListener();
			if (button.getId() == settingsButton.getId())
				addSettingsButtonListener();
			if (button.getId() == exitButton.getId())
				addExitButtonListener();
		}

		private void addStartButtonListener() {
			Intent intent = new Intent(MainActivity.this, StartActivity.class);			
		    startActivity(intent);
		}

		private void addScoresButtonListener() {
			Intent intent = new Intent(MainActivity.this, ScoresActivity.class);
		    startActivity(intent);
		}
		
		private void addSettingsButtonListener() {
			startSettingsActivity();
		}

		private void addExitButtonListener() {
			finish();
            System.exit(0);
		}
		
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