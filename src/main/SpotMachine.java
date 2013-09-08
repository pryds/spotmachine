package main;

import gui.MainFrame;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.xnap.commons.i18n.I18n;

public class SpotMachine {
	private static IntervalledSpotPlayer intervalledSpotPlayer = null;
	private static ScheduledSpotPlayer scheduledSpotPlayer = null;
	private static SpotContainer spotsAvailable = null;
	private static MainFrame frame;
	
	public static final String PROGRAM_NAME = "SpotMachine";
	public static final String PROGRAM_VERSION = "0.3.2 git";
	
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
		
		(new Thread(new StatsCollector())).start();
		
		new Thread(new Runnable() {
			public void run() {
				Util.get().threadSleep(4000);
				
				//Check if user has already answered this question. Bail out, if so.
				if (Prefs.prefs.getBoolean(Prefs.COLLECT_STATISTICS, Prefs.COLLECT_STATISTICS_DEFAULT)
						!= Prefs.COLLECT_STATISTICS_DEFAULT)
					return;
					
				I18n i18n = Util.get().i18n();
				Object[] options = {i18n.tr("I accept!"),
						i18n.tr("No, thanks!")
						};
				int answer = JOptionPane.showOptionDialog(SpotMachine.getMainFrame(),
						Util.get().wordWrap(i18n.tr("You have the option to allow SpotMachine to " +
								"collect some statistical data about your computer and your usage " +
								"of SpotMachine from time to time, and send it to the developers " +
								"of SpotMachine for better development of the program. The data is " +
								"in no way personal, and it will not be possible to identify you " +
								"from this data. Also, the data will not reach third party. You can " +
								"always change your mind about this from the preferences window.\n\n" +
								"Do you allow SpotMachine to do this?"), 60),
					    i18n.tr("Collect Statistics?"),
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null, //icon
					    options,
					    options[0]
					    		);
				Prefs.prefs.putBoolean(Prefs.COLLECT_STATISTICS, answer == 0);
			}
		}).start();
		
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
