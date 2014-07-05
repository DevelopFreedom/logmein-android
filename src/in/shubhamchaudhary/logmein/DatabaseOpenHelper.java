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
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.provider.BaseColumns;
import android.util.Log;



public class DatabaseOpenHelper extends SQLiteOpenHelper{
	private static final String TAG= DatabaseOpenHelper.class.getSimpleName();
	public static final String DB_NAME="ID";
	public static final int DB_VERSION=1;
	public static final String TABLE="INVENTORY";	//XXX
	public static final String USERNAME="username";
	public static final String PASSWORD="password";
	public static final String C_ID= "C_ID";


	public DatabaseOpenHelper(Context context) {
		//database filename
		//database version
		super(context,DB_NAME,null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//create a database execute sql for creating the database
		String sql =String.format("create table %s (%s INTEGER primary key AUTOINCREMENT, %s TEXT, %s TEXT)",TABLE,C_ID,USERNAME,PASSWORD);
		Log.d(TAG,"onCreated sql"+sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//executes whenever version of database system reports is different
		if(oldVersion==1)
		{
			//alter current table or schema and execute the sql command using db.execSQL(sql)
			this.onUpgrade(db, ++oldVersion,newVersion);
		}

	}

}

