package launcher.core;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class PropertiesFields {
    static boolean debug = false;
    static Path modsFolderPath = FileSystems.getDefault().getPath("customMods");
    static Path customModsConfig = FileSystems.getDefault().getPath("customMods.json");

}
