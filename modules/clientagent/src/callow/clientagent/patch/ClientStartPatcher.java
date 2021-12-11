package callow.clientagent.patch;

import javassist.*;
import launcher.*;
import callow.common.IClassPatcher;

import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;

public class ClientStartPatcher implements IClassPatcher {
    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        if (!ctClass.getName().equals("launcher.AUx"))
            return false;

        try {
            CtMethod method = ctClass.getDeclaredMethod("main");
            method.setBody("{ Object[] params = callow.clientagent.patch.ClientStartPatcher.modifyRunParams($1); launcher.AUx.aux((launcher.AUX)params[0], (launcher.aUX)params[1]); }");

            System.out.println("[+] Patcher | ClientStart patch created.");
        } catch (NotFoundException | CannotCompileException e) {
            System.out.println("[-] Patcher | ClientStart patch install failed.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Object[] modifyRunParams(final String... array){
        final RSAPublicKey publicKey = aux.getConfig().publicKey;
        final Path path = PRN.toPath(array[0]);
        aUX clientParams;
        AUX serverProfile;
        try (final com6 com6 = new com6(PRN.newInput(path))) {
            clientParams = new aUX(com6);
            serverProfile = (AUX)new com8(com6, publicKey, AUX.RO_ADAPTER).object;
        }
        return new Object[] { serverProfile, clientParams };
    }
}
