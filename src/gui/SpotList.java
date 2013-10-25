package gui;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.xnap.commons.i18n.I18n;

import main.SpotContainer;
import main.SpotMachine;
import main.Util;

public class SpotList extends JTable {
	private static final long serialVersionUID = 684901424002754701L;
	
	private JScrollPane containingScrollPane;
	
	protected String[] columnToolTips;
	
	private I18n i18n;
	
	
	public SpotList(TableModel tm) {
		super(tm);
		initialize();
	}
	
	private void initialize() {
		i18n = Util.get().i18n();
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//setPreferredSize(new Dimension(200, 0));
		
		containingScrollPane = new JScrollPane(this);
		this.setFillsViewportHeight(true);
		getTableHeader().setReorderingAllowed(false); // no user reordering of columns
		containingScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		containingScrollPane.setMinimumSize(new Dimension(200, 200));
        //containingScrollPane.setMaximumSize(new Dimension(400, 600));
		int preferredHeight = (getModel().getType() == SpotContainer.TYPE_AVAILABLE) ? 400 : 200;
		containingScrollPane.setPreferredSize(new Dimension(400, preferredHeight));
        
        if (getModel().getType() == SpotContainer.TYPE_INTERVALLED) {
			getColumnModel().getColumn(0).setMaxWidth(30); // spot number
			getColumnModel().getColumn(0).setResizable(false);

			getColumnModel().getColumn(2).setMaxWidth(75); // length in min:sec
			getColumnModel().getColumn(2).setResizable(false);

			getColumnModel().getColumn(3).setMaxWidth(15); // Next spot * marking
			getColumnModel().getColumn(3).setResizable(false);
			
			columnToolTips = new String[] {
					i18n.tr("Play item number"),
				    null, // "Name" assumed obvious
				    i18n.tr("Duration in minutes and seconds"),
				    i18n.tr("* means that this spot is the next to get played")
			};
		} else if (getModel().getType() == SpotContainer.TYPE_SCHEDULED) {
			getColumnModel().getColumn(1).setMaxWidth(75); // length in min:sec
			getColumnModel().getColumn(1).setResizable(false);
			
			getColumnModel().getColumn(2).setMaxWidth(75); // playtime
            getColumnModel().getColumn(2).setResizable(false);
            
			columnToolTips = new String[] {
					null, // "Name" assumed obvious
					i18n.tr("Duration in minutes and seconds"),
					i18n.tr("Played at (a ¤ means only on certain days)")
			};
		} else {
			getColumnModel().getColumn(1).setMaxWidth(75); // length in min:sec
			getColumnModel().getColumn(1).setResizable(false);

			columnToolTips = new String[] {
				    null, // "Name" assumed obvious
				    i18n.tr("Duration in minutes and seconds")
			};
		}
	}
	
	public JScrollPane getContainingScrollPane() {
		return containingScrollPane;
	}
	
	public void setNextSpot(int index) {
		int numRows = this.getModel().getRowCount();
		if (getModel().getType() == SpotContainer.TYPE_INTERVALLED) {
			for (int i = 0; i < numRows; i++) {
				this.getModel().setValueAt(
						(i == index ? "*" : ""), //value
						i, 3); // row, col
			}
		} else {
			// do nothing; we ought not end up here...
			Util.get().out("Tried to set next spot marking of a list which is not of type INTERVALLED. This should not happen. Ignored.", Util.VERBOSITY_WARNING);
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
		if (getModel().getType() == SpotContainer.TYPE_INTERVALLED) {
			int newNextSpot = SpotMachine.getIntervalledSpotPlayer().getNextSpotToPlayIndex();
			boolean rowHasStar = getModel().getValueAt(row, 3).equals("*");
			getModel().remove(row);
			if (rowHasStar) {
				newNextSpot = SpotMachine.getIntervalledSpotPlayer().getNextSpotToPlayIndex();
				Util.get().out("GUI: Removing spot that has been set as " +
						"next spot. Setting next spot to " + newNextSpot, Util.VERBOSITY_DEBUG_INFO);
				setNextSpot(newNextSpot);
			}
			SpotMachine.getMainFrame().setNextSpotLabel(newNextSpot, SpotMachine.getIntervalledSpotPlayer().getSpotAt(newNextSpot));
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
