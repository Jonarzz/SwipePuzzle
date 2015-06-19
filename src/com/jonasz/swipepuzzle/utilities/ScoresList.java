package com.jonasz.swipepuzzle.utilities;

import java.util.ArrayList;

import com.jonasz.swipepuzzle.activities.NewGameActivity;

import android.content.SharedPreferences;

public class ScoresList {
	
	private SharedPreferences scores;
	
	public ArrayList<Score> scoresList;
	
	private int numberOfScores;
	
	private int i;
	private int newScorePosition;
	private int oldScorePosition;
	private Score newScore;
	public boolean setNewScore;
	
	public boolean sameNameWorseScore;
	
	public ScoresList(SharedPreferences scores, int numberOfScores) {
		this.newScore = new Score();
		this.scores = scores;
		this.numberOfScores = numberOfScores;
		
		scoresList = new ArrayList<Score>();
		
		setScoresList();
	}
	
	private void setScoresList() {
		Score inputScore = null;
		
		String player;
		String score;
		
		// scores format: player0 - score0 (...) player9 - score9
		for (int i = 0; i < numberOfScores; i++) {
			inputScore = new Score();
			player = "player" + Integer.toString(i);
			score = "score" + Integer.toString(i);
			
			inputScore.playerName = scores.getString(player, "");
			inputScore.score = scores.getInt(score, 10001);
			
			scoresList.add(inputScore);
		}
	}
	
	public void checkForNewScore(int newScore) {
		setNewScore = false;
		
		for (int j = 0; j < scoresList.size(); j++)
			if (newScore < scoresList.get(j).score) {
				newScorePosition = j;
				this.newScore.score = newScore;
				setNewScore = true;
				break;
			}
	}

	public boolean newScoreForOldPlayer(String player) {
		sameNameWorseScore = false;
		
		for (int j = scoresList.size() - 1; j >= 0; j--)
			if (scoresList.get(j).playerName.equals(player)) {
				if (newScore.score < scoresList.get(j).score) {
					sameNameWorseScore = false;
					oldScorePosition = j;
					return true;
				}
				sameNameWorseScore = true;
			}
		
		return false;
	}
	
	public void addNewScore(String playerName) {	
		if (sameNameWorseScore)
			return;
		
		newScore.playerName = playerName;
		
		i = numberOfScores - 1;
		
		while (i > newScorePosition) {
			scoresList.set(i, scoresList.get(i - 1));
			i--;
		}
		
		scoresList.set(i, newScore);
		
		setNewHighScores();
	}
	
	public void overwriteScore(String playerName) {
		newScore.playerName = playerName;
		
		i = oldScorePosition;
		
		while (i > newScorePosition) {
			scoresList.set(i, scoresList.get(i - 1));
			i--;
		}
		
		scoresList.set(i, newScore);
		
		setNewHighScores();
	}

	private void setNewHighScores() {
		SharedPreferences.Editor editor = scores.edit();
		editor.clear();
		
		String player;
		String score;
		
		for (int i = 0; i < numberOfScores; i++) {
			player = "player" + Integer.toString(i);
			score = "score" + Integer.toString(i);
			
			editor.putString(player, scoresList.get(i).playerName);
			editor.putInt(score, scoresList.get(i).score);
		}
		
		editor.commit();
	}
}
