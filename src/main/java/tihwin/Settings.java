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
package tihwin;

import java.util.prefs.Preferences;

public class Settings {
    public static final Settings INSTANCE = new Settings();

    private final Preferences preferences;

    Settings(){
        this.preferences = Preferences.userRoot().node("tihwin");
    }

    public String getRomLocation(){
        return preferences.get("rom_location", System.getProperty("user.home"));
    }
    public String getDestination(){
        return preferences.get("destination", System.getProperty("user.home"));
    }

    public void setDestination(String location){
        preferences.put("destination", location);
    }
    public void setRomLocation(String location){
        preferences.put("rom_location", location);
    }
}
