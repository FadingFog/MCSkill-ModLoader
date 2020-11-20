package agent.core;

import javassist.CtClass;
import javassist.CtMethod;
import o.*;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

public class Callbacks {
    public static final List<String> excludeList = new ArrayList<>();

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

    public static void onSendModLog(List<Object> modContainers, Object targetBuffer, String prefix) {
        try {
            final Class<?> ByteBufClass = Class.forName("io.netty.buffer.ByteBuf");

            final Class<?> ModContainerClass = Class.forName(prefix + ".fml.common.ModContainer");
            final Method MCGetSourceMethod = ModContainerClass.getMethod("getSource");

            final Class<?> HandshakeMessageClass = Class.forName(prefix + ".fml.common.network.handshake.FMLHandshakeMessage");
            final Class<?>[] HMNestedClasses = HandshakeMessageClass.getDeclaredClasses();
            final Optional<Class<?>> ModListMessageClassOpt = Arrays.stream(HMNestedClasses)
                    .filter(clazz -> clazz.getSimpleName().equals("ModList"))
                    .findFirst();
            if (!ModListMessageClassOpt.isPresent())
                throw new ClassNotFoundException("ModList");
            final Class<?> ModListMessageClass = ModListMessageClassOpt.get();

            final Constructor<?> MLConstructor = ModListMessageClass.getConstructor(List.class);
            final Method MLToBytesMethod = ModListMessageClass.getMethod("toBytes", ByteBufClass);
            final Method MLToAsStringMethod = ModListMessageClass.getMethod("modListAsString");

            JSONObject modeChangeList = new JSONObject(System.getenv("MODS_HANDSHAKE_EXCLUDED"));
            List<Object> newModContainers = new ArrayList<>();
            for (Object modContainer: modContainers) {
                File sourceFile = (File) MCGetSourceMethod.invoke(modContainer);
                if (!modeChangeList.has(sourceFile.getName()))
                    newModContainers.add(modContainer);
                else
                    System.out.printf("[+] Skipping '%s' file.\n", sourceFile.getName());
            }

            Object MLMessage = MLConstructor.newInstance(newModContainers);
            String modifiedMods = (String)MLToAsStringMethod.invoke(MLMessage);
            MLToBytesMethod.invoke(MLMessage, targetBuffer);

            System.out.println("[+] Mod list was successfully modified.");
            System.out.println("[+] New ModList: " + modifiedMods);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            System.out.printf("[-] Problem with: %s\n", e.getMessage());
        }
    }
}
