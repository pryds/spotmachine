package main;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import gui.MainFrame;

import javax.swing.ImageIcon;

public class Util {
	
	private Util() {
	}
	private static class UtilSingletonHolder { 
		public static final Util INSTANCE = new Util();
	}
	public static Util get() {
		return UtilSingletonHolder.INSTANCE;
	}


	public String millisToMinsSecsString(long milliSeconds) {
		long totalMinutes = milliSeconds / 1000;
		int hours = (int)(totalMinutes / 60);
		int minutes = (int)(totalMinutes % 60);
		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
	}
	
	public ImageIcon createImageIcon(String relativePath) {
		URL url = MainFrame.class.getResource(relativePath);
		if (url == null) {
			System.err.println("Resource not found: " + relativePath);
			System.exit(1);
		}
		return new ImageIcon(url);
	}
	
	public String createLowerCaseRandomWAVFilename() {
		Random rand = new Random();
		StringBuffer str = new StringBuffer();
		int nameLength = 16;
		int firstChar = 97; // 'a'
		int lastChar = 122; // 'z'
		
		for (int i = 0; i < nameLength; i++) {
			str.append((char)(rand.nextInt(lastChar - firstChar + 1) + firstChar));
		}
		str.append(".wav");
		return str.toString();
	}
	
	public void deleteFile(File file) {
		if (file.exists() && !file.isDirectory() && file.canWrite()) {
			file.delete();
			System.out.println("Deleting file " + file.getName());
		}
	}
	
	private File dataDir = null;
	
	public File getDataStoreDir() {
		// If we already figured out the data store dir in this session, no need to do so again
		if (dataDir != null)
			return dataDir;
		
		// Get dir from preferences, if exists and is still valid
		String prefsDirStr = Prefs.prefs.get(Prefs.DATA_DIR, null);
		if (prefsDirStr != null) {
			File prefsDir = new File(prefsDirStr);
			if (prefsDir != null && prefsDir.exists() && prefsDir.isDirectory() && prefsDir.canWrite()) {
				dataDir = prefsDir;
				return dataDir;
			}
		}
		
		// Figure out where to store data
		File appDataDir = null;
		String sysName = System.getProperty("os.name").toLowerCase();
		if (sysName.contains("windows")) { // Windows special case
			String winDirStr = System.getenv("APPDATA");
			if (winDirStr != null) {
				File winDir = new File(winDirStr);
				if (winDir != null && winDir.exists() && winDir.isDirectory() && winDir.canWrite()) {
					appDataDir = winDir;
				}
			}
		}
		
		if (appDataDir == null) { // default: the current user's home directory
			String homeDirStr = System.getProperty("user.home");
			if (homeDirStr != null) {
				File homeDir = new File(homeDirStr);
				if (homeDir != null && homeDir.exists() && homeDir.isDirectory() && homeDir.canWrite()) {
					appDataDir = homeDir;
				}
			}
		}
		
		if (appDataDir == null) {
			return null;
		} else { // by now we have a writable dir in which we can make our own dir or use it if it already exists
			File saveDir = new File(appDataDir, ".spotmachine");
			if (!saveDir.exists())
				saveDir.mkdir();
			Prefs.prefs.put(Prefs.DATA_DIR, saveDir.getAbsolutePath());
			return saveDir;
		}
	}
	
	public int[] IntegerVectorToIntArray(Vector<Integer> source) {
		int[] target = new int[source.size()];
		for (int i = 0; i < source.size(); i++) {
			target[i] = source.get(i).intValue();
		}
		return target;
	}
	
	public void reverseSort(int[] intArray) {
		Arrays.sort(intArray);
		for(int i = 0; i < intArray.length / 2; i++) {
		     int temp = intArray[i];
		     intArray[i] = intArray[intArray.length - (i + 1)];
		     intArray[intArray.length - (i + 1)] = temp;
		}
	}
}
