package agent.core;

import javassist.NotFoundException;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation) throws NotFoundException {
        instrumentation.addTransformer(new ClassTransformer());
    }

    public static void main(String[] args) { }
}
