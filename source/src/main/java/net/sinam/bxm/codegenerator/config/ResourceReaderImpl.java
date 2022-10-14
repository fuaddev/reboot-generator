package net.sinam.bxm.codegenerator.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ResourceReaderImpl implements ResourceReader {
    private static InputStream getResourceFileAsInputStream(String fileName) {
        ClassLoader classLoader = ResourceReaderImpl.class.getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }

    public String readAllContent(String resourceUrl) {
        InputStream resourceStream;
        try {

            resourceStream = getResourceFileAsInputStream(resourceUrl);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resourceStream))) {
                return reader.lines()
                        .collect(Collectors.joining(Constants.NewLine));
            }
        } catch (IOException e) {
            return null;
        }
    }
}
