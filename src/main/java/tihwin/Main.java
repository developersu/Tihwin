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

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.ResourceBundle;

public class Main {
    public static void main(String[] args) {
        String appVersion = ResourceBundle.getBundle("app").getString("_version");
        MainAppUi frame = new MainAppUi("Tihwin "+appVersion);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Image img = new ImageIcon(Objects.requireNonNull(MainAppUi.class.getClassLoader().getResource("tray_icon.gif"))).getImage();
        frame.setIconImage(img);
        frame.setMinimumSize(new Dimension(700, 300));
        //frame.setResizable(false);
        frame.setVisible(true);
    }
}
