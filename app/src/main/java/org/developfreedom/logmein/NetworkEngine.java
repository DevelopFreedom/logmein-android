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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.developfreedom.logmein.ui.SettingsActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Network Engine is the main interface used to perform network tasks
 * like login, logout etc.
 * <p>
 */
public class NetworkEngine {

    /** The url where login request will be posted */
    public String BASE_URL = "https://securelogin.arubanetworks.com/cgi-bin/login";
    public String LOGOUT_URL = "https://securelogin.arubanetworks.com/cgi-bin/login?cmd=logout";
    public String mUsernameField = "username";
    public String mPasswordField = "password";
    private static NetworkEngine instance = null;
    private static int use_count = 0;   //like semaphores
    Context m_context;

    public NetworkEngine(Context context) {
        m_context = context;
    }


    /**
     * Singleton method with lazy initialization.
     * Desired way to create/access the Engine object
     * @param context Context in which the notification and toasts will be displayed.
     * @return Reference to singleton object of Engine
     */
    public static synchronized NetworkEngine getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkEngine(context);
        }
        use_count += 1;
        return instance;
    }

    /**
     * Start the login task in a background task
     * @param context to display toasts
     * @param username the data for username field
     * @param password the data for password field
     * @return enum StatusCode explaining the situation
     * @throws Exception
     */
    public StatusCode login(final Context context, String username, final String password) throws Exception {
        NetworkTask longRunningTask = new NetworkTask(context) {
            @Override
            protected StatusCode doInBackground(String... input_strings) {
                username = input_strings[0];
                password = input_strings[1];
                try {
                    return_status = login_runner(username, password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return return_status;
            }
        };
        longRunningTask.execute(username, password);
        return longRunningTask.return_status;
    }

    /**
     * Start the login task in a background task with default/old context
     * @param username the data for username field
     * @param password the data for password field
     * @return enum StatusCode explaining the situation
     * @throws Exception
     */
    public StatusCode login(final String username, final String password) throws Exception {
        NetworkTask longRunningTask = new NetworkTask(m_context);
        longRunningTask.execute("login", username, password);
        return longRunningTask.return_status;
    }

    /**
     * Perform logout in a background thread
     * @return
     * @throws Exception
     */
    public NetworkEngine.StatusCode logout() throws Exception {
        NetworkTask longRunningTask = new NetworkTask(m_context);
        longRunningTask.execute("logout");
        return longRunningTask.return_status;
    }

    /**
     * TODO: check documentation
     * Attempts to login into the network using credentials provided as parameters
     * @param username
     * @param password
     * @return status of login attempt
     * @throws Exception
     */
    private StatusCode login_runner(String username, String password) throws Exception {
        if (username == null || password == null) {
            Log.wtf("Error", "Either username or password is null");
            return StatusCode.CREDENTIAL_NONE;
        }
        //System.out.println("Loggin in with "+username+password);
        String urlParameters = mUsernameField + "=" + username + "&" + mPasswordField + "=" + password; // "param1=a&param2=b&param3=c";

        String request = BASE_URL + "?cmd=login";
        URL puServerUrl = new URL(request);

        URLConnection puServerConnection = puServerUrl.openConnection();
        puServerConnection.setDoOutput(true);

        //FIXME: Handle protocol exception
        StatusCode returnStatus = null;
        //TODO: use try-with-resources
        try {
            OutputStream stream = puServerConnection.getOutputStream();
            //Output
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(urlParameters);
            writer.flush();

            String lineBuffer;
            try {
                BufferedReader htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
                try {
                    while (((lineBuffer = htmlBuffer.readLine()) != null) && returnStatus == null) {
                        if (lineBuffer.contains("External Welcome Page")) {
                            Log.d("NetworkEngine", "External Welcome Match");
                            returnStatus = StatusCode.LOGIN_SUCCESS;
                        } else if (lineBuffer.contains("Authentication failed")) {
                            returnStatus = StatusCode.AUTHENTICATION_FAILED;
                        } else if (lineBuffer.contains("Only one user login session is allowed")) {
                            returnStatus = StatusCode.MULTIPLE_SESSIONS;
                        } else {
                            Log.i("html", lineBuffer);
                        }
                    }
                }finally {
                    htmlBuffer.close();
                }
            }
            catch (java.net.ProtocolException e) {
                returnStatus = StatusCode.LOGGED_IN;
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                writer.close();
            }
        } catch (java.net.ConnectException e) {
            e.printStackTrace();
            Log.d("NetworkEngine", "Connection Exception");
            return StatusCode.CONNECTION_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnStatus;
    }

    /**
     * TODO: check documentation
     * Attempt logout request to network
     * @return
     * @throws Exception
     */
    private NetworkEngine.StatusCode logout_runner() throws Exception {
        System.out.println("Loggin out");
        URL puServerUrl = new URL(LOGOUT_URL);
        URLConnection puServerConnection = puServerUrl.openConnection();

        StatusCode returnStatus = null;
        //TODO: use try-with-resources
        try {
            //Get inputStream and show output
            BufferedReader htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
            try {
                //TODO parse output
                String lineBuffer;
                while ((lineBuffer = htmlBuffer.readLine()) != null && returnStatus == null) {

                    if (lineBuffer.contains("Logout")) {
                        returnStatus = StatusCode.LOGOUT_SUCCESS;
                    } else if (lineBuffer.contains("User not logged in")) {
                        returnStatus = StatusCode.NOT_LOGGED_IN;
                    }
                    Log.w("html", lineBuffer);
                }
            }finally {
                htmlBuffer.close();
            }
        } catch (java.net.ConnectException e) {
            e.printStackTrace();
            Log.d("NetworkEngine", "Connection Exception");
            return StatusCode.CONNECTION_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnStatus;
    }

    /**
     * TODO: check documentation
     * Sets network status at current time in a string
     * @param status
     * @return status string
     */
    public String get_status_text(StatusCode status) {
        String outputText;    //To be shown in User Text Box
        if (status == NetworkEngine.StatusCode.LOGIN_SUCCESS) {
            outputText = "Login Successful";
        } else if (status == NetworkEngine.StatusCode.CREDENTIAL_NONE) {
            outputText = "Either username or password in empty";
        } else if (status == NetworkEngine.StatusCode.AUTHENTICATION_FAILED) {
            outputText = "Authentication Failed";
        } else if (status == NetworkEngine.StatusCode.MULTIPLE_SESSIONS) {
            outputText = "Only one user login session is allowed";
        } else if (status == NetworkEngine.StatusCode.LOGGED_IN) {
            outputText = "You're already logged in";
        } else if (status == NetworkEngine.StatusCode.CONNECTION_ERROR) {
            outputText = "There was a connection error";
        } else if (status == NetworkEngine.StatusCode.LOGOUT_SUCCESS) {
            outputText = "Logout Successful";
        } else if (status == NetworkEngine.StatusCode.NOT_LOGGED_IN) {
            outputText = "You're not logged in";
        } else if (status == null) {
            Log.d("NetworkEngine", "StatusCode was null in login");
            outputText = "Unable to perform the operation";
        } else {
            outputText = "Unknown operation status";
        }
        return outputText;
    }

    /* Class Variables */
    /**
     * A collection of various situations that might occur in Engine
     */
    public enum StatusCode {
        LOGIN_SUCCESS, AUTHENTICATION_FAILED, MULTIPLE_SESSIONS,
        CREDENTIAL_NONE, LOGOUT_SUCCESS, NOT_LOGGED_IN, LOGGED_IN,
        CONNECTION_ERROR,
    }

    /**
     * @return the username selected in the spinner via preferences
     */
    public String getSelectedUsername() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_context);
        return preferences.getString(SettingsActivity.KEY_CURRENT_USERNAME,SettingsActivity.DEFAULT_KEY_CURRENT_USERNAME);
/*
        View rootView = ((Activity)m_context).getWindow().getDecorView().findViewById(android.R.id.content);
        return (String) ((Spinner)rootView.findViewById(R.id.spinner_user_list)).getSelectedItem();
*/
    }

    /**
     * NetworkTask is an AsyncTask that can run in background
     * and is capable of sending login and logout requests without
     * blocking the main thread.
     */
    public class NetworkTask extends AsyncTask<String, Void, StatusCode> {
        String username, password;
        StatusCode return_status;
        Context m_context;

        public NetworkTask(Context context) {
            m_context = context;
        }

        @Override
        protected StatusCode doInBackground(String... input_strings) {

            String operation = input_strings[0];
            try {
                if (operation.equals("login")) {
                    username = input_strings[1];
                    password = input_strings[2];
                    BASE_URL = extractBaseUrl();
                    Log.d("NetworkEngine", "Base URL : " + BASE_URL);
                    extractParameters(BASE_URL);
                    return_status = login_runner(username, password);
                } else if (operation.equals("logout")) {
                    return_status = logout_runner();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return return_status;
        }

        @Override
        protected void onPostExecute(NetworkEngine.StatusCode status) {
            Toast.makeText(
                    m_context,
                    get_status_text(status),
                    Toast.LENGTH_SHORT
            ).show();
        }

        /**
         * Extracts the login URL of the network
         * @return String containing the login URL of that network
         */
        private String extractBaseUrl() throws IOException {
            Log.i("NetworkEngine", "Inside extractBaseUrl");
            String googleUrl = "http://www.google.com";
            String tryUrl = googleUrl;
            HashMap content;
            content = httpRequest(tryUrl);
            while(content != null && (Integer)content.get("responseCode") != 200){
                tryUrl = content.get("location").toString();
                content = httpRequest(tryUrl);
            }
            if(content == null){
                return null;
            }
            //Got 200! Now check if it has redirection js or not!!![local server page]
            String pattern = "/http:\\/\\/[(\\d+\\.)]+(?:(?![\"']).)*/g";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(content.get("html").toString());
            if(m.find()){
                return m.group(0);
            }else{
                return tryUrl; //No url type http://123.123.123.123/something?=/anythingTill" found
            }
        }

        /**
         * Extract the first two input type field from form
         * @param url
         * @throws IOException
         */
        private void extractParameters(String url) throws IOException{
            Log.i("NetworkEngine", "Inside extractParameters");
            HashMap content;
            content = httpRequest(url);
            String pageSource = content.get("html").toString();
            Document s = Jsoup.parse(pageSource);
            Elements forms  = s.getElementsByTag("form");
            if(forms.size() == 0){
                Log.i("NetworkEngine", "Form Not found!");
            }
            for(Element form : forms){
                Elements fields = form.getElementsByTag("input");
                for(Element field : fields){
                    if(field.toString().toLowerCase().contains("user")){
                        mUsernameField = field.attr("name");
                    }else if(field.toString().toLowerCase().contains("pass")){
                        mPasswordField = field.attr("name");
                    }
                }
            }
            Log.d("NetworkEngine", "Username Field: " + mUsernameField + "\nPassword Field: " + mPasswordField);
        }

        /**
         * Sends the http request to the specified url and checks whether we are being redirected or not
         * @param url : URL to send http Request
         * @return hashmap containing response code and source code of that page if no redirection
         * @return else hashmap with response code and URL which we are redirected to
         * @throws IOException
         */
        public HashMap httpRequest (String url) throws IOException{
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setInstanceFollowRedirects(false);
            con.connect();

            HashMap hm = new HashMap();
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                InputStream is = con.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(is));
                String line, source="";
                while ((line = bf.readLine()) != null) {
                    source += line + "\n";
                }
                hm.put("responseCode", responseCode);
                hm.put("html",source);
                return hm;
            }else if (responseCode == 301 || responseCode == 302) {
                hm.put("responseCode", responseCode);
                hm.put("location", con.getHeaderField("Location"));
                return hm;
            }
            return null;
        }
    }
}

// vim: set ts=4 sw=4 tw=79 et :
