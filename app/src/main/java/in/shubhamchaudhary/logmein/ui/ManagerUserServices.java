package in.shubhamchaudhary.logmein.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import in.shubhamchaudhary.logmein.DatabaseEngine;

/**
 * Created by tanjot on 4/10/14.
 */
public class ManagerUserServices {

//    MainActivity context;
    Context context;
    DatabaseEngine databaseEngine;
    String username;
    Boolean updated;

    ManagerUserServices(Context context){

//        aclass = context;
        this.context = context;
        databaseEngine = DatabaseEngine.getInstance(this.context);
        updated = false;
    }

    public void delete(String un) {
        this.username = un;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + username)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.e("Dleetee","positive");
                        //String username = spinner_user_list.getSelectedItem().toString();
                        updated = databaseEngine.deleteUser(username);
                        if ( updated ){
                            Toast.makeText(context, "Successfully deleted user: " + username, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context,"Problem deleting user: "+username,Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context,"Cancelled",Toast.LENGTH_SHORT).show();
                        Log.e("Dleetee","negative");
                    }
                });

        builder.create().show();
    }
}

