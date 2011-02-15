package main;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class SpotEntry {
	private String spotName;
	private File file;
	private long soundLength;
	private PlaySchedule schedule = null;
	
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
	
	public SpotEntry(File file, String spotName, PlaySchedule schedule) {
		this(file, spotName);
		this.schedule = schedule;
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
	
	public PlaySchedule getSchedule() {
		/**
		 * Return schedule instance, or null if this SpotEntry has no schedule.
		 */
		return schedule;
	}
	
	public void setSchedule(PlaySchedule schedule) {
		this.schedule = schedule;
	}
	
	public boolean hasSchedule() {
		return schedule != null;
	}
	
	public boolean pointsToSameFileAs(SpotEntry candidate) {
		Util.get().out(
				"Comparing " + candidate.getFile().getAbsolutePath() + 
				"\nto        " + this.getFile().getAbsolutePath() + 
				" RESULT: " + candidate.getFile().getAbsolutePath().equals(this.getFile().getAbsolutePath()),
				Util.VERBOSITY_DETAILED_DEBUG_INFO);
		return candidate.getFile().getAbsolutePath().equals(this.getFile().getAbsolutePath());
	}
	
	public SpotEntry duplicate() {
	    if (schedule != null)
	        return new SpotEntry(file, spotName, schedule.duplicate());
	    else
	        return new SpotEntry(file, spotName, null);
	}
}
