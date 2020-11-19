package agent.core;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ClassTransformer());
    }

    public static void main(String[] args) { }
}
