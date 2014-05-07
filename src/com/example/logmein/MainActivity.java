package com.example.logmein;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener{
	//UI
	EditText textbox_username, textbox_password;
	Button button_save, button_login;
	//Database
	SQLiteOpenHelper DBHELPER;
	 SQLiteDatabase database;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
       
        textbox_username=(EditText)findViewById(R.id.edit_username);
        textbox_password=(EditText)findViewById(R.id.edit_password);
		button_save=(Button)findViewById(R.id.button_save);
		button_login=(Button)findViewById(R.id.button_login);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String a =textbox_username.getText().toString();
		String b=textbox_password.getText().toString();
		try{
			//make a db connect to it add values to it (next task)	
				System.out.print("breakpoint1");
				DBHELPER=new dbhelper(this);
				System.out.print("breakpoint 2");
				database=DBHELPER.getWritableDatabase();
				ContentValues values=new ContentValues();
				button_save.setOnClickListener(this);
				if(a!=null && b!=null)
				{
					
					values.put(dbhelper.USERNAME,a);
					values.put(dbhelper.PASSWORD,b);
					
					//values.put(dbhelper.PROFIT, c);
					database.insert(dbhelper.TABLE,null, values);  
		            Log.d("databaseconnected and values inserted", "withprimarykey kuch bhi par sahi hogi");
		            Toast.makeText(getApplicationContext(), a+" entered into your inventory", Toast.LENGTH_SHORT).show();
				}
				textbox_username.clearComposingText();
				textbox_password.clearComposingText();
				
			}catch(Exception e){
				System.out.println("ud gaya");
			}

			database.close();
	}

}
