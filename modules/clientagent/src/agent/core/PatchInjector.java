package agent.core;

import agent.core.patch.ClientStartPatcher;
import agent.core.patch.HWIdPatcher;
import agent.core.patch.HandshakePatcher;
import agent.core.patch.IClassPatcher;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.stream.Stream;

public class PatchInjector implements ClassFileTransformer {

    private final ClassPool pool;
    private final IClassPatcher[] patchers = { new ClientStartPatcher(), new HandshakePatcher(), new HWIdPatcher() };

    PatchInjector() {
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
        byte[] classBytes = new byte[0];

        CtClass currentClass;
        try {
            currentClass = pool.get(className.replace('/', '.'));
        } catch (NotFoundException e) {
            return classBytes;
        }

        for (IClassPatcher patcher: patchers) {
            if (patcher.patch(pool, currentClass))
                try {
                    classBytes = currentClass.toBytecode();
                    System.out.println("[+] Patch injected: " + patcher.getClass().getSimpleName());
                } catch (CannotCompileException | IOException e) {
                    e.printStackTrace();
                }
        }
        return classBytes;
    }

}
