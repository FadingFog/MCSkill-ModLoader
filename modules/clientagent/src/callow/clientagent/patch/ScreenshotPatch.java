package callow.clientagent.patch;

import callow.clientagent.IClientPatch;
import javassist.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ScreenshotPatch implements IClientPatch {

    @Override
    public String getPatchName() {
        return "Patch for screenshot mod";
    }

    @Override
    public List<String> getListPatchedClasses() {
        List<String> classes = new ArrayList<>();
        classes.add("com.luffy.mixedmod.client.utils.ClientUtils");
        return classes;
    }

    @Override
    public boolean isPatchRequired() {
        return true;
    }

    @Override
    public PatchClassMode getPatchMode() {
        return PatchClassMode.ANY;
    }


    @Override
    public List<ServerInfo> getServersInfo() {
        List<ServerInfo> servers = new ArrayList<>();

        ServerInfo htc170Info = new ServerInfo("HiTechCraft", new HashMap<>());
        htc170Info.hashDependencies.put("mods/MixedMod-1.1-client-cut-final.jar",
                "657439eb8c3979aaca030ac2d4e5a883");
        servers.add(htc170Info);

        return servers;
    }

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {

        try {
            CtMethod method = ctClass.getDeclaredMethod("getScreenshot");
            method.setBody("{ callow.clientagent.patch.ScreenshotPatch.onScreenTaking(); return null; }");
        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void onScreenTaking() {
        JOptionPane.showMessageDialog(null, "Moderator trying to take screenshot. Returning null.",
                "Screenshot capture", JOptionPane.INFORMATION_MESSAGE);
    }
}
