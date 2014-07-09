/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *   Copyright (c) 2014 Vivek Aggarwal <vivekaggarwal92@gmail.com>
 *
 *   This file is part of LogMeIn.
 *
 *   LogMeIn is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LogMeIn is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LogMeIn.  If not, see <http://www.gnu.org/licenses/>.
 */

package in.shubhamchaudhary.logmein;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseEngine {
	//Class Variables
	public enum StatusCode {
		//TODO: Add more meaning full Status Codes
		DB_EMPTY, DB_DELETE_SUCCESS
	};
    Context context;
    SQLiteOpenHelper myDatabaseHelper ;
    DatabaseEngine(Context ctx){
        this.context = ctx;
        this.myDatabaseHelper = new DatabaseOpenHelper(this.context);
    }
	SQLiteDatabase database;
	Cursor cursor;

	void saveToDatabase(String username, String password){
		try{
			//make a db connect to it add values to it (next task)
			//WTH
			Log.d("DE","Saving " + username+" "+password + " to database:");
			database=myDatabaseHelper.getWritableDatabase();
			ContentValues values=new ContentValues();
			if(username!=null && password!=null)
			{
				values.put(DatabaseOpenHelper.USERNAME,username);
				values.put(DatabaseOpenHelper.PASSWORD,password);

				database.insert(DatabaseOpenHelper.TABLE,null, values);
				String[] columns=new String[]{DatabaseOpenHelper.USERNAME,DatabaseOpenHelper.PASSWORD};
				///TODO: Why cursor?
				cursor=database.query(DatabaseOpenHelper.TABLE, columns, null, null, null,null, null);
				Log.v("DE", "Cursor Object" + DatabaseUtils.dumpCursorToString(cursor));
				//Debug message
				Log.d("DE", "database connected and values inserted with primary key");    //Fuck you Vivek

				database.close();
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//TODO: Move to one common function
	String getUsername(){
		String username = null;
		try{
			database=myDatabaseHelper.getReadableDatabase();
			String[] columns=new String[]{DatabaseOpenHelper.USERNAME,DatabaseOpenHelper.PASSWORD};
			cursor=database.query(DatabaseOpenHelper.TABLE, columns, null, null, null,null, null);
			int indexUsername = cursor.getColumnIndex(DatabaseOpenHelper.USERNAME);
			cursor.moveToLast();
			username  = cursor.getString(indexUsername);
			database.close();
		}catch(Exception e){
			e.printStackTrace();
		}

		return username;
	}
	String getPassword(){
		String password = null;
		try{
			database=myDatabaseHelper.getReadableDatabase();
			String[] columns=new String[]{DatabaseOpenHelper.USERNAME,DatabaseOpenHelper.PASSWORD};
			cursor=database.query(DatabaseOpenHelper.TABLE, columns, null, null, null,null, null);
			int indexPassword = cursor.getColumnIndex(DatabaseOpenHelper.PASSWORD);

			cursor.moveToLast();
			password  = cursor.getString(indexPassword);
			database.close();
		}catch(Exception e){
			e.printStackTrace();
		}

		return password;
	}

	/*
	 * return list of all the users in database
	 */
	ArrayList<String> userList(){
		ArrayList<String> user_list = new ArrayList<String>();
		try{
			database=myDatabaseHelper.getReadableDatabase();
			String[] columns=new String[]{DatabaseOpenHelper.USERNAME,DatabaseOpenHelper.PASSWORD};
			cursor=database.query(DatabaseOpenHelper.TABLE, columns, null, null, null,null, null);

			while(cursor.moveToNext()){
				user_list.add(cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.USERNAME)));
			}
			database.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.i("DE","User List:");
		Log.i("DE",user_list.toString());

		return user_list;
	}

	/*
	 * delete the user with username as passed
	 */
	int deleteUser(String username){
		//TODO
		//Better if we only find the userid here and pass it to deleteUser(id)
		return -1;
	}

	/*
	 * delete the user with id number passed
	 */
	int deleteUser(int id){
		//TODO
		//Delete userid from database
		return -1;
	}

}
