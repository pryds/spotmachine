package main;

import java.net.URL;
import java.util.Random;

import gui.MainFrame;

import javax.swing.ImageIcon;

public class Calculate {
	public static String millisToMinsSecsString(long milliSeconds) {
		long totalMinutes = milliSeconds / 1000;
		int hours = (int)(totalMinutes / 60);
		int minutes = (int)(totalMinutes % 60);
		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
	}
	
	public static ImageIcon createImageIcon(String relativePath) {
		URL url = MainFrame.class.getResource(relativePath);
		if (url == null) {
			System.err.println("Resource not found: " + relativePath);
			System.exit(1);
		}
		return new ImageIcon(url);
	}
	
	public static String createLowerCaseRandomWAVFilename() {
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
}
