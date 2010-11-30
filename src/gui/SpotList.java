package gui;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import main.SpotContainer;

public class SpotList extends JTable {
	private static final long serialVersionUID = 684901424002754701L;

	private JScrollPane containingScrollPane;
	
	private int type; // j.f. SpotContainer.TYPE_*
	
	public SpotList(Object[][] data, String[] columnNames, int type) {
		super(data, columnNames);
		this.type = type;
		initialize();
	}
	
	private void initialize() {
		//setModel(new SpotListTableModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().setSelectionInterval(0, 0);
		//setPreferredSize(new Dimension(200, 0));
		
		containingScrollPane = new JScrollPane(this);
		this.setFillsViewportHeight(true);
		containingScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		if (type == SpotContainer.TYPE_ACTIVE) {
			getColumnModel().getColumn(0).setMaxWidth(30); // spot number
			getColumnModel().getColumn(0).setResizable(false);

			getColumnModel().getColumn(2).setMaxWidth(75); // length in min:sec
			getColumnModel().getColumn(2).setResizable(false);

			getColumnModel().getColumn(3).setMaxWidth(15); // Next spot * marking
			getColumnModel().getColumn(3).setResizable(false);
			
		} else {
			getColumnModel().getColumn(1).setMaxWidth(75); // length in min:sec
			getColumnModel().getColumn(1).setResizable(false);
		}
	}
	
	public JScrollPane getContainingScrollPane() {
		return containingScrollPane;
	}
	
	public void setNextSpot(int index) {
		int numRows = this.getModel().getRowCount();
		if (type == SpotContainer.TYPE_ACTIVE) {
			for (int i = 0; i < numRows; i++) {
				this.getModel().setValueAt(
						(i == index ? "*" : ""), //value
						i, 3); // row, col
			}
		} else {
			// do nothing; we ought not end up here...
			System.err.println("Tried to set next spot marking of a list of available spots. This should not happen. Ignored.");
		}
	}
	
	public Object[] getRow(int index) { // except first column
		Object[] data = new Object[this.getColumnCount() - 1];
		for (int i = 1; i < data.length; i++) {
			data[i] = this.getValueAt(index, i);
		}
		return data;
	}
	
	public void setRow(int index, Object[] data) { // except first column
		if (data.length != this.getColumnCount() - 1) {
			System.err.println("Trying to insert data with more or less columns than destination into JTable. This should not happen. Ignoring.");
			return;
		}
		for (int i = 1; i < data.length; i++) {
			this.setValueAt(data[i], index, i);
		}
	}
	
	public void swapRows(int index1, int index2) { // except first column
		Object[] temp = getRow(index1);
		setRow(index1, getRow(index2));
		setRow(index2, temp);
	}
}
