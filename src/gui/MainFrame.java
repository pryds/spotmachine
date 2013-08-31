package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.xnap.commons.i18n.I18n;

import main.IntervalledSpotPlayer;
import main.ScheduledSpotPlayer;
import main.Util;
import main.Prefs;
import main.SpotContainer;
import main.SpotEntry;
import main.SpotMachine;

public class MainFrame extends JFrame implements ChangeListener, ActionListener, ItemListener {
	private static final long serialVersionUID = 6219825567861104713L;
	
	// Icons from http://java.sun.com/developer/techDocs/hi/repository/
	
	private RecordDialogue recordDialogue = null;
	private I18n i18n;
	
	public MainFrame(String title) {
		super(title);
		i18n = Util.get().i18n();
		//setResizable(false);
		setContentPane(new JPanel());
		getContentPane().setLayout(new BorderLayout());
		
		getContentPane().add(createMenuBar(), BorderLayout.PAGE_START);
		JPanel p = new JPanel();
		getContentPane().add(p, BorderLayout.CENTER);
		
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(createCountdownPanel());
		p.add(createUpperMainPanel());
		p.add(new JSeparator(SwingConstants.HORIZONTAL));
		p.add(createLowerMainPanel());
	}
	
	private JTextField countdownTextField;
	private JLabel nextSpotLabel;
	
	private JPanel createCountdownPanel() {
		JPanel panel = new JPanel();
		panel.add(new JLabel(i18n.tr("Time until next spot:")));
		countdownTextField = new JTextField(
				Util.get().millisToMinsSecsString(Prefs.prefs.getLong(Prefs.MILLIS_BETWEEN_SPOTS, Prefs.MILLIS_BETWEEN_SPOTS_DEFAULT))
				);
		countdownTextField.setEditable(false);
		countdownTextField.setColumns(5);
		panel.add(countdownTextField);
		nextSpotLabel = new JLabel();
		panel.add(nextSpotLabel);
		return panel;
	}
	
	private void setNextSpotLabel(int index, String name) {
		nextSpotLabel.setText("(" + i18n.tr("#") + " " + (index+1) + ", \"" + name + "\")");
	}
	
	public void setNextSpotLabel(int index, SpotEntry spot) {
		if (spot != null)
			setNextSpotLabel(index, spot.getName());
		else
			setNextSpotLabel(0, "-");
	}
	
	public void setCountDownFieldValue(long millis) {
		countdownTextField.setText(Util.get().millisToMinsSecsString(millis));
	}
	
	private JButton playButton;
	private JButton pauseButton;
	
	private JPanel createUpperMainPanel() {
		JPanel panel = new JPanel();
		
		playButton = new JButton(i18n.tr("Play"), Util.get().createImageIcon("../resources/Play24.gif"));
		playButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    playButton.setHorizontalTextPosition(AbstractButton.CENTER);
		playButton.addActionListener(this);
		playButton.setActionCommand("play");
		panel.add(playButton);
		
		pauseButton = new JButton(i18n.tr("Pause"), Util.get().createImageIcon("../resources/Pause24.gif"));
		pauseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    pauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
		pauseButton.addActionListener(this);
		pauseButton.setActionCommand("pause");
		pauseButton.setEnabled(false);
		panel.add(pauseButton);
		
		JButton previousButton = new JButton(i18n.tr("Previous"), Util.get().createImageIcon("../resources/StepBack24.gif"));
		previousButton.setVerticalTextPosition(AbstractButton.BOTTOM);
	    previousButton.setHorizontalTextPosition(AbstractButton.CENTER);
	    previousButton.addActionListener(this);
		previousButton.setActionCommand("previous");
		panel.add(previousButton);
		
		JButton nextButton = new JButton(i18n.tr("Next"), Util.get().createImageIcon("../resources/StepForward24.gif"));
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
		removeFromAvailableButton.setEnabled(paused);
	}
	
	private JPanel createLowerMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(createAvailableSpotsPanel());
		
		JPanel activeSpotsPanel = new JPanel();
		activeSpotsPanel.setLayout(new BoxLayout(activeSpotsPanel, BoxLayout.PAGE_AXIS));
		panel.add(activeSpotsPanel);
		
