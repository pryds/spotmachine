package main;
import java.io.File;

import gui.MainFrame;

import javax.swing.JFrame;



public class SpotMachine {
	private static SpotPlayer spotPlayer;
	private static MainFrame frame;
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		
		(new Thread(spotPlayer = new SpotPlayer(SpotPlayer.TYPE_ACTIVE))).start();
		//for (int i = 0; i<10; i++)
		//	spotPlayer.addToEnd(new SpotEntry(new File("yes.wav"), "Yes!"));
		spotPlayer.initializeFromPrefs();
	}
	
	private static void createAndShowGUI() {
		frame = new MainFrame("SpotMachine");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public static SpotPlayer getSpotPlayer() {
		return spotPlayer;
	}
	
	public static MainFrame getMainFframe() {
		return frame;
	}

}
