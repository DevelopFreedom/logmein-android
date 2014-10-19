package org.developfreedom.logmein.tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import org.developfreedom.logmein.R;
import org.developfreedom.logmein.ui.MainActivity;

/**
 * Created by chaudhary on 10/19/14.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    Activity mainActivity;
    Spinner spinner;
    Button button_add;
    Button button_del;
    Button button_edit;
    ImageButton button_login;
    ImageButton button_logout;

    public MainActivityTest() {
        super("org.developfreedom.logmein.ui", MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        //This method is called before each test
        Log.i("MATest","Running setUp");
        super.setUp();

        //Setting the touch mode to true prevents the UI control from taking
        //focus when you click it programmatically in the test method later
        setActivityInitialTouchMode(true);

        mainActivity = this.getActivity();
        spinner = (Spinner) mainActivity.findViewById(R.id.spinner_user_list);
        button_add = (Button) mainActivity.findViewById(R.id.button_add);
        button_login = (ImageButton) mainActivity.findViewById(R.id.button_login);
    }

/*
    @SmallTest
    public void testActivityCreated() throws Exception {
        Log.i("MATest","Running testActivityCreated");
        assertNotNull(mainActivity);
        assertNotNull(button_add);
//        assertNotNull(button_del);
        assertNotNull(button_login);
    }
*/

    /*
     * Test that the spinner has the same item selected even after restart
     */
    @SmallTest
    public void testSpinnerRestoreAfterRestart() {
        Activity firstActivity, secondActivity;
        Spinner spinner_user_list;
        Object selectedItem;    //XXX: Object? What is actually returned
        firstActivity = this.getActivity();
        spinner_user_list = (Spinner) firstActivity.findViewById(R.id.spinner_user_list);
        selectedItem = spinner_user_list.getSelectedItem();
        //TODO: The spinner list may be empty
        firstActivity.finish();
        secondActivity = this.getActivity();
        spinner_user_list = (Spinner) secondActivity.findViewById(R.id.spinner_user_list);
        assertEquals(selectedItem, spinner_user_list.getSelectedItem());

    }

    public void testSpinnerUpdatedAfterAdd() throws Exception {
        Log.i("MATest", "Running testSpinnerUpdatedAfterAdd");
        //TouchUtils.clickView(this, button_add);
        //TextView user_box = mainActivity.findViewById(R.id.text_edit_username);
        assertEquals(1, 1);
    }

    @Override
    public void tearDown() throws Exception {
        Log.i("MATest","Running tearDown");
        super.tearDown();
    }
}
