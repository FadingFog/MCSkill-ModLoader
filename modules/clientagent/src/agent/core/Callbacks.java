package agent.core;

import javassist.bytecode.ClassFile;
import o.*;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Callbacks {
    public static Object[] onRunningClient(final String... array){
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

    public static List<Object> onDiscoveringMods(List<Object> modsContainers) {
        System.out.println(System.getenv("MOD_ID_CHANGE_LIST"));
        Class<?> modContainerClazz = null;
        try {
            modContainerClazz = Class.forName("cpw.mods.fml.common.FMLModContainer");
            System.out.println("[+] Mods fetching 1.7.10...");
        } catch (ClassNotFoundException e) {
            try {
                modContainerClazz = Class.forName("net.minecraftforge.fml.common.FMLModContainer");
                System.out.println("[+] Mods fetching 1.12.2...");
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        }
        if (modContainerClazz == null)
            return modsContainers;

        List<Object> restricted = new ArrayList<>();
        try {
            JSONObject changeIdMap = new JSONObject(System.getenv("MOD_ID_CHANGE_LIST"));
            for (Object container: modsContainers) {
                if (restricted.contains(container))
                    continue;
                System.out.println("[CInfo] Processing " + container.toString());
                File source = (File)modContainerClazz.getMethod("getSource").invoke(container);
                System.out.println("[CInfo] Source name " + source.getName());
                if (changeIdMap.has(source.getName())) {
                    System.out.printf("[CInfo] %s is changing...\n", source.getName());

                    String newModId = null;
                    String newVersion = null;
                    Object changeIdData = changeIdMap.get(source.getName());
                    if (changeIdData.getClass().equals(String.class)) {
                        final String replaceMod = (String) changeIdData;
                        System.out.printf("[CInfo] %s replaced by %s\n", source.getName(), replaceMod);
                        final Class<?> modClazz = modContainerClazz;
                        Optional<Object> modContainerOpt = modsContainers.stream().filter(mod ->
                        {
                            try {
                                return ((File)modClazz.getMethod("getSource").invoke(mod)).getName().equals(replaceMod);
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }).findFirst();
                        if (modContainerOpt.isPresent()){
                            Object replaceContainer = modContainerOpt.get();
                            newModId = (String)modContainerClazz.getMethod("getModId").invoke(container);
                            newVersion = (String)modContainerClazz.getMethod("getVersion").invoke(container);
                            System.out.printf("[CInfo] Replaced mod data %s:%s\n", newModId, newVersion);
                            restricted.add(replaceContainer);
                        }
                    } else if (changeIdData.getClass().equals(JSONObject.class)) {
                        JSONObject changeInfo = (JSONObject)changeIdData;
                        if (changeInfo.has("id"))
                            newModId = changeInfo.getString("id");
                        if (changeInfo.has("version"))
                            newVersion = changeInfo.getString("version");
                        System.out.printf("[CInfo] New mod info %s:%s\n", newModId == null ? "none" : newModId,
                                newVersion == null ? "none" : newVersion);
                    } else
                        continue;

                    if (newModId != null)
                        modContainerClazz.getMethod("setModId", String.class).invoke(container, newModId);

                    if (newVersion != null)
                        modContainerClazz.getMethod("setVersion", String.class).invoke(container, newVersion);

                    System.out.printf("[CInfo] Changed info about mod '%s'\n", source.getName());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        for (Object removeObj: restricted) {
            modsContainers.remove(removeObj);
        }

        return modsContainers;
    }

    public static void onCreateModContainer(File source, Map<String, Object> descriptor) {
        if (!descriptor.containsKey("modid"))
            return;
        String modId = (String) descriptor.get("modid");
        System.out.printf("[MInfo] Mod '%s' with id '%s' loaded.\n", source.toPath(), modId);

        JSONObject changeIdMap = new JSONObject(System.getenv("MOD_ID_CHANGE_LIST"));
        if (changeIdMap.has(source.getName())) {
            String newModId = changeIdMap.getString(source.getName());
            descriptor.put("modid", newModId);
            System.out.printf("[MInfo] Mod id '%s' changed to '%s'.\n", modId, newModId);
        }
    }
}
