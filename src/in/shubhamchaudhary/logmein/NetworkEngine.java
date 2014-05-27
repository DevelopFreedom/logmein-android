package in.shubhamchaudhary.logmein;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

public class NetworkEngine {

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

	public void logout() throws Exception {
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					logout_runner();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));

		while ((line = reader.readLine()) != null) {
			Log.w("html", line);
		}
		writer.close();
		reader.close();
	}

	private void logout_runner() throws Exception {
		System.out.println("Loggin out");
		URL puServerUrl = new URL("http://172.16.4.201/cgi-bin/login?cmd=logout");
		URLConnection puServerConnection = puServerUrl.openConnection();

		//Get inputStream and show output
		BufferedReader htmlBuffer = new BufferedReader(new InputStreamReader(puServerConnection.getInputStream()));
		//TODO parse output
		String inputLine;
		while ((inputLine = htmlBuffer.readLine()) != null){
			Log.w("html", inputLine);
		}
		htmlBuffer.close();
	}

}

/*
//Bypass android.os.NetworkOnMainThreadException
StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

StrictMode.setThreadPolicy(policy);
 */
