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

import java.io.File;
import java.io.FileFilter;
import java.util.ResourceBundle;

public class IsoFileFilter extends javax.swing.filechooser.FileFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        if (file.isDirectory())
            return true;
        String extension = file.getName().toLowerCase().replaceAll("^.+\\.", "");
        return extension.equals("iso");
    }

    @Override
    public String getDescription() {
        return ResourceBundle.getBundle("locale").getString("isoFilesText");
    }
}
