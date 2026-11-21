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
package tihwin.ui;

import tihwin.ui.model.LocaleHolder;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.awt.GridBagConstraints.*;

public final class MainUiBuilder {

    public interface TextProvider {
        String text(String key);
    }

    public static final class Parts {
        public final JPanel mainPanel;
        public final JButton diskImageSelectBtn;
        public final JButton destinationSelectBtn;
        public final JButton convertBtn;
        public final JButton ulCfgBtn;
        public final JButton zoomInBtn;
        public final JButton zoomOutBtn;
        public final JLabel diskImageNameLbl;
        public final JLabel diskImageRoLbl;
        public final JLabel titleRoLbl;
        public final JLabel ulDestinationRoLbl;
        public final JLabel destinationDirLbl;
        public final JLabel statusLbl;
        public final JTextField titleField;
        public final JRadioButton CDRadioButton;
        public final JRadioButton DVDRadioButton;
        public final JProgressBar progressBar;
        public final JPanel statusJPanel;
        public final JComboBox<LocaleHolder> ulLangComboBox;
        public final List<Component> scalableComponents;
        public final List<Map.Entry<String, Consumer<String>>> translatableComponents;

        public Parts(JPanel mainPanel,
                     JButton diskImageSelectBtn,
                     JButton destinationSelectBtn,
                     JButton convertBtn,
                     JButton ulCfgBtn,
                     JButton zoomInBtn,
                     JButton zoomOutBtn,
                     JLabel diskImageNameLbl,
                     JLabel diskImageRoLbl,
                     JLabel titleRoLbl,
                     JLabel ulDestinationRoLbl,
                     JLabel destinationDirLbl,
                     JLabel statusLbl,
                     JTextField titleField,
                     JRadioButton CDRadioButton,
                     JRadioButton DVDRadioButton,
                     JProgressBar progressBar,
                     JPanel statusJPanel,
                     JComboBox<LocaleHolder> ulLangComboBox,
                     List<Component> scalableComponents,
                     List<Map.Entry<String, Consumer<String>>> translatableComponents) {
            this.mainPanel = mainPanel;
            this.diskImageSelectBtn = diskImageSelectBtn;
            this.destinationSelectBtn = destinationSelectBtn;
            this.convertBtn = convertBtn;
            this.ulCfgBtn = ulCfgBtn;
            this.zoomInBtn = zoomInBtn;
            this.zoomOutBtn = zoomOutBtn;
            this.diskImageNameLbl = diskImageNameLbl;
            this.diskImageRoLbl = diskImageRoLbl;
            this.titleRoLbl = titleRoLbl;
            this.ulDestinationRoLbl = ulDestinationRoLbl;
            this.destinationDirLbl = destinationDirLbl;
            this.statusLbl = statusLbl;
            this.titleField = titleField;
            this.CDRadioButton = CDRadioButton;
            this.DVDRadioButton = DVDRadioButton;
            this.progressBar = progressBar;
            this.statusJPanel = statusJPanel;
            this.ulLangComboBox = ulLangComboBox;
            this.scalableComponents = scalableComponents;
            this.translatableComponents = translatableComponents;
        }
    }

    private static final Color COLOR_SKY_BLUE = new Color(114, 211, 253);
    private static final Color SELECT_BTN_BG = new Color(224, 244, 255);
    private static final Color UL_CFG_BTN_BG = new Color(224, 255, 224);
    private static final Color PROGRESS_COLOR = new Color(255, 153, 0);
    private static final int GRID_COLS = 9;

    private final TextProvider texts;
    private final List<Component> scalableComponents = new ArrayList<>();
    private final List<Map.Entry<String, Consumer<String>>> translatableComponents = new ArrayList<>();

    private JPanel mainPanel;
    private JButton diskImageSelectBtn;
    private JButton destinationSelectBtn;
    private JButton convertBtn;
    private JButton ulCfgBtn;
    private JButton zoomInBtn;
    private JButton zoomOutBtn;
    private JLabel diskImageNameLbl;
    private JLabel diskImageRoLbl;
    private JLabel titleRoLbl;
    private JLabel ulDestinationRoLbl;
    private JLabel destinationDirLbl;
    private JLabel statusLbl;
    private JTextField titleField;
    private JRadioButton CDRadioButton;
    private JRadioButton DVDRadioButton;
    private JProgressBar progressBar;
    private JPanel statusJPanel;
    private JComboBox<LocaleHolder> ulLangComboBox;

