package com.example.intellicam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

public class BTService extends Service {

	// Name and UUID for SDP records
	private static final String MY_NAME = "BluetoothListener";
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// AsyncTask object
	BTListen task;
	
	String response;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		Toast.makeText(this, "Bluetooth Listening", Toast.LENGTH_SHORT).show();
		System.out.println("Service created");

		// use an AsyncTask to do BT stuff
		task = new BTListen();
		task.execute();
	}

	//check for object in KB
	private boolean searchKB(String q) {
		
		String attr[] = q.split(" ");
		String temp = null;
		try {
			// check in KB, get null if object not found
			Knowledgebase check = new Knowledgebase(this);
			check.open();
			temp = check.searchObject(attr[0], attr[1], attr[2]);
			check.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		// if object found in KB, return true, else false
		if (temp != null) {
			response = temp;
			return true;
		}
		return false;
	}
	
	class BTListen extends AsyncTask<Void, Void, Void> {

		// BT comm. variables
		BluetoothAdapter btAdapter;
		BluetoothServerSocket BTServerSocket = null;
		BluetoothSocket socket = null;
		InputStream mInputStream = null;
		OutputStream mOutputStream = null;

		// String for incoming query
		String query = null;
		
		//flag
		boolean foundInKB = false;

		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void... params) {

			// wait for adapter to be enabled
			waitForAdapter();

			while (true) {
				
				response = "NOT_FOUND";
				System.out.println("Listening...");
				
				// create a listening server socket
				createListeningSocket();

				// listen to server socket
				listenToSS();

				if (socket != null) {
					
					// establish the connection if a connection was accepted
					establishConnection();

					// read stuff from the InputStream
					readData();
					
					if(query != null) {
						foundInKB = searchKB(query);
					}

					//respond
					sendData();
					
					//clean up stuff
					cleanUp();
				}
				
				else break;

			}

			return null;

		}

		// wait until adapter is enabled
		protected void waitForAdapter() {

			// get default adapter
			btAdapter = BluetoothAdapter.getDefaultAdapter();

			// wait for adapter to be enabled
			while (!btAdapter.isEnabled())
				try {
					System.out.println("Waiting...");
					Thread.sleep(3000);
				} catch (Exception e) {
					// cancel task if sleep interrupted
					task.cancel(true);
				}
		}

		// create a server listening socket
		protected void createListeningSocket() {

			BluetoothServerSocket tmp = null;

			try {
				tmp = btAdapter.listenUsingRfcommWithServiceRecord(MY_NAME,
						MY_UUID);
				System.out.println("Created listening socket.");
			}

			catch (IOException e) {
				e.printStackTrace();
				System.out.println("Couldnt create listening socket.");
			}
			BTServerSocket = tmp;
		}

		// listen to Server Socket
		protected void listenToSS() {
			// listen to the server socket if not connected
			System.out.println("Listening to server socket...");
			while (socket == null) {
				try {
					// will only return on a successful connection or exception
					socket = BTServerSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}

				if (socket != null) {
					System.out.println("Connection found!");
				}
			}
		}

		// establish connection if connection was accepted
		protected void establishConnection() {
			if (socket != null) {

				// get input and output streams of socket
				InputStream tis = null;
				OutputStream tos = null;
				System.out.println("Getting i/o streams.");
				try {
					tis = socket.getInputStream();
					tos = socket.getOutputStream();
					System.out.println("Got i/o streams.");
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Couldnt get i/o streams.");
				}

				mInputStream = tis;
				mOutputStream = tos;

			}
		}

		// read data from InputStream
		protected void readData() {
			byte[] buffer = new byte[1024];
			int size=0;
			// read into buffer, and store length of data into bytes
			System.out.println("Reading data...");
			try {
				size = mInputStream.read(buffer);
				System.out.println("Data read");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Couldnt read data.");
			}

			// convert byte array to string
			String t = new String(buffer, 0, size);
			query = t;
		}

		// respond to sender
		protected void sendData() {
			String msg = response;
			byte[] buffer = msg.getBytes();
			System.out.println("Responding to sender...");
			try {
				mOutputStream.write(buffer);
				System.out.println("Sent '" + response + "' to sender");
			}

			catch (Exception e) {
				e.printStackTrace();
				System.out.println("Responding unsuccessful.");
			}
		}

		//clean up
		protected void cleanUp() {
			
			// clear up stuff
			try {
				if (socket != null) {
					if (mInputStream != null) {
						mInputStream.close();
						mOutputStream.close();
						mInputStream = null;
						mOutputStream = null;
					}
					socket.close();
					socket = null;
				}

				if (BTServerSocket != null) {
					BTServerSocket.close();
					BTServerSocket = null;
				}
			}

			catch (IOException e) {
				e.printStackTrace();
				System.out.println("Exception in cleanup");
			}
			
		}
		
		@Override
		protected void onPostExecute(Void result) {
			System.out.println("Stopped listening.");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// clear up stuff
		try {
			if (task.socket != null) {
				if (task.mInputStream != null) {
					task.mInputStream.close();
					task.mOutputStream.close();
				}
				task.socket.close();
			}

			if (task.BTServerSocket != null) {
				task.BTServerSocket.close();
			}
		}

		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Exception in cleanup");

		}

		// stop AsnycTask
		task.cancel(true);
		Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
	}
}