package callow.launcheragent.patch;

import callow.launcheragent.Agent;
import callow.launcheragent.ModsConfig;
import javassist.*;
import callow.common.IClassPatcher;
import callow.common.PropertiesFields;

import java.io.IOException;
import java.nio.file.Files;

public class LauncherStartPatcher implements IClassPatcher {
    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        if (!ctClass.getName().equals("o.aux"))
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
        try {
            Agent.modsConfig = new ModsConfig(PropertiesFields.customModsConfig.toFile());
        } catch (IOException e) {
            System.out.println("[-] Loading mods config file was failed.");
            e.printStackTrace();
        }

        if (!Files.exists(PropertiesFields.modsFolderPath)) {
            try {
                Files.createDirectories(PropertiesFields.modsFolderPath);
                System.out.println("[+] Custom mods directory was created.");
            } catch (IOException e) {
                System.out.println("[-] Couldn't custom mods directory.");
            }
        }
    }
}
