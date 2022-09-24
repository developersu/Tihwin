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
import tihwin.ui.ulupdater.*;
import tihwin.ul.UlConfiguration;
import tihwin.ul.UlServiceTools;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class UpdateUlTableUi extends JFrame {
    private JTable table;
    private UlTableModel model;
    private JButton saveChangesBtn;

    private final JLabel ulLocationLbl;
    private JLabel statusLbl;
    private String recentRomLocation;
    private final ResourceBundle resourceBundle;

    public UpdateUlTableUi(String ulDestinationLocation) {
        super();
        this.resourceBundle = ResourceBundle.getBundle("locale");
        this.ulLocationLbl = new JLabel(ulDestinationLocation);
        setupTable();
        setupSaveButton();

        FormLayout primaryPanelLayout = new FormLayout(
                "80dlu, 2dlu, fill:pref:grow",
                "fill:pref:grow, 25dlu:noGrow, 25dlu:noGrow, fill:pref:noGrow"
        );
        JPanel primaryPanel = new JPanel();
        primaryPanel.setLayout(primaryPanelLayout);

        primaryPanel.add(getScrollPane(), new CellConstraints(1, 1, 3, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));

        primaryPanel.add(getSelectUlLocationButton(), new CellConstraints(1, 2, 1, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(3, 3, 3, 3)));

        primaryPanel.add(ulLocationLbl, new CellConstraints(3, 2, 1, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));

        primaryPanel.add(saveChangesBtn, new CellConstraints(1, 3, 3, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(3, 3, 3, 3)));

        primaryPanel.add(getStatusPanel(), new CellConstraints(1, 4, 3, 1,
                CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));

        setLocationRelativeTo(null);
        setContentPane(primaryPanel);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Image img = new ImageIcon(Objects.requireNonNull(
                MainAppUi.class.getClassLoader().getResource("tray_icon.gif"))).getImage();
        setIconImage(img);
        setMinimumSize(new Dimension(800, 400));
        setVisible(true);
        setTitle(resourceBundle.getString("ulManager"));

        File ulCfgFile = new File(ulDestinationLocation + File.separator + "ul.cfg");
        this.recentRomLocation = ulDestinationLocation;
        if (ulCfgFile.exists())
            showInTableUlCfgFile(ulCfgFile);
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
        try{
            if (ulCfgFile.length() < 64){
                statusLbl.setText(resourceBundle.getString("ulManagerWindow_EmptyOrIncorrectText")+" "+ulCfgFile.getAbsolutePath());
                return;
            }

            model.clear();

            String ulCfgFileLocation = ulCfgFile.getParentFile().getAbsolutePath();

            for (int i = 0; i < ulCfgFile.length()/64; i++) {
                UlConfiguration ulConfiguration = new UlConfiguration(ulCfgFile, i);
                boolean isConsistent = UlServiceTools.verifyChunksCount(ulCfgFileLocation, ulConfiguration);
                model.addRow(new UlTableModelRecord(ulConfiguration, isConsistent));
            }

            saveChangesBtn.setEnabled(true);
            statusLbl.setText(ulCfgFile.getAbsolutePath());
        }
        catch (Exception e){
            statusLbl.setText(resourceBundle.getString("ulManagerWindow_EmptyOrIncorrectText")+" "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveChangesAction(){
        try{
            String ulLocation = ulLocationLbl.getText();
            
            List<UlTableModelRecord> modelRecords = model.getInitialRows();
            List<UlConfiguration> finalConfigurationSet = new ArrayList<>();
            // Collect what we'll have in the final ul.cfg file
            for (int i = 0; i < modelRecords.size(); i++){
                UlConfiguration configuration = new UlConfiguration(
                        model.getTitle(i),
                        model.getPublisherTitle(i),
                        model.getChunksCount(i),
                        model.getCdDvd(i).equals("DVD")
                );
                finalConfigurationSet.add(configuration);
            }
            // Updating chunk file names if needed
            for (int i = 0; i < modelRecords.size(); i++){
                UlTableModelRecord initialRecord = modelRecords.get(i);
                String initialRecordTitle = initialRecord.getConfiguration().getTitle();
                if (initialRecord.isConsistent() && ! model.getTitle(i).equals(initialRecordTitle)){
                    UlServiceTools.renameChunks(ulLocation,
                            initialRecord.getConfiguration(),
                            finalConfigurationSet.get(i));
                }
            }
            // Remove chunks in case user removed record from the table
            List<UlTableModelRecord> removedRows = model.getRemovedRows();
            for (UlTableModelRecord removedRow : removedRows) {
                UlServiceTools.removeChunks(ulLocation, removedRow.getConfiguration());
            }
            // Write new ul.cfg
            UlServiceTools.writeUlCfgFile(ulLocation, finalConfigurationSet);
            File ulCfgFile = new File(ulLocation+File.separator+"ul.cfg");
            showInTableUlCfgFile(ulCfgFile);

            statusLbl.setText(resourceBundle.getString("SuccessText"));
        }
        catch (Exception e){
            statusLbl.setText(resourceBundle.getString("ulManagerWindow_SaveChangesFailureText")+" "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void selectUlCfgAction(){
        try {
            JFileChooser fileChooser = new JFileChooser(FilesHelper.getRealFolder(recentRomLocation));
            fileChooser.setDialogTitle(resourceBundle.getString("ulManagerWindow_SelectUlCfgBtn"));
            fileChooser.setFileFilter(new UlCfgFileFilter());
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