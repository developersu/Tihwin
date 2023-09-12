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
package tihwin.ui;

import tihwin.ui.model.LocaleHolder;
import tihwin.ui.model.SettingsLanguagesSetup;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.util.List;

public class LanguageComboBox extends JComboBox<LocaleHolder> {
    private static final Color COLOR_SKY_BLUE = new Color(114, 211, 253);
    private final Color COLOR_DARK = new Color(71, 81, 93);
    private final Border myBorder = new EmptyBorder(5, 10, 5, 10);

    public LanguageComboBox(){
        super();
        UIManager.put("ComboBox.selectionForeground", COLOR_DARK);
        UIManager.put("ComboBox.selectionBackground", COLOR_SKY_BLUE);
        UIManager.put("ComboBox.squareButton", Boolean.FALSE);

        setUI(new BasicComboBoxUI(){
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton();
                button.setBackground(COLOR_SKY_BLUE);
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setVisible(false);
                return button;
            }
        });

        ListCellRenderer<LocaleHolder> current = (ListCellRenderer<LocaleHolder>) getRenderer();

        setRenderer((list, localeHolder, index, isSelected, hasFocus) -> {
            JLabel component = (JLabel) current.getListCellRendererComponent(list, localeHolder, index, isSelected, hasFocus);

            component.setHorizontalAlignment(SwingConstants.RIGHT);
            component.setBorder(myBorder);

            if (isSelected) {
                component.setForeground(COLOR_DARK);
                component.setBackground(Color.ORANGE);
            } else {
                component.setForeground(Color.white);
                component.setBackground(COLOR_DARK);
            }
            return component;
        });
        setForeground(Color.white);
        setBackground(COLOR_SKY_BLUE);
        SettingsLanguagesSetup setup = new SettingsLanguagesSetup();

        List<LocaleHolder> languages = setup.getLanguages();
        for (LocaleHolder lang: languages)
            addItem(lang);
        this.setSelectedItem(setup.getRecentLanguage());
    }
}
