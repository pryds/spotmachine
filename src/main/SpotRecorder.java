package main;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
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
		outFile = null;
		targetDataLine = null;
		while (outFile == null || outFile.exists()) { // make sure file doesn't already exist (though unlikely)
			outFile = new File(Util.get().getDataStoreDir(), Util.get().createLowerCaseRandomWAVFilename());
		}
		
		AudioFormat audioFormat = new AudioFormat(
				Encoding.PCM_SIGNED, // encoding
				44100.0F, // sample rate
				16, // sample size in bits
				2, // channels
				4, // frame size
				44100.0F, // frame rate
				false // true = big endian
		);
		
		Info info = new Info(TargetDataLine.class, audioFormat);
		try {
			targetDataLine = (TargetDataLine)AudioSystem.getLine(info);
			targetDataLine.open(audioFormat);
		} catch (LineUnavailableException e) {
			System.err.println("Unable to get recording line. Exiting.");
			System.exit(1);
		}
		
		targetType = Type.WAVE;
		audioInputStream = new AudioInputStream(targetDataLine);

	}

	public void run() { // starts recording, will run as long as there is something to record
		System.out.println("Starting recording...");
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
					try {
						Thread.sleep(200);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
		
		// Start actual recording
		try {
			AudioSystem.write(audioInputStream, targetType, outFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		recordingHasEnded = true;
	}
	
	public void stopRecoding() {
		System.out.println("Stopping recording...");
		targetDataLine.stop();
		targetDataLine.close();
		System.out.println("Recording stopped.");
	}
	
	public File getOutFile() {
		return outFile;
	}
}
