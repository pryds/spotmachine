package gui;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import main.SpotContainer;
import main.SpotMachine;
import main.Util;

public class SpotList extends JTable {
	private static final long serialVersionUID = 684901424002754701L;
	
	private JScrollPane containingScrollPane;
	
	protected String[] columnToolTips;

	
	public SpotList(TableModel tm) {
		super(tm);
		initialize();
	}
	
	private void initialize() {
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().setSelectionInterval(0, 0);
		//setPreferredSize(new Dimension(200, 0));
		
		containingScrollPane = new JScrollPane(this);
		this.setFillsViewportHeight(true);
		getTableHeader().setReorderingAllowed(false);
		containingScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		if (getModel().getType() == SpotContainer.TYPE_ACTIVE) {
			getColumnModel().getColumn(0).setMaxWidth(30); // spot number
			getColumnModel().getColumn(0).setResizable(false);

			getColumnModel().getColumn(2).setMaxWidth(75); // length in min:sec
			getColumnModel().getColumn(2).setResizable(false);

			getColumnModel().getColumn(3).setMaxWidth(15); // Next spot * marking
			getColumnModel().getColumn(3).setResizable(false);
			
			columnToolTips = new String[] {
					Util.get().string("main-list-itemnumber-tooltip"),
				    null, // "Name" assumed obvious
				    Util.get().string("main-list-duration-tooltip"),
				    Util.get().string("main-list-nextspot-tooltip")
			};
			
		} else {
			getColumnModel().getColumn(1).setMaxWidth(75); // length in min:sec
			getColumnModel().getColumn(1).setResizable(false);

			columnToolTips = new String[] {
				    null, // "Name" assumed obvious
				    Util.get().string("main-list-duration-tooltip")
			};
		}
	}
	
public JScrollPane getContainingScrollPane() {
		return containingScrollPane;
	}
	
	public void setNextSpot(int index) {
		int numRows = this.getModel().getRowCount();
		if (getModel().getType() == SpotContainer.TYPE_ACTIVE) {
			for (int i = 0; i < numRows; i++) {
				this.getModel().setValueAt(
						(i == index ? "*" : ""), //value
						i, 3); // row, col
			}
		} else {
			// do nothing; we ought not end up here...
			Util.get().out("Tried to set next spot marking of a list of available spots. This should not happen. Ignored.", Util.VERBOSITY_WARNING);
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
			Util.get().out("Trying to insert data with more or less columns than destination into JTable. This should not happen. Ignoring.", Util.VERBOSITY_WARNING);
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
	
	public void remove(int row) {
		if (getModel().getType() == SpotContainer.TYPE_ACTIVE) {
			int newNextSpot = SpotMachine.getSpotPlayer().getNextSpotToPlayIndex();
			boolean rowHasStar = getModel().getValueAt(row, 3).equals("*");
			getModel().remove(row);
			if (rowHasStar) {
				newNextSpot = SpotMachine.getSpotPlayer().getNextSpotToPlayIndex();
				Util.get().out("GUI: Removing spot that has been set as " +
						"next spot. Setting next spot to " + newNextSpot, Util.VERBOSITY_DEBUG_INFO);
				setNextSpot(newNextSpot);
			}
			SpotMachine.getMainFrame().setNextSpotLabel(newNextSpot, SpotMachine.getSpotPlayer().getSpotAt(newNextSpot));
		} else {
			getModel().remove(row);
		}
	}
	
	public void removeAll(int[] rows) {
		Util.get().reverseSort(rows);
		for (int i = 0; i < rows.length; i++) {
			remove(rows[i]);
		}
	}
	
	public SpotListModel getModel() {
		return (SpotListModel)super.getModel();
	}
	
	protected JTableHeader createDefaultTableHeader() { // override from JTable class to show tool tips at table headers
        return new JTableHeader(columnModel) {
			private static final long serialVersionUID = 6795390313698951407L;

			public String getToolTipText(java.awt.event.MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = columnModel.getColumn(index).getModelIndex();
                return columnToolTips[realIndex];
            }
        };
    }
}
