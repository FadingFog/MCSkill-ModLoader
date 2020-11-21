package callow.launcheragent;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import o.AUX;
import org.json.JSONArray;
import org.json.JSONObject;
import callow.common.PropertiesFields;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ModsConfig {

    public static class StandardInfo {
        @NotNull
        private final String filename;
        @Nullable
        private final List<String> serverNames;

        public StandardInfo(String filename) {
            this(filename, null);
        }

        public StandardInfo(String filename, String[] serverNames) {
            this.filename = filename;
            if (serverNames == null)
                this.serverNames = new ArrayList<>();
            else
                this.serverNames = Arrays.asList(serverNames);
        }

        public StandardInfo(String filename, Object info) {
            this.filename = filename;
            this.serverNames = new ArrayList<>();
            if (info.getClass().equals(String.class))
                this.serverNames.add((String)info);
            else if (info.getClass().equals(JSONArray.class))
                for (Object object: (JSONArray)info)
                    if (object.getClass().equals(String.class))
                        this.serverNames.add((String)object);
        }

        public String[] getServerNames() {
            if (serverNames == null)
                return new String[0];
            return (String[]) serverNames.toArray();
        }

        public boolean hasServer(String serverName) {
            return serverNames.contains(serverName);
        }

        public String getFilename() {
            return filename;
        }

        public Map.Entry<String, Object> getJSON() {
            if (serverNames.size() == 0)
                return new AbstractMap.SimpleEntry<>(filename, JSONObject.NULL);
            else if (serverNames.size() == 1)
                return new AbstractMap.SimpleEntry<>(filename, serverNames.get(0));
            else
                return new AbstractMap.SimpleEntry<>(filename, new JSONArray(serverNames));
        }
    }

    public static class IncludeModInfo extends StandardInfo{
        private final boolean inHandshake;

        public IncludeModInfo(String filename) {
            super(filename);
            this.inHandshake = false;
        }

        public IncludeModInfo(String filename, String[] serverNames, boolean inHandshake) {
            super(filename, serverNames);
            this.inHandshake = inHandshake;
        }

        public IncludeModInfo(String filename, Object info) {
            super(filename, ((JSONObject)info).get("servers"));
            boolean inHandshake = false;
            if (info.getClass().equals(JSONObject.class)) {
                JSONObject jsonEntry = (JSONObject)info;
                if (jsonEntry.has("in_handshake") && !jsonEntry.isNull("in_handshake")){
                    Object inHandshakeObj = jsonEntry.get("in_handshake");
                    if (inHandshakeObj.getClass().equals(Boolean.class))
                        inHandshake = (Boolean)inHandshakeObj;
                }
            }
            this.inHandshake = inHandshake;
        }

        public boolean inHandshake() {
            return inHandshake;
        }

        @Override
        public Map.Entry<String, Object> getJSON() {
            Map.Entry<String, Object> entry = super.getJSON();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("servers", entry.getValue());
            jsonObject.put("in_handshake", inHandshake);
            return new AbstractMap.SimpleEntry<>(getFilename(), jsonObject);
        }
    }

    private static final String includeBlockName = "includeMods";
    private static final String excludeBlockName = "excludeMods";

    @NotNull
    private List<IncludeModInfo> includeMods;
    @NotNull
    private List<StandardInfo> excludeMods;
    @NotNull
    private final File source;

    public ModsConfig(File file) throws IOException {
        source = file;
        update();
    }

    public void update() throws IOException {
        boolean hasChanges = false;
        JSONObject jsonRoot;
        try {
            String fileContent = Util.readFile(source);
            jsonRoot = new JSONObject(fileContent.isEmpty() ? "{}" : fileContent);
        } catch (FileNotFoundException e) {
            System.out.println("[+] Created new json config file: " + source.getAbsolutePath());
            source.createNewFile();
            jsonRoot = new JSONObject();
            hasChanges = true;
        }

        List<String> serverTitles = new ArrayList<>();
        for (AUX profile : Util.getServersProfiles())
            serverTitles.add(profile.getTitle());

        if (!jsonRoot.has("ServerNames")) {
            hasChanges = true;
            jsonRoot.put("ServerNames", serverTitles);
        }
        else {
            List<Object> names = jsonRoot.getJSONArray("ServerNames").toList();
            for (String title: serverTitles){
                if (!names.contains(title)) {
                    hasChanges = true;
                    names.add(title);
                }
            }
            jsonRoot.put("ServerNames", names);
        }

        if (!jsonRoot.has(includeBlockName)){
            hasChanges = true;
            JSONObject customMods = new JSONObject();
            Map.Entry<String, Object> example = new IncludeModInfo("example.jar", new String[] { "exampleServer1", "exampleServer2" }, false).getJSON();
            customMods.put(example.getKey(), example.getValue());
            jsonRoot.put(includeBlockName, customMods);
        }

        JSONObject customMods = jsonRoot.getJSONObject(includeBlockName);
        for (String fileName : Objects.requireNonNull(PropertiesFields.modsFolderPath.toFile().list())){
            if (!fileName.endsWith(".litemod") && !fileName.endsWith(".jar"))
                continue;
            if (!customMods.has(fileName))
            {
                Map.Entry<String, Object> example = new IncludeModInfo(fileName).getJSON();
                customMods.put(example.getKey(), example.getValue());
            }
        }
        jsonRoot.put(includeBlockName, customMods);

        if (!jsonRoot.has(excludeBlockName)) {
            hasChanges = true;
            JSONObject excludeModsJSON = new JSONObject();
            Map.Entry<String, Object> example = new StandardInfo("example.jar", new String[] {"ExampleServerName"}).getJSON();
            excludeModsJSON.put(example.getKey(), example.getValue());
            jsonRoot.put(excludeBlockName, excludeModsJSON);
        }

        if (hasChanges)
            Util.writeFile(source, jsonRoot.toString(4));

        includeMods = new ArrayList<>();
        excludeMods = new ArrayList<>();

        JSONObject includeModsJSON = jsonRoot.getJSONObject(includeBlockName);
        for (String key: includeModsJSON.keySet())
            includeMods.add(new IncludeModInfo(key, includeModsJSON.get(key)));

        JSONObject excludeModsJSON = jsonRoot.getJSONObject(excludeBlockName);
        for (String key: excludeModsJSON.keySet())
            excludeMods.add(new StandardInfo(key, excludeModsJSON.get(key)));
    }

    public IncludeModInfo[] getCustomMods() {
        return (IncludeModInfo[]) includeMods.toArray();
    }

    public StandardInfo[] getExcludes() {
        return (StandardInfo[]) excludeMods.toArray();
    }

    public IncludeModInfo[] getIncludesByServerName(String serverName) {
        return includeMods.stream().filter(x -> x.hasServer(serverName)).toArray(IncludeModInfo[]::new);
    }

    public StandardInfo[] getExcludesByServerName(String serverName) {
        return excludeMods.stream().filter(x -> x.hasServer(serverName)).toArray(StandardInfo[]::new);
    }
}
