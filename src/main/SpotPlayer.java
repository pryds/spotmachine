package main;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;

public class SpotPlayer extends SpotContainer implements Runnable {
	private AudioInputStream audioInputStream;
	private AudioFormat audioFormat;
	
	private boolean paused;
	private long millisBetweenSpots;
	private int nextSpotToPlay;
	
	private static final int BUFFER_SIZE = 128000;

	public SpotPlayer(int type) {
		super(type);
		paused = false;
		millisBetweenSpots = Prefs.prefs.getLong(Prefs.MILLIS_BETWEEN_SPOTS, Prefs.MILLIS_BETWEEN_SPOTS_DEFAULT);
		nextSpotToPlay = 0;
	}
	
	public void run() { // invoke with start()
		while(true) {
			if (!paused && playQueue.size() > 0) {
				play(playQueue.get(nextSpotToPlay).getFile());
				nextSpotToPlay++;
				if (nextSpotToPlay >= playQueue.size())
					nextSpotToPlay = 0;
				waitForMilliseconds(millisBetweenSpots);
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void play(File file) {
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
		try {
			line = (SourceDataLine)AudioSystem.getLine(info);
			line.open(audioFormat);
		} catch(LineUnavailableException e) {
			JOptionPane.showMessageDialog(SpotMachine.getMainFframe(),
				    "Lydkortet er optaget af et andet program.\nHvis andre programmer kører, fx en internet browser, prøv da at lukke disse.",
				    "Fejl ved lyd",
				    JOptionPane.ERROR_MESSAGE);

			System.err.print("Lydkortet er optaget af et andet program. Hvis andre programmer kører, fx en internet browser, prøv da at lukke disse.");
			System.exit(1);
		} catch(Exception e) {
			e.printStackTrace(); System.exit(1);
		}
		line.start();
		
		/**
		 * Read from file into buffer and write to sound card
		 */
		System.out.println("Playing file " + file.getName());
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
		}
		line.drain(); //wait till sound has finished playing
		line.close();
	}
	
	private void waitForMilliseconds(long millis) {
		System.out.println("Waiting for " + (millis/(float)1000/60) + " minutes.");
		Date waitStart = Calendar.getInstance().getTime();
		Date waitNow = Calendar.getInstance().getTime();
		
		while (waitNow.getTime() - waitStart.getTime() < millis) {
			long millisLeft = millis - (waitNow.getTime() - waitStart.getTime());
			SpotMachine.getMainFframe().setCountDownFieldValue(millisLeft);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			waitNow = Calendar.getInstance().getTime();
		}
	}
		
	public boolean isPaused() {
		return paused;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	/**
	public long getMillisBetweenSpots() {
		return millisBetweenSpots;
	}
	**/
	
	public void setMillisBetweenSpots(long millis) {
		this.millisBetweenSpots = millis;
		Prefs.prefs.putLong(Prefs.MILLIS_BETWEEN_SPOTS, millis);
	}

}
