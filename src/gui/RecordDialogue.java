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

import org.xnap.commons.i18n.I18n;

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
	
	private I18n i18n;
	
	// Icons from http://java.sun.com/developer/techDocs/hi/repository/
	
	
	private JTextField spotNameTextField;
	
	public RecordDialogue() {
		i18n = Util.get().i18n();
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle(i18n.tr("Record New Spot"));
		
		JPanel panel = new JPanel();
		setContentPane(panel);  
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		panel.add(new JLabel(i18n.tr("Name:")));
		
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
		panel.add(new JLabel(i18n.tr("Current duration:") + " "));
		
		currentLengthTextField = new JTextField("0:00");
		currentLengthTextField.setEditable(false);
		panel.add(currentLengthTextField);
		
		return panel;
	}
	
	private JPanel createStatusTextFieldPanel() {
		JPanel panel = new JPanel();
		
		statusTextField = new JLabel(i18n.tr("STOPPED"));
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
		panel.setBorder(BorderFactory.createTitledBorder(i18n.trc("Noun, as in 'a recording'", "Recording")));
		
		recordButton = new JButton(i18n.tr("Re-record"), Util.get().createImageIcon("../resources/Record24.gif"));
		recordButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    recordButton.setHorizontalTextPosition(AbstractButton.CENTER);
		recordButton.addActionListener(this);
		recordButton.setActionCommand("record");
		panel.add(recordButton);
		
		pauseButton = new JButton(i18n.tr("Pause"), Util.get().createImageIcon("../resources/Pause24.gif"));
		pauseButton.setEnabled(false);
		pauseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    pauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand("pause");
		//panel.add(pauseButton);
		
		stopButton = new JButton(i18n.tr("Stop"), Util.get().createImageIcon("../resources/Stop24.gif"));
		stopButton.setEnabled(false);
		stopButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    stopButton.setHorizontalTextPosition(AbstractButton.CENTER);
		stopButton.addActionListener(this);
		stopButton.setActionCommand("stop");
		panel.add(stopButton);
		
		return panel;
	}
	
	private JTextArea createNoteTextArea() {
		JTextArea note = new JTextArea(Util.get().wordWrap(i18n.tr("Remember to disconnect audio output, " +
				"amplifier, or similar before recording starts, since the recorded spot will be played " +
				"immediately after recording has finished."), 50));
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
		
		okButton = new JButton(i18n.tr("OK"));
		okButton.setToolTipText(i18n.tr("Close the recording window and save the recorded spot"));
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		panel.add(okButton);
		
		cancelButton = new JButton(i18n.tr("Cancel"));
		cancelButton.setToolTipText(i18n.tr("Close the recording window without saving"));
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
						i18n.tr("No recording has been done yet."),
					    i18n.tr("No recording"),
					    JOptionPane.WARNING_MESSAGE);
			} else if (spotName.length() == 0) {
				Util.get().out("No spot name given. Ignoring ok request.", Util.VERBOSITY_WARNING);
				JOptionPane.showMessageDialog(SpotMachine.getMainFrame().getRecordDialogue(),
						i18n.tr("Write a name for the recorded spot."),
					    i18n.tr("No name for spot"),
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
			setStatusTextField(i18n.trc("Verb, as in 'now recording'", "RECORDING"));
		} else if (action.equals("pause")) {
			// TODO: Detect if already paused and unpause instead if so
			recordButton.setEnabled(true);
			stopButton.setEnabled(true);
			pauseButton.setEnabled(true);
			setStatusTextField(i18n.tr("PAUSED"));
		} else if (action.equals("stop")) {
			stopButton.setEnabled(false);

			setStatusTextField(i18n.trc("I.e. stopping the process of recording", "STOPPING RECORDING…"));
			if (rec != null) {
				rec.stopRecoding();
				lastFinishedRecording = rec.getOutFile();
				rec = null;
			}
			
			boolean doDCOffset = Prefs.prefs.getBoolean(Prefs.RECORDING_DO_DC_OFFSET_REMOVAL, Prefs.RECORDING_DO_DC_OFFSET_REMOVAL_DEFAULT);
			boolean doFade = Prefs.prefs.getBoolean(Prefs.RECORDING_DO_FADEIN_FADEOUT, Prefs.RECORDING_DO_FADEIN_FADEOUT_DEFAULT);
			boolean doVolume = Prefs.prefs.getBoolean(Prefs.RECORDING_DO_VOLUME_NORMALIZATION, Prefs.RECORDING_DO_VOLUME_NORMALIZATION_DEFAULT);
			
			if (doDCOffset || doFade || doVolume) {
				setStatusTextField(i18n.tr("NORMALISING AUDIO"));
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
			
			setStatusTextField(i18n.tr("PLAYING"));

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
					
					setStatusTextField(i18n.tr("STOPPED"));
					
					recordButton.setEnabled(true);
					pauseButton.setEnabled(false);
					okButton.setEnabled(true);
					cancelButton.setEnabled(true);
				}
			}).start();
			
		}
	}
}
