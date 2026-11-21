/*
    Copyright 2022-2026 Dmitry Isaenko

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
package tihwin;

import tihwin.cd.ISO9660;
import tihwin.ui.UiUpdater;
import tihwin.ul.UlConfiguration;
import tihwin.ul.UlMaker;

import javax.swing.*;
import java.io.File;

public class ConversionController {

    private static final long MAX_ISO_LENGTH = 0x40000000;
    private static final int MAX_TITLE_LENGTH = 31;
    private static final String DEFAULT_TITLE = "My favorite game";

    private final ConversionPort port;

    private File diskImage;
    private String publisherTitle;
    private volatile boolean isConvertingNow = false;
    private Thread splitThread;

    public ConversionController(ConversionPort port) {
        this.port = port;
    }

    public boolean isConvertingNow() {
        return isConvertingNow;
    }

    public void setDiskImage(File imageFile) {
        try {
            ISO9660 iso9660 = new ISO9660(imageFile);
            publisherTitle = iso9660.getTitle();

            diskImage = imageFile;
            port.setDiskImageName(imageFile.getName());
            port.setConvertButtonEnabled(true);
            port.setStatusText(imageFile.getAbsolutePath());
            port.setTitle(proposeTitle(imageFile.getName()));
        }
        catch (Exception e) {
            diskImage = null;
            publisherTitle = null;
            port.setConvertButtonEnabled(false);
            port.setStatusText(e.getMessage());
            e.printStackTrace();
        }
    }

    public void onConvertClicked() {
        try {
            if (isConvertingNow) {
                abort();
                return;
            }
            if (port.titleFieldText().isEmpty()) {
                port.setTitle(proposeTitle(diskImage.getName()));
                return;
            }
            start();
        }
        catch (Exception e) {
            port.setStatusText(e.getMessage());
        }
    }

    public void notifyFinished() {
        isConvertingNow = false;
        SwingUtilities.invokeLater(() -> {
            port.setConvertButtonEnabled(true);
            port.setConvertButtonText("ConvertBtn");
        });
    }

    private void start() throws Exception {
        UlMaker ulMaker = new UlMaker(
                diskImage,
                port.destinationDir(),
                new UlConfiguration(
                        port.titleFieldText(),
                        publisherTitle,
                        (byte) chunksCount(diskImage.length()),
                        port.isDvdSelected()),
                new UiUpdater(port.progressBar(), port.statusLabel(), this::notifyFinished));

        port.setLocalizedStatus("InProgressText");
        splitThread = new Thread(ulMaker);
        splitThread.start();
        isConvertingNow = true;
        port.setConvertButtonText("AbortText");
    }

    private void abort() {
        splitThread.interrupt();
        port.setConvertButtonEnabled(false);
    }

    private static int chunksCount(long length) {
        int chunks = (int) (length / MAX_ISO_LENGTH);
        if (length % MAX_ISO_LENGTH > 0)
            chunks++;
        return chunks;
    }

    static String proposeTitle(String fileName) {
        String proposedName = fileName.replaceAll("(\\..*)|(\\[.*)", "").trim();
        if (proposedName.length() > MAX_TITLE_LENGTH)
            proposedName = proposedName.substring(0, MAX_TITLE_LENGTH);
        else if (proposedName.isEmpty())
            proposedName = DEFAULT_TITLE;
        return proposedName;
    }
}