    private MainUiBuilder(TextProvider texts) {
        this.texts = texts;
    }

    public static Parts build(TextProvider texts) {
        MainUiBuilder builder = new MainUiBuilder(texts);
        builder.setupUi();
        return new Parts(
                builder.mainPanel,
                builder.diskImageSelectBtn,
                builder.destinationSelectBtn,
                builder.convertBtn,
                builder.ulCfgBtn,
                builder.zoomInBtn,
                builder.zoomOutBtn,
                builder.diskImageNameLbl,
                builder.diskImageRoLbl,
                builder.titleRoLbl,
                builder.ulDestinationRoLbl,
                builder.destinationDirLbl,
                builder.statusLbl,
                builder.titleField,
                builder.CDRadioButton,
                builder.DVDRadioButton,
                builder.progressBar,
                builder.statusJPanel,
                builder.ulLangComboBox,
                builder.scalableComponents,
                builder.translatableComponents);
    }

    private void setupUi() {
        mainPanel = new JPanel(new GridBagLayout());

        addToMainPanel(createBannerPanel(),
                placeInsets0(0, 0, GRID_COLS, CENTER, BOTH, 1, 0));
        addToMainPanel(Box.createGlue(),
                placeInsets0(0, 1, GRID_COLS, NORTH, HORIZONTAL, 0f, 1.1f));

        addGapStruts();

        diskImageSelectBtn = new JButton();
        withText(diskImageSelectBtn, "SelectBtn");
        diskImageSelectBtn.setBackground(SELECT_BTN_BG);
        addToMainPanel(diskImageSelectBtn,
                place(0, 2, 3, CENTER, HORIZONTAL, 0, 0, new Insets(4, 5, 0, 0)));

        diskImageRoLbl = new JLabel();
        withText(diskImageRoLbl, "DiskImageLbl");
        addToMainPanel(diskImageRoLbl,
                place(4, 2, 1, WEST, HORIZONTAL, 0, 0, new Insets(4, 0, 0, 0)));

        diskImageNameLbl = new JLabel();
        addToMainPanel(diskImageNameLbl,
                place(6, 2, 1, WEST, HORIZONTAL, 1, 0, new Insets(4, 0, 0, 0)));

        ulCfgBtn = new JButton();
        withText(ulCfgBtn, "editUlCfgBtn");
        ulCfgBtn.setBackground(UL_CFG_BTN_BG);
        addToMainPanel(ulCfgBtn,
                place(8, 2, 1, CENTER, HORIZONTAL, 0.2f, 0f, new Insets(4, 0, 0, 5)));

        CDRadioButton = new JRadioButton();
        withText(CDRadioButton, "CD");
        addToMainPanel(CDRadioButton,
                place(0, 3, 1, CENTER, NONE, 0, 0, new Insets(4, 0, 4, 0)));

        DVDRadioButton = new JRadioButton();
        withText(DVDRadioButton, "DVD");
        addToMainPanel(DVDRadioButton,
                place(2, 3, 1, CENTER, NONE, 0, 0, new Insets(4, 0, 4, 0)));

        titleRoLbl = new JLabel();
        withText(titleRoLbl, "TitleLbl");
        addToMainPanel(titleRoLbl,
                place(4, 3, 1, WEST, HORIZONTAL, 0, 0, new Insets(4, 0, 4, 0)));

        titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(150, 16));
        addToMainPanel(titleField,
                place(6, 3, 3, CENTER, HORIZONTAL, 0, 0, new Insets(4, 0, 4, 5)));

        destinationSelectBtn = new JButton();
        withText(destinationSelectBtn, "SelectBtn");
        destinationSelectBtn.setBackground(SELECT_BTN_BG);
        addToMainPanel(destinationSelectBtn,
                place(0, 4, 3, CENTER, HORIZONTAL, 0, 0, new Insets(4, 5, 0, 0)));

        ulDestinationRoLbl = new JLabel();
        withText(ulDestinationRoLbl, "ulDestinationLbl");
        addToMainPanel(ulDestinationRoLbl,
                place(4, 4, 1, WEST, HORIZONTAL, 0, 0, new Insets(4, 0, 0, 0)));

