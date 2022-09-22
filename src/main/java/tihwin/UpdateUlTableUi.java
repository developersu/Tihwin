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
package tihwin;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import tihwin.ui.*;
import tihwin.ui.ulupdater.*;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.ResourceBundle;

public class UpdateUlTableUi extends JFrame {
    private JTable table;
    private UlTableModel model;
    private JButton saveChangesBtn;

    private JLabel ulLocationLbl, statusLbl;
    private String recentRomLocation;
    private final ResourceBundle resourceBundle;

    public UpdateUlTableUi(File ulCfgFile) {
        super();
        this.resourceBundle = ResourceBundle.getBundle("locale");
        setupUlLocationLbl();
        setupTable();
        setupSaveButton();

        FormLayout primaryPanelLayout = new FormLayout(
                "75dlu, 4dlu, fill:pref:grow",
                "fill:pref:grow, 25dlu:noGrow, 25dlu:noGrow, fill:pref:noGrow"
        );
        JPanel primaryPanel = new JPanel();
        primaryPanel.setLayout(primaryPanelLayout);

        primaryPanel.add(getScrollPane(), new CellConstraints(1, 1, 3, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        primaryPanel.add(getSelectUlLocationButton(), new CellConstraints(1, 2, 1, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(5, 5, 5, 5)));
        primaryPanel.add(ulLocationLbl, new CellConstraints(3, 2, 1, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        primaryPanel.add(saveChangesBtn, new CellConstraints(1, 3, 3, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(5, 5, 5, 5)));
        primaryPanel.add(getStatusPanel(), new CellConstraints(1, 4, 3, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));

        setLocationRelativeTo(null);
        setContentPane(primaryPanel);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Image img = new ImageIcon(Objects.requireNonNull(
                MainAppUi.class.getClassLoader().getResource("tray_icon.gif"))).getImage();
        setIconImage(img);
        setMinimumSize(new Dimension(800, 500));
        setVisible(true);
        setTitle(resourceBundle.getString("ulManager"));
        System.out.println(ulCfgFile.getAbsolutePath());
        this.recentRomLocation = ulCfgFile.getParent();
        if (ulCfgFile.exists())
            showInTableUlCfgFile(ulCfgFile);
        setAlwaysOnTop(true); // TODO:DELETE
        //statusLbl.setText("TEST"); // TODO:DELETE
    }

    private void setupUlLocationLbl(){
        ulLocationLbl = new JLabel(Settings.INSTANCE.getDestination());
    }
    private void setupSaveButton(){
        saveChangesBtn = new JButton(resourceBundle.getString("ulManagerWindow_SaveBtn"));
        saveChangesBtn.setBackground(Color.getHSBColor(0.5591398f, 0.12156863f, 1));
        saveChangesBtn.addActionListener(actionEvent -> saveChangesAction());
        saveChangesBtn.setEnabled(false);
    }
    private void setupTable(){
        model = new UlTableModel();
        table = new JTable(model);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new UlTableHeaderRenderer());
        header.setPreferredSize(new Dimension(header.getHeight(), 25));

        table.setDefaultRenderer(JButton.class, new UlTableContentJButtonRenderer());
        table.setDefaultRenderer(JLabel.class, new UlTableContentJLabelRenderer());

        table.setRowHeight(30);
        UlTableColumnModel columnModel = new UlTableColumnModel((DefaultTableColumnModel) table.getColumnModel());
        table.setColumnModel(columnModel);
        table.setRowSelectionAllowed(false);

        JTextField textField = new JTextField();
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new TitleFieldFilter());
        table.setDefaultEditor(JLabel.class, new DefaultCellEditor(textField));
    }
    private JScrollPane getScrollPane(){
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0,50));
        return scrollPane;
    }
    private JButton getSelectUlLocationButton(){
        JButton selectUlBtn = new JButton(resourceBundle.getString("ulManagerWindow_SelectUlCfgBtn"));
        selectUlBtn.setBackground(Color.getHSBColor(0.5591398f, 0.12156863f, 1));
        selectUlBtn.addActionListener(actionEvent -> selectUlCfgAction());
        return selectUlBtn;
    }
    private JPanel getStatusPanel(){
        statusLbl = new JLabel();
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(Color.white);
        statusPanel.setBorder(new MatteBorder(1,0,0,0, Color.darkGray));
        statusPanel.add(statusLbl);
        return statusPanel;
    }

    private void showInTableUlCfgFile(File ulCfgFile){
        for (int i=0; i<15; i++) {
            model.addRow(
                    new UlTableRow(table.getRowCount() + 1, i + "_lent Hi11 **atter** ***ories1", "NORG_000.00", 5, true));
            model.addRow(
                    new UlTableRow(table.getRowCount() + 1, "name"+i, "NORG_000.11", 5, false));
            model.addRow(
                    new UlTableRow(table.getRowCount() + 1, i+"name", "NORG_000.99", 5, true));
        }
        /*-*-*-*-*-*-*-*-**/
        if (ulCfgFile.length() < 64){
            statusLbl.setText(resourceBundle.getString("ulManagerWindow_EmptyOrIncorrectText")+" "+ulCfgFile.getAbsolutePath());
            return;
        }
        System.out.println("IMPLMENET ME"); // TODO

        saveChangesBtn.setEnabled(true);
        statusLbl.setText(ulCfgFile.getAbsolutePath());
    }

    private void saveChangesAction(){
        System.out.println("IMPLMENET ME"); // TODO
    }

    private void selectUlCfgAction(){
        try {
            JFileChooser fileChooser = new JFileChooser(FilesHelper.getRealFolder(recentRomLocation));
            fileChooser.setDialogTitle(resourceBundle.getString("ulManagerWindow_SelectUlCfgBtn"));
            fileChooser.setFileFilter(new ulCfgFileFilter());
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                recentRomLocation = file.getParent();
                showInTableUlCfgFile(file);
            }
        } catch (Exception e) {
            statusLbl.setText(e.getMessage());
            e.printStackTrace();
        }
    }
}