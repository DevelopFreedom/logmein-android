package in.shubhamchaudhary.logmein.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import in.shubhamchaudhary.logmein.DatabaseEngine;
import in.shubhamchaudhary.logmein.R;

public class FragmentEdit extends Fragment {

    static View v;
    EditText textbox_username, textbox_password;
    CheckBox cb_show_password;
    Button button_save,button_cancel;
    DatabaseEngine de;
    UserStructure activity_user;
    boolean add_update;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        v = inflater.inflate(
                R.layout.fragment_edit_layout, container, false);
        cb_show_password = (CheckBox) v.findViewById(R.id.cb_show_password);
        textbox_username = (EditText) v.findViewById(R.id.edit_username);
        textbox_password = (EditText) v.findViewById(R.id.edit_password);
        button_save = (Button) v.findViewById(R.id.button_edit_save);
        de = DatabaseEngine.getInstance(container.getContext());
        button_cancel = (Button) v.findViewById(R.id.button_edit_cancel);

        // when user clicks on this checkbox, this is the handler.
//TODO: this is not functional.....the one in UserDatabase is!!!
//        cb_show_password.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // checkbox status is changed from uncheck to checked.
//                if (!isChecked) {
//                    // show password
//                    textbox_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                } else {
//                    // hide password
//                    textbox_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                }
//            }
//        });


        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"Activity cancelled",Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
                /*the following can also be used to remove activity from stack !!! couldn't make out which way
                is better.....if we want to save some data from this fragment then the method above won't suffice !!!*/
//                pop_fragment();
//                ((UserDatabase)getActivity()).buttons_enabled(true);
            }
        });

        add_update = (Boolean)getArguments().getBoolean("add_update");
        if(add_update){
            textbox_username.setText("");
            textbox_password.setText("");
        } else{
            activity_user = (UserStructure) getArguments().getSerializable("user");
            Log.e("a_u", activity_user.getUsername());
            textbox_username.setText(activity_user.getUsername());
            textbox_password.setText(activity_user.getPassword());

        }

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserStructure updated_user = new UserStructure();
                updated_user.setPassword(textbox_password.getText().toString());
                updated_user.setUsername(textbox_username.getText().toString());
                Log.e("username", textbox_username.getText().toString());

                if(add_update){
                    saveCredential();
                }else {
                    int i = de.updateUser(updated_user, activity_user.getUsername());
                    //TODO: show pop ups instead of logs
                    Boolean flag = false;
                    if (i == 1) {
                        Log.e("Updated", "Updated user");
                        Toast.makeText(getActivity(), "Updated account", Toast.LENGTH_SHORT).show();
                        flag = true;
                    } else if (i == 0) {
                        Toast.makeText(getActivity(), "Problem in updating account", Toast.LENGTH_SHORT).show();
                        Log.e("Updated", "Error updating");
                    } else {
                        Toast.makeText(getActivity(), "Updated more than 1 records", Toast.LENGTH_SHORT).show();
                        Log.e("Updated", "Updated more than 1 records");
                        flag = true;
                    }

                    if (flag) {
                        ((UserDatabase) getActivity()).update_spinner_list(activity_user.getUsername(), textbox_username.getText().toString());
                        pop_fragment();
                    } else {
                        //TODO: pop up a dialog box to show error
                    }
                }
            }
        });

        return v;
    }//end of onCreate
    private void pop_fragment(){
        getFragmentManager().popBackStack();
    }//end of pop_fragment

    void saveCredential() {
        UserStructure userStructure = new UserStructure();
        userStructure.setUsername(textbox_username.getText().toString());
        userStructure.setPassword(textbox_password.getText().toString());

        if(!de.existsUser(userStructure.getUsername())){
            if(de.insert(userStructure)){
                Toast.makeText(getActivity(), userStructure.getUsername() + " entered into your inventory", Toast.LENGTH_SHORT).show();
                textbox_password.clearComposingText();
            } else {
                Toast.makeText(getActivity()," problem inserting record", Toast.LENGTH_SHORT).show();
            }

        } else{
            //TODO: pop up an alert here
            Toast.makeText(getActivity(),"Username already exists", Toast.LENGTH_SHORT).show();
        }

    }//end saveCredential
}
// vim: set ts=4 sw=4 tw=79 et :
