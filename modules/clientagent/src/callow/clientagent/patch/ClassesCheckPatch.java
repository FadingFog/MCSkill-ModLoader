package callow.clientagent.patch;

import callow.clientagent.IClientPatch;
import callow.common.IClassPatch;
import javassist.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClassesCheckPatch implements IClientPatch {

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        if (ctClass.getName().equals("ic2.core.network.CsReplyPacket"))
            try {
                CtConstructor method = ctClass.getConstructor("(I[BI)V");
                method.insertBeforeBody("callow.clientagent.patch.ClassesCheckPatch.onReplyPacket($2);");
            } catch (NotFoundException | CannotCompileException e) {
                e.printStackTrace();
                return false;
            }
        else if (ctClass.getName().equals("ic2.core.network.CsChPacket")) {
            try {
                CtConstructor method = ctClass.getConstructor("(II[B)V");
                method.insertBeforeBody("callow.clientagent.patch.ClassesCheckPatch.onChPacket($3);");
            } catch (NotFoundException | CannotCompileException e) {
                e.printStackTrace();
                return false;
            }
        }
//        else if (ctClass.getName().equals("ic2.core.network.ThreadIC2")) {
//            try {
//                CtMethod method = ctClass.getDeclaredMethod("run");
//                method.setBody("{}");
//            } catch (NotFoundException | CannotCompileException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
        return true;
    }

    @Override
    public List<String> getListPatchedClasses() {
        List<String> classes = new ArrayList<>();
        classes.add("ic2.core.network.CsReplyPacket");
        classes.add("ic2.core.network.CsChPacket");
        classes.add("ic2.core.network.ThreadIC2");
        return classes;
    }

    @Override
    public boolean isPatchRequired() {
        return true;
    }

    @Override
    public String getPatchName() {
        return "Патч на подмену состава классов";
    }

    @Override
    public List<String> getServersNames() {
        List<String> servers = new ArrayList<>();
        servers.add("HiTech 1.12.2");
        servers.add("HiTechCraft");
        return servers;
    }

    public static void onChPacket(byte[] data)
    {
        File file = new File("C:\\Users\\strog\\Desktop\\Hack templates\\MCSkill\\IC 1.12.2 Patch\\data.txt");
        writeDataToFile(data, file);
    }

    public static void onReplyPacket(byte[] data)
    {
        File file = new File("C:\\Users\\strog\\Desktop\\Hack templates\\MCSkill\\IC 1.12.2 Patch\\hashes.txt");
        writeDataToFile(data, file);
    }

    private static void writeDataToFile(byte[] data, File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write(new String(data, StandardCharsets.UTF_8));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
