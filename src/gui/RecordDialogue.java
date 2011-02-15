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

import main.AudioSamples;
import main.Prefs;
import main.SpotContainer;
import main.SpotEntry;
import main.IntervalledSpotPlayer;
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
		setTitle(Util.get().string("record-headline"));
		
		JPanel panel = new JPanel();
		setContentPane(panel);  
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		panel.add(new JLabel(Util.get().string("record-name-label")));
		
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
		panel.add(new JLabel(Util.get().string("record-currentlength-label") + " "));
		
		currentLengthTextField = new JTextField("0:00");
		currentLengthTextField.setEditable(false);
		panel.add(currentLengthTextField);
		
		return panel;
	}
	
	private JPanel createStatusTextFieldPanel() {
		JPanel panel = new JPanel();
		
		statusTextField = new JLabel(Util.get().string("record-status-stopped-label"));
		panel.add(statusTextField);
		
		return panel;
	}
	
	public void setStatusTextField(String text) {
		statusTextField.setText(text.toUpperCase(Util.get().getCurrentLocale()));
		Util.get().threadSleep(10);
	}
	
	public void setCurrentDurationTextField(long millis) {
		currentLengthTextField.setText(Util.get().millisToMinsSecsString(millis));
	}
	
	private JButton recordButton;
	private JButton pauseButton;
	private JButton stopButton;
	
	private JPanel createControlButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(Util.get().string("record-recording-bordertitle")));
		
		recordButton = new JButton(Util.get().string("record-rerecord-button"), Util.get().createImageIcon("../resources/Record24.gif"));
		recordButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    recordButton.setHorizontalTextPosition(AbstractButton.CENTER);
		recordButton.addActionListener(this);
		recordButton.setActionCommand("record");
		panel.add(recordButton);
		
		pauseButton = new JButton(Util.get().string("record-pause-button"), Util.get().createImageIcon("../resources/Pause24.gif"));
		pauseButton.setEnabled(false);
		pauseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    pauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand("pause");
		//panel.add(pauseButton);
		
		stopButton = new JButton(Util.get().string("record-stop-button"), Util.get().createImageIcon("../resources/Stop24.gif"));
		stopButton.setEnabled(false);
		stopButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    stopButton.setHorizontalTextPosition(AbstractButton.CENTER);
		stopButton.addActionListener(this);
		stopButton.setActionCommand("stop");
		panel.add(stopButton);
		
		return panel;
	}
	
	private JTextArea createNoteTextArea() {
		JTextArea note = new JTextArea(Util.get().wordWrap(Util.get().string("record-disconnect-text"), 50));
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
		
		okButton = new JButton(Util.get().string("record-ok-button"));
		okButton.setToolTipText(Util.get().string("record-ok-tooltip"));
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		panel.add(okButton);
		
		cancelButton = new JButton(Util.get().string("record-cancel-button"));
		cancelButton.setToolTipText(Util.get().string("record-cancel-tooltip"));
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("cancel");
		panel.add(cancelButton);
		
		return panel;
	}
	
	private SpotRecorder rec = null;

	public void actionPerformed(ActionEvent e) {
		Util.get().out("Record window action performed! " + e.getActionCommand(), Util.VERBOSITY_DEBUG_INFO);
		String action = e.getActionCommand();
		if (action.equals("ok")) {
			String spotName = spotNameTextField.getText().trim();
			if (lastFinishedRecording == null) {
				Util.get().out("Nothing recorded yet. Ignoring ok request.", Util.VERBOSITY_WARNING);
				JOptionPane.showMessageDialog(SpotMachine.getMainFrame().getRecordDialogue(),
						Util.get().string("record-norecording-text"),
					    Util.get().string("record-norecording-headline"),
					    JOptionPane.WARNING_MESSAGE);
			} else if (spotName.length() == 0) {
				Util.get().out("No spot name given. Ignoring ok request.", Util.VERBOSITY_WARNING);
				JOptionPane.showMessageDialog(SpotMachine.getMainFrame().getRecordDialogue(),
						Util.get().string("record-noname-text"),
					    Util.get().string("record-noname-headline"),
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
			setStatusTextField(Util.get().string("record-status-recording-label"));
		} else if (action.equals("pause")) {
			// TODO: Detect if already paused and unpause instead if so
			recordButton.setEnabled(true);
			stopButton.setEnabled(true);
			pauseButton.setEnabled(true);
			setStatusTextField(Util.get().string("record-status-paused-label"));
		} else if (action.equals("stop")) {
			stopButton.setEnabled(false);

			setStatusTextField(Util.get().string("record-status-stopping-label"));
			if (rec != null) {
				rec.stopRecoding();
				lastFinishedRecording = rec.getOutFile();
				rec = null;
			}
			
			boolean doDCOffset = Prefs.prefs.getBoolean(Prefs.RECORDING_DO_DC_OFFSET_REMOVAL, Prefs.RECORDING_DO_DC_OFFSET_REMOVAL_DEFAULT);
			boolean doFade = Prefs.prefs.getBoolean(Prefs.RECORDING_DO_FADEIN_FADEOUT, Prefs.RECORDING_DO_FADEIN_FADEOUT_DEFAULT);
			boolean doVolume = Prefs.prefs.getBoolean(Prefs.RECORDING_DO_VOLUME_NORMALIZATION, Prefs.RECORDING_DO_VOLUME_NORMALIZATION_DEFAULT);
			
			if (doDCOffset || doFade || doVolume) {
				setStatusTextField(Util.get().string("record-status-normalizing-label"));
				AudioSamples spotSamples = new AudioSamples(lastFinishedRecording);
				if (spotSamples.initializedOK()) {
					if (doDCOffset)
						spotSamples.removeDCOffset();
					if (doFade)
						spotSamples.makeFadeInFadeOut();
					if (doVolume)
						spotSamples.normalizeVolume();
					File saveFile = Util.get().createUniqueLowerCaseRandomWAVFileInDataDir();
					if (spotSamples.writeToFile(saveFile)) {
						Util.get().deleteFile(lastFinishedRecording);
						lastFinishedRecording = saveFile;
					} else {
						Util.get().out("Error on saving normalized spot. Keeping original spot.", Util.VERBOSITY_ERROR);
					}
				} else {
					Util.get().out("Error on initializing normalization process. Ignoring all normalization.", Util.VERBOSITY_ERROR);
				}
			}
			
			setStatusTextField(Util.get().string("record-status-playing-label"));

			final IntervalledSpotPlayer tempPlayer = new IntervalledSpotPlayer(SpotContainer.TYPE_TEMPORARY); // final in order to be accessed from inner class below
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
						Util.get().threadSleep(200);
					}
					
					setStatusTextField(Util.get().string("record-status-stopped-label"));
					
					recordButton.setEnabled(true);
					pauseButton.setEnabled(false);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
				}
			}).start();
			
		}
	}
}
