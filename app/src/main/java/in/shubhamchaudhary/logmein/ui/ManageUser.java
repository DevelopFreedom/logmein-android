package in.shubhamchaudhary.logmein.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import in.shubhamchaudhary.logmein.R;

public class ManageUser extends ActionBarActivity {

    Button update,add,delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_manage_user);

       update = (Button) findViewById(R.id.button_update);
       update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_user();
            }
        });

       add = (Button) findViewById(R.id.button_add);
       add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_user();
            }
        });

        delete = (Button) findViewById(R.id.button_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_user();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void update_user(){
        Intent intent = new Intent(this, UserDatabase.class);
        //true for add and false for update
        intent.putExtra("add_update",false);
        startActivity(intent);

    }

    public void add_user(){
        Intent intent = new Intent(this, UserDatabase.class);
        //true for add and false for update
        intent.putExtra("add_update",true);
        startActivity(intent);

    }

    public void delete_user(){
        Intent intent = new Intent(this,DeleteUser.class);
        startActivity(intent);
    }
}
