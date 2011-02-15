package gui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import main.PlaySchedule;
import main.Util;
import main.SpotContainer;
import main.SpotEntry;

public class SpotListModel extends AbstractTableModel {
	private static final long serialVersionUID = -3418532435840096221L;
	
	private String[] columnNames;
	private Vector<Object[]> data = new Vector<Object[]>();
	private int type; // j.f. SpotContainer.TYPE_*
	
	public SpotListModel(int type) {
		this.type = type;
		if (type == SpotContainer.TYPE_INTERVALLED) {
			columnNames = new String[] {
					Util.get().string("main-list-itemnumber-label"),
					Util.get().string("main-list-name-label"),
					Util.get().string("main-list-duration-label"),
					"*"
			};
		} else if (type == SpotContainer.TYPE_SCHEDULED) {
		    columnNames = new String[] {
		            Util.get().string("main-list-name-label"),
		            Util.get().string("main-list-duration-label"),
		            Util.get().string("main-list-playat-label")
		    };
		} else {
			columnNames = new String[] {
					Util.get().string("main-list-name-label"),
					Util.get().string("main-list-duration-label")
			};
		}
	}
	
	public void replaceData(Vector<SpotEntry> replacement) {
		data = new Vector<Object[]>();
		
		for (int i = 0; i < replacement.size(); i++) {
		    addToEnd(replacement.get(i));
		}
	}
	
	public void addToEnd(SpotEntry spot) {
		if (type == SpotContainer.TYPE_INTERVALLED) {
			data.add(new Object[] {
					data.size() + 1,
					spot.getName(),
					Util.get().millisToMinsSecsString(spot.getLengthInMillis()),
					""
			});
        } else if (type == SpotContainer.TYPE_SCHEDULED) {
            data.add(new Object[] {
                    spot.getName(),
                    Util.get().millisToMinsSecsString(spot.getLengthInMillis()),
                    spot.getSchedule().getTimeFormattedShortStringWithNotAllDaysIndication()
            });
		} else {
			data.add(new Object[] {
					spot.getName(),
					Util.get().millisToMinsSecsString(spot.getLengthInMillis())
			});
		}
		fireTableRowsInserted(data.size() - 1, data.size() - 1);
	}
	
	public void rename(int row, String newName) {
	    int columnNumber = (type == SpotContainer.TYPE_AVAILABLE ||
	            type == SpotContainer.TYPE_SCHEDULED)
	            ? 0 : 1;
		setValueAt(newName, row, columnNumber);
	}
	
	public void setPlayAt(int row, PlaySchedule schedule) {
	    if (type == SpotContainer.TYPE_SCHEDULED) {
	        setValueAt(schedule.getTimeFormattedShortStringWithNotAllDaysIndication(), row, 2);
	    } else {
	        // do nothing; we ought not end up here...
            Util.get().out("Tried to set 'playAt' for a list which is not of type SCHEDULED. This should not happen. Ignored.", Util.VERBOSITY_WARNING);
        }
	}

	
	public void remove(int row) {
		data.remove(row);
		updateSpotNumbersFrom(row);
		fireTableDataChanged();
	}
	
	private void updateSpotNumbersFrom(int index) {
		if (type == SpotContainer.TYPE_INTERVALLED) {
			for (int i = index; i < getRowCount(); i++) {
				setValueAt(i + 1, i, 0);
			}
		}
	}
	
	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.size();
	}
	
	public Object getValueAt(int row, int col) {
		return data.get(row)[col];
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Object[] row = data.get(rowIndex);
		row[columnIndex] = aValue;
		data.set(rowIndex, row);
		fireTableCellUpdated(rowIndex, columnIndex);
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	public int getType() {
		return type;
	}
}
