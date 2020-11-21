package callow.common;

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
    private final IClassPatcher[] patchers;

    public PatchInjector(IClassPatcher[] patchers, boolean loadDirs) {
        this.patchers = patchers;
        pool = ClassPool.getDefault();
        if (loadDirs)
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
