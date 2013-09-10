package main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class StatsReporter implements Runnable {
	
	public void run() {
		//Check here whether it's ok to collect (and thereby report) stats:
		while (!Prefs.prefs.getBoolean(Prefs.COLLECT_STATISTICS, Prefs.COLLECT_STATISTICS_DEFAULT)) {
			Util.get().out("StatsReporter: User opted not to allow collection of statistics, or did not " +
					"yet decide so. Not reporting any stats for now.", Util.VERBOSITY_DEBUG_INFO);
			Util.get().threadSleep(1000*60);
		}
		
		while(true) {
			Util.get().threadSleep(1000*60); // every minute
			
			
		}
	}
		
	public static String reportText(String text) {
		URL url;
		String urlStr = "", paramStr = "";
		HttpURLConnection con = null;
		try {
			urlStr = "http://pryds.eu/spotmachine/acceptstats.php";
			paramStr = "data=" + URLEncoder.encode(text, "UTF-8");
			Util.get().out("Preparing to make http request to server:\n" + urlStr + "\nwith data:\n" + paramStr, Util.VERBOSITY_DEBUG_INFO);
			
			//Create connection
			url = new URL(urlStr);
			con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Content-Length", "" + Integer.toString(paramStr.getBytes().length));
			con.setRequestProperty("Content-Language", "en-US");
			con.setUseCaches(false);
			con.setDoInput(true);
			con.setDoOutput(true);
			
			//Send request
			DataOutputStream dos = new DataOutputStream(con.getOutputStream());
			dos.writeBytes(paramStr);
			dos.flush();
			dos.close();
			
			//Get response
			InputStream is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = br.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			br.close();
			
			return response.toString();
		} catch (Exception e) {
			Util.get().out("StatsReporter: Something went wrong trying to connect to http.\nURL: " + urlStr + "\nParams: " + paramStr + "\nError:\n" + e, Util.VERBOSITY_ERROR);
			return null;
		} finally {
			if (con != null)
				con.disconnect();
		}
	}

}
