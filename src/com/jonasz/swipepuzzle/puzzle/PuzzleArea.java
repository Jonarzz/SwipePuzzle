package com.jonasz.swipepuzzle.puzzle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

public class PuzzleArea extends Observable {
	
	private static final int NUMBER_OF_RANDOM_ITERATIONS = 200;
	private static final String DEFAULT_IMAGE_NAME = "girl";

	private int SWIPE_TIME = 200;
	
	private Context context;
	private AbsoluteLayout layout;
	
	private SharedPreferences savedGame;
	
	private int dimensions;
	private String imageName;
	
	public PuzzleImage[] puzzleImages;
	private boolean[] isEmpty;
	
	private ImageView solvedImage;
	
	private int screenWidth;
	private int imageSize;
	
	public boolean blockTouchListener;
	private boolean userImageChosen;

	public PuzzleArea(Context context, AbsoluteLayout layout, int dimensions, String imageName) {
		this.dimensions = dimensions;
		this.imageName = imageName;
		this.context = context;
		this.layout = layout;
		this.savedGame = context.getSharedPreferences("SavedGame", 0);
				
		if (context.getSharedPreferences("Settings", 0).getString("imageName", DEFAULT_IMAGE_NAME).equals("userImage"))
			userImageChosen = true;
		
		puzzleImages = new PuzzleImage[dimensions * dimensions];
		isEmpty = new boolean[dimensions * dimensions];
	}

	public void setScreenAndImagesSize(int width, int height) {
		screenWidth = width;
		imageSize = screenWidth / dimensions;
	}

	public void setPuzzleImages(boolean startNewGame) {
		createPuzzleImagesAndAddToLayout();
		setSolvedImage();
		
		if (userImageChosen)
			for (int i = 0; i < puzzleImages.length - 1; i++)
				puzzleImages[i].setImageBitmap(loadUserImage(i));
		else
			for (int i = 0; i < puzzleImages.length - 1; i++)
				puzzleImages[i].setImageBitmap(loadImage(i));
		
		setImagesNeighbourExists();
		addNeighbours();
		
		if (wasGameSaved() == false)
			startNewGame = true;
		
		if (startNewGame)
			randomizePuzzleArea();
		else
			loadGame();
		
		saveGame();
		
		setPuzzleImagesSizeAndPositions();
	}
	
	private void createPuzzleImagesAndAddToLayout() {
		for (int i = 0; i < dimensions * dimensions; i++) {
			puzzleImages[i] = new PuzzleImage(context);
			puzzleImages[i].correctPosition = i;
			puzzleImages[i].currentPosition = i;
			layout.addView(puzzleImages[i]);
		}
	}
	
	private void setSolvedImage() {
		solvedImage = new ImageView(context);
		
		if (userImageChosen)
			solvedImage.setImageBitmap(loadUserImage());
		else	
			solvedImage.setImageBitmap(loadImage());
		
		AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(screenWidth, screenWidth, 0, 0);
		solvedImage.setLayoutParams(params);
		solvedImage.setX(0);
		solvedImage.setY(0);
		solvedImage.setVisibility(View.INVISIBLE);
		layout.addView(solvedImage);
	}
	
	private Bitmap loadImage() {
		AssetManager assetManager = context.getAssets();
		Bitmap bitmap = null;
		
		String filePath = imageName + "/" + imageName + ".jpg";
	
		try {
			InputStream fis = assetManager.open(filePath);
			bitmap = BitmapFactory.decodeStream(fis);
		}
		catch (IOException e) {
			Log.e("PuzzleArea", "IOException", e);
		}

		return bitmap;
	}
	
	private Bitmap loadImage(int imageNumber) {		
		AssetManager assetManager = context.getAssets();
		InputStream istream;
		Bitmap bitmap = null;
		
		String filePath = imageName + "/" + Integer.toString(imageNumber) + ".jpg";
		
		try {
			istream = assetManager.open(filePath);
			bitmap = BitmapFactory.decodeStream(istream);
		}
		catch (IOException e) {
			Log.e("PuzzleArea", "Exception", e);
		}

		return bitmap;
	}
	
