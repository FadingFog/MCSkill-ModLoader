package callow.launcheragent.patch;

import callow.launcheragent.Agent;
import callow.launcheragent.ModsConfig;
import callow.launcheragent.Util;
import javassist.*;
import javassist.bytecode.Descriptor;
import launcher.Prn;
import callow.common.IClassPatcher;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;

public class UpdateFilePatcher implements IClassPatcher {
    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        // File updating class
        if (!ctClass.getName().equals("launcher.Com4"))
            return false;

        // Method aux used for every check file
        // final Path path, final Prn prn, final InputStream inputStream
        // aux(final String s, final long n, final long n2)

        // Param for method
        try {
            // Update callback
            CtClass[] paramTypes = {
                    pool.get("java.nio.file.Path"),
                    pool.get("launcher.Prn"),
                    pool.get("java.io.InputStream"),
            };

            CtMethod method = ctClass.getMethod("aux",
                    Descriptor.ofMethod(CtPrimitiveType.voidType, paramTypes));
            method.insertBefore("if (callow.launcheragent.patch.UpdateFilePatcher.isExclude($1, $2, $3)) {return;}");

            System.out.println("[+] Update | Com4.aux(): Patch was created.");

        } catch (NotFoundException | CannotCompileException e) {
            System.out.println("[-] Update | Com4.aux(): Patch creation was failed.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isExclude(Path path, Prn prn, InputStream inputStream) throws IOException {
        if (path.getParent().getFileName().toString().equals("mods")){
            String clientName = path.getParent().getParent().getFileName().toString();
            String serverName = Util.ClientDirToName.get(clientName);
            String modName = path.getFileName().toString();

            ModsConfig config = Agent.modsConfig;
            config.update();

            if (Arrays.stream(config.getExcludesByServerName(serverName)).anyMatch(x -> x.getFilename().equals(modName)))
            {
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
