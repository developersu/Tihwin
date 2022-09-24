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

public class UlCdDvdCellEditor extends DefaultCellEditor {
    static {
        CD_DVD = new String[]{"CD", "DVD"};
    }
    private static final String[] CD_DVD;

    public UlCdDvdCellEditor(){
        super(new JComboBox<>(CD_DVD));
    }
}
