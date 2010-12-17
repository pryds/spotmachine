package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import main.SpotEntry;
import main.SpotMachine;
import main.SpotPlayer;

public class MainFrame extends JFrame implements ChangeListener, ActionListener, ItemListener {
	private static final long serialVersionUID = 6219825567861104713L;
	
	// Icons from http://java.sun.com/developer/techDocs/hi/repository/
	
	// TODO: Add tool tips at various places (also on recording window)

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
		
		playButton = new JButton("Afspil", Calculate.createImageIcon("../resources/Play24.gif"));
		playButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    playButton.setHorizontalTextPosition(AbstractButton.CENTER);
		playButton.addActionListener(this);
		playButton.setActionCommand("play");
		panel.add(playButton);
		
		pauseButton = new JButton("Pause", Calculate.createImageIcon("../resources/Pause24.gif"));
		pauseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    pauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand("pause");
		pauseButton.setEnabled(false);
		panel.add(pauseButton);
		
		JButton previousButton = new JButton("Forrige", Calculate.createImageIcon("../resources/StepBack24.gif"));
		previousButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    previousButton.setHorizontalTextPosition(AbstractButton.CENTER);
	    previousButton.addActionListener(this);
		previousButton.setActionCommand("previous");
		panel.add(previousButton);
		
		JButton nextButton = new JButton("Næste", Calculate.createImageIcon("../resources/StepForward24.gif"));
		nextButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    nextButton.setHorizontalTextPosition(AbstractButton.CENTER);
	    nextButton.addActionListener(this);
		nextButton.setActionCommand("next");
		panel.add(nextButton);
		
