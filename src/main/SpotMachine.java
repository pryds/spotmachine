package main;

import gui.MainFrame;

import javax.swing.JFrame;

public class SpotMachine {
	private static SpotPlayer spotPlayer = null;
	private static SpotContainer spotsAvailable = null;
	private static MainFrame frame;
	
	public static final String PROGRAM_NAME = "SpotMachine";
	public static final String PROGRAM_VERSION = "0.1.2 svn";
	
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
				System.out.println("-nowarn     Suppress all command line messages, inclusive warnings.");
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
		
		(new Thread(spotPlayer = new SpotPlayer(SpotPlayer.TYPE_ACTIVE))).start();
		spotPlayer.initializeFromPrefs();
		
		spotsAvailable = new SpotContainer(SpotPlayer.TYPE_AVAILABLE);
		spotsAvailable.initializeFromPrefs();
	}
	
	private static void createAndShowGUI() {
		frame = new MainFrame(PROGRAM_NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public static SpotPlayer getSpotPlayer() {
		return spotPlayer;
	}
	
	public static SpotContainer getAvailableSpots() {
		return spotsAvailable;
	}
	
	public static MainFrame getMainFrame() {
		return frame;
	}

}
