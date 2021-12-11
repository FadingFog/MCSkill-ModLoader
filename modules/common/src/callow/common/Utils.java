package callow.common;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
    public static void copyResourceFile(String resource, Path destination) throws IOException {
        InputStream is = Utils.class.getResourceAsStream("/" + resource);

        if (is == null) {
            System.out.println("[-] Cannot find LauncherAgent.jar file withing .jar classpath. " +
                    "Maybe you have ran program with IDEA?");
            System.out.println("[-] File path: "
                    + Thread.currentThread().getContextClassLoader().getResource("/LauncherAgent.jar"));
            return;
        }

        if (!Files.exists(destination))
            Files.createFile(destination);
        OutputStream os = new FileOutputStream(destination.toFile());

        byte[] buffer = new byte[2048];
        while (is.available() > 0){
            int len = is.read(buffer);
            os.write(buffer, 0, len);
        }
        is.close();
        os.close();
    }

    public static Path findMCSkillDir() {
        Path launcher = Paths.get(System.getenv("APPDATA")).resolve("McSkill/updates");
        if (!Files.isDirectory(launcher))
            return null;
        return launcher;
    }
}
