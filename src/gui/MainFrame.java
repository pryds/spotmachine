package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

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

import main.Calculate;
import main.Prefs;
import main.SpotContainer;
import main.SpotMachine;
import main.SpotPlayer;

public class MainFrame extends JFrame implements ChangeListener, ActionListener, ItemListener {
	private static final long serialVersionUID = 6219825567861104713L;

	public MainFrame(String title) {
		super(title);
		//setResizable(false);
		
		setContentPane(new JPanel());
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		
		getContentPane().add(createCountdownPanel());
		getContentPane().add(createUpperMainPanel());
		getContentPane().add(new JSeparator(SwingConstants.HORIZONTAL));
		getContentPane().add(createLowerMainPanel());
	}
	
	private JTextField countdownTextField;
	private JLabel nextSpotLabel;
	
	private JPanel createCountdownPanel() {
		JPanel panel = new JPanel();
		panel.add(new JLabel("Tid til næste spot:"));
		countdownTextField = new JTextField(
				Calculate.millisToMinsSecsString(Prefs.prefs.getLong(Prefs.MILLIS_BETWEEN_SPOTS, Prefs.MILLIS_BETWEEN_SPOTS_DEFAULT))
				);
		countdownTextField.setEditable(false);
		panel.add(countdownTextField);
		nextSpotLabel = new JLabel();
		panel.add(nextSpotLabel);
		return panel;
	}
	
	public void setNextSpotLabel(int index, String name) {
		nextSpotLabel.setText("(nr. " + (index+1) + ", \"" + name + "\")");
	}
	
	public void setCountDownFieldValue(long millis) {
		countdownTextField.setText(Calculate.millisToMinsSecsString(millis));
	}
	
	private JButton playButton;
	private JButton pauseButton;
	
	private JPanel createUpperMainPanel() {
		JPanel panel = new JPanel();
		
		playButton = new JButton("Afspil");
		playButton.addActionListener(this);
		playButton.setActionCommand("play");
		panel.add(playButton);
		
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand("pause");
		pauseButton.setEnabled(false);
		panel.add(pauseButton);
		
		JButton previousButton = new JButton("Forrige");
		previousButton.addActionListener(this);
		previousButton.setActionCommand("previous");
		panel.add(previousButton);
		
		JButton nextButton = new JButton("Næste");
		nextButton.addActionListener(this);
		nextButton.setActionCommand("next");
		panel.add(nextButton);
		
		return panel;
	}
	
	public void setGUIPaused(boolean paused) {
		playButton.setEnabled(paused);
		pauseButton.setEnabled(!paused);
	
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
	
	private SpotList availableSpotList;
	
	private JPanel createAvailableSpotsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JLabel("Tilgængelige spots:"));
		
		SpotContainer availableSpots = SpotMachine.getAvailableSpots();
		availableSpotList = new SpotList(availableSpots.getData(), availableSpots.getColumnNames(), SpotContainer.TYPE_AVAILABLE) {
			private static final long serialVersionUID = 6882050480728309992L;
			public boolean isCellEditable(int row, int column){
				return false;
			}
		};
		panel.add(availableSpotList.getContainingScrollPane());
		
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
	
	private SpotList activeSpotList;
	private JCheckBox repeatAllCheckBox;
	
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
		
		SpotPlayer activeSpots = SpotMachine.getSpotPlayer();
		activeSpotList = new SpotList(activeSpots.getData(), activeSpots.getColumnNames(), SpotContainer.TYPE_ACTIVE) {
			private static final long serialVersionUID = 7721297301307563517L;
			public boolean isCellEditable(int row, int column){
				return false;
			}
		};
		activeSpotList.setNextSpot(activeSpots.getNextSpotToPlayIndex());
		setNextSpotLabel(activeSpots.getNextSpotToPlayIndex(), activeSpots.getNextSpotToPlay().getName());
		panel.add(activeSpotList.getContainingScrollPane());
		repeatAllCheckBox = new JCheckBox("Gentag alle");
		repeatAllCheckBox.setSelected(Prefs.prefs.getBoolean(Prefs.REPEAT_ALL, Prefs.REPEAT_ALL_DEFAULT));
		repeatAllCheckBox.addItemListener(this);
		panel.add(repeatAllCheckBox);
		
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
	
	public SpotList getAvailableSpotList() {
		return availableSpotList;
	}
	
	public SpotList getActiveSpotList() {
		return activeSpotList;
	}

	private JPanel createChangeOrderPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JButton upButton = new JButton("Flyt op");
		upButton.addActionListener(this);
		upButton.setActionCommand("moveup");
		panel.add(upButton);
		
		JButton downButton = new JButton("Flyt ned");
		downButton.addActionListener(this);
		downButton.setActionCommand("movedown");
		panel.add(downButton);
		
		return panel;
	}

	
	public void stateChanged(ChangeEvent e) {
		System.out.println("State changed!");
		JComponent source = (JComponent)e.getSource();
		if (source == minBetweenSpotsSpinner) {
			double readValue = (Double)((JSpinner)source).getValue();
			SpotMachine.getSpotPlayer().setMillisBetweenSpots((int)Math.round(readValue * 60 * 1000));
		}
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("Action performed! " + e.getActionCommand());
		String action = e.getActionCommand();
		if (action.equals("moveup") || action.equals("movedown")) {
			int oldPos;
			int newPos;
			if (action.equals("moveup")) {
				oldPos = activeSpotList.getSelectedRow();
				newPos = oldPos - 1;
			} else {
				oldPos = activeSpotList.getSelectedRow();
				newPos = oldPos + 1;
			}
			if (newPos < 0 || newPos > activeSpotList.getRowCount() - 1) {
				System.err.println("Trying to move a spot off-range. Ignored.");
				return;
			}
			activeSpotList.swapRows(newPos, oldPos);
			activeSpotList.getSelectionModel().setSelectionInterval(newPos, newPos);
			SpotMachine.getSpotPlayer().swapSpots(newPos, oldPos);
			setNextSpotLabel(SpotMachine.getSpotPlayer().getNextSpotToPlayIndex(), SpotMachine.getSpotPlayer().getNextSpotToPlay().getName());
		} else if (action.equals("play")) {
			SpotMachine.getSpotPlayer().setPaused(false);
			setGUIPaused(false);
		} else if (action.equals("pause")) {
			SpotMachine.getSpotPlayer().setPaused(true);
			setGUIPaused(true);
		} else if (action.equals("previous")) {
			int prev = SpotMachine.getSpotPlayer().setNextSpotToPlayOneBackward();
			activeSpotList.setNextSpot(prev);
			setNextSpotLabel(prev, SpotMachine.getSpotPlayer().getSpotAt(prev).getName());
		} else if (action.equals("next")) {
			int next = SpotMachine.getSpotPlayer().setNextSpotToPlayOneForward();
			activeSpotList.setNextSpot(next);
			setNextSpotLabel(next, SpotMachine.getSpotPlayer().getSpotAt(next).getName());
		}
	}

	public void itemStateChanged(ItemEvent e) {
		System.out.println("Item state changed!");
		Object source = e.getItemSelectable();
		if (source == repeatAllCheckBox) {
			SpotMachine.getSpotPlayer().setRepeatAll(repeatAllCheckBox.isSelected());
		}

	}
	
}
