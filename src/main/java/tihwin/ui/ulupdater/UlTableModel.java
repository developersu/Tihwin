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
import java.util.ResourceBundle;

public class UlTableModel extends DefaultTableModel {
    static {
        ResourceBundle bundle = ResourceBundle.getBundle("locale");
        columns = new String[]{
                bundle.getString("ulManagerWindow_ColumnNameNumber"),
                bundle.getString("ulManagerWindow_ColumnNameTitle"),
                bundle.getString("ulManagerWindow_ColumnNamePublisherTitle"),
                bundle.getString("ulManagerWindow_ColumnNameChunksCount"),
                bundle.getString("ulManagerWindow_ColumnCdDvdFlag"),
                ""};
    }
    private static final String[] columns;

    private int rowsCount;

    public UlTableModel(){
        super();
        super.setColumnIdentifiers(columns);
    }

    public void addRow(UlTableRow row){
        super.addRow(row.getRow());
        this.rowsCount++;
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
        super.removeRow(row);
        --rowsCount;
        for (int i = row; i < rowsCount; i++)
            setValueAt(i+1, i, 0);
    }
}
