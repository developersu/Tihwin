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
import java.awt.*;

import static java.util.Objects.requireNonNull;

public class TwZoomButton extends JButton {
    public TwZoomButton(String iconPath) {
        super();
        setAlignmentY(0.0f);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(true);
        setIcon(new ImageIcon(requireNonNull(getClass().getResource(iconPath))));
        setIconTextGap(0);
        setOpaque(false);
        setPreferredSize(new Dimension(25, 25));
    }    
}
