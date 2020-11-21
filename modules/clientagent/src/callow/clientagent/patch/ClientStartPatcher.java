package callow.clientagent.patch;

import javassist.*;
import o.*;
import callow.common.IClassPatcher;

import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;

public class ClientStartPatcher implements IClassPatcher {
    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        if (!ctClass.getName().equals("o.AUx"))
            return false;

        try {
            CtMethod method = ctClass.getDeclaredMethod("main");
            method.setBody("{ Object[] params = callow.clientagent.patch.ClientStartPatcher.modifyRunParams($1); o.AUx.aux((o.AUX)params[0], (o.aUX)params[1]); }");

            System.out.println("[+] Patcher | ClientStart patch created.");
        } catch (NotFoundException | CannotCompileException e) {
            System.out.println("[-] Patcher | ClientStart patch install failed.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Object[] modifyRunParams(final String... array){
        final RSAPublicKey publicKey = o.aux.getConfig().publicKey;
        final Path path = PRn.toPath(array[0]);
        aUX clientParams;
        AUX serverProfile;
        try (final cOm5 cOm5 = new cOm5(PRn.newInput(path))) {
            clientParams = new aUX(cOm5);
            serverProfile = (AUX)new cOm7(cOm5, publicKey, AUX.RO_ADAPTER).object;
        }
        return new Object[] { serverProfile, clientParams };
    }
}
