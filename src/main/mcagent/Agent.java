package main.mcagent;

import java.lang.instrument.Instrumentation;
import javassist.*;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation){
        System.out.println("Test java agent.");
    }

    public static void main(String[] args) {
        System.out.println("This file cannot be execute separately.");
    }

}