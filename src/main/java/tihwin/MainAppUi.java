/*
    Copyright 2022-2023 Dmitry Isaenko

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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainAppUi extends JFrame {
    private JPanel mainPanel;
    private JButton diskImageSelectBtn;
    private JButton destinationSelectBtn;
    private JButton convertBtn;
    private JLabel diskImageNameLbl;
    private JLabel statusLbl;
    private JProgressBar progressBar;
    private JPanel statusJPanel;
    private JLabel destinationDirectoryLbl;
    private JTextField titleField;
    private JRadioButton CDRadioButton;
    private JRadioButton DVDRadioButton;
    private JButton ulCfgBtn;
    private JComboBox<LocaleHolder> ulLangComboBox;
    private JLabel diskImageRoLbl;
    private JLabel titleRoLbl;
    private JLabel ulDestinationRoLbl;
    private ResourceBundle resourceBundle;

    private String recentRomLocation;
    private File diskImage;
    private String publisherTitle;

    private boolean doWeConvertAnythingNow = false;
    private Thread splitThread;

    public MainAppUi(String appName) {
        $$$setupUI$$$();
        resourceBundle = ResourceBundle.getBundle("locale");
        AwesomeMediator.setMainUi(this);
        setLocationRelativeTo(null); // Set window on [kinda] center
        new FilesDropListener(mainPanel);
        setContentPane(mainPanel);
        statusJPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.darkGray));
        convertBtn.setEnabled(false);
        diskImageSelectBtn.addActionListener(actionEvent -> diskImageSelectEventHandler());
        destinationSelectBtn.addActionListener(actionEvent -> destinationSelectEventHandler());
        convertBtn.addActionListener(actionEvent -> convertButtonAction());
        ulCfgBtn.addActionListener(actionEvent -> ulConfigButtonAction());
        ((AbstractDocument) titleField.getDocument()).setDocumentFilter(new TitleFieldFilter());
        if (Settings.INSTANCE.getDvdSelected())
            DVDRadioButton.setSelected(true);
        else
            CDRadioButton.setSelected(true);
        recentRomLocation = Settings.INSTANCE.getRomLocation();
        destinationDirectoryLbl.setText(FilesHelper.getRealFolder(Settings.INSTANCE.getDestination()));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                Settings.INSTANCE.setRomLocation(recentRomLocation);
                Settings.INSTANCE.setDestination(destinationDirectoryLbl.getText());
                Settings.INSTANCE.setDvdSelected(DVDRadioButton.isSelected());
                Settings.INSTANCE.setLocale(((LocaleHolder) ulLangComboBox.getSelectedItem()).getLocaleCode());
            }
        });

        Border fitMoreTextOnButtonBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.gray),
                BorderFactory.createEmptyBorder(5, 5, 5, 5));
        diskImageSelectBtn.setBorder(fitMoreTextOnButtonBorder);
        destinationSelectBtn.setBorder(fitMoreTextOnButtonBorder);
        diskImageSelectBtn.addMouseListener(new TwButtonsActionListener());
        destinationSelectBtn.addMouseListener(new TwButtonsActionListener());

        titleField.setBorder(new LineBorder(Color.lightGray));
    }

    private void diskImageSelectEventHandler() {
        JFileChooser fileChooser = new JFileChooser(FilesHelper.getRealFolder(recentRomLocation));
        fileChooser.setDialogTitle(resourceBundle.getString("SelectDiskImageText"));
        fileChooser.setFileFilter(new IsoFileFilter());
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            setDiskImageFile(fileChooser.getSelectedFile());
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
        } catch (Exception e) {
            statusLbl.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    private void setProposedTitle() {
        String proposedName = diskImage.getName().replaceAll("(\\..*)|(\\[.*)", "").trim();
        if (proposedName.length() > 31)
            proposedName = proposedName.substring(0, 31);
        if (proposedName.isEmpty())
            proposedName = "My favorite game";
        titleField.setText(proposedName);
    }

    private void destinationSelectEventHandler() {
        JFileChooser fileChooser = new JFileChooser(FilesHelper.getRealFolder(destinationDirectoryLbl.getText()));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(resourceBundle.getString("SetDestinationDirectoryText"));
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            setDestinationDir(fileChooser.getSelectedFile());
    }

    public void setDestinationDir(File folder) {
        destinationDirectoryLbl.setText(folder.getAbsolutePath());
    }

    private void convertButtonAction() {
        try {
            if (doWeConvertAnythingNow) {
                splitThread.interrupt();
                convertBtn.setEnabled(false);
                return;
            }

            if (titleField.getText().isEmpty()) {
                setProposedTitle();
                return;
            }

            String userTitle = titleField.getText();

            byte chunksCount = (byte) (diskImage.length() / 0x40000000);
            if (diskImage.length() % 0x40000000 > 0)
                chunksCount++;

            UlConfiguration ulConfiguration = new UlConfiguration(
                    userTitle,
                    publisherTitle,
                    chunksCount,
                    DVDRadioButton.isSelected());
            UlMaker ulMaker = new UlMaker(
                    diskImage,
                    destinationDirectoryLbl.getText(),
                    ulConfiguration,
                    new UiUpdater(progressBar, statusLbl));
            statusLbl.setText(resourceBundle.getString("InProgressText"));
            splitThread = new Thread(ulMaker);
            splitThread.start();
            doWeConvertAnythingNow = true;
            convertBtn.setText(resourceBundle.getString("AbortText"));
        } catch (Exception e) {
            statusLbl.setText(e.getMessage());
        }
    }

    public void notifySplitFinished() {
        doWeConvertAnythingNow = false;
        convertBtn.setEnabled(true);
        convertBtn.setText(resourceBundle.getString("ConvertBtn"));
    }

    private void ulConfigButtonAction() {
        new UpdateUlTableUi(destinationDirectoryLbl.getText());
    }

    private void onLanguageChanged() {
        Locale newLocale = ((LocaleHolder) ulLangComboBox.getSelectedItem()).getLocale();
        Locale.setDefault(newLocale);
        resourceBundle = ResourceBundle.getBundle("locale");

        diskImageSelectBtn.setText(resourceBundle.getString("SelectBtn"));
        destinationSelectBtn.setText(resourceBundle.getString("SelectBtn"));
        if (doWeConvertAnythingNow) {
            convertBtn.setText(resourceBundle.getString("AbortText"));
            statusLbl.setText(resourceBundle.getString("InProgressText"));
        } else
            convertBtn.setText(resourceBundle.getString("ConvertBtn"));

        diskImageRoLbl.setText(resourceBundle.getString("DiskImageLbl"));
        titleRoLbl.setText(resourceBundle.getString("TitleLbl"));
        ulDestinationRoLbl.setText(resourceBundle.getString("ulDestinationLbl"));

        CDRadioButton.setText(resourceBundle.getString("CD"));
        DVDRadioButton.setText(resourceBundle.getString("DVD"));
        ulCfgBtn.setText(resourceBundle.getString("editUlCfgBtn"));
    }

    private void createUIComponents() {
        this.ulLangComboBox = new LanguageComboBox();
        ulLangComboBox.addActionListener(e -> onLanguageChanged());
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new FormLayout("fill:p:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:p:noGrow,fill:max(d;4px):noGrow,fill:d:grow,left:4dlu:noGrow,fill:p:noGrow", "center:max(d;4px):noGrow,top:m:grow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;1dlu):noGrow"));
        diskImageSelectBtn = new JButton();
        diskImageSelectBtn.setBackground(new Color(-2034433));
        this.$$$loadButtonText$$$(diskImageSelectBtn, this.$$$getMessageFromBundle$$$("locale", "SelectBtn"));
        CellConstraints cc = new CellConstraints();
        mainPanel.add(diskImageSelectBtn, new CellConstraints(1, 4, 3, 1, CellConstraints.DEFAULT, CellConstraints.CENTER, new Insets(0, 5, 0, 0)));
        titleField = new JTextField();
        mainPanel.add(titleField, new CellConstraints(7, 6, 3, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
        destinationDirectoryLbl = new JLabel();
        destinationDirectoryLbl.setText("");
        mainPanel.add(destinationDirectoryLbl, cc.xyw(7, 8, 3));
        diskImageNameLbl = new JLabel();
        diskImageNameLbl.setText("");
        mainPanel.add(diskImageNameLbl, cc.xy(7, 4));
        destinationSelectBtn = new JButton();
        destinationSelectBtn.setBackground(new Color(-2034433));
        this.$$$loadButtonText$$$(destinationSelectBtn, this.$$$getMessageFromBundle$$$("locale", "SelectBtn"));
        mainPanel.add(destinationSelectBtn, new CellConstraints(1, 8, 3, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 5, 0, 0)));
        ulDestinationRoLbl = new JLabel();
        this.$$$loadLabelText$$$(ulDestinationRoLbl, this.$$$getMessageFromBundle$$$("locale", "ulDestinationLbl"));
        mainPanel.add(ulDestinationRoLbl, cc.xy(5, 8));
        diskImageRoLbl = new JLabel();
        this.$$$loadLabelText$$$(diskImageRoLbl, this.$$$getMessageFromBundle$$$("locale", "DiskImageLbl"));
        mainPanel.add(diskImageRoLbl, cc.xy(5, 4));
        titleRoLbl = new JLabel();
        this.$$$loadLabelText$$$(titleRoLbl, this.$$$getMessageFromBundle$$$("locale", "TitleLbl"));
        mainPanel.add(titleRoLbl, cc.xy(5, 6));
        CDRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(CDRadioButton, this.$$$getMessageFromBundle$$$("locale", "CD"));
        mainPanel.add(CDRadioButton, cc.xy(1, 6));
        DVDRadioButton = new JRadioButton();
        DVDRadioButton.setSelected(true);
        this.$$$loadButtonText$$$(DVDRadioButton, this.$$$getMessageFromBundle$$$("locale", "DVD"));
        mainPanel.add(DVDRadioButton, cc.xy(3, 6));
        convertBtn = new JButton();
        convertBtn.setBackground(new Color(-2034433));
        this.$$$loadButtonText$$$(convertBtn, this.$$$getMessageFromBundle$$$("locale", "ConvertBtn"));
        mainPanel.add(convertBtn, new CellConstraints(1, 10, 9, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 5, 0, 5)));
        progressBar = new JProgressBar();
        progressBar.setBorderPainted(false);
        progressBar.setForeground(new Color(-26368));
        progressBar.setIndeterminate(false);
        mainPanel.add(progressBar, cc.xyw(1, 12, 9, CellConstraints.FILL, CellConstraints.TOP));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-9251843));
        mainPanel.add(panel1, cc.xyw(1, 1, 9));
        final JLabel label1 = new JLabel();
        label1.setIcon(new ImageIcon(getClass().getResource("/banner.png")));
        label1.setText("");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ulLangComboBox.setBackground(new Color(-9251843));
        ulLangComboBox.setForeground(new Color(-1));
        ulLangComboBox.putClientProperty("html.disable", Boolean.FALSE);
        panel1.add(ulLangComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        ulCfgBtn = new JButton();
        ulCfgBtn.setBackground(new Color(-2031648));
        ulCfgBtn.setMargin(new Insets(1, 1, 1, 1));
        ulCfgBtn.setMinimumSize(new Dimension(130, 30));
        ulCfgBtn.setPreferredSize(new Dimension(140, 30));
        this.$$$loadButtonText$$$(ulCfgBtn, this.$$$getMessageFromBundle$$$("locale", "editUlCfgBtn"));
        mainPanel.add(ulCfgBtn, new CellConstraints(9, 4, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
        statusJPanel = new JPanel();
        statusJPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        statusJPanel.setBackground(new Color(-1));
        mainPanel.add(statusJPanel, cc.xyw(1, 14, 9));
        statusLbl = new JLabel();
        this.$$$loadLabelText$$$(statusLbl, this.$$$getMessageFromBundle$$$("locale", "WelcomeText"));
        statusJPanel.add(statusLbl, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(DVDRadioButton);
        buttonGroup.add(CDRadioButton);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
