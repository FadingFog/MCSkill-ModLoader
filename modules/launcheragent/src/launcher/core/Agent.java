package launcher.core;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.FileSystems;
import java.util.Properties;

import static common.PropertiesFields.loadProperties;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
        loadProperties();
        System.out.println("[+] Agent successfully loaded.");
        instrumentation.addTransformer(new ClassTransformer());
    }


    public static void main(String[] args) {
        System.out.println("[-] Loading failed. You must load this file as agent. " +
                "\nCheck readme: https://github.com/CallowBlack/MCSkill-Custom-Mods-Loader");
        System.out.println("Press any key to continue...");
        try { System.in.read(); }
        catch(Exception ignored) {}
    }

}