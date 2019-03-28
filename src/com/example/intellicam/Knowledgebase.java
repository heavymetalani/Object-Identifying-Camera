package com.example.intellicam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//this class creates and works with the database
public class Knowledgebase {
	
	//set up database variables 
	public static final String KEY_ROWID = "_id";
	public static final String KEY_OBJECT = "object_name";
	public static final String KEY_COLOR = "object_color";
	public static final String KEY_SIZE = "object_size";
	public static final String KEY_SHAPE = "object_shape";
	
	private static final String DB_NAME = "Knowledgebase";
	private static final String DB_TABLE = "ObjectTable";
	private static final int DB_VERSION = 1;
	
	private DBHelper ourHelper;
	private final Context ourContext;
	private SQLiteDatabase ourDB;
	
	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// this function is called only once, when the DB is created for the first time
			
			//create table command
			db.execSQL("CREATE TABLE " + DB_TABLE + " (" +
					KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_OBJECT + " TEXT NOT NULL, " +
					KEY_COLOR + " TEXT NOT NULL, " +
					KEY_SIZE + " TEXT NOT NULL, " +
					KEY_SHAPE + " TEXT NOT NULL);"
					);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//used to drop a table from the database
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
			onCreate(db);
		}
		
	}
	
	public Knowledgebase(Context c) {
		//constructor. Gets the context of the calling activity and assigns it to ourContext
		ourContext = c;
	}
	
	public Knowledgebase open() throws Exception {
		
		//open database to read/write into it
		ourHelper = new DBHelper(ourContext);
		ourDB = ourHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		
		//close database
		ourHelper.close();
	}

	
	public long createEntry(String object, String color, String size, String shape) {
		//INSERT ENTRY IN DATABASE
		
		//set up a COntentValues object which holds the values for each column of an entry
		ContentValues cv = new ContentValues();
		cv.put(KEY_OBJECT, object);
		cv.put(KEY_COLOR, color);
		cv.put(KEY_SIZE, size);
		cv.put(KEY_SHAPE, shape);
		
		//insert cv into DB_TABLE. Returns ID of the newly inserted row
		 return ourDB.insert(DB_TABLE, null, cv);
	}

	public String getData() {
		
		//READ DATA FROM TABLE
		
		//use the cursor to read from the database
		String[] columns = new String[] {KEY_ROWID, KEY_OBJECT, KEY_COLOR, KEY_SIZE, KEY_SHAPE};
		Cursor c = ourDB.query(DB_TABLE, columns, null, null, null, null, null);
		String result = "";
		
		//set up int variables which store column indexes for each field
		//int iRow = c.getColumnIndex(KEY_ROWID);
		int iObject = c.getColumnIndex(KEY_OBJECT);
		int iColor = c.getColumnIndex(KEY_COLOR);
		int iSize = c.getColumnIndex(KEY_SIZE);
		int iShape = c.getColumnIndex(KEY_SHAPE);
		
		//start a loop starting with first position of cursor, which goes to next row after each iteration, until it goes beyond last entry
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			
			//get the entire data in a single string
			result = result + c.getString(iObject) + " " + 
					c.getString(iColor) + " " + c.getString(iSize) + " " + c.getString(iShape) + "\n";
		}
		return result;
	}

	public String searchObject(String color, String size, String shape) {
		//CHECK WHETHER OBJECT INFO PRESENT IN KB
		
		String[] columns = new String[] {KEY_ROWID, KEY_OBJECT, KEY_COLOR, KEY_SIZE, KEY_SHAPE};
		
		//WHERE statement in SQL
		color = "'" + color + "'";
		size = "'" + size + "'";
		shape = "'" + shape + "'";
		//String selection = KEY_COLOR + "=" + color + " AND " +  KEY_SIZE + "=" + size; //+ " AND "  + KEY_SHAPE + "=" + shape;
		String selection = KEY_COLOR + "=" + color + " AND " +  KEY_SIZE + "=" + size + " AND "  + KEY_SHAPE + "=" + shape;
		System.out.println("SQL statement : "+ selection);
		
		//execute SQL query to search for the entry
		Cursor c = ourDB.query(DB_TABLE, columns, selection, null, null, null, null);
		
		//if entry is found, return object name
		if(c!=null) {
			c.moveToFirst();
			String searchResult = c.getString(c.getColumnIndex(KEY_OBJECT));
			return searchResult;
		}
		
		//else return null
		return null;
	}

	public String searchObjectSizeShape(String size, String shape) {
		//CHECK WHETHER OBJECT INFO PRESENT IN KB
		
		String[] columns = new String[] {KEY_ROWID, KEY_OBJECT, KEY_COLOR, KEY_SIZE, KEY_SHAPE};
		
		//WHERE statement in SQL
		size = "'" + size + "'";
		shape = "'" + shape + "'";
		String selection = KEY_SIZE + "=" + size + " AND "  + KEY_SHAPE + "=" + shape;
		System.out.println("SQL statement : "+ selection);
		
		//execute SQL query to search for the entry
		Cursor c = ourDB.query(DB_TABLE, columns, selection, null, null, null, null);
		
		//if entries are found, return the object names 
		if(c!=null) {
			c.moveToFirst();
			String searchResult = c.getString(c.getColumnIndex(KEY_OBJECT));
			boolean next = c.moveToNext();
			
			//for more than 1 entry
			while(next) {
				searchResult = searchResult + " " + c.getString(c.getColumnIndex(KEY_OBJECT));
				next = c.moveToNext();
			}
			
			System.out.println(searchResult);
			return searchResult;
		}
		
		//else return null
		return null;
	}

	public String searchObjectColorShape(String color, String shape) {
		//CHECK WHETHER OBJECT INFO PRESENT IN KB
		
		String[] columns = new String[] {KEY_ROWID, KEY_OBJECT, KEY_COLOR, KEY_SIZE, KEY_SHAPE};
		
		//WHERE statement in SQL
		color = "'" + color + "'";
		shape = "'" + shape + "'";
		String selection = KEY_COLOR + "=" + color + " AND "  + KEY_SHAPE + "=" + shape;
		System.out.println("SQL statement : "+ selection);
		
		//execute SQL query to search for the entry
		Cursor c = ourDB.query(DB_TABLE, columns, selection, null, null, null, null);
		
		//if entries are found, return the object names 
		if(c!=null) {
			c.moveToFirst();
			String searchResult = c.getString(c.getColumnIndex(KEY_OBJECT));
			boolean next = c.moveToNext();
			
			//for more than 1 entry
			while(next) {
				searchResult = searchResult + " " + c.getString(c.getColumnIndex(KEY_OBJECT));
				next = c.moveToNext();
			}
			
			System.out.println(searchResult);
			return searchResult;
		}
		
		//else return null
		return null;
	}
}
