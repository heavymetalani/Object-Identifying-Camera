package com.example.intellicam;

import java.io.File;
import java.io.FileOutputStream;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;

public class Home extends Activity {

	FileOutputStream fos;
	Uri outputFileUri;
	File root, MainDir;
	Bitmap bmp = null;
	final String FILENAME = "Object";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		System.out.println("Created BT listening service");
		//start BTService for listening
		startService(new Intent(getApplicationContext(), BTService.class));
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}

	private int getLastImageId() {
		// gets the ID of the picture clicked last
		final String[] imageColumns = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
		final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
		Cursor imageCursor = getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
				null, null, imageOrderBy);
		if (imageCursor.moveToFirst()) {
			int id = imageCursor.getInt(imageCursor
					.getColumnIndex(MediaStore.Images.Media._ID));
			imageCursor.close();
			return id;
		} else
			return 0;
	}

	private void removeImage(int id) {
		// deletes the image specified by id from the gallery
		ContentResolver cr = getContentResolver();
		cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				MediaStore.Images.Media._ID + "=?",
				new String[] { Long.toString(id) });
	}

	public void startCam(View view) {
		// set up new path to save output of camera
		root = new File(Environment.getExternalStorageDirectory()
				+ "/IntelliCam/");
		
		//if directory does not exist, create a new directory
		if (!root.exists()) {
			root.mkdirs();
		}
		
		MainDir = new File(root, "Object.png");
		outputFileUri = Uri.fromFile(MainDir);
		
		// setup and start camera intent
		Intent StartCamera = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		StartCamera.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(StartCamera, 0);
	}

	public void viewKB(View view) {
		Intent intent = new Intent(Home.this, Viewkb.class);
		startActivity(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// called when camera returns results
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == 0) {
			
			// deleting last picture from gallery
				int id = getLastImageId();
				removeImage(id);
				
				// go to ImageMenu Activity
				Intent i = new Intent("com.example.intellicam.IMAGEMENU");
				
				//pass location of file to next activity
				Bundle b = new Bundle();
				b.putSerializable("data", MainDir);
				i.putExtras(b);
				
				startActivity(i);
		}
	}

	@Override
    public void onDestroy() {
          super.onDestroy();
          
        //stop service
      	stopService(new Intent(getApplicationContext(), BTService.class));
    }
}

