package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.Calculate;
import main.SpotMachine;
import main.SpotRecorder;

public class RecordDialogue extends JFrame implements ActionListener {
	private static final long serialVersionUID = 2437740287713575921L;
	
	// Icons from http://java.sun.com/developer/techDocs/hi/repository/
	
	public RecordDialogue() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle("Optag nyt spot");
		
		JPanel panel = new JPanel();
		setContentPane(panel);  
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		panel.add(new JLabel("Navn:"));
		panel.add(new JTextField());
		panel.add(createCurrentLengthPanel());
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
		
		statusTextField = new JLabel("stoppet");
		panel.add(statusTextField);
		
		return panel;
	}
	
	private JButton recordButton;
	private JButton pauseButton;
	private JButton stopButton;
	
	private JPanel createControlButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Optagelse"));
		
		recordButton = new JButton("Optag forfra", Calculate.createImageIcon("../resources/Record24.gif"));
		recordButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    recordButton.setHorizontalTextPosition(AbstractButton.CENTER);
		recordButton.addActionListener(this);
		recordButton.setActionCommand("record");
		panel.add(recordButton);
		
		pauseButton = new JButton("Pause", Calculate.createImageIcon("../resources/Pause24.gif"));
		pauseButton.setEnabled(false);
		pauseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    pauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand("pause");
		panel.add(pauseButton);
		
		stopButton = new JButton("Stop", Calculate.createImageIcon("../resources/Stop24.gif"));
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
	
	private JPanel createOKCancelButtonPanel() {
		JPanel panel = new JPanel();
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		panel.add(okButton);
		
		JButton cancelButton = new JButton("Fortryd");
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
		} else if (action.equals("cancel")) {
			SpotMachine.getMainFrame().setEnabled(true);
			this.dispose();
		} else if (action.equals("record")) {
			recordButton.setEnabled(false);
			stopButton.setEnabled(true);
			pauseButton.setEnabled(true);
			rec = new SpotRecorder();
			new Thread(rec).start();
			statusTextField.setText("optager");
		} else if (action.equals("pause")) {
			// TODO: Detect if already paused and unpause instead if so
			recordButton.setEnabled(true);
			stopButton.setEnabled(true);
			pauseButton.setEnabled(true);
			statusTextField.setText("pause");
		} else if (action.equals("stop")) {
			recordButton.setEnabled(true);
			stopButton.setEnabled(false);
			pauseButton.setEnabled(false);
			statusTextField.setText("stopper...");
			if (rec != null) {
				rec.stopRecoding();
				rec = null;
			}
			statusTextField.setText("stoppet");
		}
	}
}
