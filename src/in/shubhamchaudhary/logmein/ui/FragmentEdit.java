package in.shubhamchaudhary.logmein.ui;

import in.shubhamchaudhary.logmein.R;
import in.shubhamchaudhary.logmein.R.id;
import in.shubhamchaudhary.logmein.R.layout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

public class FragmentEdit extends Fragment {

	EditText username,password;
	@Override

	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {

		//Inflate the layout for this fragment

		UserStructure user = (UserStructure)getArguments().getSerializable("user");
		View v = inflater.inflate(
				R.layout.fragment_edit_layout, container, false);
		username = (EditText)v.findViewById(R.id.edit_username);
		password = (EditText)v.findViewById(R.id.edit_password);
		username.setText(user.getUsername());
		password.setText(user.getPassword());

		return v;
	}
}
