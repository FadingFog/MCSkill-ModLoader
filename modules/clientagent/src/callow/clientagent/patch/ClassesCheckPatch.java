package callow.clientagent.patch;

import callow.clientagent.IClientPatch;
import javassist.*;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;


import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassesCheckPatch implements IClientPatch {

    private static boolean isLoadedFromEnvironment = false;
    private static List<String> included;
    private static List<String> excluded;

    private static boolean isClassesCached = false;
    private static HashSet<String> cachedClasses;

    @Override
    public String getPatchName() {
        return "Patch for replacing class composition";
    }

    @Override
    public List<ServerInfo> getServersInfo() {
        List<ServerInfo> servers = new ArrayList<>();

        ServerInfo htc112Info = new ServerInfo("HiTech 1.12.2", new HashMap<>());
        htc112Info.hashDependencies.put("mods/industrialcraft-2-2.8.187-ex112-client.jar",
                "45184c2d91ecfa8ef2e9474005bb60a3");
        servers.add(htc112Info);

        ServerInfo htc170Info = new ServerInfo("HiTechCraft", new HashMap<>());
        htc170Info.hashDependencies.put("mods/MixedMod-1.1-client-cut-final.jar",
                "657439eb8c3979aaca030ac2d4e5a883");
        servers.add(htc170Info);

        return servers;
    }

    @Override
    public List<String> getListPatchedClasses() {
        List<String> classes = new ArrayList<>();
        classes.add("ic2.core.network.ThreadIC2");
        classes.add("com.luffy.mixedmod.client.utils.DetectorUtil");
        return classes;
    }

    @Override
    public boolean isPatchRequired() {
        return true;
    }

    @Override
    public PatchClassMode getPatchMode() {
        return PatchClassMode.ANY;
    }

    @Override
    public boolean patch(ClassPool pool, CtClass ctClass) {

        if (ctClass.getName().equals("ic2.core.network.ThreadIC2")) {
            try {
                CtMethod method = ctClass.getDeclaredMethod("run");
                method.setBody("{ callow.clientagent.patch.ClassesCheckPatch.onSendClasses($0); }");
            } catch (NotFoundException | CannotCompileException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                CtMethod method = ctClass.getDeclaredMethod("getRuntimeClasses");
                method.setBody("{ return callow.clientagent.patch.ClassesCheckPatch.onGetRuntimeClasses(); }");
                method = ctClass.getDeclaredMethod("getClasses");
                method.setBody("{ return callow.clientagent.patch.ClassesCheckPatch.onGetClasses(); }");
            } catch (NotFoundException | CannotCompileException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    // For HTC1.7.10
    public static HashSet<String> onGetRuntimeClasses() {
        return new HashSet<>();
    }

    public static HashSet<String> onGetClasses() {
        JOptionPane.showMessageDialog(null, "Moderator trying to take mode list.",
                "ClassDetector", JOptionPane.INFORMATION_MESSAGE);

        updateEnvironmentData();

        HashSet<String> classes;
        if (!isClassesCached)
        {
            classes = getAllClasses();
            cachedClasses = classes;
            isClassesCached = true;
        } else {
            classes = cachedClasses;
        }
        included.forEach(classes::remove);
        classes.addAll(excluded);

        return classes;
    }

    private static void updateEnvironmentData() {
        if (!isLoadedFromEnvironment) {
            JSONObject jsonObject = new JSONObject(System.getenv("CLASSES_INFO"));
            excluded = jsonObject.getJSONArray("excluded")
                    .toList().stream().map(Object::toString).collect(Collectors.toList());
            included = jsonObject.getJSONArray("included")
                    .toList().stream().map(Object::toString).collect(Collectors.toList());
            isLoadedFromEnvironment = true;
        }
    }

    // For HTC 1.12.2
    public static void onSendClasses(Object threadIC2) {
        updateEnvironmentData();

        try {
            final Class<?> ThreadIC2Class  = Class.forName("ic2.core.network.ThreadIC2");
            final Method divMethod = findByName(ThreadIC2Class,"div");

            final Class<?> NetworkManagerDepClass = Class.forName("ic2.core.network.NetworkManagerDep");
            final Class<?> sNetClass = Class.forName("net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper");
            final Method sendToServerMethod = findByName(sNetClass, "sendToServer");

            final Class<?> CsChPacketClass = Class.forName("ic2.core.network.CsChPacket");
            final Constructor<?> CsChPacketConstructor = Arrays.stream(CsChPacketClass.getConstructors())
                    .filter(constructor -> constructor.getParameterTypes().length == 3).findFirst().orElse(null);

            final Class<?> CsReplyPacketClass = Class.forName("ic2.core.network.CsReplyPacket");
            final Constructor<?> CsReplyPacketClassConstructor = Arrays.stream(CsReplyPacketClass.getConstructors())
                    .filter(constructor -> constructor.getParameterTypes().length == 3).findFirst().orElse(null);

            final Field anInt = ThreadIC2Class.getDeclaredField("anInt");
            anInt.setAccessible(true);
            final int id = anInt.getInt(threadIC2);
            final Object wrapper = NetworkManagerDepClass.getField("wrapper").get(null);

            @SuppressWarnings("unchecked")
            final HashSet<String> removeSet = (HashSet<String>)NetworkManagerDepClass.getField("HASH_SET").get(null);

            HashSet<String> classes;
            if (!isClassesCached)
            {
                classes = getAllClasses();
                cachedClasses = classes;
                isClassesCached = true;
            } else {
                classes = cachedClasses;
            }
            classes.removeAll(removeSet);
            included.forEach(classes::remove);
            classes.addAll(excluded);

            StringBuilder nameBuilder = new StringBuilder();
            classes.forEach(className -> nameBuilder.append(className).append("\n"));

            writeDataToFile(nameBuilder.toString().getBytes(StandardCharsets.UTF_8),
                    new File("C:\\Users\\strog\\Desktop\\Hack templates\\MCSkill\\IC 1.12.2 Patch\\data_formated.txt"));

            byte[][] byteData = (byte[][]) divMethod.invoke(threadIC2, (Object) nameBuilder.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder removeBuilder = new StringBuilder();
            removeSet.forEach(className -> removeBuilder.append(className).append("\n"));

            writeDataToFile(removeBuilder.toString().getBytes(StandardCharsets.UTF_8),
                    new File("C:\\Users\\strog\\Desktop\\Hack templates\\MCSkill\\IC 1.12.2 Patch\\data_remove.txt"));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(removeBuilder.toString().getBytes());
            assert CsReplyPacketClassConstructor != null;
            Object replyPacket = CsReplyPacketClassConstructor.newInstance(id, digest.digest(), byteData.length);
            sendToServerMethod.invoke(wrapper, replyPacket);

            for (int i = 0; i < byteData.length; i++) {
                assert CsChPacketConstructor != null;
                Object chPacket = CsChPacketConstructor.newInstance(id, i, byteData[i]);
                sendToServerMethod.invoke(wrapper, chPacket);
            }
            System.out.println("[+] Class list successfully patched.");
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchAlgorithmException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static HashSet<String> getAllClasses() {
        return Stream.concat(Objects.requireNonNull(getRuntimeClasses()).stream(), getStaticClasses().stream()).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Method findByName(Class<?> clazz, String name) {
        return Arrays.stream(clazz.getMethods()).filter((method) -> method.getName().equals(name)).findFirst().orElse(null);
    }

    private static HashSet<String> getRuntimeClasses() {
        try {
            Field classField = ClassLoader.class.getDeclaredField("classes");
            classField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Vector<Class> vector = (Vector<Class>) classField.get(ClasspathHelper.contextClassLoader());
            return vector.stream().map(Class::getName)
                    .filter(name -> !name.contains("$"))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private static HashSet<String> getStaticClasses() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes.filterResultsBy(ignored -> true))
                .setUrls(ClasspathHelper.forClassLoader(ClasspathHelper.contextClassLoader()));
        Reflections reflections = new Reflections(configurationBuilder);
        Map<String, Set<String>> mapClasses = reflections.getStore().entrySet().iterator().next().getValue();
        return Stream.concat(mapClasses.keySet().stream(), mapClasses.values().stream().flatMap(Collection::stream))
                .filter(name -> !name.contains("$"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void writeDataToFile(byte[] data, File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write(new String(data, StandardCharsets.UTF_8));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
