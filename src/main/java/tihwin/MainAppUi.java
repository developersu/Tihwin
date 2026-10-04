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
package tihwin;

import tihwin.cd.ISO9660;
import tihwin.ui.*;
import tihwin.ui.model.LocaleHolder;
import tihwin.ul.UlConfiguration;
import tihwin.ul.UlMaker;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.GridBagConstraints.*;
import static java.util.Objects.requireNonNull;
import static java.util.ResourceBundle.getBundle;

public class MainAppUi extends JFrame {

    private static final Color COLOR_SKY_BLUE = new Color(114, 211, 253);
    private static final Color SELECT_BTN_BG = new Color(224, 244, 255);
    private static final Color UL_CFG_BTN_BG = new Color(224, 255, 224);
    private static final Color PROGRESS_COLOR = new Color(255, 153, 0);
    private static final int GRID_COLS = 9;

    private static final long MAX_ISO_LENGTH = 0x40000000;

    private ResourceBundle resourceBundle;

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

    private List<Component> components;
    private String recentRomLocation;
    private File diskImage;
    private String publisherTitle;
    private boolean isConvertingNow = false;
    private Thread splitThread;

    public MainAppUi(String appName) {
        super(appName);
        resourceBundle = getBundle("locale");
        setupUi();
        configureComponents();
        setupKeyboardShortcuts();
        AwesomeMediator.setMainUi(this);
        setLocationRelativeTo(null);
        new FilesDropListener(mainPanel);
        setContentPane(mainPanel);
        applyScaling();
    }

    private String msg(String key) {
        return resourceBundle.getString(key);
    }

