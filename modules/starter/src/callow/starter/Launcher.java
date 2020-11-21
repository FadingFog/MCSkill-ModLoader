package callow.starter;

import callow.common.PropertiesFields;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static callow.common.Utils.copyResourceFile;
import static callow.common.Utils.findMCSkillDir;

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

        PropertiesFields.loadProperties();

        Runtime runtime = Runtime.getRuntime();
        runtime.exec(String.format("cmd /c %s \"%s -javaagent:%s -jar Launcher.jar\"",
                                    PropertiesFields.launcherDebug ? "start cmd /k" : "", javaExec.toString(), LauncherAgentName),
                null, launcherDir.toFile());

        System.out.println("[+] Launcher successfully started.");
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
}
