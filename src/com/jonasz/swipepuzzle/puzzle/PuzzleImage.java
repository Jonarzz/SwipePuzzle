package com.jonasz.swipepuzzle.puzzle;

import java.util.ArrayList;

import android.content.Context;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

public class PuzzleImage extends ImageView {
	
	public int currentPosition;
	public int correctPosition;
	public boolean emptySquare;
	
	public boolean upNeighbourExists;
	public boolean rightNeighbourExists;
	public boolean downNeighbourExists;
	public boolean leftNeighbourExists;
	
	public ArrayList<Integer> neighboursNumbers;
	
	public PuzzleImage(Context context) {
		super(context);
		
		setDefaultNeighbourExists();
		neighboursNumbers = new ArrayList<Integer>();
	}
	
	private void setDefaultNeighbourExists() {
		upNeighbourExists = true;
		rightNeighbourExists = true;
		downNeighbourExists = true;
		leftNeighbourExists = true;
	}

	public void setSize(int width, int height) {
		AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(height, width, 0, 0);
		this.setLayoutParams(params);
	}

}
