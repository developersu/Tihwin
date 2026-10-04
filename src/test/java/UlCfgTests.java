import tihwin.ul.UlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class UlCfgTests {

    public static void main(String[] args) throws Exception {
        UlCfgTests test = new UlCfgTests();
        test.createBigUlCfgTest();
    }

    void createBigUlCfgTest() throws Exception {
        List<UlConfiguration> configs = new ArrayList<>();
        for (byte i = 1; i < 31; i++) {
            configs.add(new UlConfiguration("Application test name #" + i, "SCUS_974.71", i, true));
        }

        String location = System.getProperty("java.io.tmpdir") + File.separator + "ul.cfg";

        try (RandomAccessFile raf = new RandomAccessFile(location, "rw")) {
            raf.seek(raf.length());
            configs.forEach(config -> {
                try {
                    raf.write(config.generateUlConfig());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        System.out.println("Saved to: " + location);
    }
}
