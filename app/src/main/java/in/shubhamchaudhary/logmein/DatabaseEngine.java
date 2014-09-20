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

package in.shubhamchaudhary.logmein;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseEngine {
    // Singleton method with lazy initialization.
    private static DatabaseEngine instance = null;

    ;
    private static int use_count = 0;   //like semaphores
    Context context;
    SQLiteOpenHelper myDatabaseHelper;
    SQLiteDatabase database;
    Cursor cursor;

    public DatabaseEngine(Context ctx) {
        this.context = ctx;
        this.myDatabaseHelper = new DatabaseOpenHelper(this.context);
    }

    public static synchronized DatabaseEngine getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseEngine(context);
        }
        use_count += 1;
        return instance;
    }

    public boolean insert(UserStructure us){
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

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

    //TODO: this function looks like shit
    public void saveToDatabase(String username, String password) {
        try {
            //make a db connect to it add values to it (next task)
            //WTH
            Log.d("DE", "Saving " + username + " " + password + " to database:");
            database = myDatabaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            if (username != null && password != null) {
                values.put(DatabaseOpenHelper.USERNAME, username);
                values.put(DatabaseOpenHelper.PASSWORD, password);

                database.insert(DatabaseOpenHelper.TABLE, null, values);
                String[] columns = new String[]{DatabaseOpenHelper.USERNAME, DatabaseOpenHelper.PASSWORD};
                ///TODO: Why cursor?
                cursor = database.query(DatabaseOpenHelper.TABLE, columns, null, null, null, null, null);
                Log.v("DE", "Cursor Object" + DatabaseUtils.dumpCursorToString(cursor));
                //Debug message
                Log.d("DE", "database connected and values inserted with primary key");    //Fuck you Vivek

                database.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * return list of all the users in database
     */
    public ArrayList<String> userList() {
        ArrayList<String> user_list = new ArrayList<String>();
        try {
            database = myDatabaseHelper.getReadableDatabase();
            String[] columns = new String[]{DatabaseOpenHelper.USERNAME, DatabaseOpenHelper.PASSWORD};
            cursor = database.query(DatabaseOpenHelper.TABLE, columns, null, null, null, null, null);

            while (cursor.moveToNext()) {
                user_list.add(cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.USERNAME)));
            }
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("DE", "User List:");
        Log.i("DE", user_list.toString());

        return user_list;
    }

    /*
     * delete the user with username as passed
     */
    int deleteUser(int uid) {
        //TODO
        //Better if we only find the userid here and pass it to deleteUser(id)
        return -1;
    }

    /*
     * delete the user with id number passed
     */
    public boolean deleteUser(String username) {
        //Delete userid from database
        try{
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
            String[] columns = new String[]{username};
            long success = db.delete(DatabaseOpenHelper.TABLE, DatabaseOpenHelper.USERNAME + "=?", columns);
            if(success != -1){
                return true;
            }
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //TODO: Move to one common function
    public UserStructure getUsernamePassword(String un) {
        UserStructure user = null;
        try {

            database = myDatabaseHelper.getReadableDatabase();
            cursor = database.query(DatabaseOpenHelper.TABLE, new String[]{DatabaseOpenHelper.PASSWORD}, DatabaseOpenHelper.USERNAME + "=?", new String[]{un}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            user = new UserStructure();
            user.setUsername(un);
            user.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.PASSWORD)));


            /*
            //String[] columns=new String[]{DatabaseOpenHelper.USERNAME,DatabaseOpenHelper.PASSWORD};
            cursor = database.rawQuery("select * from ? where username=?", new String[]{DatabaseOpenHelper.TABLE,un} );
            if(!cursor.isNull(0)){
                //TODO:make sure that check is made when users are saved that no more than one entry for same user is made
                user = new UserStructure();
                user.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
            */

            //Log.e("unnnnnn", user.getUsername());
            //Log.e("pwwwwwd", user.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
        return (user);
    }//end of getUsernamePassword(String)

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
        database.close();
      }
      return false;
    }//end of existsUser(String)

    public int updateUser(UserStructure user, String oldname) {
        database = myDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String username = DatabaseOpenHelper.USERNAME;

        values.put(username, user.getUsername());
        values.put(DatabaseOpenHelper.PASSWORD, user.getPassword());

        return database.update(DatabaseOpenHelper.TABLE, values, username + "=?", new String[]{oldname});

    }//end of updateUser(UserStructure)

    //Class Variables
    public enum StatusCode {
        //TODO: Add more meaning full Status Codes
        DB_EMPTY, DB_DELETE_SUCCESS
    }

}
// vim: set ts=4 sw=4 tw=79 et :
