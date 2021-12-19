package callow.clientagent.patch;

import callow.clientagent.IClientPatch;
import javassist.*;
import launcher.*;

import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientStartPatch implements IClientPatch {

    @Override
    public String getPatchName() {
        return "Patch for client start modification";
    }

    @Override
    public List<String> getListPatchedClasses() {
        List<String> classes = new ArrayList<>();
        classes.add("launcher.AUx");
        return classes;
    }

    @Override
    public List<ServerInfo> getServersInfo() {
        List<ServerInfo> servers = new ArrayList<>();

        ServerInfo info = new ServerInfo("*", new HashMap<>());
        info.hashDependencies.put("../Launcher.jar",
                "b5ecba726daec152af9bbd2c7bce34b1");
        servers.add(info);

        return servers;
    }

    @Override
    public boolean isPatchRequired() {
        return true;
    }

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        try {
            CtMethod method = ctClass.getDeclaredMethod("main");
            method.setBody("{ Object[] params = callow.clientagent.patch.ClientStartPatch.modifyRunParams($1); launcher.AUx.aux((launcher.AUX)params[0], (launcher.aUX)params[1]); }");
        } catch (NotFoundException | CannotCompileException e) {
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
