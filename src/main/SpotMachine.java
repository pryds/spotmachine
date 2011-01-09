package main;

import gui.MainFrame;

import javax.swing.JFrame;

public class SpotMachine {
	private static SpotPlayer spotPlayer = null;
	private static SpotContainer spotsAvailable = null;
	private static MainFrame frame;
	
	public static final String PROGRAM_NAME = "SpotMachine";
	public static final String PROGRAM_VERSION = "0.1.1";
	
	public static int currentVerbosityLevel = Util.VERBOSITY_DEBUG_INFO; // level of debug info
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		
		(new Thread(spotPlayer = new SpotPlayer(SpotPlayer.TYPE_ACTIVE))).start();
		//for (int i = 0; i<10; i++)
		//	spotPlayer.addToEnd(new SpotEntry(new File(Util.get().getDataStoreDir(), "yes.wav"), "Yes!"));
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
