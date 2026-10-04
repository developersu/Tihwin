/*
    Copyright 2019-2026 Dmitry Isaenko
     
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

import javax.swing.*;
import java.io.File;

import static tihwin.FilesHelper.getRealFolder;

public class TwFileChooser extends JFileChooser {

    private static final IsoFileFilter ISO_FILE_FILTER = new IsoFileFilter();

    public TwFileChooser(String currentDirectoryPath, String dialogTitle, boolean isIsoFileFilter) {
        super(getRealFolder(currentDirectoryPath));
        setDialogTitle(dialogTitle);
        if (isIsoFileFilter)
            setFileFilter(ISO_FILE_FILTER);
        else
            setFileSelectionMode(DIRECTORIES_ONLY);
    }

    public File getFile() {
        if (showOpenDialog(null) == APPROVE_OPTION)
            return getSelectedFile();
        return null;
    }
}
