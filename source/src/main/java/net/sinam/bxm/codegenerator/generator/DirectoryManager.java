package net.sinam.bxm.codegenerator.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.sinam.bxm.codegenerator.config.ArgumentConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class DirectoryManager {

    final ArgumentConfig argumentConfig;

    public String prepareDirectoryForPackage(String packageName) throws FileNotFoundException {
        File projectRootDirectory = new File(argumentConfig.getOutputDirectory());
        if (!projectRootDirectory.isDirectory()) {
            throw new FileNotFoundException("Directory not found: " + argumentConfig.getOutputDirectory());
        }

        String codeRootPath = argumentConfig.getOutputDirectory()
                + File.separator
                + "src"
                + File.separator
                + "main"
                + File.separator
                + "java";

        File codeRootDirectory = new File(codeRootPath);
        if (!codeRootDirectory.isDirectory()) {
            throw new FileNotFoundException("Coding root directory not found: " + codeRootPath);
        }

        String packageFolder = codeRootPath;
        while (StringUtils.hasText(packageName)) {
            int firstIndex = packageName.indexOf(".");
            String currentFolderName;
            if (firstIndex < 0) {
                currentFolderName = packageName;
                packageName = null;
            } else {
                currentFolderName = packageName.substring(0, firstIndex);
                packageName = packageName.substring(firstIndex + 1);
            }
            packageFolder = packageFolder + File.separator + currentFolderName;
            File currentFolder = new File(packageFolder);
            if (!currentFolder.exists()) {
                currentFolder.mkdir();
            }
        }
        return packageFolder;
    }

    @SneakyThrows
    public void addFile(String directoryPath, String fileName, String fileContent) {
        File projectRootDir = new File(argumentConfig.getOutputDirectory());
        if (!projectRootDir.exists())
            projectRootDir.mkdir();
         File dirPath = new File(projectRootDir, directoryPath);
        if (!dirPath.exists())
            dirPath.mkdir();
        File file = new File(dirPath, fileName);
        file.createNewFile();

        BufferedWriter writer = Files.newBufferedWriter(file.toPath());
        writer.write(fileContent);
        writer.close();
    }
}
