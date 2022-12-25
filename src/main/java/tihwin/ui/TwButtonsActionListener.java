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
package tihwin.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TwButtonsActionListener implements MouseListener {
    private final Border normalBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.gray),
            BorderFactory.createEmptyBorder(5, 5, 5, 5));

    private final Border boldBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.gray),
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(164, 181, 255)),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)
            )
    );

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        ((JButton) mouseEvent.getSource()).setBorder(boldBorder);
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        ((JButton) mouseEvent.getSource()).setBorder(normalBorder);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}
    @Override
    public void mousePressed(MouseEvent mouseEvent) {}
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

}
