package callow.launcheragent.patch;

import callow.common.FileClasses;
import callow.launcheragent.Agent;
import callow.launcheragent.ModsConfig;
import callow.launcheragent.Util;
import callow.common.IClassPatch;
import javassist.*;
import javassist.bytecode.Descriptor;
import launcher.PRn;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateFilePatch implements IClassPatch {
    @Override
    public List<String> getListPatchedClasses() {
        List<String> classes = new ArrayList<>();
        classes.add("launcher.COM4");
        return classes;
    }

    @Override
    public boolean isPatchRequired() {
        return true;
    }

    @Override
    public String getPatchName() {
        return "Патч на удаление сторонних модов";
    }

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        // Method aux used for every check file
        // final Path path, final Prn prn, final InputStream inputStream
        // aux(final String s, final long n, final long n2)

        // Param for method
        try {
            // Update callback
            CtClass[] paramTypes = {
                    pool.get("java.nio.file.Path"),
                    pool.get("launcher.PRn"),
                    pool.get("java.io.InputStream"),
            };

            CtMethod method = ctClass.getMethod("aux",
                    Descriptor.ofMethod(CtPrimitiveType.voidType, paramTypes));
            method.insertBefore("if (callow.launcheragent.patch.UpdateFilePatch.isExclude($1, $2, $3)) {return;}");

        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isExclude(Path path, PRn prn, InputStream inputStream) throws IOException {
        if (path.getParent().getFileName().toString().equals("mods")){
            String clientName = path.getParent().getParent().getFileName().toString();
            String serverName = Util.ClientDirToName.get(clientName);
            String modName = path.getFileName().toString();

            ModsConfig config = Agent.modsConfig;
            config.update();

            if (Arrays.stream(config.getExcludesByServerName(serverName)).anyMatch(x -> x.getFilename().equals(modName)))
            {
                if (!FileClasses.has(path.toAbsolutePath().toString()))
                    return false;

                System.out.println("[+] Excluded file " + modName + " download prevented");
                byte[] buffer = new byte[2048];
                int n = 0;
                while (n < prn.size) {
                    final int read = inputStream.read(buffer, 0, (int)Math.min(prn.size - n, buffer.length));
                    if (read < 0) {
                        throw new EOFException(String.format("%d bytes remaining", prn.size - n));
                    }
                    n += read;
                }
                return true;
            }
        }
        return false;
    }
}
