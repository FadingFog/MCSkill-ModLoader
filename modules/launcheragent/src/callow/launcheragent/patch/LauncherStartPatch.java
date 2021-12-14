package callow.launcheragent.patch;

import callow.launcheragent.Agent;
import callow.launcheragent.ModsConfig;
import callow.common.IClassPatch;
import callow.common.PropertiesFields;
import javassist.*;
import launcher.CoM1;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LauncherStartPatch implements IClassPatch {

    @Override
    public List<String> getListPatchedClasses() {
        List<String> classes = new ArrayList<>();
        classes.add("launcher.aux");
        return classes;
    }

    @Override
    public boolean isPatchRequired() {
        return true;
    }

    @Override
    public String getPatchName() {
        return "Патч на запуск клиента";
    }

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        try {
            // Method start running after program initialization
            CtMethod method = ctClass.getDeclaredMethod("start");

            // Insert in begin method our callback method
            method.insertBefore("callow.launcheragent.patch.LauncherStartPatch.updateConfigs();");

        } catch (NotFoundException  | CannotCompileException e) {
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