        destinationDirLbl = new JLabel("");
        addToMainPanel(destinationDirLbl,
                place(6, 4, 3, WEST, HORIZONTAL, 0, 0, new Insets(4, 0, 0, 0)));

        convertBtn = new JButton();
        withText(convertBtn, "ConvertBtn");
        convertBtn.setBackground(SELECT_BTN_BG);
        convertBtn.setMinimumSize(new Dimension(convertBtn.getMinimumSize().width, 50));
        addToMainPanel(convertBtn,
                place(0, 5, GRID_COLS, CENTER, HORIZONTAL, 0, 0, new Insets(4, 5, 4, 5)));

        progressBar = new JProgressBar();
        progressBar.setBorderPainted(false);
        progressBar.setForeground(PROGRESS_COLOR);
        progressBar.setIndeterminate(false);
        addToMainPanel(progressBar,
                place(0, 6, GRID_COLS, NORTH, HORIZONTAL, 0, 0, new Insets(4, 0, 4, 0)));

        statusJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        statusJPanel.setBackground(Color.WHITE);
        addToMainPanel(statusJPanel,
                place(0, 7, GRID_COLS, CENTER, BOTH, 0, 0, new Insets(4, 0, 0, 0)));

        statusLbl = new JLabel();
        withText(statusLbl, "WelcomeText");
        statusJPanel.add(statusLbl);
        scalableComponents.add(statusLbl);

        ButtonGroup driveButtonGroup = new ButtonGroup();
        driveButtonGroup.add(DVDRadioButton);
        driveButtonGroup.add(CDRadioButton);
    }

    private JPanel createBannerPanel() {
        JPanel banner = new JPanel(new GridBagLayout());
        banner.setBackground(COLOR_SKY_BLUE);

        JLabel bannerLabel = new JLabel();
        bannerLabel.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/banner.png"))));
        banner.add(bannerLabel, placeInsets0(0, 0, 1, CENTER, NONE, 1, 0));

        ulLangComboBox = new LanguageComboBox();
        banner.add(ulLangComboBox, placeInsets0(1, 0, 1, NORTHWEST, HORIZONTAL, 0, 0));
        scalableComponents.add(ulLangComboBox);

        zoomInBtn = new TwZoomButton("/zoom-in.png");
        zoomOutBtn = new TwZoomButton("/zoom-out.png");
        banner.add(new TwZoomPanel(zoomInBtn, zoomOutBtn),
                placeInsets0(0, 0, 1, NORTHWEST, NONE, 0, 0));

        return banner;
    }

    private void addGapStruts() {
        addToMainPanel(Box.createHorizontalStrut(8), placeGapStruts(1));
        addToMainPanel(Box.createHorizontalStrut(8), placeGapStruts(3));
        addToMainPanel(Box.createHorizontalStrut(4), placeGapStruts(5));
        addToMainPanel(Box.createHorizontalStrut(8), placeGapStruts(7));
    }

    private void withText(AbstractButton component, String key) {
        component.setText(texts.text(key));
        translatableComponents.add(new AbstractMap.SimpleEntry<>(key, component::setText));
    }

    private void withText(JLabel component, String key) {
        component.setText(texts.text(key));
        translatableComponents.add(new AbstractMap.SimpleEntry<>(key, component::setText));
    }

    private void addToMainPanel(Component component, GridBagConstraints constraints) {
        mainPanel.add(component, constraints);
        scalableComponents.add(component);
    }

    private GridBagConstraints placeGapStruts(int gridx) {
        return placeInsets0(gridx, 2, 1, CENTER, NONE, 0, 0);
    }

    private GridBagConstraints placeInsets0(int gridx, int gridy, int gridwidth,
                                            int anchor, int fill, float weightx, float weighty) {
        return place(gridx, gridy, gridwidth, anchor, fill, weightx, weighty,
                new Insets(0, 0, 0, 0));
    }

    private GridBagConstraints place(int gridx, int gridy, int gridwidth,
                                     int anchor, int fill, float weightx, float weighty,
                                     Insets insets) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.gridwidth = gridwidth;
        c.anchor = anchor;
        c.fill = fill;
        c.weightx = weightx;
        c.weighty = weighty;
        c.insets = insets;
        return c;
    }
}
