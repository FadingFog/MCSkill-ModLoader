package callow.clientagent;

import callow.clientagent.patch.ClientStartPatcher;
import callow.clientagent.patch.HWIdPatcher;
import callow.clientagent.patch.HandshakePatcher;
import callow.common.IClassPatcher;
import callow.common.PatchInjector;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new PatchInjector(
                new IClassPatcher[] { new ClientStartPatcher(), new HandshakePatcher(), new HWIdPatcher() },
                true));
    }

    public static void main(String[] args) { }
}
