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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.developfreedom.logmein.ui.MainActivity;
import org.developfreedom.logmein.ui.SettingsActivity;

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
    private NotificationManager mNotificationManager;
    private boolean prefUseNotifications;
    private boolean prefNeedPersistence;
    private SharedPreferences preferences;
    NetworkEngine networkEngine;
    DatabaseEngine databaseEngine;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!isWifiLoginable()) {
                try {
                    mNotificationManager.cancelAll();
                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
            }
            showNotificationOrStop();
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
        mNotificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(receiver, filter);
//        showNotificationOrStop();
        Log.i("LoginService", "Login service created");
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
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    //TODO: check visibility
    protected void showNotificationOrStop() {
        //Only show expanding notification after version 16 i.e Jelly Bean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if(prefUseNotifications)
                showPersistentNotification();
        } else {
            //XXX: Stop service only because we can't show notification right
            stopService(new Intent(this, LoginService.class));
        }
    }

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
                .setOngoing(prefNeedPersistence)
                //.setAutoCancel(true)
                //.setStyle(new Notification.BigTextStyle().bigText(longText))
                .addAction(R.drawable.notif_button_login, "Login", contentLoginIntent)
                .addAction(R.drawable.notif_button_logout, "Logout", contentLogoutIntent)
                .build();
        mNotificationManager.notify(0, notif);
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
    }//end logout

}//end class LoginService
