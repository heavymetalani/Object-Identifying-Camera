package com.example.intellicam;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class Splash extends Activity {

	BluetoothAdapter btAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_splash, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        turnOnBT();
    }
    
    //turn BT on
    private void turnOnBT() {
    	
    	//if BT is off, turn it on
    	if(!btAdapter.isEnabled()) {
        	Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(i, 0);
    	}
    	
    	//else, go to next activity and destroy this
    	else {
    		Intent i = new Intent(this, Home.class);
    		startActivity(i);
    		finish();
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	//if user cancelled, try again :P
    	if(resultCode==RESULT_CANCELED) {
    		Toast.makeText(getApplicationContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
    		turnOnBT();
    	}
    	
    	//if user turned on BT, go to next activity and destroy this
    	else {
    		Intent i = new Intent(this, Home.class);
    		startActivity(i);
    		finish();
    	}
    }
    
}
