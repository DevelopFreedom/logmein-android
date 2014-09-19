package in.shubhamchaudhary.logmein.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.support.v4.app.DialogFragment;

import in.shubhamchaudhary.logmein.R;

/**
 * Created by tanjot on 19/9/14.
 */
public class DialogAlert extends DialogFragment {

    String title = "Alert!!!";//""+R.string.alert_title;
    String message = "Do you want to proceed";//""+R.string.alert_message;
    String positive_message = "YES";//""+R.string.alert_positive_message;
    String negative_message = "NO";//""+R.string.alert_negative_message;

    //You can set title,message and positive and negative button strings as per your requirement
    public void setAlertStrings(String tit, String msg,String pos_msg, String neg_msg){
        this.title = tit;
        this.message = msg;
        this.positive_message = pos_msg;
        this.negative_message = neg_msg;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positive_message,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            })
            .setNegativeButton(negative_message,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            return builder.create();

        }
    }