	private Bitmap loadUserImage() {
		Bitmap bitmap = null;

		try {
			FileInputStream fis = context.openFileInput("userImage.jpg");
			bitmap = BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			imageName = DEFAULT_IMAGE_NAME;
			context.getSharedPreferences("Settings", 0).edit().putString("imageName", DEFAULT_IMAGE_NAME).commit();
			bitmap = loadImage();
			userImageChosen = false;
			
			Log.e("PuzzleArea", "Exception", e);
		}

		return bitmap;
	}
	
	private Bitmap loadUserImage(int imageNumber) {
		Bitmap bitmap = null;
		
		try {
			FileInputStream fis = context.openFileInput(Integer.toString(imageNumber) + ".jpg");
			bitmap = BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			Log.e("PuzzleArea", "Exception", e);
		}

		return bitmap;
	}
	
	private void setImagesNeighbourExists() {
		for (int i = 0; i < puzzleImages.length; i++) {
			if (puzzleImages[i].currentPosition < dimensions)
				puzzleImages[i].upNeighbourExists = false;
			
			if (puzzleImages[i].currentPosition  % dimensions == dimensions - 1)
				puzzleImages[i].rightNeighbourExists = false;
			
			if (puzzleImages[i].currentPosition >= puzzleImages.length - dimensions)
				puzzleImages[i].downNeighbourExists = false;
			
			if (puzzleImages[i].currentPosition % dimensions == 0)
				puzzleImages[i].leftNeighbourExists = false;
		}
	}
	
	private void addNeighbours() {
		for (int i = 0; i < puzzleImages.length; i++) {
			if (puzzleImages[i].upNeighbourExists)
				puzzleImages[i].neighboursNumbers.add(i - dimensions);
				
			if (puzzleImages[i].rightNeighbourExists)
				puzzleImages[i].neighboursNumbers.add(i + 1);
				
			if (puzzleImages[i].downNeighbourExists)
				puzzleImages[i].neighboursNumbers.add(i + dimensions);
				
			if (puzzleImages[i].leftNeighbourExists)
				puzzleImages[i].neighboursNumbers.add(i - 1);
		}
	}
	
	private void randomizePuzzleArea() {
		int emptyPosition = dimensions*dimensions - 1;
		Random random = new Random();
		
		int randomNumber, positionToSwap;
		
		for (int m = 0; m < NUMBER_OF_RANDOM_ITERATIONS; m++) {
			randomNumber = random.nextInt(puzzleImages[emptyPosition].neighboursNumbers.size());
			positionToSwap = puzzleImages[emptyPosition].neighboursNumbers.get(randomNumber);
			
			swapElements(positionToSwap, emptyPosition);
			
			emptyPosition = positionToSwap;
		}
		
		while (emptyPosition != dimensions*dimensions - 1) {
			if (puzzleImages[emptyPosition].downNeighbourExists) {
				swapElements(emptyPosition + dimensions, emptyPosition);
				
				emptyPosition = emptyPosition + dimensions;
			}
			else {
				swapElements(emptyPosition + 1, emptyPosition);
				
				emptyPosition = emptyPosition + 1;
			}
		}
		
		for (int i = 0; i < puzzleImages.length - 1; i++)
			isEmpty[i] = false;
		isEmpty[puzzleImages.length - 1] = true;
		
		puzzleImages[puzzleImages.length - 1].setVisibility(View.INVISIBLE);
	}
	
	private void setPuzzleImagesSizeAndPositions() {
		for (int i = 0; i < puzzleImages.length; i++) {
			puzzleImages[i].setSize(imageSize, imageSize);
			puzzleImages[i].setX(i % dimensions * imageSize);
			puzzleImages[i].setY((int)i/3 * imageSize);
		}
	}
	
