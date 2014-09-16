/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *   Copyright (c) 2014 Tanjot Kaur <tanjot28@gmail.com>
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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import in.shubhamchaudhary.logmein.ui.ManageUser;
import in.shubhamchaudhary.logmein.ui.UserStructure;

public class MainActivity extends ActionBarActivity {
    ///Class Variables
    Button button_login, button_logout;
    TextView outputTextView;
    Spinner spinner_user_list;
    ArrayList<String> user_list;
    ArrayAdapter adapter;
    /* Engines */
    NetworkEngine networkEngine;
    DatabaseEngine databaseEngine;

    @Override
    protected void onResume() {
        // Make sure that when we return from manage use activity, the username is right
        updateHomescreenData();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkEngine = NetworkEngine.getInstance(this);
        databaseEngine = DatabaseEngine.getInstance(this);

        outputTextView = (TextView) findViewById(R.id.outputTextView);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());

        button_login = (Button) findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });

        button_logout = (Button) findViewById(R.id.button_logout);
        button_logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logout();
            }
        });

        user_list = databaseEngine.userList();
        adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, user_list);
        spinner_user_list = (Spinner) findViewById(R.id.spinner_user_list);
        spinner_user_list.setAdapter(adapter);

        spinner_user_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
                // An item was selected. We can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                parent.setSelection(pos);
                updateHomescreenData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
        updateHomescreenData();
    }

    public void updateHomescreenData() {

        String username = getSelectedUsername();
        if (username != null) {
            //if (username.length() != 0){
            outputTextView.setText("Current user: " + username);
        } else {
            username = "Welcome, Please enter username and password for the first time!";
            outputTextView.setText(username);
        }

    }

    public String getSelectedUsername() {
        return (String)spinner_user_list.getSelectedItem();
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
            //TODO: Calling manage_user until we have some settings
            manage_user(this.findViewById(android.R.id.content));
            return true;
        } else if (id == R.id.action_manage_user) {
            manage_user(this.findViewById(android.R.id.content));
        }
        return super.onOptionsItemSelected(item);
    }

    void showText(String text) {
        outputTextView.append("\n" + text);
        //int scroll_amount = (int) (outputTextView.getLineCount() * outputTextView.getLineHeight()) - (outputTextView.getBottom() - outputTextView.getTop());
        //outputTextView.scrollTo(0, scroll_amount);
    }

    void login() {
        NetworkEngine.StatusCode status = null;
        Log.d("login", "Insiide Login");
        String username, password;
        // Use username/password from textbox if both filled
        username = getSelectedUsername();
        UserStructure user_structure = databaseEngine.getUsernamePassword(username);
        password = user_structure.getPassword();

        if (username.length() == 0 && password.length() == 0) {
            username = databaseEngine.getUsername();
            password = databaseEngine.getPassword();
        }
        try {
            status = networkEngine.login(username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end login

    void logout() {
        NetworkEngine.StatusCode status = null;
        Log.d("logout", "Insiede Logout");
        try {
            status = networkEngine.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end logout

    public void manage_user(View v) {

        Intent intent_user_db = new Intent(this, ManageUser.class);
//        String un = textbox_username.getText().toString();
//        intent_user_db.putExtra("username", un);
        startActivity(intent_user_db);

    }//end of manage_user(View)

    public void launch_browser(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.google.com")));
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
}//end MainActivity class
/* vim: set tabstop=4:shiftwidth=4:textwidth=79:et */
