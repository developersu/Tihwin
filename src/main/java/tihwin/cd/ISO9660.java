/*
    Copyright 2022 Dmitry Isaenko
     
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
package tihwin.cd;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ISO9660 {
    private final RandomAccessFile randomAccessFile;
    private final ResourceBundle resourceBundle;
    private DirectoryEntry rootEntry;
    private String title;

    public ISO9660(File iso) throws Exception {
        this.randomAccessFile = new RandomAccessFile(iso, "r");
        this.resourceBundle = ResourceBundle.getBundle("locale");
        skipFirst16Sectors();
        getRootDirectoryDescriptor();
        getSystemCnfContent();
    }
    private void skipFirst16Sectors() throws Exception{
        randomAccessFile.seek(2048*16);
    }
    private void getRootDirectoryDescriptor() throws Exception{
        byte[] firstPayloadDescriptor = new byte[2048];
        if (2048 != randomAccessFile.read(firstPayloadDescriptor))
            throw new Exception(resourceBundle.getString("ISO_CantReadISOInitialDescriptorText"));

        byte type = firstPayloadDescriptor[0];
        String identifier = new String(firstPayloadDescriptor, 0x1, 0x5, StandardCharsets.US_ASCII);
        byte version = firstPayloadDescriptor[0x6];
        // Let's perform some basic validations
        if (type != 1)
            throw new Exception(resourceBundle.getString("ISO_NotSupportedCdDvd")+" Type "+ type);
        if (! identifier.contentEquals("CD001"))
            throw new Exception(resourceBundle.getString("ISO_NotSupportedCdDvd")+" Identifier "+ identifier);
        if (version != 1)
            throw new Exception(resourceBundle.getString("ISO_NotSupportedCdDvd")+" Version "+ version);

        this.rootEntry = new DirectoryEntry(Arrays.copyOfRange(firstPayloadDescriptor, 0x9c, 0xbe));
    }

    private void getSystemCnfContent() throws Exception{
        randomAccessFile.seek(rootEntry.getExtentLocation() * 2048L);
        byte[] bytes = new byte[rootEntry.getDataSize()];
        if (rootEntry.getDataSize() != randomAccessFile.read(bytes))
            throw new Exception(resourceBundle.getString("ISO_CantReadRootDescriptor"));

        int entryOffset = 0;
        while (entryOffset < rootEntry.getDataSize()){
            int entryLength = Byte.toUnsignedInt(bytes[entryOffset]);

            if (entryLength == 0)
                break;

            byte[] entryBytes = Arrays.copyOfRange(bytes, entryOffset, entryOffset+entryLength);
            DirectoryEntry entryIn = new DirectoryEntry(entryBytes);
            entryOffset += entryLength;

            if (entryIn.getIdentifier().toUpperCase().contains("SYSTEM.CNF")){
                randomAccessFile.seek(entryIn.getExtentLocation() * 2048L);
                byte[] configurationData = new byte[entryIn.getDataSize()];
                randomAccessFile.read(configurationData);

                String systemCnf = new String(configurationData, StandardCharsets.UTF_8);
                getTitleFromSystemCnf(systemCnf);
                return;
            }
        }
        throw new Exception(resourceBundle.getString("ISO_NoSystemCnf"));
    }

    private void getTitleFromSystemCnf(String systemCnf) throws Exception{
        String systemCnfOneLine = systemCnf.replace("\n", "").replace("\r", "");
        title = systemCnfOneLine.replaceAll("(.*cdrom0:\\\\)|(;.*)","");
        if (title.length() == 0)
            throw new Exception(resourceBundle.getString("ISO_PublisherTitleNotFound")+" "+systemCnf);
    }

    public String getTitle() { return title; }
}
