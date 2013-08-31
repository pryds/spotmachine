package gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import org.xnap.commons.i18n.I18n;

import main.PlaySchedule;
import main.Util;

public class SchedulePropertiesDialogue extends JFrame implements ActionListener {
    private static final long serialVersionUID = 4288923338379614705L;
    
    private PlaySchedule schedule;
    private boolean okClicked;
    
	private I18n i18n;
    
    public SchedulePropertiesDialogue() {
        // For copying new spot to scheduled spot list
        this(null);
    }
    
    public SchedulePropertiesDialogue(PlaySchedule schedule) {
        // For editing existing scheduled spot's details
        this.schedule = schedule;
        
        i18n = Util.get().i18n();
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setTitle(i18n.tr("Properties for Scheduled Spot"));
        
        okClicked = false;
        
        JPanel panel = new JPanel();
        setContentPane(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        
        panel.add(createTimePanel());
        panel.add(createWeekdayPanel());
        panel.add(createDayOfMonthPanel());
        panel.add(createMonthsPanel());
        panel.add(createNoteTextArea());
        panel.add(createOkCancelPanel());
        
        pack();
    }
    
    private JSpinner hourSpinner, minuteSpinner;
    
    private JPanel createTimePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(i18n.tr("Play spot at (24 hours format)")));
        
        int defaultHour, defaultMinute;
        if (schedule != null) {
            System.out.println("Getting time from schedule " + schedule);
            defaultHour = schedule.getHourToPlay();
            defaultMinute = schedule.getMinuteToPlay();
        } else {
            System.out.println("Getting time from now. schedule = " + schedule);
            Calendar now = Calendar.getInstance();
            defaultHour = now.get(Calendar.HOUR_OF_DAY);
            defaultMinute = now.get(Calendar.MINUTE);
        }
        
        hourSpinner = new JSpinner(new SpinnerNumberModel(defaultHour, 0, 23, 1));
        panel.add(hourSpinner);
        
        panel.add(new JLabel(" : "));
        
        minuteSpinner = new JSpinner(new SpinnerNumberModel(defaultMinute, 0, 59, 1));
        panel.add(minuteSpinner);
        
        return panel;
    }
    
    private JCheckBox[] weekdayCheckBox;
    
    private JPanel createWeekdayPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(i18n.tr("On these weekdays")));
        panel.setLayout(new GridLayout(2, 4));
        String[] weekdayStrings = new DateFormatSymbols(Util.get().getCurrentLocale()).getWeekdays();
        weekdayCheckBox = new JCheckBox[7];
        
        for (int i = 0; i < weekdayCheckBox.length; i++) {
            weekdayCheckBox[i] = 
                new JCheckBox(weekdayStrings[i+1]);
            weekdayCheckBox[i].setSelected(schedule != null ? schedule.getWeekdaysToPlay()[i] : true);
            panel.add(weekdayCheckBox[i]);
        }
        
        return panel;
    }
    
    private JCheckBox[] dayOfMonthCheckBox;
    
    private JPanel createDayOfMonthPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(i18n.tr("On these days of the month")));
        panel.setLayout(new GridLayout(5, 7));
        dayOfMonthCheckBox = new JCheckBox[31];
        
        for (int i = 0; i < dayOfMonthCheckBox.length; i++) {
            dayOfMonthCheckBox[i] = new JCheckBox("" + (i+1));
            dayOfMonthCheckBox[i].setSelected(schedule != null ? schedule.getDaysToPlay()[i] : true);
            panel.add(dayOfMonthCheckBox[i]);
        }
        
        return panel;
    }
    
    private JCheckBox[] monthCheckBox;

    private JPanel createMonthsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(i18n.tr("On these months")));
        panel.setLayout(new GridLayout(3, 4));
        String[] monthStrings = new DateFormatSymbols(Util.get().getCurrentLocale()).getMonths();
        monthCheckBox = new JCheckBox[12];
        
        for (int i = 0; i < monthCheckBox.length; i++) {
            monthCheckBox[i] = new JCheckBox(monthStrings[i]);
            monthCheckBox[i].setSelected(schedule != null ? schedule.getMonthsToPlay()[i] : true);
            panel.add(monthCheckBox[i]);
        }
        
        return panel;
    }
    
    private JTextArea createNoteTextArea() {
        JTextArea note = new JTextArea(Util.get().wordWrap(i18n.tr("Note, that the spot is only played if ALL " +
        		"conditions above are met. If, for instance, only 'Wednesday' and '20' (plus all months) are " +
        		"selected, the spot is only played if today is BOTH Wednesday AND the 20th of the month."), 70));
        note.setEditable(false);
        
        Color textColour = new Color(0, 0, 0); // black text
        Color bgColour = new Color(255, 255, 255, 0); // "invisible" white, i.e. alpha value of 0
        note.setBackground(bgColour); 
        note.setForeground(textColour);
        note.setSelectionColor(bgColour);
        note.setSelectedTextColor(textColour);
        return note;
    }
    
    private JPanel createOkCancelPanel() {
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
    
    public PlaySchedule getSchedule() {
        // Guaranteed to return a valid PlaySchedule, if isOkClicked() returs true.
        // Might return null otherwise.
        return schedule;
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    
    public void actionPerformed(ActionEvent ae) {
        Util.get().out("Schedule Properties window action performed! " + ae.getActionCommand(), Util.VERBOSITY_DEBUG_INFO);
        String action = ae.getActionCommand();
        if (action.equals("ok")) {
            boolean makeNewSchedule = (schedule == null);
            if (makeNewSchedule)
                schedule = new PlaySchedule();
            
            schedule.setTimeToPlay((Integer)hourSpinner.getValue(), (Integer)minuteSpinner.getValue());
            
            boolean[] weekdays = new boolean[weekdayCheckBox.length];
            for (int i = 0; i < weekdays.length; i++)
                weekdays[i] = weekdayCheckBox[i].isSelected();
            schedule.setWeekdaysToPlay(weekdays);
            
            boolean[] dayOfMonth = new boolean[dayOfMonthCheckBox.length];
            for (int i = 0; i < dayOfMonth.length; i++)
                dayOfMonth[i] = dayOfMonthCheckBox[i].isSelected();
            schedule.setDaysToPlay(dayOfMonth);
            
            boolean[] months = new boolean[monthCheckBox.length];
            for (int i = 0; i < months.length; i++)
                months[i] = monthCheckBox[i].isSelected();
            schedule.setMonthsToPlay(months);
            
            okClicked = true;
            this.dispose();
        } else if (action.equals("cancel")) {
            this.dispose();
        }
    }
}
