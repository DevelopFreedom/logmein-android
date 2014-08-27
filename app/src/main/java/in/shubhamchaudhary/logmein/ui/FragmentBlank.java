package in.shubhamchaudhary.logmein.ui;

import in.shubhamchaudhary.logmein.R;
import in.shubhamchaudhary.logmein.R.layout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentBlank extends Fragment{
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
	   
		       //Inflate the layout for this fragment
		        
		      return inflater.inflate(
		              R.layout.fragment_blank, container, false);
		   }
}
