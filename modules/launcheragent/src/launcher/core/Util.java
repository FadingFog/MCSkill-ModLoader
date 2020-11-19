package launcher.core;

import common.PropertiesFields;
import o.AUX;
import org.json.JSONArray;
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
                            "{" +
                                        "\"exampleMod.jar\": {" +
                                            "\"servers\" : [\"ServerName1\", \"ServerName2\"], " +
                                            "\"mod_info\": { " +
                                                "\"id\": \"newIdValue\", " +
                                                "\"version\": \"newVersionValue\" " +
                                            "} " +
                                        "}, " +
                                        " \"exampleMod2.jar\": {" +
                                            "\"servers\" : \"SingleName\", " +
                                            "\"mod_info\": \"file_to_replace.jar\" " +
                                        "}, " +
                                    "}");
                    jsonRoot.put("customMods", customMods);
                }

                JSONObject customMods = jsonRoot.getJSONObject("customMods");
                for (String fileName : Objects.requireNonNull(PropertiesFields.modsFolderPath.toFile().list())){
                    if (!fileName.endsWith(".litemod") && !fileName.endsWith(".jar"))
                        continue;
                    if (!customMods.has(fileName))
                    {
                        hasChanges = true;
                        customMods.put(fileName, new JSONObject("{ \"servers\": null , \"mod_info\": null }"));
                    }
                }
                jsonRoot.put("customMods", customMods);

                if (!jsonRoot.has("excludeMods")) {
                    hasChanges = true;
                    JSONObject excludeMods = new JSONObject(
                            "{\"example.jar\": [\"ServerName1\", \"ServerName2\"], " +
                                    "\"example2.jar\": \"SingleServerName\" }");
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

    public static boolean isContains(Object source, String value){
        return (source.getClass().equals(String.class) && source.toString().equals(value)) ||
                (source.getClass().equals(JSONArray.class) && ((JSONArray) source).toList().contains(value));
    }
//
//    public static JSONObject getModInfo(Path mod)
//    {
//        try (ZipFile zipFile = new ZipFile(mod.toString())) {
//            Optional<? extends ZipEntry> modinfo = zipFile.stream()
//                    .filter(zipEntry -> zipEntry.getName().equals("mcmod.info"))
//                    .findFirst();
//
//            if (modinfo.isPresent())
//            {
//                JSONObject result = new JSONObject();
//                JSONObject modInfo = new JSONObject(zipFile.getInputStream(modinfo.get()));
//            }
//            System.out.println(String.format(
//                    "Item: %s \nType: %s \nSize: %d\n",
//                    entry.getName(),
//                    entry.isDirectory() ? "directory" : "file",
//                    entry.getSize()
//            ));
//        } catch (IOException e) {
//            System.out.println("[-] Mod file to get info isn't found.");
//        }
//        return null;
//    }
}