		JPanel intervalledSpotsPanel = new JPanel();
		intervalledSpotsPanel.setLayout(new BoxLayout(intervalledSpotsPanel, BoxLayout.LINE_AXIS));
		intervalledSpotsPanel.add(createIntervalledSpotTransferPanel());
		intervalledSpotsPanel.add(createIntervalledSpotsPanel());
		activeSpotsPanel.add(intervalledSpotsPanel);
		
		JPanel scheduledSpotsPanel = new JPanel();
		scheduledSpotsPanel.setLayout(new BoxLayout(scheduledSpotsPanel, BoxLayout.LINE_AXIS));
		scheduledSpotsPanel.add(createScheduledSpotsTransferPanel());
		scheduledSpotsPanel.add(createScheduledSpotsPanel());
		activeSpotsPanel.add(scheduledSpotsPanel);
		
		return panel;
	}
	
	private SpotList availableSpotList;
	private JButton recordNewButton;
	private JButton removeFromAvailableButton;
	
	private JPanel createAvailableSpotsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(i18n.tr("Available spots")));
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		SpotContainer availableSpots = SpotMachine.getAvailableSpots();
		availableSpotList = new SpotList(new SpotListModel(SpotContainer.TYPE_AVAILABLE));
		availableSpotList.getModel().replaceData(availableSpots.getDataCopy());
		panel.add(availableSpotList.getContainingScrollPane());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 0));
		
		recordNewButton = new JButton(i18n.tr("Record new"));
		recordNewButton.addActionListener(this);
		recordNewButton.setActionCommand("record");
		buttonPanel.add(recordNewButton);
		
		removeFromAvailableButton = new JButton(i18n.tr("Delete"));
		removeFromAvailableButton.addActionListener(this);
		removeFromAvailableButton.setActionCommand("removefromavailable");
		buttonPanel.add(removeFromAvailableButton);
		
		JButton importButton = new JButton(""); //i18n.tr("Import")
		importButton.setEnabled(false);
		importButton.addActionListener(this);
		importButton.setActionCommand("importspot");
		buttonPanel.add(importButton);
		
		JButton renameButton = new JButton(i18n.tr("Rename"));
		renameButton.addActionListener(this);
		renameButton.setActionCommand("rename");
		buttonPanel.add(renameButton);
		
		panel.add(buttonPanel);
		
		return panel;
	}
	
	private JPanel createIntervalledSpotTransferPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JButton copyToIntervalledButton = new JButton(Util.get().createImageIcon("../resources/Forward24.gif"));
		copyToIntervalledButton.setToolTipText(i18n.tr("Insert selected available spot into list of interval spots"));
		copyToIntervalledButton.addActionListener(this);
		copyToIntervalledButton.setActionCommand("copytointervalled");
		panel.add(copyToIntervalledButton);
	    
		JButton removeFromIntervalledButton = new JButton(Util.get().createImageIcon("../resources/Back24.gif"));
		removeFromIntervalledButton.setToolTipText(i18n.tr("Remove spot from list of interval spots"));
		removeFromIntervalledButton.addActionListener(this);
		removeFromIntervalledButton.setActionCommand("removefromintervalled");
		panel.add(removeFromIntervalledButton);
		
		return panel;
	}

	private JSpinner minBetweenSpotsSpinner;
	
	private SpotList intervalledSpotList;
	private JCheckBox repeatAllCheckBox;
	
	private JPanel createIntervalledSpotsPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createTitledBorder(i18n.tr("Interval spots")));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		while(SpotMachine.getIntervalledSpotPlayer() == null) {
			Util.get().out("GUI; IntervalledSpotPlayer not initialized yet. Waiting a bit and then retrying.", Util.VERBOSITY_WARNING);
			Util.get().threadSleep(500);
		}
		
		IntervalledSpotPlayer intervalSpots = SpotMachine.getIntervalledSpotPlayer();
		intervalledSpotList = new SpotList(new SpotListModel(SpotContainer.TYPE_INTERVALLED));
		intervalledSpotList.getModel().replaceData(intervalSpots.getDataCopy());
		intervalledSpotList.setNextSpot(intervalSpots.getNextSpotToPlayIndex());
		if (intervalSpots.getNextSpotToPlayIndex() != -1 && intervalSpots.getNextSpotToPlay() != null)
			setNextSpotLabel(
					intervalSpots.getNextSpotToPlayIndex(), 
					intervalSpots.getNextSpotToPlay().getName());
		else
			setNextSpotLabel(0, "-");
		panel.add(intervalledSpotList.getContainingScrollPane());
		repeatAllCheckBox = new JCheckBox(i18n.tr("Repeat all"));
		repeatAllCheckBox.setToolTipText(i18n.tr("Repeat list of spots when last spot has been played"));
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
		spinnerPanel.add(new JLabel(i18n.tr("mins between spots")));
		panel.add(spinnerPanel);
		
		mainPanel.add(panel);
		mainPanel.add(createChangeOrderIntervalledPanel());
		
		return mainPanel;
	}
	
	private JPanel createScheduledSpotsTransferPanel() {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	    
	    JButton copyToScheduledButton = new JButton(Util.get().createImageIcon("../resources/Forward24.gif"));
	    copyToScheduledButton.setToolTipText(i18n.tr("Insert selected available spot into list of scheduled spots"));
	    copyToScheduledButton.addActionListener(this);
	    copyToScheduledButton.setActionCommand("copytoscheduled");
	    panel.add(copyToScheduledButton);
	    
	    JButton removeFromScheduledButton = new JButton(Util.get().createImageIcon("../resources/Back24.gif"));
	    removeFromScheduledButton.setToolTipText(i18n.tr("Remove spot from list of scheduled spots"));
	    removeFromScheduledButton.addActionListener(this);
	    removeFromScheduledButton.setActionCommand("removefromscheduled");
	    panel.add(removeFromScheduledButton);
	    
	    return panel;
	}
	
	private SpotList scheduledSpotList;
	
	private JPanel createScheduledSpotsPanel() {
	    JPanel panel = new JPanel();
	    panel.setBorder(BorderFactory.createTitledBorder(i18n.tr("Scheduled spots")));
	    
	    while(SpotMachine.getScheduledSpotPlayer() == null) {
	        Util.get().out("GUI: ScheduledSpotPlayer not initialized yet. Waiting a bit and then retrying.", Util.VERBOSITY_WARNING);
	        Util.get().threadSleep(500);
	    }
	    
	    ScheduledSpotPlayer scheduledSpots = SpotMachine.getScheduledSpotPlayer();
	    scheduledSpotList = new SpotList(new SpotListModel(SpotContainer.TYPE_SCHEDULED));
	    Vector<SpotEntry> v = scheduledSpots.getDataCopy();
	    scheduledSpotList.getModel().replaceData(v);
	    panel.add(scheduledSpotList.getContainingScrollPane());
	    
	    panel.add(createEditScheduledPanel());
	    
	    return panel;
	}
	
	public SpotList getAvailableSpotList() {
		return availableSpotList;
	}
	
	public SpotList getIntervalledSpotList() {
		return intervalledSpotList;
	}
	
	public SpotList getScheduledSpotList() {
	    return scheduledSpotList;
	}
	
	public RecordDialogue getRecordDialogue() {
		return recordDialogue;
	}
	
	public void setRecordDialogueNull() {
		recordDialogue = null;
	}
	
	private JPanel createChangeOrderIntervalledPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JButton upButton = new JButton(i18n.tr("Move up"), Util.get().createImageIcon("../resources/Up24.gif"));
		upButton.addActionListener(this);
		upButton.setActionCommand("intervalledmoveup");
		panel.add(upButton);
		
		JButton downButton = new JButton(i18n.tr("Move down"), Util.get().createImageIcon("../resources/Down24.gif"));
		downButton.addActionListener(this);
		downButton.setActionCommand("intervalledmovedown");
		panel.add(downButton);
		
		return panel;
	}
	
	private JPanel createEditScheduledPanel() {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	    
	    JButton editButton = new JButton(i18n.tr("Edit"), Util.get().createImageIcon("../resources/Edit24.gif"));
	    editButton.addActionListener(this);
	    editButton.setActionCommand("editscheduled");
	    panel.add(editButton);
	    
	    return panel;
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menu = new JMenu(i18n.tr("File"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem(i18n.tr("Preferences"));
		menuItem.setMnemonic(KeyEvent.VK_P);
		menuItem.addActionListener(this);
		menuItem.setActionCommand("prefs");
		menu.add(menuItem);
		
		menuItem = new JMenuItem(i18n.tr("About {0}", SpotMachine.PROGRAM_NAME));
		menuItem.setMnemonic(KeyEvent.VK_A);
		menuItem.addActionListener(this);
		menuItem.setActionCommand("aboutprogram");
		menu.add(menuItem);
		
		return menuBar;
	}

	
	public void stateChanged(ChangeEvent e) {
		Util.get().out("ChangeListener: State changed!", Util.VERBOSITY_DEBUG_INFO);
		JComponent source = (JComponent)e.getSource();
		if (source == minBetweenSpotsSpinner) {
			double readValue = (Double)((JSpinner)source).getValue();
			SpotMachine.getIntervalledSpotPlayer().setMillisBetweenSpots((int)Math.round(readValue * 60 * 1000));
		}
	}

	public void actionPerformed(ActionEvent e) {
		Util.get().out("ActionListener: Action performed! " + e.getActionCommand(), Util.VERBOSITY_DEBUG_INFO);
		String action = e.getActionCommand();
		if (action.equals("intervalledmoveup") || action.equals("intervalledmovedown")) {
			int oldPos = intervalledSpotList.getSelectedRow();
			if (oldPos != -1 && intervalledSpotList.getModel().getRowCount() > 0) {
				int newPos;
				if (action.equals("intervalledmoveup")) {
					newPos = oldPos - 1;
				} else {
					newPos = oldPos + 1;
				}
				if (newPos < 0 || newPos > intervalledSpotList.getRowCount() - 1) {
					Util.get().out("Trying to move an intervalled spot off-range. Ignored.", Util.VERBOSITY_WARNING);
					return;
				}
				intervalledSpotList.swapRows(newPos, oldPos);
				intervalledSpotList.getSelectionModel().setSelectionInterval(newPos, newPos);
				SpotMachine.getIntervalledSpotPlayer().swapSpots(newPos, oldPos);
				setNextSpotLabel(SpotMachine.getIntervalledSpotPlayer().getNextSpotToPlayIndex(), SpotMachine.getIntervalledSpotPlayer().getNextSpotToPlay().getName());
			}
		} else if (action.equals("play")) {
			SpotMachine.getIntervalledSpotPlayer().setPaused(false);
			setGUIPaused(false);
		} else if (action.equals("pause")) {
			SpotMachine.getIntervalledSpotPlayer().setPaused(true);
			setGUIPaused(true);
		} else if (action.equals("previous")) {
			int prev = SpotMachine.getIntervalledSpotPlayer().setNextSpotToPlayOneBackward();
			intervalledSpotList.setNextSpot(prev);
			setNextSpotLabel(prev, SpotMachine.getIntervalledSpotPlayer().getSpotAt(prev).getName());
		} else if (action.equals("next")) {
			int next = SpotMachine.getIntervalledSpotPlayer().setNextSpotToPlayOneForward();
			intervalledSpotList.setNextSpot(next);
			setNextSpotLabel(next, SpotMachine.getIntervalledSpotPlayer().getSpotAt(next).getName());
		} else if (action.equals("copytointervalled")) {
			int selectedAvailable = availableSpotList.getSelectedRow();
			if (selectedAvailable != -1 && availableSpotList.getModel().getRowCount() > 0) {
				int selectedIntervalled = intervalledSpotList.getSelectedRow();
				SpotEntry source = SpotMachine.getAvailableSpots().getSpotAt(selectedAvailable);
				SpotMachine.getIntervalledSpotPlayer().addToEnd(source);
				intervalledSpotList.getModel().addToEnd(source);
				if (SpotMachine.getIntervalledSpotPlayer().numberOfSpots() == 1) { // i.e., if this is the only spot
					Util.get().out("Added spot to empty list. Setting next spot to that spot.", Util.VERBOSITY_DEBUG_INFO);
					SpotMachine.getIntervalledSpotPlayer().setNextSpotToPlay(0);
					setNextSpotLabel(0, source.getName());
					intervalledSpotList.setNextSpot(0);
				}
				intervalledSpotList.getSelectionModel().setSelectionInterval(selectedIntervalled, selectedIntervalled);
			}
		} else if (action.equals("removefromintervalled")) {
			int selectedIntervalled = intervalledSpotList.getSelectedRow();
			if (selectedIntervalled != -1  && intervalledSpotList.getModel().getRowCount() > 0) {
				SpotMachine.getIntervalledSpotPlayer().remove(selectedIntervalled);
				intervalledSpotList.remove(selectedIntervalled);
				int newSelection = (selectedIntervalled-1 >= 0) ? selectedIntervalled-1 : 0;
				intervalledSpotList.getSelectionModel().setSelectionInterval(newSelection, newSelection);
			}
		} else if (action.equals("removefromavailable")) {
			int selectedAvailable = availableSpotList.getSelectedRow();
			if (selectedAvailable != -1 && availableSpotList.getModel().getRowCount() > 0) {
				Object[] options = {i18n.tr("Yes, delete spot!"), i18n.tr("No")};
				int userChoise = JOptionPane.showOptionDialog(
						this,
						i18n.tr("Are you sure you want to delete spot permanently?\n" +
								"The spot will be removed from all lists.\nYou cannot undo this action."),
						i18n.tr("Delete spot"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null, // icon
						options,
						options[1]
				);
				
				if (userChoise == 0) {
					SpotEntry removedAvailableSpot = SpotMachine.getAvailableSpots().remove(selectedAvailable);
					int[] removedIntervalSpots = SpotMachine.getIntervalledSpotPlayer().removeAllSpotsContaining(removedAvailableSpot);
					Util.get().out("RemovedIntervalSpots: " + Arrays.toString(removedIntervalSpots), Util.VERBOSITY_DEBUG_INFO);
					
					Util.get().deleteFile(removedAvailableSpot.getFile());
					
					availableSpotList.remove(selectedAvailable);
					intervalledSpotList.removeAll(removedIntervalSpots);
					
					int newSelection = (selectedAvailable-1 >= 0) ? selectedAvailable-1 : 0;
					availableSpotList.getSelectionModel().setSelectionInterval(newSelection, newSelection);
				}
			}
		} else if (action.equals("record")) {
			(recordDialogue = new RecordDialogue()).setVisible(true);
			this.setEnabled(false);
		} else if (action.equals("importspot")) {
			// TODO
		} else if (action.equals("rename")) {
			int selectedAvailable = availableSpotList.getSelectedRow();
			if (selectedAvailable != -1 && availableSpotList.getModel().getRowCount() > 0) {
				SpotEntry spot = SpotMachine.getAvailableSpots().getSpotAt(selectedAvailable);
				
				String newName = null;
				do {
					newName = (String)JOptionPane.showInputDialog(
							this,
							i18n.tr("Type new name for spot:"),
							i18n.tr("Rename spot"),
							JOptionPane.PLAIN_MESSAGE,
							null, // icon
							null, // options, null gives text field
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
        } else if (action.equals("copytoscheduled")) {
            int selectedAvailable = availableSpotList.getSelectedRow();
            if (selectedAvailable != -1 && availableSpotList.getModel().getRowCount() > 0) {
                final SpotEntry source = SpotMachine.getAvailableSpots().getSpotAt(selectedAvailable);
                final SchedulePropertiesDialogue spd = new SchedulePropertiesDialogue();
                spd.setVisible(true);
                this.setEnabled(false);
                
                /**
                 * The following thread will wait for the user to click ok
                 * in the properties dialogue, and then do the appropriate
                 * changes, followed by reenabling the main window.
                 * We can't just wait in the current thread, as it would halt
                 * the GUI, making the impression that the program has stalled
                 */
                new Thread(new Runnable() {
                    public void run() {
                        while (spd.isVisible()) {
                            Util.get().threadSleep(200);
                        }
                        if (spd.isOkClicked()) {
                            SpotEntry addedSpot = SpotMachine.getScheduledSpotPlayer().addToEnd(source, spd.getSchedule());
                            SpotMachine.getMainFrame().getScheduledSpotList().getModel().addToEnd(addedSpot);
                        }
                        SpotMachine.getMainFrame().setEnabled(true);
                    }
                }).start();
            }
        } else if (action.equals("removefromscheduled")) {
            int selectedScheduledSpot = scheduledSpotList.getSelectedRow();
            if (selectedScheduledSpot != -1  && scheduledSpotList.getModel().getRowCount() > 0) {
                SpotMachine.getScheduledSpotPlayer().remove(selectedScheduledSpot);
                scheduledSpotList.remove(selectedScheduledSpot);
                int newSelection = (selectedScheduledSpot-1 >= 0) ? selectedScheduledSpot-1 : 0;
                scheduledSpotList.getSelectionModel().setSelectionInterval(newSelection, newSelection);
            }
        } else if (action.equals("editscheduled")) {
            final int selectedScheduledSpot = scheduledSpotList.getSelectedRow();
            if (selectedScheduledSpot != -1 && scheduledSpotList.getModel().getRowCount() > 0) {
                SpotEntry selectedSpot = SpotMachine.getScheduledSpotPlayer().getSpotAt(selectedScheduledSpot);
                final SchedulePropertiesDialogue spd = new SchedulePropertiesDialogue(selectedSpot.getSchedule());
    		    spd.setVisible(true);
                this.setEnabled(false);
                
                /**
                 * The following thread will wait for the user to click ok
                 * in the properties dialogue, and then do the appropriate
                 * changes, followed by reenabling the main window.
                 * We can't just wait in the current thread, as it would halt
                 * the GUI, making the impression that the program has stalled
                 */
                new Thread(new Runnable() {
                    public void run() {
                        while (spd.isVisible()) {
                            Util.get().threadSleep(200);
                        }
                        if (spd.isOkClicked()) {
                            SpotMachine.getScheduledSpotPlayer().setNewScheduleForSpot(selectedScheduledSpot, spd.getSchedule());
                            SpotMachine.getMainFrame().getScheduledSpotList().getModel().setPlayAt(selectedScheduledSpot, spd.getSchedule());
                        }
                        SpotMachine.getMainFrame().setEnabled(true);
                    }
                }).start();
            }
		} else if (action.equals("aboutprogram")) {
			JOptionPane.showMessageDialog(this,
					i18n.tr("{0} version {1}", SpotMachine.PROGRAM_NAME, SpotMachine.PROGRAM_VERSION) + "\n" +
				    i18n.tr("By {0}", "Thomas Pryds") + "\n" +
				    i18n.trc("Correct into your language and name", "Translated into English by Thomas Pryds") + "\n" +
				    "http://pryds.eu/spotmachine\n" +
				    "\n" +
				    Util.get().wordWrap(i18n.tr("Released under the GPL license. This program can be " +
				    		"used without restrictions. This includes redistribution, with or without own " +
				    		"changes (e.g. additions and translations). However, redistribution (including " +
				    		"changes) may only happen under the same license. The complete license text comes " +
				    		"with this program. The complete source code for this program is available at the " +
				    		"above mentioned web site."), 70),
				    		i18n.tr("About {0}", SpotMachine.PROGRAM_NAME),
				    JOptionPane.INFORMATION_MESSAGE);
		} else if (action.equals("prefs")) {
			PreferencesDialogue preferencesDialogue = new PreferencesDialogue();
			preferencesDialogue.setVisible(true);
			this.setEnabled(false);
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Util.get().out("ItemListener: Item state changed!", Util.VERBOSITY_DEBUG_INFO);
		Object source = e.getItemSelectable();
		if (source == repeatAllCheckBox) {
			SpotMachine.getIntervalledSpotPlayer().setRepeatAll(repeatAllCheckBox.isSelected());
		}
	}
}
