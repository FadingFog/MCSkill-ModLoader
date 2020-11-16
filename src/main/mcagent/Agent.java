package main.mcagent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.FileSystems;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Agent {

    static String propertiesFilename = "mcAgent.cfg";
    public static void premain(String args, Instrumentation instrumentation){
        PrintStream myStream = new PrintStream(System.out) {
            @Override
            public void println(String line) {
                Pattern pattern = Pattern.compile("^\\[D(\\d)] ([^\\n]+)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches())
                    line = matcher.group(2);

                if (PropertiesFields.filter.level() > 0){
                    if (matcher.matches())
                    {
                        int level = Integer.parseInt(matcher.group(1));
                        if (level <= PropertiesFields.filter.level()) {
                            super.println(line);
                        }
                    }
                }
                else {
                    super.println(line);
                }
            }
        };
        System.setOut(myStream);

        loadProperties();
        System.out.println("[D1] [+] Agent successfully loaded.");
    }

    public static void loadProperties(){
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propertiesFilename));
            PropertiesFields.debug = Boolean.parseBoolean(props.getProperty("debug"));
            PropertiesFields.excludeModsPath = FileSystems.getDefault().getPath(props.getProperty("excludeModsFile"));
            PropertiesFields.modsFolderPath = FileSystems.getDefault().getPath(props.getProperty("modsFolder"));
            PropertiesFields.filter = PropertiesFields.Filter.valueOf(props.getProperty("filter"));
            System.out.println("[D1] [+] Config was read successfully.");
        } catch (IOException readException) {
            System.out.println("[D1] [-] File with config wasn't found.");
            System.out.println("[D1] Error: " + readException.getMessage());
            readException.printStackTrace();
            try {
                props.setProperty("debug", Boolean.toString(PropertiesFields.debug));
                props.setProperty("excludeModsFile", PropertiesFields.excludeModsPath.toString());
                props.setProperty("modsFolder", PropertiesFields.modsFolderPath.toString());
                props.setProperty("filter", PropertiesFields.filter.name());
                props.store(new FileOutputStream(propertiesFilename), "filter can be SIMPLE, EXTENDED or FULL");
                System.out.println("[D1] [+] New config file has created.");
            } catch (IOException writeException){
                System.out.println("[D1] [-] Filed writing config file.");
                System.out.println("[D1] Error: " + writeException.getMessage());
                writeException.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("[-] Loading failed. You must load this file as agent. " +
                "\nCheck readme: https://github.com/CallowBlack/MCSkill-Custom-Mods-Loader");
        System.out.println("Press any key to continue...");
        try { System.in.read(); }
        catch(Exception ignored) {}
    }

}