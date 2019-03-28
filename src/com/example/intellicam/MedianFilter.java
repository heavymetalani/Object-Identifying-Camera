package com.example.intellicam;

import android.graphics.Bitmap;
import android.graphics.Color;

public class MedianFilter {

	private int size; // size of the square filter
	private int[] pixels;

	public MedianFilter(int s) {
		if ((s % 2 == 0) || (s < 3)) { // check if the filter size is an odd
										// number > = 3
			System.out.println(s + "is not a valid filter size.");
			System.out.println("Filter size is now set to 3");
			s = 3;
		}
		size = s;
	}

	public int getFilterSize() {
		return size;
	}

	// sort the array, and return the median
	public int median(int[] a) {
		int asize = a.length;
		// sort the array in increasing order
		
		//Bubble sort
		for (int i = 0; i < asize; i++)
			for (int j = i + 1; j < asize; j++)
				if (a[i] > a[j]) {
					int temp = a[i];
					a[i] = a[j];
					a[j] = temp;
				}
		
		//return median
		if (asize % 2 == 1)
			return a[asize / 2];
		else
			return ((a[asize / 2] + a[asize / 2 - 1]) / 2);
	}

	public int[] getArray(Bitmap image, int x, int y) {
		int[] n=new int[9]; 
		
		int pos = y*image.getWidth() + x;
		
		// store the pixel values of position(x, y) and its neighbors from pixel array
		n[0] = pixels[pos - image.getWidth() - 1];
		n[1] = pixels[pos - image.getWidth()];
		n[2] = pixels[pos - image.getWidth() + 1];
		n[3] = pixels[pos - 1];
		n[4] = pixels[pos];
		n[5] = pixels[pos + 1];
		n[6] = pixels[pos + image.getWidth() - 1];
		n[7] = pixels[pos + image.getWidth()];
		n[8] = pixels[pos + image.getWidth() + 1];
		
		return n;
	}

	public void filter(Bitmap srcImage, Bitmap dstImage) {
		int height = srcImage.getHeight();
		int width = srcImage.getWidth();
		
		//get pixels in an array
		pixels = new int[height*width];
		srcImage.getPixels(pixels, 0, srcImage.getWidth(), 0, 0, srcImage.getWidth(), srcImage.getHeight());

		int[] a; // the array that gets the pixel value at (x, y) and its
					// neightbors

		for (int k = 1; k < height-1; k++) {
			for (int j = 1; j < width-1; j++) {
				a = getArray(srcImage, j, k);
				int pos = k*width + j;
				int[] red, green, blue;
				red = new int[a.length];
				green = new int[a.length];
				blue = new int[a.length];

				// get the red,green,blue value from the pixel
				for (int i = 0; i < a.length; i++) {
					red[i] = Color.red(a[i]);
					green[i] = Color.green(a[i]);
					blue[i] = Color.blue(a[i]);
				}
				// find the median for each color
				int R = median(red);
				int G = median(green);
				int B = median(blue);

				// set the new pixel value using the median just found
				int spixel = Color.rgb(R, G, B);
				pixels[pos] = spixel;
			}
		}
		
		//set dst bitmap from median filtered pixel array
		dstImage.setPixels(pixels, 0, width, 0, 0, width, height);
	}
}
