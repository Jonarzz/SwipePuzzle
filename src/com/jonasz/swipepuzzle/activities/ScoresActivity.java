package com.jonasz.swipepuzzle.activities;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jonasz.swipepuzzle.R;
import com.jonasz.swipepuzzle.utilities.ScoresList;

public class ScoresActivity extends SuperClassActivity {
	
	private static final int NUMBER_OF_SCORES = 10;
	private static final int SCORE_TEXT_SIZE = 20;
	
	private LinearLayout layout;

	private SharedPreferences scores;
	private ScoresList highScores;
	
	private ArrayList<TextView> scoresTextList;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scores);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		layout = (LinearLayout) findViewById(R.id.scores_layout);

		createHighScoresAndList();
		setScoresTextList();
		
		addScoresTextListToLayout();
	}
	
	private void createHighScoresAndList() {
		scores = getSharedPreferences("Scores", 0);
		highScores = new ScoresList(scores, NUMBER_OF_SCORES);
		scoresTextList = new ArrayList<TextView>();
	}
	
	private void setScoresTextList() {
		String line = "";
		TextView textView = null;
		
		for (int i = 0; i < NUMBER_OF_SCORES; i++) {	
			textView = new TextView(this);
			
			line = highScores.scoresList.get(i).playerName;
			
			while (line.length() < 18)
				line += " ";
			
			if (highScores.scoresList.get(i).score < 1000)
				line += highScores.scoresList.get(i).score;
			
			textView.setText(line);
			textView.setTextColor(Color.WHITE);
			textView.setTextSize(SCORE_TEXT_SIZE);
			textView.setTypeface(Typeface.MONOSPACE);

			scoresTextList.add(textView);
		}
	}
	
	private void addScoresTextListToLayout() {
		for (int i = 0; i < scoresTextList.size(); i++) {
			layout.addView(scoresTextList.get(i));
		}
	}
}
