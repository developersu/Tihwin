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
import java.util.ResourceBundle;

public class UlTableRow {
    static {
        ResourceBundle bundle = ResourceBundle.getBundle("locale");
        REMOVE_ME_BUTTON_TEXT = bundle.getString("ulManagerWindow_Row_RemoveRowBtn");
        CD_TEXT = bundle.getString("CD");
        DVD_TEXT = bundle.getString("DVD");
    }
    private static final String REMOVE_ME_BUTTON_TEXT;
    private static final String CD_TEXT;
    private static final String DVD_TEXT;

    private final int rowNumber;
    private final String name;
    private final String publisherTitle;
    private final int chunksCount;
    private final String cdDvdFlag;
    private final JButton removeRowButton;

    public UlTableRow(int rowNumber, String name, String publisherTitle, int chunksCount, boolean isDvd){
        this.rowNumber = rowNumber;
        this.name = name;
        this.publisherTitle = publisherTitle;
        this.chunksCount = chunksCount;
        this.cdDvdFlag = isDvd? DVD_TEXT : CD_TEXT;
        this.removeRowButton = new JButton(REMOVE_ME_BUTTON_TEXT);
    }

    public Object[] getRow(){
        return new Object[]{
                rowNumber,
                name,
                publisherTitle,
                chunksCount,
                cdDvdFlag,
                removeRowButton
        };
    }
}
