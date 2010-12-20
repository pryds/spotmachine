package main;

import java.io.File;
import java.net.URL;
import java.util.Random;

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
		String sysName = System.getProperty("os.name").toLowerCase();
		if (dataDir != null)
			return dataDir;
		
		if (sysName.contains("windows")) { // Windows special case
			String winDirStr = System.getenv("APPDATA");
			if (winDirStr != null) {
				File winDir = new File(winDirStr);
				if (winDir != null && winDir.exists() && winDir.isDirectory() && winDir.canWrite()) {
					dataDir = winDir;
				}
			}
		}
		
		if (dataDir == null) { // default: the current user's home directory
			String homeDirStr = System.getProperty("user.home");
			if (homeDirStr != null) {
				File homeDir = new File(homeDirStr);
				if (homeDir != null && homeDir.exists() && homeDir.isDirectory() && homeDir.canWrite()) {
					dataDir = homeDir;
				}
			}
		}
		
		if (dataDir == null) {
			return null;
		} else { // by now we have a writable dir in which we can make our own dir or use it if it already exists
			File saveDir = new File(dataDir, ".spotmachine");
			if (!saveDir.exists())
				saveDir.mkdir();
			return saveDir;
		}
	}
}
