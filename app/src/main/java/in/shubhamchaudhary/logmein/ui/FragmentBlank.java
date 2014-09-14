package in.shubhamchaudhary.logmein.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.shubhamchaudhary.logmein.R;

public class FragmentBlank extends Fragment{
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

               //Inflate the layout for this fragment

              return inflater.inflate(
                      R.layout.fragment_blank, container, false);
           }
}
// vim: set ts=4 sw=4 tw=79 et :
