package com.jonasz.swipepuzzle.activities;

import java.util.Observable;
import java.util.Observer;

import com.jonasz.swipepuzzle.R;
import com.jonasz.swipepuzzle.puzzle.PuzzleArea;
import com.jonasz.swipepuzzle.utilities.ScoresList;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class NewGameActivity extends SuperClassActivity implements Observer {
	
	private static final int DIMENSIONS = 3;
	private static final int SCORE_TEXT_SIZE = 20;
	private static final int MIN_SWIPE_DISTANCE = 150;
	private static final int NUMBER_OF_SCORES = 10;
	private static final int PRESS_LENGTH_TO_SHOW_SOLVED = 500;

	private static final String END_GAME_MESSAGE_TITLE = "Game over";
	private static final String SAVE_SCORE_TITLE = "Save your score";
	private static final String OVERWRITE_SCORE_MESSAGE_TITLE = "Overwrite score?";
	private static final String OVERWRITE_SCORE_MESSAGE = "There is a high score with the name you typed in used already. Do you want to overwrite it?";

	private AbsoluteLayout layout;
	
	private String imageName;
	
	private ScoresList scoresList;
	
	private PuzzleArea puzzleArea;
	private TextView scoreText;
	
	private AdView adView;
	
	private Handler handler;
	private Runnable longPressed;
	
	private int x1, y1, x2, y2;
	
	private int numberOfSwipes;
	
	private String playerName;
	
	private AlertDialog saveScoreDialog;
	
	private boolean showingSolvedPuzzle;
	private boolean startNewGame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_game);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		layout = (AbsoluteLayout) findViewById(R.id.layout); 
		
		createScoresList();		
		getScreenSize();
		getExtrasFromIntent();
		createPuzzleArea();		
		createLongPressedHandler();
		createAdView();
		setScoreText();
	}
	
	private void createScoresList() {
		SharedPreferences scores = getSharedPreferences("Scores", 0);
		scoresList = new ScoresList(scores, NUMBER_OF_SCORES);
	}
	
	private void getExtrasFromIntent() {
		startNewGame = getIntent().getExtras().getBoolean("startNewGame");
		imageName = getIntent().getExtras().getString("imageName");
	}
	
	private void createPuzzleArea() {
		puzzleArea = new PuzzleArea(this, layout, DIMENSIONS, imageName);
		puzzleArea.setScreenAndImagesSize(screenWidth, screenHeight);

		puzzleArea.setPuzzleImages(startNewGame);
		puzzleArea.addObserver(this);
	}
	
	private void createLongPressedHandler() {
		handler  = new Handler();
		longPressed = new Runnable() { 
		    public void run() { 
		        puzzleArea.showSolvedPuzzle();
		        showingSolvedPuzzle = true;
		    }   
		};
	}
	
	private void createAdView() {
		adView = (AdView)findViewById(R.id.adView);
		
		adView.measure(0, 0);
		adView.setX(screenWidth/2 - adView.getMeasuredWidth()/2);
		adView.setY(screenHeight - adView.getMeasuredHeight());
		
		adView.setAdListener(new AdListener() {
			public void onAdOpened() {
				puzzleArea.saveGame();
				saveNumberOfSwipes();
			}
			public void onAdLeftApplication() {
				puzzleArea.saveGame();
				saveNumberOfSwipes();
			}
		});
		
		AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
	}
	
	private void setScoreText() {
		scoreText = new TextView(this);
		layout.addView(scoreText);
		
		scoreText.setTextSize(SCORE_TEXT_SIZE);
		scoreText.setTextColor(Color.GRAY);
		scoreText.setTypeface(Typeface.SERIF);
		
		if (startNewGame)
			numberOfSwipes = 0;
		else {
			SharedPreferences savedGame = getSharedPreferences("SavedGame", 0);
			numberOfSwipes = savedGame.getInt("numberOfSwipes", 0);
		}
		
		scoreText.setText("Number of swipes: " + Integer.toString(numberOfSwipes));
		
		scoreText.measure(0, 0);
		scoreText.setX(screenWidth/2 - scoreText.getMeasuredWidth()/2);
		scoreText.setY(adView.getY() - scoreText.getMeasuredHeight());
	}
	
	public void onPause() {
		super.onPause();
		puzzleArea.saveGame();
		saveNumberOfSwipes();
	}
	
	public void onStop() {
		super.onStop();
		puzzleArea.saveGame();
		saveNumberOfSwipes();
		
	}
	
	public void onDestroy() {
		super.onDestroy();
		puzzleArea.saveGame();
		saveNumberOfSwipes();
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (!puzzleArea.blockTouchListener)
			switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					showingSolvedPuzzle = false;
					handler.postDelayed(longPressed, PRESS_LENGTH_TO_SHOW_SOLVED);
					
					x1 = (int)event.getX();
					y1 = (int)event.getY();
					break;
					
				case MotionEvent.ACTION_UP:
					handler.removeCallbacks(longPressed);
					if (showingSolvedPuzzle)
						puzzleArea.backToUnsolvedPuzzle();
					else {
						x2 = (int)event.getX();
						y2 = (int)event.getY();
						if (Math.abs(x2 - x1) > MIN_SWIPE_DISTANCE)
							if (x2 - x1 > 0)
								puzzleArea.checkRight();
							else
								puzzleArea.checkLeft();
						else
							if (Math.abs(y2 - y1) > MIN_SWIPE_DISTANCE)
								if (y2 - y1 > 0)
									puzzleArea.checkDown();
								else
									puzzleArea.checkUp();
					}
					break;
			}

		return super.onTouchEvent(event);
	}
	
	private void saveNumberOfSwipes() {
		SharedPreferences.Editor editor = getSharedPreferences("SavedGame", 0).edit();
		editor.putInt("numberOfSwipes", numberOfSwipes);
		editor.commit();
	}
	
	public void update(Observable observable, Object data) {
		if (data == "swipe") {
			scoreText.setText("Number of swipes: " + Integer.toString(++numberOfSwipes));

			return;
		}
		
		if (data == "solved") {
			scoresList.checkForNewScore(numberOfSwipes);
			if (scoresList.setNewScore)
				saveScoreDialog();
			else
				endGame();
		}			
	}
	
	private void saveScoreDialog() {
		final EditText input = new EditText(this);
		input.setHint("Player name");
		input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		
		final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(SAVE_SCORE_TITLE)
			.setView(input)
			.setPositiveButton("Save", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {           
					playerName = input.getText().toString();

					playerName.trim();
				}
			});		
		
		saveScoreDialog = builder.create();
		
		saveScoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {		
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

				if (playerName == null || playerName.isEmpty()) {
					endGame();
					return;
				}
				
				if (scoresList.newScoreForOldPlayer(playerName)) 
					overwriteScoreDialog(playerName);
				else {
					scoresList.addNewScore(playerName);
					endGame();
				}				
			}
		});
		
		saveScoreDialog.setCanceledOnTouchOutside(false);
		saveScoreDialog.show();
		
		saveScoreDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		
		input.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable arg0) {
				if (input.getText().toString().length() >= 2 && input.getText().toString().length() <= 15)
					saveScoreDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				else
					saveScoreDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			}
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
		});		
	}
	
	private void overwriteScoreDialog(final String playerName) {	
		AlertDialog overwriteScoreDialog;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(OVERWRITE_SCORE_MESSAGE_TITLE)
			.setMessage(OVERWRITE_SCORE_MESSAGE)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {           
					scoresList.overwriteScore(playerName);
				}
			})	
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which) {           
					saveScoreDialog();
				}
			});
			
		overwriteScoreDialog = builder.create();
		overwriteScoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				endGame();
			}
		});
		overwriteScoreDialog.show();
	}
	
	private void endGame() {
		AlertDialog endGameDialog;
		String endGameMessage;
		
		SharedPreferences.Editor editor = getSharedPreferences("SavedGame", 0).edit();
		editor.putInt("numberOfSwipes", 0);
		editor.commit();
		
		if (scoresList.sameNameWorseScore)
			endGameMessage = "You had a better score before! Your score is: " + numberOfSwipes + " swipes.";
		else
			endGameMessage = "Your score is: " + numberOfSwipes + " swipes.";
				
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(END_GAME_MESSAGE_TITLE)
			.setMessage(endGameMessage)
			.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {           
					dialog.dismiss();
				}
			});				
				
		endGameDialog = builder.create();
		endGameDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				finish();
	            System.exit(0);
			}
		});
		endGameDialog.show();
	}
}
