package main;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.DataLine.Info;

public class SpotRecorder implements Runnable {
	
	private File outFile;
	private TargetDataLine targetDataLine;
	private AudioInputStream audioInputStream;
	private Type targetType;
	private boolean recordingHasEnded = false;
	
	public SpotRecorder() {
		outFile = Util.get().createUniqueLowerCaseRandomWAVFileInDataDir();
		targetDataLine = null;
		
		AudioFormat audioFormat = new AudioFormat(
				Encoding.PCM_SIGNED, // encoding
				44100.0F, // sample rate
				16, // sample size in bits
				1, // channels
				2, // frame size
				44100.0F, // frame rate
				false // true = big endian
		);
		
		Info info = new Info(TargetDataLine.class, audioFormat);
		try {
		    int forceMixer = Prefs.prefs.getInt(Prefs.FORCE_RECORDING_ON_MIXER_NUMBER, Prefs.FORCE_RECORDING_ON_MIXER_NUMBER_DEFAULT);
            if (forceMixer >= 0) {
                javax.sound.sampled.Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                if (forceMixer < mixerInfo.length) {
                    Util.get().out("Getting line for forced recording mixer #" + forceMixer, Util.VERBOSITY_DEBUG_INFO);
                    Mixer mixer = AudioSystem.getMixer(mixerInfo[forceMixer]);
                    targetDataLine = (TargetDataLine)mixer.getLine(info);
                } else {
                    String availableMixers = ""; int number = 0;
                    for (javax.sound.sampled.Mixer.Info i : mixerInfo)
                        availableMixers += "[#" + (number++) + "] " + i.getName() + " - " + i.getDescription() + " - " + i.getVendor() + "\n";
                    Util.get().out("Error: Recording mixer #" + forceMixer + " was forced, yet only " + mixerInfo.length + " mixers exist:\n" + availableMixers, Util.VERBOSITY_ERROR);
                    System.exit(1);
                }
            } else {
                Util.get().out("Getting recording line for default mixer", Util.VERBOSITY_DEBUG_INFO);
                targetDataLine = (TargetDataLine)AudioSystem.getLine(info);
            }
			targetDataLine.open(audioFormat);
		} catch (LineUnavailableException e) {
			Util.get().out("Unable to get recording line. Exiting.", Util.VERBOSITY_ERROR);
			System.exit(1);
		}
		
		targetType = Type.WAVE;
		audioInputStream = new AudioInputStream(targetDataLine);

	}

	public void run() { // starts recording, will run as long as there is something to record
		Util.get().out("Starting target data line...", Util.VERBOSITY_DEBUG_INFO);
		targetDataLine.start();
		
		/**
		 * Inner thread class to count and show in GUI how many seconds
		 * has currently been recorded
		 */
		
		new Thread(new Runnable() {
			public void run() {
				//Note: The measurement will only be an approximation, as the start time is measured
				//by a different thread than the one which does the actual recording.
				long startTime = Calendar.getInstance().getTime().getTime();
				while (!recordingHasEnded) {
					long nowTime = Calendar.getInstance().getTime().getTime();
					SpotMachine.getMainFrame().getRecordDialogue().setCurrentDurationTextField(nowTime - startTime);
					Util.get().threadSleep(200);
				}
			}
		}).start();
		
		// Start actual recording
		try {
			Util.get().out("Starting recording...", Util.VERBOSITY_DEBUG_INFO);
			AudioSystem.write(audioInputStream, targetType, outFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		recordingHasEnded = true;
		Util.get().out("Record thread: Recording has ended.", Util.VERBOSITY_DEBUG_INFO);
	}
	
	public void stopRecoding() {
		Util.get().out("Requesting recording to stop...", Util.VERBOSITY_DEBUG_INFO);
		targetDataLine.stop();
		targetDataLine.close();
		Util.get().out("Recording stopped.", Util.VERBOSITY_DEBUG_INFO);
	}
	
	public File getOutFile() {
		return outFile;
	}
}
