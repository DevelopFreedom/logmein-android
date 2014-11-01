/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *   Copyright (c) 2014 Tanjot Kaur <tanjot28@gmail.com>
 *   Copyright (c) 2014 Varun Verma <mailvarun93@gmail.com>
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

package org.developfreedom.logmein.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.developfreedom.logmein.DatabaseEngine;
import org.developfreedom.logmein.LoginService;
import org.developfreedom.logmein.NetworkEngine;

import java.util.ArrayList;

/**
 * The main activity which contains all the UI elements.
 * <p>
 * It is based upon the {@link ActionBarActivity}
 */
public class MainActivity extends ActionBarActivity {
    /* Engines */
    NetworkEngine networkEngine;
    DatabaseEngine databaseEngine;
    //TODO: Mark proper visibilities
    ///Class Variables
    Button button_edit;
    Button button_del;
    Button button_add;
    ImageButton button_login, button_logout,button_web;
    Spinner spinner_user_list;
    ArrayList<String> user_list;
    ArrayAdapter adapter;
    boolean spinnerUpdateFlag;
    SharedPreferences preferences;

    @Override
    protected void onResume() {
        // Make sure that when we return from manage use activity, the username is right
        updateHomescreenData();
        startAnimation();
        if (!preferences.getBoolean(SettingsActivity.KEY_PERSISTENCE,SettingsActivity.DEFAULT_KEY_PERSISTENCE)) {
            stopService(new Intent(this, LoginService.class));
        } else {
            //XXX: Start only if not running
            startService(new Intent(this, LoginService.class));
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.developfreedom.logmein.R.layout.activity_main);

        networkEngine = NetworkEngine.getInstance(this);
        databaseEngine = DatabaseEngine.getInstance(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        button_login = (ImageButton) findViewById(org.developfreedom.logmein.R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });

        button_logout = (ImageButton) findViewById(org.developfreedom.logmein.R.id.button_logout);
        button_logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logout();
            }
        });

        button_web = (ImageButton) findViewById(org.developfreedom.logmein.R.id.button_web);

        button_del = (Button ) findViewById(org.developfreedom.logmein.R.id.button_del);
        button_del.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(getSelectedUsername() == null){
                    Toast.makeText(MainActivity.this,"User List is empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                String username = spinner_user_list.getSelectedItem().toString();
//                showDeleteDialog("Delete User", "Are you sure you want to delete " + username, "YES", "NO").show();
                ManagerUserServices managerUserServices = new ManagerUserServices(MainActivity.this);
                Dialog dialog = managerUserServices.delete(spinner_user_list.getSelectedItem().toString());
                dialogDismissUpdater(dialog,0);
            }
        });

        button_add = (Button) findViewById(org.developfreedom.logmein.R.id.button_add);
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ManagerUserServices managerUserServices = new ManagerUserServices(MainActivity.this);
                Dialog dialog = managerUserServices.add(getLayoutInflater());
                dialogDismissUpdater(dialog, spinner_user_list.getCount());
            }
        });

        button_edit = (Button ) findViewById(org.developfreedom.logmein.R.id.button_edit);
        button_edit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(getSelectedUsername() == null){
                    Toast.makeText(MainActivity.this,"User List is empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                LayoutInflater inflater = getLayoutInflater();
                ManagerUserServices managerUserServices = new ManagerUserServices(MainActivity.this);
                Dialog dialog = managerUserServices.update(spinner_user_list.getSelectedItem().toString(),inflater);
                dialogDismissUpdater(dialog,spinner_user_list.getSelectedItemPosition());
            }
        });



        user_list = databaseEngine.userList();
        adapter = new ArrayAdapter<String>(this, org.developfreedom.logmein.R.layout.spinner_layout, user_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner_user_list = (Spinner) findViewById(org.developfreedom.logmein.R.id.spinner_user_list);
        spinner_user_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinnerUpdateFlag = false;
        //Recover saved position
        if (user_list.size() > 0) { //Crashes otherwise at first startup
            int saved_pos = preferences.getInt(SettingsActivity.KEY_CURRENT_USERNAME_POS, 0);
            spinner_user_list.setSelection(saved_pos % user_list.size());
        }


        spinner_user_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
                // An item was selected. We can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                Log.d("Main","onSelect: calling updateHomescreenData");
                updateHomescreenData();
                parent.setSelection(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("Main","onNothinSelect: calling updateHomescreenData");
                updateHomescreenData();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(org.developfreedom.logmein.R.id.container, new PlaceholderFragment()).commit();
        }
        updateHomescreenData();


        //animations
        startAnimation();

        //FIXME: Not the best way to call login method
        String intentMethod = getIntent().getStringExtra("methodName");
        if (intentMethod != null && intentMethod.equals("login")) {
            login();
            finish();
        } else if (intentMethod != null && intentMethod.equals("logout")) {
            logout();
            finish();
        }

        boolean prefNeedPersistence = preferences.getBoolean(SettingsActivity.KEY_PERSISTENCE, SettingsActivity.DEFAULT_KEY_PERSISTENCE);
        boolean perfStartupLogin = preferences.getBoolean(SettingsActivity.KEY_STARTUP_LOGIN,SettingsActivity.DEFAULT_KEY_STARTUP_LOGIN);
        if (prefNeedPersistence) {
            startService(new Intent(this, LoginService.class));
        }
        if (perfStartupLogin)
            login();

    }//end onCreate

    /**
     * Start all the animations
     */
    private void startAnimation() {
        ImageView centerWheel = (ImageView)findViewById(org.developfreedom.logmein.R.id.center_wheel);
        View infoView = findViewById(org.developfreedom.logmein.R.id.info);
        Animation rotation = AnimationUtils.loadAnimation(this, org.developfreedom.logmein.R.anim.rotation_start);
        centerWheel.startAnimation(rotation);

        Animation slideLeft = AnimationUtils.loadAnimation(this, org.developfreedom.logmein.R.anim.slide_in_left);
        button_logout.startAnimation(slideLeft);

        Animation slideRight = AnimationUtils.loadAnimation(this, org.developfreedom.logmein.R.anim.slide_in_right);
        button_web.startAnimation(slideRight);

        Animation slideTop = AnimationUtils.loadAnimation(this, org.developfreedom.logmein.R.anim.slide_in_top);
        infoView.startAnimation(slideTop);
        button_login.startAnimation(slideTop);

        Animation slideBottom = AnimationUtils.loadAnimation(this, org.developfreedom.logmein.R.anim.slide_in_bottom);
    }

    /**
     * Update the username data in main screen.
     * <p>
     * The main need for this is when a particular user is deleted
     * in a dialog box, then the main screen should reflect that
     * change.
     */
    public void updateHomescreenData() {
        Log.d("Main","Updating Home Screen"+spinnerUpdateFlag);
        int pos = spinner_user_list.getSelectedItemPosition();
        if (spinnerUpdateFlag) {
            spinnerUpdateFlag = false; //Avoid recursive loop via onItemSelected Listner
            return;
        } else {
            user_list = databaseEngine.userList();
            if (user_list.isEmpty()) {
                adapter.clear();
                spinner_user_list.setAdapter(adapter);

            } else {
                pos = spinner_user_list.getSelectedItemPosition();
                if (pos >= user_list.size())
                    pos = user_list.size() - 1;

                user_list = databaseEngine.userList();
                adapter = new ArrayAdapter<String>(this, org.developfreedom.logmein.R.layout.spinner_layout, user_list);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_user_list.setAdapter(adapter);
                spinnerUpdateFlag = true;
                spinner_user_list.setSelection(pos);
//                adapter.notifyDataSetChanged();
            }
        }
        String username = getSelectedUsername();
        if (username != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SettingsActivity.KEY_CURRENT_USERNAME, username);
            //Save current position for recovery
            editor.putInt(SettingsActivity.KEY_CURRENT_USERNAME_POS, pos);
            editor.apply();
        } else {
            //TODO: What happens when empty
            username = "Welcome, Please enter username and password for the first time!";
        }

    }//end updateHomescreenData

    @Override
    protected void onNewIntent(Intent intent) {
        //FIXME: This is not working. This task is done by ifs in onCreate
        Log.d("NewIntent", "New intent called: "+intent.getStringExtra("methodName"));
        if(intent.getStringExtra("methodName").equals("login")){
            login();
        } else if(intent.getStringExtra("methodName").equals("logout")){
            logout();
        } else {
            super.onNewIntent(intent);
        }
    }

    /**
     * Get the selected username from the spinner
     * @return String
     */
    public String getSelectedUsername() {
        return (String) spinner_user_list.getSelectedItem();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(org.developfreedom.logmein.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == org.developfreedom.logmein.R.id.action_settings) {
            //TODO: Calling manage_user until we have some settings
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show text on home-screen textview
     * @param text
     */
    void showText(String text) {
        //int scroll_amount = (int) (outputTextView.getLineCount() * outputTextView.getLineHeight()) - (outputTextView.getBottom() - outputTextView.getTop());
        //outputTextView.scrollTo(0, scroll_amount);
    }

    /**
     * Perform login task
     */
    void login() {
        if(getSelectedUsername() == null){
            Toast.makeText(MainActivity.this,"User List is empty",Toast.LENGTH_SHORT).show();
            return;
        }
        NetworkEngine.StatusCode status = null;
        Log.d("login", "Insiide Login");
        String username, password;
        // Use username/password from textbox if both filled
        username = getSelectedUsername();
        password = databaseEngine.getUsernamePassword(username).getPassword();

        if(password.isEmpty()){
            Toast.makeText(this,"Password not saved for "+username,Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            status = networkEngine.login(username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end login

    /**
     * Perform logout task
     */
    void logout() {
        NetworkEngine.StatusCode status = null;
        Log.d("logout", "Insiede Logout");
        try {
            status = networkEngine.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end logout

    /**
     * Send an intent to start the default web browser.
     * @param v
     */
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
    }

    /**
     * TODO: Documentation
     * @param dialog
     * @param pos
     */
    public void dialogDismissUpdater(Dialog dialog, final int pos){
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                spinnerUpdateFlag=false;
                updateHomescreenData();
                if(spinner_user_list.getCount() > pos){
                    spinner_user_list.setSelection(pos);
                }
            }
        });
    }
}//end MainActivity class
/* vim: set tabstop=4:shiftwidth=4:textwidth=79:et */
