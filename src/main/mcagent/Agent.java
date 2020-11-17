package main.mcagent;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.FileSystems;
import java.util.Properties;

public class Agent {

    static String propertiesFilename = "mcAgent.cfg";

    public static void premain(String args, Instrumentation instrumentation) {
        loadProperties();
        System.out.println("[+] Agent successfully loaded.");
        instrumentation.addTransformer(new ClassTransformer());
    }

    public static void loadProperties(){
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propertiesFilename));
            PropertiesFields.debug = Boolean.parseBoolean(props.getProperty("debug"));
            PropertiesFields.excludeModsPath = FileSystems.getDefault().getPath(props.getProperty("excludeModsFile"));
            PropertiesFields.modsFolderPath = FileSystems.getDefault().getPath(props.getProperty("modsFolder"));
            System.out.println("[+] Config was read successfully.");
        } catch (IOException readException) {
            System.out.println("[-] File with config wasn't found.");
            readException.printStackTrace();
            try {
                props.setProperty("debug", Boolean.toString(PropertiesFields.debug));
                props.setProperty("excludeModsFile", PropertiesFields.excludeModsPath.toString());
                props.setProperty("modsFolder", PropertiesFields.modsFolderPath.toString());
                props.store(new FileOutputStream(propertiesFilename), null);
                System.out.println("[+] New config file has created.");
            } catch (IOException writeException){
                System.out.println("[-] Filed writing config file.");
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