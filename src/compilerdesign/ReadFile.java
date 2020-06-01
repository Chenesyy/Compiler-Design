package compilerdesign;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Ched
 */
public class ReadFile {

    public static PushbackInputStream ReadText(String pathDirectory) {
        FileReader inputStream = null;
        FileWriter outputStream = null;

        PushbackInputStream fr = null;
        byte[] syntax;

        Path path = Paths.get(pathDirectory); //insert path of textfile
        try {
            syntax = Files.readAllBytes(path);
            ByteArrayInputStream array = new ByteArrayInputStream(syntax);
            PushbackInputStream push = new PushbackInputStream(array);

            return push;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
