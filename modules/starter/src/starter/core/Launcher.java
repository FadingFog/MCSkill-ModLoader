package starter.core;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Launcher {

    private static final String LauncherAgentName = "LauncherAgent.jar";
    // Main class
    public static void main(String[] args) throws IOException {
        Path launcherDir = findMCSkillDir();
        if (launcherDir == null) {
            System.out.println("[-] Error: MCSkill directory isn't found.");
            return;
        }

        Path javaExec = getJVMExec(launcherDir);
        if (javaExec == null) {
            System.out.println("[-] Error: JVM file isn't found.");
            return;
        }
        Path launcherAgent = launcherDir.resolve(LauncherAgentName);
        if (Files.exists(launcherAgent))
            Files.delete(launcherAgent);
        copyResourceFile(LauncherAgentName, launcherDir.resolve(launcherAgent));

        ProcessBuilder builder = new ProcessBuilder(javaExec.toString(), "-javaagent:"+LauncherAgentName,
                                                    "-jar", "Launcher.jar");
        builder.directory(launcherDir.toFile());
        builder.start();

        System.out.println("[+] Process was started.");
    }

    public static Path findMCSkillDir() {
        Path launcher = Paths.get(System.getenv("APPDATA")).resolve("McSkill/updates");
        if (!Files.isDirectory(launcher))
            return null;
        return launcher;
    }

    public static Path getJVMExec(Path launcher) {
        final String[] javaExecs = {"java.exe", "javaw.exe"};
        Path jvmDir = launcher.resolve("jdk-win64/bin");
        if (!Files.isDirectory(jvmDir))
            return null;
        for (String javaExecName: javaExecs){
            Path javaExec = jvmDir.resolve(javaExecName);
            if (Files.exists(javaExec))
                return javaExec;
        }
        return null;
    }

    public static void copyResourceFile(String resource, Path destination) throws IOException {
        InputStream is = Launcher.class.getResourceAsStream("/" + resource);

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
}
