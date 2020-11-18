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

        ProcessBuilder processBuilder = new ProcessBuilder(new ArrayList<String>());
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

    public static List<String> onProcessBuilderConstruct(List<String> command) throws IOException {
        System.out.println("[-] Process builder completing.");
        Path clientPath = Paths.get(System.getenv("TEMP")).resolve("ClientAgent.jar");
        copyResourceFile("ClientAgent.jar", clientPath);
        command.add(1, String.format("-javaagent:\"%s\"", clientPath.toAbsolutePath().toString()));
        command.add(0, "cmd");
        command.add(0, "/c");
        command.add(0, "start");
        command.add(0, "cmd");
        command.add(0, "/k");
        return command;
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

    public static void onClientLaunch(aUX serverProfile) throws IOException {

        String clientName = serverProfile.clientDir.getFileName().toString();
        System.out.println("[/] Launching: " + clientName);

        File customModsDir = PropertiesFields.modsFolderPath.toFile();
        Path clientMods = serverProfile.clientDir.resolve("mods");

        String fileContent = Util.readFile(PropertiesFields.customModsConfig.toFile());
        JSONObject jsonRoot = new JSONObject(fileContent.isEmpty() ? "{}" : fileContent);

        if (jsonRoot.has("customMods")){
            JSONObject customMods = jsonRoot.getJSONObject("customMods");
            for (File file : Objects.requireNonNull(customModsDir.listFiles())) {
                if (!customMods.has(file.getName()))
                    continue;

                JSONObject customMod = customMods.getJSONObject(file.getName());
                if (!customMod.has("servers") || !customMod.getJSONArray("servers").toList().contains(clientName))
                    continue;

                Path modPath = clientMods.resolve(file.getName());
                if (Files.exists(modPath))
                    Files.delete(modPath);
                Files.copy(file.toPath(), modPath);
            }
        }

        if (jsonRoot.has("excludeMods")){
            JSONObject excludeMods = jsonRoot.getJSONObject("excludeMods");
            for (String key : excludeMods.keySet()){
                Object object = excludeMods.get(key);
                if (!object.getClass().equals(JSONArray.class))
                    continue;
                if (((JSONArray)object).toList().contains(clientName)){
                    Path excludePath = clientMods.resolve(key);
                    if (Files.exists(excludePath))
                        Files.delete(excludePath);
                }
            }
        }
    }

    public static void onRequestError(final String string){
        System.out.println("[ERROR] Request error: " + string);
    }
}
