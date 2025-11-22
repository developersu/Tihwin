/*

     Copyright "2022-2025" Dmitry Isaenko

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

import tihwin.ScaleUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class UlTableContentJLabelRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                          boolean isSelected, boolean hasFocus,
                                          int row, int column){
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setBackground(Color.getHSBColor(0, 0, 0.9411765f));
        label.setBorder(new EmptyBorder(0, 3, 0, 3));
        ScaleUi.applyInitialScale(label);
        switch (column){
            case 0:
            case 3:
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                break;
            case 2:
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setOpaque(true);
                break;
            default:
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setOpaque(false);
        }
        return label;
    }
}
