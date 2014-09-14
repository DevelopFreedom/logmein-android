package in.shubhamchaudhary.logmein;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//import android.provider.BaseColumns;



public class dbhelper extends SQLiteOpenHelper{
    private static final String TAG= dbhelper.class.getSimpleName();
    public static final String DB_NAME="ID";
    public static final int DB_VERSION=1;
    public static final String TABLE="INVENTORY";
    public static final String USERNAME="username";
    public static final String PASSWORD="password";
    public static final String C_ID= "C_ID";


    public dbhelper(Context context) {
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

// vim: set ts=4 sw=4 tw=79 et :
