/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Tanjot Kaur <tanjot28@gmail.com>
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
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

package in.shubhamchaudhary.logmein.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import in.shubhamchaudhary.logmein.DatabaseEngine;
import in.shubhamchaudhary.logmein.R;

public class UserDatabase extends FragmentActivity {

    Spinner spinner_user_list;
    ArrayAdapter<String> adapter;
    ArrayList<String> user_list;
    DatabaseEngine databaseEngine;
    Button button_edit;
    Boolean add_update;
    TextView test_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_database);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
        Intent intent = getIntent();
        add_update = intent.getBooleanExtra("add_update",false);
        Log.e("ud a_u", "" + add_update);

        databaseEngine = DatabaseEngine.getInstance(this);
        button_edit = (Button) findViewById(R.id.button_edit);
        test_list = (TextView) findViewById(R.id.tb_user_list);
        spinner_user_list = (Spinner) findViewById(R.id.spinner_user_list);
        user_list = databaseEngine.userList();

        adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, user_list);
        spinner_user_list.setAdapter(adapter);

        if(add_update){
            buttons_enabled(false);

            spinner_user_list.setVisibility(View.INVISIBLE);
            button_edit.setVisibility(View.INVISIBLE);
            test_list.setVisibility(View.INVISIBLE);
            edit_user_profile();
        }

        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_user_profile();
            }
        });

    }

    public void buttons_enabled(boolean flag){
        spinner_user_list.setEnabled(flag);
        button_edit.setEnabled(flag);
        test_list.setEnabled(flag);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_database, menu);
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

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        buttons_enabled(true);
    }
    public void edit_user_profile() {
        Bundle bundle = new Bundle();
        Fragment frag = new FragmentEdit();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragment_transaction = fm.beginTransaction();
        bundle.putBoolean("add_update", add_update);

        if(add_update){
            frag.setArguments(bundle);
            fragment_transaction.replace(R.id.fragment_blank, frag);

        }else {
            String username = (String) spinner_user_list.getSelectedItem();
            UserStructure user = databaseEngine.getUsernamePassword(username);
            bundle.putSerializable("user", user);
            frag.setArguments(bundle);
            fragment_transaction.replace(R.id.fragment_blank, frag);
            fragment_transaction.addToBackStack(null);
        }
        buttons_enabled(false);
        fragment_transaction.commit();

    }//end

    public void show_password(View v) {
        CheckBox cb_show_pwd = (CheckBox) FragmentEdit.v.findViewById(R.id.cb_show_password);
        EditText pwd = (EditText) FragmentEdit.v.findViewById(R.id.edit_password);
        if (cb_show_pwd.isChecked()) {
            pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            return;
        }
        pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }//end of show_password(View)

    public void update_spinner_list(String oldname, String newname) {
        buttons_enabled(true);
        adapter.remove(oldname);
        adapter.add(newname);
        adapter.notifyDataSetChanged();
        spinner_user_list.setSelection(adapter.getPosition(newname));
    }//end of update_spinner_list

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_user_database,
                    container, false);
            return rootView;
        }
    }

}//end of class UserDatabase
// vim: set ts=4 sw=4 tw=79 et :
