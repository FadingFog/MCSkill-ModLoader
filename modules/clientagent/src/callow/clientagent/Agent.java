package callow.clientagent;

import callow.clientagent.patch.ClassesCheckPatch;
import callow.clientagent.patch.ClientStartPatch;
import callow.clientagent.patch.HWIdPatch;
import callow.clientagent.patch.HandshakePatch;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ClientPatchManager(
                new IClientPatch[] {
                        new ClientStartPatch(),
                        new HandshakePatch(),
                        new HWIdPatch(),
                        new ClassesCheckPatch()
                },
                true,
                System.getenv("SERVER_NAME")));
    }

    public static void main(String[] args) { }
}
