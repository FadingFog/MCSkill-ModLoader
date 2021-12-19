package callow.launcheragent;

import callow.common.IClassPatch;
import callow.common.PatchManager;
import callow.common.Utils;
import callow.launcheragent.patch.LauncherStartPatch;
import callow.launcheragent.patch.RunClientPatch;
import callow.launcheragent.patch.UpdateFilePatch;

import javax.swing.*;
import java.lang.instrument.Instrumentation;
import java.util.Objects;

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
                        new UpdateFilePatch() },
                false));
        ShowLauncherHashMismatch();
    }

    public static void ShowLauncherHashMismatch() {
        if (!Objects.equals(Utils.getMD5Checksum("Launcher.jar"), "b5ecba726daec152af9bbd2c7bce34b1"))
            JOptionPane.showMessageDialog(null, "Launcher hash mismatch. Patches may be unsuitable.",
                    "Hash mismatch detected", JOptionPane.ERROR_MESSAGE);
    }
    public static void main(String[] args) { }

}