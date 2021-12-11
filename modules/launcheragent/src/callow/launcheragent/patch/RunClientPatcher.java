package callow.launcheragent.patch;

import callow.launcheragent.Agent;
import callow.launcheragent.ModsConfig;
import callow.launcheragent.Util;
import javassist.*;
import launcher.*;
import org.json.JSONObject;
import callow.common.IClassPatcher;
import callow.common.PropertiesFields;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static callow.common.Utils.copyResourceFile;

public class RunClientPatcher implements IClassPatcher {

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {
        if (!ctClass.getName().equals("launcher.AUx"))
            return false;

        // Minecraft Client launch method
        // launch
        try {

            CtMethod method = ctClass.getDeclaredMethod("launch");
            method.setBody("return callow.launcheragent.patch.RunClientPatcher.customLaunch($1, $2, $3, $4, $5, $6, $7);");

            System.out.println("[+] ClientLaunch | AUx.launch(): Patch was created.");

        } catch (NotFoundException | CannotCompileException e) {
            System.out.println("[-] ClientLaunch | AUx.launch(): Patch creation was failed.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Process customLaunch(final Path jvmDir, final com8 jvmHDir, final com8 assetHDir,
                                       final com8 clientHDir, final com8 profile, final aUX serverParams,
                                       final boolean isDebug) throws IOException
    {
        try {
            Agent.modsConfig = new ModsConfig(PropertiesFields.modJSONConfig.toFile());
        } catch (IOException e) {
            System.out.println("[-] Loading mods config file was failed.");
            e.printStackTrace();
        }

        PropertiesFields.loadProperties();

        String clientName = serverParams.clientDir.getFileName().toString();
        String serverName = Util.ClientDirToName.get(clientName);
        System.out.println("[/] Launching: " + clientName);

        File customModsDir = PropertiesFields.includeModsDir.toFile();
        Path clientMods = serverParams.clientDir.resolve("mods");

        ModsConfig config = Agent.modsConfig;
        config.update();

        JSONObject excludesHandshake = new JSONObject();
        for (ModsConfig.IncludeModInfo modInfo : config.getIncludesByServerName(serverName)) {
            Path modCustomPath = customModsDir.toPath().resolve(modInfo.getFilename());
            Path modClientPath = clientMods.resolve(modInfo.getFilename());
            if (!modCustomPath.toFile().exists())
                continue;
            try {
                if (modClientPath.toFile().exists())
                    Files.delete(modClientPath);
                Files.copy(modCustomPath, modClientPath);
            } catch (IOException e) { continue; }
            if (!modInfo.inHandshake())
                excludesHandshake.put(modInfo.getFilename(), true);
        }

        for (ModsConfig.StandardInfo modInfo : config.getExcludesByServerName(serverName))
        {
            Path modClientPath = clientMods.resolve(modInfo.getFilename());
            if (modClientPath.toFile().exists())
                Files.delete(modClientPath);
        }

        // Copied from decompiled code
        final Path tempFile = Files.createTempFile("ClientLauncherParams", ".bin");
        try (final Com6 cOm11 = new Com6(PRN.newOutput(tempFile))) {
            serverParams.write(cOm11);
            profile.write(cOm11);
            jvmHDir.write(cOm11);
            assetHDir.write(cOm11);
            clientHDir.write(cOm11);
        }

        CoM1.debug("Resolving JVM binary");
        final Path resolveJavaBin = PRN.resolveJavaBin(jvmDir);
        final List<String> list = new ArrayList<>();
        Path clientPath = Paths.get(System.getenv("TEMP")).resolve("ClientAgent.jar");
        copyResourceFile("ClientAgent.jar", clientPath);

        if (PropertiesFields.clientDebug)
            list.addAll(Arrays.asList("cmd", "/c", "start", "cmd", "/k") );

        list.add(resolveJavaBin.toString());

        if (Boolean.getBoolean("idea.debug")){
            list.add("-Xdebug");
            list.add("-agentlib:jdwp=transport=dt_socket,address=9000,server=y,suspend=y");
        }

        if (PropertiesFields.clientInjection){
            list.add(String.format("-javaagent:\"%s\"", clientPath.toAbsolutePath()));
            System.out.println("[+] Client agent was injected.");
        }
        else {
            System.out.println("[+] Client agent wasn't injected.");
        }

        list.add("-XX:HeapDumpPath=ThisTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");

        if (serverParams.ram > 0 && serverParams.ram <= COm1.RAM) {
            list.add("-Xms" + serverParams.ram + 'M');
            list.add("-Xmx" + serverParams.ram + 'M');
        }
        list.add(String.format("-D%s=%s", "launcher.debug", CoM1.isDebugEnabled()));

        if (launcher.Aux.ADDRESS_OVERRIDE != null) {
            list.add(String.format("-D%s=%s", "launcher.addressOverride", launcher.Aux.ADDRESS_OVERRIDE));
        }

        if (COm1.OS_TYPE == coM1.MUSTDIE && COm1.OS_VERSION.startsWith("10.")) {
            CoM1.debug("MustDie 10 fix is applied");
            list.add(String.format("-D%s=%s", "os.name", "Windows 10"));
            list.add(String.format("-D%s=%s", "os.version", "10.0"));
        }
        Collections.addAll(list, ((AUX)profile.object).getJvmArgs());

        Stream<?> stream = AUx.aux((AUX)profile.object, serverParams.clientDir);
        List<String> classes = stream.map(Object::toString).collect(Collectors.toList());

        String sep = COm1.OS_TYPE == coM1.MUSTDIE ? ";" : ":";

        Collections.addAll(list, "-classpath",
                PRN.getCodeSource(AUx.class).toString() + sep + String.join(sep, classes),
                AUx.class.getName());
        list.add(tempFile.toString());
        CoM1.debug("Commandline: " + list);
        CoM1.debug("Launching client instance");

        final ProcessBuilder processBuilder = new ProcessBuilder(list);
        processBuilder.directory(serverParams.clientDir.toFile());
        processBuilder.inheritIO();

        final Map<String, String> environment = processBuilder.environment();
        environment.put("_JAVA_OPTS", "");
        environment.put("_JAVA_OPTIONS", "");
        environment.put("JAVA_OPTS", "");
        environment.put("JAVA_OPTIONS", "");

        environment.put("MODS_HANDSHAKE_EXCLUDED", excludesHandshake.toString());

        Process process = processBuilder.start();
        COm1.RUNTIME.exit(0);

        // Never reach
        return process;
    }
}
