/*
    Copyright 2023 Dmitry Isaenko

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

import tihwin.AwesomeMediator;
import tihwin.UpdateUlTableUi;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class FilesDropListener extends DropTargetAdapter {
    public FilesDropListener(JPanel panel){
        new DropTarget(panel, DnDConstants.ACTION_COPY, this, true, null);
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        if (! event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            event.rejectDrop();
            return;
        }

        event.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable transferable = event.getTransferable();
        try {
            List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

            for (File file : files) {
                if (file.isDirectory())
                    continue;
                // Pick up first ISO file found and drop iteration
                if (file.getName().toLowerCase().endsWith(".iso")) {
                    AwesomeMediator.setDiskImage(file);
                    break;
                }
                // If no ISO maybe there are ul.cfg than
                if (file.isFile() && file.getName().equalsIgnoreCase("ul.cfg")){
                    new UpdateUlTableUi(file.getParentFile().getAbsolutePath());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.rejectDrop();
        }
        event.dropComplete(true);
    }
}
