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

import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.util.Enumeration;

public class UlTableColumnModel extends DefaultTableColumnModel {
    public UlTableColumnModel(DefaultTableColumnModel parentModel){
        Enumeration<TableColumn> columns = parentModel.getColumns();
        while (columns.hasMoreElements()){
            addColumn(columns.nextElement());
        }

        getColumn(0).setMinWidth(20);
        getColumn(0).setPreferredWidth(25);
        getColumn(0).setMaxWidth(50);

        getColumn(1).setMinWidth(200);
        getColumn(1).setPreferredWidth(225);

        getColumn(2).setMinWidth(100);
        getColumn(2).setPreferredWidth(135);
        getColumn(2).setMaxWidth(150);

        getColumn(3).setMinWidth(75);
        getColumn(3).setPreferredWidth(100);
        getColumn(3).setMaxWidth(125);

        getColumn(4).setMinWidth(50);
        getColumn(4).setPreferredWidth(75);
        getColumn(4).setMaxWidth(100);

        getColumn(4).setCellEditor(new UlCdDvdCellEditor());

        getColumn(5).setCellEditor(new UlButtonCellEditor());

        setColumnSelectionAllowed(false);

    }

    @Override
    public void addColumn(TableColumn aColumn) {
        super.addColumn(aColumn);
    }

    @Override
    public void removeColumn(TableColumn column) {
        super.removeColumn(column);
    }

    @Override
    public void moveColumn(int columnIndex, int newIndex) {}

    @Override
    protected void fireColumnAdded(TableColumnModelEvent e) {
        super.fireColumnAdded(e);
    }

    @Override
    protected void fireColumnRemoved(TableColumnModelEvent e) {
        super.fireColumnRemoved(e);
    }

    @Override
    protected void fireColumnMoved(TableColumnModelEvent e) {
        super.fireColumnMoved(e);
    }

    @Override
    protected void fireColumnMarginChanged() {
        super.fireColumnMarginChanged();
    }
}
