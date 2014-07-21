package in.shubhamchaudhary.logmein;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentEdit extends Fragment {
@Override
public View onCreateView(LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {
   
	       //Inflate the layout for this fragment
	        
	      return inflater.inflate(
	              R.layout.fragment_edit_layout, container, false);
	   }
}
