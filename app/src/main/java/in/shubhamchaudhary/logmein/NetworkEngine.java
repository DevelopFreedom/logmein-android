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

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class NetworkEngine {

    public String BASE_URL = "http://172.16.4.201/cgi-bin/login";
    private static NetworkEngine instance = null;
    private static int use_count = 0;   //like semaphores
    Context m_context;

    public NetworkEngine(Context context) {
        m_context = context;
    }

    // Singleton method with lazy initialization.
    public static synchronized NetworkEngine getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkEngine(context);
        }
        use_count += 1;
        return instance;
    }

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

    public StatusCode login(final String username, final String password) throws Exception {
        NetworkTask longRunningTask = new NetworkTask(m_context);
        longRunningTask.execute("login", username, password);
        return longRunningTask.return_status;
    }

    public NetworkEngine.StatusCode logout() throws Exception {
        NetworkTask longRunningTask = new NetworkTask(m_context);
        longRunningTask.execute("logout");
        return longRunningTask.return_status;
    }

    private StatusCode login_runner(String username, String password) throws Exception {
        if (username == null || password == null) {
            Log.wtf("Error", "Either username or password is null");
            return StatusCode.CREDENTIAL_NONE;
        }
        //System.out.println("Loggin in with "+username+password);
        String urlParameters = "user=" + username + "&password=" + password; // "param1=a&param2=b&param3=c";

        String request = BASE_URL;
        URL puServerUrl = new URL(request);

        URLConnection puServerConnection = puServerUrl.openConnection();
        puServerConnection.setDoOutput(true);

        //FIXME: Handle protocol exception
        OutputStream stream = null; //XXX Wrong
        try {
            stream = puServerConnection.getOutputStream();
        } catch (java.net.ConnectException e) {
            e.printStackTrace();
            Log.d("NetworkEngine", "Connection Exception");
            return StatusCode.CONNECTION_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Output
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(urlParameters);
        writer.flush();
        StatusCode returnStatus = null;

        String lineBuffer;
        BufferedReader htmlBuffer = null; //FIXME Null pointer exceptions eminent, May the forth be with you!!!
        try {
            htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
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
            writer.close();
            htmlBuffer.close();
        } catch (java.net.ProtocolException e) {
            returnStatus = StatusCode.LOGGED_IN;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnStatus;
    }

    private NetworkEngine.StatusCode logout_runner() throws Exception {
        System.out.println("Loggin out");
        URL puServerUrl = new URL(BASE_URL+"?cmd=logout");
        URLConnection puServerConnection = puServerUrl.openConnection();

        //Get inputStream and show output
        BufferedReader htmlBuffer = null;   //XXX
        try {
            htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
        } catch (java.net.ConnectException e) {
            e.printStackTrace();
            Log.d("NetworkEngine", "Connection Exception");
            return StatusCode.CONNECTION_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO parse output
        String lineBuffer;
        StatusCode returnStatus = null;
        while ((lineBuffer = htmlBuffer.readLine()) != null && returnStatus == null) {

            if (lineBuffer.contains("Logout")) {
                returnStatus = StatusCode.LOGOUT_SUCCESS;
            } else if (lineBuffer.contains("User not logged in")) {
                returnStatus = StatusCode.NOT_LOGGED_IN;
            }
            Log.w("html", lineBuffer);
        }
        htmlBuffer.close();
        return returnStatus;
    }

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
            outputText = "You're not logged in " + getSelectedUsername();
        } else if (status == null) {
            Log.d("NetworkEngine", "StatusCode was null in login");
            outputText = "null";
        } else {
            outputText = "Unknown Login status";
        }
        return outputText;
    }

    //Class Variables
    public enum StatusCode {
        LOGIN_SUCCESS, AUTHENTICATION_FAILED, MULTIPLE_SESSIONS,
        CREDENTIAL_NONE, LOGOUT_SUCCESS, NOT_LOGGED_IN, LOGGED_IN,
        CONNECTION_ERROR,
    }

    public String getSelectedUsername() {
        View rootView = ((Activity)m_context).getWindow().getDecorView().findViewById(android.R.id.content);
        return (String) ((Spinner)rootView.findViewById(R.id.spinner_user_list)).getSelectedItem();
    }

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
    }
}

// vim: set ts=4 sw=4 tw=79 et :
