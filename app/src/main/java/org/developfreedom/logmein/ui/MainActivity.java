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
    //Class Variables
    private Button mButtonEdit;
    private Button mButtonDel;
    private Button mButtonAdd;
    private ImageButton mButtonLogin, mButtonLogout, mButtonWeb;
    private Spinner mSpinnerUserList;
    private ArrayList<String> mUserList;
    private ArrayAdapter mAdapter;
    private boolean mSpinnerUpdateFlag;
    private SharedPreferences mPreferences;

    @Override
    protected void onResume() {
        // Make sure that when we return from manage use activity, the username is right
        updateHomescreenData();
        startAnimation();
        if (!mPreferences.getBoolean(SettingsActivity.KEY_USE_NOTIF,SettingsActivity.DEFAULT_KEY_USE_NOTIFICATION)) {
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

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        mButtonLogin = (ImageButton) findViewById(org.developfreedom.logmein.R.id.button_login);
        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });

        mButtonLogout = (ImageButton) findViewById(org.developfreedom.logmein.R.id.button_logout);
        mButtonLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logout();
            }
        });

        mButtonWeb = (ImageButton) findViewById(org.developfreedom.logmein.R.id.button_web);

        mButtonDel = (Button ) findViewById(org.developfreedom.logmein.R.id.button_del);
        mButtonDel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (getSelectedUsername() == null) {
                    Toast.makeText(MainActivity.this, "User List is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                String username = mSpinnerUserList.getSelectedItem().toString();
                ManagerUserServices managerUserServices = new ManagerUserServices(MainActivity.this);
                Dialog dialog = managerUserServices.delete(mSpinnerUserList.getSelectedItem().toString());
                dialogDismissUpdater(dialog, 0);
            }
        });

        mButtonAdd = (Button) findViewById(org.developfreedom.logmein.R.id.button_add);
        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ManagerUserServices managerUserServices = new ManagerUserServices(MainActivity.this);
                Dialog dialog = managerUserServices.add(getLayoutInflater());
                dialogDismissUpdater(dialog, mSpinnerUserList.getCount());
            }
        });

        mButtonEdit = (Button ) findViewById(org.developfreedom.logmein.R.id.button_edit);
        mButtonEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (getSelectedUsername() == null) {
                    Toast.makeText(MainActivity.this, "User List is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                LayoutInflater inflater = getLayoutInflater();
                ManagerUserServices managerUserServices = new ManagerUserServices(MainActivity.this);
                Dialog dialog = managerUserServices.update(mSpinnerUserList.getSelectedItem().toString(), inflater);
                dialogDismissUpdater(dialog, mSpinnerUserList.getSelectedItemPosition());
            }
        });



        mUserList = databaseEngine.userList();
        mAdapter = new ArrayAdapter<String>(this, org.developfreedom.logmein.R.layout.spinner_layout, mUserList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpinnerUserList = (Spinner) findViewById(org.developfreedom.logmein.R.id.spinner_user_list);
        mSpinnerUserList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mSpinnerUpdateFlag = false;
        //Recover saved position
        if (mUserList.size() > 0) { //Crashes otherwise at first startup
            int saved_pos = mPreferences.getInt(SettingsActivity.KEY_CURRENT_USERNAME_POS, 0);
            mSpinnerUserList.setSelection(saved_pos % mUserList.size());
        }


        mSpinnerUserList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
                // An item was selected. We can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                Log.d("Main", "onSelect: calling updateHomescreenData");
                updateHomescreenData();
                parent.setSelection(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("Main", "onNothinSelect: calling updateHomescreenData");
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

        boolean prefUseNotifications = mPreferences.getBoolean(SettingsActivity.KEY_USE_NOTIF, SettingsActivity.DEFAULT_KEY_USE_NOTIFICATION);
        if (prefUseNotifications) {
            startService(new Intent(this, LoginService.class));
        }

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
        mButtonLogout.startAnimation(slideLeft);

        Animation slideRight = AnimationUtils.loadAnimation(this, org.developfreedom.logmein.R.anim.slide_in_right);
        mButtonWeb.startAnimation(slideRight);

        Animation slideTop = AnimationUtils.loadAnimation(this, org.developfreedom.logmein.R.anim.slide_in_top);
        infoView.startAnimation(slideTop);
        mButtonLogin.startAnimation(slideTop);

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
        Log.d("Main","Updating Home Screen"+ mSpinnerUpdateFlag);
        int pos = mSpinnerUserList.getSelectedItemPosition();
        if (mSpinnerUpdateFlag) {
            mSpinnerUpdateFlag = false; //Avoid recursive loop via onItemSelected Listner
            return;
        } else {
            mUserList = databaseEngine.userList();
            if (mUserList.isEmpty()) {
                mAdapter.clear();
                mSpinnerUserList.setAdapter(mAdapter);

            } else {
                pos = mSpinnerUserList.getSelectedItemPosition();
                if (pos >= mUserList.size())
                    pos = mUserList.size() - 1;

                mUserList = databaseEngine.userList();
                mAdapter = new ArrayAdapter<String>(this, org.developfreedom.logmein.R.layout.spinner_layout, mUserList);
                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinnerUserList.setAdapter(mAdapter);
                mSpinnerUpdateFlag = true;
                mSpinnerUserList.setSelection(pos);
            }
        }
        String username = getSelectedUsername();
        if (username != null) {
            SharedPreferences.Editor editor = mPreferences.edit();
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
        return (String) mSpinnerUserList.getSelectedItem();
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
     * Updates the spinner selection to proper index
     * @param dialog to detect onDismiss of dialog
     * @param pos is the position of spinner to be selected
     */
    public void dialogDismissUpdater(Dialog dialog, final int pos){
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mSpinnerUpdateFlag =false;
                updateHomescreenData();
                if(mSpinnerUserList.getCount() > pos){
                    mSpinnerUserList.setSelection(pos);
                }
            }
        });
    }
}//end MainActivity class
/* vim: set tabstop=4:shiftwidth=4:textwidth=79:et */
