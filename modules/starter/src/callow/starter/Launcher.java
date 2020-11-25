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

        PropertiesFields.loadProperties();

        Runtime runtime = Runtime.getRuntime();
        runtime.exec(String.format("cmd /c %s \"java -javaagent:%s -jar Launcher.jar\"",
                                    PropertiesFields.launcherDebug ? "start cmd /k" : "", LauncherAgentName),
                null, launcherDir.toFile());

        System.out.println("[+] Launcher successfully started.");
    }
}
