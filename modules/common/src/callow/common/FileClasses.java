package callow.common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static callow.common.Utils.getMD5Checksum;
import static callow.common.Utils.readFile;

public class FileClasses {

    static class FileEntry {
        public String hash;
        public List<String> classes;

        public FileEntry(JSONObject object) {
            hash = object.getString("hash");
            classes = object.getJSONArray("classes")
                    .toList().stream().map(Object::toString)
                    .collect(Collectors.toList());
        }

        public FileEntry(String hash, List<String> classes) {
            this.hash = hash;
            this.classes = classes;
        }
    }

    private static boolean isLoaded = false;
    private static HashMap<String, FileEntry> entries;

    public static List<String> get(String filepath) {
        if (!isLoaded) load();

        if (has(filepath))
            return entries.get(filepath).classes;

        List<String> result = new ArrayList<>();
        JarFile jarFile;
        try {
            jarFile = new JarFile(filepath);
        } catch (IOException ignored) { return result; }

        for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
            if (jarEntry.getName().endsWith(".class"))
                result.add(jarEntry.getName().replace(".class", "").replace('/', '.'));
        }

        entries.put(filepath, new FileEntry(getMD5Checksum(filepath), result));
        return result;
    }

    public static boolean has(String filepath) {
        if (!isLoaded) load();

        if (!entries.containsKey(filepath))
            return false;

        String fileHash = entries.get(filepath).hash;
        return Objects.equals(fileHash, getMD5Checksum(filepath));
    }

    private static void load() {
        File cacheFile = new File(".classes_cache");
        entries = new HashMap<>();
        isLoaded = true;
        JSONObject jsonRoot;
        try {
            String fileContent = readFile(cacheFile);
            jsonRoot = new JSONObject(fileContent);
        } catch (FileNotFoundException | JSONException e) {
            return;
        }
        for (Iterator<String> it = jsonRoot.keys(); it.hasNext(); ) {
            String filepath = it.next();
            JSONObject fileObject = jsonRoot.getJSONObject(filepath);
            if (!fileObject.has("hash") || !fileObject.has("classes"))
                continue;
            entries.put(filepath, new FileEntry(fileObject));
        }
    }

    public static void save() {
        if (!isLoaded) return;

        JSONObject rootObject = new JSONObject();
        for (Map.Entry<String, FileEntry> entry: entries.entrySet()) {
            FileEntry fileEntry = entry.getValue();
            String filename = entry.getKey();

            JSONObject fileObject = new JSONObject();
            fileObject.put("hash", fileEntry.hash);
            fileObject.put("classes", fileEntry.classes);

            rootObject.put(filename, fileObject);
        }

        try {
            Writer fileWriter = new FileWriter(".classes_cache");
            rootObject.write(fileWriter);
            fileWriter.close();
        } catch (IOException ignored) {}
    }

}
