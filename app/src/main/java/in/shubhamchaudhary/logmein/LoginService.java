/**
 *   LogMeIn - Automatically log into Panjab University Wifi Network
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *
 *   This file is part of LogMeIn.
 *
 *   LogMeIn is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LogMeIn is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LogMeIn.  If not, see <http://www.gnu.org/licenses/>.
 */
package in.shubhamchaudhary.logmein;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import in.shubhamchaudhary.logmein.ui.MainActivity;
import in.shubhamchaudhary.logmein.ui.SettingsActivity;

public class LoginService extends Service {

    private final int ID = 2603;    //TODO: ID used for showing notification not unique
    /**
     * For showing and hiding our notification.
     */
    private NotificationManager mNotificationManager;
    private boolean prefNeedPersistence;
    NetworkEngine networkEngine;
    DatabaseEngine databaseEngine;

    @Override
    public void onCreate() {
        networkEngine = NetworkEngine.getInstance(this);
        databaseEngine = DatabaseEngine.getInstance(this);
        mNotificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        // Display a notification about us starting.
        Log.i("LoginService", "Login service created");

        prefNeedPersistence = true; //TODO
        if (prefNeedPersistence) {
            showPersistentNotification();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LoginService", "performing onStartCommand");
        if (prefNeedPersistence)
            showPersistentNotification();
        return 1;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNotificationManager.cancel(ID);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Other functions and classes
     */

    /**
     * Show a notification while this service is running.
     */
    private void showPersistentNotification() {
        int API = android.os.Build.VERSION.SDK_INT;//TODO: global
        if (API < 16) return;
        final int NOTIFICATION_ID = ID; // TODO: Random id again
        final String notification_title = "Lazy Music";  //TODO: Get from R.string
        final CharSequence notification_text = "Lazy Music at your Service";
        Notification.Builder builder = new Notification.Builder(this);

        Intent launchIntent = new Intent(this, MainActivity.class);
        Intent loginIntent = new Intent(this, MainActivity.class);
        loginIntent.putExtra("methodName","login");    //FIXME: Not best way to invoke login

        // The PendingIntent to launch our activity if the user selects this
        // notification
        PendingIntent contentLaunchIntent = PendingIntent.getActivity(this, -1,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent contentLoginIntent = PendingIntent.getActivity(this, -1,
                loginIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif  = new Notification.Builder(this)
                .setContentTitle("LogMeIn")
                .setContentText("Click below to login")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(contentLaunchIntent)
                .setOngoing(true)
                //.setAutoCancel(true)
                //.setStyle(new Notification.BigTextStyle().bigText(longText))
                .addAction(R.drawable.ic_launcher, "Login", contentLoginIntent)
                //.addAction(R.drawable.ic_launcher, "Logout", contentLaunchIntent)
                .build();
        mNotificationManager.notify(0, notif);
    }

    void login() {
        NetworkEngine.StatusCode status = null;
        Log.d("Service", "Insiide Login");
        String username, password;
        // Use username/password from textbox if both filled
        //username = getSelectedUsername();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        username = preferences.getString(SettingsActivity.KEY_CURRENT_USERNAME,SettingsActivity.DEFAULT_KEY_CURRENT_USERNAME);
        password = databaseEngine.getUsernamePassword(username).getPassword();

        if(password.isEmpty()){
            Toast.makeText(this, "Password not saved for " + username, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            status = networkEngine.login(username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end login

    void logout() {
        NetworkEngine.StatusCode status = null;
        Log.d("Service", "Insiede Logout");
        try {
            status = networkEngine.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end logout

}//end class LoginService