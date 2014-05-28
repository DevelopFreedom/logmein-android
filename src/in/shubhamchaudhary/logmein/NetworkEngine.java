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
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import android.util.Log;

public class NetworkEngine {

	//Class Variables
	public enum StatusCode { 
		LOGIN_SUCCESS, LOGOUT_SUCCESS, AUTHENTICATION_ERROR, LOGGED_IN
	};

	public void login(final String username, final String password) throws Exception {
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
	}

	public NetworkEngine.StatusCode logout() throws Exception {
//		Thread thread = new Thread(new Runnable(){
//			@Override
//			public void run() {
//				try {
//					logout_runner();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		thread.start();
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
		// future.get() returns 2
		executor.shutdown();
		return future.get();
	}

	private void login_runner(String username, String password) throws Exception{
		if (username == null || password == null){
			Log.wtf("Error", "Either username or password is null");
			return;
		}
		System.out.println("Loggin in with "+username+password);
		//String username = "11uit424", password = "screwYou";
		String urlParameters = "user="+username+"&password="+password; // "param1=a&param2=b&param3=c";

		String request = "http://172.16.4.201/cgi-bin/login";
		URL puServerUrl = new URL(request);

		URLConnection puServerConnection = puServerUrl.openConnection();
		puServerConnection.setDoOutput(true);
		OutputStreamWriter writer = new OutputStreamWriter(puServerConnection.getOutputStream());
		writer.write(urlParameters);
		writer.flush();

		//Output
		String line;
		//FIXME: Handle protocol exception
		BufferedReader reader = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));

		while ((line = reader.readLine()) != null) {
			Log.w("html", line);
		}
		writer.close();
		reader.close();
	}

	private NetworkEngine.StatusCode logout_runner() throws Exception {
		System.out.println("Loggin out");
		URL puServerUrl = new URL("http://172.16.4.201/cgi-bin/login?cmd=logout");
		URLConnection puServerConnection = puServerUrl.openConnection();

		//Get inputStream and show output
		BufferedReader htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
		//TODO parse output
/*
		 if re.search('Logout',the_page):
	            print ('Logout successful');
	        elif re.search('User not logged in',the_page):
	            print('You\'re not logged in');
*/
		String inputLine;
		while ((inputLine = htmlBuffer.readLine()) != null){

			if (Pattern.matches("Logout", inputLine)){
				return StatusCode.LOGOUT_SUCCESS;
			}
			Log.w("html", inputLine);
		}
		htmlBuffer.close();
		return null;
	}

}

/*
//Bypass android.os.NetworkOnMainThreadException
StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

StrictMode.setThreadPolicy(policy);
 */
