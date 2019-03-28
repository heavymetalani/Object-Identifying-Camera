package com.example.intellicam;

import java.io.File;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageMenu extends Activity {

	boolean btn;

	// UI elements
	Button identify;
	ImageView ObjImage;
	ProgressDialog LoadDialog;
	TextView tvImageInfo;

	// Image variables
	Bitmap bmp = null, tbmp = null, ebmp = null;
	int width = 0, height = 0;
	float hsvpixels[][];
	int rgbpixels[];
	int objwidth = 0, objheight = 0;
	final String FILENAME = "Object";
	File path;

	// boundaries of object
	int TOP = 0, BOTTOM = 0, LEFT = 0, RIGHT = 0;

	// Object color variables
	final int RED = 0, ORANGE = 1, YELLOW = 2, GREEN = 3, BLUE = 4, VIOLET = 5,
			PINK = 6, WHITE = 7, GRAY = 8;
	int colorarray[];
	final String ColorList[] = { "Red", "Orange", "Yellow", "Green", "Blue",
			"Violet", "Pink", "White", "Gray" };

	// Object size variables
	long area = 0;
	final String SizeList[] = { "Tiny", "Small", "Medium", "Large" };

	// Object shape variables
	final String ShapeList[] = { "Square", "Rectangle", "Circular",
			"Triangular", "Oblong", "Random" };

	// Object attribute array (Color at position 0, size at position 1, and
	// shape at position 2)
	String ObjAttr[];

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_image_menu, menu);
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set up variables
		setContentView(R.layout.activity_image_menu);
		ObjAttr = new String[3];
		ObjAttr[0] = "-";
		ObjAttr[1] = "-";
		ObjAttr[2] = "-";
		identify = (Button) findViewById(R.id.bIdentify);
		ObjImage = (ImageView) findViewById(R.id.ivObjImage);
		tvImageInfo = (TextView) findViewById(R.id.tvImageInfo);
		LoadDialog = new ProgressDialog(ImageMenu.this);
		LoadDialog.setMessage("Loading image");
		colorarray = new int[9];
		btn = true;

		// get file directory from the intent that started this activity
		Bundle b = getIntent().getExtras();
		path = (File) b.get("data");

		// call AsyncTask to fetch bitmap and set it in ImageView
		new SetUp().execute();
	}

	private void getBmp() {

		// get bitmap from SD card location, scale it to 800x600 and delete the
		// file on SD card
		bmp = Bitmap.createScaledBitmap(
				BitmapFactory.decodeFile(path.getAbsolutePath()), 800, 600,
				false);
		path.delete();
	}

	private void initVar() {

		// initialize variables
		height = bmp.getHeight();
		width = bmp.getWidth();
		rgbpixels = new int[width * height];
		hsvpixels = new float[width * height][3];

		// get pixels of bitmap into an array
		bmp.getPixels(rgbpixels, 0, width, 0, 0, width, height);

		// create mutable temporary bitmaps
		tbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}

	private void detectEdges() {

		// sobel edge detector

		float[] template = { -1, 0, 1, -2, 0, 2, -1, 0, 1 };
		int templateSize = 3;
		int[] rgbData = new int[width * height];
		ebmp.getPixels(rgbData, 0, width, 0, 0, width, height);
		int[] total = new int[width * height];
		int sumY = 0;
		int sumX = 0;
		int max = 0;

		for (int n = 0; n < 1; n++) {
			for (int x = (templateSize - 1) / 2; x < width - (templateSize + 1)
					/ 2; x++) {
				for (int y = (templateSize - 1) / 2; y < height
						- (templateSize + 1) / 2; y++) {
					sumY = 0;

					for (int x1 = 0; x1 < templateSize; x1++) {
						for (int y1 = 0; y1 < templateSize; y1++) {
							int x2 = (x - (templateSize - 1) / 2 + x1);
							int y2 = (y - (templateSize - 1) / 2 + y1);
							float value = (rgbData[y2 * width + x2] & 0xff)
									* (template[y1 * templateSize + x1]);

							sumY += value;
						}
					}

					sumX = 0;
					for (int x1 = 0; x1 < templateSize; x1++) {
						for (int y1 = 0; y1 < templateSize; y1++) {
							int x2 = (x - (templateSize - 1) / 2 + x1);
							int y2 = (y - (templateSize - 1) / 2 + y1);
							float value = (rgbData[y2 * width + x2] & 0xff)
									* (template[x1 * templateSize + y1]);

							sumX += value;
						}
					}
					total[y * width + x] = (int) Math.sqrt(sumX * sumX + sumY
							* sumY);

					if (max < total[y * width + x])
						max = total[y * width + x];
				}
			}

			float ratio = (float) max / 255;

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					sumX = (int) (total[y * width + x] / ratio);
					total[y * width + x] = 0xff000000 | ((int) sumX << 16
							| (int) sumX << 8 | (int) sumX);
				}
			}
		}
		System.arraycopy(total, 0, rgbData, 0, width * height);

		/*
		 * for (int i = 0; i < rgbData.length; i++) { int r =
		 * Color.red(rgbData[i]); int g = Color.green(rgbData[i]); int b =
		 * Color.blue(rgbData[i]);
		 * 
		 * // perform thresholding if (r < 40 && g < 40 && b < 40) rgbData[i] =
		 * Color.BLACK; else rgbData[i] = Color.WHITE; }
		 */
		ebmp.setPixels(rgbData, 0, width, 0, 0, width, height);

		// Canny Edge detector
		/*
		 * CannyEdgeDetector canny = new CannyEdgeDetector();
		 * canny.setSourceImage(bmp); canny.process(); ebmp =
		 * canny.getEdgesImage();
		 */
	}

	public void attemptId(View view) {
		if (btn) {

			// call AsyncTask to identify object
			new AttemptIdentification().execute();
			btn = false;
		}

		else {

			// put attribute array in a bundle
			Bundle b = new Bundle();
			b.putStringArray("Attributes", ObjAttr);

			// create intent, and pass the bundle to next activity
			Intent a = new Intent(ImageMenu.this, AttrScreen.class);
			a.putExtras(b);
			startActivity(a);
		}
	}

	private void isolateObject() {

		int minr = Color.red(rgbpixels[0]);
		int ming = Color.green(rgbpixels[0]);
		int minb = Color.blue(rgbpixels[0]);
		int maxr = Color.red(rgbpixels[0]);
		int maxg = Color.green(rgbpixels[0]);
		int maxb = Color.blue(rgbpixels[0]);

		// first 5 rows
		for (int y = 0; y < 5; y++) {
			for (int x = 0; x < bmp.getWidth(); x++) {

				int pos = y * bmp.getWidth() + x;

				int pixel = rgbpixels[pos];

				if (Color.red(pixel) > maxr) {
					maxr = Color.red(pixel);
				}

				if (Color.green(pixel) > maxg) {
					maxg = Color.green(pixel);
				}

				if (Color.blue(pixel) > maxb) {
					maxb = Color.blue(pixel);
				}

				if (Color.red(pixel) < minr) {
					minr = Color.red(pixel);
				}

				if (Color.green(pixel) < ming) {
					ming = Color.green(pixel);
				}

				if (Color.blue(pixel) < minb) {
					minb = Color.blue(pixel);
				}
			}
		}

		// last 5 rows
		for (int y = bmp.getHeight() - 1; y > bmp.getHeight() - 6; y--) {
			for (int x = 0; x < bmp.getWidth(); x++) {

				int pos = y * bmp.getWidth() + x;

				int pixel = rgbpixels[pos];

				if (Color.red(pixel) > maxr) {
					maxr = Color.red(pixel);
				}

				if (Color.green(pixel) > maxg) {
					maxg = Color.green(pixel);
				}

				if (Color.blue(pixel) > maxb) {
					maxb = Color.blue(pixel);
				}

				if (Color.red(pixel) < minr) {
					minr = Color.red(pixel);
				}

				if (Color.green(pixel) < ming) {
					ming = Color.green(pixel);
				}

				if (Color.blue(pixel) < minb) {
					minb = Color.blue(pixel);
				}
			}
		}

		// first 5 columns
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < bmp.getHeight(); y++) {

				int pos = y * bmp.getWidth() + x;

				int pixel = rgbpixels[pos];

				if (Color.red(pixel) > maxr) {
					maxr = Color.red(pixel);
				}

				if (Color.green(pixel) > maxg) {
					maxg = Color.green(pixel);
				}

				if (Color.blue(pixel) > maxb) {
					maxb = Color.blue(pixel);
				}

				if (Color.red(pixel) < minr) {
					minr = Color.red(pixel);
				}

				if (Color.green(pixel) < ming) {
					ming = Color.green(pixel);
				}

				if (Color.blue(pixel) < minb) {
					minb = Color.blue(pixel);
				}
			}
		}

		// last 5 columns
		for (int x = bmp.getWidth() - 1; x > bmp.getWidth() - 6; x--) {
			for (int y = 0; y < bmp.getHeight(); y++) {

				int pos = y * bmp.getWidth() + x;

				int pixel = rgbpixels[pos];

				if (Color.red(pixel) > maxr) {
					maxr = Color.red(pixel);
				}

				if (Color.green(pixel) > maxg) {
					maxg = Color.green(pixel);
				}

				if (Color.blue(pixel) > maxb) {
					maxb = Color.blue(pixel);
				}

				if (Color.red(pixel) < minr) {
					minr = Color.red(pixel);
				}

				if (Color.green(pixel) < ming) {
					ming = Color.green(pixel);
				}

				if (Color.blue(pixel) < minb) {
					minb = Color.blue(pixel);
				}
			}
		}

		// we have minr, maxr, ming, maxg, minb, maxb.
		// we now threshold the image based on these values

		for (int y = 0; y < bmp.getHeight(); y++) {
			for (int x = 0; x < bmp.getWidth(); x++) {

				// y is row number, x is column number. We proceed row-wise
				int pos = y * bmp.getWidth() + x;
				int pixel = rgbpixels[pos];
				int r = Color.red(pixel);
				int g = Color.green(pixel);
				int b = Color.blue(pixel);

				if ((r >= minr && r <= maxr) && (g >= ming && g <= maxg)
						&& (b >= minb && r <= maxb)) {
					rgbpixels[pos] = Color.BLACK;
				}
			}
		}

		// set tbmp to modified pixel array
		tbmp.setPixels(rgbpixels, 0, width, 0, 0, width, height);
	}

	private void mFilter() {

		// median filtering to remove salt and pepper noise
		MedianFilter mf = new MedianFilter(3);
		mf.filter(tbmp, tbmp);

		// making almost black pixels completely BLACK
		for (int i = 0; i < rgbpixels.length; i++) {

			int r = Color.red(rgbpixels[i]);
			int g = Color.green(rgbpixels[i]);
			int b = Color.blue(rgbpixels[i]);

			if (r < 50 && g < 50 && b < 50 && Math.abs(r - g) < 20
					&& Math.abs(r - b) < 20 && Math.abs(g - b) < 20) {
				rgbpixels[i] = Color.BLACK;
			}
		}
	}

	private void getBounds() {
		// find boundary limits
		TOP = findLimit(1);
		BOTTOM = findLimit(2);
		LEFT = findLimit(3);
		RIGHT = findLimit(4);

		System.out.println("Top: " + TOP);
		System.out.println("Bottom: " + BOTTOM);
		System.out.println("Left: " + LEFT);
		System.out.println("Right: " + RIGHT);

		objwidth = RIGHT - LEFT;
		objheight = BOTTOM - TOP;

		// draw a white outline
		int x = TOP;
		for (int y = LEFT; y <= RIGHT; y++) {
			int pos = x * width + y;
			// rgbpixels[pos] = Color.WHITE;

			if (TOP > 5) {
				rgbpixels[pos - width] = Color.WHITE;
				rgbpixels[pos - (2 * width)] = Color.WHITE;
			}

		}

		x = BOTTOM;
		for (int y = LEFT; y <= RIGHT; y++) {
			int pos = x * width + y;
			// rgbpixels[pos] = Color.WHITE;

			if (BOTTOM < height - 5) {
				rgbpixels[pos + width] = Color.WHITE;
				rgbpixels[pos + (2 * width)] = Color.WHITE;
			}
		}

		x = LEFT;
		for (int y = TOP; y <= BOTTOM; y++) {
			int pos = y * width + x;
			// rgbpixels[pos] = Color.WHITE;

			if (LEFT > 5) {
				rgbpixels[pos - 1] = Color.WHITE;
				rgbpixels[pos - 2] = Color.WHITE;
			}
		}

		x = RIGHT;
		for (int y = TOP; y <= BOTTOM; y++) {
			int pos = y * width + x;
			// rgbpixels[pos] = Color.WHITE;

			if (RIGHT < width - 5) {
				rgbpixels[pos + 1] = Color.WHITE;
				rgbpixels[pos + 2] = Color.WHITE;
			}
		}

		// set tbmp to modified pixel array
		tbmp.setPixels(rgbpixels, 0, width, 0, 0, width, height);
	}

	private void getColor() {

		// convert from RGB to HSV format
		for (int i = 0; i < rgbpixels.length; i++) {
			Color.colorToHSV(rgbpixels[i], hsvpixels[i]);
		}

		// run while loop for all pixels, and check for their hues to find the
		// most dominant color
		int i = 0;
		while (i < (width * height)) {

			float h = hsvpixels[i][0];
			float s = hsvpixels[i][1];
			float v = hsvpixels[i][2];

			if (v > 0.1) {

				if (s < 0.1 && v >= 0.3 && v < 0.6)
					colorarray[GRAY]++;

				else if (s < 0.1 && v >= 0.6)
					colorarray[WHITE]++;

				else if (h <= 12 && s > 0.35 && v > 0.30)
					colorarray[RED]++;

				else if (h > 12 && h <= 42 && s > 0.35 && v > 0.30)
					colorarray[ORANGE]++;

				else if (h > 42 && h <= 68 && s > 0.35 && v > 0.30)
					colorarray[YELLOW]++;

				else if (h > 68 && h <= 148 && s > 0.35 && v > 0.30)
					colorarray[GREEN]++;

				else if (h > 148 && h <= 240 && s > 0.35 && v > 0.30)
					colorarray[BLUE]++;

				else if (h > 240 && h <= 285 && s > 0.35 && v > 0.30)
					colorarray[VIOLET]++;

				else if (h > 285 && h <= 342 && s > 0.35 && v > 0.30)
					colorarray[PINK]++;

				else if (h > 342 && s > 0.35 && v > 0.30)
					colorarray[RED]++;
			}
			i++;
		}

		// find out most dominant color
		int max = colorarray[0];
		int pointer = 0;
		for (int x = 1; x < 9; x++) {
			if (colorarray[x] > max) {
				max = colorarray[x];
				pointer = x;
			}
		}
		ObjAttr[0] = ColorList[pointer];
	}

	private void getSize() {

		// find total area of object
		for (int y = TOP; y <= BOTTOM; y++) {
			for (int x = LEFT; x <= RIGHT; x++) {
				int pos = y * width + x;
				if (rgbpixels[pos] != Color.BLACK)
					area++;
			}
		}

		// assign size descriptor based on area
		String temp = null;
		if (area <= 7500)
			temp = SizeList[0];

		else if (area > 7500 && area <= 20000)
			temp = SizeList[1];

		else if (area > 20000 && area <= 80000)
			temp = SizeList[2];

		else if (area > 80000)
			temp = SizeList[3];

		ObjAttr[1] = temp;

		System.out.println("Area of object: " + area);

	}

	private void getShape() {

		float a = (float) (objwidth) * (objheight);
		float ratio = (float) objwidth / objheight;
		System.out.println("Object height - " + objheight);
		System.out.println("Object width - " + objwidth);
		System.out.println("w/h ratio - " + ratio);
		System.out.println("Area of boundary - " + a);

		if (ratio <= 0.25 || ratio >= 6) {
			// oblong
			ObjAttr[2] = ShapeList[4];
			
			float t = Math.max(objwidth, objheight);
			
			//override size if shape is oblong
			if(t<=350)
				//small (chalk)
				ObjAttr[1] = SizeList[1];
			
			else if(t>350 && t<=600)
				//medium (pen)
				ObjAttr[1] = SizeList[2];
			
			else
				//large (Bamboo :D)
				ObjAttr[1] = SizeList[3];
		}

		else if (area >= 0.80 * a) {
			// its a quadrilateral

			if (ratio >= 0.9 && ratio <= 1.1) {
				// its a square
				ObjAttr[2] = ShapeList[0];
			}

			else
				// its a rectangle
				ObjAttr[2] = ShapeList[1];
		}

		else if (area < 0.80 * a && area >= 0.65 * a)
			ObjAttr[2] = ShapeList[2];

		else if (area < 0.65 * a && area >= 0.45 * a)
			ObjAttr[2] = ShapeList[3];
		
		else
			//random
			ObjAttr[2] = ShapeList[5];

	}

	private void getShape1() {
		
		double max = 0;
		double mean = 0;
		double variance = 0;
		double stddev = 0;
		int lim[][] = new int [BOTTOM - TOP + 1][2];
		
		for (int y = TOP; y <= BOTTOM; y++) {
			
			int x = LEFT;
			
			//loop for finding 'left'
			for (x = LEFT; x <= RIGHT; x++) {
				int pos = y * width + x;
				
				int f3=0, f4=0;
				
				
				if (y > 5 && y < width - 6) {
					if (rgbpixels[pos] != Color.BLACK) {
						for (int i = 0; i < 5; i++) {

							if (rgbpixels[pos - i] == Color.BLACK)
								f3++;
							else
								f3 = 0;

							if (rgbpixels[pos + i] != Color.BLACK)
								f4++;
							else
								f4 = 0;

						}
					}
				}

				//get left limit of current row
				if (f3 >= 2 && f4 >= 2) {
					lim[y-TOP][0] = x;
					break;
				}
				else
					f3 = f4 = 0;
			}//end of first inner for loop
			
			//if the current row does not contain a single non-BLACK pixel
			if(x==RIGHT) {
				lim[y-TOP][0] = 0;
				lim[y-TOP][1] = 0;
				continue;
			}
			
			x=RIGHT;
			
			//loop for finding 'right'
			for(x = RIGHT; x >= LEFT; x--) {
				
				int pos = y * width + x;
				int f3=0, f4=0;
				
				if (y < bmp.getWidth() - 6) {
					if (rgbpixels[pos] != Color.BLACK) {
						for (int i = 0; i < 5; i++) {

							if (rgbpixels[pos - i] != Color.BLACK)
								f3++;
							else
								f3 = 0;

							if (rgbpixels[pos + i] == Color.BLACK)
								f4++;
							else
								f4 = 0;

						}
					}
				}
				
				//get 'right' limit of current row
				if (f3 >= 2 && f4 >= 2)
					lim[y-TOP][1] = x;
				else
					f3 = f4 = 0;

				
			}//end of second inner for loop
			
		}//end of outer for loop
		
		//get difference of 'left' and 'right'
		int a[] = new int[lim.length];
		
		for(int i = 0; i<lim.length; i++) {
			a[i] = lim[i][1] - lim[i][0];
		}
		
		//find max
		for(int i=0; i<a.length; i++)
            if(a[i]>max) max=a[i];
        System.out.println("Max = "+max);
        
        //find mean
        double sum=0.0;
        for(int i=0; i<a.length;i++)
            sum=sum+a[i];
        mean = sum/a.length;
        System.out.println("Mean = "+mean);
        
        //find variance
        sum = 0.0;
        for(int i=0; i<a.length;i++)
            sum+=(a[i]-mean)*(a[i]-mean);
        variance = sum/(a.length);
        System.out.println("Variance = "+variance);
        
        //find stddev
        stddev = Math.sqrt(variance);
        System.out.println("Standard deviation = "+stddev);
        
        if (max==mean && objheight==max)
            System.out.println("The shape is a square.");
        
        else if (max==mean && objheight!=max)
            System.out.println("The shape is a rectangle.");
        
        else if (max==objheight && stddev!=variance)
            System.out.println("The shape is a circle.");
        else if(.5*max<mean && objheight>max)
            System.out.println("The shape is a triangle.");
        else if(mean!=max && (1/3)*max==(1/2)*objheight)
            System.out.println("The shape is a polygon.");
        else
            System.out.println("The shape is unknown.");
	}
	
	private int findLimit(int mode) {

		// TOP LIMIT
		if (mode == 1) {

			int f3 = 0, f4 = 0;

			for (int y = 0; y < bmp.getHeight(); y++) {
				for (int x = 0; x < bmp.getWidth(); x++) {
					// y is row number, x is column number. We proceed row-wise
					int pos = y * bmp.getWidth() + x;

					if (y > 5 && y < height - 6) {
						if (rgbpixels[pos] != Color.BLACK) {
							for (int i = 0; i < 5; i++) {

								if (rgbpixels[pos + (i * width)] != Color.BLACK)
									f3++;
								else
									f3 = 0;

								if (rgbpixels[pos - (i * width)] == Color.BLACK)
									f4++;
								else
									f4 = 0;

							}
						}
					}

					if (f3 >= 3 && f4 >= 3)
						return y;
					else
						f3 = f4 = 0;
				}
			}

			return 0;
		}

		// BOTTOM LIMIT
		else if (mode == 2) {

			int f3 = 0, f4 = 0;

			for (int y = bmp.getHeight() - 1; y >= 0; y--) {
				for (int x = 0; x < bmp.getWidth(); x++) {

					// y is row number, x is column number. We proceed row-wise
					int pos = y * bmp.getWidth() + x;

					if (y < bmp.getHeight() - 6) {
						if (rgbpixels[pos] != Color.BLACK) {
							for (int i = 0; i < 5; i++) {

								if (rgbpixels[pos + (i * width)] == Color.BLACK)
									f3++;
								else
									f3 = 0;

								if (rgbpixels[pos - (i * width)] != Color.BLACK)
									f4++;
								else
									f4 = 0;

							}
						}
					}

					if (f3 >= 3 && f4 >= 3)
						return y;
					else
						f3 = f4 = 0;
				}
			}

			return height;
		}

		// LEFT LIMIT
		else if (mode == 3) {

			int f3 = 0, f4 = 0;

			for (int y = 0; y < bmp.getWidth(); y++) {
				for (int x = 0; x < bmp.getHeight(); x++) {

					// y is column number, x is row number. We proceed
					// column-wise
					int pos = x * bmp.getWidth() + y;

					if (y > 5 && y < width - 6) {
						if (rgbpixels[pos] != Color.BLACK) {
							for (int i = 0; i < 5; i++) {

								if (rgbpixels[pos - i] == Color.BLACK)
									f3++;
								else
									f3 = 0;

								if (rgbpixels[pos + i] != Color.BLACK)
									f4++;
								else
									f4 = 0;

							}
						}
					}

					if (f3 >= 3 && f4 >= 3)
						return y;
					else
						f3 = f4 = 0;

				}
			}

			return 0;
		}

		// RIGHT LIMIT
		else if (mode == 4) {

			int f3 = 0, f4 = 0;

			for (int y = bmp.getWidth() - 1; y >= 0; y--) {

				for (int x = 0; x < bmp.getHeight(); x++) {

					// y is column number, x is row number. We proceed
					// column-wise
					int pos = x * bmp.getWidth() + y;

					if (y < bmp.getWidth() - 6) {
						if (rgbpixels[pos] != Color.BLACK) {
							for (int i = 0; i < 5; i++) {

								if (rgbpixels[pos - i] != Color.BLACK)
									f3++;
								else
									f3 = 0;

								if (rgbpixels[pos + i] == Color.BLACK)
									f4++;
								else
									f4 = 0;

							}
						}
					}

					if (f3 >= 3 && f4 >= 3)
						return y;
					else
						f3 = f4 = 0;

				}
			}

			return width;
		}
		return 0;
	}

	class SetUp extends AsyncTask<Void, Void, Void> {
		// runs in background without clogging the UI thread

		protected void onPreExecute() {
			// show dialog
			LoadDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			// get the original bitmap
			getBmp();

			// initialize variables
			initVar();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			// setting the original image in the ImageView, setting up content
			// in TextView and Button
			ObjImage.setImageBitmap(bmp);
			identify.setText("Extract Attributes");
			tvImageInfo.setText("Original Image");

			// make views visible
			ObjImage.setVisibility(View.VISIBLE);
			identify.setVisibility(View.VISIBLE);
			identify.setClickable(true);
			tvImageInfo.setVisibility(View.VISIBLE);

			// remove dialog
			LoadDialog.dismiss();
		}
	}

	class AttemptIdentification extends AsyncTask<Void, Void, Void> {
		// runs in background without clogging the UI thread
		ProgressDialog IDprogress;
		final String ProgMsgs[] = { "A little housekeeping",
				"Extracting color", "Extracting size", "Extracting shape",
				"Attribute extraction complete" };
		int p = 0;

		protected void onPreExecute() {

			// make views invisible
			ObjImage.setVisibility(View.INVISIBLE);
			identify.setVisibility(View.INVISIBLE);
			identify.setClickable(false);
			tvImageInfo.setVisibility(View.INVISIBLE);

			// show progress dialog
			IDprogress = new ProgressDialog(ImageMenu.this);
			IDprogress.setTitle("Attribute extraction in progress");
			IDprogress.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			// isolate object
			publishProgress((Void) null);
			isolateObject();
			mFilter();

			// get boundaries
			getBounds();
			p++;

			// find color of object
			publishProgress((Void) null);
			getColor();
			p++;

			// find size of object
			publishProgress((Void) null);
			getSize();
			p++;

			// find shape of object
			publishProgress((Void) null);
			getShape();
			p++;

			// Sys out attributes
			System.out.println("Color: " + ObjAttr[0]);
			System.out.println("Size: " + ObjAttr[1]);
			System.out.println("Shape: " + ObjAttr[2]);

			// Completion
			publishProgress((Void) null);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onProgressUpdate(Void... progress) {
			IDprogress.setMessage(ProgMsgs[p]);
		}

		@Override
		protected void onPostExecute(Void result) {
			// set tbmp to modified pixels
			ObjImage.setImageBitmap(tbmp);
			identify.setText("Attempt Identification");
			tvImageInfo.setText("Isolated object");

			// make views visible
			ObjImage.setVisibility(View.VISIBLE);
			identify.setVisibility(View.VISIBLE);
			identify.setClickable(true);
			tvImageInfo.setVisibility(View.VISIBLE);

			// dismiss progress dialog
			IDprogress.dismiss();
			
			//display extracted attributes
			Toast att = Toast.makeText(getApplicationContext(), ObjAttr[0]+"  "+ObjAttr[1]+"  "+ObjAttr[2], Toast.LENGTH_LONG);
			att.show();
		}
	}
}