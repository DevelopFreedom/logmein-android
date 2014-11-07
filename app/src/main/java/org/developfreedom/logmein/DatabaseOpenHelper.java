/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *   Copyright (c) 2014 Tanjot Kaur <tanjot28@gmail.com>
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

package org.developfreedom.logmein;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * An SQLiteOpenHelper class
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
    /** Database Filename */
    public static final String DB_NAME = "ID";
    /** Database Version */
    public static final int DB_VERSION = 1;
    public static final String TABLE = "CREDENTIALS";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String C_ID = "C_ID";
    private static final String TAG = DatabaseOpenHelper.class.getSimpleName();


    public DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * {@inheritDoc}
     * Create a database, Execute sql for creating the database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("create table %s (%s INTEGER primary key AUTOINCREMENT, %s TEXT, %s TEXT)", TABLE, C_ID, USERNAME, PASSWORD);
        Log.d(TAG, "onCreated sql" + sql);
        db.execSQL(sql);
    }

    /**
     * {@inheritDoc}
     * Executes whenever version of database system reports is different
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            //alter current table or schema and execute the sql command using db.execSQL(sql)
            this.onUpgrade(db, ++oldVersion, newVersion);
        }

    }

}

// vim: set ts=4 sw=4 tw=79 et :
