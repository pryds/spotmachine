package main;

import java.util.Vector;

public class ScheduledSpotPlayer extends SpotPlayer implements Runnable {
	public ScheduledSpotPlayer(int type) {
		super(type);
	}
	
	public void run() {
		while(true) {
			SpotEntry[] spotsToPlay = getSpotsReadyForPlay();
			IntervalledSpotPlayer sequentialPlayer = SpotMachine.getIntervalledSpotPlayer();
			boolean sequentialSpotsAreInPlayLoop = !sequentialPlayer.isPaused();
			if (spotsToPlay.length > 0 && sequentialSpotsAreInPlayLoop) {
				sequentialPlayer.setPaused(true);
			}
			for (int i = 0; i < spotsToPlay.length; i++) {
			    if (SpotMachine.getMainFrame().getRecordDialogue() == null) { //only play if we're not currently recording
			        spotsToPlay[i].getSchedule().setLastPlayIncidentNow();
			        play(spotsToPlay[i].getFile());
			    } else {
			        Util.get().out("ScheduledPlayer warning: Did NOT play spot " + spotsToPlay[i].getName() + " since the recording window is open.", Util.VERBOSITY_WARNING);
			    }
				Util.get().threadSleep(1000);
			}
			if (spotsToPlay.length > 0 && sequentialSpotsAreInPlayLoop) {
				sequentialPlayer.setPaused(false);
			}
			Util.get().threadSleep(10000);
		}
	}
	
	private SpotEntry[] getSpotsReadyForPlay() {
		Vector<SpotEntry> spots = new Vector<SpotEntry>();
		for (int i = 0; i < spotList.size(); i++) {
			if (spotList.get(i).hasSchedule() &&
					spotList.get(i).getSchedule().shouldPlayWithinCurrentMinute() &&
					!spotList.get(i).getSchedule().hasPlayedWithinPastMinute()) {
				spots.add(spotList.get(i));
			}
		}
		SpotEntry[] out = new SpotEntry[spots.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = spots.get(i);
		Util.get().out("Scheduled player: Found " + out.length + " spots due for play now.", Util.VERBOSITY_DEBUG_INFO);
		return out;
	}
	
	public void addToEnd(SpotEntry spot) {
	    addToEnd(spot, spot.getSchedule());
	}
	
	public SpotEntry addToEnd(SpotEntry spot, PlaySchedule schedule) {
	    SpotEntry dup = spot.duplicate();
	    dup.setSchedule(schedule);
	    super.addToEnd(dup);
	    return dup;
	}
}
