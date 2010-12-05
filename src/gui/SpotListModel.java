package gui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import main.Calculate;
import main.SpotContainer;
import main.SpotEntry;

public class SpotListModel extends AbstractTableModel {
	private static final long serialVersionUID = -3418532435840096221L;
	
	private String[] columnNames;
	private Vector<Object[]> data = new Vector<Object[]>();
	private int type; // j.f. SpotContainer.TYPE_*
	
	public SpotListModel(int type) {
		this.type = type;
		if (type == SpotContainer.TYPE_ACTIVE) {
			columnNames = new String[] {"Nr." , "Navn" , "Varighed", "*"};
		} else {
			columnNames = new String[] {"Navn" , "Varighed"};
		}
	}
	
	public void replaceData(Vector<SpotEntry> replacement) {
		data = new Vector<Object[]>();
		if (type == SpotContainer.TYPE_ACTIVE) {
			for (int i = 0; i < replacement.size(); i++) {
				data.add(new Object[] {
						i,
						replacement.get(i).getName(),
						Calculate.millisToMinsSecsString(replacement.get(i).getLengthInMillis()),
						""
				});
			}
		} else {
			for (int i = 0; i < replacement.size(); i++) {
				data.add(new Object[] {
						replacement.get(i).getName(),
						Calculate.millisToMinsSecsString(replacement.get(i).getLengthInMillis())
				});
			}
		}
	}
	
	public void addToEnd(SpotEntry spot) {
		if (type == SpotContainer.TYPE_ACTIVE) {
			data.add(new Object[] {
					data.size(),
					spot.getName(),
					Calculate.millisToMinsSecsString(spot.getLengthInMillis()),
					""
			});
		} else {
			data.add(new Object[] {
					spot.getName(),
					Calculate.millisToMinsSecsString(spot.getLengthInMillis())
			});
		}
		fireTableRowsInserted(data.size() - 1, data.size() - 1);
	}
	
	public void rename(int row, String newName) {
		setValueAt(newName, row, (type == SpotContainer.TYPE_ACTIVE ? 1 : 0) );
	}
	
	public void remove(int row) {
		data.remove(row);
		updateSpotNumbersFrom(row);
		// TODO: if active spots, update spot numbers
		fireTableDataChanged();
	}
	
	private void updateSpotNumbersFrom(int index) {
		if (type == SpotContainer.TYPE_ACTIVE) {
			for (int i = index; i < getRowCount(); i++) {
				setValueAt(i, i, 0);
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
