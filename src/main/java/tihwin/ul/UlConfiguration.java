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
package tihwin.ul;

import java.io.BufferedInputStream;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class UlConfiguration {
    private static final byte DVD_FLAG = 0x14;
    private static final byte CD_FLAG = 0x12;

    private final String title;
    private final String publisherTitle;
    private final String crc32;
    private final byte chunksCount;
    private final byte cdDvdFlag;
    private final boolean romIsDvdImage;

    public UlConfiguration(File ulCfg, int recordNumber) throws Exception{
        try(BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(ulCfg.toPath()))){
            int offset = recordNumber * 0x40;
            int read = 0;
            while (offset != read)
                read += stream.skip(offset);
            byte[] buffer = new byte[0x40];
            if (0x40 != stream.read(buffer))
                throw new Exception(recordNumber+" 0x40");
            this.title = new String(buffer, 0, 0x20, StandardCharsets.US_ASCII).trim();
            this.publisherTitle = new String(buffer, 0x23, 0xB, StandardCharsets.US_ASCII).trim();
            this.crc32 = String.format("%08x", OplCRC32(title)).toUpperCase();
            this.chunksCount = buffer[0x2f];
            this.cdDvdFlag = buffer[0x30];
            this.romIsDvdImage = (cdDvdFlag == DVD_FLAG);
        }
    }

    public UlConfiguration(String title, String publisherTitle, byte chunksCount, boolean isDVD) throws Exception{
        this.title = title;
        this.publisherTitle = publisherTitle;
        this.crc32 = String.format("%08x", OplCRC32(title)).toUpperCase();
        this.chunksCount = chunksCount;
        if (isDVD)
            cdDvdFlag = DVD_FLAG;
        else
            cdDvdFlag = CD_FLAG;
        this.romIsDvdImage = isDVD;
    }

    private int OplCRC32(String string) throws Exception{
        string = string.trim();

        if (string.length() > 31)
            throw new Exception("Maximum title length exceed. Must be less than 32 symbols!");

        byte[] decodedString = Arrays.copyOf(string.getBytes(StandardCharsets.US_ASCII), 32);
        int crc = 0;
        int[] crcTable = new int[256];

        for (int table = 0; table < 256; table++) {
            crc = table << 24;

            for (int i = 8; i > 0; i--) {
                if (crc < 0)
                    crc = crc << 1;
                else
                    crc = (crc << 1) ^ 0x04C11DB7;
            }
            crcTable[255 - table] = crc;
        }
        for (int i = 0; i <= string.length(); i++) {
            int bytes = decodedString[i];
            crc = crcTable[bytes ^ ((crc >> 24) & 0xFF)] ^ ((crc << 8) & 0xFFFFFF00);
        }
        return crc;
    }

    public String getCrc32() {
        return crc32;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDvd() {
        return romIsDvdImage;
    }

    public String getPublisherTitle() {
        return publisherTitle;
    }

    public byte getChunksCount() {
        return chunksCount;
    }

    public byte[] generateUlConfig(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(0x40);
        byteBuffer.put(title.getBytes(StandardCharsets.US_ASCII));
        ((Buffer) byteBuffer).position(32);
        byteBuffer.put(("ul."+publisherTitle).getBytes());
        ((Buffer) byteBuffer).position(32+15);
        byteBuffer.put(chunksCount);
        byteBuffer.put(cdDvdFlag);
        ((Buffer) byteBuffer).position(53);
        byteBuffer.put((byte) 0x8);        // weird. no idea why it's here
        ((Buffer) byteBuffer).flip();
        return byteBuffer.array();
    }
}
