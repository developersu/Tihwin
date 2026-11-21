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

import tihwin.ui.*;
import tihwin.ui.model.LocaleHolder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.ResourceBundle.getBundle;

public class MainAppUi extends JFrame {

    private final MainUiBuilder.Parts ui;
    private final ConversionController conversion;

    private final Texts texts;

    private String recentRomLocation;

    public MainAppUi(String appName) {
        super(appName);
        this.texts = new Texts();
        this.ui = MainUiBuilder.build(texts::text);
        this.conversion = new ConversionController(new UiPort());

        configureComponents();
        setupKeyboardShortcuts();
        setLocationRelativeTo(null);
        new FilesDropListener(ui.mainPanel, this);
        setContentPane(ui.mainPanel);
        ScaleUi.applyInitialScale(ui.scalableComponents);
    }

    private void configureComponents() {
        Border selectBtnBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.gray),
                BorderFactory.createEmptyBorder(5, 5, 5, 5));
        ui.diskImageSelectBtn.setBorder(selectBtnBorder);
        ui.destinationSelectBtn.setBorder(selectBtnBorder);
        ui.ulCfgBtn.setBorder(selectBtnBorder);
        ui.diskImageSelectBtn.addMouseListener(new TwButtonsActionListener());
        ui.destinationSelectBtn.addMouseListener(new TwButtonsActionListener());
        ui.titleField.setBorder(new LineBorder(Color.lightGray));
        ui.statusJPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.darkGray));

        ui.convertBtn.setEnabled(false);
        ui.diskImageSelectBtn.addActionListener(e -> diskImageSelectEventHandler());
        ui.destinationSelectBtn.addActionListener(e -> destinationSelectEventHandler());
        ui.convertBtn.addActionListener(e -> conversion.onConvertClicked());
        ui.ulCfgBtn.addActionListener(e -> ulConfigButtonAction());
        ui.zoomInBtn.addActionListener(e -> zoomIn());
        ui.zoomOutBtn.addActionListener(e -> zoomOut());
        ui.ulLangComboBox.addActionListener(e -> onLanguageChanged());
        ((AbstractDocument) ui.titleField.getDocument()).setDocumentFilter(new TitleFieldFilter());

        if (Settings.INSTANCE.getDvdSelected())
            ui.DVDRadioButton.setSelected(true);
        else
            ui.CDRadioButton.setSelected(true);
        recentRomLocation = Settings.INSTANCE.getRomLocation();
        ui.destinationDirLbl.setText(FilesHelper.getRealFolder(Settings.INSTANCE.getDestination()));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Settings.INSTANCE.setRomLocation(recentRomLocation);
                Settings.INSTANCE.setDestination(ui.destinationDirLbl.getText());
                Settings.INSTANCE.setDvdSelected(ui.DVDRadioButton.isSelected());
                Settings.INSTANCE.setLocale(((LocaleHolder) ui.ulLangComboBox.getSelectedItem()).getLocaleCode());
            }
        });
    }

    private void setupKeyboardShortcuts() {
        InputMap inputMap = ui.mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
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
        ui.mainPanel.getActionMap().put("zoomIn", zoomAction);
        ui.mainPanel.getActionMap().put("zoomOut", zoomAction);
    }

    private void zoomIn() {
        ScaleUi.increaseScale(ui.scalableComponents);
        pack();
    }

    private void zoomOut() {
        ScaleUi.decreaseScale(ui.scalableComponents);
        pack();
    }

    private void diskImageSelectEventHandler() {
        File diskImageFile = new TwFileChooser(recentRomLocation,
                texts.text("SelectDiskImageText"), true).getFile();
        if (diskImageFile != null) {
            recentRomLocation = diskImageFile.getParent();
            conversion.setDiskImage(diskImageFile);
        }
    }

    public void setDiskImageFile(File diskImageFile) {
        recentRomLocation = diskImageFile.getParent();
        conversion.setDiskImage(diskImageFile);
    }

    private void destinationSelectEventHandler() {
        File destination = new TwFileChooser(ui.destinationDirLbl.getText(),
                texts.text("SetDestinationDirectoryText"), false).getFile();
        if (destination != null)
            setDestinationDir(destination);
    }

    public void setDestinationDir(File folder) {
        ui.destinationDirLbl.setText(folder.getAbsolutePath());
    }

    private void ulConfigButtonAction() {
        new UpdateUlTableUi(ui.destinationDirLbl.getText());
    }

    private void onLanguageChanged() {
        Locale newLocale = ((LocaleHolder) requireNonNull(ui.ulLangComboBox.getSelectedItem())).getLocale();
        Locale.setDefault(newLocale);
        retranslate();
    }

    private void retranslate() {
        texts.refresh();
        for (Map.Entry<String, Consumer<String>> entry : ui.translatableComponents)
            entry.getValue().accept(texts.text(entry.getKey()));
        // convert button text depends on the conversion state
        ui.convertBtn.setText(texts.text(conversion.isConvertingNow() ? "AbortText" : "ConvertBtn"));
        if (conversion.isConvertingNow())
            ui.statusLbl.setText(texts.text("InProgressText"));
    }

    /**
     * Wraps the translation source: the bundle is re-loaded on language change.
     */
    private static final class Texts {
        private ResourceBundle bundle = getBundle("locale");

        String text(String key) {
            return bundle.getString(key);
        }

        void refresh() {
            bundle = getBundle("locale");
        }
    }

    /**
     * Wires the controller to the UI without exposing the frame to it.
     */
    private final class UiPort implements ConversionPort {
        @Override
        public void setStatusText(String text) {
            ui.statusLbl.setText(text);
        }

        @Override
        public void setLocalizedStatus(String key) {
            ui.statusLbl.setText(texts.text(key));
        }

        @Override
        public void setDiskImageName(String name) {
            ui.diskImageNameLbl.setText(name);
        }

        @Override
        public void setConvertButtonEnabled(boolean enabled) {
            ui.convertBtn.setEnabled(enabled);
        }

        @Override
        public void setConvertButtonText(String key) {
            ui.convertBtn.setText(texts.text(key));
        }

        @Override
        public void setTitle(String title) {
            ui.titleField.setText(title);
        }

        @Override
        public JProgressBar progressBar() {
            return ui.progressBar;
        }

        @Override
        public JLabel statusLabel() {
            return ui.statusLbl;
        }

        @Override
        public String titleFieldText() {
            return ui.titleField.getText();
        }

        @Override
        public String destinationDir() {
            return ui.destinationDirLbl.getText();
        }

        @Override
        public boolean isDvdSelected() {
            return ui.DVDRadioButton.isSelected();
        }
    }
}
