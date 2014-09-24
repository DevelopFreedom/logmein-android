package in.shubhamchaudhary.logmein.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import in.shubhamchaudhary.logmein.DatabaseEngine;
import in.shubhamchaudhary.logmein.R;
import in.shubhamchaudhary.logmein.UserStructure;

public class ManageUser extends ActionBarActivity implements DialogAlert.ReturnDialogMessage{

    Button update, add, delete;
    boolean add_update;
    String username;
    DatabaseEngine databaseEngine;
    DialogAlert dialogAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        databaseEngine = DatabaseEngine.getInstance(this);

        update = (Button) findViewById(R.id.button_update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_user();
            }
        });

        add = (Button) findViewById(R.id.button_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_user();
            }
        });

        delete = (Button) findViewById(R.id.button_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_user();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_user, menu);
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

    public void update_user() {
        add_update = false;
        show_dialog_box();
    }

    public void add_user() {

        add_update = true;
        show_dialog_box();
    }

    public void delete_user() {
        showDeleteDialog("Delete User", "Are you sure you want to delete " + username, "YES", "NO").show();
    }


    public void onClickPositive(String local_username, String password){

        if( local_username.isEmpty()){
            Toast.makeText(this,"Username cannot be an empty string",Toast.LENGTH_LONG).show();
            return;
        }
        if( password.isEmpty()){
            Toast.makeText(this,"Password cannot be an empty string",Toast.LENGTH_LONG).show();
            return;
        }
        UserStructure userStructure = new UserStructure();
        userStructure.setUsername(local_username);
        userStructure.setPassword(password);

        if(add_update){
            saveCredential(userStructure);
        }else{
            updateCredentials(userStructure);
        }
        dialogAlert.dismiss();
    }

    public void onClickNegative(){
        Toast.makeText(this,"Activity cancelled",Toast.LENGTH_SHORT).show();

        dialogAlert.dismiss();

    }
    public void show_dialog_box() {
        dialogAlert = new DialogAlert();

        if (add_update) {
            dialogAlert.setAlertStrings("Add user", "", "SAVE", "CANCEL");
        } else {
            dialogAlert.setAlertStrings("Update user", "", "UPDATE", "CANCEL");
            UserStructure user = databaseEngine.getUsernamePassword(username);

            if (user != null) {
                dialogAlert.fill_textboxes(username, user.getPassword());
            } else {
                Toast.makeText(this, "Problem fetching record for username: " + username, Toast.LENGTH_SHORT).show();
            }
        }
        dialogAlert.show(getSupportFragmentManager(), null);

    }//end of edit_user_profile


    void saveCredential(UserStructure userStructure) {

        if(!databaseEngine.existsUser(userStructure.getUsername())){
            if(databaseEngine.insert(userStructure)){
                Toast.makeText(this, userStructure.getUsername() + " entered into your inventory", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this," problem inserting record", Toast.LENGTH_SHORT).show();
            }

        } else{
            Toast.makeText(this,"Username already exists", Toast.LENGTH_SHORT).show();
        }

    }//end saveCredential

    public void updateCredentials(UserStructure userStructure){
        int i = databaseEngine.updateUser(userStructure, username);
        if (i == 1) {
            Log.e("Updated", "Updated user");
            Toast.makeText(this, "Updated account", Toast.LENGTH_SHORT).show();
        } else if (i == 0) {
            Toast.makeText(this, "Problem in updating account", Toast.LENGTH_SHORT).show();
            Log.e("Updated", "Error updating");
        } else {
            Toast.makeText(this, "Updated more than 1 records", Toast.LENGTH_SHORT).show();
            Log.e("Updated", "Updated more than 1 records");
        }

    }//end of updateCredentials


    public Dialog showDeleteDialog(String title, String message,String positive_message, String negative_message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Boolean deleted = databaseEngine.deleteUser(username);
                        if ( deleted ){
                            Toast.makeText(getApplicationContext(),"Successfully deleted user: "+username,Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Problem deleting user: "+username,Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(negative_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(),"Cancelled",Toast.LENGTH_SHORT).show();
                    }
                });

        return builder.create();

    }

}//end of class
