package callow.launcheragent.patch;

import callow.launcheragent.Agent;
import callow.launcheragent.ModsConfig;
import callow.common.IClassPatcher;
import callow.common.PropertiesFields;
import javassist.*;
import launcher.CoM1;

import java.io.IOException;
import java.nio.file.Files;

public class LauncherStartPatcher implements IClassPatcher {
    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        if (!ctClass.getName().equals("launcher.aux"))
            return false;
        try {
            // Method start running after program initialization
            CtMethod method = ctClass.getDeclaredMethod("start");

            // Insert in begin method our callback method
            method.insertBefore("callow.launcheragent.patch.LauncherStartPatcher.updateConfigs();");

            System.out.println("[+] Launcher | aux.start(): Patch was created.");

        } catch (NotFoundException  | CannotCompileException e) {
            System.out.println("[-] Launcher | aux.start(): Patch creation failed.");
            return false;
        }
        return true;
    }

    public static void updateConfigs() {
        CoM1.setDebugEnabled(PropertiesFields.clientDebug);
        try {
            Agent.modsConfig = new ModsConfig(PropertiesFields.modJSONConfig.toFile());
        } catch (IOException e) {
            System.out.println("[-] Loading mods config file was failed.");
            e.printStackTrace();
        }

        if (!Files.exists(PropertiesFields.includeModsDir)) {
            try {
                Files.createDirectories(PropertiesFields.includeModsDir);
                System.out.println("[+] Custom mods directory was created.");
            } catch (IOException e) {
                System.out.println("[-] Couldn't custom mods directory.");
            }
        }
    }
}
