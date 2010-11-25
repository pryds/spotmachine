package gui;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

public class SpotList extends JTable {
	private JScrollPane containingScrollPane;
	
	public SpotList(Object[][] data, String[] columnNames) {
		super(data, columnNames);
		initialize();
	}
	
	/**public SpotList(Vector<?> data, Vector<?> columnNames) {
		super(data, columnNames);
		initialize();
	}**/
	
	private void initialize() {
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//setSelectedIndex(0);
		//setPreferredSize(new Dimension(200, 0));
		
		containingScrollPane = new JScrollPane(this);
		this.setFillsViewportHeight(true);
		containingScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		getColumnModel().getColumn(0).setMaxWidth(30); // spot number
		getColumnModel().getColumn(2).setMaxWidth(75); // spot number
	}
	
	public JScrollPane getContainingScrollPane() {
		return containingScrollPane;
	}
}
