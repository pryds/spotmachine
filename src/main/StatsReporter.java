package main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
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
		
		File statsDir = Util.get().getCollectedStatsDir();
		while(true) {
			Util.get().threadSleep(1000*60); // every minute
			
			File[] reports = statsDir.listFiles();
			Util.get().out("Found " + reports.length + " unreported stat files." + (reports.length==0 ? "" : " Trying to report it/them."), Util.VERBOSITY_DEBUG_INFO);
			for (int i = 0; i < reports.length; i++) {
				try {
					Util.get().out("Reporting contents of stats file " + reports[i].getName(), Util.VERBOSITY_DETAILED_DEBUG_INFO);
					BufferedReader reader = new BufferedReader(new FileReader(reports[i]));
					StringBuffer str = new StringBuffer();
					String readLine;
					while ((readLine = reader.readLine()) != null) {
						str.append(readLine + "\n");
					}
					reader.close();
					if (str.length() != 0) {
						String reply = reportText(str.toString());
						if (reply == null) {
							throw new Exception("Error when reporting stats");
						} else {
							Util.get().out("Reply from webserver: " + reply, Util.VERBOSITY_DETAILED_DEBUG_INFO);
						}
					} else {
						Util.get().out("Empty stats file found: " + reports[i].getAbsolutePath() + ". Deleting without reporting.", Util.VERBOSITY_WARNING);
					}
					Util.get().deleteFile(reports[i]);
				} catch (Exception e) {
					Util.get().out("Warning: Something went wrong when trying to report stats file: " + e.toString(), Util.VERBOSITY_WARNING);
				}
				Util.get().threadSleep(1000*60);
			}
		}
	}
		
	private static String reportText(String text) {
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
