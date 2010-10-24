package gui;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

public class SpotList extends JList {
	private JScrollPane containingScrollPane;
	
	public SpotList(Object[] data) {
		super(data);
		initialize();
	}
	
	public SpotList(Vector<?> data) {
		super(data);
		initialize();
	}
	
	private void initialize() {
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setSelectedIndex(0);
		setVisibleRowCount(5);
		//setPreferredSize(new Dimension(200, 0));
		
		containingScrollPane = new JScrollPane(this);
		containingScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	}
	
	public JScrollPane getContainingScrollPane() {
		return containingScrollPane;
	}
}
