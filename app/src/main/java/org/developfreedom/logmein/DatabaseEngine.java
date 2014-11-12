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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * DatabaseEngine is the main interface that must be used to perform
 * any database related task like adding, deleting, updating users.
 * <p>
 * The main motive is to abstract away all the nitty gritty database
 * stuff for simpler cleaner high level access.
 */
public class DatabaseEngine {
    // Singleton method with lazy initialization.
    private static DatabaseEngine instance = null;
    private static int use_count = 0;   //like semaphores
    private Context mContext;
    private SQLiteOpenHelper mMyDatabaseHelper;
    private SQLiteDatabase mDatabase;
    Cursor cursor;

    public DatabaseEngine(Context ctx) {
        this.mContext = ctx;
        this.mMyDatabaseHelper = new DatabaseOpenHelper(this.mContext);
    }

    /**
     * Singleton method with lazy initialization.
     * Desired way to create/access the Engine object
     * @param context Context in which the notification and toasts will be displayed.
     * @return Reference to singleton object of Engine
     */
    public static synchronized DatabaseEngine getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseEngine(context);
        }
        use_count += 1;
        return instance;
    }

    /**
     * Insert a new user in Database using UserStructure us
     * @param us
     * @return
     */
    public boolean insert(UserStructure us){
        SQLiteDatabase db = mMyDatabaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseOpenHelper.USERNAME, us.getUsername()); // Contact Name
        values.put(DatabaseOpenHelper.PASSWORD, us.getPassword()); // Contact Phone Number

        // Inserting Row
        long success = db.insert(DatabaseOpenHelper.TABLE, null, values);
        db.close(); // Closing database connection
        Log.e("success",""+success);
        if(success == -1){
            return false;
        }
        return true;
    }
    /**
     * List of all the users in database
     * @return ArrayList
     */
    public ArrayList<String> userList() {
        ArrayList<String> user_list = new ArrayList<String>();
        try {
            mDatabase = mMyDatabaseHelper.getReadableDatabase();
            String[] columns = new String[]{DatabaseOpenHelper.USERNAME, DatabaseOpenHelper.PASSWORD};
            cursor = mDatabase.query(DatabaseOpenHelper.TABLE, columns, null, null, null, null, null);

            while (cursor.moveToNext()) {
                user_list.add(cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.USERNAME)));
            }
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("DE", "User List:");
        Log.i("DE", user_list.toString());

        return user_list;
    }

    /**
     * Delete the user with username as passed
     * @param username
     * @return
     */
    public boolean deleteUser(String username) {
        try{
            SQLiteDatabase db = mMyDatabaseHelper.getWritableDatabase();
            String[] columns = new String[]{username};
            long success = db.delete(DatabaseOpenHelper.TABLE, DatabaseOpenHelper.USERNAME + "=?", columns);
            if(success != -1){
                return true;
            }
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the username/password of user with given name un
     * @param un
     * @return
     */
    public UserStructure getUsernamePassword(String un) {
        UserStructure user = null;
        try {

            mDatabase = mMyDatabaseHelper.getReadableDatabase();
            cursor = mDatabase.query(DatabaseOpenHelper.TABLE, new String[]{DatabaseOpenHelper.PASSWORD}, DatabaseOpenHelper.USERNAME + "=?", new String[]{un}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            user = new UserStructure();
            user.setUsername(un);
            user.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.PASSWORD)));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDatabase.close();
        }
        return (user);
    }//end of getUsernamePassword(String)

    /**
     * Check if a given user exists in database
     * @param username
     * @return boolean true false
     */
    public boolean existsUser(String username){
      try{
        ArrayList users = userList();
        for(int i=0; i<users.size(); i++){
            if(username.equals((String) users.get(i)) ){
                return true;
            }
        }//end of for i
      } catch(Exception e){
          System.out.println(e);
      } finally{
        mDatabase.close();
      }
      return false;
    }//end of existsUser(String)

    /**
     * Updates existing record with whose username = oldname
     * @param user is the new record
     * @param oldname is the existing username whose record needs to be updated
     * @return no of entries updated
     */
    public int updateUser(UserStructure user, String oldname) {
        mDatabase = mMyDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String username = DatabaseOpenHelper.USERNAME;

        values.put(username, user.getUsername());
        values.put(DatabaseOpenHelper.PASSWORD, user.getPassword());

        return mDatabase.update(DatabaseOpenHelper.TABLE, values, username + "=?", new String[]{oldname});

    }//end of updateUser(UserStructure)

    //Class Variables

    /**
     * A collection of various situations that might occur in Engine
     */
    public enum StatusCode {
        //TODO: Add more meaning full Status Codes
        DB_EMPTY, DB_DELETE_SUCCESS
    }

}
// vim: set ts=4 sw=4 tw=79 et :
