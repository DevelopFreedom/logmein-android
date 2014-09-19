package in.shubhamchaudhary.logmein.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

import in.shubhamchaudhary.logmein.DatabaseEngine;
import in.shubhamchaudhary.logmein.R;

/**
 * Created by tanjot on 18/9/14.
 */
public class FragmentDelete extends Fragment {
    Spinner spinner_user_list;
    Button button_delete;
    ArrayList<String> user_list;
    DatabaseEngine databaseEngine;
    ArrayAdapter adapter;
    View v;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        //Inflate the layout for this fragment
Log.e("here","here");
        v = inflater.inflate(
                R.layout.fragment_delete_user, container, false);

        databaseEngine = DatabaseEngine.getInstance(container.getContext());
        spinner_user_list = (Spinner) v.findViewById(R.id.spinner_duser_list);
        user_list = databaseEngine.userList();
Log.e("user_list",""+user_list.size());
        adapter = new ArrayAdapter<String>(v.getContext(), R.layout.spinner_layout,user_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner_user_list.setAdapter(adapter);

        button_delete = (Button) v.findViewById(R.id.button_delete);
        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DialogAlert da = new DialogAlert();

                setAlertStrings("Delete User", "Are you sure you want to delete " + spinner_user_list.getSelectedItem(), "YES", "NO");
                //da.show(getActivity().getSupportFragmentManager(),"delete_user" );
                showDialog().show();
            }
        });
        return v;
    }


    /*Not using this method here..... instead make another fragment file to make it generic i.e. usable for other alerts*/
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
    public Dialog showDialog() {
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
