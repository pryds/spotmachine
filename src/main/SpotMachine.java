package main;

import gui.MainFrame;

import javax.swing.JFrame;

public class SpotMachine {
	private static IntervalledSpotPlayer intervalledSpotPlayer = null;
	private static ScheduledSpotPlayer scheduledSpotPlayer = null;
	private static SpotContainer spotsAvailable = null;
	private static MainFrame frame;
	
	public static final String PROGRAM_NAME = "SpotMachine";
	public static final String PROGRAM_VERSION = "0.3.2";
	
	public static int currentVerbosityLevel; // level of debug info
	
	public static void main(String[] args) {
		currentVerbosityLevel = Util.VERBOSITY_WARNING;
		if (args.length > 0) {
			if (args[0].equals("-debug")) {
				currentVerbosityLevel = Util.VERBOSITY_DEBUG_INFO;
			} else if (args[0].equals("-ddebug")) {
				currentVerbosityLevel = Util.VERBOSITY_DETAILED_DEBUG_INFO;
			} else if (args[0].equals("-nowarn")) {
				currentVerbosityLevel = Util.VERBOSITY_ERROR;
			} else if (args[0].equals("-version")) {
				System.out.println(PROGRAM_NAME + " version " + PROGRAM_VERSION);
				System.exit(0);
			} else {
				System.out.println("Command line arguments:");
				System.out.println("-version    Print version info and exit.");
				System.out.println("-help       Show this list and exit.");
				System.out.println("-nowarn     Suppress all command line messages, including warnings.");
				System.out.println("-debug      In addition to errors and warnings, show debug info.");
				System.out.println("-ddebug     In addition to errors and warnings, show detailled debug info.");
				System.exit(0);
			}
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		
		(new Thread(intervalledSpotPlayer = new IntervalledSpotPlayer(SpotPlayer.TYPE_INTERVALLED))).start();
		intervalledSpotPlayer.initializeFromPrefs();
		
		(new Thread(scheduledSpotPlayer = new ScheduledSpotPlayer(SpotPlayer.TYPE_SCHEDULED))).start();
		scheduledSpotPlayer.initializeFromPrefs();
		
		
		/*
		PlaySchedule sch = new PlaySchedule();
		sch.setTimeToPlay(14, 33);
		scheduledSpotPlayer.addToEnd(new SpotEntry(new File("/home/thomas/.spotmachine/cheer.wav"), "test test", sch));
		
		sch = new PlaySchedule();
		sch.setWeekdaysToPlay(new boolean[] {true, false, false, false, false, false, true});
		sch.setTimeToPlay(14, 34);
		scheduledSpotPlayer.addToEnd(new SpotEntry(new File("/home/thomas/.spotmachine/cheer.wav"), "test test 2", sch));
		*/
		
		
		spotsAvailable = new SpotContainer(SpotPlayer.TYPE_AVAILABLE);
		spotsAvailable.initializeFromPrefs();
	}
	
	private static void createAndShowGUI() {
		frame = new MainFrame(PROGRAM_NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public static IntervalledSpotPlayer getIntervalledSpotPlayer() {
		return intervalledSpotPlayer;
	}
	
	public static ScheduledSpotPlayer getScheduledSpotPlayer() {
	    return scheduledSpotPlayer;
	}
	
	public static SpotContainer getAvailableSpots() {
		return spotsAvailable;
	}
	
	public static MainFrame getMainFrame() {
		return frame;
	}

}
