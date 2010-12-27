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
		double lengthInSecs = audioInputStream.getFrameLength() / (double)audioFormat.getFrameRate();
		soundLength = Math.round(lengthInSecs * 1000);
	}
	
	public File getFile() {
		return file;
	}
	
	public String getName() {
		return spotName;
	}
	
	public void setName(String name) {
		spotName = name;
	}
	
	public long getLengthInMillis() {
		return soundLength;
	}
	
	public boolean pointsToSameFileAs(SpotEntry candidate) {
		System.out.println("Comparing " + candidate.getFile().getAbsolutePath() + "\nto        " + this.getFile().getAbsolutePath() + " RESULT: " + candidate.getFile().getAbsolutePath().equals(this.getFile().getAbsolutePath()));
		return candidate.getFile().getAbsolutePath().equals(this.getFile().getAbsolutePath());
	}
}