	public void checkDown() {
		for (int i = 0; i < puzzleImages.length - dimensions; i++)
			if (isEmpty[i + dimensions]) {
				isEmpty[i + dimensions] = false;
				isEmpty[i] = true;
				
				for (int j = 0; j < puzzleImages.length; j++)
					if (puzzleImages[j].currentPosition == i) {
						puzzleImages[j].currentPosition = i + dimensions;
						
						for (int m = 0; m <puzzleImages.length; m++)
							if (puzzleImages[m].getDrawable() == null) {
								puzzleImages[m].currentPosition = i;
								break;
							}
						
						moveDown(j);
						break;
					}

				break;
			}
	}
	
	public void checkUp() {
		for (int i = dimensions; i < puzzleImages.length; i++)
			if (isEmpty[i - dimensions]) {
				isEmpty[i - dimensions] = false;
				isEmpty[i] = true;
				
				for (int j = 0; j < puzzleImages.length; j++)
					if (puzzleImages[j].currentPosition == i) {
						puzzleImages[j].currentPosition = i - dimensions;
						
						for (int m = 0; m <puzzleImages.length; m++)
							if (puzzleImages[m].getDrawable() == null) {
								puzzleImages[m].currentPosition = i;
								break;
							}
						
						moveUp(j);
						break;
					}

				break;
			}
	}
	
	public void checkRight() {
		for (int i = 0; i < puzzleImages.length; i++)
			if (i % dimensions != dimensions - 1)
				if (isEmpty[i + 1]) {
					isEmpty[i + 1] = false;
					isEmpty[i] = true;
					
					for (int j = 0; j < puzzleImages.length; j++)
						if (puzzleImages[j].currentPosition == i) {
							puzzleImages[j].currentPosition = i + 1;
							
							for (int m = 0; m <puzzleImages.length; m++)
								if (puzzleImages[m].getDrawable() == null) {
									puzzleImages[m].currentPosition = i;
									break;
								}
							
							moveRight(j);
							break;
						}

					break;
				}
	}
	
	public void checkLeft() {
		for (int i = 0; i < puzzleImages.length; i++)
			if (i % dimensions != 0)
				if (isEmpty[i - 1]) {
					isEmpty[i - 1] = false;
					isEmpty[i] = true;
					
					for (int j = 0; j < puzzleImages.length; j++)				
						if (puzzleImages[j].currentPosition == i) {
							puzzleImages[j].currentPosition = i - 1;
							
							for (int m = 0; m <puzzleImages.length; m++)
								if (puzzleImages[m].getDrawable() == null) {
									puzzleImages[m].currentPosition = i;
									break;
								}
							
							moveLeft(j);
							break;
						}

					break;
				}
	}
	
