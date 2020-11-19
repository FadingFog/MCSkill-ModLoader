package common;

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
    public static boolean debug = false;
    public static boolean console = false;
    public static Path modsFolderPath = FileSystems.getDefault().getPath("customMods");
    public static Path customModsConfig = FileSystems.getDefault().getPath("customMods.json");

    public static void loadProperties(){
        Properties props = new Properties();
        Path mcSkillDir = Objects.requireNonNull(Utils.findMCSkillDir());
        File configFile = mcSkillDir.resolve(propertiesFilename).toFile();
        try {
            props.load(new FileInputStream(configFile));
            PropertiesFields.debug = Boolean.parseBoolean(props.getProperty("debug"));
            PropertiesFields.console = Boolean.parseBoolean(props.getProperty("console"));
            PropertiesFields.customModsConfig = FileSystems.getDefault().getPath(props.getProperty("customModsConfig"));
            PropertiesFields.modsFolderPath = FileSystems.getDefault().getPath(props.getProperty("modsFolder"));
            System.out.println("[+] Config was read successfully.");
        } catch (IOException readException) {
            System.out.println("[-] File with config wasn't found.");
            readException.printStackTrace();
            try {
                props.setProperty("console", Boolean.toString(false));
                props.setProperty("debug", Boolean.toString(PropertiesFields.debug));
                props.setProperty("customModsConfig", PropertiesFields.customModsConfig.toString());
                props.setProperty("modsFolder", PropertiesFields.modsFolderPath.toString());
                props.store(new FileOutputStream(configFile), null);
                System.out.println("[+] New config file has created.");
            } catch (IOException writeException){
                System.out.println("[-] Filed writing config file.");
                writeException.printStackTrace();
            }
        }
    }
}
