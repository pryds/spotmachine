package main;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class SpotEntry {
	private String spotName;
	private File file;
	private long soundLength;
	
	public SpotEntry(File file, String spotName) {
		this.file = file;
		this.spotName = spotName;
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch(Exception e) {
			e.printStackTrace();
		}
		AudioFormat audioFormat = audioInputStream.getFormat();
		//soundLength = (Long)audioFormat.getProperty("duration"); // microseconds
		double lengthInSecs = audioInputStream.getFrameLength() / (double)audioFormat.getFrameRate();
		soundLength = Math.round(lengthInSecs * 1000);
	}
	
	public File getFile() {
		return file;
	}
	
	public String getName() {
		return spotName;
	}
	
	public long getLengthInMillis() {
		return soundLength;
	}
}
