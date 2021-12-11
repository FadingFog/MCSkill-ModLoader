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

        Path launcherAgent = launcherDir.resolve(LauncherAgentName);
        if (Files.exists(launcherAgent))
            Files.delete(launcherAgent);
        copyResourceFile(LauncherAgentName, launcherDir.resolve(launcherAgent));

        Path javaExecPath = findJavaExecutor(launcherDir);
        PropertiesFields.loadProperties();

        String command = String.format("cmd /c %s \"%s -javaagent:%s -jar Launcher.jar\"",
                PropertiesFields.launcherDebug ? "start cmd /k" : "",
                javaExecPath == null ? "java" : javaExecPath + "\\bin\\java.exe",
                LauncherAgentName);

        Runtime runtime = Runtime.getRuntime();
        runtime.exec(command, null, launcherDir.toFile());

        System.out.printf("[+] Command: %s.\n", command);
    }

    private static Path findJavaExecutor(Path launcherDir)
    {
        File[] directories = launcherDir.toFile().listFiles((current, name) ->
                (name.contains("jdk") || name.contains("jre")) && new File(current, name).isDirectory());
        if (directories == null)
            return null;
        if (directories.length == 0)
            return null;
        return directories[0].toPath();
    }

}
