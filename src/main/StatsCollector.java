package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatsCollector implements Runnable {

	public void run() {
		//Check here whether it's ok to collect stats:
		while (!Prefs.prefs.getBoolean(Prefs.COLLECT_STATISTICS, Prefs.COLLECT_STATISTICS_DEFAULT)) {
			Util.get().out("StatsCollector: User opted not to allow collection of statistics, or did not " +
					"yet decide so. Not collecting any stats for now.", Util.VERBOSITY_DEBUG_INFO);
			Util.get().threadSleep(1000*60);
		}
		
		String programInstallationId = getUniqueIdentifier();
		long millisBetweenCollecting = 100000; //TODO: Change to 1000 * 60 * 60 * 24 * 7; // a week(!)
		
		//wait if stats have been collected recently:
		while (System.currentTimeMillis()
				- Prefs.prefs.getLong(Prefs.LAST_COLLECTION_OF_STATS, Prefs.LAST_COLLECTION_OF_STATS_DEFAULT)
				< millisBetweenCollecting) {
			Util.get().out("Statistics have already been collected recently. Waiting.", Util.VERBOSITY_DEBUG_INFO);
			Util.get().threadSleep(1000*60);
		}
		
		//Do the actual collection of stats after waiting a bit to make sure everything has started:
		Util.get().threadSleep(1000*10); // 10 secs
		while (true) {
			//First re-check to make sure user hasn't disabled stats since last time
			if (!Prefs.prefs.getBoolean(Prefs.COLLECT_STATISTICS, Prefs.COLLECT_STATISTICS_DEFAULT)) {
				Util.get().out("User has disabled stats. Creating a new StatsCollector thread (that " +
						"will wait until stats are reenabled), and ending this one.", Util.VERBOSITY_DEBUG_INFO);
				
				(new Thread(new StatsCollector())).start();
				return;
			}
			
			Util.get().out("Collecting stats.", Util.VERBOSITY_DEBUG_INFO);
			StringBuffer outData = new StringBuffer();
			
			long now = System.currentTimeMillis();
			outData.append("installationID: " + programInstallationId + "\n"); //Unique Program Installation ID
			outData.append("reportCreated: " + now + "\n");
			outData.append("systemUptime: " + getSystemUptime() + "\n");
			//outData.append("spotmachineUptime: " + "\n");
			//TODO: First run of SpotMachine
			outData.append("systemTimeZone: " + TimeZone.getDefault().getID() + "\n");
			outData.append("systemLocale: " + Locale.getDefault().toString() + "\n");
			outData.append("spotmachineLanguage: " + Util.get().getCurrentLocale() + "\n");
			outData.append("spotmachineVersion: " + SpotMachine.PROGRAM_VERSION + "\n");
			
			//TODO: number of scheduled, intervalled spots
			// (average) spot length
			
			outData.append("osName: " + System.getProperty("os.name") + "\n");
			outData.append("osVersion: " + System.getProperty("os.version") + "\n");
			outData.append("osArch: " + System.getProperty("os.arch") + "\n");
			outData.append("jreVendor: " + System.getProperty("java.vendor") + "\n");
			outData.append("jreVersion: " + System.getProperty("java.version") + "\n");
			outData.append("jreInstallDir: " + System.getProperty("java.home") + "\n");
			outData.append("workingDir: " + System.getProperty("user.dir") + "\n");
			outData.append("classPath: " + System.getProperty("java.class.path") + "\n");
			
			outData.append("availableProcessorCores: " + Runtime.getRuntime().availableProcessors() + "\n");
			outData.append("totalMemInBytes: " + Runtime.getRuntime().totalMemory() + "\n");
			outData.append("freeMemInBytes: " + Runtime.getRuntime().freeMemory() + "\n");
			long maxMemory = Runtime.getRuntime().maxMemory();
			outData.append("maxMemToBeUsedInBytes: " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory) + "\n");
			File[] roots = File.listRoots();
			
			outData.append("fileRoot: ");
			for (int i = 0; i < roots.length; i++)
				outData.append("(" + i + ") " + roots[i].getAbsolutePath() + ", ");
			outData.append("\n");
			
			outData.append("fileRootTotalSpaceInBytes: ");
			for (int i = 0; i < roots.length; i++)
				outData.append("(" + i + ") " + roots[i].getTotalSpace() + ", ");
			outData.append("\n");
			
			outData.append("fileRootFreeSpaceInBytes: ");
			for (int i = 0; i < roots.length; i++)
				outData.append("(" + i + ") " + roots[i].getFreeSpace() + ", ");
			outData.append("\n");
			
			outData.append("fileRootUsableSpaceInBytes: ");
			for (int i = 0; i < roots.length; i++)
				outData.append("(" + i + ") " + roots[i].getUsableSpace() + ", ");
			outData.append("\n");
			
			Util.get().out("Collected stats:\n" + outData.toString(), Util.VERBOSITY_DETAILED_DEBUG_INFO);
			
			File outputfile = Util.get().createUniqueLowerCaseRandomStatsFileInStatsDir();
			try {
				Util.get().out("Writing stats file " + outputfile.getAbsolutePath(), Util.VERBOSITY_DEBUG_INFO);
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputfile));
				writer.write(outData.toString());
				writer.flush();
				writer.close();
			} catch (Exception e) {
				Util.get().out("Error writing stats file: " + e.toString(), Util.VERBOSITY_ERROR);
			}
			
			Prefs.prefs.putLong(Prefs.LAST_COLLECTION_OF_STATS, now);
			
			Util.get().threadSleep(millisBetweenCollecting);
		}
	}
	
	public String getUniqueIdentifier() {
		String id = Prefs.prefs.get(Prefs.UNIQUE_PROGRAM_INSTALLATION_IDENTIFIER, null);
		if (id == null) {
			id = Util.get().createLowerCaseRandomString();
			Prefs.prefs.put(Prefs.UNIQUE_PROGRAM_INSTALLATION_IDENTIFIER, id);
		}
		return id;
	}
	
	public static long getSystemUptime() {
		long uptime = -1;
		try {
		    String os = System.getProperty("os.name").toLowerCase();
		    if (os.contains("win")) {
		        Process uptimeProc = Runtime.getRuntime().exec("net stats srv");
		        BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
		        String line;
		        while ((line = in.readLine()) != null) {
		            if (line.startsWith("Statistics since")) {
		                SimpleDateFormat format = new SimpleDateFormat("'Statistics since' MM/dd/yyyy hh:mm:ss a");
		                Date boottime = format.parse(line);
		                uptime = System.currentTimeMillis() - boottime.getTime();
		                break;
		            }
		        }
		    } else if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
		        Process uptimeProc = Runtime.getRuntime().exec("uptime");
		        BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
		        String line = in.readLine();
		        if (line != null) {
		            Pattern parse = Pattern.compile("((\\d+) days,)? (\\d+):(\\d+)");
		            Matcher matcher = parse.matcher(line);
		            if (matcher.find()) {
		                String _days = matcher.group(2);
		                String _hours = matcher.group(3);
		                String _minutes = matcher.group(4);
		                int days = _days != null ? Integer.parseInt(_days) : 0;
		                int hours = _hours != null ? Integer.parseInt(_hours) : 0;
		                int minutes = _minutes != null ? Integer.parseInt(_minutes) : 0;
		                uptime = (minutes * 60000) + (hours * 60000 * 60) + (days * 6000 * 60 * 24);
		            }
		        }
		    }
		} catch (Exception e) {
			Util.get().out("Exception caught while trying to determine system uptime:\n" + e, Util.VERBOSITY_ERROR);
		}
		return uptime;
	}
}
