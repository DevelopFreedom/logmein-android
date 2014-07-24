package in.shubhamchaudhary.logmein;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

public class FragmentEdit extends Fragment {

	DatabaseEngine databaseEngine;
	Spinner spinner_ul;
	@Override

public View onCreateView(LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {
   
	       //Inflate the layout for this fragment
	        
			UserStructure user = (UserStructure)getArguments().getSerializable("user");
			Log.e("In FRagEdit un",user.getUsername());
			Log.e("In FRagEdit pwd",user.getPassword());
				
	      return inflater.inflate(
	              R.layout.fragment_edit_layout, container, false);
	   }
}
