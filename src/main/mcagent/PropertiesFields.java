package main.mcagent;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class PropertiesFields {
    public enum Filter {
        FULL(0), SIMPLE(1), EXTENDED(2);

        private final int level;
        Filter(int level) {
            this.level = level;
        }

        int level(){
            return this.level;
        }
    }

    static boolean debug = false;
    static Filter filter = Filter.FULL;
    static Path modsFolderPath = FileSystems.getDefault().getPath("customMods");
    static Path excludeModsPath = FileSystems.getDefault().getPath("excludes.json");

}
