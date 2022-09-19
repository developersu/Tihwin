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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class DirectoryEntry {
    private final int extentLocation;   // lsb start
    private final int dataSize;
    private final String identifier;

    public DirectoryEntry(byte[] entryBytes){
        this.extentLocation = getLEint(entryBytes, 0x2);
        this.dataSize = getLEint(entryBytes, 0xA);
        byte identifierLength = entryBytes[0x20];
        this.identifier = new String(entryBytes, 0x21, identifierLength, StandardCharsets.US_ASCII);
    }
    private int getLEint(byte[] bytes, int fromOffset){
        return ByteBuffer.wrap(bytes, fromOffset, 0x4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public int getExtentLocation() {return extentLocation;}
    public int getDataSize() {return dataSize;}
    public String getIdentifier() {return identifier;}
}
