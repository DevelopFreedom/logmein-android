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
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.developfreedom.logmein.ui.MainActivity;
import org.developfreedom.logmein.ui.SettingsActivity;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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
    private boolean prefUseNotifications;
    private boolean prefNeedPersistence;
    private boolean perfWifiStartupLogin;
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
            }catch (Exception e) {
                e.printStackTrace();
            }
            final String action = intent.getAction();

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                /* We're connected properly */
                NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                //This implies the WiFi connection is through
                if(nwInfo != null
                        && NetworkInfo.State.CONNECTED.equals(nwInfo.getState())){
                    // Send login request
                    if (perfWifiStartupLogin
                            && isWifiLoginable()) {
                        login();
                    }
                }
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                /* Using this to show notif as soon as possible, else can be merged above */
                if (isWifiLoginable()) {
                    // Show notification
                    showNotification();
                } else {
                    // End notification since WiFi is not loginable
                    try {
                        mNotificationManager.cancelAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        networkEngine = NetworkEngine.getInstance(this);
        databaseEngine = DatabaseEngine.getInstance(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        // Display a notification about us starting.
        prefUseNotifications = preferences.getBoolean(SettingsActivity.KEY_USE_NOTIF, SettingsActivity.DEFAULT_KEY_USE_NOTIFICATION);
        prefNeedPersistence = preferences.getBoolean(SettingsActivity.KEY_NOTIF_PERSISTENCE, SettingsActivity.DEFAULT_KEY_NOTIF_PERSISTENCE);
        perfWifiStartupLogin = preferences.getBoolean(SettingsActivity.KEY_WIFI_STARTUP_LOGIN, SettingsActivity.DEFAULT_KEY_WIFI_STARTUP_LOGIN);
        mNotificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("org.developfreedom.ping");
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
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
//        showNotification();
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
     * Check if WiFi needs credentials
     * @return true if credentials needed
     */
    public boolean isWifiLoginable() {
        final ArrayList<String> desired_ssid_list = new ArrayList<String>();
        desired_ssid_list.add("pu@campus");
        desired_ssid_list.add("\"pu@campus\"");
        WifiManager wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                for(String desired_ssid: desired_ssid_list) {
                    if (desired_ssid.equalsIgnoreCase(ssid))
                        return true;
                }
            }
        }
        return false;
    }

   /**
     * Show a notification while this service is running.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {
        if(!prefUseNotifications) return;
        int API = android.os.Build.VERSION.SDK_INT;//TODO: global
        if (API < Build.VERSION_CODES.JELLY_BEAN) return;

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
                .setContentText("Expand to login/logout")
                .setSmallIcon(R.drawable.notif_ic_main)
                .setContentIntent(contentLaunchIntent)
                .setOngoing(prefNeedPersistence)
                //.setAutoCancel(true)
                //.setStyle(new Notification.BigTextStyle().bigText(longText))
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
                Log.d("LoginService", "URL host : " + url.getHost() + "&& URL Connection Host : " + urlConnection.getURL().getHost());

                if (!urlConnection.getURL().getHost().contains("google")) {
                    Log.d("LoginService", "User not Logged In");
                    showNotification();
                } else {
                    mNotificationManager.cancelAll();
                    Log.d("LoginService", "User Already Logged In");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return isLoggedIn;
        }
    }
    /**
     * Uses the username saved in SharedPreferences and it' corresponding password
     * from database to login. For each login attempt result is stored as 'status'
     */
    public void login() {
        NetworkEngine.StatusCode status = null;
        Log.d("Service", "Insiide Login");
        String username, password;
        // Use username/password from textbox if both filled
        //username = getSelectedUsername();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        username = preferences.getString(SettingsActivity.KEY_CURRENT_USERNAME,SettingsActivity.DEFAULT_KEY_CURRENT_USERNAME);
        UserStructure user = databaseEngine.getUsernamePassword(username);
        if (user != null) {
            password = user.getPassword();

            if (password == null
                    || password.isEmpty()) {
                Toast.makeText(this, "Password not saved for " + username, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                status = networkEngine.login(username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//end login

    /**
     * Attempts to logout from the current logged in network
     */
    public void logout() {
        NetworkEngine.StatusCode status = null;
        Log.d("Service", "Insiede Logout");
        try {
            status = networkEngine.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}//end class LoginService
