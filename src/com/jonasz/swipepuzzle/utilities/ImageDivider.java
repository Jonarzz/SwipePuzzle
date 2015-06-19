package com.jonasz.swipepuzzle.utilities;

import android.graphics.Bitmap;

public class ImageDivider {
	
	public static Bitmap[] generateDividedImageArray(int dimensions, Bitmap image) {

		Bitmap dividedImageArray[] = new Bitmap[dimensions * dimensions];
		
		int partWidth = image.getWidth() / dimensions;
		int partHeight = image.getHeight() / dimensions;
		
		int count = 0;
		
		for (int i = 0; i < dimensions; i++)
			for (int j = 0; j < dimensions; j++)
				dividedImageArray[count++] = Bitmap.createBitmap(image, j * partWidth, i * partHeight, partWidth, partHeight);

		return dividedImageArray;
	}
}
