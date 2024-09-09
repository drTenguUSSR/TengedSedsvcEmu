/*
 *
 * @author DrTengu. 2024/09
 */

package mil.teng.q2024.sedsvc.emu.via.kafka.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class TengUtils {

    private static final Class<?> clazz = TengUtils.class;

    public static void copyResourceTo(String fileName, String srcFolder, File resultFolder) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(resultFolder, fileName))) {
            try (InputStream inputStream = clazz.getResourceAsStream("/" + srcFolder + "/" + fileName)) {
                if (inputStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        File dat1 = new File(resultFolder, fileName);
        log.debug("fakeCopy: {} -> {}, size={}", fileName, dat1.getAbsolutePath(), dat1.length());
    }

    public static File getTempFolder() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    public static Path makeTempSubfolder(String folderSuffix) throws IOException {
        return makeTempSubfolder(getTempFolder().getAbsolutePath(), folderSuffix);
    }

    public static Path makeTempSubfolder(String baseFolder, String tmpFolderPrefix) throws IOException {
        log.debug("makeTempSubfolder: baseFolder={}, tmpFolderPrefix={}", baseFolder, tmpFolderPrefix);
        if (!StringUtils.hasText(baseFolder)) {
            throw new IllegalArgumentException("baseFolder must not be null or empty");
        }
        File baseFolderFile = new File(baseFolder);
        if (!baseFolderFile.exists() || !baseFolderFile.isDirectory()) {
            throw new IllegalStateException("base folder [" + baseFolder + "] not exist or not directory");
        }
        Path base = Paths.get(baseFolder);
        String prefix = tmpFolderPrefix + "-";
        Path resultFolder = Files.createTempDirectory(base, prefix);
        File resFile = resultFolder.toFile();
        if (!resFile.exists()) {
            throw new IllegalStateException("failed to create temp dir [" + baseFolder + "]"
                    + " for [" + tmpFolderPrefix + "]");
        }
        if (!resFile.isDirectory()) {
            throw new IllegalStateException("temp dir [" + baseFolder + "]"
                    + " for [" + tmpFolderPrefix + "] not a directory");
        }
        return resultFolder;
    }
}