    private void configureComponents() {
        Border selectBtnBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.gray),
                BorderFactory.createEmptyBorder(5, 5, 5, 5));
        diskImageSelectBtn.setBorder(selectBtnBorder);
        destinationSelectBtn.setBorder(selectBtnBorder);
        ulCfgBtn.setBorder(selectBtnBorder);
        diskImageSelectBtn.addMouseListener(new TwButtonsActionListener());
        destinationSelectBtn.addMouseListener(new TwButtonsActionListener());
        titleField.setBorder(new LineBorder(Color.lightGray));
        statusJPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.darkGray));

        convertBtn.setEnabled(false);
        diskImageSelectBtn.addActionListener(e -> diskImageSelectEventHandler());
        destinationSelectBtn.addActionListener(e -> destinationSelectEventHandler());
        convertBtn.addActionListener(e -> convertButtonAction());
        ulCfgBtn.addActionListener(e -> ulConfigButtonAction());
        zoomInBtn.addActionListener(e -> zoomIn());
        zoomOutBtn.addActionListener(e -> zoomOut());
        ((AbstractDocument) titleField.getDocument()).setDocumentFilter(new TitleFieldFilter());

        if (Settings.INSTANCE.getDvdSelected())
            DVDRadioButton.setSelected(true);
        else
            CDRadioButton.setSelected(true);
        recentRomLocation = Settings.INSTANCE.getRomLocation();
        destinationDirLbl.setText(FilesHelper.getRealFolder(Settings.INSTANCE.getDestination()));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Settings.INSTANCE.setRomLocation(recentRomLocation);
                Settings.INSTANCE.setDestination(destinationDirLbl.getText());
                Settings.INSTANCE.setDvdSelected(DVDRadioButton.isSelected());
                Settings.INSTANCE.setLocale(((LocaleHolder) ulLangComboBox.getSelectedItem()).getLocaleCode());
                Settings.INSTANCE.setScaleFactor(AwesomeMediator.getScaleValue());
            }
        });
    }

    private void setupKeyboardShortcuts() {
        javax.swing.InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK), "zoomIn");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK), "zoomOut");
        Action zoomAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("+".equals(e.getActionCommand()))
                    zoomIn();
                else
                    zoomOut();
            }
        };
        mainPanel.getActionMap().put("zoomIn", zoomAction);
        mainPanel.getActionMap().put("zoomOut", zoomAction);
    }

    private void applyScaling() {
        components = Arrays.stream(mainPanel.getComponents()).collect(Collectors.toList());
        components.add(statusLbl);
        components.add(ulLangComboBox);

        ScaleUi.applyInitialScale(components);
    }

    private void zoomIn() {
        ScaleUi.increaseScale(components);
        pack();
    }

    private void zoomOut() {
        ScaleUi.decreaseScale(components);
        pack();
    }

    private void diskImageSelectEventHandler() {
        File diskImageFile = new TwFileChooser(recentRomLocation, msg("SelectDiskImageText"), true)
                .getFile();
        if (diskImageFile != null)
            setDiskImageFile(diskImageFile);
    }

    public void setDiskImageFile(File imageFile) {
        try {
            recentRomLocation = imageFile.getParent();
            ISO9660 iso9660 = new ISO9660(imageFile);
            publisherTitle = iso9660.getTitle();

            diskImageNameLbl.setText(imageFile.getName());
            convertBtn.setEnabled(true);
            statusLbl.setText(imageFile.getAbsolutePath());

            diskImage = imageFile;
            setProposedTitle();
        }
        catch (Exception e) {
            statusLbl.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    private void setProposedTitle() {
        String proposedName = diskImage.getName().replaceAll("(\\..*)|(\\[.*)", "").trim();
        if (proposedName.length() > 31)
            proposedName = proposedName.substring(0, 31);
        else if (proposedName.isEmpty())
            proposedName = "My favorite game";
        titleField.setText(proposedName);
    }

    private void destinationSelectEventHandler() {
        File destination = new TwFileChooser(destinationDirLbl.getText(), msg("SetDestinationDirectoryText"), false)
                .getFile();
        if (destination != null)
            setDestinationDir(destination);
    }

    public void setDestinationDir(File folder) {
        destinationDirLbl.setText(folder.getAbsolutePath());
    }

    private void convertButtonAction() {
        try {
            if (isConvertingNow) {
                splitThread.interrupt();
                convertBtn.setEnabled(false);
                return;
            }

            if (titleField.getText().isEmpty()) {
                setProposedTitle();
                return;
            }

            byte chunksCount = (byte) (diskImage.length() / MAX_ISO_LENGTH);
            if (diskImage.length() % MAX_ISO_LENGTH > 0)
                chunksCount++;

            UlConfiguration ulConfiguration = new UlConfiguration(
                    titleField.getText(),
                    publisherTitle,
                    chunksCount,
                    DVDRadioButton.isSelected());
            UlMaker ulMaker = new UlMaker(
                    diskImage,
                    destinationDirLbl.getText(),
                    ulConfiguration,
                    new UiUpdater(progressBar, statusLbl));
            statusLbl.setText(msg("InProgressText"));
            splitThread = new Thread(ulMaker);
            splitThread.start();
            isConvertingNow = true;
            convertBtn.setText(msg("AbortText"));
        }
        catch (Exception e) {
            statusLbl.setText(e.getMessage());
        }
    }

    public void notifySplitFinished() {
        isConvertingNow = false;
        convertBtn.setEnabled(true);
        convertBtn.setText(msg("ConvertBtn"));
    }

    private void ulConfigButtonAction() {
        new UpdateUlTableUi(destinationDirLbl.getText());
    }

    private void onLanguageChanged() {
        Locale newLocale = ((LocaleHolder) requireNonNull(ulLangComboBox.getSelectedItem())).getLocale();
        Locale.setDefault(newLocale);
        resourceBundle = getBundle("locale");

        diskImageSelectBtn.setText(msg("SelectBtn"));
        destinationSelectBtn.setText(msg("SelectBtn"));
        if (isConvertingNow) {
            convertBtn.setText(msg("AbortText"));
            statusLbl.setText(msg("InProgressText"));
        }
        else
            convertBtn.setText(msg("ConvertBtn"));

        diskImageRoLbl.setText(msg("DiskImageLbl"));
        titleRoLbl.setText(msg("TitleLbl"));
        ulDestinationRoLbl.setText(msg("ulDestinationLbl"));

        CDRadioButton.setText(msg("CD"));
        DVDRadioButton.setText(msg("DVD"));
        ulCfgBtn.setText(msg("editUlCfgBtn"));
    }

    private void setupUi() {
        mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(createBannerPanel(),
                placeInsets0(0, 0, GRID_COLS, CENTER, BOTH, 1, 0));
        mainPanel.add(Box.createGlue(),
                placeInsets0(0, 1, GRID_COLS, NORTH, HORIZONTAL, 0f, 1.1f));

        addGapStruts();

        diskImageSelectBtn = new JButton(msg("SelectBtn"));
        diskImageSelectBtn.setBackground(SELECT_BTN_BG);
        mainPanel.add(diskImageSelectBtn,
                place(0, 2, 3, CENTER, HORIZONTAL, 0, 0, new Insets(4, 5, 0, 0)));

        diskImageRoLbl = new JLabel(msg("DiskImageLbl"));
        mainPanel.add(diskImageRoLbl,
                place(4, 2, 1, WEST, HORIZONTAL, 0, 0, new Insets(4, 0, 0, 0)));

        diskImageNameLbl = new JLabel();
        mainPanel.add(diskImageNameLbl,
                place(6, 2, 1, WEST, HORIZONTAL, 1, 0, new Insets(4, 0, 0, 0)));

        ulCfgBtn = new JButton(msg("editUlCfgBtn"));
        ulCfgBtn.setBackground(UL_CFG_BTN_BG);
        mainPanel.add(ulCfgBtn,
                place(8, 2, 1, CENTER, HORIZONTAL, 0.2f, 0f, new Insets(4, 0, 0, 5)));

        CDRadioButton = new JRadioButton(msg("CD"));
        mainPanel.add(CDRadioButton,
                place(0, 3, 1, CENTER, NONE, 0, 0, new Insets(4, 0, 4, 0)));

        DVDRadioButton = new JRadioButton(msg("DVD"));
        mainPanel.add(DVDRadioButton,
                place(2, 3, 1, CENTER, NONE, 0, 0, new Insets(4, 0, 4, 0)));

        titleRoLbl = new JLabel(msg("TitleLbl"));
        mainPanel.add(titleRoLbl,
                place(4, 3, 1, WEST, HORIZONTAL, 0, 0, new Insets(4, 0, 4, 0)));

        titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(150, 16));
        mainPanel.add(titleField,
                place(6, 3, 3, CENTER, HORIZONTAL, 0, 0, new Insets(4, 0, 4, 5)));

        destinationSelectBtn = new JButton(msg("SelectBtn"));
        destinationSelectBtn.setBackground(SELECT_BTN_BG);
        mainPanel.add(destinationSelectBtn,
                place(0, 4, 3, CENTER, HORIZONTAL, 0, 0, new Insets(4, 5, 0, 0)));

        ulDestinationRoLbl = new JLabel(msg("ulDestinationLbl"));
        mainPanel.add(ulDestinationRoLbl,
                place(4, 4, 1, WEST, HORIZONTAL, 0, 0, new Insets(4, 0, 0, 0)));

        destinationDirLbl = new JLabel("");
        mainPanel.add(destinationDirLbl,
                place(6, 4, 3, WEST, HORIZONTAL, 0, 0, new Insets(4, 0, 0, 0)));

        convertBtn = new JButton(msg("ConvertBtn"));
        convertBtn.setBackground(SELECT_BTN_BG);
        convertBtn.setMinimumSize(new Dimension(convertBtn.getMinimumSize().width, 50));
        mainPanel.add(convertBtn,
                place(0, 5, GRID_COLS, CENTER, HORIZONTAL, 0, 0, new Insets(4, 5, 4, 5)));

        progressBar = new JProgressBar();
        progressBar.setBorderPainted(false);
        progressBar.setForeground(PROGRESS_COLOR);
        progressBar.setIndeterminate(false);
        mainPanel.add(progressBar,
                place(0, 6, GRID_COLS, NORTH, HORIZONTAL, 0, 0, new Insets(4, 0, 4, 0)));

        statusJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        statusJPanel.setBackground(Color.WHITE);
        mainPanel.add(statusJPanel,
                place(0, 7, GRID_COLS, CENTER, BOTH, 0, 0, new Insets(4, 0, 0, 0)));

        statusLbl = new JLabel(msg("WelcomeText"));
        statusJPanel.add(statusLbl);

        ButtonGroup driveButtonGroup = new ButtonGroup();
        driveButtonGroup.add(DVDRadioButton);
        driveButtonGroup.add(CDRadioButton);
    }

    private JPanel createBannerPanel() {
        JPanel banner = new JPanel(new GridBagLayout());
        banner.setBackground(COLOR_SKY_BLUE);

        JLabel bannerLabel = new JLabel();
        bannerLabel.setIcon(new ImageIcon(requireNonNull(getClass().getResource("/banner.png"))));
        banner.add(bannerLabel, placeInsets0(0, 0, 1, CENTER, NONE, 1, 0));

        ulLangComboBox = new LanguageComboBox();
        ulLangComboBox.addActionListener(e -> onLanguageChanged());
        banner.add(ulLangComboBox, placeInsets0(1, 0, 1, NORTHWEST, HORIZONTAL, 0, 0));

        zoomInBtn = new TwZoomButton("/zoom-in.png");
        zoomOutBtn = new TwZoomButton("/zoom-out.png");
        banner.add(new TwZoomPanel(zoomInBtn, zoomOutBtn),
                placeInsets0(0, 0, 1, NORTHWEST, NONE, 0, 0));

        return banner;
    }

    private void addGapStruts() {
        mainPanel.add(Box.createHorizontalStrut(8), placeGapStruts(1));
        mainPanel.add(Box.createHorizontalStrut(8), placeGapStruts(3));
        mainPanel.add(Box.createHorizontalStrut(4), placeGapStruts(5));
        mainPanel.add(Box.createHorizontalStrut(8), placeGapStruts(7));
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
