package me.brunosantana.mockserver.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReaderUtil {

    public String read(String fileName) throws IOException, URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("json/" + fileName);
        byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(bytes);
    }

}
