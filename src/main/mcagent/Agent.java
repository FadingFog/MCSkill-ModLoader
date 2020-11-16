package main.mcagent;

import java.lang.instrument.Instrumentation;
import javassist.*;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation){
        System.out.println("Test java agent.");
    }

    public static void main(String[] args) {
        System.out.println("[-] Loading failed. You must load this file as agent. " +
                "\nCheck readme: https://github.com/CallowBlack/MCSkill-Custom-Mods-Loader");
        System.out.println("Press any key to continue...");
        try { System.in.read(); }
        catch(Exception ignored) {}
    }

}