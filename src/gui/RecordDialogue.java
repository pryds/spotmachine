package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.SpotContainer;
import main.SpotEntry;
import main.SpotPlayer;
import main.Util;
import main.SpotMachine;
import main.SpotRecorder;

public class RecordDialogue extends JFrame implements ActionListener {
	private static final long serialVersionUID = 2437740287713575921L;
	
	private File lastFinishedRecording = null;
	
	// Icons from http://java.sun.com/developer/techDocs/hi/repository/
	
	
	private JTextField spotNameTextField;
	
	public RecordDialogue() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle("Optag nyt spot");
		
		JPanel panel = new JPanel();
		setContentPane(panel);  
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		panel.add(new JLabel("Navn:"));
		
		spotNameTextField = new JTextField();
		panel.add(spotNameTextField);
		
		panel.add(createCurrentLengthPanel());
		panel.add(createStatusTextFieldPanel());
		panel.add(createControlButtonsPanel());
		
		panel.add(createNoteTextArea());
		
		panel.add(createOKCancelButtonPanel());
		
		pack(); 
	}
	
	private JTextField currentLengthTextField;
	private JLabel statusTextField;
	
	private JPanel createCurrentLengthPanel() {
		JPanel panel = new JPanel();
		panel.add(new JLabel("Aktuel længde: "));
		
		currentLengthTextField = new JTextField("0:00");
		currentLengthTextField.setEditable(false);
		panel.add(currentLengthTextField);
		
		return panel;
	}
	
	private JPanel createStatusTextFieldPanel() {
		JPanel panel = new JPanel();
		
		statusTextField = new JLabel("STOPPET");
		panel.add(statusTextField);
		
		return panel;
	}
	
	public void setCurrentDurationTextField(long millis) {
		currentLengthTextField.setText(Util.get().millisToMinsSecsString(millis));
	}
	
	private JButton recordButton;
	private JButton pauseButton;
	private JButton stopButton;
	
	private JPanel createControlButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Optagelse"));
		
		recordButton = new JButton("Optag forfra", Util.get().createImageIcon("../resources/Record24.gif"));
		recordButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    recordButton.setHorizontalTextPosition(AbstractButton.CENTER);
		recordButton.addActionListener(this);
		recordButton.setActionCommand("record");
		panel.add(recordButton);
		
		pauseButton = new JButton("Pause", Util.get().createImageIcon("../resources/Pause24.gif"));
		pauseButton.setEnabled(false);
		pauseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    pauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand("pause");
		//panel.add(pauseButton);
		
		stopButton = new JButton("Stop", Util.get().createImageIcon("../resources/Stop24.gif"));
		stopButton.setEnabled(false);
		stopButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    stopButton.setHorizontalTextPosition(AbstractButton.CENTER);
		stopButton.addActionListener(this);
		stopButton.setActionCommand("stop");
		panel.add(stopButton);
		
		return panel;
	}
	
	private JTextArea createNoteTextArea() {
		JTextArea note = new JTextArea("Husk at afbryde lydudgang, tilsluttet forstærker\n"
				+ "eller lignende inden optagelse begyndes, da det\n"
				+ "optagede afspilles umiddelbart efter optagelse.");
		note.setEditable(false);
		
		Color textColour = new Color(255, 0, 0); // red text
		Color bgColour = new Color(255, 255, 255, 0); // "invisible" white, i.e. alpha value of 0
		note.setBackground(bgColour); 
		note.setForeground(textColour);
		note.setSelectionColor(bgColour);
		note.setSelectedTextColor(textColour);
		return note;
	}
	
	private JButton okButton;
	private JButton cancelButton;
	
	private JPanel createOKCancelButtonPanel() {
		JPanel panel = new JPanel();
		
		okButton = new JButton("OK");
		okButton.setToolTipText("Luk optagelsesvinduet og gem det optagede spot");
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		panel.add(okButton);
		
		cancelButton = new JButton("Fortryd");
		cancelButton.setToolTipText("Luk optagelsesvinduet uden at gemme");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("cancel");
		panel.add(cancelButton);
		
		return panel;
	}
	
	SpotRecorder rec = null;

	public void actionPerformed(ActionEvent e) {
		System.out.println("Record window action performed! " + e.getActionCommand());
		String action = e.getActionCommand();
		if (action.equals("ok")) {
			String spotName = spotNameTextField.getText().trim();
			if (lastFinishedRecording == null) {
				System.err.println("Nothing recorded yet. Ignoring ok request.");
				JOptionPane.showMessageDialog(SpotMachine.getMainFrame().getRecordDialogue(),
					    "Der er endnu ikke optaget et spot.",
					    "Ingen optagelse",
					    JOptionPane.WARNING_MESSAGE);
			} else if (spotName.length() == 0) {
				System.err.println("No spot name given. Ignoring ok request.");
				JOptionPane.showMessageDialog(SpotMachine.getMainFrame().getRecordDialogue(),
					    "Skriv et navn for det optagede spot.",
					    "Intet navn for spot",
					    JOptionPane.WARNING_MESSAGE);
			} else {
				SpotEntry newSpot = new SpotEntry(lastFinishedRecording, spotName);
				SpotMachine.getAvailableSpots().addToEnd(newSpot);
				SpotMachine.getMainFrame().getAvailableSpotList().getModel().addToEnd(newSpot);
				SpotMachine.getMainFrame().setEnabled(true);
				SpotMachine.getMainFrame().setRecordDialogueNull();
				this.dispose();
			}
		} else if (action.equals("cancel")) {
			if (lastFinishedRecording != null) {
				Util.get().deleteFile(lastFinishedRecording);
				lastFinishedRecording = null;
			}
			SpotMachine.getMainFrame().setEnabled(true);
			SpotMachine.getMainFrame().setRecordDialogueNull();
			this.dispose();
		} else if (action.equals("record")) {
			if (lastFinishedRecording != null) {
				Util.get().deleteFile(lastFinishedRecording);
				lastFinishedRecording = null;
			}
			recordButton.setEnabled(false);
			stopButton.setEnabled(true);
			pauseButton.setEnabled(true);
			okButton.setEnabled(false);
			cancelButton.setEnabled(false);
			rec = new SpotRecorder();
			new Thread(rec).start();
			statusTextField.setText("OPTAGER");
		} else if (action.equals("pause")) {
			// TODO: Detect if already paused and unpause instead if so
			recordButton.setEnabled(true);
			stopButton.setEnabled(true);
			pauseButton.setEnabled(true);
			statusTextField.setText("pause");
		} else if (action.equals("stop")) {
			stopButton.setEnabled(false);

			statusTextField.setText("STOPPER OPTAGELSE...");
			if (rec != null) {
				rec.stopRecoding();
				lastFinishedRecording = rec.getOutFile();
				rec = null;
			}

			statusTextField.setText("AAFSPILLER");

			final SpotPlayer tempPlayer = new SpotPlayer(SpotContainer.TYPE_TEMPORARY); // final in order to be accessed from inner class below
			tempPlayer.addToEnd(new SpotEntry(lastFinishedRecording, "Temporary"));
			new Thread(tempPlayer).start();
			
			/**
			 * The following thread will wait for the playback to finish,
			 * and then reenable disabled buttons.
			 * we can't just wait in the current thread, as it would halt
			 * the GUI, making the impression that the program has stalled
			 */
			new Thread(new Runnable() {
				public void run() {
					while (tempPlayer.inPlayLoop()) {
						try {
							Thread.sleep(200);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
					statusTextField.setText("STOPPET");
					
					recordButton.setEnabled(true);
					pauseButton.setEnabled(false);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
				}
			}).start();
			
		}
	}
}
