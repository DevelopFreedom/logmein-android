package in.shubhamchaudhary.logmein.ui;

import in.shubhamchaudhary.logmein.R;
import in.shubhamchaudhary.logmein.R.id;
import in.shubhamchaudhary.logmein.R.layout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class FragmentEdit extends Fragment {

	EditText username,password;
	static View v;
	@Override

	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {

		//Inflate the layout for this fragment

		UserStructure user = (UserStructure)getArguments().getSerializable("user");
		v = inflater.inflate(
				R.layout.fragment_edit_layout, container, false);
		username = (EditText)v.findViewById(R.id.edit_username);
		password = (EditText)v.findViewById(R.id.edit_password);
		username.setText(user.getUsername());
		password.setText(user.getPassword());

		return v;
	}//end of onCreate
//	
//	public void show_password_edit_fragment(){
//		CheckBox cb_show_pwd = (CheckBox)v.findViewById(R.id.cb_show_password);
//		if(cb_show_pwd.isChecked()){
//			password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//			return;
//		}
//		password.setTransformationMethod(PasswordTransformationMethod.getInstance());
//	}
}
