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
package tihwin.ul;

import tihwin.ui.UiUpdater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UlMaker implements Runnable{
    private final File fileToSplit;
    private final String ulLocation;
    private final UiUpdater updater;
    private final UlConfiguration ulConfiguration;
    private final ResourceBundle resourceBundle;

    private final String locationPattern;
    
    public UlMaker(File fileToSplit,
                   String ulLocation,
                   UlConfiguration ulConfiguration,
                   UiUpdater updater){
        this.ulLocation = ulLocation;
        this.ulConfiguration = ulConfiguration;
        this.fileToSplit = fileToSplit;
        this.updater = updater;

        this.resourceBundle = ResourceBundle.getBundle("locale");
        this.locationPattern = ulLocation+
                File.separator +
                String.format("ul.%s.%s.", ulConfiguration.getCrc32(), ulConfiguration.getPublisherTitle()) +
                "%02d";
    }

    @Override
    public void run() {
        try {
            splitToChunks();
            validateSplitFile();
            makeUlFile();
            updater.setStatus(resourceBundle.getString("SuccessText"));
        }
        catch (InterruptedException ie){
            safeShutdown();
        }
        catch (Exception e){
            updater.setStatus(resourceBundle.getString("FailedText")+" "+e.getMessage());
            e.printStackTrace();
        }
        finally {
            updater.close();
        }
    }

    private void safeShutdown(){
        boolean isDeleted = true;
        for (int i = 0; i < ulConfiguration.getChunksCount(); i++) {
            File chunkFile = new File(String.format(locationPattern, i));
            if (chunkFile.exists())
                isDeleted &= chunkFile.delete();
        }

        if (isDeleted)
            updater.setStatus(resourceBundle.getString("InterruptedAndFilesDeletedText"));
        else
            updater.setStatus(resourceBundle.getString("InterruptedAndFilesNotDeletedText"));
    }

    private void makeUlFile() throws Exception {
        byte[] config = ulConfiguration.generateUlConfig();

        RandomAccessFile raf = new RandomAccessFile(ulLocation+File.separator+"ul.cfg", "rw");
        raf.seek(raf.length());
        raf.write(config);
        raf.close();
    }
    
    private void splitToChunks() throws Exception{
        try(BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(fileToSplit.toPath()))){
            updater.incrementProgressBar(fileToSplit.length());
            
            int readBytesCount;

            main_loop:
            for (int chunkNumber = 0; ; chunkNumber++){
                String pathname = String.format(locationPattern, chunkNumber);
                BufferedOutputStream fragmentBos = new BufferedOutputStream(Files.newOutputStream(Paths.get(pathname)));

                long counter = 0;

                while (counter < 256){
                    byte[] chunk = new byte[4194304];

                    if ((readBytesCount = bis.read(chunk)) < 4194304){
                        if (readBytesCount > 0)
                            fragmentBos.write(chunk, 0, readBytesCount);
                        fragmentBos.close();
                        updater.updateProgressBySize(readBytesCount);
                        break main_loop;
                    }
                    if (interrupted())
                        throw new InterruptedException();

                    fragmentBos.write(chunk);
                    counter++;
                    updater.updateProgressBySize(readBytesCount);
                }
                fragmentBos.close();
            }
        }
    }

    private void validateSplitFile() throws Exception{
        if (interrupted())
            throw new InterruptedException(resourceBundle.getString("InterruptedText"));

        List<File> chunkFiles = new ArrayList<>();

        for (int i = 0; i < ulConfiguration.getChunksCount(); i++) {
            File chunkFile = new File(String.format(locationPattern, i));
            chunkFiles.add(chunkFile);
        }

        if (chunkFiles.size() == 0)
            throw new Exception(resourceBundle.getString("UnableCheckResultsText"));

        long totalChunksSize = 0;

        for (File chunkFile : chunkFiles) {
            totalChunksSize += chunkFile.length();
        }

        if (fileToSplit.length() != totalChunksSize)
            throw new Exception(resourceBundle.getString("SizesDifferent"));
    }

    private boolean interrupted(){
        return Thread.interrupted();
    }
    
}
