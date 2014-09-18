package in.shubhamchaudhary.logmein.ui;

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
        return v;
    }
}
