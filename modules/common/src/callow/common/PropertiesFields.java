package callow.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class PropertiesFields {
    static final String propertiesFilename = "customLauncher.ini";
    public static boolean clientDebug = false;
    public static boolean launcherDebug = false;
    public static Path includeModsDir = FileSystems.getDefault().getPath("customMods");
    public static Path modJSONConfig = FileSystems.getDefault().getPath("customMods.json");

    public static void loadProperties(){
        Properties props = new Properties();
        Path mcSkillDir = Objects.requireNonNull(Utils.findMCSkillDir());
        File configFile = mcSkillDir.resolve(propertiesFilename).toFile();
        try {
            props.load(new FileInputStream(configFile));
            PropertiesFields.clientDebug = Boolean.parseBoolean(props.getProperty("clientDebug"));
            PropertiesFields.launcherDebug = Boolean.parseBoolean(props.getProperty("launcherDebug"));
            PropertiesFields.modJSONConfig = FileSystems.getDefault().getPath(props.getProperty("modJSONConfig"));
            PropertiesFields.includeModsDir = FileSystems.getDefault().getPath(props.getProperty("includeModsDir"));
            System.out.println("[+] Config was read successfully.");
        } catch (IOException readException) {
            System.out.println("[-] File with config wasn't found.");
            readException.printStackTrace();
            try {
                props.setProperty("launcherDebug", Boolean.toString(PropertiesFields.launcherDebug));
                props.setProperty("clientDebug", Boolean.toString(PropertiesFields.clientDebug));
                props.setProperty("modJSONConfig", PropertiesFields.modJSONConfig.toString());
                props.setProperty("includeModsDir", PropertiesFields.includeModsDir.toString());
                props.store(new FileOutputStream(configFile), null);
                System.out.println("[+] New config file has created.");
            } catch (IOException writeException){
                System.out.println("[-] Filed writing config file.");
                writeException.printStackTrace();
            }
        }
    }
}
