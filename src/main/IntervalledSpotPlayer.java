package main;

import java.util.Calendar;

public class IntervalledSpotPlayer extends SpotPlayer implements Runnable {
	private long millisBetweenSpots;
	private int nextSpotToPlay;
	private boolean repeatAll;
	
	private boolean inPlayLoop = true;
	
	public IntervalledSpotPlayer(int type) {
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
						Util.get().out("Exiting interval spotPlayer thread. Type " + type, Util.VERBOSITY_DEBUG_INFO);
						return;
					} else {
						setPaused(true);
						SpotMachine.getMainFrame().setGUIPaused(true);
					}
				} else {
					waitForMilliseconds(millisBetweenSpots);
				}
			} else {
				Util.get().threadSleep(1000);
			}
		}
	}
	
	private void waitForMilliseconds(long millis) {
		Util.get().out("Waiting for " + (millis/(float)1000/60) + " minutes.", Util.VERBOSITY_DEBUG_INFO);
		long waitStart = Calendar.getInstance().getTime().getTime();
		long waitNow = Calendar.getInstance().getTime().getTime();
		
		while (waitNow - waitStart < millis) {
			long millisLeft = millis - (waitNow - waitStart);
			SpotMachine.getMainFrame().setCountDownFieldValue(millisLeft);
			Util.get().threadSleep(200);
			if (paused) {
				long pauseStart;
				while (paused) {
					pauseStart = Calendar.getInstance().getTime().getTime();
					Util.get().threadSleep(200);
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
		Util.get().out("Pause set to " + paused, Util.VERBOSITY_DEBUG_INFO);
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
			SpotMachine.getMainFrame().getIntervalledSpotList().setNextSpot(index);
			SpotMachine.getMainFrame().setNextSpotLabel(index, getNextSpotToPlay());
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
	
	public SpotEntry remove(int index) {
		SpotEntry removedSpot = super.remove(index);
		if (index < nextSpotToPlay) {
			Util.get().out("index < nextSpotToPlay  =>  nextSpotToPlay--", Util.VERBOSITY_DEBUG_INFO);
			setNextSpotToPlayOneBackward();
		}
		Util.get().out("SpotPlayer: Removed spot index " + index + ". New NextSpotToPlay: " + nextSpotToPlay, Util.VERBOSITY_DEBUG_INFO);
		if (numberOfSpots() == 0) {
			setNextSpotToPlayAndUpdateGUI(0);
			setPaused(true);
			SpotMachine.getMainFrame().setGUIPaused(true);
			Util.get().out("SpotPlayer: Since removed spot was the last one, pause has been set (even if it was already set) and next spot set to " + nextSpotToPlay, Util.VERBOSITY_DEBUG_INFO);
		}
		return removedSpot;
	}

}
