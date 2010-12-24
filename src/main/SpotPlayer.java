package main;

import java.io.File;
import java.util.Calendar;

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
	private boolean repeatAll;
	private boolean inPlayLoop = true;
	
	private static final int BUFFER_SIZE = 128000;

	public SpotPlayer(int type) {
		super(type);
		if (type != TYPE_TEMPORARY) {
			paused = true;
			millisBetweenSpots = Prefs.prefs.getLong(Prefs.MILLIS_BETWEEN_SPOTS, Prefs.MILLIS_BETWEEN_SPOTS_DEFAULT);
			nextSpotToPlay = Prefs.prefs.getInt(Prefs.NEXT_SPOT_TO_PLAY, Prefs.NEXT_SPOT_TO_PLAY_DEFAULT);
			repeatAll = Prefs.prefs.getBoolean(Prefs.REPEAT_ALL, Prefs.REPEAT_ALL_DEFAULT);
		} else {
			paused = false;
			millisBetweenSpots = 2000;
			nextSpotToPlay = 0;
			repeatAll = false;
		}
	}
	
	public void run() { // invoke with start()
		while(true) {
			if (!paused && spotList.size() > 0) {
				play(spotList.get(nextSpotToPlay).getFile());
				
				if (nextSpotToPlay+1 < spotList.size())
					setNextSpotToPlayAndUpdateGUI(nextSpotToPlay + 1);
				else
					setNextSpotToPlayAndUpdateGUI(0);
				
				if (nextSpotToPlay == 0 && !repeatAll) {
					if (type == TYPE_TEMPORARY) {
						inPlayLoop = false;
						System.out.println("Exiting spotPlayer thread. Type " + type);
						return;
					} else {
						setPaused(true);
						SpotMachine.getMainFrame().setGUIPaused(true);
					}
				} else {
					waitForMilliseconds(millisBetweenSpots);
				}
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
			JOptionPane.showMessageDialog(SpotMachine.getMainFrame(),
				    "Lydkortet er optaget af et andet program.\nHvis andre programmer kører, fx en "
					+ "musik/videoafspiller, internet browser, prøv da at lukke disse.",
				    "Fejl ved lyd",
				    JOptionPane.ERROR_MESSAGE);
			System.err.println(e);
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
			if (paused)
				break;
		}
		System.out.println("Draining line...");
		line.drain(); //wait till sound has finished playing
		line.close();
	}
	
	private void waitForMilliseconds(long millis) {
		System.out.println("Waiting for " + (millis/(float)1000/60) + " minutes.");
		long waitStart = Calendar.getInstance().getTime().getTime();
		long waitNow = Calendar.getInstance().getTime().getTime();
		
		while (waitNow - waitStart < millis) {
			long millisLeft = millis - (waitNow - waitStart);
			SpotMachine.getMainFrame().setCountDownFieldValue(millisLeft);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (paused) {
				long pauseStart;
				while (paused) {
					pauseStart = Calendar.getInstance().getTime().getTime();
					try {
						Thread.sleep(200);
					} catch (Exception e) {
						e.printStackTrace();
					}
					long pauseNow = Calendar.getInstance().getTime().getTime();
					waitStart = waitStart + (pauseNow - pauseStart);
				}
			}
			waitNow = Calendar.getInstance().getTime().getTime();
		}
	}
		
	public boolean isPaused() {
		return paused;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
		System.out.println("Pause set to " + paused);
	}

	public void setMillisBetweenSpots(long millis) {
		this.millisBetweenSpots = millis;
		if (type != TYPE_TEMPORARY)
			Prefs.prefs.putLong(Prefs.MILLIS_BETWEEN_SPOTS, millis);
	}
	
	public int getNextSpotToPlayIndex() {
		if (spotList.size() < 1)
			return -1;
		else
			return nextSpotToPlay;
	}
	
	public SpotEntry getNextSpotToPlay() {
		return getSpotAt(nextSpotToPlay);
	}
	
	private void setNextSpotToPlayAndUpdateGUI(int index) {
		setNextSpotToPlay(index);
		if (type != TYPE_TEMPORARY) {
			SpotMachine.getMainFrame().getActiveSpotList().setNextSpot(index);
			SpotMachine.getMainFrame().setNextSpotLabel(index, getNextSpotToPlay().getName());
		}
	}
	
	public int setNextSpotToPlay(int index) {
		nextSpotToPlay = index;
		if (type != TYPE_TEMPORARY)
			Prefs.prefs.putInt(Prefs.NEXT_SPOT_TO_PLAY, index);
		return nextSpotToPlay;
	}
	
	public int setNextSpotToPlayOneForward() {
		int nextSpot = nextSpotToPlay + 1;
		if (nextSpot >= spotList.size())
			nextSpot = 0;
		return setNextSpotToPlay(nextSpot);
	}
	
	public int setNextSpotToPlayOneBackward() {
		int nextSpot = nextSpotToPlay - 1;
		if (nextSpot < 0)
			nextSpot = spotList.size() - 1;
		return setNextSpotToPlay(nextSpot);
	}
	
	public void setRepeatAll(boolean state) {
		this.repeatAll = state;
		if (type != TYPE_TEMPORARY)
			Prefs.prefs.putBoolean(Prefs.REPEAT_ALL, state);
	}
	
	public boolean inPlayLoop() {
		return inPlayLoop;
	}
}
