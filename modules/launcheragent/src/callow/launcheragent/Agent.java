package callow.launcheragent;

import callow.common.IClassPatch;
import callow.common.PatchManager;
import callow.launcheragent.patch.ClientStartPatch;
import callow.launcheragent.patch.LauncherStartPatch;
import callow.launcheragent.patch.RunClientPatch;
import callow.launcheragent.patch.UpdateFilePatch;

import java.lang.instrument.Instrumentation;

import static callow.common.PropertiesFields.loadProperties;

public class Agent {
    public static ModsConfig modsConfig;

    public static void premain(String args, Instrumentation instrumentation) {
        loadProperties();
        System.out.println("[+] Agent successfully loaded.");
        instrumentation.addTransformer(new PatchManager(
                new IClassPatch[] {
                        new LauncherStartPatch(),
                        new RunClientPatch(),
                        new UpdateFilePatch(),
                        new ClientStartPatch()
                },
                false));
    }

    public static void main(String[] args) { }

}