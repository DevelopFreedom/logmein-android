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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseEngine {
    Context context;
    SQLiteOpenHelper myDatabaseHelper ;
    DatabaseEngine(Context ctx){
        this.context = ctx;
        this.myDatabaseHelper = new dbhelper(this.context);
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
				values.put(dbhelper.USERNAME,username);
				values.put(dbhelper.PASSWORD,password);

				database.insert(dbhelper.TABLE,null, values);
				String[] columns=new String[]{dbhelper.USERNAME,dbhelper.PASSWORD};
				cursor=database.query(dbhelper.TABLE, columns, null, null, null,null, null);
				Log.v("DE", "Cursor Object" + DatabaseUtils.dumpCursorToString(cursor));
				//Debug message
				Log.d("DE", "database connected and values inserted with primary key");    //Fuck you Vivek

				database.close();
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	String getUsername(){
		String username = null;
		try{
			database=myDatabaseHelper.getReadableDatabase();
			String[] columns=new String[]{dbhelper.USERNAME,dbhelper.PASSWORD};
			cursor=database.query(dbhelper.TABLE, columns, null, null, null,null, null);
			int indexUsername = cursor.getColumnIndex(dbhelper.USERNAME);
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
			String[] columns=new String[]{dbhelper.USERNAME,dbhelper.PASSWORD};
			cursor=database.query(dbhelper.TABLE, columns, null, null, null,null, null);
			int indexPassword = cursor.getColumnIndex(dbhelper.PASSWORD);

			cursor.moveToLast();
			password  = cursor.getString(indexPassword);
			database.close();
		}catch(Exception e){
			e.printStackTrace();
		}

		return password;
	}




}
