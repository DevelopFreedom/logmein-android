/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *   Copyright (c) 2014 Vivek Aggarwal <vivekaggarwal92@gmail.com>
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity{
	///Class Variables
	EditText textbox_username, textbox_password;
	Button button_save, button_login, button_logout;
	TextView debugTextView;

	/* Engines */
	NetworkEngine networkEngine;
	DatabaseEngine databaseEngine;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		networkEngine = new NetworkEngine();
		databaseEngine = new DatabaseEngine(this);

		debugTextView = (TextView)findViewById(R.id.debugTextView);
		String username = databaseEngine.getUsername();
		if (username.length() != 0){
			debugTextView.setText("Current user: " + username);
		}

		button_save=(Button)findViewById(R.id.button_save);
		button_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//TODO: Check user input
				debugTextView.setText(debugTextView.getText().toString()+"Trying to saveCredential");
				saveCredential();
			}
		});

		button_login=(Button)findViewById(R.id.button_login);
		button_login.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				Log.d("login","Insiide Login");
				System.out.println("Insinde login");
				try{
//					login(textbox_username.getText().toString(),textbox_password.getText().toString());
					networkEngine.login(databaseEngine.getUsername(),databaseEngine.getPassword());
				}catch(Exception e){
					//TODO
					System.out.println("Exception message: "+e.toString());
				}
			}
		});

		button_logout=(Button)findViewById(R.id.button_logout);
		button_logout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				logout();
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

	void logout(){
		NetworkEngine.StatusCode status = null;
		Log.d("logout","Insiede Logout");
		System.out.println("Insiade logout");
		try{
			status = networkEngine.logout();
		}catch(Exception e){
			System.out.println("Exception message: "+e.toString());
		}
		if (status == NetworkEngine.StatusCode.LOGOUT_SUCCESS){
			debugTextView.setText("Logout Successful");
			Toast.makeText(getApplicationContext(), "Logout Successful", Toast.LENGTH_SHORT).show();
		}else{
			debugTextView.setText("Logout Successful");
			Toast.makeText(getApplicationContext(), "DEBUG: Not Logout Successful", Toast.LENGTH_SHORT).show();
		}
		//TODO: handle if not success
	}

	void saveCredential(){
		String username =textbox_username.getText().toString();
		String password =textbox_password.getText().toString();
		
		debugTextView.setText(debugTextView.getText().toString()+"Saving: "+username+password);
		databaseEngine.saveToDatabase(username, password);

		Toast.makeText(getApplicationContext(), databaseEngine.getUsername()+" entered into your inventory", Toast.LENGTH_SHORT).show();

//		textbox_username.clearComposingText();
		textbox_password.clearComposingText();
	}

}
/* vim: set tabstop=4:softtabstop=8:shiftwidth=8:noexpandtab:textwidth=0:sta */
