package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.xnap.commons.i18n.I18n;

import main.Prefs;
import main.SpotMachine;
import main.Util;

public class PreferencesDialogue extends JFrame implements ActionListener {
	private static final long serialVersionUID = 3574893252942240648L;
	
	private I18n i18n;

	public PreferencesDialogue() {
		i18n = Util.get().i18n();
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle(i18n.tr("Preferences"));
		
		JPanel panel = new JPanel();
		setContentPane(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		panel.add(createLanguageSelectionPanel());
		panel.add(createNormalizationPanel());
		panel.add(createOKCancelButtonPanel());
		
		pack();
	}
	
	private JComboBox langSelect;
	
	private JPanel createLanguageSelectionPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createTitledBorder(i18n.tr("Program Language")));
		
		JPanel panel = new JPanel();
		
		panel.add(new JLabel(i18n.tr("Choose language")));
		
		langSelect = new JComboBox();
		Locale[] locales = Util.get().getSupportedLocales();
		for (int i = 0; i < locales.length; i++) {
			langSelect.addItem(locales[i].getDisplayName());
			if (locales[i].toString().equals(Util.get().getCurrentLocale().toString()) ) {
		        langSelect.setSelectedIndex(i);
			}
		}
		panel.add(langSelect);
		mainPanel.add(panel);
		
		mainPanel.add(createLanguageNoteTextArea());
		
		return mainPanel;
	}
	
	private JTextArea createLanguageNoteTextArea() {
		JTextArea note = new JTextArea(Util.get().wordWrap(i18n.tr("Changes will be applied after program restart."), 50));
		note.setEditable(false);
		
		//Color textColour = new Color(255, 0, 0); // red text
		Color bgColour = new Color(255, 255, 255, 0); // "invisible" white, i.e. alpha value of 0
		note.setBackground(bgColour); 
		//note.setForeground(textColour);
		note.setSelectionColor(bgColour);
		//note.setSelectedTextColor(textColour);
		return note;
	}
	
	private JCheckBox dcCheckbox;
	private JCheckBox fadeCheckbox;
	private JCheckBox volumeCheckbox;
	
	private JPanel createNormalizationPanel() {
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder(i18n.tr("Audio Normalization")));
		
		panel.add(new JLabel(i18n.tr("Perform on newly recorded spots:")));
		
		dcCheckbox = new JCheckBox(i18n.tr("Remove DC offset"));
		dcCheckbox.setToolTipText(i18n.tr("May remove certain 'click' noises in start/end of recording"));
		dcCheckbox.setSelected(
				Prefs.prefs.getBoolean(Prefs.RECORDING_DO_DC_OFFSET_REMOVAL, Prefs.RECORDING_DO_DC_OFFSET_REMOVAL_DEFAULT));
		panel.add(dcCheckbox);
		
		fadeCheckbox = new JCheckBox(i18n.tr("Add fade-in/out"));
		fadeCheckbox.setToolTipText(i18n.tr("Add short fade-in and fade-out"));
		fadeCheckbox.setSelected(
				Prefs.prefs.getBoolean(Prefs.RECORDING_DO_FADEIN_FADEOUT, Prefs.RECORDING_DO_FADEIN_FADEOUT_DEFAULT));
		panel.add(fadeCheckbox);
		
		volumeCheckbox = new JCheckBox(i18n.tr("Normalise volume"));
		volumeCheckbox.setToolTipText(i18n.tr("Gives a uniform volume level of spots"));
		volumeCheckbox.setSelected(
				Prefs.prefs.getBoolean(Prefs.RECORDING_DO_VOLUME_NORMALIZATION, Prefs.RECORDING_DO_VOLUME_NORMALIZATION_DEFAULT));
		panel.add(volumeCheckbox);
		
		return panel;
	}
	
	private JPanel createOKCancelButtonPanel() {
		JPanel panel = new JPanel();
		
		JButton okButton = new JButton(i18n.tr("OK"));
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		panel.add(okButton);
		
		JButton cancelButton = new JButton(i18n.tr("Cancel"));
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("cancel");
		panel.add(cancelButton);
		
		return panel;
	}
	
	private void savePrefs() {
		// Save language prefs
			String userSelectedLocaleDisplayString = (String)langSelect.getSelectedItem();
			Locale userSelectedLocale = null;
			Locale[] allLocales = Util.get().getSupportedLocales();
			for (int i = 0; i < allLocales.length && userSelectedLocale == null; i++) {
				if (userSelectedLocaleDisplayString.equals(allLocales[i].getDisplayName()))
					userSelectedLocale = allLocales[i];
			}
			
			Util.get().out("Saving locale "
					+ userSelectedLocale.getLanguage() + "_" + userSelectedLocale.getCountry()
					+ " to preferences file.", Util.VERBOSITY_DEBUG_INFO);
			Prefs.prefs.put(Prefs.LOCALE_LANGUAGE, userSelectedLocale.getLanguage());
			Prefs.prefs.put(Prefs.LOCALE_COUNTRY, userSelectedLocale.getCountry());
		
		// Save normalization prefs
		Util.get().out("Saving normalization prefs: " +
				"DC " + dcCheckbox.isSelected() + ", " +
				"fade " + fadeCheckbox.isSelected() + ", " +
				"vol " + volumeCheckbox.isSelected(),
				Util.VERBOSITY_DEBUG_INFO);
		Prefs.prefs.putBoolean(Prefs.RECORDING_DO_DC_OFFSET_REMOVAL, dcCheckbox.isSelected());
		Prefs.prefs.putBoolean(Prefs.RECORDING_DO_FADEIN_FADEOUT, fadeCheckbox.isSelected());
		Prefs.prefs.putBoolean(Prefs.RECORDING_DO_VOLUME_NORMALIZATION, volumeCheckbox.isSelected());
	}

	public void actionPerformed(ActionEvent ae) {
		Util.get().out("Preferences window action performed! " + ae.getActionCommand(), Util.VERBOSITY_DEBUG_INFO);
		String action = ae.getActionCommand();
		if (action.equals("ok")) {
			savePrefs();
			SpotMachine.getMainFrame().setEnabled(true);
			this.dispose();
		} else if (action.equals("cancel")) {
			SpotMachine.getMainFrame().setEnabled(true);
			this.dispose();
		}
	}
}