	private void moveDown(final int i) {
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, imageSize);
		animation.setDuration(SWIPE_TIME);
		
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
				blockTouchListener = true;
			}
			public void onAnimationEnd(Animation animation) {
				puzzleImages[i].clearAnimation();
				puzzleImages[i].setY(puzzleImages[i].getY() + imageSize);
				blockTouchListener = false;
			}
			public void onAnimationRepeat(Animation animation) { }
			
		});
		
		puzzleImages[i].startAnimation(animation);
		
		setChanged();
		notifyObservers("swipe");
		isPuzzleSolved();
	}
	
	private void moveUp(final int i) {
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -imageSize);
		animation.setDuration(SWIPE_TIME);
		
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
				blockTouchListener = true;
			}
			public void onAnimationEnd(Animation animation) {
				puzzleImages[i].clearAnimation();
				puzzleImages[i].setY(puzzleImages[i].getY() - imageSize);
				blockTouchListener = false;
			}
			public void onAnimationRepeat(Animation animation) { }
			
		});
		
		puzzleImages[i].startAnimation(animation);
		
		setChanged();
		notifyObservers("swipe");
		isPuzzleSolved();
	}
	
	private void moveRight(final int i) {
		TranslateAnimation animation = new TranslateAnimation(0, imageSize, 0, 0);
		animation.setDuration(SWIPE_TIME);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
				blockTouchListener = true;
			}
			public void onAnimationEnd(Animation animation) {
				puzzleImages[i].clearAnimation();
				puzzleImages[i].setX(puzzleImages[i].getX() + imageSize);
				blockTouchListener = false;
			}
			public void onAnimationRepeat(Animation animation) { }
			
		});
		
		puzzleImages[i].startAnimation(animation);
		
		setChanged();
		notifyObservers("swipe");
		isPuzzleSolved();
	}
	
	private void moveLeft(final int i) {
		TranslateAnimation animation = new TranslateAnimation(0, -imageSize, 0, 0);
		animation.setDuration(SWIPE_TIME);
		
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
				blockTouchListener = true;
			}
			public void onAnimationEnd(Animation animation) {
				puzzleImages[i].clearAnimation();
				puzzleImages[i].setX(puzzleImages[i].getX() - imageSize);
				blockTouchListener = false;
			}
			public void onAnimationRepeat(Animation animation) { }
			
		});
		
		puzzleImages[i].startAnimation(animation);
		
		setChanged();
		notifyObservers("swipe");
		isPuzzleSolved();
	}
	
	private void isPuzzleSolved() {
		for (int i = 0; i < puzzleImages.length; i++)
			if (puzzleImages[i].currentPosition != puzzleImages[i].correctPosition) 
				return;
		
		SharedPreferences.Editor editor = savedGame.edit();
		editor.putBoolean("savedGame", false);
		editor.commit();
		
		setChanged();
		notifyObservers("solved");
	}
	
	public void showSolvedPuzzle() {
		solvedImage.setVisibility(View.VISIBLE);
	}

	public void backToUnsolvedPuzzle() {
		solvedImage.setVisibility(View.INVISIBLE);
	}
	
	private boolean wasGameSaved() {
		return savedGame.getBoolean("savedGame", false);
	}
	
	public void saveGame() {
		SharedPreferences.Editor editor = savedGame.edit();
		
		String element;
		int j;
		
		for (int i = 0; i < dimensions * dimensions; i++) {
			element = "puzzleImage" + Integer.toString(i);
			
			j = 0;
			while (puzzleImages[j].currentPosition != i && j < puzzleImages.length - 1)
				j++;
			
			editor.putInt(element, puzzleImages[j].correctPosition);
		}

		editor.putBoolean("savedGame", true);
		
		editor.commit();
	}

	private void loadGame() {
		savedGame = context.getSharedPreferences("SavedGame", 0);
		int correctPosition, j;
		
		for (int i = 0; i < puzzleImages.length; i++) {
			correctPosition = savedGame.getInt("puzzleImage" + Integer.toString(i), 0);
			
			j = 0;
			while(puzzleImages[j].correctPosition != correctPosition)
				j++;
			
			swapElements(i, j);
		}
		
		for (int i = 0; i < puzzleImages.length; i++) {
			isEmpty[i] = false;
			puzzleImages[i].setVisibility(View.VISIBLE);
			if (puzzleImages[i].getDrawable() == null) {
				isEmpty[i] = true;
				puzzleImages[i].setVisibility(View.INVISIBLE);
			}
		}
	}
	
	private void swapElements(int position1, int position2) {
		if (position1 == position2)
			return;

		Drawable temp;
		int temp2;
		
		temp = puzzleImages[position1].getDrawable();
		puzzleImages[position1].setImageDrawable(puzzleImages[position2].getDrawable());
		puzzleImages[position2].setImageDrawable(temp);
		
		temp2 = puzzleImages[position1].correctPosition;
		puzzleImages[position1].correctPosition = puzzleImages[position2].correctPosition;
		puzzleImages[position2].correctPosition = temp2;
	}
}
