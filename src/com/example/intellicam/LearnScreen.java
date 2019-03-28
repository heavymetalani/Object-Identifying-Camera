package com.example.intellicam;

import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LearnScreen extends Activity {
	
	//UI elements
	EditText etObjectName;
	TextView tvColor, tvSize, tvShape;
	Button bEnter;
	String attr[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_screen);
      
        //initialization
        initVar();
    }

    private void initVar() {
    	//initialize variables
    	
    	// get String array from intent
    	Bundle b = getIntent().getExtras();
    	attr = b.getStringArray("Attributes");
        
        //initialize UI elements
        etObjectName = (EditText) findViewById(R.id.etObjectName);
        tvColor = (TextView) findViewById(R.id.tvColor);
        tvSize = (TextView) findViewById(R.id.tvSize);
        tvShape = (TextView) findViewById(R.id.tvShape);
        bEnter = (Button) findViewById(R.id.bEnterData);
        
        //set text for respective attributes
        tvColor.setText("Color : " + attr[0]);
        tvSize.setText("Size : " + attr[1]);
        tvShape.setText("Shape : " + attr[2]);
	}
    
    public void insertData(View view) {
    	//called when button is clicked
    	
    	//get the text that the user has entered
    	String name = etObjectName.getText().toString();
    	
		//insert object name and attributes in KB
    	boolean sflag = true; 
    	try { 
    		// open DB, insert entry, and close it 
    		Knowledgebase insertEntry = new Knowledgebase(this);
    		insertEntry.open();
    		insertEntry.createEntry(name, attr[0], attr[1], attr[2]);
    		insertEntry.close();
    		
    	}
    	
    	catch (Exception e) {
    		sflag = false;
    	}
    	finally {
    		//display success toast
    		if(sflag) {
    			Toast t = Toast.makeText(getApplicationContext(), "System Learning Successful", Toast.LENGTH_LONG);
    			t.setGravity(Gravity.CENTER, 0, 0);
    			t.show();
    		}
    	}
    	
    	
    	finish();
    }
    

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_learn_screen, menu);
        return true;
    }

    
}
