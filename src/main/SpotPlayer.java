package main;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;

public class SpotPlayer extends SpotContainer {
	private AudioInputStream audioInputStream;
	private AudioFormat audioFormat;
	
	protected boolean paused;
		
	private static final int BUFFER_SIZE = 128000;
	
	public SpotPlayer(int type) {
		super(type);
	}
	
	
	protected void play(File file) {
		/**
		 * Open file stream
		 */
		audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch(Exception e) {
			e.printStackTrace(); System.exit(1);
		}
		audioFormat = audioInputStream.getFormat();
		/**
		 * Open line for writing to sound card
		 */
		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		boolean lineAvailable = true;
		int currentTry = -1;
		int totalTries = 10;
		do {
		    try {
		        int forceMixer = Prefs.prefs.getInt(Prefs.FORCE_PLAY_ON_MIXER_NUMBER, Prefs.FORCE_PLAY_ON_MIXER_NUMBER_DEFAULT);
		        if (forceMixer >= 0) {
		            javax.sound.sampled.Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		            if (forceMixer < mixerInfo.length) {
		                Util.get().out("Getting line for forced mixer #" + forceMixer, Util.VERBOSITY_DEBUG_INFO);
		                Mixer mixer = AudioSystem.getMixer(mixerInfo[forceMixer]);
		                line = (SourceDataLine)mixer.getLine(info);
		            } else {
		                String availableMixers = ""; int number = 0;
		                for (javax.sound.sampled.Mixer.Info i : mixerInfo)
		                    availableMixers += "[#" + (number++) + "] " + i.getName() + " - " + i.getDescription() + " - " + i.getVendor() + "\n";
		                Util.get().out("Error: Output mixer #" + forceMixer + " was forced, yet only " + mixerInfo.length + " mixers exist:\n" + availableMixers, Util.VERBOSITY_ERROR);
		                System.exit(1);
		            }
		        } else {
                    Util.get().out("Getting line for default mixer", Util.VERBOSITY_DEBUG_INFO);
		            line = (SourceDataLine)AudioSystem.getLine(info);
		        }
    			line.open(audioFormat);
    			lineAvailable = true;
    		} catch(LineUnavailableException e) {
    			Util.get().out(e.toString(), Util.VERBOSITY_ERROR);
    			lineAvailable = false;
                Util.get().out("Warning: Line unavailable. This is try " + (currentTry+2) + " of " + totalTries, Util.VERBOSITY_WARNING);
    			Util.get().threadSleep(2000);
    		} catch(Exception e) {
    			e.printStackTrace(); System.exit(1);
    		}
    		currentTry++;
		} while (!lineAvailable && currentTry < totalTries);
		if (!lineAvailable) {
	        JOptionPane.showMessageDialog(SpotMachine.getMainFrame(),
	                Util.get().string("main-soundboardbusy-errordialogue"),
	                Util.get().string("main-soundboardbusy-errordialoguetitle"),
	                JOptionPane.ERROR_MESSAGE);
	        System.exit(1);
		}
		
		line.start();

		/**
		 * Read from file into buffer and write to sound card
		 */
		Util.get().out("Playing file " + file.getName(), Util.VERBOSITY_DEBUG_INFO);
		int nBytesRead = 0;
		byte[] dataBuffer = new byte[BUFFER_SIZE];
		while(nBytesRead != -1) {
			try {
				nBytesRead = audioInputStream.read(dataBuffer, 0, dataBuffer.length);
			} catch(Exception e) {
				e.printStackTrace(); System.exit(1);
			}
			if (nBytesRead >= 0) {
				line.write(dataBuffer, 0, nBytesRead); 
			}
			if (paused)
				break;
		}
		Util.get().out("Draining line...", Util.VERBOSITY_DEBUG_INFO);
		line.drain(); //wait till sound has finished playing
		line.close();
	}
}
