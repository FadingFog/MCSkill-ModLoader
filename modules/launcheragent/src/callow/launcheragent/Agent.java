package callow.launcheragent;

import callow.common.IClassPatcher;
import callow.common.PatchInjector;
import callow.launcheragent.patch.LauncherStartPatcher;
import callow.launcheragent.patch.RunClientPatcher;
import callow.launcheragent.patch.UpdateFilePatcher;

import java.lang.instrument.Instrumentation;

import static callow.common.PropertiesFields.loadProperties;

public class Agent {
    public static ModsConfig modsConfig;

    public static void premain(String args, Instrumentation instrumentation) {
        loadProperties();
        System.out.println("[+] Agent successfully loaded.");
        instrumentation.addTransformer(new PatchInjector(
                new IClassPatcher[] { new LauncherStartPatcher(), new RunClientPatcher(), new UpdateFilePatcher() },
                false));
    }

    public static void main(String[] args) { }

}