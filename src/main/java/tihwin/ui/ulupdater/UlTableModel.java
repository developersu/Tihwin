/*

     Copyright "2022" Dmitry Isaenko

     This file is part of Tihwin.

     Tihwin is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     Tihwin is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with Tihwin.  If not, see <https://www.gnu.org/licenses/>.

 */
package tihwin.ui.ulupdater;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UlTableModel extends DefaultTableModel {

    private final String REMOVE_ME_BUTTON_TEXT;
    
    private final String OK_STATUS;
    private final String INCONSISTENT_STATUS;

    private final List<UlTableModelRecord> rows;
    private final List<UlTableModelRecord> removedRows;

    public UlTableModel(){
        super();
        ResourceBundle bundle = ResourceBundle.getBundle("locale");
        String[] columns = new String[]{
                bundle.getString("ulManagerWindow_ColumnNameNumber"),
                bundle.getString("ulManagerWindow_ColumnNameTitle"),
                bundle.getString("ulManagerWindow_ColumnNamePublisherTitle"),
                bundle.getString("ulManagerWindow_ColumnNameChunksCount"),
                bundle.getString("ulManagerWindow_ColumnCdDvdFlag"),
                "",
                ""};
        super.setColumnIdentifiers(columns);
        REMOVE_ME_BUTTON_TEXT = bundle.getString("ulManagerWindow_Row_RemoveRowBtn");
        OK_STATUS = bundle.getString("Ok");
        INCONSISTENT_STATUS = bundle.getString("ulManagerWindow_InconsistentFileText");

        this.rows = new ArrayList<>();
        this.removedRows = new ArrayList<>();
    }

    public void addRow(UlTableModelRecord record){
        rows.add(record);
        super.addRow(new Object[]{
                rows.size(),
                record.getConfiguration().getTitle(),
                record.getConfiguration().getPublisherTitle(),
                record.getConfiguration().getChunksCount(),
                record.getConfiguration().isDvd()? "DVD" : "CD",
                new JButton(REMOVE_ME_BUTTON_TEXT),
                record.isConsistent()?OK_STATUS:INCONSISTENT_STATUS
                });
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 5)
            return JButton.class;
        return JLabel.class;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        switch (column){
            case 1:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void removeRow(int row) {
        removedRows.add(rows.get(row));
        rows.remove(row);
        super.removeRow(row);
        for (int i = row; i < rows.size(); i++)
            setValueAt(i+1, i, 0);
    }

    public void clear(){
        for (int i = getRowCount()-1; i >= 0 ; i--)
            super.removeRow(i);
        rows.clear();
        removedRows.clear();
    }

    public List<UlTableModelRecord> getInitialRows(){
        return rows;
    }

    public List<UlTableModelRecord> getRemovedRows() {
        return removedRows;
    }

    public String getTitle(int row){
        return (String) getValueAt(row, 1);
    }
    public String getPublisherTitle(int row){
        return (String) getValueAt(row, 2);
    }
    public byte getChunksCount(int row){
        return (byte) getValueAt(row, 3);
    }
    public String getCdDvd(int row){
        return (String) getValueAt(row, 4);
    }
}
