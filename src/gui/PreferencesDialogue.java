package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import main.Prefs;
import main.SpotMachine;
import main.Util;

public class PreferencesDialogue extends JFrame implements ActionListener {
	private static final long serialVersionUID = 3574893252942240648L;

	public PreferencesDialogue() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle(Util.get().string("prefs-headline"));
		
		JPanel panel = new JPanel();
		setContentPane(panel);  
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		panel.add(createLanguageSelectionPanel());
		panel.add(createOKCancelButtonPanel());
		
		pack();
	}
	
	private JComboBox langSelect;
	
	private JPanel createLanguageSelectionPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createTitledBorder(Util.get().string("prefs-language-bordertitle")));
		
		
		JPanel panel = new JPanel();
		
		panel.add(new JLabel(Util.get().string("prefs-language-label")));
		
		langSelect = new JComboBox();
		Locale[] locales = Util.get().getAvailableLocales();
		langSelect.addItem(Util.get().string("prefs-language-sysdefault-comboboxitem"));
		int selectedIndex = 0;
		for (int i = 0; i < locales.length; i++) {
			langSelect.addItem(locales[i].getDisplayName());
			if (Util.get().getSavedLocale() != null
					&& locales[i].toString().equals(Util.get().getSavedLocale().toString()) ) {
				selectedIndex = i+1;
			}
		}
		langSelect.setSelectedIndex(selectedIndex);
		panel.add(langSelect);
		mainPanel.add(panel);
		
		mainPanel.add(createLanguageNoteTextArea());
		
		return mainPanel;
	}
	
	private JTextArea createLanguageNoteTextArea() {
		JTextArea note = new JTextArea(Util.get().string("prefs-language-note-text"));
		note.setEditable(false);
		
		//Color textColour = new Color(255, 0, 0); // red text
		Color bgColour = new Color(255, 255, 255, 0); // "invisible" white, i.e. alpha value of 0
		note.setBackground(bgColour); 
		//note.setForeground(textColour);
		note.setSelectionColor(bgColour);
		//note.setSelectedTextColor(textColour);
		return note;
	}

	
	private JPanel createOKCancelButtonPanel() {
		JPanel panel = new JPanel();
		
		JButton okButton = new JButton(Util.get().string("prefs-ok-button"));
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		panel.add(okButton);
		
		JButton cancelButton = new JButton(Util.get().string("prefs-cancel-button"));
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("cancel");
		panel.add(cancelButton);
		
		return panel;
	}
	
	private void savePrefs() {
		if (langSelect.getSelectedIndex() == 0) {
			Util.get().out("Removing locale data from preferences file, if any.", Util.VERBOSITY_DEBUG_INFO);
			Prefs.prefs.remove(Prefs.LOCALE_LANGUAGE);
			Prefs.prefs.remove(Prefs.LOCALE_COUNTRY);
		} else {
			String userSelectedLocaleDisplayString = (String)langSelect.getSelectedItem();
			Locale userSelectedLocale = null;
			Locale[] allLocales = Util.get().getAvailableLocales();
			for (int i = 0; i < allLocales.length && userSelectedLocale == null; i++) {
				if (userSelectedLocaleDisplayString.equals(allLocales[i].getDisplayName()))
					userSelectedLocale = allLocales[i];
			}
			
			Util.get().out("Saving locale "
					+ userSelectedLocale.getLanguage() + "_" + userSelectedLocale.getCountry()
					+ " to preferences file.", Util.VERBOSITY_DEBUG_INFO);
			Prefs.prefs.put(Prefs.LOCALE_LANGUAGE, userSelectedLocale.getLanguage());
			Prefs.prefs.put(Prefs.LOCALE_COUNTRY, userSelectedLocale.getCountry());
		}
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
