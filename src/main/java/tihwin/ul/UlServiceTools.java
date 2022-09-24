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

import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.*;
import java.util.List;

public class UlServiceTools {
    public static boolean verifyChunksCount(String ulCfgLocation, UlConfiguration configuration){
        int declaredChunksCount = configuration.getChunksCount();

        File[] files = collectChunks(ulCfgLocation, configuration);
        int realChunkCount = 0;

        for (File chunkFile : files) {
            if (chunkFile.exists())
                realChunkCount++;
        }

        return declaredChunksCount == realChunkCount;
    }

    private static File[] collectChunks(String ulCfgLocation, UlConfiguration configuration){
        String pattern = makePattern(ulCfgLocation, configuration);

        int declaredChunksCount = configuration.getChunksCount();
        File[] files = new File[declaredChunksCount];

        for (int i = 0; i < declaredChunksCount; i++) {
            File chunkFile = new File(String.format(pattern, i));
            files[i] = chunkFile;
        }

        return files;
    }

    private static String makePattern(String ulCfgLocation, UlConfiguration configuration){
        return ulCfgLocation +
                File.separator +
                String.format("ul.%s.%s.", configuration.getCrc32(), configuration.getPublisherTitle()) +
                "%02d";
    }

    public static void renameChunks(String ulCfgLocation,
                                       UlConfiguration oldConfiguration,
                                       UlConfiguration newConfiguration) throws Exception{
        String pattern = makePattern(ulCfgLocation, newConfiguration);

        File[] files = collectChunks(ulCfgLocation, oldConfiguration);
        for (int i = 0; i < files.length; i++){
            String fileName = String.format(pattern, i);
            Files.move(files[i].toPath(), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void removeChunks(String ulCfgLocation, UlConfiguration configuration) throws Exception{
        File[] files = collectChunks(ulCfgLocation, configuration);

        for (File file : files) {
            Files.deleteIfExists(file.toPath());
        }
    }

    public static void writeUlCfgFile(String ulCfgLocation, List<UlConfiguration> fileContent) throws Exception{
        String ulCfg = ulCfgLocation + File.separator + "ul.cfg";

        try (BufferedOutputStream stream = new BufferedOutputStream(Files.newOutputStream(Paths.get(ulCfg)))){
            for (UlConfiguration configuration: fileContent){
                stream.write(configuration.generateUlConfig());
            }
        }
    }
}
