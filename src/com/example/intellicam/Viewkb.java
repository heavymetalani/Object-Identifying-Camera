package com.example.intellicam;

import android.os.Bundle;
import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class Viewkb extends Activity {
	// this class extends ListActivity, because we want the entries in a list

	// contains entries from KB
	TableLayout myTable;
	float tvsize;
	String entries[];
	boolean entryExist = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewkb);
		
		//find table layout defined in xml file
		myTable = (TableLayout) findViewById(R.id.tlTable);
		
		//initialize size of textview to be used in putrows
		TextView temptv = (TextView) findViewById(R.id.tvKBColor);
		tvsize = temptv.getTextSize();
		
		//get the entries from the KB into the entries array
		getData();
		
		//if KB contains entries, put rows in table for display
		if(entryExist)
			putRows();
		
		//else display "empty KB toast
		else {
			Toast t = Toast.makeText(getApplicationContext(), "Knowledgebase is empty. Need to learn!", Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		}
			
	}

	private void getData() {

		// get data from Knowledgebase
		try {
			Knowledgebase info = new Knowledgebase(this);
			info.open();

			// the String data contains all the entries from the KB seperated by
			// \n
			String data = info.getData();
			
			info.close();
			
			//if KB is empty, don't do anything
			if(data.equals("")) {
				entryExist = false;
				return;
			}

			// splitting data into separate entries
			if (data.contains("\n")) {
				
				// if multiple entries in KB, separate them by \n
				entries = data.split("\n");
			}

			// single entry in KB
			else {
				entries = new String[1];
				entries[0] = data;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void putRows() {
		
		//put rows in the table layout dynamically
		
		for(int i = 0; i<entries.length; i++) {
			
			//split up entry into columns
			String cols[] = entries[i].split(" ");
			
			//set up 4 text views for 4 columns of each row
			TextView obj = new TextView(this);
			TextView color = new TextView(this);
			TextView size = new TextView(this);
			TextView shape = new TextView(this);
			
			//set text of each column
			obj.setText(cols[0]);
			color.setText(cols[1]);
			size.setText(cols[2]);
			shape.setText(cols[3]);
			
			//set size of text to 18sp
			obj.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
			color.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
			size.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
			shape.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
			
			//set gravity to centre text
			obj.setGravity(Gravity.CENTER_HORIZONTAL);
			color.setGravity(Gravity.CENTER_HORIZONTAL);
			size.setGravity(Gravity.CENTER_HORIZONTAL);
			shape.setGravity(Gravity.CENTER_HORIZONTAL);
			
			//create a new table row
			TableRow tr = new TableRow(this);
			
			//add columns to the row
			tr.addView(obj);
			tr.addView(color);
			tr.addView(size);
			tr.addView(shape);
			
			//get params for each column text view
			TableRow.LayoutParams colparams = (TableRow.LayoutParams)obj.getLayoutParams();
			
			//set width, height, wieght and gravity of params
			colparams.width = 0;
			colparams.height = TableRow.LayoutParams.WRAP_CONTENT;
			colparams.weight = 1;
			
			//update the layout params
			obj.setLayoutParams(colparams);
			color.setLayoutParams(colparams);
			size.setLayoutParams(colparams);
			shape.setLayoutParams(colparams);
			
			//set background gray for every even numbered row
			if(i%2!=0) {
				tr.setBackgroundColor(0xfff0f0f0);
			}
			
			//row setup is complete here
			
			//add the row into the table
			myTable.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
		} //repeat for as many entries as are present
	}
}
