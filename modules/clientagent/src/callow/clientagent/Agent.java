package callow.clientagent;

import callow.clientagent.patch.*;

import javax.swing.*;
import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
        ClientPatchManager manager = new ClientPatchManager(
                new IClientPatch[] {
                        new ClientStartPatch(),
                        new HandshakePatch(),
                        new HWIdPatch(),
                        new ClassesCheckPatch()
                }, true, System.getenv("SERVER_NAME"));
        instrumentation.addTransformer(manager);
        manager.ShowDependenciesHashMismatch();
    }

    public static void main(String[] args)  {

    }
}
