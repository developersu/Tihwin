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
package tihwin.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class TitleFieldFilter extends DocumentFilter {
    private final static int MAX_USER_DEFINED_GAME_TITLE_LENGTH = 31;
    public TitleFieldFilter() {}

    protected boolean isNotAscii(String str){
        if (str == null)
            return false;

        return ! str.matches("\\A\\p{ASCII}*\\z");
    }

    @Override
    public void insertString(FilterBypass fb, int offs, String str, AttributeSet attr) throws BadLocationException {
        if (isNotAscii(str))
            return;
        int documentLength = fb.getDocument().getLength();
        if ((documentLength + str.length()) <= MAX_USER_DEFINED_GAME_TITLE_LENGTH)
            super.insertString(fb, offs, str, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet attr) throws BadLocationException {
        if (isNotAscii(str))
            return;
        int documentLength = fb.getDocument().getLength();
        if ((documentLength + str.length() - length) <= MAX_USER_DEFINED_GAME_TITLE_LENGTH)
            super.replace(fb, offs, length, str, attr);
    }
}