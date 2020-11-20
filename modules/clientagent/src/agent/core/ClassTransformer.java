package agent.core;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ClassTransformer implements ClassFileTransformer {

    private final ClassPool pool;
    ClassTransformer() {
        pool = ClassPool.getDefault();
        try (Stream<Path> paths = Files.walk(Paths.get(""))) {
            paths
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            pool.appendPathList(path.toString() + "\\*");
                        } catch (NotFoundException e) {
                            System.out.println("[-] Failed to add dir to ClassPool: " + path.toString());
                        }
                    });
        } catch (Exception e) {
            System.out.println("[-] Failed add paths in ClassPool.");
            e.printStackTrace();
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        className = className.replace('/', '.');
        byte[] classBytes = new byte[0];

        CtClass currentClass;
        try {
            currentClass = pool.get(className);
        } catch (NotFoundException e) {
            return classBytes;
        }

        switch (className) {
            case "cpw.mods.fml.common.network.handshake.FMLHandshakeCodec":
            case "net.minecraftforge.fml.common.network.handshake.FMLHandshakeCodec":
                String prefix = "net.minecraftforge";
                if (className.startsWith("cpw.mods"))
                    prefix = "cpw.mods";
                try {
                    CtMethod method = currentClass.getDeclaredMethod("encodeInto");

                    method.insertBefore(String.format(
                            "if ($2.getClass().equals(%s.fml.common.network.handshake.FMLHandshakeMessage.ModList.class)) " +
                            "{ agent.core.Callbacks.onSendModLog(%s.fml.common.Loader.instance().getActiveModList(), $3, \"%s\"); return; }",
                            prefix, prefix, prefix));

                    classBytes = currentClass.toBytecode();
                    System.out.println("[+] FMLCodec was patched.");
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    System.out.println("[-] FMLCodec patch process failed.");
                    e.printStackTrace();
                }
                break;

            // Minecraft Client run
            case "o.AUx":
                try {
                    CtMethod method = currentClass.getDeclaredMethod("main");
                    method.setBody("{ Object[] params = agent.core.Callbacks.onRunningClient($1); o.AUx.aux((o.AUX)params[0], (o.aUX)params[1]); }");

                    System.out.println("[+] ClientRun | AUx.main(): Callback was installed.");

                    classBytes = currentClass.toBytecode();

                } catch (NotFoundException | CannotCompileException | IOException e) {
                    System.out.println("[-] ClientRun | AUx.main(): Callback install failed.");
                    e.printStackTrace();
                }
                break;
        }

        return classBytes;
    }

}
