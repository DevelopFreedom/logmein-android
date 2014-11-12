/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Tanjot Kaur <tanjot28@gmail.com>
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.developfreedom.logmein.DatabaseEngine;
import org.developfreedom.logmein.R;
import org.developfreedom.logmein.UserStructure;

/**
 * Provides services for managing the user information in database
 */
public class ManagerUserServices {

    private Context mContext;
    private DatabaseEngine mDatabaseEngine;
    private String mUsername;
    private Boolean flagAddUpdate;
    private View mView;
    private EditText mTextboxUsername = null, mTextboxPassword = null;
    private CheckBox mChbShowPwd;
    Toast m_currentToast;

    ManagerUserServices(Context context){
        this.mContext = context;
        mDatabaseEngine = DatabaseEngine.getInstance(this.mContext);
    }

    /**
     * Method to initialize field values for the dialog layout
     * @param inflater
     */
    private void initialize(LayoutInflater inflater){
        mView = inflater.inflate(R.layout.alert_dialog, null);
        mTextboxUsername = (EditText) mView.findViewById(R.id.edit_username);
        mTextboxPassword = (EditText) mView.findViewById(R.id.edit_password);
        mChbShowPwd = (CheckBox) mView.findViewById(R.id.cb_show_password);
        mChbShowPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_password();
            }
        });
    }

    /**
     * Performs tasks similar for both add and update methods
     * @param un
     * @param pwd
     * @return
     */
    private boolean add_update(String un,String pwd){

        if( un.trim().isEmpty()){
            Toast.makeText(this.mContext,"Username cannot be an empty string",Toast.LENGTH_LONG).show();
            return false;
        }
        if( pwd.trim().isEmpty()){
            Toast.makeText(this.mContext,"Password cannot be an empty string",Toast.LENGTH_LONG).show();
            return false;
        }

        UserStructure userStructure = new UserStructure();
        userStructure.setUsername(un);
        userStructure.setPassword(pwd);

        boolean status = false;
        if(flagAddUpdate){
            status = saveCredential(userStructure);

        }else{
            status = updateCredentials(userStructure);
        }
        return status;
    }
    void showToast(String text, int period)
    {
        if(m_currentToast != null) {
            m_currentToast.cancel();
        }
        m_currentToast = Toast.makeText(this.mContext, text, period);
        m_currentToast.show();
    }
    /**
     * Add user information to database
     * @param userStructure contains the information to be added to database
     * @return whether userStructure was added to the database or not
     */
    private boolean saveCredential(UserStructure userStructure) {

        if(!mDatabaseEngine.existsUser(userStructure.getUsername())){
            if(mDatabaseEngine.insert(userStructure)){
                showToast(userStructure.getUsername() + " entered", Toast.LENGTH_SHORT);
                return true;
            } else {
                showToast(" problem inserting record", Toast.LENGTH_SHORT);
                return false;
            }

        } else{
            showToast("Username already exists", Toast.LENGTH_SHORT);
            return false;
        }

    }//end saveCredential

    /**
     * Updates the information which corresponds to mUsername in database to information in userStructure
     * @param userStructure contains the information to be added to database
     * @return whether userStructure was added to the database or not
     */
    private boolean updateCredentials(UserStructure userStructure){
        int i = mDatabaseEngine.updateUser(userStructure, mUsername);
        if (i == 1) {
            Log.e("Updated", "Updated user");
            showToast("Updated account", Toast.LENGTH_SHORT);
            return true;
        } else if (i == 0) {
            showToast("Problem in updating account", Toast.LENGTH_SHORT);
            Log.e("Updated", "Error updating");
            return false;
        } else {
            showToast("Updated more than 1 records", Toast.LENGTH_SHORT);
            Log.e("Updated", "Updated more than 1 records");
            return true;
        }

    }//end of updateCredentials

    /**
     * Displays dialog box and manages updation of information
     * @param un username whose information is to be updated
     * @param inflater to instantiate layout
     * @return object of Dialog created
     */
    public Dialog update(String un,LayoutInflater inflater){
        this.mUsername = un;
        initialize(inflater);
        mTextboxUsername.setText(un);
        final UserStructure us = mDatabaseEngine.getUsernamePassword(un);
        mTextboxPassword.setHint("(unchanged)");


        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setView(mView)
               .setTitle("Update user")
               .setPositiveButton("UPDATE",null)
        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flagAddUpdate = false;
                String tb_username = mTextboxUsername.getText().toString();
                String tb_password = mTextboxPassword.getText().toString();
                if(tb_password.isEmpty()){
                    if(tb_username != us.getUsername()){
                        if(add_update(tb_username, us.getPassword())){
                            //Save new username to preference for service
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(SettingsActivity.KEY_CURRENT_USERNAME, tb_username);
                            //XXX: Should we save current position for recovery
                            editor.apply();
                            dialog.dismiss();
                        }
                    }else{
                        dialog.dismiss();
                    }
                }else if(add_update(mTextboxUsername.getText().toString(), mTextboxPassword.getText().toString())){
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }//end of edit

    /**
     * Displays dialog box and manages insertion of information in database
     * @param inflater to instantiate layout
     * @return object of Dialog created
     */
    public Dialog add(LayoutInflater inflater){
        initialize(inflater);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setView(mView)
               .setTitle("Add User")
               .setPositiveButton("SAVE", null)
        .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flagAddUpdate = true;
                String tb_username = mTextboxUsername.getText().toString();
                String tb_password = mTextboxPassword.getText().toString();
                if(add_update(tb_username, tb_password)){
                    //Save new username to preference for service
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SettingsActivity.KEY_CURRENT_USERNAME, tb_username);
                    //XXX: Should we save current position for recovery
                    editor.apply();
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }

    /**
     * Displays dialog box asking confirmation to delete information
     * @param un is the username to be deleted
     * @return object of Dialog created
     */
    public Dialog delete(String un) {
        this.mUsername = un;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + mUsername)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //String username = spinner_user_list.getSelectedItem().toString();
                        boolean updated = mDatabaseEngine.deleteUser(mUsername);
                        if ( updated ){
                            showToast("Successfully deleted user: " + mUsername, Toast.LENGTH_SHORT);
                            //XXX: Save the new username in Preferences here
                        }else{
                            showToast("Problem deleting user: " + mUsername, Toast.LENGTH_SHORT);
                        }
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
            Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    return dialog;
    }

    /**
     * Takes care if checked/unchecked box for showing/not showing password
     */
    private void show_password() {
        if (mChbShowPwd.isChecked()) {
            mTextboxPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            return;
        }
        mTextboxPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }//end of show_password(View)

}

