package launcher.core;

import common.PropertiesFields;
import o.AUX;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Util {
    public static Boolean checkFileOrCreate(Path path){
        if (!Files.isRegularFile(PropertiesFields.customModsConfig)) {
            try {
                Files.deleteIfExists(PropertiesFields.customModsConfig);
                Files.createFile(path);
            } catch (IOException e) {
                System.out.println("[-] File creation filed.");
                return false;
            }
        }
        return true;
    }

    public static String getServerName(String serverDir){
        for (AUX profile : CustomMethods.serverProfiles)
        {
            if (profile.getDir().equals(serverDir))
                return profile.getTitle();
        }
        return null;
    }

    public static String readFile(File file) throws FileNotFoundException {
        Scanner myReader = new Scanner(file);
        StringBuilder builder = new StringBuilder();
        while (myReader.hasNextLine())
            builder.append(myReader.nextLine());
        myReader.close();
        return builder.toString();
    }

    public static void writeFile(File file, String data) throws IOException {
        Writer writer = new FileWriter(file);
        writer.write(data);
        writer.close();
    }

    public static void updateModsConfig() {
        if (Util.checkFileOrCreate(PropertiesFields.customModsConfig)){
            boolean hasChanges = false;
            try {
                File excludeFile = PropertiesFields.customModsConfig.toFile();
                String fileContent = Util.readFile(excludeFile);
                JSONObject jsonRoot = new JSONObject(fileContent.isEmpty() ? "{}" : fileContent);

                List<String> serverDirs = new ArrayList<>();
                for (AUX profile : CustomMethods.serverProfiles)
                    serverDirs.add(profile.getDir());

                if (!jsonRoot.has("ServerNames")) {
                    hasChanges = true;
                    jsonRoot.put("ServerNames", serverDirs);
                }
                else {
                    List<Object> names = jsonRoot.getJSONArray("ServerNames").toList();
                    for (String dirName: serverDirs){
                        if (!names.contains(dirName)) {
                            hasChanges = true;
                            names.add(dirName);
                        }
                    }
                    jsonRoot.put("ServerNames", names);
                }

                if (!jsonRoot.has("customMods")){
                    hasChanges = true;
                    JSONObject customMods = new JSONObject(
                            "{ \"exampleMod.jar\": {" +
                                    " \"comment\" : \"This is example of custom mod config." +
                                    " 'servers' consist names of servers which will USE this modification." +
                                    " This field isn't necessary.\"," +
                                    "\"servers\" : [\"ServerName1\", \"ServerName2\"]" +
                                    "} }");
                    jsonRoot.put("customMods", customMods);
                }

                JSONObject customMods = jsonRoot.getJSONObject("customMods");
                for (String fileName : Objects.requireNonNull(PropertiesFields.modsFolderPath.toFile().list())){
                    if (!fileName.endsWith(".litemod") && !fileName.endsWith(".jar"))
                        continue;
                    if (!customMods.has(fileName))
                    {
                        hasChanges = true;
                        customMods.put(fileName, new JSONObject("{ \"servers\" : [] }"));
                    }
                }
                jsonRoot.put("customMods", customMods);

                if (!jsonRoot.has("excludeMods")) {
                    hasChanges = true;
                    JSONObject excludeMods = new JSONObject(
                            "{ \"comment\": \"Here are excluded mods filenames and corresponding servers on which they are excluded.\"," +
                                    "\"exampleMod.jar\": [\"ServerName1\", \"ServerName2\"] }");
                    jsonRoot.put("excludeMods", excludeMods);
                }

                if (hasChanges)
                    writeFile(excludeFile, jsonRoot.toString(4));

                System.out.println("[+] Custom mods config file was updated successfully.");
            } catch (IOException e) {
                System.out.println("[-] Failed to read custom mods config file.");
            }
        }
    }
}
