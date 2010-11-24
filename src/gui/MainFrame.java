package gui;

import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.Prefs;
import main.SpotMachine;
import main.SpotPlayer;

public class MainFrame extends JFrame implements ChangeListener {
	public MainFrame(String title) {
		super(title);
		//setResizable(false);
		
		setContentPane(new JPanel());
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		
		getContentPane().add(createCoundownPanel());
		getContentPane().add(createUpperMainPanel());
		getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL));
		getContentPane().add(createLowerMainPanel());
	}
	
	JTextField countdownTextField;
	
	private JPanel createCoundownPanel() {
		JPanel panel = new JPanel();
		panel.add(new JLabel("Tid til næste spot:"));
		countdownTextField = new JTextField(
				millisToMinsSecsString(Prefs.prefs.getLong(Prefs.MILLIS_BETWEEN_SPOTS, Prefs.MILLIS_BETWEEN_SPOTS_DEFAULT))
				);
		countdownTextField.setEditable(false);
		panel.add(countdownTextField);
		panel.add(new JLabel("(spot 2, \"Tekstil\")"));
		return panel;
	}
	
	public void setCountDownFieldValue(long millis) {
		countdownTextField.setText(millisToMinsSecsString(millis));
	}
	
	private String millisToMinsSecsString(long milliSeconds) {
		long totalMinutes = milliSeconds / 1000;
		int hours = (int)(totalMinutes / 60);
		int minutes = (int)(totalMinutes % 60);
		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
	}
	
	private JPanel createUpperMainPanel() {
		JPanel panel = new JPanel();
		panel.add(new JButton("Afspil"));
		panel.add(new JButton("Pause"));
		panel.add(new JButton("Forrige"));
		panel.add(new JButton("Næste"));
		return panel;
	}
	
	private JPanel createLowerMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(createAvailableSpotsPanel());
		panel.add(createSpotTransferPanel());
		panel.add(createActiveSpotsPanel());
		panel.add(createChangeOrderPanel());
		return panel;
	}
	
	private JPanel createAvailableSpotsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JLabel("Tilgængelige spots:"));
		/**
		Vector<String> s = new Vector<String>();
		s.add("Kolonial");
		s.add("Slagter");
		s.add("Frost");
		s.add("Frugt og grønt");
		s.add("Fisk");
		s.add("Teknik");
		s.add("Køkken/bord");
		s.add("Sæson");
		s.add("Tekstil");
		s.add("Åbent på søndag");
		**/
		panel.add(new SpotList(SpotMachine.getAvailableSpots().getNames()).getContainingScrollPane());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 0));
		buttonPanel.add(new JButton("Optag nyt"));
		buttonPanel.add(new JButton("Slet"));
		buttonPanel.add(new JButton("Afspil"));
		buttonPanel.add(new JButton("Omdøb"));
		panel.add(buttonPanel);
		
		return panel;
	}
	
	private JPanel createSpotTransferPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JButton("->"));
		panel.add(new JButton("<-"));
		return panel;
	}

	private JSpinner minBetweenSpotsSpinner;
	
	private JPanel createActiveSpotsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JLabel("Aktive spots:"));
		
		while(SpotMachine.getSpotPlayer() == null) {
			System.out.println("SpotPlayer not initialized yet. Waiting a bit and then retrying.");
			try {
				Thread.sleep(500);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		SpotPlayer spotPlayer = SpotMachine.getSpotPlayer();

		panel.add(new SpotList(spotPlayer.getNames()).getContainingScrollPane());
		panel.add(new JCheckBox("Gentag alle"));
		
		JPanel spinnerPanel = new JPanel();
		//spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.LINE_AXIS));
		minBetweenSpotsSpinner = new JSpinner(new SpinnerNumberModel(
				(double)Prefs.prefs.getLong(Prefs.MILLIS_BETWEEN_SPOTS, Prefs.MILLIS_BETWEEN_SPOTS_DEFAULT) / 1000 / 60,
				0, 1000, 1));
		minBetweenSpotsSpinner.addChangeListener(this);
		spinnerPanel.add(minBetweenSpotsSpinner);
		spinnerPanel.add(new JLabel("min mellem spots"));
		panel.add(spinnerPanel);

		return panel;
	}

	private JPanel createChangeOrderPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JButton("Flyt op"));
		panel.add(new JButton("Flyt ned"));
		return panel;
	}

	
	public void stateChanged(ChangeEvent e) {
		System.out.println("Value changed!");
		JComponent source = (JComponent)e.getSource();
		if (source == minBetweenSpotsSpinner) {
			double readValue = (Double)((JSpinner)source).getValue();
			SpotMachine.getSpotPlayer().setMillisBetweenSpots((int)Math.round(readValue * 60 * 1000));
		}
	}
	
}
