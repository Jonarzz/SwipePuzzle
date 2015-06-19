package com.jonasz.swipepuzzle.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.jonasz.swipepuzzle.R;
import com.jonasz.swipepuzzle.shapes.Point;
import com.jonasz.swipepuzzle.shapes.Rectangle;
import com.jonasz.swipepuzzle.utilities.ImageDivider;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;

import com.jonasz.swipepuzzle.camera.CropImageIntentBuilder;
import com.jonasz.swipepuzzle.camera.MediaStoreUtils;

public class SettingsActivity extends SuperClassActivity {

	static final String RESET_SCORE_DIALOG_TITLE = "Reset scores?";
	static final String RESET_SCORE_SCORE_DIALOG_MESSAGE = "Are you sure you want to reset scores?";
	
	static final int VIBRATE_LENGTH = 25;
	static final int NUMBER_OF_IMAGE_BUTTONS = 4;
	
	private static int REQUEST_PICTURE = 1;
    private static int REQUEST_CROP_PICTURE = 2;
	
	Vibrator vibe;
	
	private String[] imageNames;
	private Button[] imageButtons;
	
	private Button resetScoresButton;
	
	private int imageSize;
	
	private int indexOfOldSelectedImage;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		imageNames = new String[]{"girl", "flower", "water", "userImage"};
		imageButtons = new Button[NUMBER_OF_IMAGE_BUTTONS];