		return panel;
	}
	
	public void setGUIPaused(boolean paused) {
		playButton.setEnabled(paused);
		pauseButton.setEnabled(!paused);
		recordNewButton.setEnabled(paused);
	
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
	private JButton recordNewButton;
	
	private JPanel createAvailableSpotsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JLabel("Tilgængelige spots:"));
		
		SpotContainer availableSpots = SpotMachine.getAvailableSpots();
		availableSpotList = new SpotList(new SpotListModel(SpotContainer.TYPE_AVAILABLE));
		availableSpotList.getModel().replaceData(availableSpots.getDataCopy());
		panel.add(availableSpotList.getContainingScrollPane());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 0));
		
		recordNewButton = new JButton("Optag nyt");
		recordNewButton.addActionListener(this);
		recordNewButton.setActionCommand("record");
		buttonPanel.add(recordNewButton);
		
		JButton removeFromAvailableButton = new JButton("Slet");
		removeFromAvailableButton.addActionListener(this);
		removeFromAvailableButton.setActionCommand("removefromavailable");
		buttonPanel.add(removeFromAvailableButton);
		
		JButton importButton = new JButton("Importér");
		importButton.setEnabled(false);
		importButton.addActionListener(this);
		importButton.setActionCommand("importspot");
		buttonPanel.add(importButton);
		
		JButton renameButton = new JButton("Omdøb");
		renameButton.addActionListener(this);
		renameButton.setActionCommand("rename");
		buttonPanel.add(renameButton);
		
		panel.add(buttonPanel);
		
		return panel;
	}
	
	private JPanel createSpotTransferPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JButton copyToActiveButton = new JButton(Calculate.createImageIcon("../resources/Forward24.gif"));
		copyToActiveButton.setToolTipText("Indsæt det valgte tilgængelige spot i listen over aktive spots");
		copyToActiveButton.addActionListener(this);
		copyToActiveButton.setActionCommand("copytoactive");
		panel.add(copyToActiveButton);
	    
		JButton removeFromActiveButton = new JButton(Calculate.createImageIcon("../resources/Back24.gif"));
		removeFromActiveButton.setToolTipText("Fjern spot fra listen over aktive spots");
		removeFromActiveButton.addActionListener(this);
		removeFromActiveButton.setActionCommand("removefromactive");
		panel.add(removeFromActiveButton);
		
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
		activeSpotList = new SpotList(new SpotListModel(SpotContainer.TYPE_ACTIVE));
		activeSpotList.getModel().replaceData(activeSpots.getDataCopy());
		activeSpotList.setNextSpot(activeSpots.getNextSpotToPlayIndex());
		setNextSpotLabel(activeSpots.getNextSpotToPlayIndex(), activeSpots.getNextSpotToPlay().getName());
		panel.add(activeSpotList.getContainingScrollPane());
		repeatAllCheckBox = new JCheckBox("Gentag alle");
		repeatAllCheckBox.setToolTipText("Start afspilningen af spots forfra, når sidste spot er afspillet");
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
		
		JButton upButton = new JButton("Flyt op", Calculate.createImageIcon("../resources/Up24.gif"));
		upButton.addActionListener(this);
		upButton.setActionCommand("moveup");
		panel.add(upButton);
		
		JButton downButton = new JButton("Flyt ned", Calculate.createImageIcon("../resources/Down24.gif"));
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
			int oldPos = activeSpotList.getSelectedRow();
			if (oldPos != -1) {
				int newPos;
				if (action.equals("moveup")) {
					newPos = oldPos - 1;
				} else {
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
			}
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
		} else if (action.equals("copytoactive")) {
			int selectedAvailable = availableSpotList.getSelectedRow();
			if (selectedAvailable != -1) {
				int selectedActive = activeSpotList.getSelectedRow();
				SpotEntry source = SpotMachine.getAvailableSpots().getSpotAt(selectedAvailable);
				SpotMachine.getSpotPlayer().addToEnd(source);
				activeSpotList.getModel().addToEnd(source);
				activeSpotList.getSelectionModel().setSelectionInterval(selectedActive, selectedActive);
			}
		} else if (action.equals("removefromactive")) {
			int selectedActive = activeSpotList.getSelectedRow();
			if (selectedActive != -1) {
				SpotMachine.getSpotPlayer().remove(selectedActive);
				activeSpotList.getModel().remove(selectedActive);
				int newSelection = (selectedActive-1 >= 0) ? selectedActive-1 : 0;
				activeSpotList.getSelectionModel().setSelectionInterval(newSelection, newSelection);
			}
		} else if (action.equals("removefromavailable")) {
			int selectedAvailable = availableSpotList.getSelectedRow();
			if (selectedAvailable != -1) {
				Object[] options = {"Ja, slet spot!", "Nej"};
				int selection = JOptionPane.showOptionDialog(
						this,
						"Er du sikker på, at du vil slette spot permanent?\n"
						+ "Du kan ikke fortryde denne handling.",
						"Slet spot", // headline
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null, // icon
						options,
						options[1]
				);
				
				if (selection == 0) {
					SpotMachine.getAvailableSpots().remove(selectedAvailable);
					availableSpotList.getModel().remove(selectedAvailable);
					int newSelection = (selectedAvailable-1 >= 0) ? selectedAvailable-1 : 0;
					availableSpotList.getSelectionModel().setSelectionInterval(newSelection, newSelection);
				}
			}
		} else if (action.equals("record")) {
			// TODO
			new RecordDialogue().setVisible(true);
			this.setEnabled(false);
		} else if (action.equals("importspot")) {
			// TODO
		} else if (action.equals("rename")) {
			int selectedAvailable = availableSpotList.getSelectedRow();
			if (selectedAvailable != -1) {
				SpotEntry spot = SpotMachine.getAvailableSpots().getSpotAt(selectedAvailable);
				
				String newName = null;
				do {
					newName = (String)JOptionPane.showInputDialog(
							this,
							"Skriv nyt navn for spot:",
							"Omdøb spot",
							JOptionPane.PLAIN_MESSAGE,
							null, // icon
							null, // possibilities, null gives text field
							spot.getName()
					);
					if (newName != null)
						newName = newName.trim();
				} while (newName != null && newName.length() == 0);
				
				if (newName != null) {
					SpotMachine.getAvailableSpots().renameSpot(selectedAvailable, newName);
					availableSpotList.getModel().rename(selectedAvailable, newName);
				}
			}
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
