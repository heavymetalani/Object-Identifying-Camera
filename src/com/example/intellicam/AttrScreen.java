package com.example.intellicam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AttrScreen extends Activity {

	TextView tvObjectName;
	Button bIncorrect;
	String attr[];
	String objectName = null;
	String msg = null;
	boolean foundInKB = false, match3 = false, matchSS = false,
			matchCS = false, many = false;

	// Bluetooth stuff
	BluetoothAdapter btAdapter;
	Boolean NotAlone = false;
	BluetoothDevice[] DeviceArray;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Bluetooth communication variables
	BluetoothSocket mSocket = null;
	BluetoothDevice mDevice;
	InputStream mInputStream = null;
	OutputStream mOutputStream = null;

	BTSend task = null;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_attr_screen, menu);
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attr_screen);

		// initialize the variables
		initVar();

		// check if object has been 'seen' before
		System.out.println("Checking KB");
		isFound();

		// if object found, display
		if (foundInKB) {
			displayText();
		}

		// else check if device is paired with any peers
		else if (NotAlone) {
			tryPeers();
		}

		// else go to next activity to learn from user
		else {
			proceedAhead();
		}
	}

	private void initVar() {
		// INITIALIZE ALL VARIABLES

		// set up UI elements
		tvObjectName = (TextView) findViewById(R.id.tvObjectName);
		bIncorrect = (Button) findViewById(R.id.bIncorrect);

		// get String array from intent
		Bundle b = getIntent().getExtras();
		attr = b.getStringArray("Attributes");

		// get default BT adapter
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		// get paired devices
		getPairedDevices();
	}

	private void getPairedDevices() {
		// get set of paired devices
		Set<BluetoothDevice> PairedDevices;
		PairedDevices = btAdapter.getBondedDevices();

		// if set is not empty, copy the devices from the set into an array
		if (PairedDevices.size() > 0) {
			NotAlone = true;
			DeviceArray = new BluetoothDevice[PairedDevices.size()];
			int i = 0;
			String t = "";
			for (BluetoothDevice device : PairedDevices) {
				DeviceArray[i] = device;
				i++;
				t = t + device.getName() + " " + device.getAddress() + "\n";
			}
			System.out.println(t);
		}
	}

	private void isFound() {
		// CHECK FOR AVAILABILITY

		String temp = null;
		try {

			Knowledgebase check = new Knowledgebase(this);
			check.open();

			// check for color, size and shape. Get null if no entry found.
			temp = check.searchObject(attr[0], attr[1], attr[2]);

			check.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// if match found for all 3 attributes
		if (temp != null) {
			foundInKB = true;
			match3 = true;
			objectName = temp;
		}

		// else try for size and shape match
		else {

			try {
				Knowledgebase check = new Knowledgebase(this);
				check.open();
				
			// check for size and shape. Get null if no entry found.
			temp = check.searchObjectSizeShape(attr[1], attr[2]);
			
			check.close();
			}
			
			catch (Exception e) {
				e.printStackTrace();
			}
			
			// if match(es) found for size and shape
			if (temp != null) {
				foundInKB = true;
				matchSS = true;

				// for multiple entries
				if (temp.contains(" ")) {
					many = true;
				}

				objectName = temp;
			}

			// else try for color and shape (scaling)
			else {
				
				try {
					Knowledgebase check = new Knowledgebase(this);
					check.open();
					
				// check for size and shape. Get null if no entry found.
				temp = check.searchObjectColorShape(attr[0], attr[2]);
				
				check.close();
				}
				
				catch (Exception e) {
					e.printStackTrace();
				}

				// if match(es) found for color and shape
				if (temp != null) {
					foundInKB = true;
					matchCS = true;

					// for multiple entries
					if (temp.contains(" ")) {
						many = true;
					}

					objectName = temp;
				}
			}
		}
	}

	private void proceedAhead() {

		// go to LEARN SCREEN

		Toast t = Toast.makeText(getApplicationContext(),
				"Unidentified Object. Please, Make me learn about it.",
				Toast.LENGTH_LONG);
		t.show();

		System.out.println("Unidentified object");

		// put attribute array in a bundle
		Bundle b = new Bundle();
		b.putStringArray("Attributes", attr);

		// create intent, and pass the bundle to next activity
		Intent a = new Intent("com.example.intellicam.LEARNSCREEN");
		a.putExtras(b);
		startActivity(a);
		finish();
	}

	private void tryPeers() {
		msg = attr[0] + " " + attr[1] + " " + attr[2];
		task = new BTSend();
		task.execute();
	}

	private void displayText() {

		String text = "";
		String manyobj[];

		// perfect match
		if (match3) {
			text = "Object identified as " + objectName;
		}

		// if size and shape match
		else if (matchSS) {

			text = "Color does not match. Object could possibly be ";

			// if multiple entries exist
			if (many) {
				manyobj = objectName.split(" ");

				text = text + "either ";

				int i = 0;
				while (i < manyobj.length) {
					text = text + manyobj[i];

					if (i < manyobj.length - 1)
						text = text + " or ";

					i++;
				}
			}

			else
				text = text + objectName;

		}

		// if color and size match
		else if (matchCS) {

			text = "Size does not match. Object could possibly be a scaled version of ";

			// if multiple entries exist
			if (many) {
				manyobj = objectName.split(" ");

				text = text + "either ";

				int i = 0;
				while (i < manyobj.length) {
					text = text + manyobj[i];

					if (i < manyobj.length - 1)
						text = text + " or ";

					i++;
				}
			}

			else
				text = text + objectName;
		}
		
		text = text + ".";

		//set UI fields
		this.setTitle("Positive ID on object");
		bIncorrect.setVisibility(Button.VISIBLE);
		tvObjectName.setText(text);
		System.out.println("Object = " + text);
	}

	public void incorrectPred(View view) {
		proceedAhead();
	}
	
	// overloaded method for peer message
	private void displayText(String text) {
		this.setTitle("Positive ID on object");
		text = text + ".";
		tvObjectName.setText(text);
		System.out.println("Object = " + text);
	}

	private void updateKB(String Objname) {
		// insert object name and attributes in KB
		boolean sflag = true;
		try {
			// open DB, insert entry, and close it
			Knowledgebase insertEntry = new Knowledgebase(this);
			insertEntry.open();
			insertEntry.createEntry(Objname, attr[0], attr[1], attr[2]);
			insertEntry.close();

		}

		catch (Exception e) {
			sflag = false;
		} finally {
			// display success toast
			if (sflag) {
				Toast t = Toast
						.makeText(
								getApplicationContext(),
								"Knowledge share karne se badhta hai. Peer learning successful.",
								Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
			}
		}
	}

	class BTSend extends AsyncTask<Void, String, Void> {
		String resp = null;

		ProgressDialog BTProgress;

		protected void onPreExecute() {

			resp = "NOT_FOUND";

			// show progress dialog
			BTProgress = new ProgressDialog(AttrScreen.this);
			BTProgress.setTitle("Establishing connection with peers");
			BTProgress.show();
			BTProgress.setMessage("Initiating connection");
		}

		@Override
		protected Void doInBackground(Void... params) {

			int i = 0;
			// try to connect to all paired devices one by one
			while (i < DeviceArray.length) {

				mDevice = DeviceArray[i];
				i++;
				BluetoothSocket tmp = null;

				// get a BluetoothSocket for a connection to BluetoothDevice
				try {
					// use reflection to create a socket
					tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Couldn't create socket to connect to "
							+ mDevice.getName());

					// clean up before trying for next device
					cleanUp();
					continue;
				}

				mSocket = tmp;

				if (mSocket != null) {
					// connect to BluetoothSocket
					try {
						mSocket.connect();
					} catch (IOException e) {
						// Close the socket
						System.out.println("Couldn't connect to "
								+ mDevice.getName());
						try {
							mSocket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						publishProgress("Couldn't connect to "
								+ mDevice.getName() + ". Trying for next peer.");
						// clean up before trying for next device
						cleanUp();
						continue;
					}

					System.out.println("Connected to " + mDevice.getName());
					publishProgress("Connected to " + mDevice.getName()
							+ ". Checking its KB.");

					// get input and output streams of socket
					getIOStreams();

					// send message
					sendMsg();

					resp = getResponse();
				}

				// clean up stuff before connecting to next device
				cleanUp();

				// check if peer could identify object
				if (resp != null) {
					if (!resp.equalsIgnoreCase("NOT_FOUND")) {
						System.out.println("Object identified by peer");
						break;
					}
				}
				publishProgress(mDevice.getName()
						+ " couldn't identify object. Trying for next peer.");
			}

			return null;

		}

		// get input and output streams of socket
		protected void getIOStreams() {
			InputStream tis = null;
			OutputStream tos = null;
			System.out.println(mDevice.getName() + " : Getting i/o streams");
			try {
				tis = mSocket.getInputStream();
				tos = mSocket.getOutputStream();
				System.out.println(mDevice.getName() + " : Got i/o streams");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(mDevice.getName()
						+ " : Couldnt get i/o streams");
			}

			mInputStream = tis;
			mOutputStream = tos;
		}

		// send message
		protected void sendMsg() {
			byte[] temp = msg.getBytes();

			System.out.println("Sending message to " + mDevice.getName());
			try {
				mOutputStream.write(temp);
				System.out.println("Message successully sent to "
						+ mDevice.getName());
			}

			catch (Exception e) {
				e.printStackTrace();
				System.out.println("Sending unsuccessful to "
						+ mDevice.getName());
			}
		}

		protected String getResponse() {
			byte[] buffer = new byte[1024];
			int size = 0;

			// read into buffer, and store length of data into bytes
			System.out.println("Reading response...");
			try {
				size = mInputStream.read(buffer);
				System.out.println("Response read.");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Couldnt read response.");
				return "NOT_FOUND";
			}

			// convert byte array to string
			String t = new String(buffer, 0, size);
			return t;
		}

		protected void cleanUp() {

			// clear up stuff
			try {

				if (mInputStream != null) {
					mInputStream.close();
					mOutputStream.close();
					mInputStream = null;
					mOutputStream = null;
				}

				if (mSocket != null) {
					mSocket.close();
					mSocket = null;
				}
			}

			catch (IOException e) {
				e.printStackTrace();
				System.out.println("Exception in cleanup");
			}
		}

		protected void onProgressUpdate(String... progress) {
			BTProgress.setMessage(progress[0]);
		}

		@Override
		protected void onPostExecute(Void result) {

			// clear up stuff
			try {

				if (mInputStream != null) {
					mInputStream.close();
					mOutputStream.close();
					mInputStream = null;
					mOutputStream = null;
				}

				if (mSocket != null) {
					mSocket.close();
					mSocket = null;
				}
			}

			catch (IOException e) {
				e.printStackTrace();
				System.out.println("Exception in cleanup");
			}

			// dismiss dialog box
			BTProgress.dismiss();

			// display object name, identify object
			if (resp != null) {
				if (!resp.equalsIgnoreCase("NOT_FOUND")) {
					displayText("Object identified as " + resp + " by "
							+ mDevice.getName());

					// insert new knowledge in own KB
					updateKB(resp);
				}

				// else learn from user
				else {
					proceedAhead();
				}
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();

		// clear up stuff
		if (task != null) {
			if (task.getStatus() == AsyncTask.Status.RUNNING) {

				// close I/O streams and socket if activity destroyed while task
				// is runnning
				try {

					if (mInputStream != null) {
						mInputStream.close();
						mOutputStream.close();
					}

					if (mSocket != null) {
						mSocket.close();
					}
				}

				catch (IOException e) {
					e.printStackTrace();
					System.out.println("Exception in cleanup");
				}

				// stop AsyncTask
				task.BTProgress.dismiss();
				task.cancel(true);
			}
		}
	}
}