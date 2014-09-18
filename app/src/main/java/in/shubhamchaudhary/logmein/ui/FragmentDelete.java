package in.shubhamchaudhary.logmein.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.shubhamchaudhary.logmein.R;

/**
 * Created by tanjot on 18/9/14.
 */
public class FragmentDelete extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment

        return inflater.inflate(
                R.layout.fragment_delete_user, container, false);
    }
}
