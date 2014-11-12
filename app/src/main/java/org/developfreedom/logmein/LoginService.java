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
package org.developfreedom.logmein;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import org.developfreedom.logmein.ui.MainActivity;
import org.developfreedom.logmein.ui.SettingsActivity;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Login Service runs in background to manage various tasks like
 * handling the backend checks for wifi connection, for notification etc.
 * <p>
 * TODO: More Documentation
 */
public class LoginService extends Service {

    /** Notification ID used throughout */
    private final int ID = 2603;    //TODO: ID used for showing notification not unique
    /** For showing and hiding our notification. */
    public static NotificationManager mNotificationManager;
    private boolean prefNeedPersistence;
    private SharedPreferences preferences;
    NetworkEngine networkEngine;
    DatabaseEngine databaseEngine;
    Boolean isLoggedIn;
    Ping m_ping = new Ping();
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                m_ping.cancel(true);
                m_ping.execute();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        networkEngine = NetworkEngine.getInstance(this);
        databaseEngine = DatabaseEngine.getInstance(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        // Display a notification about us starting.
        prefNeedPersistence = preferences.getBoolean(SettingsActivity.KEY_PERSISTENCE, SettingsActivity.DEFAULT_KEY_PERSISTENCE);
        mNotificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.developfreedom.ping");
        registerReceiver(receiver, filter);
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent("org.developfreedom.ping"), 0);
        alarmMgr = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
        Log.i("LoginService", "Login service created");
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 300000, alarmIntent);
        m_ping.execute();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LoginService", "performing onStartCommand");
//        showNotificationOrStop();
        return 1;
    }

    @Override
    public void onDestroy() {
        Log.d("Service","Destroying notifs");
        // Cancel the persistent notification.
        mNotificationManager.cancel(ID);
        mNotificationManager.cancelAll();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /*
     * Other functions and classes
     */

    /**
     * Show a notification if possible else stop service
     */
    void showNotificationOrStop() {
        //Only show expanding notification after version 16 i.e Jelly Bean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (prefNeedPersistence) {
                showPersistentNotification();
            }
        } else {
            //XXX: Stop service only because we can't show notification right
            stopService(new Intent(this, LoginService.class));
        }
    }

    /**
     * Show a notification while this service is running.
     */
    @TargetApi(16)
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
        Intent logoutIntent = new Intent(this, MainActivity.class);
        logoutIntent.putExtra("methodName","logout");

        // The PendingIntent to launch our activity if the user selects this
        // notification
        PendingIntent contentLaunchIntent = PendingIntent.getActivity(this, -1,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent contentLoginIntent = PendingIntent.getActivity(this, -2,
                loginIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent contentLogoutIntent = PendingIntent.getActivity(this, -3,
                logoutIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif  = new Notification.Builder(this)
                .setContentTitle("LogMeIn")
                .setContentText("Click below to login/logout")
                .setSmallIcon(R.drawable.notif_ic_main)
                .setContentIntent(contentLaunchIntent)
                .setOngoing(true)
                .addAction(R.drawable.notif_button_login, "Login", contentLoginIntent)
                .addAction(R.drawable.notif_button_logout, "Logout", contentLogoutIntent)
                .build();
        mNotificationManager.notify(0, notif);
    }

    public class Ping extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... input_strings) {
            try {
                URL url = new URL("http://google.com");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Log.d("LoginService","URL host : " + url.getHost() + "&& URL Connection Host : " + urlConnection.getURL().getHost());

                if (!urlConnection.getURL().getHost().contains("google")) {
                    Log.d("LoginService", "User not Logged In");
                    showNotificationOrStop();
                }else{
                    mNotificationManager.cancelAll();
                    Log.d("LoginService", "User Already Logged In");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return isLoggedIn;
        }
    }

}//end class LoginService
