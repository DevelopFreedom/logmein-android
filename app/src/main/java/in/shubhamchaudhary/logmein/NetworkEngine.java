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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import android.util.Log;

public class NetworkEngine {

	//Class Variables
	public enum StatusCode {
		LOGIN_SUCCESS,  AUTHENTICATION_FAILED, MULTIPLE_SESSIONS,
		CREDENTIAL_NONE, LOGOUT_SUCCESS, NOT_LOGGED_IN, LOGGED_IN,
		CONNECTION_ERROR,
	};

	public StatusCode login(final String username, final String password) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<StatusCode> callable = new Callable<StatusCode>() {
			@Override
			public StatusCode call() {
				try {
					return login_runner(username,password);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		Future<StatusCode> future = executor.submit(callable);
		executor.shutdown();
		return future.get();
/*
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					login_runner(username,password);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		return StatusCode.LOGIN_SUCCESS;	//XXX
*/
	}

	public NetworkEngine.StatusCode logout() throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<StatusCode> callable = new Callable<StatusCode>() {
			@Override
			public StatusCode call() {
				try {
					return logout_runner();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		Future<StatusCode> future = executor.submit(callable);
		executor.shutdown();
		return future.get();
	}

	private StatusCode login_runner(String username, String password) throws Exception{
		if (username == null || password == null){
			Log.wtf("Error", "Either username or password is null");
			return StatusCode.CREDENTIAL_NONE;
		}
		//System.out.println("Loggin in with "+username+password);
		String urlParameters = "user="+username+"&password="+password; // "param1=a&param2=b&param3=c";

		String request = "http://172.16.4.201/cgi-bin/login";
		URL puServerUrl = new URL(request);

		URLConnection puServerConnection = puServerUrl.openConnection();
		puServerConnection.setDoOutput(true);

		//FIXME: Handle protocol exception
		OutputStream stream = null;	//XXX Wrong
		try {
			stream = puServerConnection.getOutputStream();
		}catch(java.net.ConnectException e){
			e.printStackTrace();
			Log.d("NetworkEngine","Connection Exception");
			return StatusCode.CONNECTION_ERROR;
		}catch (Exception e){
			e.printStackTrace();
		}

		//Output
		OutputStreamWriter writer = new OutputStreamWriter(stream);
		writer.write(urlParameters);
		writer.flush();
		StatusCode returnStatus = null;

		String lineBuffer;
		BufferedReader htmlBuffer = null; //FIXME Null pointer exceptions eminent, May the forth be with you!!!
		try{
			htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
			while (((lineBuffer = htmlBuffer.readLine()) != null) && returnStatus == null) {
				if (lineBuffer.contains("External Welcome Page")){
					Log.d("NetworkEngine", "External Welcome Match");
					returnStatus = StatusCode.LOGIN_SUCCESS;
				}else if (lineBuffer.contains("Authentication failed")){
					returnStatus = StatusCode.AUTHENTICATION_FAILED;
				}else if (lineBuffer.contains("Only one user login session is allowed")){
					returnStatus = StatusCode.MULTIPLE_SESSIONS;
				}else{
					Log.i("html", lineBuffer);
				}
			}
			writer.close();
			htmlBuffer.close();
		}catch(java.net.ProtocolException e){
			returnStatus = StatusCode.LOGGED_IN;
		}catch(Exception e){
			e.printStackTrace();
		}
		return returnStatus;
	}

	private NetworkEngine.StatusCode logout_runner() throws Exception {
		System.out.println("Loggin out");
		URL puServerUrl = new URL("http://172.16.4.201/cgi-bin/login?cmd=logout");
		URLConnection puServerConnection = puServerUrl.openConnection();

		//Get inputStream and show output
		BufferedReader htmlBuffer = null;	//XXX
		try {
			htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
		}catch(java.net.ConnectException e){
			e.printStackTrace();
			Log.d("NetworkEngine","Connection Exception");
			return StatusCode.CONNECTION_ERROR;
		}catch (Exception e){
			e.printStackTrace();
		}
		//TODO parse output
		String lineBuffer;
		StatusCode returnStatus = null;
		while ((lineBuffer = htmlBuffer.readLine()) != null && returnStatus == null){

			if (lineBuffer.contains("Logout")){
				returnStatus = StatusCode.LOGOUT_SUCCESS;
			}else if(lineBuffer.contains("User not logged in")){
				returnStatus = StatusCode.NOT_LOGGED_IN;
			}
			Log.w("html", lineBuffer);
		}
		htmlBuffer.close();
		return returnStatus;
	}

}

/*
//Bypass android.os.NetworkOnMainThreadException
StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

StrictMode.setThreadPolicy(policy);
 */
