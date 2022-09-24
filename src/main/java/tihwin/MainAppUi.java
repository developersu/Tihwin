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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import tihwin.cd.ISO9660;
import tihwin.ui.IsoFileFilter;
import tihwin.ui.TitleFieldFilter;
import tihwin.ui.UiUpdater;
import tihwin.ul.UlConfiguration;
import tihwin.ul.UlMaker;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.reflect.Method;
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
    private final ResourceBundle resourceBundle;

    private String recentRomLocation;
    private File diskImage;
    private String publisherTitle;

    private boolean doWeConvertAnythingNow = false;
    private Thread splitThread;

    public MainAppUi(String appName) {
        super(appName);
        resourceBundle = ResourceBundle.getBundle("locale");
        AwesomeMediator.setMainUi(this);
        setLocationRelativeTo(null); // Set window on [kinda] center
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

        addWindowListener(getWindowListener());
    }

    private WindowListener getWindowListener() {
        return new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {

            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                Settings.INSTANCE.setRomLocation(recentRomLocation);
                Settings.INSTANCE.setDestination(destinationDirectoryLbl.getText());
                Settings.INSTANCE.setDvdSelected(DVDRadioButton.isSelected());
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {
            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {

            }
        };
    }

    private void diskImageSelectEventHandler() {
        try {
            JFileChooser fileChooser = new JFileChooser(FilesHelper.getRealFolder(recentRomLocation));
            fileChooser.setDialogTitle(resourceBundle.getString("SelectDiskImageText"));
            fileChooser.setFileFilter(new IsoFileFilter());
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                setDiskImageFile(fileChooser.getSelectedFile());
        } catch (Exception e) {
            statusLbl.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    private void setDiskImageFile(File imageFile) throws Exception {
        recentRomLocation = imageFile.getParent();
        ISO9660 iso9660 = new ISO9660(imageFile);
        publisherTitle = iso9660.getTitle();

        diskImageNameLbl.setText(imageFile.getName());
        convertBtn.setEnabled(true);
        statusLbl.setText(imageFile.getAbsolutePath());

        diskImage = imageFile;
        setProposedTitle();
    }

    private void setProposedTitle() {
        String proposedName = diskImage.getName().replaceAll("(\\..*)|(\\[.*)", "").trim();
        if (proposedName.length() > 31)
            proposedName = proposedName.substring(0, 31);
        if (proposedName.length() == 0)
            proposedName = "My favorite game";
        titleField.setText(proposedName);
    }

    private void destinationSelectEventHandler() {
        JFileChooser fileChooser = new JFileChooser(FilesHelper.getRealFolder(destinationDirectoryLbl.getText()));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(resourceBundle.getString("SetDestinationDirectoryText"));
        fileChooser.setFileFilter(new IsoFileFilter());
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            setDestinationDir(fileChooser.getSelectedFile());
    }

    private void setDestinationDir(File folder) {
        destinationDirectoryLbl.setText(folder.getAbsolutePath());
    }

    private void convertButtonAction() {
        try {
            if (doWeConvertAnythingNow) {
                splitThread.interrupt();
                convertBtn.setEnabled(false);
                return;
            }

            if (titleField.getText().length() == 0) {
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new FormLayout("fill:p:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:p:noGrow,fill:max(d;4px):noGrow,fill:d:grow,left:4dlu:noGrow,fill:p:noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:p:grow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        diskImageSelectBtn = new JButton();
        diskImageSelectBtn.setBackground(new Color(-2034433));
        this.$$$loadButtonText$$$(diskImageSelectBtn, this.$$$getMessageFromBundle$$$("locale", "SelectBtn"));
        CellConstraints cc = new CellConstraints();
        mainPanel.add(diskImageSelectBtn, new CellConstraints(1, 3, 3, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 5, 0, 0)));
        titleField = new JTextField();
        mainPanel.add(titleField, cc.xyw(7, 5, 3));
        destinationDirectoryLbl = new JLabel();
        destinationDirectoryLbl.setText("");
        mainPanel.add(destinationDirectoryLbl, cc.xyw(7, 7, 3));
        diskImageNameLbl = new JLabel();
        diskImageNameLbl.setText("");
        mainPanel.add(diskImageNameLbl, cc.xy(7, 3));
        destinationSelectBtn = new JButton();
        destinationSelectBtn.setBackground(new Color(-2034433));
        this.$$$loadButtonText$$$(destinationSelectBtn, this.$$$getMessageFromBundle$$$("locale", "SelectBtn"));
        mainPanel.add(destinationSelectBtn, new CellConstraints(1, 7, 3, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 5, 0, 0)));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("locale", "ulDestinationLbl"));
        mainPanel.add(label1, cc.xy(5, 7));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("locale", "DiskImageLbl"));
        mainPanel.add(label2, cc.xy(5, 3));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("locale", "TitleLbl"));
        mainPanel.add(label3, cc.xy(5, 5));
        CDRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(CDRadioButton, this.$$$getMessageFromBundle$$$("locale", "CD"));
        mainPanel.add(CDRadioButton, cc.xy(1, 5));
        DVDRadioButton = new JRadioButton();
        DVDRadioButton.setSelected(true);
        this.$$$loadButtonText$$$(DVDRadioButton, this.$$$getMessageFromBundle$$$("locale", "DVD"));
        mainPanel.add(DVDRadioButton, cc.xy(3, 5));
        convertBtn = new JButton();
        convertBtn.setBackground(new Color(-2034433));
        this.$$$loadButtonText$$$(convertBtn, this.$$$getMessageFromBundle$$$("locale", "ConvertBtn"));
        mainPanel.add(convertBtn, new CellConstraints(1, 9, 9, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 5, 0, 5)));
        statusJPanel = new JPanel();
        statusJPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        statusJPanel.setBackground(new Color(-1));
        mainPanel.add(statusJPanel, cc.xyw(1, 12, 9));
        statusLbl = new JLabel();
        this.$$$loadLabelText$$$(statusLbl, this.$$$getMessageFromBundle$$$("locale", "WelcomeText"));
        statusJPanel.add(statusLbl, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar = new JProgressBar();
        progressBar.setBorderPainted(false);
        progressBar.setForeground(new Color(-9251843));
        progressBar.setIndeterminate(false);
        mainPanel.add(progressBar, cc.xyw(1, 11, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-9251843));
        mainPanel.add(panel1, cc.xyw(1, 1, 9));
        final JLabel label4 = new JLabel();
        label4.setIcon(new ImageIcon(getClass().getResource("/banner.png")));
        label4.setText("");
        panel1.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ulCfgBtn = new JButton();
        ulCfgBtn.setBackground(new Color(-2031648));
        ulCfgBtn.setMargin(new Insets(1, 1, 1, 1));
        ulCfgBtn.setMinimumSize(new Dimension(130, 30));
        ulCfgBtn.setPreferredSize(new Dimension(140, 30));
        this.$$$loadButtonText$$$(ulCfgBtn, this.$$$getMessageFromBundle$$$("locale", "editUlCfgBtn"));
        mainPanel.add(ulCfgBtn, new CellConstraints(9, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
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
