package agent.core;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("Agent in the house");
    }

    public static void main(String[] args) { }
}