		getScreenSize();
		setButtonProperties();
		createImageSelectPanel();
		createResetScoresButton();
	}
	
	private void createImageSelectPanel() {
		imageSize = screenWidth / 2;
		
		int i = 0;
		for (String name : imageNames) {
			String buttonName = name + "Button";
			int resID = getResources().getIdentifier(buttonName, "id", this.getPackageName());

			imageButtons[i] = (Button)findViewById(resID);
			
			createImageButton(imageButtons[i++], name);
		}
	}
	
	private void createImageButton(Button button, String imageName) {
		button.setWidth(imageSize);
		button.setHeight(imageSize);
		
		button.setOnClickListener(new AddImageButtonClickListener(button, imageName));
		button.setOnTouchListener(new AddImageButtonTouchListener());
		
		SharedPreferences settings = getSharedPreferences("Settings", 0);
		if (settings.getString("imageName", "girl").equals(imageName))
			addSelectedImageButtonImage(button, imageName);
		else
			addImageButtonImage(button, imageName);
	}
	
	private class AddImageButtonClickListener implements OnClickListener {
		private String imageName;
		private Button button;
		
		public AddImageButtonClickListener(Button button, String imageName) {
			this.imageName = imageName;
			this.button = button;
		}
		
		public void onClick(View arg0) {
			if (imageName == "userImage")
				addUserImage();
			
			SharedPreferences settings = getSharedPreferences("Settings", 0);
			if (settings.getString("imageName", "girl").equals(imageName))
				return;
			
			if (indexOfOldSelectedImage == 0)
				while (!settings.getString("imageName", "girl").equals(imageNames[indexOfOldSelectedImage]))
					indexOfOldSelectedImage++;
			
			resetSettings(imageName, settings);
			
			addImageButtonImage(imageButtons[indexOfOldSelectedImage], imageNames[indexOfOldSelectedImage]);
			addSelectedImageButtonImage(button, imageName);
			
			setIndexOfOldSelectedImage(button);
			showToastAfterChoosingImage(imageName);
		}
	}
	
	private void resetSettings(String imageName, SharedPreferences settings) {
		SharedPreferences savedGame = getSharedPreferences("SavedGame", 0);			
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("imageName", imageName);
		editor.commit();
		
		editor = savedGame.edit();
		editor.putInt("numberOfSwipes", 0);
		editor.putBoolean("savedGame", false);
		editor.commit();
	}
	
	private void setIndexOfOldSelectedImage(Button button) {
		for (int i = 0; i < imageButtons.length; i++)
			if (imageButtons[i] == button) {
				indexOfOldSelectedImage = i;
				break;
			}
	}
	
	private void showToastAfterChoosingImage(String imageName) {
		String toastMessage = "Image chosen: ";
		
		if (imageName == "girl")
			toastMessage += "girl with a sparkler";
		if (imageName == "flower")
			toastMessage += "flower";
		if (imageName == "water")
			toastMessage += "reflection in water";
		if (imageName == "userImage")
			toastMessage += "user image";
		
		showToast(toastMessage);
	}
	
	private void addUserImage() {
		startActivityForResult(MediaStoreUtils.getPickImageIntent(this), REQUEST_PICTURE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        File croppedImageFile = new File(getFilesDir(), "test.jpg");

        if ((requestCode == REQUEST_PICTURE) && (resultCode == RESULT_OK)) {
            Uri croppedImage = Uri.fromFile(croppedImageFile);

            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(screenWidth, screenWidth, croppedImage);
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(data.getData());
            cropImage.setOutputQuality(100);
            cropImage.setScale(true);
            cropImage.setScaleUpIfNeeded(true);

            startActivityForResult(cropImage.getIntent(this), REQUEST_CROP_PICTURE);
        }
        else 
        	if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == RESULT_OK)) {
	            Bitmap bitmap = BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
	           	            
	            FileOutputStream fos;
	            
				try {
					fos = getApplicationContext().openFileOutput("userImage.jpg", Context.MODE_PRIVATE);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.close();
				} catch (Exception e) {
					Log.e("SettingsActivity", "Exception", e);
				}
				
				Bitmap dividedImageArray[] = ImageDivider.generateDividedImageArray(3, bitmap);
				
				for (int i = 0; i < dividedImageArray.length; i++)
					try {
						fos = getApplicationContext().openFileOutput(Integer.toString(i) + ".jpg", Context.MODE_PRIVATE);
						dividedImageArray[i].compress(Bitmap.CompressFormat.JPEG, 100, fos);
						fos.close();
					} catch (IOException e) {
						Log.e("SettingsActivity", "IOException", e);
					}			
				
				showToast("The image was set.\nIt could have been scaled up.");
	        }
    }
	
	private class AddImageButtonTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				vibe.vibrate(VIBRATE_LENGTH);
			}
            if(event.getAction() == MotionEvent.ACTION_UP) {
            	if(touchingInsideButton(view, event))
            		view.performClick();
            }
            
			return false;
		}
		
		private boolean touchingInsideButton(View view, MotionEvent event) {
			Point touchingPoint = new Point((int)event.getX(), (int)event.getY());
			Rectangle buttonArea = new Rectangle((int)view.getX(), (int)view.getY(), imageSize, imageSize);
			
			if (buttonArea.contains(touchingPoint))
				return true;
			else
				return false;
		}
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void addImageButtonImage(Button button, String imageName) {
		imageName = setImageNameForUserImage(imageName);
		
		String buttonName = imageName + "_button";
		int resID = getResources().getIdentifier(buttonName, "drawable", this.getPackageName());
		
		Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resID);
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, imageSize, imageSize, true);
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
			button.setBackground(new BitmapDrawable(getResources(), scaledBitmap));
		else
			button.setBackgroundDrawable(new BitmapDrawable(scaledBitmap));
	}	

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void addSelectedImageButtonImage(Button button, String imageName) {
		imageName = setImageNameForUserImage(imageName);
		
		String buttonName = imageName + "_button_clicked";
		int resID = getResources().getIdentifier(buttonName, "drawable", this.getPackageName());

		Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resID);
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, imageSize, imageSize, true);
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
			button.setBackground(new BitmapDrawable(getResources(), scaledBitmap));
		else
			button.setBackgroundDrawable(new BitmapDrawable(scaledBitmap));
	}
	
	private String setImageNameForUserImage(String imageName) {
		for (int i = 0; i < imageName.length(); i++)
			if (Character.isUpperCase(imageName.charAt(i))) {
				StringBuilder sb = new StringBuilder(imageName);
				sb.setCharAt(i, Character.toLowerCase(imageName.charAt(i)));
				sb.insert(i, '_');
				imageName = sb.toString();
			}
		
		return imageName;
	}
	
	private void createResetScoresButton() {
		resetScoresButton = (Button)findViewById(R.id.resetScoresButton);
		
		resetScoresButton.setWidth(buttonWidth);
		resetScoresButton.setHeight(buttonHeight);
		
		resetScoresButton.setOnClickListener(new AddResetButtonClickListener());
		resetScoresButton.setOnTouchListener(new AddResetButtonTouchListener());
		
		addDefaultResetButtonImage();		
	}
	
	private class AddResetButtonClickListener implements OnClickListener {
		public void onClick(View arg0) {
			resetScoresDialog();
		}
	}
	
	private void resetScoresDialog() {
		AlertDialog resetScoresDialog;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(RESET_SCORE_DIALOG_TITLE)
			.setMessage(RESET_SCORE_SCORE_DIALOG_MESSAGE)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {           
					resetScoresInSharedPreferences();
					showToast("Scores reset!");
				}
			})	
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which) {           
					dialog.dismiss();
				}
			});
			
		resetScoresDialog = builder.create();
		resetScoresDialog.show();
	}
	
	private void resetScoresInSharedPreferences() {
		SharedPreferences scores = getSharedPreferences("Scores", 0);
		SharedPreferences.Editor editor = scores.edit();
		editor.clear();
		editor.commit();
	}
	
	private class AddResetButtonTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				addClickedResetButtonImage();
				vibe.vibrate(VIBRATE_LENGTH);
			}
            if(event.getAction() == MotionEvent.ACTION_UP) {
            	addDefaultResetButtonImage();
            	if(touchingInsideButton(view, event))
            		view.performClick();
            }
            
			return false;
		}
		
		private boolean touchingInsideButton(View view, MotionEvent event) {
			Point touchingPoint = new Point((int)event.getX(), (int)event.getY());
			Rectangle buttonArea = new Rectangle((int)view.getX(), (int)view.getY(), buttonWidth, buttonHeight);
			
			if (buttonArea.contains(touchingPoint))
				return true;
			else
				return false;
		}
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void addDefaultResetButtonImage() {
		Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.reset_scores_button);
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, buttonWidth, buttonHeight, true);
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
			resetScoresButton.setBackground(new BitmapDrawable(getResources(), scaledBitmap));
		else
			resetScoresButton.setBackgroundDrawable(new BitmapDrawable(scaledBitmap));
	}	

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void addClickedResetButtonImage() {
		Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.reset_scores_button_clicked);
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, buttonWidth, buttonHeight, true);
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
			resetScoresButton.setBackground(new BitmapDrawable(getResources(), scaledBitmap));
		else
			resetScoresButton.setBackgroundDrawable(new BitmapDrawable(scaledBitmap));
	}	
}
