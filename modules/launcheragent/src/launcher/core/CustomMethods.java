package launcher.core;

import common.PropertiesFields;
import o.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;

import static common.Utils.copyResourceFile;

// Callback methods class
public class CustomMethods {

    public static List<AUX> serverProfiles;

    public static void onStart() {
        Aux config = aux.getConfig();

        Com1.setDebugEnabled(PropertiesFields.debug);
        // Getting server profiles objects
        CoM3 launcherRequest = new CoM3(config);
        COM3 response = (COM3)launcherRequest.request();
        serverProfiles = new ArrayList<>();
        for (Object profile : response.profiles)
        {
            cOm7 profileInfo = (cOm7)profile;
            serverProfiles.add((AUX)profileInfo.object);
        }

        if (!Files.exists(PropertiesFields.modsFolderPath)) {
            try {
                Files.createDirectories(PropertiesFields.modsFolderPath);
                System.out.println("[+] Custom mods directory was created.");
            } catch (IOException e) {
                System.out.println("[-] Couldn't custom mods directory.");
            }
        }

        System.out.println("[+] Server profiles was loaded.");

        Util.updateModsConfig();
    }

    public static boolean onUpdateFile(Path path, Prn prn, InputStream inputStream) throws IOException {
        if (path.getParent().getFileName().toString().equals("mods")){
            String clientName = path.getParent().getParent().getFileName().toString();
            String modName = path.getFileName().toString();

            String fileContent = Util.readFile(PropertiesFields.customModsConfig.toFile());
            JSONObject jsonRoot = new JSONObject(fileContent.isEmpty() ? "{}" : fileContent);
            if (!jsonRoot.has("excludeMods"))
                return true;
            jsonRoot = jsonRoot.getJSONObject("excludeMods");
            if (jsonRoot.has(modName) && jsonRoot.getJSONArray(modName).toList().contains(clientName)){
                byte[] buffer = new byte[2048];
                int n = 0;
                while (n < prn.size) {
                    final int read = inputStream.read(buffer, 0, (int)Math.min(prn.size - n, buffer.length));
                    if (read < 0) {
                        throw new EOFException(String.format("%d bytes remaining", prn.size - n));
                    }
                    n += read;
                }
                return false;
            }
        }
        return true;
    }

    public static Process onClientLaunch(final Path jvmDir, final cOm7 jvmHDir, final cOm7 assetHDir, final cOm7 clientHDir, final cOm7 profile, final aUX serverParams, final boolean isDebug) throws IOException {

        String clientName = serverParams.clientDir.getFileName().toString();
        System.out.println("[/] Launching: " + clientName);

        File customModsDir = PropertiesFields.modsFolderPath.toFile();
        Path clientMods = serverParams.clientDir.resolve("mods");

        String fileContent = Util.readFile(PropertiesFields.customModsConfig.toFile());
        JSONObject jsonRoot = new JSONObject(fileContent.isEmpty() ? "{}" : fileContent);

        JSONObject changeIdList = new JSONObject();

        if (jsonRoot.has("customMods")){
            JSONObject customMods = jsonRoot.getJSONObject("customMods");
            for (File file : Objects.requireNonNull(customModsDir.listFiles())) {
                if (!customMods.has(file.getName()))
                    continue;

                JSONObject customMod = customMods.getJSONObject(file.getName());
                Path modPath = clientMods.resolve(file.getName());

                if (customMod.has("mod_info") && !customMod.isNull("mod_info")){
                    Object modInfoObject = customMod.get("mod_info");

//                    if (modInfoObject.getClass().equals(String.class))
//                        modPath = clientMods.resolve(modInfoObject.toString());

                    changeIdList.put(file.getName(), modInfoObject);
                }

                if (customMod.has("servers") && !customMod.isNull("servers") && Util.isContains(customMod.get("servers"), clientName))
                {

                    if (Files.exists(modPath))
                        Files.delete(modPath);
                    Files.copy(file.toPath(), modPath);
                }
            }
        }

        if (jsonRoot.has("excludeMods")){
            JSONObject excludeMods = jsonRoot.getJSONObject("excludeMods");
            for (String key : excludeMods.keySet()){
                if (Util.isContains(excludeMods.get(key), clientName)){
                    Path excludePath = clientMods.resolve(key);
                    if (Files.exists(excludePath))
                        Files.delete(excludePath);
                }
            }
        }

        // Copied from decompiled code
        final Path tempFile = Files.createTempFile("ClientLauncherParams", ".bin");
        try (final COm5 cOm11 = new COm5(PRn.newOutput(tempFile))) {
            serverParams.write(cOm11);
            profile.write(cOm11);
            jvmHDir.write(cOm11);
            assetHDir.write(cOm11);
            clientHDir.write(cOm11);
        }

        Com1.debug("Resolving JVM binary");
        final Path resolveJavaBin = PRn.resolveJavaBin(jvmDir);
        final List<String> list = new ArrayList<>();
        Path clientPath = Paths.get(System.getenv("TEMP")).resolve("ClientAgent.jar");
        copyResourceFile("ClientAgent.jar", clientPath);

        if (isDebug)
            list.addAll(Arrays.asList("cmd", "/c", "start", "cmd", "/k") );

        list.add(resolveJavaBin.toString());
        list.add(String.format("-javaagent:\"%s\"", clientPath.toAbsolutePath().toString()));
        list.add("-XX:HeapDumpPath=ThisTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");

        if (serverParams.ram > 0 && serverParams.ram <= PRN.RAM) {
            list.add("-Xms" + serverParams.ram + 'M');
            list.add("-Xmx" + serverParams.ram + 'M');
        }
        list.add(String.format("-D%s=%s", "launcher.debug", Com1.isDebugEnabled()));

        if (o.Aux.ADDRESS_OVERRIDE != null) {
            list.add(String.format("-D%s=%s", "launcher.addressOverride", o.Aux.ADDRESS_OVERRIDE));
        }

        if (PRN.OS_TYPE == com1.MUSTDIE && PRN.OS_VERSION.startsWith("10.")) {
            Com1.debug("MustDie 10 fix is applied");
            list.add(String.format("-D%s=%s", "os.name", "Windows 10"));
            list.add(String.format("-D%s=%s", "os.version", "10.0"));
        }

        Collections.addAll(list, ((AUX)profile.object).getJvmArgs());
        Collections.addAll(list, "-classpath", PRn.getCodeSource(AUx.class).toString(), AUx.class.getName());
        list.add(tempFile.toString());
        Com1.debug("Commandline: " + list);
        Com1.debug("Launching client instance");

        final ProcessBuilder processBuilder = new ProcessBuilder(list);
        processBuilder.directory(serverParams.clientDir.toFile());
        processBuilder.inheritIO();

        final Map<String, String> environment = processBuilder.environment();
        environment.put("_JAVA_OPTS", "");
        environment.put("_JAVA_OPTIONS", "");
        environment.put("JAVA_OPTS", "");
        environment.put("JAVA_OPTIONS", "");

        environment.put("MOD_ID_CHANGE_LIST", changeIdList.toString());
        Process process = processBuilder.start();
        System.exit(0);
        return process;
    }

    public static void onRequestError(final String string){
        System.out.println("[ERROR] Request error: " + string);
    }

}
