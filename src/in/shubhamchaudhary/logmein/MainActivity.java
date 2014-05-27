package in.shubhamchaudhary.logmein;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener{
	///Class Variables
	EditText textbox_username, textbox_password;
	Button button_save, button_login, button_logout;
	TextView debugTextView;
	SQLiteOpenHelper DBHELPER;
	SQLiteDatabase database;
    Cursor cursor;
	/*
	//Bypass android.os.NetworkOnMainThreadException
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

	StrictMode.setThreadPolicy(policy); 
	*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DBHELPER=new dbhelper(this);
		button_save=(Button)findViewById(R.id.button_save);
		button_save.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 //TODO: Check user input
	        	 saveToDatabase();
	         }
	     });
		button_login=(Button)findViewById(R.id.button_login);
		button_login.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	             // Perform action on click
	        	 Log.d("login","Insiide Login");
	        	 System.out.println("Insinde login");
	        	 try{
//	        		 login(textbox_username.getText().toString(),textbox_password.getText().toString());
	        		 login(getUsername(),getPassword());
	        	 }catch(Exception e){
	        		 //TODO
	        		 System.out.println("Exception message: "+e.toString());
	 			 }
	         }
	     });
		
		button_logout=(Button)findViewById(R.id.button_logout);
		button_logout.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	             // Perform action on click
	        	 Log.d("logout","Insiede Logout");
	        	 System.out.println("Insiade logout");
	        	 try{
	        		 logout();
	        	 }catch(Exception e){
	        		 //TODO
	        		 System.out.println("Exception message: "+e.toString());
	 			 }
	         }
	     });

        textbox_username=(EditText)findViewById(R.id.edit_username);
        textbox_password=(EditText)findViewById(R.id.edit_password);
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
		}

    public void login(final String username, final String password) throws Exception {
    	Thread thread = new Thread(new Runnable(){
    		@Override
    		public void run() {
    			try {
    				login_runner(username,password);    				
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	});
    	thread.start(); 
    }
    
    public void login_runner(String username, String password) throws Exception{
        System.out.println("Loggin in");
        if (username == null || password == null){
        	Log.wtf("Error", "Either username or password is null");
        	return;
        }
        //String username = "11uit424", password = "screwYou";
        String urlParameters = "user="+username+"&password="+password; // "param1=a&param2=b&param3=c";

        String request = "http://172.16.4.201/cgi-bin/login";
        URL puServerUrl = new URL(request);

        URLConnection puServerConnection = puServerUrl.openConnection();
        puServerConnection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(puServerConnection.getOutputStream());
        writer.write(urlParameters);
        writer.flush();
        
        //Output
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));

        while ((line = reader.readLine()) != null) {
        	Log.w("html", line);
        }
        writer.close();
        reader.close();
    }

    public void logout() throws Exception {
    	Thread thread = new Thread(new Runnable(){
    		@Override
    		public void run() {
    			try {
    				logout_runner();    				
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	});
    	thread.start(); 
    }
    
    public void logout_runner() throws Exception {
        System.out.println("Loggin out");
        URL puServerUrl = new URL("http://172.16.4.201/cgi-bin/login?cmd=logout");
        URLConnection puServerConnection = puServerUrl.openConnection();
        
        //Get inputStream and show output
        BufferedReader htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
    	//TODO parse output
        String inputLine;
        while ((inputLine = htmlBuffer.readLine()) != null){
        	Log.w("html", inputLine);
        }
        htmlBuffer.close();
    }
    

    void saveToDatabase(){
    	// TODO Auto-generated method stub
    	String a =textbox_username.getText().toString();
    	String b=textbox_password.getText().toString();
    	try{
    		//make a db connect to it add values to it (next task)	
    		//WTH
    		System.out.print("breakpoint1");

    		System.out.print("breakpoint 2");
    		database=DBHELPER.getWritableDatabase();
    		ContentValues values=new ContentValues();
    		button_save.setOnClickListener(this);
    		if(a!=null && b!=null)
    		{
    			values.put(dbhelper.USERNAME,a);
    			values.put(dbhelper.PASSWORD,b);

    			database.insert(dbhelper.TABLE,null, values); 
    			String[] columns=new String[]{"USERNAME","PASSWORD"};
    			cursor=database.query("INVENTORY", columns, null, null, null,null, null);
    			Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));  
    			//Debug message
    			Log.d("tag: main, onClick, try", "database connected and values inserted with primary key");	//Fuck you Vivek
    			Toast.makeText(getApplicationContext(), a+" entered into your inventory", Toast.LENGTH_SHORT).show();
    		}
    		textbox_username.clearComposingText();
    		textbox_password.clearComposingText();

    	}catch(Exception e){
    		System.out.println("ud gaya");
    	}

    	database.close();
    }
    String getUsername(){
    	String username = null;
    	try{
    		database=DBHELPER.getReadableDatabase();
    		String[] columns=new String[]{"USERNAME","PASSWORD"};
    		cursor=database.query("INVENTORY", columns, null, null, null,null, null);
    		int indexUsername = cursor.getColumnIndex("USERNAME");
    		cursor.moveToFirst();
    		username  = cursor.getString(indexUsername);
    	}catch(Exception e){
    		System.out.println("ud gaya");
    	}

    	database.close();
    	return username;
    }
    String getPassword(){
    	String password = null;
    	try{
    		database=DBHELPER.getReadableDatabase();
    		String[] columns=new String[]{"USERNAME","PASSWORD"};
    		cursor=database.query("INVENTORY", columns, null, null, null,null, null);
    		int indexUsername = cursor.getColumnIndex("USERNAME");
    		cursor.moveToFirst();
    		password  = cursor.getString(indexUsername);
    	}catch(Exception e){
    		System.out.println("ud gaya");
    	}

    	database.close();
    	return password;
    }


}
