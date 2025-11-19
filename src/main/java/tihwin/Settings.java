/*
    Copyright 2022-2025 Dmitry Isaenko
     
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

import java.util.Locale;
import java.util.prefs.Preferences;

public class Settings {
    public static final Settings INSTANCE = new Settings();

    private final Preferences preferences;
    private final Locale locale;

    Settings(){
        this.preferences = Preferences.userRoot().node("tihwin");
        String localeCode = preferences.get("locale", Locale.getDefault().toString());
        this.locale = new Locale(localeCode.substring(0, 2), localeCode.substring(3));
    }

    public String getRomLocation(){
        return preferences.get("rom_location", System.getProperty("user.home"));
    }
    public String getDestination(){
        return preferences.get("destination", System.getProperty("user.home"));
    }
    public boolean getDvdSelected(){
        return preferences.getBoolean("dvd_selected", true);
    }

    public void setDestination(String location){
        preferences.put("destination", location);
    }
    public void setRomLocation(String location){
        preferences.put("rom_location", location);
    }
    public void setDvdSelected(boolean value) {
        preferences.putBoolean("dvd_selected", value);
    }

    public Locale getLocale(){ return this.locale; }
    public void setLocale(String localeId){ preferences.put("locale", localeId); }

    public int getScaleFactor(){
        return preferences.getInt("scale", 0);
    }
    public void setScaleFactor(int value){
        preferences.putInt("scale", value);
    }
}
